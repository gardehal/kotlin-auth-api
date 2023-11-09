package grd.kotlin.authapi

import grd.kotlin.authapi.dto.WrappedResponse
import grd.kotlin.authapi.extensions.join
import com.fasterxml.jackson.databind.ObjectMapper
import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.MappingBuilder
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.common.ConsoleNotifier
import com.github.tomakehurst.wiremock.core.WireMockConfiguration
import grd.kotlin.authapi.models.AUser
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import io.restassured.RestAssured
import io.restassured.specification.RequestSpecification
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.mockito.Mockito
import org.springframework.boot.test.util.TestPropertyValues
import org.springframework.context.ApplicationContextInitializer
import org.springframework.context.ConfigurableApplicationContext
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import java.io.File
import java.nio.file.Paths
import java.time.Instant
import java.util.*

class TestUtil(val port: Int)
{
    companion object
    {
        private lateinit var wm: WireMockServer

        @BeforeAll
        @JvmStatic
        fun initClass()
        {
            RestAssured.baseURI = "http://localhost"
            // RestAssured.basePath = ""
            RestAssured.enableLoggingOfRequestAndResponseIfValidationFails()

            wm = WireMockServer(
                WireMockConfiguration.wireMockConfig()
                    .dynamicPort()
                    .notifier(ConsoleNotifier(true)))
            wm.start()
        }

        @AfterAll
        @JvmStatic
        fun tearDown()
        {
            wm.stop()
        }

        class Initializer : ApplicationContextInitializer<ConfigurableApplicationContext>
        {
            override fun initialize(configurableApplicationContext: ConfigurableApplicationContext)
            {
                TestPropertyValues.of("IngredientControllerIntegrationTests: localhost:${wm.port()}")
                    .applyTo(configurableApplicationContext.environment)
            }
        }
    }

    fun <T> any(type: Class<T>): T = Mockito.any<T>(type)

    fun restAssuredGiven(wiremock: Boolean = false, authenticate: Boolean = false): RequestSpecification
    {
        val encoded = "MToxMjM=" // TODO: Get from .env encode base64 "user:pass"

        val given = RestAssured.given()
        if(!wiremock)
            given.port(port) // RestAssured with given port set to port will not run with WireMock
        if(authenticate)
            given.header("Authorization", "Basic $encoded") // Login with HTTP basic authentication

        return given
    }

    fun setSecurityContextHolderUser(user: AUser): UsernamePasswordAuthenticationToken
    {
        val g = GrantedAuthority { user.role.toString() }
        val gl = listOf(g)

        val usernamePasswordAuthenticationToken = UsernamePasswordAuthenticationToken(user, user.id, gl)
        SecurityContextHolder.getContext().authentication = usernamePasswordAuthenticationToken
        return usernamePasswordAuthenticationToken
    }

    fun doGenerateToken(claims: Map<String, Any>, subject: String): String
    {
        return Jwts.builder()
            .setClaims(claims)
            .setSubject(subject)
            .setIssuedAt(Date(System.currentTimeMillis()))
            .setExpiration(Date(System.currentTimeMillis() + (1000 * 60))) // One minute validity
            .signWith(SignatureAlgorithm.HS512, "testing-secret") // Secret not important
            .compact()
    }

    fun getFilePath(logFileDirectory: String, prefix: String, instant: Instant, extension: String): String
    {
        val dateTime = instant.toString().replace(":", "+")
        val fileName = "${prefix}_${dateTime}.${extension}"
        return Paths.get(logFileDirectory, fileName).toString()
    }

    fun makeAndGetLogChangesFile(logFileDirectory: String, prefix: String, extension: String): File
    {
        val now = Instant.now()
        val file = File(getFilePath(logFileDirectory, prefix, now, extension))

        val contents = listOf("$now id:id1 operation:EDITED itemType:USER itemId:itemId1 editorId:editorId1 automated:manual fields changed:[username], comment:null",
            "\tusername: username1 -> username2",
            "$now id:id2 operation:EDITED itemType:USER itemId:itemId1 editorId:editorId1 automated:manual fields changed:[email, about], comment:null",
            "\temail: example@exampl.com -> test@exampl.com",
            "\tabout: I am a test -> This is a test",
            "$now id:id3 operation:EDITED itemType:USER itemId:itemId42 editorId:editorId1 automated:automatic fields changed:[password], comment:null",
            "\temail: example123@exampl.com -> example321@exampl.com",
            "\tpassword: <sensitive> -> <sensitive>",)

        val contentsString = contents.join("\n")
        file.writeText(contentsString!!)
        return file
    }
}

fun <T> WireMockServer.stub(stubMapping: MappingBuilder, code: Int, data: T? = null, message: String? = null)
{
    val wrapped = WrappedResponse(code, data, message)
    this.stubFor(stubMapping
        .willReturn(
            WireMock.aResponse()
                .withStatus(code)
                .withBody(ObjectMapper().writeValueAsString(wrapped))))
}

object MockitoHelper
{
    fun <T> anyObject(): T
    {
        Mockito.any<T>()
        return uninitialized()
    }

    @Suppress("UNCHECKED_CAST")
    fun <T> uninitialized(): T = null as T

    fun <T : Any> safeEq(value: T): T = Mockito.eq(value) ?: value
}
