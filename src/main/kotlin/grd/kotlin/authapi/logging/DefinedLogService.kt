package grd.kotlin.authapi.logging

import grd.kotlin.authapi.extensions.isNull
import grd.kotlin.authapi.settings.Settings
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import jakarta.annotation.PostConstruct
import kotlin.reflect.KFunction1
import kotlin.reflect.KFunction2
import kotlin.reflect.KProperty

@Service
class DefinedLogService: ILogService
{
    @Autowired
    lateinit var settings: Settings
    @Autowired
    lateinit var logUtilService: LogUtilService

    lateinit var declaredLogLevel: LogLevel

    // Logging to self-defined sink, note that user must assign these as methods if logSink is set to LogSink.DEFINED
    // e.g. "definedLogEventMethod = ::myMethod", "fun myMethod(logEvent: LogEvent): Boolean { [...] return true }"
    override var definedLogEventMethod: KFunction1<LogEvent, LogEvent?>? = null
    override var definedLogChangesMethod: KFunction1<LogHead, LogHead?>? = null
    override var definedGetLogEventMethod: KFunction1<String, List<LogEvent>>? = null
    override var definedGetLogChangesMethod: KFunction1<String, List<LogHead>>? = null
    override var definedCensureMethod: KFunction2<List<String>, String, List<String>>? = null

    /**
     * Initialize LogFileUtilService properties.
     * @return none
     * @throws none
     **/
    @PostConstruct
    fun initialize()
    {
        declaredLogLevel = settings.logging.logLevel!!
    }

    override fun logEvent(logLevel: LogLevel, event: String, userId: String?, itemId: String?): LogEvent?
    {
        if(declaredLogLevel.value < 1 || declaredLogLevel.value > logLevel.value)
            return null

        val logEvent = logUtilService.createLogEvent(event, userId, itemId)
        return definedLogEventMethod!!.invoke(logEvent)
    }

    override fun logChanges(logLevel: LogLevel, before: Any?, after: Any?, itemId: String, editorId: String, automatedChange: Boolean, changes: List<KProperty<*>>?): LogHead?
    {
        if(declaredLogLevel.value < 1 || declaredLogLevel.value > logLevel.value)
            return null

        var incomingChanges = changes
        if(incomingChanges.isNull())
            incomingChanges = logUtilService.getChanges(before, after)

        val logHead = logUtilService.createLogHeadAndLines(before, after, itemId, editorId, automatedChange, incomingChanges!!)
        return definedLogChangesMethod!!.invoke(logHead)
    }

    override fun getLogEvents(queryString: String): List<LogEvent>
    {
        return definedGetLogEventMethod!!.invoke(queryString)
    }

    override fun getLogChanges(queryString: String): List<LogHead>
    {
        return definedGetLogChangesMethod!!.invoke(queryString)
    }

    override fun censureLogChanges(propertyNames: List<String>, itemId: String): List<String>
    {
        return definedCensureMethod!!.invoke(propertyNames, itemId)
    }
}