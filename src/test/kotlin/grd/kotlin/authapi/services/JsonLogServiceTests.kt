package grd.kotlin.authapi

import grd.kotlin.authapi.logging.JsonLogService
import grd.kotlin.authapi.logging.LogFileUtilService
import grd.kotlin.authapi.logging.LogLevel
import grd.kotlin.authapi.logging.LogUtilService
import grd.kotlin.authapi.settings.Logging
import grd.kotlin.authapi.settings.Settings
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.test.context.junit.jupiter.SpringExtension
import java.io.File
import java.nio.file.Paths

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ExtendWith(SpringExtension::class, MockitoExtension::class)
class JsonLogServiceTests
{
    @LocalServerPort
    private var port = 0
    private lateinit var util: TestUtil

    @InjectMocks
    private lateinit var logService: JsonLogService

    @Mock
    private lateinit var settings: Settings

    @Mock
    private lateinit var logUtilService: LogUtilService

    @Mock
    private lateinit var logFileUtilService: LogFileUtilService

    private val testOutputDirectory = ".\\TEST_OUTPUT"
    private val definedMethodLogFileName = "JsonLogServiceTests.log"

    @BeforeEach
    fun setup()
    {
        settings = mock(Settings::class.java)
        logUtilService = mock(LogUtilService::class.java)
        logFileUtilService = mock(LogFileUtilService::class.java)
        MockitoAnnotations.openMocks(this)

        util = TestUtil(port)

        // Reset output directory
        File(testOutputDirectory).deleteRecursively()
        File(testOutputDirectory).mkdir()
    }

    // region initialize
    @Test
    fun testInitialize_Normal_Return()
    {
        val logging = Logging()
        logging.logLevel = LogLevel.DEBUG
        logging.disableLogRotation = false
        logging.logFileRotatePeriodInDays = 30
        logging.logFileDirectory = testOutputDirectory
        logging.logFilePrefix = "test"
        logging.logFileExtension = "log"

        `when`(settings.logging).thenReturn(logging)

        val result = logService.initialize()

        assertNotNull(result) // Unit
    }
    // endregion

    // region initializeJsonFile
    @Test
    fun testInitializeJsonFile_Normal_Return()
    {
        val path = Paths.get(testOutputDirectory, "testInitializeJsonFile_Normal_Return.json").toString()
        val file = File(path)

        val result = logService.initializeJsonFile(file)

        assertNotNull(result)
        val contents = result.readText()
        assertNotNull(contents.contains("{"))
        assertNotNull(contents.contains("}"))
    }
    // endregion
}
