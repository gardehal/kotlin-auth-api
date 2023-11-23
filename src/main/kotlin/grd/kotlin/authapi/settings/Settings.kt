package grd.kotlin.authapi.settings

import jakarta.annotation.PostConstruct
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration
@ConfigurationProperties
open class Settings
{
    var server: Server = Server()
    var project: Project = Project()
    var jwt: Jwt = Jwt()
    var logging: Logging = Logging()
    var userModeration: UserModeration = UserModeration()

    var env: Env = Env()

    @PostConstruct
    fun initialize()
    {
        val directory = project.resourceDir!!
        env.initialize(directory)
    }
}
