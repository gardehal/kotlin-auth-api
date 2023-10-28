package grd.kotlin.authapi.interfaces

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import java.util.*

interface RepositoryInterface<TEntity>
{
    fun save(id: String, entity: TEntity, asNew: Boolean = true): TEntity?
    fun saveAll(dictionary: Dictionary<String, TEntity>): Iterable<TEntity?>
    fun findById(id: String): TEntity?
    fun findAllById(ids: Iterable<String>): Iterable<TEntity>
    fun findAll(): Iterable<TEntity>
    fun findAll(pageable: Pageable): Page<TEntity>
    fun <T, R> getQueried(expression: (T) -> R): List<TEntity>
    fun <T, R> getQueried(expression: (T) -> R, pageable: Pageable): Page<TEntity>
    fun deleteById(id: String): String?
}
