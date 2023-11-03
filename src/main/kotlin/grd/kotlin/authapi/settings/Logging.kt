package grd.kotlin.authapi.settings

import grd.kotlin.authapi.logging.LogLevel
import grd.kotlin.authapi.logging.LogSink
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration
@ConfigurationProperties(prefix = "logging")
open class Logging
{
    var logSink: LogSink? = null
    var logLevel: LogLevel? = null
    var disableLogs: Boolean? = null
    var disableLogRotation: Boolean? = null
    var logFileRotatePeriodInDays: Long? = null
    var logFileDirectory: String? = null
    var logFilePrefix: String? = null
    var logFileExtension: String? = null
}
