package grd.kotlin.authapi

import grd.kotlin.authapi.jwt.JwtRequestFilter
import kotlinx.coroutines.runBlocking
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.http.HttpMethod
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter

@Primary
@Configuration
@EnableWebSecurity
open class SecurityConfiguration
{
    @Autowired
    private lateinit var jwtRequestFilter: JwtRequestFilter

    @Bean
    open fun configure(http: HttpSecurity): SecurityFilterChain = runBlocking {
        Log.main.info("Configuring HttpSecurity...", "WebSecurityConfig.configure")

        // Filter JWT requests
        http.addFilterBefore(jwtRequestFilter, UsernamePasswordAuthenticationFilter::class.java)

        http.authorizeHttpRequests() {c -> c.requestMatchers(HttpMethod.GET, "/").permitAll()}

        http
            .authorizeHttpRequests { request ->
                // Redirects, defaults
                request.requestMatchers(HttpMethod.GET, "/").permitAll()
                request.requestMatchers(HttpMethod.GET, "/swagger").permitAll()
                // Swagger GUI
                request.requestMatchers("/v3/api-docs/**").permitAll()
                request.requestMatchers("/swagger-ui/**").permitAll()
                // User
                request.requestMatchers(HttpMethod.GET, "/user/getToken**").permitAll()
                // Metadata
                request.requestMatchers(HttpMethod.GET, "/meta/").permitAll()
                // None - Require any other request have authentication
                request.anyRequest()
                    .authenticated()
//                    .permitAll() // FOR TESTING ONLY
            }
            .cors { }
            .csrf { e -> e.disable() } // Disabled due to JWT
            .sessionManagement { e -> e.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }

        return@runBlocking http.build()
    }
}