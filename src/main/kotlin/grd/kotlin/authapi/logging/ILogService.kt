package grd.kotlin.authapi.logging

import grd.kotlin.authapi.exceptions.NotImplementedException
import kotlin.jvm.Throws
import kotlin.reflect.KFunction1
import kotlin.reflect.KFunction2
import kotlin.reflect.KProperty

interface ILogService
{
    // Needs to be accessible for LogSink.DEFINED, and it's service
    var definedLogEventMethod: KFunction1<LogEvent, LogEvent?>?
    var definedLogChangesMethod: KFunction1<LogHead, LogHead?>?
    var definedGetLogEventMethod: KFunction1<String, List<LogEvent>>?
    var definedGetLogChangesMethod: KFunction1<String, List<LogHead>>?
    var definedCensureMethod: KFunction2<List<String>, String, List<String>>?

    /**
     * Log an event, such as a failure or debug info, with options to include ID of user and item related.
     * @param logLevel LogLevel to log to.
     * @param event String message or details of event.
     * @param userId String optional ID of user that caused event.
     * @param itemId String optional ID of item or entity that caused event.
     * @return LogEvent to be saved, null if logging is disabled though LogLevel in settings.
     * @throws none
     **/
    fun logEvent(logLevel: LogLevel, event: String, userId: String? = null, itemId: String? = null): LogEvent?

    /**
     * Log an event, such as a failure or debug info, with options to include ID of user and item related.
     * @param logLevel LogLevel to log to.
     * @param before Any object before changes were applied.
     * @param after Any object after changes were applied.
     * @param itemId String ID of item that was updated.
     * @param editorId String ID of editor that updated entity.
     * @param automatedChange Boolean was this update caused by automated jobs or scripts?
     * @param changes List<KProperty<*>> optional list of changes to log, this is automatically set if argument is null.
     * @return LogHead to be saved, null if logging is disabled though LogLevel in settings.
     * @throws none
     **/
    fun logChanges(logLevel: LogLevel, before: Any?, after: Any?, itemId: String, editorId: String, automatedChange: Boolean, changes: List<KProperty<*>>? = null): LogHead?

    /**
     * Get LogEvents based on [queryString]. Disabled for LogSink.TEXT_FILE. For LogSink.JSON, the fields searched are id, itemId, userId.
     * @param queryString String to find, this can be an ID or Instant.
     * @return List<LogEvent> of related logs.
     * @throws NotImplementedException if this function is used when it is not implemented due to unavoidable inefficient getting.
     **/
    @Throws(NotImplementedException::class)
    fun getLogEvents(queryString: String): List<LogEvent>

    /**
     * Get LogHeads based on [queryString]. Disabled for LogSink.TEXT_FILE. For LogSink.JSON, the fields searched are id, itemId, editorId.
     * @param queryString String to find, this can be an ID or Instant.
     * @return List<LogHead> of related logs.
     * @throws NotImplementedException if this function is used when it is not implemented due to unavoidable inefficient getting.
     **/
    @Throws(NotImplementedException::class)
    fun getLogChanges(queryString: String): List<LogHead>

    /**
     * Censure values in [propertyNames] LogLines for LogHeads given by [itemId]. [itemId] here is the ID of any entity to be censured.
     * @param propertyNames List<String> of properties to update, such as email, phone, names, etc.
     * @param itemId String ID of item to change fields for.
     * @return List<String> names of logs changed.
     * @throws none
     **/
    fun censureLogChanges(propertyNames: List<String>, itemId: String): List<String>
}
