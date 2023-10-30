package grd.kotlin.authapi.services

import grd.kotlin.authapi.exceptions.ArgumentException
import grd.kotlin.authapi.exceptions.DatabaseErrorException
import grd.kotlin.authapi.exceptions.DuplicateException
import grd.kotlin.authapi.exceptions.NotFoundException
import grd.kotlin.authapi.extensions.isNull
import grd.kotlin.authapi.logging.LogLevel
import grd.kotlin.authapi.repositories.FirebaseRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import org.springframework.data.repository.NoRepositoryBean
import org.springframework.stereotype.Component
import java.time.Instant
import java.util.*
import javax.annotation.PostConstruct
import kotlin.collections.ArrayList

@Component
@NoRepositoryBean
open class BaseService<TEntity: Any>(final var tClass: Class<TEntity>?, var validateClass: Boolean?)
{
    @Autowired
    lateinit var utilityService: UtilityService

    @Autowired
    lateinit var logService: LogService

    var repository = FirebaseRepository(tClass)

    // If validateClass, TEntity must contain the following properties: id, added, deleted as String?
    val idPropertyName = "id"
    val addedPropertyName = "added"
    val deletedPropertyName = "deleted"

    val addFailedMessage = "Failed to add entity."
    val duplicateExceptionMessage = "Failed to add entity; duplicate."
    val notFoundMessage = "No such entity."
    val updateFailedMessage = "Failed to update entity."
    val deleteFailedMessage = "Failed to delete entity."
    val restoreFailedMessage = "Failed to restore entity."
    val removeFailedMessage = "Failed to remove entity."
    val deleteDeletedFailedMessage = "Entity is already deleted."
    val notDeletedFailedMessage = "Entity is not deleted."

    var disableLogs = false

    /**
     * Initialize class:
     * - validateEntity to validate that expected properties exist
     * @return none
     * @throws none
     **/
    @PostConstruct
    fun initialize()
    {
        if(tClass != null)
            validateEntity(tClass!!)

        // Logging
//        logService.disableLogs = disableLogs
//        logService.logSink = LogSink.TEXT_FILE
//        logService.logFileDirectory = "./logs"
//        logService.logFilePrefix = "AuthApi"
    }

    /**
     * Get a new ID.
     * @return String new ID
     * @throws none
     **/
    fun getNewId(): String
    {
        return UUID.randomUUID().toString()
    }

    /**
     * Add an entity.
     * @param entity to add
     * @param editorId whoever called for an update
     * @return TEntity? added entity added, else null
     * @throws DuplicateException on duplicate found in DB
     * @throws DatabaseErrorException on failure to save to DB
     **/
    @Throws(DuplicateException::class, DatabaseErrorException::class)
    fun add(entity: TEntity, editorId: String, automatedChange: Boolean = false): TEntity
    {
        updateProperty(entity, addedPropertyName, Instant.now().toString())
        updateProperty(entity, deletedPropertyName, null)

        val id = utilityService.getReflection(entity, idPropertyName)?.toString() ?: getNewId()
        val result = repository.save(id, entity, asNew = true) ?:
            throw DatabaseErrorException(addFailedMessage)

        val dbId = utilityService.getReflection(entity, idPropertyName)?.toString() ?: getNewId()
        logService.logger.logChanges(LogLevel.INFORMATION, null, result, dbId, editorId, automatedChange)
        return result
    }

    /**
     * Check if entity exists, even if it is soft-deleted.
     * @param id of entity to check
     * @return Boolean true if entity exists (even soft-deleted), else false
     * @throws none
     **/
    fun exists(id: String): Boolean
    {
        return try
        {
            get(id, includeSoftDeleted = true)
            true
        }
        catch(e: NotFoundException)
        {
            false
        }
    }

    /**
     * Check if entity is soft-deleted.
     * @param id of entity to check
     * @return Boolean true if soft-deleted, false if removed or not soft-deleted
     * @throws none
     **/
    fun isDeleted(id: String): Boolean
    {
        return try
        {
            val entity = get(id, includeSoftDeleted = true)
            isSoftDeleted(entity)
        }
        catch(e: NotFoundException)
        {
            false
        }
    }

    /**
     * Get an entity by ID.
     * @param id of entity to get
     * @param includeSoftDeleted should soft-deleted entities be included?
     * @return TEntity if entity was found, else null
     * @throws NotFoundException if entity does not exist
     **/
    @Throws(NotFoundException::class)
    fun get(id: String, includeSoftDeleted: Boolean = false): TEntity
    {
        val entity = repository.findById(id)?: throw NotFoundException(notFoundMessage)
        if(!includeSoftDeleted && isSoftDeleted(entity))
            throw NotFoundException(notFoundMessage)

        return entity
    }

