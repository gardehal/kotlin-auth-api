package grd.kotlin.authapi.logging

import grd.kotlin.authapi.exceptions.LoggingException
import grd.kotlin.authapi.exceptions.NotImplementedException
import grd.kotlin.authapi.extensions.isNull
import grd.kotlin.authapi.extensions.join
import grd.kotlin.authapi.settings.Settings
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.io.File
import java.time.Duration
import jakarta.annotation.PostConstruct
import kotlin.reflect.KFunction1
import kotlin.reflect.KFunction2
import kotlin.reflect.KProperty

@Service
class TextLogService: ILogService
{
    @Autowired
    lateinit var settings: Settings
    @Autowired
    lateinit var logUtilService: LogUtilService
    @Autowired
    lateinit var logFileUtilService: LogFileUtilService

    lateinit var declaredLogLevel: LogLevel

    // Not used but needed for interface
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
        logFileUtilService.disableLogRotation = settings.logging.disableLogRotation!!
        logFileUtilService.logFileRotatePeriod = Duration.ofDays(settings.logging.logFileRotatePeriodInDays!!)
        logFileUtilService.logFileDirectory = settings.logging.logFileDirectory!!
        logFileUtilService.logFilePrefix = settings.logging.logFilePrefix!!
        logFileUtilService.logFileExtension = settings.logging.logFileExtension!!
    }

    /**
     * Save log text to file.
     * @param logText String to save.
     * @return Boolean result.
     * @throws none
     **/
    fun saveLog(logText: String): Boolean
    {
        val file = logFileUtilService.getLatestLogFile()
        file.writeText(logText)

        return true
    }

    override fun logEvent(logLevel: LogLevel, event: String, userId: String?, itemId: String?): LogEvent?
    {
        if(declaredLogLevel.value < 1 || declaredLogLevel.value > logLevel.value)
            return null

        val logEvent = logUtilService.createLogEvent(event, userId, itemId)
        val logText = getEventString(logEvent)
        if(saveLog(logText))
            return logEvent

        throw LoggingException("Failed logging: $logEvent.")
    }

    override fun logChanges(logLevel: LogLevel, before: Any?, after: Any?, itemId: String, editorId: String, automatedChange: Boolean, changes: List<KProperty<*>>?): LogHead?
    {
        if(declaredLogLevel.value < 1 || declaredLogLevel.value > logLevel.value)
            return null

        try
        {
            var incomingChanges = changes
            if(incomingChanges.isNull())
                incomingChanges = logUtilService.getChanges(before, after)

            val logHead = logUtilService.createLogHeadAndLines(before, after, itemId, editorId, automatedChange, incomingChanges!!)
            val logText = getLogChangesString(logHead)
            if(saveLog(logText))
                return logHead
        }
        catch(e: Exception)
        {
            return null
        }

        return null
    }

    override fun getLogEvents(queryString: String): List<LogEvent>
    {
        throw NotImplementedException("Get is not enabled for LogSink.TEXT_FILE")
    }

    override fun getLogChanges(queryString: String): List<LogHead>
    {
        throw NotImplementedException("Get is not enabled for LogSink.TEXT_FILE")
    }

    override fun censureLogChanges(propertyNames: List<String>, itemId: String): List<String>
    {
        val fileNames = mutableListOf<String>()
        val files = logFileUtilService.getAllLogFiles()
        for(file in files)
        {
            if(!file.readText().contains("itemId:${itemId}"))
                continue

            censureLogChangesTextFile(file, propertyNames, itemId)
            fileNames.add(file.name)
        }

        return fileNames
    }

    /**
     * Censure [propertyNames] for items with [itemId] in given text log file.
     * @param file to censure.
     * @param propertyNames names of properties to censure.
     * @param itemId ID if item to censure.
     * @return File updated with censured text.
     * @throws none
     **/
    fun censureLogChangesTextFile(file: File, propertyNames: List<String>, itemId: String): File
    {
        // TODO could probably be improved
        val censuredText = "<censured>"
        val lines = file.readLines().toMutableList()
        for(i in lines.indices)
        {
            val line = lines[i]
            if(!line.contains("itemId:${itemId}"))
                continue

            var j = i
            while(true)
            {
                val propertyLine = lines[++j]
                if(!propertyLine.startsWith("\t") || (j + 1) >= lines.size)
                    break

                for(property in propertyNames)
                {
                    if(propertyLine.contains("\t${property}: "))
                    {
                        lines[j] = getLogLinesPropertyString(property, censuredText, censuredText)
                    }
                }
            }
        }

        file.writeText(lines.join("\n") ?: "")
        return file
    }

    /**
     * Get logged event as a string for a plaintext log file.
     * Format:
     * "ID REGISTERED EVENT (userId:USERID, itemId:ITEMID)"
     * @param logEvent LogEvent to log.
     * @return String of [logEvent].
     * @throws none
     **/
    fun getEventString(logEvent: LogEvent): String
    {
        val newLine = "\r\n"
        return "${newLine}${logEvent.registered} id:${logEvent.id} caller:${logEvent.caller} event:${logEvent.event} (userId:${logEvent.userId}, itemId:${logEvent.itemId})"
    }

    /**
     * Get logged chances as a string for a plaintext log file.
     * @param logHead LogHead to log.
     * @return String of [logHead].
     * @throws none
     **/
    fun getLogChangesString(logHead: LogHead): String
    {
        // Note: changing this format requires changes in the method censureChangesText()
        val newLine = "\r\n"
        val automated = if(logHead.automatedChange == true) "automated" else "manual"
        val head = "${newLine}${logHead.registered} id:${logHead.id} operation:${logHead.operation!!.name} itemType:${logHead.itemType!!.name} itemId:${logHead.itemId} editorId:${logHead.editorId} automated:${automated}" +
            " fields changed:[${logHead.fieldsUpdated}], comment:${logHead.comment}"
        var lines = ""
        logHead.logLines!!.forEach { lines += "${newLine}${getLogLinesPropertyString(it.fieldName, it.oldValue, it.newValue)}" }

        return head + lines
    }

    /**
     * Get the string for changes in text for LogLine properties
     * @param propertyName String name of property changed.
     * @param before String property value before change.
     * @param after String property value after change.
     * @return String of changed property.
     * @throws none
     **/
    fun getLogLinesPropertyString(propertyName: String?, before: String?, after: String?): String
    {
        return "\t${propertyName}: $before -> $after"
    }
}
