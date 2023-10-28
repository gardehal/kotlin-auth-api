package grd.kotlin.authapi.repositories

import grd.kotlin.authapi.exceptions.ArgumentException
import grd.kotlin.authapi.exceptions.NotImplementedException
import grd.kotlin.authapi.exceptions.TestEnvironmentException
import grd.kotlin.authapi.interfaces.RepositoryInterface
import grd.kotlin.authapi.settings.Settings
import com.fasterxml.jackson.databind.ObjectMapper
import com.google.cloud.firestore.CollectionReference
import com.google.cloud.firestore.Firestore
import com.google.cloud.firestore.Query
import com.google.cloud.firestore.QuerySnapshot
import com.google.common.reflect.TypeToken
import com.google.firebase.cloud.FirestoreClient
import com.google.gson.Gson
import org.springframework.beans.support.PagedListHolder
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import org.springframework.data.repository.NoRepositoryBean
import org.springframework.stereotype.Component
import java.util.*

@Component
@NoRepositoryBean
class FirebaseRepository<TEntity: Any>(private val tClass: Class<TEntity>?) : RepositoryInterface<TEntity>
{
    private val settings = Settings()
    private val gson = Gson()
    private val mapper = ObjectMapper()

    private lateinit var fs: Firestore
    private lateinit var col: CollectionReference

    val idPropertyName = "id"

    init
    {
        if(!this::fs.isInitialized && tClass != null)
        {
            println("Firebase initializing (${tClass.simpleName})...")
            try
            {
                fs = FirestoreClient.getFirestore()
                col = fs.collection(tClass.simpleName.lowercase())
            }
            catch(e: Exception)
            {
                println("Firebase failed initializing:")
                e.printStackTrace()
            }
        }
    }

    /**
     * Save an entity to the database.
     * @param id TId of the entity to save
     * @param entity Entity to save
     * @param asNew Boolean, if entity is a new item, check for existing IDs before saving.
     * @return TEntity? if saved
     * @throws ArgumentException
     **/
    @Throws(ArgumentException::class)
    override fun save(id: String, entity: TEntity, asNew: Boolean): TEntity?
    {
        abortOnTest()

        if(asNew && findById(id) != null)
            throw ArgumentException("Duplicate of ID: $id, save aborted")

        return saveToDatabase(id, entity)
    }

    /**
     * Save a dictionary of IDs and entities to the database.
     * @param dictionary Dictionary with IDs as keys, entities as elements/values
     * @return Iterable<TEntity?> of saved entities
     * @throws NotImplementedException from save
     **/
    override fun saveAll(dictionary: Dictionary<String, TEntity>): Iterable<TEntity?>
    {
        abortOnTest()

        val internalIds = dictionary.keys().toList()
        val es = dictionary.elements().toList()
        val results = ArrayList<TEntity?>()

        for(i in 0 until dictionary.size())
            results.add(save(internalIds[i], es[i]))

        return results
    }

    /**
     * Find an entity by ID in database.
     * @param id String of entity to find
     * @return TEntity? if found
     * @throws none
     **/
    override fun findById(id: String): TEntity?
    {
        abortOnTest()

        return mapper.convertValue(col.document(id).get().get().data, tClass)
    }

    /**
     * Find all entities by IDs in database.
     * @param ids IDs of entities to find
     * @return Iterable<TEntity?> of found entities
     * @throws none
     **/
    override fun findAllById(ids: Iterable<String>): Iterable<TEntity>
    {
        abortOnTest()

        val internalIds = ids.toList()
        val results = ArrayList<TEntity>()

        for(i in 0 until ids.count())
            results.add(findById(internalIds[i])!!)

        return results
    }

    /**
     * Find all entities in database.
     * @return Iterable<TEntity?> of found entities
     * @throws none
     **/
    override fun findAll(): Iterable<TEntity>
    {
        abortOnTest()

        return col.listDocuments().map { e -> mapper.convertValue(e.get().get().data, tClass) }
    }

    /**
     * Find all entities by IDs in database as Page.
     * @param pageable Pageable/PageRequest to return entities in
     * @return Page<TEntity?> of found entities
     * @throws none
     **/
    override fun findAll(pageable: Pageable): Page<TEntity>
    {
        abortOnTest()

        val all = findAll().toList()
        val pagedList = PagedListHolder(all)
        pagedList.page = pageable.pageNumber
        pagedList.pageSize = pageable.pageSize

        return PageImpl(pagedList.pageList, pageable, all.count().toLong())
    }

