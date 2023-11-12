package grd.kotlin.authapi.services

import grd.kotlin.authapi.dto.HealthDataDto
import grd.kotlin.authapi.models.ApiMetadata
import grd.kotlin.authapi.models.AUser
import grd.kotlin.authapi.settings.Settings
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
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.junit.jupiter.SpringExtension
import java.time.Instant

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ExtendWith(SpringExtension::class, MockitoExtension::class)
class ApiMetadataServiceUnitTests
{
    @InjectMocks
    private lateinit var apiMetadataService: ApiMetadataService

    @Mock
    private var utilityService: UtilityService? = null // Note: For BaseService

    @Mock
    private lateinit var logService: LogService // Note: For BaseService

    @Autowired
    private lateinit var injectedSettings: Settings

    private val apiMetadata = ApiMetadata(id = "content", totalUsers = 12, updatedTime = "2021-03-30T10:00:00.000Z")

    @BeforeEach
    fun setup()
    {
        utilityService = mock(UtilityService::class.java)
        logService = mock(LogService::class.java)
        MockitoAnnotations.openMocks(this)

        apiMetadataService.apiUpDateTime = Instant.now()
        apiMetadataService.settings = injectedSettings
        apiMetadataService.disableLogs = true
    }

    // region setData
    @Test
    fun testSetData_NormalEmpty_Return() = runBlocking {
        val copy = ApiMetadata(id = apiMetadata.id)
        val editor = AUser(id = "editor", username = "user", password = "pass")
        val expected = ApiMetadata(id = apiMetadata.id, totalUsers = 0, updatedTime = null)

        val result = apiMetadataService.setData(copy, editor.id)

        assertNotNull(result)
        assertEquals(expected.id, result.id)
        assertNotNull(result.updatedTime)
    }

    @Test
    fun testSetData_Normal_Return() = runBlocking {
        val copy = ApiMetadata(id = apiMetadata.id)
        val editor = AUser(id = "editor", username = "user", password = "pass")
        val expected = ApiMetadata(id = apiMetadata.id, totalUsers = 1, updatedTime = null)

        val result = apiMetadataService.setData(copy, editor.id)

        assertNotNull(result)
        assertEquals(expected.id, result.id)
        assertFalse(result.added.isBlank())
        assertNotNull(result.updatedTime)
    }
    // endregion

    // region getHealthData
    @Test
    fun testGetHealthData_Exceptions_Return() = runBlocking {
        val spy = spy(apiMetadataService)
        lenient().doThrow(NullPointerException("mocked")).`when`(spy).getAll()

        val result = spy.getHealthData()

        assertNotNull(result)
        assertNotNull(result!!.now)
        assertNotNull(result.apiUpAt)
        assertNotNull(result.apiUptime)
        assertNull(result.dbCallFirebase)
        assertNotNull(result.messages)
        assertEquals(3, result.messages!!.size)
    }

    @Test
    fun testGetHealthData_GetNothing_Return() = runBlocking {
        val spy = spy(apiMetadataService)
        lenient().doReturn(emptyList<HealthDataDto>()).`when`(spy).getAll()

        val result = spy.getHealthData()

        assertNotNull(result)
        assertNotNull(result!!.now)
        assertNotNull(result.apiUpAt)
        assertNotNull(result.apiUptime)
        assertNotNull(result.dbCallFirebase)
        assertNotNull(result.messages)
        assertEquals(2, result.messages!!.size)
    }

    @Test
    fun testGetHealthData_Normal_Return() = runBlocking {
        val spy = spy(apiMetadataService)
        lenient().doReturn(emptyList<HealthDataDto>()).`when`(spy).getAll()

        val result = spy.getHealthData()

        assertNotNull(result)
        assertNotNull(result!!.now)
        assertNotNull(result.apiUpAt)
        assertNotNull(result.apiUptime)
        assertNotNull(result.dbCallFirebase)
        assertNotNull(result.messages)
        assertEquals(1, result.messages!!.size)
    }
    // endregion
}
