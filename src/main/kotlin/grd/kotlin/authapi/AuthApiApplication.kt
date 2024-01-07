package grd.kotlin.authapi

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.data.jpa.repository.config.EnableJpaRepositories

@SpringBootApplication
@EnableJpaRepositories
class AuthApiApplication

suspend fun main(args: Array<String>)
{
	Log.main.info("AuthAPI starting...")
	Log.main.debug("Java version: {jVersion}, Kotlin version: {kVersion}", System.getProperty("java.version"), KotlinVersion.CURRENT)
	runApplication<AuthApiApplication>(*args)
}
