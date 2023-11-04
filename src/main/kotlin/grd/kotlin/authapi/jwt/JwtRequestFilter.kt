package grd.kotlin.authapi.jwt

import grd.kotlin.authapi.Log
import grd.kotlin.authapi.services.JwtService
import grd.kotlin.authapi.settings.Settings
import jakarta.servlet.FilterChain
import jakarta.servlet.ServletException
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import kotlinx.coroutines.runBlocking
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Primary
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource
import org.springframework.stereotype.Service
import org.springframework.web.filter.OncePerRequestFilter
import java.io.IOException

@Primary
@Service
class JwtRequestFilter : OncePerRequestFilter()
{
    @Autowired
    private lateinit var settings: Settings

    @Autowired
    private lateinit var jwtTokenUtil: JwtUtil

    private lateinit var jwtService: JwtService

    private var repositoryInitiated = false

    @Throws(ServletException::class, IOException::class)
    override fun doFilterInternal(request: HttpServletRequest, response: HttpServletResponse, chain: FilterChain) = runBlocking {
        if(!repositoryInitiated)
        {
            jwtService = JwtService()
            repositoryInitiated = true
        }

        val requestTokenHeader = request.getHeader(settings.server.authenticationHeader)
        var username: String? = null
        var jwtToken: String? = null

        if(requestTokenHeader == null)
        {
            Log.main.info("JWT token was not found with header {header}, {function}", settings.server.authenticationHeader, this.toString())
            chain.doFilter(request, response)
            return@runBlocking
        }
        else if(!requestTokenHeader.startsWith(settings.jwt.tokenPrefix!!))
        {
            val tokenStart = requestTokenHeader.substring(0, 7)
            Log.main.info("JWT token does not begin with prefix {prefix}, {actualTokenStart}, {header}, {function}", settings.jwt.tokenPrefix!!, tokenStart, settings.server.authenticationHeader, this.toString())
            chain.doFilter(request, response)
            return@runBlocking
        }
        else
        {
            try
            {
                // JWT Token is in the form "Bearer <token>". Remove Bearer word and get only the Token
                jwtToken = requestTokenHeader.substring(7)
                username = jwtTokenUtil.getUsernameFromToken(jwtToken)
            }
            catch(e: Exception)
            {
                Log.main.info("JWT token was invalid, {message}, {function}", e.message, this.toString())
//                e.printStackTrace()
            }
        }

        // Once we get the token validate it.
        if(username != null && SecurityContextHolder.getContext().authentication == null)
        {
            val user = jwtService.findByUsernameEmail(username)

            // If token is valid configure Spring Security to manually set authentication
            if(jwtTokenUtil.validateToken(jwtToken!!, user))
            {
                val g = GrantedAuthority { user.role.toString() }
                val gl = listOf(g)

                val usernamePasswordAuthenticationToken = UsernamePasswordAuthenticationToken(user, user.id, gl)
                usernamePasswordAuthenticationToken.details = WebAuthenticationDetailsSource().buildDetails(request)

                // After setting the Authentication in the context, we specify
                // that the current user is authenticated. So it passes the Spring Security Configurations successfully.
                SecurityContextHolder.getContext().authentication = usernamePasswordAuthenticationToken
            }
        }

        chain.doFilter(request, response)
    }
}