package grd.kotlin.authapi.settings

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration
@ConfigurationProperties(prefix = "project")
open class Project
{
    var basePackage: String? = null
    var creator: String? = null
    var url: String? = null
    var email: String? = null
    var title: String? = null
    var desc: String? = null
    var version: String? = null
    var isTest: String? = null
    var isTestBool: Boolean? = null
    var resourceDir: String? = null

    init
    {
        isTestBool = isTest.toBoolean()
    }
}
