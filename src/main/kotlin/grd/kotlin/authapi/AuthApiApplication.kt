package grd.kotlin.authapi

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.boot.runApplication
import org.springframework.context.annotation.ComponentScan
import org.springframework.data.jpa.repository.config.EnableJpaRepositories

@SpringBootApplication
@EnableJpaRepositories("grd.kotlin.authapi")
@ComponentScan("grd.kotlin.authapi")
@EntityScan("grd.kotlin.authapi.models.auser", "grd.kotlin.authapi.models.apimetadata", "grd.kotlin.authapi.models.system", "grd.kotlin.authapi.models.usersystem")
class AuthApiApplication

suspend fun main(args: Array<String>)
{
	Log.main.info("AuthAPI starting...")
	Log.main.debug("Java version: {jVersion}, Kotlin version: {kVersion}", System.getProperty("java.version"), KotlinVersion.CURRENT)
	runApplication<AuthApiApplication>(*args)
}
