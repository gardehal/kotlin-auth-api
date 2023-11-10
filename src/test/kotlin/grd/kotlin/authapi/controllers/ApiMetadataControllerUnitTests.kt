package grd.kotlin.authapi.controllers

import grd.kotlin.authapi.MockitoHelper
import grd.kotlin.authapi.dto.HealthDataDto
import grd.kotlin.authapi.models.ApiMetadata
import grd.kotlin.authapi.models.AUser
import grd.kotlin.authapi.models.HttpReturn
import grd.kotlin.authapi.services.*
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.MockitoAnnotations
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.junit.jupiter.SpringExtension
import java.time.Duration
import java.time.Instant

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ExtendWith(SpringExtension::class, MockitoExtension::class)
class ApiMetadataControllerUnitTests
{
    @InjectMocks
    private lateinit var apiMetadataController: ApiMetadataController

    @Mock
    private lateinit var controllerUtilityService: ControllerUtilityService

    @Mock
    private lateinit var apiMetadataService: ApiMetadataService

    @Mock
    private lateinit var chookUserService: UserService

    @Mock
    private lateinit var utilityService: UtilityService

    @BeforeEach
    fun init()
    {
        controllerUtilityService = mock(ControllerUtilityService::class.java)
        apiMetadataService = mock(ApiMetadataService::class.java)
        chookUserService = mock(UserService::class.java)
        utilityService = mock(UtilityService::class.java)
        MockitoAnnotations.openMocks(this)

        apiMetadataService.apiUpDateTime = Instant.now()
    }

    val authorizationFieldName = "authorization"
    val metadata = ApiMetadata(id = "content", totalUsers = 1, updatedTime = "2021-03-30T10:00:00.000Z")
    val healthData = HealthDataDto(
        now = Instant.now(),
        apiUpAt = Instant.now().minusSeconds(60L),
        apiUptime = Duration.ofMinutes(1L),
        dbCallFirebase = Duration.ofSeconds(2L),
        messages = mutableListOf("No errors."))

    // GET /meta/health
    // region getHealth
    @Test
    fun testGetHealth_Normal_Return200() = runBlocking {
        val copy = healthData.copy()
        val expectedCode = 200

        lenient().`when`(apiMetadataService.getHealthData()).thenReturn(copy)

        val result = apiMetadataController.getHealth()

        assertNotNull(result)
        assertNotNull(result.body)
        assertEquals(expectedCode, result.body!!.code)
        assertNotNull(result.body!!.data)
        assertNull(result.body!!.message)
    }

    @Test
    fun testGetHealth_ServerError_Return500() = runBlocking {
        val expectedCode = 500

        lenient().`when`(apiMetadataService.getHealthData()).thenThrow(NullPointerException("mocked"))

        val result = apiMetadataController.getHealth()

        assertNotNull(result)
        assertNotNull(result.body)
        assertEquals(expectedCode, result.body!!.code)
        assertNull(result.body!!.data)
        assertNotNull(result.body!!.message)
    }
    // endregion

    // GET /meta
    // region getMetadata
    @Test
    fun testGetMetadata_Normal_Return200() = runBlocking {
        val copy = metadata.copy()
        val expectedCode = 200

        lenient().`when`(apiMetadataService.get(anyString(), anyBoolean())).thenReturn(copy)

        val result = apiMetadataController.getMetadata()

        assertNotNull(result)
        assertNotNull(result.body)
        assertEquals(expectedCode, result.body!!.code)
        assertNotNull(result.body!!.data)
        assertNull(result.body!!.message)
    }

    @Test
    fun testGetMetadata_ServerError_Return500() = runBlocking {
        val expectedCode = 500

        lenient().`when`(apiMetadataService.get(anyString(), anyBoolean())).thenThrow(NullPointerException("mocked"))
        lenient().`when`(controllerUtilityService.getErrorHttpReturn(MockitoHelper.anyObject(), MockitoHelper.anyObject(), MockitoHelper.anyObject(), anyInt()))
            .thenReturn(HttpReturn(expectedCode, "mocked"))

        val result = apiMetadataController.getMetadata()

        assertNotNull(result)
        assertNotNull(result.body)
        assertEquals(expectedCode, result.body!!.code)
        assertNull(result.body!!.data)
        assertNotNull(result.body!!.message)
    }
    // endregion

    // GET /meta/update
    // region updateMetadata
    @Test
    fun testUpdateMetadata_Normal_Return200() = runBlocking {
        val headers = mapOf<String, String>()
        val editor = AUser(id = "editor", username = "editor", password = "pass")
        val updated = metadata.copy()
        val expectedCode = 200

        lenient().`when`(chookUserService.getUserFromHeaders(MockitoHelper.anyObject())).thenReturn(editor)
        lenient().`when`(chookUserService.actionAllowed(MockitoHelper.anyObject(), MockitoHelper.anyObject())).thenReturn(true)
        lenient().`when`(apiMetadataService.setData(MockitoHelper.anyObject(), anyString())).thenReturn(updated)
        lenient().`when`(apiMetadataService.update(MockitoHelper.anyObject(), anyString(), anyBoolean(), anyBoolean())).thenReturn(updated)

        val result = apiMetadataController.updateMetadata(headers)

        assertNotNull(result)
        assertNotNull(result.body)
        assertEquals(expectedCode, result.body!!.code)
        assertNotNull(result.body!!.data)
        assertNull(result.body!!.message)
    }

    @Test
    fun testUpdateMetadata_ServerError_Return500() = runBlocking {
        val headers = mapOf(Pair(authorizationFieldName, "valid-token"))
        val expectedCode = 500

        lenient().`when`(chookUserService.getUserFromHeaders(MockitoHelper.anyObject())).thenThrow(NullPointerException("mocked"))
        lenient().`when`(controllerUtilityService.getErrorHttpReturn(MockitoHelper.anyObject(), MockitoHelper.anyObject(), MockitoHelper.anyObject(), anyInt()))
            .thenReturn(HttpReturn(expectedCode, "mocked"))

        val result = apiMetadataController.updateMetadata(headers)

        assertNotNull(result)
        assertNotNull(result.body)
        assertEquals(expectedCode, result.body!!.code)
        assertNull(result.body!!.data)
        assertNotNull(result.body!!.message)
    }
    // endregion
}