    /**
     * Get a random entity.
     * @param includeSoftDeleted should soft-deleted entities be included?
     * @return TEntity if entity was found, else null
     * @throws NotFoundException if entity does not exist
     **/
    @Throws(NotFoundException::class)
    fun getRandom(includeSoftDeleted: Boolean = false): TEntity
    {
        val all = getAll().toList()
        if(all.isEmpty())
            throw NotFoundException("No entities found.")

        val random = (Math.random() * (all.count())).toInt()
        return all[random]
    }

    /**
     * Query entities in database.
     * @param expression, for example lambda like { e: Query -> e.whereEqualTo("username", "test")
     * @return List<TEntity> of result
     * @throws none
     **/
    fun <T, R> getQueried(expression: (T) -> R): List<TEntity>
    {
        return repository.getQueried(expression)
    }

    /**
     * Query entities in database.
     * @param expression, for example lambda like { e: Query -> e.whereEqualTo("username", "test")
     * @param pageable to return entities in
     * @return Page<TEntity> of result
     * @throws NotFoundException if entities were not found
     **/
    @Throws(NotFoundException::class)
    fun <T, R> getQueried(expression: (T) -> R, pageable: Pageable): Page<TEntity>
    {
        return repository.getQueried(expression, pageable)
    }

    /**
     * Get all entities.
     * @param includeSoftDeleted should soft-deleted entities be included?
     * @return Iterable<TEntity> of all entities found
     * @throws NotFoundException if entities were not found
     **/
    @Throws(NotFoundException::class)
    fun getAll(includeSoftDeleted: Boolean = false): Iterable<TEntity>
    {
        val entities = repository.findAll()
        if(includeSoftDeleted)
            return entities

        val result = ArrayList<TEntity>()
        for(entity in entities)
            if(!isSoftDeleted(entity))
                result.add(entity)

        return result
    }

    /**
     * Get all entities.
     * @param includeSoftDeleted should soft-deleted entities be included?
     * @param pageable to return entities in
     * @return Iterable<TEntity> of all entities found
     * @throws NotFoundException if entities were not found
     **/
    @Throws(NotFoundException::class)
    fun getAll(includeSoftDeleted: Boolean = false, pageable: Pageable): Page<TEntity>
    {
        val entities = getAll(includeSoftDeleted)

        return PageImpl(entities.toList(), pageable, entities.count().toLong())
    }

    /**
     * Get all IDs in database.
     * @param includeSoftDeleted should soft-deleted entities be included?
     * @return Iterable<String> of all IDs found
     * @throws NotFoundException if entities were not found
     **/
    @Throws(NotFoundException::class)
    fun getAllIds(includeSoftDeleted: Boolean = false): Iterable<String>
    {
        val entities = repository.findAll()
        if(includeSoftDeleted)
            return entities.map { utilityService.getReflection(it, idPropertyName)!!.toString() }

        val result = ArrayList<String>()
        for(entity in entities)
            if(!isSoftDeleted(entity))
                result.add(utilityService.getReflection(entity, idPropertyName)!!.toString())

        return result
    }

    /**
     * Update an entity.
     * @param entity to update
     * @param editorId whoever called for an update
     * @param includeSoftDeleted should soft-deleted entities be included?
     * @return TEntity updated entity if something was found and then updated
     * @throws NotFoundException if entity does not exist
     * @throws DatabaseErrorException on failure to update
     **/
    @Throws(NotFoundException::class, DatabaseErrorException::class)
    fun update(entity: TEntity, editorId: String, includeSoftDeleted: Boolean = false, automatedChange: Boolean = false): TEntity
    {
        val id = utilityService.getReflection(entity, idPropertyName)!!.toString()
        val before = get(id, includeSoftDeleted) // Throws NotFoundException on no entity

        val result = repository.save(id, entity, asNew = false) ?:
            throw DatabaseErrorException(updateFailedMessage)

        val dbId = utilityService.getReflection(entity, idPropertyName)?.toString() ?: getNewId()
        logService.logger.logChanges(LogLevel.INFORMATION, before, result, dbId, editorId, automatedChange)
        return result
    }

    /**
     * Patch an entity given by [id] applying the changes from [json]
     * @param id of entity to apply patch to
     * @param json of changes to apply
     * @return Entity updated with changes, not saved to database
     * @throws NotFoundException on entity not found
     * @throws ArgumentException on problems with JSON or merge patch problems
     **/
    @Throws(NotFoundException::class, ArgumentException::class)
    fun patch(id: String, json: String): TEntity
    {
        val entity = get(id)
        val ignoreProperties = listOf("id", "added", "deleted")
        val jsonPatch = utilityService.convertJsonToJsonPatch(json, ignoreProperties)
        try
        {
            return utilityService.mergePatch(entity, tClass!!, jsonPatch)
        }
        catch(e: Exception)
        {
            e.printStackTrace()
            throw ArgumentException("There was an issue with merging the changes with the entity.")
        }
    }

