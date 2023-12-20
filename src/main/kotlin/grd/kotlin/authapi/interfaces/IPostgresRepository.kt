package grd.kotlin.authapi.interfaces

import org.springframework.context.annotation.Configuration
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.config.EnableJpaRepositories
import org.springframework.stereotype.Repository

@Configuration
@EnableJpaRepositories
public class JpaConfiguration {
}

@Repository
interface  IPostgresRepository<TEntity: Any> : JpaRepository<TEntity, String>
