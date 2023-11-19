package grd.kotlin.authapi

import grd.kotlin.authapi.settings.Settings
import io.swagger.v3.oas.models.Components
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Contact
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.security.SecurityRequirement
import io.swagger.v3.oas.models.security.SecurityScheme
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
@EnableAutoConfiguration
open class OpenApiConfig
{
    @Autowired
    private lateinit var settings: Settings

    @Bean
    open fun api(): OpenAPI
    {
        println("OpenAPI for ${settings.project.title} on ${settings.server.host}:${settings.server.port} is enabled:")
        println("http://${settings.server.host}:${settings.server.port}/swagger-ui/index.html")

        return OpenAPI()
            .components(Components())
            .info(apiInfo())
            .operationParameters()
    }

    private fun apiInfo(): Info
    {
        val dev = Contact()
        return Info()
            .title(settings.project.title)
            .description(settings.project.desc)
            .version(settings.project.version)
            .contact(dev)
    }

    private fun OpenAPI.operationParameters(): OpenAPI
    {
        val scheme = "bearer"
        val securitySchemeName = "bearerAuth"
        val securityItem = SecurityRequirement().addList(securitySchemeName)
        val component = Components()
            .addSecuritySchemes(securitySchemeName, SecurityScheme()
                .name(securitySchemeName)
                .description("${settings.jwt.tokenPrefix} JWT token")
                .type(SecurityScheme.Type.HTTP)
                .scheme(scheme)
                .bearerFormat("JWT"))

        return this
            .addSecurityItem(securityItem)
            .components(component)
    }
}