    /**
     * Soft-delete an entity.
     * @param id of entity to delete
     * @param editorId whoever called for an update
     * @return TEntity updated entity if something was found and then deleted
     * @throws ArgumentException on attempt to delete already soft-deleted entity
     * @throws NotFoundException if entity does not exist
     * @throws DatabaseErrorException on failure to delete
     **/
    @Throws(ArgumentException::class, NotFoundException::class, DatabaseErrorException::class)
    fun delete(id: String, editorId: String, automatedChange: Boolean = false): TEntity
    {
        val entity = get(id, includeSoftDeleted = false) // Throws NotFoundException on no entity

        if(isSoftDeleted(entity))
            throw ArgumentException(deleteDeletedFailedMessage)

        updateProperty(entity, deletedPropertyName, Instant.now().toString())

        val result = repository.save(id, entity, asNew = false) ?:
            throw DatabaseErrorException(deleteFailedMessage)

        val dbId = utilityService.getReflection(entity, idPropertyName)?.toString() ?: getNewId()
        logService.logger.logChanges(LogLevel.INFORMATION, entity, result, dbId, editorId, automatedChange)
        return result
    }

    /**
     * Restore a soft-deleted entity.
     * @param id of entity to update
     * @param editorId whoever called for an update
     * @return TEntity updated entity if something was found and then restored
     * @throws ArgumentException on attempt to restore non-soft-deleted entity
     * @throws NotFoundException if entity does not exist
     * @throws DatabaseErrorException on failure to restore
     **/
    @Throws(ArgumentException::class, NotFoundException::class, DatabaseErrorException::class)
    fun restore(id: String, editorId: String, automatedChange: Boolean = false): TEntity
    {
        val entity = get(id, includeSoftDeleted = true) // Throws NotFoundException on no entity

        if(!isSoftDeleted(entity))
            throw ArgumentException(notDeletedFailedMessage)

        updateProperty(entity, deletedPropertyName, null)

        val result = repository.save(id, entity, asNew = false) ?:
            throw DatabaseErrorException(restoreFailedMessage)

        val dbId = utilityService.getReflection(entity, idPropertyName)?.toString() ?: getNewId()
        logService.logger.logChanges(LogLevel.INFORMATION, entity, result, dbId, editorId, automatedChange)
        return result
    }

    /**
     * Permanently delete/remove an entity.
     * @param id of entity to remove
     * @param includeSoftDeleted should soft-deleted entities be included?
     * @return TEntity updated entity if something was found and then removed
     * @throws NotFoundException if entity does not exist
     * @throws DatabaseErrorException on failure to remove
     **/
    @Throws(NotFoundException::class, DatabaseErrorException::class)
    fun remove(id: String, editorId: String, includeSoftDeleted: Boolean = false, automatedChange: Boolean = false): TEntity
    {
        val entity = get(id, includeSoftDeleted) // Throws NotFoundException on no entity

        val result = repository.deleteById(id)
        if(result.isNull())
            throw DatabaseErrorException(removeFailedMessage)

        val dbId = utilityService.getReflection(entity, idPropertyName)?.toString() ?: getNewId()
        logService.logger.logChanges(LogLevel.INFORMATION, entity, null, dbId, editorId, automatedChange)
        return entity
    }

    // Private methods (not made private due to testing) ---------------------------------------------------------------------------------------------------------------------------

    /**
     * Validate entity if it contains expected properties.
     * @param tClass to check
     * @return none
     * @throws ArgumentException on any issues with entity
     **/
    @Throws(ArgumentException::class)
    fun validateEntity(tClass: Class<TEntity>)
    {
        if(validateClass != true)
            return

        val message = "Entity $tClass %s-property was not valid."

        if(!utilityService.hasField(tClass, idPropertyName))
            throw ArgumentException(message.format(idPropertyName))
        if(!utilityService.hasField(tClass, addedPropertyName))
            throw ArgumentException(message.format(addedPropertyName))
        if(!utilityService.hasField(tClass, deletedPropertyName))
            throw ArgumentException(message.format(deletedPropertyName))
    }

    /**
     * Update a property for TEntity.
     * @param entity to update
     * @param propertyName name of property to update
     * @param change to apply to property
     * @return copy of updated map
     * @throws NoSuchFieldException on no attempting to set property not existing
     **/
    @Throws(NoSuchFieldException::class)
    fun updateProperty(entity: TEntity, propertyName: String, change: String?): String?
    {
        // If validateClass is null or false, return since there is no property to set
        if(validateClass != true)
            return null

        utilityService.setReflection(entity, propertyName, change)

        return change
    }

    /**
     * Check if entity is soft-deleted checking deleted property has value (preferably String of Instant when deleted).
     * @param entity to check
     * @return entity is soft-deleted
     * @throws NoSuchFieldException on missing deleted property
     **/
    @Throws(NoSuchFieldException::class)
    fun isSoftDeleted(entity: TEntity): Boolean
    {
        if(validateClass != true)
            return false

        val deleted = utilityService.getReflection(entity, deletedPropertyName) as String?

        return !deleted.isNullOrBlank()
    }
}
