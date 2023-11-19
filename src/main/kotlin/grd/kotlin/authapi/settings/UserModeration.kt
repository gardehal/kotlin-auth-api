package grd.kotlin.authapi.settings

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration
@ConfigurationProperties(prefix = "userModeration")
open class UserModeration
{
    var disableInactiveAccounts: Boolean? = null
    var disableInactiveAccountsAfterMonths: Int? = null
}
