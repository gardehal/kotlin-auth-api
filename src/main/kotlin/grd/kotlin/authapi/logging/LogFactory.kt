package grd.kotlin.authapi.logging

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
class LogFactory
{
    @Autowired
    lateinit var textLogService: TextLogService
    @Autowired
    lateinit var jsonLogService: JsonLogService
    @Autowired
    lateinit var definedLogService: DefinedLogService

    fun createFromLogSink(logSink: LogSink): ILogService
    {
        return when(logSink)
        {
            LogSink.TEXT_FILE ->
                textLogService
            LogSink.JSON ->
                jsonLogService
            LogSink.DEFINED ->
                definedLogService
//            else ->
//                throw NotImplementedException("This LogSink is not implemented: $logSink")
        }
    }
}
