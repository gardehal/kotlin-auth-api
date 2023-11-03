package grd.kotlin.authapi.settings

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration
@ConfigurationProperties(prefix = "controller")
open class Controller
{
    var users: String? = null
    var metadata: String? = null
    var enums: String? = null
    var ingredients: String? = null
    var recipes: String? = null
    var planner: String? = null
    var scraper: String? = null
}
