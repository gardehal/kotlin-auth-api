package grd.kotlin.authapi.logging

import grd.kotlin.authapi.annotations.NotLogged
import grd.kotlin.authapi.annotations.Sensitive
import grd.kotlin.authapi.exceptions.ArgumentException
import grd.kotlin.authapi.exceptions.NotImplementedException
import grd.kotlin.authapi.extensions.hasAnnotation
import grd.kotlin.authapi.extensions.isNull
import grd.kotlin.authapi.extensions.join
import grd.kotlin.authapi.models.*
import org.springframework.stereotype.Service
import java.time.Instant
import java.util.*
import kotlin.reflect.KProperty
import kotlin.reflect.full.memberProperties

@Service
class LogUtilService
{
    /**
     * Get the string for changes in text for LogLine properties
     * @return LogEvent result.
     * @throws none
     **/
    fun createLogEvent(event: String, userId: String?, itemId: String?): LogEvent
    {
        val caller = Thread.currentThread().stackTrace[2].toString() // Class, function and line that called this function
        return LogEvent(UUID.randomUUID().toString(), Instant.now().toString(),
            event, userId, itemId, caller)
    }

    /**
     * Get changes between two objects.
     * @param left Any objects to compare.
     * @param right Any objects to compare.
     * @return List of KProperty that was changed.
     * @throws ArgumentException when arguments are not the same class
     **/
    @Throws(ArgumentException::class)
    fun getChanges(left: Any?, right: Any?): List<KProperty<*>>
    {
        val obj = left ?: right
        if(obj.isNull())
            throw ArgumentException("One or more arguments are null, will not compare.")
        else if(left.isNull())
            return right!!::class.memberProperties.toList()
        else if(right.isNull())
            return left!!::class.memberProperties.toList()

        if(left!!::class.java != right!!::class.java)
            throw ArgumentException("Arguments are not the same class, will not compare.")
        if(obj!!.hasAnnotation(NotLogged::class.java))
            throw ArgumentException("Class is marked as ignored for logging, will not compare.")

        val changes = mutableListOf<KProperty<*>>()
        for(property in obj::class.memberProperties)
        {
            val leftProperty = getProperty(left, property.name)
            val rightProperty = getProperty(right, property.name)

            if(leftProperty == rightProperty
                || property.hasAnnotation(NotLogged::class.java))
                continue

            changes.add(property)
        }

        return changes
    }

    /**
     * Get the operation performed for logging.
     * @param before Any object to log changes for, this is the object before changes are applied.
     * @param after Any object to log changes for, this is the object after changes are applied.
     * @return LogOperation result
     * @throws none
     **/
    fun getOperation(before: Any?, after: Any?): LogOperation
    {
        if(before.isNull())
            return LogOperation.ADDED
        else if(after.isNull())
            return LogOperation.REMOVED

        return LogOperation.EDITED
    }

    /**
     * Get the LogItemType for logging. Note that before and after should be the same type, but there is no check.
     * @param before Any object to log changes for, this is the object before changes are applied.
     * @param after Any object to log changes for, this is the object after changes are applied.
     * @return LogItemType result
     * @throws ArgumentException when both before and after is null
     * @throws NotImplementedException when before or after is of a type that is not implemented
     **/
    fun getLogItemType(before: Any?, after: Any?): LogItemType
    {
        if(before.isNull() && after.isNull())
            throw ArgumentException("Cannot get LogItemType, both arguments are null.")

        val obj = before ?: after
        if(obj!!::class == ApiMetadata::class)
            return LogItemType.DB_METADATA
        else if(obj::class == AUser::class)
            return LogItemType.USER

        throw NotImplementedException("Class was not implemented for logging.")
    }

    /**
     * Get properties updates in list of [changes].
     * @param changes List of changes to log for.
     * @return Comma-separated string of changes or null, if no changes.
     * @throws none
     **/
    fun getPropertiesUpdated(changes: List<KProperty<*>>): String?
    {
        if(changes.isEmpty())
            return null

        return changes.map { it.name }.join(", ")
    }

    /**
     * Get a property named [propertyName] from class [parent].
     * @param parent Object with property to get.
     * @param propertyName String name of property.
     * @return Any property within [parent].[propertyName].
     * @throws TODO probably something
     **/
    //    @Throws(ArgumentException::class)
    fun getProperty(parent: Any, propertyName: String): Any?
    {
        return parent.javaClass.getDeclaredField(propertyName).get(parent)
    }

    /**
     * Get the [parent].[propertyName] as string for logging. If [parent] is null, return null.
     * @param parent Object with property to get.
     * @param propertyName String name of property.
     * @return String of field value.
     * @throws none
     **/
    fun getLogProperty(parent: Any?, propertyName: String): String?
    {
        if(parent.isNull())
            return null

        if(parent!!::class.memberProperties.first { it.name == propertyName }.hasAnnotation(Sensitive::class.java))
            return "<sensitive>"

        val result = getProperty(parent, propertyName)
        return if(result.isNull()) null else result.toString()
    }

    /**
     * Get LogHead/LogLines from [changes].
     * @param before Any object to log changes for, this is the object before changes are applied.
     * @param after Any object to log changes for, this is the object after changes are applied.
     * @param itemId ID of entity updated.
     * @param editorId ID of whoever ordered the change (for scripts etc. it can be the name of script).
     * @param automatedChange Was this change automated by a job or script?
     * @param changes List of changes to log for.
     * @return LogHead and LogLines detailing changes.
     * @throws ArgumentException when [changes] is empty, since no changes will be logged.
     **/
    @Throws(ArgumentException::class)
    fun createLogHeadAndLines(before: Any?, after: Any?, itemId: String, editorId: String, automatedChange: Boolean, changes: List<KProperty<*>>): LogHead
    {
        if(changes.isEmpty())
            throw ArgumentException("Argument changes was empty, will not log any changes.")

        val head = LogHead(
            operation = getOperation(before, after),
            itemType = getLogItemType(before, after),
            itemId = itemId,
            registered = Instant.now().toString(),
            editorId = editorId,
            automatedChange = automatedChange,
            fieldsUpdated = getPropertiesUpdated(changes),
            logLines = mutableListOf(),)

        for(change in changes)
        {
            val line = LogLine(logHeadId = "", // Need to save head before setting. For current setup with noSQL, it's fine since lines is a list in head
                fieldName = change.name,
                oldValue = getLogProperty(before, change.name),
                newValue = getLogProperty(after, change.name),)
            head.logLines!!.add(line)
        }

        return head
    }

    /**
     * Get the string for changes in text for LogLine properties
     * @param propertyName String name of property changed.
     * @param before String property value before change.
     * @param after String property value after change.
     * @return String of changed property.
     * @throws none
     **/
    fun getLogLineChangeString(propertyName: String?, before: String?, after: String?): String
    {
        return "\t${propertyName}: $before -> $after"
    }
}
