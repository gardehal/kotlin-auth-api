package grd.kotlin.authapi

import grd.kotlin.authapi.exceptions.*
import grd.kotlin.authapi.services.ControllerUtilityService
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.MockitoAnnotations
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.junit.jupiter.SpringExtension
import java.net.MalformedURLException

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ExtendWith(SpringExtension::class, MockitoExtension::class)
class ControllerUtilityServiceTests
{
    @InjectMocks
    private lateinit var controllerUtilityService: ControllerUtilityService

    @BeforeEach
    fun setup()
    {
        MockitoAnnotations.openMocks(this)
    }

    // region getErrorHttpReturn
    @Test
    fun testGetErrorHttpReturn_NotFoundException_Return400() = runBlocking {
        val exception = NotFoundException("NotFoundException")
        val context = this.toString()

        val result = controllerUtilityService.getErrorHttpReturn(exception, context, "editor")

        assertNotNull(result)
        assertEquals(400, result.code)
        assertEquals(exception.message, result.message)
    }

    @Test
    fun testGetErrorHttpReturn_NotFoundException_Return200() = runBlocking {
        val exception = NotFoundException("NotFoundException")
        val context = this.toString()
        val notFoundCode = 200

        val result = controllerUtilityService.getErrorHttpReturn(exception, context, "editor", notFoundCode)

        assertNotNull(result)
        assertEquals(notFoundCode, result.code)
        assertEquals(exception.message, result.message)
    }

    @Test
    fun testGetErrorHttpReturn_ArgumentException_Return400() = runBlocking {
        val exception = ArgumentException("ArgumentException")
        val context = this.toString()

        val result = controllerUtilityService.getErrorHttpReturn(exception, context, "editor")

        assertNotNull(result)
        assertEquals(400, result.code)
        assertEquals(exception.message, result.message)
    }

    @Test
    fun testGetErrorHttpReturn_DuplicateException_Return400() = runBlocking {
        val exception = DuplicateException("DuplicateException")
        val context = this.toString()

        val result = controllerUtilityService.getErrorHttpReturn(exception, context, "editor")

        assertNotNull(result)
        assertEquals(400, result.code)
        assertEquals(exception.message, result.message)
    }

    @Test
    fun testGetErrorHttpReturn_MalformedURLException_Return400() = runBlocking {
        val exception = MalformedURLException("MalformedURLException")
        val context = this.toString()

        val result = controllerUtilityService.getErrorHttpReturn(exception, context, "editor")

        assertNotNull(result)
        assertEquals(400, result.code)
        assertEquals(exception.message, result.message)
    }

    @Test
    fun testGetErrorHttpReturn_NotAuthorizedException_Return401() = runBlocking {
        val exception = NotAuthorizedException("NotAuthorizedException")
        val context = this.toString()

        val result = controllerUtilityService.getErrorHttpReturn(exception, context, "editor")

        assertNotNull(result)
        assertEquals(401, result.code)
        assertEquals(exception.message, result.message)
    }

    @Test
    fun testGetErrorHttpReturn_DatabaseErrorException_Return500() = runBlocking {
        val exception = DatabaseErrorException("DatabaseErrorException")
        val context = this.toString()

        val result = controllerUtilityService.getErrorHttpReturn(exception, context, "editor")

        assertNotNull(result)
        assertEquals(500, result.code)
        assertEquals(exception.message, result.message)
    }

    @Test
    fun testGetErrorHttpReturn_LoggingException_Return500() = runBlocking {
        val exception = LoggingException("LoggingException")
        val context = this.toString()

        val result = controllerUtilityService.getErrorHttpReturn(exception, context, "editor")

        assertNotNull(result)
        assertEquals(500, result.code)
        assertEquals(exception.message, result.message)
    }

    @Test
    fun testGetErrorHttpReturn_TestEnvironmentException_Return500() = runBlocking {
        val exception = TestEnvironmentException("TestEnvironmentException")
        val context = this.toString()

        val result = controllerUtilityService.getErrorHttpReturn(exception, context, "editor")

        assertNotNull(result)
        assertEquals(500, result.code)
        assertEquals(exception.message, result.message)
    }

    @Test
    fun testGetErrorHttpReturn_UnexpectedStateException_Return500() = runBlocking {
        val exception = UnexpectedStateException("UnexpectedStateException")
        val context = this.toString()

        val result = controllerUtilityService.getErrorHttpReturn(exception, context, "editor")

        assertNotNull(result)
        assertEquals(500, result.code)
        assertEquals(exception.message, result.message)
    }

    @Test
    fun testGetErrorHttpReturn_NotImplementedException_Return501() = runBlocking {
        val exception = NotImplementedException("NotImplementedException")
        val context = this.toString()

        val result = controllerUtilityService.getErrorHttpReturn(exception, context, "editor")

        assertNotNull(result)
        assertEquals(501, result.code)
        assertEquals(exception.message, result.message)
    }

    @Test
    fun testGetErrorHttpReturn_ElseNotHandledException_Return500() = runBlocking {
        val exception = NullPointerException("NullPointerException")
        val context = this.toString()

        val result = controllerUtilityService.getErrorHttpReturn(exception, context, "editor")

        assertNotNull(result)
        assertEquals(500, result.code)
        assertNotEquals(exception.message, result.message)
    }

    @Test
    fun testGetErrorHttpReturn_NoLogging_Return400() = runBlocking {
        val exception = ArgumentException("ArgumentException")

        val result = controllerUtilityService.getErrorHttpReturn(exception, null, "editor")

        assertNotNull(result)
        assertEquals(400, result.code)
        assertEquals(exception.message, result.message)
    }

    @Test
    fun testGetErrorHttpReturn_ElseNotHandledExceptionNoLogging_Return500() = runBlocking {
        val exception = NullPointerException("NullPointerException")

        val result = controllerUtilityService.getErrorHttpReturn(exception, null, "editor")

        assertNotNull(result)
        assertEquals(500, result.code)
        assertNotEquals(exception.message, result.message)
    }
    // endregion
}
