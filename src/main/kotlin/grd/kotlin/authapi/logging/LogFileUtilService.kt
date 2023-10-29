package grd.kotlin.authapi.logging

import grd.kotlin.authapi.models.*
import org.springframework.stereotype.Service
import java.io.File
import java.nio.file.Paths
import java.time.Duration
import java.time.Instant
import java.time.format.DateTimeParseException
import java.util.*

@Service
class LogFileUtilService
{
    var disableLogRotation: Boolean = false
    var logFileRotatePeriod: Duration = Duration.ofDays(30)
    var logFileDirectory: String = "./logs"
    var logFilePrefix: String = "MyLogs"
    var logFileExtension: String = "log"
    var logFile: File? = null

    /**
     * Initialize properties.
     * @return none
     * @throws none
     **/
    fun initialize(disableLogRotation: Boolean, logFileRotatePeriod: Duration, logFileDirectory: String, logFilePrefix: String, logFileExtension: String, logFile: File?)
    {
         this.disableLogRotation = disableLogRotation
         this.logFileRotatePeriod = logFileRotatePeriod
         this.logFileDirectory = logFileDirectory
         this.logFilePrefix = logFilePrefix
         this.logFileExtension = logFileExtension
         this.logFile = logFile
    }

    /**
     * Create a new logfile.
     * @return File logfile.
     * @throws none
     **/
    fun getNewLogFile(): File
    {
        val newFileName = getLogFileFilename()
        val file = File(Paths.get(logFileDirectory, newFileName).toString())
        file.appendText("/* New logfile created by LogFileUtilService.getNewLogFile() at ${Instant.now()} */")

        logFile = file
        return logFile!!
    }

    /**
     * Get all logfiles used by this service.
     * @return List<File> of files.
     * @throws none
     **/
    fun getAllLogFiles(): List<File>
    {
        return File(logFileDirectory).listFiles()?.filter { it.name.contains(logFilePrefix) && it.name.contains(".$logFileExtension") }!!
    }

    /**
     * Get the latest available logfile in class.logFilePath with class.logFilePrefix and class.logFileExtension.
     * @return File logfile.
     * @throws none
     **/
    fun getLatestLogFile(): File
    {
        val files = getAllLogFiles()
        if(files.isNotEmpty())
        {
            val latestFile = files.sortedBy { it.name }.last() // Natural sort not ideal, but will work when prefix is the same and with ISO 8601 datetime format

            val latestFileInstant = getLogFileInstant(latestFile.name)
            if(!disableLogRotation && latestFileInstant < Instant.now().minus(logFileRotatePeriod))
                return getNewLogFile()

            logFile = latestFile
            return logFile!!
        }

        return getNewLogFile()
    }

    /**
     * Get the Instant from filename of logfile.
     * @param filename String name of file.
     * @return Instant from [filename].
     * @throws DateTimeParseException when [filename] is not on expected format (":" replaced with "+")
     **/
    @Throws(DateTimeParseException::class)
    fun getLogFileInstant(filename: String): Instant
    {
        val dateTimeString = filename
            .split("${logFilePrefix}_")
            .last()
            .split(".$logFileExtension")
            .first()
            .replace("+",":")
        return Instant.parse(dateTimeString)
    }

    /**
     * Get filename for new logfile.
     * @return String filename.
     * @throws none
     **/
    fun getLogFileFilename(): String
    {
        val now = Instant.now().toString().replace(":", "+")
        return "${logFilePrefix}_${now}.$logFileExtension"
    }
}
