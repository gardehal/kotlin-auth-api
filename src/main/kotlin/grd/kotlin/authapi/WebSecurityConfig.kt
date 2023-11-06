package grd.kotlin.authapi

import grd.kotlin.authapi.jwt.JwtRequestFilter
import grd.kotlin.authapi.settings.Settings
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
open class WebSecurityConfig
{
    @Autowired
    private lateinit var settings: Settings

    @Autowired
    private lateinit var jwtRequestFilter: JwtRequestFilter

    @Bean
    open fun configure(http: HttpSecurity): SecurityFilterChain = runBlocking {
        Log.main.info("Configuring HttpSecurity...", "WebSecurityConfig.configure")

        // Filter JWT requests
        http.addFilterBefore(jwtRequestFilter, UsernamePasswordAuthenticationFilter::class.java)

        http
            .authorizeRequests()
                // Redirects, defaults
                .antMatchers(HttpMethod.GET, "/").permitAll()
                .antMatchers(HttpMethod.GET, "/swagger").permitAll()
                // Swagger GUI
                .antMatchers("/v3/api-docs/**").permitAll()
                .antMatchers("/swagger-ui/**").permitAll()
                // User
                .antMatchers(HttpMethod.GET, "/user/getToken**").permitAll()
                // Metadata
                .antMatchers(HttpMethod.GET, "/meta/").permitAll()
                // None - Require any other request have authentication
                .anyRequest()
                    .authenticated()
//                    .permitAll() // TESTING ONLY
            .and()
                .cors()
            .and()
                .csrf()
                    .disable() // Disabled due to JWT usage
//            .and()
                .sessionManagement()
                    .sessionCreationPolicy(SessionCreationPolicy.STATELESS)

        return@runBlocking http.build()
    }
}