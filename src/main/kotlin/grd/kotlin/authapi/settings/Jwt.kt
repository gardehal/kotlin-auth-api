package grd.kotlin.authapi.settings

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration
@ConfigurationProperties(prefix = "jwt")
open class Jwt
{
    var validity: String? = null
    var validityLong: Long? = null
    var issuer: String? = null
    var tokenPrefix: String? = null

    init
    {
        validityLong = validity?.toLong()
    }
}
