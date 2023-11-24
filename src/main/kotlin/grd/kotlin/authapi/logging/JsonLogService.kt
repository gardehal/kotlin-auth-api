package grd.kotlin.authapi.logging

import com.fasterxml.jackson.databind.ObjectMapper
import grd.kotlin.authapi.extensions.isNull
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
class JsonLogService: ILogService
{
    @Autowired
    lateinit var settings: Settings
    @Autowired
    lateinit var logUtilService: LogUtilService
    @Autowired
    lateinit var logFileUtilService: LogFileUtilService

    private val mapper = ObjectMapper()
    lateinit var declaredLogLevel: LogLevel

    // Not used but needed for interface
    override var definedLogEventMethod: KFunction1<LogEvent, LogEvent?>? = null
    override var definedLogChangesMethod: KFunction1<LogHead, LogHead?>? = null
    override var definedGetLogEventMethod: KFunction1<String, List<LogEvent>>? = null
    override var definedGetLogChangesMethod: KFunction1<String, List<LogHead>>? = null
    override var definedCensureMethod: KFunction2<List<String>, String, List<String>>? = null

    /**
     * Initialize basic properties.
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
     * Initialize structure for logging in JSON file, if file is empty.
     * @param file File to initialize.
     * @return File updated file.
     * @throws none
     **/
    fun initializeJsonFile(file: File): File
    {
        if(file.length() == 0L)
            mapper.writeValue(file, JsonFileLog().initialize())

        return file
    }

    /**
     * Save LogEvent to JSON file.
     * @param logEvent LogEvent to save.
     * @return Boolean result.
     * @throws none
     **/
    fun saveLogEvent(logEvent: LogEvent): Boolean
    {
        val file = logFileUtilService.getLatestLogFile()
        initializeJsonFile(file)

        val logs = mapper.readValue(file.readText(), JsonFileLog::class.java)
        logs.logEvents!!.add(logEvent)
        val jsonLogs = mapper.writeValueAsString(logs)
        file.writeText(jsonLogs)

        return true
    }

    /**
     * Save LogHead to JSON file.
     * @param logHead LogHead to save.
     * @return Boolean result.
     * @throws none
     **/
    fun saveLogHead(logHead: LogHead): Boolean
    {
        val file = logFileUtilService.getLatestLogFile()
        initializeJsonFile(file)

        val logs = mapper.readValue(file.readText(), JsonFileLog::class.java)
        logs.logHeads!!.add(logHead)
        val jsonLogs = mapper.writeValueAsString(logs)
        file.writeText(jsonLogs)

        return true
    }

    override fun logEvent(logLevel: LogLevel, event: String, userId: String?, itemId: String?): LogEvent?
    {
        if(declaredLogLevel.value < 1 || declaredLogLevel.value > logLevel.value)
            return null

        val logEvent = logUtilService.createLogEvent(event, userId, itemId)
        if(saveLogEvent(logEvent))
            return logEvent

        return null
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
            if(saveLogHead(logHead))
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
        val file = logFileUtilService.getLatestLogFile()
        val logs = mapper.readValue(file.readText(), JsonFileLog::class.java)

        return logs.logEvents!!
            .filter { it.id == queryString || it.itemId == queryString || it.userId == queryString }
            .toList()
    }

    override fun getLogChanges(queryString: String): List<LogHead>
    {
        val file = logFileUtilService.getLatestLogFile()
        val logs = mapper.readValue(file.readText(), JsonFileLog::class.java)

        return logs.logHeads!!
            .filter { it.id == queryString || it.itemId == queryString || it.editorId == queryString }
            .toList()
    }

    override fun censureLogChanges(propertyNames: List<String>, itemId: String): List<String>
    {
        val ids = mutableListOf<String>()
        val logFiles = logFileUtilService.getAllLogFiles()
        for(logFile in logFiles)
        {
            val censuredIds = censureLogChangeFiles(logFile, propertyNames, itemId)
            ids.addAll(censuredIds)
        }

        return ids
    }

    /**
     * Censure logs in given JSON file.
     * @param file File with potential logs to censure.
     * @param propertyNames List<String> name of properties to censure.
     * @param itemId String ID of item to censure for.
     * @return List<String> IDs of LogHeads censured.
     * @throws none
     **/
    fun censureLogChangeFiles(file: File, propertyNames: List<String>, itemId: String): List<String>
    {
        val ids = mutableListOf<String>()
        if(file.length() == 0L)
            return ids

        val logs = mapper.readValue(file.readText(), JsonFileLog::class.java)
        val logHeads = logs.logHeads!!
            .filter { it.itemId == itemId }

        for(logHead in logHeads)
        {
            for(logLine in logHead.logLines!!)
            {
                if(propertyNames.contains(logLine.fieldName))
                {
                    logLine.newValue = "<censured>"
                    logLine.oldValue = "<censured>"
                }
            }

            ids.add(logHead.id!!)
        }

        val jsonLogs = mapper.writeValueAsString(logs)
        file.writeText(jsonLogs)
        return ids
    }
}
