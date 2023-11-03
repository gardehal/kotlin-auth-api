package grd.kotlin.authapi.settings

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration
@ConfigurationProperties(prefix = "server")
open class Server
{
    var host: String? = null
    var port: String? = null
    var portInt: Int? = null
    var authenticationHeader: String? = null

    init
    {
        portInt = port?.toInt()
    }
}
