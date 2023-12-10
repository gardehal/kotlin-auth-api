package grd.kotlin.authapi.interfaces

import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface  IPostgresRepository<TEntity: Any> : CrudRepository<TEntity, String>
