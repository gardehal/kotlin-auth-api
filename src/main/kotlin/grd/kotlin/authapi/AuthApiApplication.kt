package grd.kotlin.authapi

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import io.klogging.logger

@SpringBootApplication
class AuthApiApplication

object Log
{
	val main = logger("main")

	/**
	 * {function}, {code}, {message}
	 */
	fun controllerMsg(): String
	{
		return "{function}, {code}, {message}"
	}

	/**
	 * {function}, {code}, {message}, {editor}
	 */
	fun controllerEditorMsg(): String
	{
		return "{function}, {code}, {message}, {editor}"
	}
}

suspend fun main(args: Array<String>)
{
	Log.main.info("AuthAPI starting...")
	Log.main.debug("Java version: {jVersion}, Kotlin version: {kVersion}", System.getProperty("java.version"), KotlinVersion.CURRENT)
	runApplication<AuthApiApplication>(*args)
}
