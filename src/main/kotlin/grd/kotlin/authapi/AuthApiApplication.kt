package grd.kotlin.authapi

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Primary
import org.springframework.data.jpa.repository.config.EnableJpaRepositories
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean

@SpringBootApplication
@EnableJpaRepositories
@ComponentScan
@EntityScan("grd.kotlin.authapi.models")
class AuthApiApplication

suspend fun main(args: Array<String>)
{
	Log.main.info("AuthAPI starting...")
	Log.main.debug("Java version: {jVersion}, Kotlin version: {kVersion}", System.getProperty("java.version"), KotlinVersion.CURRENT)
	runApplication<AuthApiApplication>(*args)
}
