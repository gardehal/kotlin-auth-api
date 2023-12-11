package grd.kotlin.authapi.repositories

import com.fasterxml.jackson.databind.ObjectMapper
import com.google.gson.Gson
import grd.kotlin.authapi.exceptions.ArgumentException
import grd.kotlin.authapi.exceptions.NotImplementedException
import grd.kotlin.authapi.exceptions.TestEnvironmentException
import grd.kotlin.authapi.interfaces.IPostgresRepository
import grd.kotlin.authapi.interfaces.RepositoryInterface
import grd.kotlin.authapi.settings.Settings
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.support.PagedListHolder
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import org.springframework.data.repository.NoRepositoryBean
import org.springframework.stereotype.Component
import java.time.Instant
import java.util.*
import kotlin.jvm.optionals.getOrNull

@Component
@NoRepositoryBean
class PostgresRepository<TEntity: Any>(private val tClass: Class<TEntity>?) : RepositoryInterface<TEntity>
{
    private val settings = Settings()
    private val gson = Gson()
    private val mapper = ObjectMapper()

    val idPropertyName = "id"

    @Autowired
    private lateinit var repository: IPostgresRepository<TEntity>

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

        return repository.save(entity)
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

        return repository.findById(id).getOrNull()
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

        return repository.findAll()
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

//        repository.takeIf { expression(repository.findAll() as T) == true }
        return emptyList()
//        @Suppress("UNCHECKED_CAST")
//        val documents = col as T
//        val result = expression(documents)
//
//        return (result as Query).get().get().map { e -> mapper.convertValue(e.data, tClass) }
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

        repository.deleteById(id)
        return Instant.now().toString()
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
}
