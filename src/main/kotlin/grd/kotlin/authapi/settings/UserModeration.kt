package grd.kotlin.authapi.settings

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration
@ConfigurationProperties(prefix = "moderation")
open class UserModeration
{
    var disableInactiveAccounts: Boolean? = null
    var disableInactiveAccountsAfterMonths: Int? = null
}