    /**
     * Query entities in database.
     * @param expression, for example lambda like { e: Query -> e.whereEqualTo("username", "test")
     * @return List<TEntity> of result
     * @throws none
     **/
    override fun <T, R> getQueried(expression: (T) -> R): List<TEntity>
    {
        abortOnTest()

        @Suppress("UNCHECKED_CAST")
        val documents = col as T
        val result = expression(documents)

        return (result as Query).get().get().map { e -> mapper.convertValue(e.data, tClass) }
    }

    /**
     * Query entities in database.
     * @param expression, for example lambda like { e: Query -> e.whereEqualTo("username", "test")
     * @param pageable to return entities in
     * @return Page<TEntity> of result
     * @throws none
     **/
    override fun <T, R> getQueried(expression: (T) -> R, pageable: Pageable): Page<TEntity>
    {
        abortOnTest()

        val queried = getQueried(expression)
        val pagedList = PagedListHolder(queried)
        pagedList.page = pageable.pageNumber
        pagedList.pageSize = pageable.pageSize

        return PageImpl(pagedList.pageList, pageable, queried.count().toLong())
    }

    /**
     * Delete an entity by ID.
     * @param id String of entity to delete
     * @return String of date and time deleted
     * @throws none
     **/
    override fun deleteById(id: String): String?
    {
        abortOnTest()

        return col.document(id).delete().get().updateTime.toString()
    }

    /**
     * Throws an exception if the environment is testing. Should be used in every database function to prevent stray calls.
     * @return none
     * @throws TestEnvironmentException
     **/
    @Throws(TestEnvironmentException::class)
    private fun abortOnTest()
    {
        if(settings.project.isTestBool == null || settings.project.isTestBool!!)
            throw TestEnvironmentException("Test environment: ${settings.project.isTest}")
    }

    /**
     * Helper function. Verifies if an entity can be saved (quality check).
     * @param entity Entity to check
     * @return Boolean of result, true = is ok to upload
     * @throws none
     **/
    private fun verifyEntityForSave(entity: TEntity): Boolean
    {
        entity.serializeToMap()

        return true
    }

    /**
     * Helper function. Saves data to database.
     * @param id ID to save
     * @param entity Entity to save
     * @return TEntity? of result
     * @throws none
     **/
    private fun saveToDatabase(id: String?, entity: TEntity): TEntity?
    {
        try
        {
            verifyEntityForSave(entity)
        }
        catch(e: ArgumentException)
        {
            throw e
        }

        val e = mapper.convertValue(entity, MutableMap::class.java)
//        val e = entity.serializeToMap<TEntity>()
        val internalId = if(!id.isNullOrBlank())
            id
        else
            e[idPropertyName].toString()

        val writeResult = col.document(internalId).set(e.toMap())
        writeResult.get() // Async?
        return if(writeResult.isDone)
            entity
        else
            null
    }

    /**
     * Helper function. Serializes entity to Map.
     * @param TEntity this
     * @return Map of entity
     * @throws none
     * @source https://stackoverflow.com/questions/49860916/how-to-convert-a-kotlin-data-class-object-to-map
     **/
    fun <TEntity> TEntity.serializeToMap(): Map<String, Any>
    {
        return convert()
    }

    /**
     * Helper function. Convert objects from type I to type O
     * @param I (in) this
     * @return O (out)
     * @throws none
     * @source https://stackoverflow.com/questions/49860916/how-to-convert-a-kotlin-data-class-object-to-map
     **/
    private inline fun <I, reified O> I.convert(): O
    {
        // val json = gson.toJson(this) // Has issues with RecipeIngredient quantity to LinkedHashMap. Can not set java.lang.Float property  to com.google.gson.internal.LinkedTreeMap
        val json = mapper.writeValueAsString(this)
        return gson.fromJson(json, object : TypeToken<O>() {}.type)
    }

    // Use like: debugPrint((result as Query).get().get())
    private fun debugPrint(snapshot: QuerySnapshot)
    {
        println("--------------------------------")
        val fields = tClass!!.declaredFields
        fields.sortBy { it.name }
        for(name in fields)
        {
            println("${name.name} - ${snapshot.first().data[name.name].toString()}")
        }
        println("--------------------------------")
    }
}
