package grd.kotlin.authapi

import grd.kotlin.authapi.services.LogService
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.junit.jupiter.SpringExtension

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ExtendWith(SpringExtension::class, MockitoExtension::class)
class LogServiceTests
{
    @InjectMocks
    private lateinit var logService: LogService

    private val testOutputDirectory = ".\\TEST_OUTPUT"
    private val definedMethodLogFileName = "myLoggingLogFile.log"

//    @BeforeEach
//    fun setup()
//    {
//        MockitoAnnotations.openMocks(this)
//
//        // Reset output directory
//        File(testOutputDirectory).deleteRecursively()
//        File(testOutputDirectory).mkdir()
//        logService.logFileDirectory = testOutputDirectory
//    }
//
//    // region helper functions
//    fun getFilePath(instant: Instant): String
//    {
//        logService.logFileDirectory = testOutputDirectory
//        val dateTime = instant.toString().replace(":", "+")
//        val fileName = "${logService.logFilePrefix}_${dateTime}.${logService.logFileExtension}"
//        return Paths.get(logService.logFileDirectory, fileName).toString()
//    }
//
//    fun makeAndGetLogChangesFile(): File
//    {
//        val now = Instant.now()
//        val file = File(getFilePath(now))
//
//        val contents = listOf("$now id:id1 operation:EDITED itemType:USER itemId:itemId1 editorId:editorId1 automated:manual fields changed:[username], comment:null",
//            "\tusername: username1 -> username2",
//            "$now id:id2 operation:EDITED itemType:USER itemId:itemId1 editorId:editorId1 automated:manual fields changed:[email, about], comment:null",
//            "\temail: example@exampl.com -> test@exampl.com",
//            "\tabout: I am a test -> This is a test",
//            "$now id:id3 operation:EDITED itemType:USER itemId:itemId42 editorId:editorId1 automated:automatic fields changed:[password], comment:null",
//            "\temail: example123@exampl.com -> example321@exampl.com",
//            "\tpassword: <sensitive> -> <sensitive>",)
//
//        val contentsString = contents.join("\n")
//        file.writeText(contentsString!!)
//        return file
//    }
//
//    fun myEventLogging(logEvent: LogEvent): String
//    {
//        // This is just an example, usage can be storing events wherever.
//        val path = Paths.get(testOutputDirectory, definedMethodLogFileName).toString()
//        val file = File(path)
//        file.appendText(logEvent.itemId!!)
//
//        return path
//    }
//
//    fun myChangesLogging(logHead: LogHead): String
//    {
//        // This is just an example, usage can be storing head and lines wherever.
//        val path = Paths.get(testOutputDirectory, definedMethodLogFileName).toString()
//        val file = File(path)
//        file.appendText(logHead.itemId!!)
//
//        return path
//    }
//
//    fun myCensure(propertyNames: List<String>, itemId: String): List<String>
//    {
//        // This is just an example, usage can be altering data wherever it's stored.
//        val dir = File(testOutputDirectory)
//        val files = dir.listFiles()!!
//        dir.deleteRecursively()
//        File(testOutputDirectory).mkdir()
//        return files.map { it.name }
//    }
//
//    fun myGetEvents(queryString: String): List<LogEvent>
//    {
//        // This is just an example, usage should be searching for LogEvents related to queryString.
//        return listOf(LogEvent("logEvent1", Instant.now().toString(), queryString))
//    }
//
//    fun myGetChanges(queryString: String): List<LogHead>
//    {
//        // This is just an example, usage should be searching for LogHead related to queryString.
//        return listOf(LogHead("logHead1", itemId = queryString))
//    }
//    // endregion
//
//    // region help
//    @Test
//    fun testHelp_Normal_ThrowNotImplementedException()
//    {
//        try
//        {
//            logService.help()
//            Assertions.fail() // Fail here
//        }
//        catch(e: NotImplementedException)
//        {
//            Assertions.assertTrue(true)
//        }
//    }
//    // endregion
//
//    // region getLogEvents
//    @Test
//    fun testGetLogEvents_LogsDisabled_ReturnList()
//    {
//        logService.disableLogs = true
//        val queryString = "query"
//
//        val result = logService.getLogEvents(queryString)
//
//        Assertions.assertNotNull(result)
//        Assertions.assertTrue(result.isEmpty())
//    }
//
//    @Test
//    fun testGetLogEvents_GetTextLogs_ThrowNotImplementedException()
//    {
//        val queryString = "query"
//
//        try
//        {
//            logService.getLogEvents(queryString)
//            Assertions.fail() // Fail here
//        }
//        catch(e: NotImplementedException)
//        {
//            Assertions.assertTrue(true)
//        }
//    }
//
//    @Test
//    fun testGetLogEvents_DefinedNormal_ReturnLogEvent()
//    {
//        logService.logSink = LogSink.DEFINED
//        logService.definedGetLogEventMethod = ::myGetEvents
//        val queryString = "query"
//
//        val result = logService.getLogEvents(queryString)
//
//        Assertions.assertNotNull(result)
//        Assertions.assertEquals(1, result.size)
//        Assertions.assertEquals(queryString, result.first().event)
//    }
//    // endregion
//
//    // region getLogChanges
//    @Test
//    fun testGetLogChanges_LogsDisabled_ReturnList()
//    {
//        logService.disableLogs = true
//        val queryString = "query"
//
//        val result = logService.getLogChanges(queryString)
//
//        Assertions.assertNotNull(result)
//        Assertions.assertTrue(result.isEmpty())
//    }
//
//    @Test
//    fun testGetLogChanges_GetTextLogs_ThrowNotImplementedException()
//    {
//        val queryString = "query"
//
//        try
//        {
//            logService.getLogChanges(queryString)
//            Assertions.fail() // Fail here
//        }
//        catch(e: NotImplementedException)
//        {
//            Assertions.assertTrue(true)
//        }
//    }
//
//    @Test
//    fun testGetLogChanges_DefinedNormal_ReturnLogEvent()
//    {
//        logService.logSink = LogSink.DEFINED
//        logService.definedGetLogChangesMethod = ::myGetChanges
//        val queryString = "query"
//
//        val result = logService.getLogChanges(queryString)
//
//        Assertions.assertNotNull(result)
//        Assertions.assertEquals(1, result.size)
//        Assertions.assertEquals(queryString, result.first().itemId)
//    }
//    // endregion
//
//    // region logEvent
//    @Test
//    fun testLogEvent_LogsDisabled_ReturnNull()
//    {
//        logService.disableLogs = true
//        logService.logSink = LogSink.TEXT_FILE
//        val event = "some_event"
//        val userId = "user_id1"
//        val itemId = "item_id1"
//
//        val result = logService.logEvent(event, userId, itemId)
//
//        Assertions.assertNull(result)
//    }
//
//    @Test
//    fun testLogEvent_NormalText_ReturnString()
//    {
//        logService.logFileDirectory = testOutputDirectory
//        logService.logSink = LogSink.TEXT_FILE
//        val event = "some_event"
//        val userId = "user_id1"
//        val itemId = "item_id1"
//        val logPath = "some_path"
//
//        val spy = Mockito.spy(logService)
//        Mockito.lenient().doReturn(logPath).`when`(spy).logEventText(MockitoHelper.anyObject())
//
//        val result = spy.logEvent(event, userId, itemId)
//
//        Assertions.assertNotNull(result)
//    }
//
//    @Test
//    fun testLogEvent_DefinedNull_ThrowArgumentException()
//    {
//        logService.logFileDirectory = testOutputDirectory
//        logService.logSink = LogSink.DEFINED
//        logService.definedLogEventMethod = null
//        val event = "some_event"
//        val userId = "user_id1"
//        val itemId = "item_id1"
//
//        try
//        {
//            logService.logEvent(event, userId, itemId)
//            Assertions.fail() // Fail here
//        }
//        catch(e: ArgumentException)
//        {
//            Assertions.assertTrue(true)
//        }
//    }
//
//    @Test
//    fun testLogEvent_NormalDefined_ReturnString()
//    {
//        logService.logFileDirectory = testOutputDirectory
//        logService.logSink = LogSink.DEFINED
//        logService.definedLogEventMethod = ::myEventLogging
//        val event = "some_event"
//        val userId = "user_id1"
//        val itemId = "item_id1"
//
//        val directoryBefore = File(logService.logFileDirectory).listFiles()
//        Assertions.assertTrue(directoryBefore!!.isEmpty())
//
//        val result = logService.logEvent(event, userId, itemId)
//
//        val directoryAfter = File(logService.logFileDirectory).listFiles()
//        Assertions.assertTrue(directoryAfter!!.isNotEmpty())
//
//        Assertions.assertNotNull(result)
//        val definedMethodLogFile = directoryAfter.first()
//        Assertions.assertEquals(definedMethodLogFileName, definedMethodLogFile.name)
//        val file = File(definedMethodLogFile.absolutePath)
//        val lines = file.readLines()
//        Assertions.assertEquals(1, lines.size)
//        Assertions.assertEquals(lines.first(), itemId)
//    }
//    // endregion
//
//    // region logEventText
//    @Test
//    fun testGetLogEventText_Normal_ReturnString()
//    {
//        val logEvent = LogEvent()
//        val path = getFilePath(Instant.now())
//        val file = File(path)
//        file.appendText("testGetLogEventText_Normal_ReturnString")
//        val loggedData = "loggedData"
//
//        val spy = Mockito.spy(logService)
//        Mockito.lenient().doReturn(file).`when`(spy).getLatestLogFile()
//        Mockito.lenient().doReturn(loggedData).`when`(spy).getEventString(MockitoHelper.anyObject())
//
//        val directoryBefore = File(logService.logFileDirectory).listFiles()
//        Assertions.assertTrue(directoryBefore!!.isNotEmpty())
//
//        val result = spy.logEventText(logEvent)
//
//        val directoryAfter = File(logService.logFileDirectory).listFiles()
//        Assertions.assertTrue(directoryAfter!!.isNotEmpty())
//
//        Assertions.assertNotNull(result)
//        Assertions.assertTrue(path.contains(result))
//        val resultFile = File(path)
//        Assertions.assertNotNull(resultFile)
//        Assertions.assertTrue(resultFile.isFile)
//        val resultFileContent = resultFile.readText()
//        Assertions.assertTrue(resultFileContent.contains(loggedData))
//    }
//    // endregion
//
//    // region getEventString
//    @Test
//    fun testGetEventString_NullValues_ReturnString()
//    {
//        val logEvent = LogEvent()
//
//        val result = logService.getEventString(logEvent)
//
//        Assertions.assertNotNull(result)
//        Assertions.assertTrue(result.contains("null"))
//    }
//
//    @Test
//    fun testGetEventString_Normal_ReturnString()
//    {
//        val logEvent = LogEvent(id = "some_guid", registered = "some_datetime", event = "Something bad happened", userId = "user_id", itemId = "item_id")
//
//        val result = logService.getEventString(logEvent)
//
//        Assertions.assertNotNull(result)
//        Assertions.assertTrue(result.contains(logEvent.id!!))
//        Assertions.assertTrue(result.contains(logEvent.registered!!))
//        Assertions.assertTrue(result.contains(logEvent.event!!))
//        Assertions.assertTrue(result.contains(logEvent.userId!!))
//        Assertions.assertTrue(result.contains(logEvent.itemId!!))
//    }
//    // endregion
//
//    // region censureLogChanges
//    @Test
//    fun testCensureLogChanges_LogsDisabled_ReturnFile()
//    {
//        logService.disableLogs = true
//        logService.logSink = LogSink.TEXT_FILE
//        val propertyToUpdate = "email"
//        val properties = listOf(propertyToUpdate)
//        val itemId = "itemId1"
//
//        val result = logService.censureLogChanges(properties, itemId)
//
//        Assertions.assertNull(result)
//    }
//
//    @Test
//    fun testCensureLogChanges_NormalText_ReturnFile()
//    {
//        logService.logSink = LogSink.TEXT_FILE
//        val propertyToUpdate = "email"
//        val properties = listOf(propertyToUpdate)
//        val itemId = "itemId1"
//        val updatedLogs = listOf("some_log_file")
//
//        val spy = Mockito.spy(logService)
//        Mockito.lenient().doReturn(updatedLogs).`when`(spy).censureLogChangesText(MockitoHelper.anyObject(), Mockito.anyString())
//
//        val result = spy.censureLogChanges(properties, itemId)
//
//        Assertions.assertNotNull(result)
//        Assertions.assertEquals(1, result!!.size)
//        Assertions.assertEquals(updatedLogs.first(), result.first())
//    }
//
//    @Test
//    fun testCensureLogChanges_DefinedMethod_ReturnFile()
//    {
//        logService.logSink = LogSink.DEFINED
//        logService.definedCensureMethod = ::myCensure
//        makeAndGetLogChangesFile()
//        val propertyToUpdate = "email"
//        val properties = listOf(propertyToUpdate)
//        val itemId = "itemId1"
//
//        val result = logService.censureLogChanges(properties, itemId)
//
//        Assertions.assertNotNull(result)
//        Assertions.assertTrue(result!!.isNotEmpty())
//
//        val directoryAfter = File(logService.logFileDirectory).listFiles()
//        Assertions.assertTrue(directoryAfter!!.isEmpty())
//
//    }
//    // endregion
//
//    // region censureLogChangesText
//    @Test
//    fun testCensureLogChangesText_NoFile_ReturnFile()
//    {
//        val propertyToUpdate = "email"
//        val properties = listOf(propertyToUpdate)
//        val itemId = "itemId1"
//
//        val spy = Mockito.spy(logService)
//        Mockito.lenient().doReturn(listOf<File>()).`when`(spy).getAllLogFiles()
//
//        val directoryBefore = File(logService.logFileDirectory).listFiles()
//        Assertions.assertTrue(directoryBefore!!.isEmpty())
//
//        val result = spy.censureLogChangesText(properties, itemId)
//
//        Assertions.assertNotNull(result)
//        Assertions.assertTrue(result.isEmpty())
//    }
//
//    @Test
//    fun testCensureLogChangesText_Normal_ReturnFile()
//    {
//        val file = makeAndGetLogChangesFile()
//        val propertyToUpdate = "email"
//        val properties = listOf(propertyToUpdate)
//        val itemId = "itemId1"
//
//        val directoryBefore = File(logService.logFileDirectory).listFiles()
//        Assertions.assertTrue(directoryBefore!!.isNotEmpty())
//
//        val spy = Mockito.spy(logService)
//        Mockito.lenient().doReturn(listOf(file)).`when`(spy).getAllLogFiles()
//        Mockito.lenient().doReturn(null).`when`(spy).censureLogChangesTextFile(MockitoHelper.anyObject(), MockitoHelper.anyObject(), Mockito.anyString()) // Return value not important
//
//        val result = spy.censureLogChangesText(properties, itemId)
//
//        Assertions.assertNotNull(result)
//        Assertions.assertTrue(result.isNotEmpty())
//        Assertions.assertEquals(file.name, result.first())
//    }
//    // endregion
//
//    // region censureLogChangesTextFile
//    @Test
//    fun testCensureLogChangesTextFile_NoSuchPropery_ReturnFile()
//    {
//        val file = makeAndGetLogChangesFile()
//        val propertyToUpdate = "NoSuchPropery"
//        val properties = listOf(propertyToUpdate)
//        val itemId = "itemId1"
//
//        val directoryBefore = File(logService.logFileDirectory).listFiles()
//        Assertions.assertTrue(directoryBefore!!.isNotEmpty())
//        val fileBefore = directoryBefore.first()
//        val linesBefore = fileBefore.readLines().toMutableList()
//
//        val result = logService.censureLogChangesTextFile(file, properties, itemId)
//
//        Assertions.assertNotNull(result)
//        Assertions.assertEquals(file.name, result.name)
//
//        val directoryAfter = File(logService.logFileDirectory).listFiles()
//        Assertions.assertTrue(directoryAfter!!.isNotEmpty())
//        val fileAfter = directoryAfter.first()
//        Assertions.assertNotNull(fileAfter)
//        Assertions.assertEquals(file.name, fileAfter.name)
//
//        val linesAfter = fileBefore.readLines().toMutableList()
//        Assertions.assertEquals(linesBefore.size, linesAfter.size)
//
//        for(i in linesBefore.indices)
//        {
//            Assertions.assertEquals(linesBefore[i], linesAfter[i])
//        }
//    }
//
//    @Test
//    fun testCensureLogChangesTextFile_NoSuchItem_ReturnFile()
//    {
//        val file = makeAndGetLogChangesFile()
//        val propertyToUpdate = "email"
//        val properties = listOf(propertyToUpdate)
//        val itemId = "NoSuchItem"
//
//        val directoryBefore = File(logService.logFileDirectory).listFiles()
//        Assertions.assertTrue(directoryBefore!!.isNotEmpty())
//        val fileBefore = directoryBefore.first()
//        val linesBefore = fileBefore.readLines().toMutableList()
//
//        val result = logService.censureLogChangesTextFile(file, properties, itemId)
//
//        Assertions.assertNotNull(result)
//        Assertions.assertEquals(file.name, result.name)
//
//        val directoryAfter = File(logService.logFileDirectory).listFiles()
//        Assertions.assertTrue(directoryAfter!!.isNotEmpty())
//        val fileAfter = directoryAfter.first()
//        Assertions.assertNotNull(fileAfter)
//        Assertions.assertEquals(file.name, fileAfter.name)
//
//        val linesAfter = fileBefore.readLines().toMutableList()
//        Assertions.assertEquals(linesBefore.size, linesAfter.size)
//
//        for(i in linesBefore.indices)
//        {
//            Assertions.assertEquals(linesBefore[i], linesAfter[i])
//        }
//    }
//
//    @Test
//    fun testCensureLogChangesTextFile_Normal_ReturnFile()
//    {
//        val file = makeAndGetLogChangesFile()
//        val propertyToUpdate = "email"
//        val properties = listOf(propertyToUpdate)
//        val itemId = "itemId1"
//        val propertyLineStart = "\t${propertyToUpdate}:"
//        val logLineStringMock = "$propertyLineStart <censured> -> <censured>"
//
//        val spy = Mockito.spy(logService)
//        Mockito.lenient().doReturn(logLineStringMock).`when`(spy).getLogLinesPropertyString(Mockito.anyString(), Mockito.anyString(), Mockito.anyString())
//
//        val directoryBefore = File(logService.logFileDirectory).listFiles()
//        Assertions.assertTrue(directoryBefore!!.isNotEmpty())
//        val fileBefore = directoryBefore.first()
//        val linesBefore = fileBefore.readLines().toMutableList()
//
//        val result = spy.censureLogChangesTextFile(file, properties, itemId)
//
//        Assertions.assertNotNull(result)
//        Assertions.assertEquals(file.name, result.name)
//
//        val directoryAfter = File(logService.logFileDirectory).listFiles()
//        Assertions.assertTrue(directoryAfter!!.isNotEmpty())
//        val fileAfter = directoryAfter.first()
//        Assertions.assertNotNull(fileAfter)
//        Assertions.assertEquals(file.name, fileAfter.name)
//
//        val linesAfter = fileBefore.readLines().toMutableList()
//        Assertions.assertEquals(linesBefore.size, linesAfter.size)
//
//        // Only first occurrence is changed, with itemId "itemId1"
//        val propertyLineBefore = linesBefore.first { e -> e.startsWith(propertyLineStart) }
//        val propertyLineAfter = linesAfter.first { e -> e.startsWith(propertyLineStart) }
//        Assertions.assertNotNull(propertyLineBefore)
//        Assertions.assertNotNull(propertyLineAfter)
//        Assertions.assertNotEquals(propertyLineBefore, propertyLineAfter)
//
//        val indexBefore = linesBefore.indexOf(propertyLineBefore)
//        val indexAfter = linesAfter.indexOf(propertyLineAfter)
//        linesBefore.removeAt(indexBefore)
//        linesAfter.removeAt(indexAfter)
//        for(i in linesBefore.indices)
//        {
//            Assertions.assertEquals(linesBefore[i], linesAfter[i])
//        }
//    }
//    // endregion
//
//    // region logChanges
//    @Disabled // What happens when all methods are implemented?
//    @Test
//    fun testLogChanges_LogSinkNotImplemented_ThrowNotImplementedException()
//    {
//        logService.logSink = LogSink.DEFINED
//        val itemId = "id1"
//        val before = ChookUser(id = itemId, username = "username1")
//        val after = ChookUser(id = itemId, username = "username2")
//        val propertyName = "username"
//        val editorId = "editor1"
//        val automatedChange = false
//        val changes = before::class.declaredMemberProperties.filter { it.name == propertyName }
//        val logHead = LogHead()
//
//        val spy = Mockito.spy(logService)
//        Mockito.lenient().doReturn(logHead).`when`(spy).getLogData(
//            MockitoHelper.anyObject(), MockitoHelper.anyObject(), Mockito.anyString(), Mockito.anyString(), Mockito.anyBoolean(), MockitoHelper.anyObject())
//
//        try
//        {
//            logService.logChanges(before, after, itemId, editorId, automatedChange, changes)
//            Assertions.fail() // Fail here
//        }
//        catch(e: NotImplementedException)
//        {
//            Assertions.assertTrue(true)
//        }
//    }
//
//    @Test
//    fun testLogChanges_LogsDisabled_ReturnNull()
//    {
//        logService.logSink = LogSink.TEXT_FILE
//        logService.disableLogs = true
//        val itemId = "id1"
//        val before = ChookUser(id = itemId, username = "username1")
//        val after = ChookUser(id = itemId, username = "username2")
//        val propertyName = "username"
//        val editorId = "editor1"
//        val automatedChange = false
//        val changes = before::class.declaredMemberProperties.filter { it.name == propertyName }
//
//        val result = logService.logChanges(before, after, itemId, editorId, automatedChange, changes)
//
//        Assertions.assertNull(result)
//    }
//
//    @Test
//    fun testLogChanges_NoChanges_ReturnNull()
//    {
//        logService.logSink = LogSink.TEXT_FILE
//        val itemId = "id1"
//        val before = ChookUser(id = itemId, username = "username1")
//        val after = ChookUser(id = itemId, username = "username2")
//        val editorId = "editor1"
//        val automatedChange = false
//        val changes = listOf<KProperty1<out ChookUser, *>>()
//
//        val spy = Mockito.spy(logService)
//        Mockito.lenient().doReturn(changes).`when`(spy).getChanges(MockitoHelper.anyObject(), MockitoHelper.anyObject())
//
//        val result = spy.logChanges(before, after, itemId, editorId, automatedChange, null)
//
//        Assertions.assertNull(result)
//    }
//
//    @Test
//    fun testLogChanges_GetChanges_ReturnLogHead()
//    {
//        logService.logSink = LogSink.TEXT_FILE
//        val itemId = "id1"
//        val before = ChookUser(id = itemId, username = "username1")
//        val after = ChookUser(id = itemId, username = "username2")
//        val propertyName = "username"
//        val editorId = "editor1"
//        val automatedChange = false
//        val changes = before::class.declaredMemberProperties.filter { it.name == propertyName }
//        val logHead = LogHead()
//
//        val spy = Mockito.spy(logService)
//        Mockito.lenient().doReturn(changes).`when`(spy).getChanges(MockitoHelper.anyObject(), MockitoHelper.anyObject())
//        Mockito.lenient().doReturn(logHead).`when`(spy).getLogData(
//            MockitoHelper.anyObject(), MockitoHelper.anyObject(), Mockito.anyString(), Mockito.anyString(), Mockito.anyBoolean(), MockitoHelper.anyObject())
//        Mockito.lenient().doReturn("log as string").`when`(spy).logChangesText(MockitoHelper.anyObject())
//
//        val result = spy.logChanges(before, after, itemId, editorId, automatedChange)
//
//        Assertions.assertNotNull(result)
//        Assertions.assertEquals(logHead, result)
//    }
//
//    @Test
//    fun testLogChanges_TextFile_ReturnLogHead()
//    {
//        logService.logSink = LogSink.TEXT_FILE
//        val itemId = "id1"
//        val before = ChookUser(id = itemId, username = "username1")
//        val after = ChookUser(id = itemId, username = "username2")
//        val propertyName = "username"
//        val editorId = "editor1"
//        val automatedChange = false
//        val changes = before::class.declaredMemberProperties.filter { it.name == propertyName }
//        val operation = LogOperation.EDITED
//        val itemType = LogItemType.USER
//        val logLine = LogLine(fieldName = propertyName,
//            oldValue = before.username,
//            newValue = after.username,)
//        val logHead = LogHead(operation = operation,
//            itemType = itemType,
//            itemId = itemId,
//            editorId = editorId,
//            automatedChange = automatedChange,
//            fieldsUpdated = propertyName,
//            logLines = mutableListOf(logLine))
//
//        val spy = Mockito.spy(logService)
//        Mockito.lenient().doReturn(logHead).`when`(spy).getLogData(
//            MockitoHelper.anyObject(), MockitoHelper.anyObject(), Mockito.anyString(), Mockito.anyString(), Mockito.anyBoolean(), MockitoHelper.anyObject())
//        Mockito.lenient().doReturn("log as string").`when`(spy).logChangesText(MockitoHelper.anyObject())
//
//        val result = spy.logChanges(before, after, itemId, editorId, automatedChange, changes)
//
//        Assertions.assertNotNull(result)
//        Assertions.assertEquals(itemType, result!!.itemType)
//        Assertions.assertEquals(itemId, result.itemId)
//        Assertions.assertEquals(editorId, result.editorId)
//        Assertions.assertEquals(automatedChange, result.automatedChange)
//        Assertions.assertEquals(propertyName, result.fieldsUpdated)
//        Assertions.assertNotNull(result.logLines)
//        Assertions.assertEquals(1, result.logLines!!.size)
//        //Assertions.assertEquals(???, result.logLines!!.first().logHeadId)
//        Assertions.assertEquals(propertyName, result.logLines!!.first().fieldName)
//        Assertions.assertEquals(before.username, result.logLines!!.first().oldValue)
//        Assertions.assertEquals(after.username, result.logLines!!.first().newValue)
//    }
//
//    @Test
//    fun testLogChanges_DefinedNull_ThrowArgumentException()
//    {
//        logService.logSink = LogSink.DEFINED
//        logService.definedLogChangesMethod = null
//        val itemId = "id1"
//        val before = ChookUser(id = itemId, username = "username1")
//        val after = ChookUser(id = itemId, username = "username2")
//        val propertyName = "username"
//        val editorId = "editor1"
//        val automatedChange = false
//        val changes = before::class.declaredMemberProperties.filter { it.name == propertyName }
//        val operation = LogOperation.EDITED
//        val itemType = LogItemType.USER
//        val logLine = LogLine(fieldName = propertyName,
//            oldValue = before.username,
//            newValue = after.username,)
//        val logHead = LogHead(operation = operation,
//            itemType = itemType,
//            itemId = itemId,
//            editorId = editorId,
//            automatedChange = automatedChange,
//            fieldsUpdated = propertyName,
//            logLines = mutableListOf(logLine))
//
//        val spy = Mockito.spy(logService)
//        Mockito.lenient().doReturn(logHead).`when`(spy).getLogData(
//            MockitoHelper.anyObject(), MockitoHelper.anyObject(), Mockito.anyString(), Mockito.anyString(), Mockito.anyBoolean(), MockitoHelper.anyObject())
//        Mockito.lenient().doReturn("log as string").`when`(spy).logChangesText(MockitoHelper.anyObject())
//
//        try
//        {
//            spy.logChanges(before, after, itemId, editorId, automatedChange, changes)
//            Assertions.fail() // Fail here
//        }
//        catch(e: ArgumentException)
//        {
//            Assertions.assertTrue(true)
//        }
//    }
//
//    @Test
//    fun testLogChanges_DefinedMethod_ReturnLogHead()
//    {
//        logService.logFileDirectory = testOutputDirectory
//        logService.logSink = LogSink.DEFINED
//        logService.definedLogChangesMethod = ::myChangesLogging
//        val itemId = "id1"
//        val before = ChookUser(id = itemId, username = "username1")
//        val after = ChookUser(id = itemId, username = "username2")
//        val propertyName = "username"
//        val editorId = "editor1"
//        val automatedChange = false
//        val changes = before::class.declaredMemberProperties.filter { it.name == propertyName }
//        val operation = LogOperation.EDITED
//        val itemType = LogItemType.USER
//        val logLine = LogLine(fieldName = propertyName,
//            oldValue = before.username,
//            newValue = after.username,)
//        val logHead = LogHead(operation = operation,
//            itemType = itemType,
//            itemId = itemId,
//            editorId = editorId,
//            automatedChange = automatedChange,
//            fieldsUpdated = propertyName,
//            logLines = mutableListOf(logLine))
//
//        val spy = Mockito.spy(logService)
//        Mockito.lenient().doReturn(logHead).`when`(spy).getLogData(
//            MockitoHelper.anyObject(), MockitoHelper.anyObject(), Mockito.anyString(), Mockito.anyString(), Mockito.anyBoolean(), MockitoHelper.anyObject())
//        Mockito.lenient().doReturn("log as string").`when`(spy).logChangesText(MockitoHelper.anyObject())
//
//        val directoryBefore = File(logService.logFileDirectory).listFiles()
//        Assertions.assertTrue(directoryBefore!!.isEmpty())
//
//        val result = spy.logChanges(before, after, itemId, editorId, automatedChange, changes)
//
//        val directoryAfter = File(logService.logFileDirectory).listFiles()
//        Assertions.assertTrue(directoryAfter!!.isNotEmpty())
//
//        Assertions.assertNotNull(result)
//        val definedMethodLogFile = directoryAfter.first()
//        Assertions.assertEquals(definedMethodLogFileName, definedMethodLogFile.name)
//        val file = File(definedMethodLogFile.absolutePath)
//        val lines = file.readLines()
//        Assertions.assertEquals(1, lines.size)
//        Assertions.assertEquals(lines.first(), logHead.itemId)
//    }
//    // endregion
//
//    // region logChangesText
//    @Test
//    fun testLogChangesText_ClassPropertyFile_ReturnLogHead()
//    {
//        logService.logFileDirectory = testOutputDirectory
//        logService.logFilePrefix = "Test"
//        logService.logFileExtension = "log"
//        val logHead = LogHead()
//        val now = Instant.now().toString().replace(":", "+")
//        val fileName = "${logService.logFilePrefix}_${now}.${logService.logFileExtension}"
//        val path = Paths.get(logService.logFileDirectory, fileName).toString()
//        val file = File(path)
//        file.appendText("testLogText_ClassPropertyFile_ReturnLogHead")
//        logService.logFile = file
//        val loggedData = "\nlogged data"
//
//        val spy = Mockito.spy(logService)
//        Mockito.lenient().doReturn(loggedData).`when`(spy).getLogChangesString(MockitoHelper.anyObject())
//
//        val directoryBefore = File(logService.logFileDirectory).listFiles()
//        Assertions.assertTrue(directoryBefore!!.isNotEmpty())
//
//        val result = spy.logChangesText(logHead)
//
//        val directoryAfter = File(logService.logFileDirectory).listFiles()
//        Assertions.assertTrue(directoryAfter!!.isNotEmpty())
//
//        Assertions.assertNotNull(result)
//        Assertions.assertEquals(fileName, result)
//
//        val resultFile = File(path)
//        Assertions.assertNotNull(resultFile)
//        Assertions.assertTrue(resultFile.isFile)
//        val resultFileContent = resultFile.readText()
//        Assertions.assertTrue(resultFileContent.contains(loggedData))
//    }
//
//    @Test
//    fun testLogChangesText_GetLatestFile_ReturnLogHead()
//    {
//        logService.logFileDirectory = testOutputDirectory
//        logService.logFilePrefix = "Test"
//        logService.logFileExtension = "log"
//        val logHead = LogHead()
//        val now = Instant.now().toString().replace(":", "+")
//        val fileName = "${logService.logFilePrefix}_${now}.${logService.logFileExtension}"
//        val path = Paths.get(logService.logFileDirectory, fileName).toString()
//        val file = File(path)
//        file.appendText("testLogText_ClassPropertyFile_ReturnLogHead")
//        logService.logFile = null
//        val loggedData = "\nlogged data"
//
//        val spy = Mockito.spy(logService)
//        Mockito.lenient().doReturn(file).`when`(spy).getLatestLogFile()
//        Mockito.lenient().doReturn(loggedData).`when`(spy).getLogChangesString(MockitoHelper.anyObject())
//
//        val directoryBefore = File(logService.logFileDirectory).listFiles()
//        Assertions.assertTrue(directoryBefore!!.isNotEmpty())
//
//        val result = spy.logChangesText(logHead)
//
//        val directoryAfter = File(logService.logFileDirectory).listFiles()
//        Assertions.assertTrue(directoryAfter!!.isNotEmpty())
//
//        Assertions.assertNotNull(result)
//        Assertions.assertEquals(fileName, result)
//
//        val resultFile = File(path)
//        Assertions.assertNotNull(resultFile)
//        Assertions.assertTrue(resultFile.isFile)
//        val resultFileContent = resultFile.readText()
//        Assertions.assertTrue(resultFileContent.contains(loggedData))
//    }
//    // endregion
//
//    // region getLatestLogFile
//    @Test
//    fun testGetLatestLogFile_NoMatchCreateNew_ReturnFile()
//    {
//        logService.logFileDirectory = testOutputDirectory
//        logService.logFilePrefix = "Test"
//        logService.logFileExtension = "log"
//        val otherPath = Paths.get(logService.logFileDirectory, "some_other_file.txt").toString()
//        val otherFile = File(otherPath)
//        otherFile.appendText("testGetLatestLogFile_NoMatchCreateNew_ReturnFile")
//
//        val now = Instant.now().minus(99, ChronoUnit.DAYS)
//        val nowFilenameSafe = now.toString().replace(":", "+")
//        val path = Paths.get(logService.logFileDirectory, "${logService.logFilePrefix}_${nowFilenameSafe}.${logService.logFileExtension}").toString()
//        val file = File(path)
//
//        val spy = Mockito.spy(logService)
//        Mockito.lenient().doReturn(listOf(file)).`when`(spy).getAllLogFiles()
//        Mockito.lenient().doReturn(file).`when`(spy).getNewLogFile()
//
//        val directoryBefore = File(logService.logFileDirectory).listFiles()
//        Assertions.assertTrue(directoryBefore!!.isNotEmpty())
//
//        val result = spy.getLatestLogFile()
//
//        Assertions.assertNotNull(result)
//        Assertions.assertTrue(result.name.contains(logService.logFilePrefix))
//        Assertions.assertTrue(result.name.contains(nowFilenameSafe))
//        Assertions.assertTrue(result.name.contains(".${logService.logFileExtension}"))
//    }
//
//    @Test
//    fun testGetLatestLogFile_SingleFileOutdatedGetNew_ReturnFile()
//    {
//        logService.logFileRotatePeriod = Duration.ofMinutes(1)
//        logService.logFileDirectory = testOutputDirectory
//        logService.logFilePrefix = "Test"
//        logService.logFileExtension = "log"
//        val past1 = Instant.now().minus(2, ChronoUnit.DAYS)
//        val past1FilenameSafe = past1.toString().replace(":", "+")
//        val past1Path = Paths.get(logService.logFileDirectory, "${logService.logFilePrefix}_${past1FilenameSafe}.${logService.logFileExtension}").toString()
//        val past1File = File(past1Path)
//        past1File.appendText("testGetLatestLogFile_SingleFileOutdatedGetNew_ReturnFile past1")
//
//        val now = Instant.now().minus(99, ChronoUnit.DAYS)
//        val nowFilenameSafe = now.toString().replace(":", "+")
//        val path = Paths.get(logService.logFileDirectory, "${logService.logFilePrefix}_${nowFilenameSafe}.${logService.logFileExtension}").toString()
//        val file = File(path)
//
//        val spy = Mockito.spy(logService)
//        Mockito.lenient().doReturn(listOf(past1File)).`when`(spy).getAllLogFiles()
//        Mockito.lenient().doReturn(now).`when`(spy).getLogFileInstant(Mockito.anyString())
//        Mockito.lenient().doReturn(file).`when`(spy).getNewLogFile()
//
//        val result = spy.getLatestLogFile()
//
//        Assertions.assertNotNull(result)
//        Assertions.assertTrue(result.name.contains(logService.logFilePrefix))
//        Assertions.assertTrue(result.name.contains(nowFilenameSafe))
//        Assertions.assertTrue(result.name.contains(".${logService.logFileExtension}"))
//    }
//
//    @Test
//    fun testGetLatestLogFile_DisableRotation_ReturnFile()
//    {
//        logService.disableLogRotation = true
//        logService.logFileRotatePeriod = Duration.ofDays(1)
//        logService.logFileDirectory = testOutputDirectory
//        logService.logFilePrefix = "Test"
//        logService.logFileExtension = "log"
//        val now = Instant.now().minus(99, ChronoUnit.DAYS)
//        val nowFilenameSafe = now.toString().replace(":", "+")
//        val path = Paths.get(logService.logFileDirectory, "${logService.logFilePrefix}_${nowFilenameSafe}.${logService.logFileExtension}").toString()
//        val file = File(path)
//        file.appendText("testGetLatestLogFile_SingleReturnOnlyExisting_ReturnFile")
//
//        val spy = Mockito.spy(logService)
//        Mockito.lenient().doReturn(listOf(file)).`when`(spy).getAllLogFiles()
//        Mockito.lenient().doReturn(now).`when`(spy).getLogFileInstant(Mockito.anyString())
//
//        val directoryBefore = File(logService.logFileDirectory).listFiles()
//        Assertions.assertTrue(directoryBefore!!.isNotEmpty())
//
//        val result = spy.getLatestLogFile()
//
//        val directoryAfter = File(logService.logFileDirectory).listFiles()
//        Assertions.assertEquals(1, directoryBefore.size)
//
//        Assertions.assertNotNull(result)
//        Assertions.assertTrue(result.name.contains(logService.logFilePrefix))
//        Assertions.assertTrue(result.name.contains(nowFilenameSafe))
//        Assertions.assertTrue(result.name.contains(".${logService.logFileExtension}"))
//        Assertions.assertTrue(File(Paths.get(logService.logFileDirectory, result.name).toString()).isFile) // File exist in path, not just as a Kotlin object
//    }
//
//    @Test
//    fun testGetLatestLogFile_SingleReturnOnlyExisting_ReturnFile()
//    {
//        logService.logFileRotatePeriod = Duration.ofDays(1)
//        logService.logFileDirectory = testOutputDirectory
//        logService.logFilePrefix = "Test"
//        logService.logFileExtension = "log"
//        val now = Instant.now()
//        val nowFilenameSafe = now.toString().replace(":", "+")
//        val path = Paths.get(logService.logFileDirectory, "${logService.logFilePrefix}_${nowFilenameSafe}.${logService.logFileExtension}").toString()
//        val file = File(path)
//        file.appendText("testGetLatestLogFile_SingleReturnOnlyExisting_ReturnFile")
//
//        val spy = Mockito.spy(logService)
//        Mockito.lenient().doReturn(listOf(file)).`when`(spy).getAllLogFiles()
//        Mockito.lenient().doReturn(now).`when`(spy).getLogFileInstant(Mockito.anyString())
//
//        val directoryBefore = File(logService.logFileDirectory).listFiles()
//        Assertions.assertTrue(directoryBefore!!.isNotEmpty())
//
//        val result = spy.getLatestLogFile()
//
//        Assertions.assertNotNull(result)
//        Assertions.assertTrue(result.name.contains(logService.logFilePrefix))
//        Assertions.assertTrue(result.name.contains(nowFilenameSafe))
//        Assertions.assertTrue(result.name.contains(".${logService.logFileExtension}"))
//        Assertions.assertTrue(File(Paths.get(logService.logFileDirectory, result.name).toString()).isFile) // File exist in path, not just as a Kotlin object
//    }
//
//    @Test
//    fun testGetLatestLogFile_MultipleReturnCurrentLog_ReturnFile()
//    {
//        logService.logFileRotatePeriod = Duration.ofDays(1)
//        logService.logFileDirectory = testOutputDirectory
//        logService.logFilePrefix = "Test"
//        logService.logFileExtension = "log"
//        val past1 = Instant.now().minus(2, ChronoUnit.DAYS)
//        val past1FilenameSafe = past1.toString().replace(":", "+")
//        val past1Path = Paths.get(logService.logFileDirectory, "${logService.logFilePrefix}_${past1FilenameSafe}.${logService.logFileExtension}").toString()
//        val past1File = File(past1Path)
//        past1File.appendText("testGetLatestLogFile_MultipleReturnCurrentLog_ReturnFile past1")
//
//        val past2 = Instant.now().minus(3, ChronoUnit.DAYS)
//        val past2FilenameSafe = past2.toString().replace(":", "+")
//        val past2Path = Paths.get(logService.logFileDirectory, "${logService.logFilePrefix}_${past2FilenameSafe}.${logService.logFileExtension}").toString()
//        val past2File = File(past2Path)
//        past2File.appendText("testGetLatestLogFile_MultipleReturnCurrentLog_ReturnFile past2")
//
//        val now = Instant.now()
//        val nowFilenameSafe = now.toString().replace(":", "+")
//        val path = Paths.get(logService.logFileDirectory, "${logService.logFilePrefix}_${nowFilenameSafe}.${logService.logFileExtension}").toString()
//        val file = File(path)
//        file.appendText("testGetLatestLogFile_MultipleReturnCurrentLog_ReturnFile")
//
//        val spy = Mockito.spy(logService)
//        Mockito.lenient().doReturn(listOf(past1File, past2File, file)).`when`(spy).getAllLogFiles()
//        Mockito.lenient().doReturn(now).`when`(spy).getLogFileInstant(Mockito.anyString())
//
//        val directoryBefore = File(logService.logFileDirectory).listFiles()
//        Assertions.assertEquals(3, directoryBefore!!.size)
//
//        val result = spy.getLatestLogFile()
//
//        val directoryAfter = File(logService.logFileDirectory).listFiles()
//        Assertions.assertEquals(3, directoryAfter!!.size)
//
//        Assertions.assertNotNull(result)
//        Assertions.assertTrue(result.name.contains(logService.logFilePrefix))
//        Assertions.assertTrue(result.name.contains(nowFilenameSafe))
//        Assertions.assertTrue(result.name.contains(".${logService.logFileExtension}"))
//        Assertions.assertTrue(File(Paths.get(logService.logFileDirectory, result.name).toString()).isFile) // File exist in path, not just as a Kotlin object
//    }
//
//    @Test
//    fun testGetLatestLogFile_NoneCreateNew_ReturnFile()
//    {
//        logService.logFileDirectory = testOutputDirectory
//        logService.logFilePrefix = "Test"
//        logService.logFileExtension = "log"
//
//        val spy = Mockito.spy(logService)
//        Mockito.lenient().doReturn(listOf<File>()).`when`(spy).getAllLogFiles()
//
//        val directoryBefore = File(logService.logFileDirectory).listFiles()
//        Assertions.assertTrue(directoryBefore!!.isEmpty())
//
//        val result = spy.getLatestLogFile()
//
//        val directoryAfter = File(logService.logFileDirectory).listFiles()
//        Assertions.assertTrue(directoryAfter!!.isNotEmpty())
//
//        Assertions.assertNotNull(result)
//        Assertions.assertTrue(result.name.contains(logService.logFilePrefix))
//        Assertions.assertTrue(result.name.contains(".${logService.logFileExtension}"))
//        Assertions.assertTrue(File(Paths.get(logService.logFileDirectory, result.name).toString()).isFile) // File exist in path, not just as a Kotlin object
//    }
//    // endregion
//
//    // region getNewLogFile
//    @Test
//    fun testGetNewLogFile_Normal_ReturnFile()
//    {
//        logService.logFileDirectory = testOutputDirectory
//        val mockedFilename = "testGetNewLogFile_Normal_ReturnLogHead.log"
//        val path = Paths.get(logService.logFileDirectory, mockedFilename).toString()
//
//        val directoryBefore = File(logService.logFileDirectory).listFiles()
//        Assertions.assertTrue(directoryBefore!!.isEmpty())
//
//        val spy = Mockito.spy(logService)
//        Mockito.lenient().doReturn(mockedFilename).`when`(spy).getLogFileFilename()
//
//        val result = spy.getNewLogFile()
//
//        val directoryAfter = File(logService.logFileDirectory).listFiles()
//        Assertions.assertTrue(directoryAfter!!.isNotEmpty())
//
//        Assertions.assertTrue(File(path).exists())
//        Assertions.assertTrue(File(path).isFile)
//        Assertions.assertNotNull(result)
//        Assertions.assertTrue(result.length() > 0)
//    }
//    // endregion
//
//    // region getLogFileInstant
//    @Test
//    fun testGetLogFileInstant_Malformed_ThrowDateTimeParseException()
//    {
//        logService.logFilePrefix = "Test"
//        logService.logFileExtension = "log"
//        val now = Instant.now()
//        val nowFilenameSafe = now.toString().replace(":", "-") // Note: not expected replacement
//        val filename = "${logService.logFilePrefix}_${nowFilenameSafe}.${logService.logFileExtension}"
//
//        try
//        {
//            logService.getLogFileInstant(filename)
//            Assertions.fail() // Fail here
//        }
//        catch(e: DateTimeParseException)
//        {
//            Assertions.assertTrue(true)
//        }
//    }
//
//    @Test
//    fun testGetLogFileInstant_Normal_ReturnInstant()
//    {
//        logService.logFilePrefix = "Test"
//        logService.logFileExtension = "log"
//        val now = Instant.now()
//        val nowFilenameSafe = now.toString().replace(":", "+")
//        val filename = "${logService.logFilePrefix}_${nowFilenameSafe}.${logService.logFileExtension}"
//        val nowDateTime = ZonedDateTime.ofInstant(now, ZoneId.of("UTC"))
//
//        val result = logService.getLogFileInstant(filename)
//
//        Assertions.assertNotNull(result)
//        val resultDateTime = ZonedDateTime.ofInstant(result, ZoneId.of("UTC"))
//        Assertions.assertEquals(nowDateTime.year, resultDateTime.year)
//        Assertions.assertEquals(nowDateTime.monthValue, resultDateTime.monthValue)
//        Assertions.assertEquals(nowDateTime.dayOfMonth, resultDateTime.dayOfMonth)
//        Assertions.assertEquals(nowDateTime.hour, resultDateTime.hour)
//        Assertions.assertEquals(nowDateTime.minute, resultDateTime.minute)
//        Assertions.assertEquals(nowDateTime.second, resultDateTime.second)
//        Assertions.assertEquals(nowDateTime.toEpochSecond(), resultDateTime.toEpochSecond())
//    }
//    // endregion
//
//    // region getLogFileFilename
//    @Test
//    fun testGetLogFileFilename_Normal_ReturnString()
//    {
//        logService.logFilePrefix = "Test"
//        logService.logFileExtension = "log"
//
//        val result = logService.getLogFileFilename()
//
//        Assertions.assertNotNull(result)
//        Assertions.assertTrue(result.contains("${logService.logFilePrefix}_"))
//        val fileNameDateTimeRegex = """\d+-\d+-\d+T\d+\+\d+\+\d+\.\d+Z""".toRegex()
//        Assertions.assertTrue(result.contains(fileNameDateTimeRegex))
//        Assertions.assertTrue(result.contains(".${logService.logFileExtension}"))
//    }
//    // endregion
//
//    // region getLogChangesString
//    @Test
//    fun testGetLogChangesString_NormalAutomated_ReturnString()
//    {
//        val itemId = "id1"
//        val before = ChookUser(id = itemId, username = "username1")
//        val after = ChookUser(id = itemId, username = "username2")
//        val propertyName = "username"
//        val editorId = "editor1"
//        val automatedChange = true
//        val operation = LogOperation.EDITED
//        val itemType = LogItemType.USER
//        val logLine = LogLine(fieldName = propertyName,
//            oldValue = before.username,
//            newValue = after.username,)
//        val logHead = LogHead(operation = operation,
//            itemType = itemType,
//            itemId = itemId,
//            registered = "now",
//            editorId = editorId,
//            automatedChange = automatedChange,
//            fieldsUpdated = propertyName,
//            logLines = mutableListOf(logLine))
//        val logLineStringMock = "logged_changes"
//
//        val spy = Mockito.spy(logService)
//        Mockito.lenient().doReturn(logLineStringMock).`when`(spy).getLogLinesPropertyString(Mockito.anyString(), Mockito.anyString(), Mockito.anyString())
//
//        val result = spy.getLogChangesString(logHead)
//
//        Assertions.assertNotNull(result)
//        Assertions.assertTrue(result.contains(logHead.operation!!.name))
//        Assertions.assertTrue(result.contains(logHead.itemType!!.name))
//        Assertions.assertTrue(result.contains(logHead.itemId!!))
//        Assertions.assertTrue(result.contains(logHead.registered!!))
//        Assertions.assertTrue(result.contains(logHead.editorId!!))
//        Assertions.assertTrue(result.contains("automated"))
//        Assertions.assertTrue(result.contains(propertyName))
//        Assertions.assertTrue(result.contains(logLineStringMock))
//    }
//
//    @Test
//    fun testGetLogChangesString_NormalManual_ReturnString()
//    {
//        val itemId = "id1"
//        val before = ChookUser(id = itemId, username = "username1")
//        val after = ChookUser(id = itemId, username = "username2")
//        val propertyName = "username"
//        val editorId = "editor1"
//        val automatedChange = false
//        val operation = LogOperation.EDITED
//        val itemType = LogItemType.USER
//        val logLine = LogLine(fieldName = propertyName,
//            oldValue = before.username,
//            newValue = after.username,)
//        val logHead = LogHead(operation = operation,
//            itemType = itemType,
//            itemId = itemId,
//            registered = "now",
//            editorId = editorId,
//            automatedChange = automatedChange,
//            fieldsUpdated = propertyName,
//            logLines = mutableListOf(logLine))
//        val logLineStringMock = "logged_changes"
//
//        val spy = Mockito.spy(logService)
//        Mockito.lenient().doReturn(logLineStringMock).`when`(spy).getLogLinesPropertyString(Mockito.anyString(), Mockito.anyString(), Mockito.anyString())
//
//        val result = spy.getLogChangesString(logHead)
//
//        Assertions.assertNotNull(result)
//        Assertions.assertTrue(result.contains(logHead.operation!!.name))
//        Assertions.assertTrue(result.contains(logHead.itemType!!.name))
//        Assertions.assertTrue(result.contains(logHead.itemId!!))
//        Assertions.assertTrue(result.contains(logHead.registered!!))
//        Assertions.assertTrue(result.contains(logHead.editorId!!))
//        Assertions.assertTrue(result.contains("manual"))
//        Assertions.assertTrue(result.contains(propertyName))
//        Assertions.assertTrue(result.contains(logLineStringMock))
//    }
//    // endregion
//
//    // region getLogLinesPropertyString
//    @Test
//    fun testGetLogLinesPropertyString_Null_ReturnString()
//    {
//        val propertyName: String? = null
//        val before: String? = null
//        val after: String? = null
//
//        val result = logService.getLogLinesPropertyString(propertyName, before, after)
//
//        Assertions.assertNotNull(result)
//        Assertions.assertTrue(result.contains(" -> "))
//        Assertions.assertTrue(result.contains("null")) // one "null" per null argument
//    }
//
//    @Test
//    fun testGetLogLinesPropertyString_Normal_ReturnString()
//    {
//        val propertyName = "username"
//        val before = "username1"
//        val after = "username2"
//
//        val result = logService.getLogLinesPropertyString(propertyName, before, after)
//
//        Assertions.assertNotNull(result)
//        Assertions.assertTrue(result.contains(propertyName))
//        Assertions.assertTrue(result.contains(before))
//        Assertions.assertTrue(result.contains(" -> "))
//        Assertions.assertTrue(result.contains(after))
//    }
//    // endregion
//
//    // region getLogData
//    @Test
//    fun testGetLogData_NoChanges_ThrowArgumentException()
//    {
//        val itemId = "id1"
//        val before = ChookUser(id = itemId, username = "username1")
//        val after = ChookUser(id = itemId, username = "username1")
//        val editorId = "editor1"
//        val automatedChange = false
//        val changes = listOf<KProperty<*>>()
//
//        try
//        {
//            logService.getLogData(before, after, itemId, editorId, automatedChange, changes)
//            Assertions.fail() // Fail here
//        }
//        catch(e: ArgumentException)
//        {
//            Assertions.assertTrue(true)
//        }
//    }
//
//    @Test
//    fun testGetLogData_NormalSingle_ReturnLogHead()
//    {
//        val itemId = "id1"
//        val before = ChookUser(id = itemId, username = "username1")
//        val after = ChookUser(id = itemId, username = "username2")
//        val propertyName = "username"
//        val editorId = "editor1"
//        val automatedChange = false
//        val changes = before::class.declaredMemberProperties.filter { it.name == propertyName }
//        val operation = LogOperation.EDITED
//        val itemType = LogItemType.USER
//
//        val spy = Mockito.spy(logService)
//        Mockito.lenient().doReturn(operation).`when`(spy).getOperation(MockitoHelper.anyObject(), MockitoHelper.anyObject())
//        Mockito.lenient().doReturn(itemType).`when`(spy).getLogItemType(MockitoHelper.anyObject(), MockitoHelper.anyObject())
//        Mockito.lenient().doReturn(propertyName).`when`(spy).getPropertiesUpdated(MockitoHelper.anyObject())
//        Mockito.lenient().doReturn(before.username).`when`(spy).getLogProperty(MockitoHelper.safeEq(before), MockitoHelper.anyObject())
//        Mockito.lenient().doReturn(after.username).`when`(spy).getLogProperty(MockitoHelper.safeEq(after), MockitoHelper.anyObject())
//
//        val result = spy.getLogData(before, after, itemId, editorId, automatedChange, changes)
//
//        Assertions.assertNotNull(result)
//        Assertions.assertEquals(itemType, result.itemType)
//        Assertions.assertEquals(itemId, result.itemId)
//        Assertions.assertEquals(editorId, result.editorId)
//        Assertions.assertEquals(automatedChange, result.automatedChange)
//        Assertions.assertEquals(propertyName, result.fieldsUpdated)
//        Assertions.assertNotNull(result.logLines)
//        Assertions.assertEquals(1, result.logLines!!.size)
//        //Assertions.assertEquals(???, result.logLines!!.first().logHeadId)
//        Assertions.assertEquals(propertyName, result.logLines!!.first().fieldName)
//        Assertions.assertEquals(before.username, result.logLines!!.first().oldValue)
//        Assertions.assertEquals(after.username, result.logLines!!.first().newValue)
//    }
//
//    @Test
//    fun testGetLogData_NormalMultiple_ReturnLogHead()
//    {
//        val itemId = "id1"
//        val before = ChookUser(id = itemId, username = "username1", email = "malformed")
//        val after = ChookUser(id = itemId, username = "username2", email = "example@example.com")
//        val propertyName1 = "username"
//        val propertyName2 = "email"
//        val propertiesUpdated = "${propertyName1}, $propertyName2"
//        val editorId = "editor1"
//        val automatedChange = false
//        val changes = before::class.declaredMemberProperties.filter { it.name == propertyName1 || it.name == propertyName2 }
//        val operation = LogOperation.EDITED
//        val itemType = LogItemType.USER
//
//        val spy = Mockito.spy(logService)
//        Mockito.lenient().doReturn(operation).`when`(spy).getOperation(MockitoHelper.anyObject(), MockitoHelper.anyObject())
//        Mockito.lenient().doReturn(itemType).`when`(spy).getLogItemType(MockitoHelper.anyObject(), MockitoHelper.anyObject())
//        Mockito.lenient().doReturn(propertiesUpdated).`when`(spy).getPropertiesUpdated(MockitoHelper.anyObject())
//        Mockito.lenient().doReturn(before.username).`when`(spy).getLogProperty(MockitoHelper.safeEq(before), MockitoHelper.safeEq(propertyName1))
//        Mockito.lenient().doReturn(after.username).`when`(spy).getLogProperty(MockitoHelper.safeEq(after), MockitoHelper.safeEq(propertyName1))
//        Mockito.lenient().doReturn(before.email).`when`(spy).getLogProperty(MockitoHelper.safeEq(before), MockitoHelper.safeEq(propertyName2))
//        Mockito.lenient().doReturn(after.email).`when`(spy).getLogProperty(MockitoHelper.safeEq(after), MockitoHelper.safeEq(propertyName2))
//
//        val result = spy.getLogData(before, after, itemId, editorId, automatedChange, changes)
//
//        Assertions.assertNotNull(result)
//        Assertions.assertEquals(itemType, result.itemType)
//        Assertions.assertEquals(itemId, result.itemId)
//        Assertions.assertEquals(editorId, result.editorId)
//        Assertions.assertEquals(automatedChange, result.automatedChange)
//        Assertions.assertEquals(propertiesUpdated, result.fieldsUpdated)
//        Assertions.assertNotNull(result.logLines)
//        Assertions.assertEquals(2, result.logLines!!.size)
//        //Assertions.assertEquals(???, result.logLines!!.first().logHeadId)
//        Assertions.assertEquals(propertyName2, result.logLines!!.first().fieldName)
//        Assertions.assertEquals(before.email, result.logLines!!.first().oldValue)
//        Assertions.assertEquals(after.email, result.logLines!!.first().newValue)
//        Assertions.assertEquals(propertyName1, result.logLines!!.last().fieldName)
//        Assertions.assertEquals(before.username, result.logLines!!.last().oldValue)
//        Assertions.assertEquals(after.username, result.logLines!!.last().newValue)
//    }
//    // endregion
//
//    // region getChanges
//    @Test
//    fun testGetChanges_BeforeNullAfterNull_ThrowArgumentException()
//    {
//        val before: ChookUser? = null
//        val after: ChookUser? = null
//
//        try
//        {
//            logService.getChanges(before, after)
//            Assertions.fail() // Fail here
//        }
//        catch(e: ArgumentException)
//        {
//            Assertions.assertTrue(true)
//        }
//    }
//
//    @Test
//    fun testGetChanges_DifferentClass_ThrowArgumentException()
//    {
//        val before = ChookUser(id = "id1")
//        val after = Recipe(id = "id1")
//
//        try
//        {
//            logService.getChanges(before, after)
//            Assertions.fail() // Fail here
//        }
//        catch(e: ArgumentException)
//        {
//            Assertions.assertTrue(true)
//        }
//    }
//
//    @Test
//    fun testGetChanges_ClassAnnotatedIgnore_ThrowArgumentException()
//    {
//        val before = LogHead(id = "id1", itemId = "0")
//        val after = LogHead(id = "id1", itemId = "123456")
//
//        val annotation = before.javaClass.isAnnotationPresent(NotLogged::class.java)
//        Assertions.assertNotNull(annotation)
//
//        try
//        {
//            logService.getChanges(before, after)
//            Assertions.fail() // Fail here
//        }
//        catch(e: ArgumentException)
//        {
//            Assertions.assertTrue(true)
//        }
//    }
//
//    @Test
//    fun testGetChanges_Equal_ReturnEmptyList()
//    {
//        val before = ChookUser(id = "id1")
//        val after = ChookUser(id = "id1")
//
//        val result = logService.getChanges(before, after)
//
//        Assertions.assertNotNull(result)
//        Assertions.assertTrue(result.isEmpty())
//    }
//
//    @Test
//    fun testGetChanges_AnnotatedIgnore_ReturnKPropertyList()
//    {
//        val before = ChookUser(id = "id1", username = "username1", moderatorComments = mutableMapOf())
//        val after = ChookUser(id = "id1", username = "username2", moderatorComments = mutableMapOf(Pair("datetime", "comment")))
//        val propertyName1 = "username"
//        val propertyName2 = "moderatorComments"
//
//        val spy = Mockito.spy(logService)
//        Mockito.lenient().doReturn(before.username).`when`(spy).getProperty(MockitoHelper.safeEq(before), MockitoHelper.safeEq(propertyName1))
//        Mockito.lenient().doReturn(after.username).`when`(spy).getProperty(MockitoHelper.safeEq(after), MockitoHelper.safeEq(propertyName1))
//        Mockito.lenient().doReturn(before.moderatorComments).`when`(spy).getProperty(MockitoHelper.safeEq(before), MockitoHelper.safeEq(propertyName2))
//        Mockito.lenient().doReturn(after.moderatorComments).`when`(spy).getProperty(MockitoHelper.safeEq(after), MockitoHelper.safeEq(propertyName2))
//
//        val annotation = before::class.memberProperties.first { it.name == propertyName2 }.javaField!!.getDeclaredAnnotation(NotLogged::class.java)
//        Assertions.assertNotNull(annotation)
//
//        val result = spy.getChanges(before, after)
//
//        Assertions.assertNotNull(result)
//        Assertions.assertTrue(result.isNotEmpty())
//        Assertions.assertEquals(1, result.size)
//        val simplified = result.map { it.name }
//        Assertions.assertTrue(simplified.contains(propertyName1))
//        Assertions.assertFalse(simplified.contains(propertyName2))
//    }
//
//    @Test
//    fun testGetChanges_ObjectAdded_ReturnKPropertyList()
//    {
//        val before: ChookUser? = null
//        val after = ChookUser(id = "id1", username = "username1", email = "example@example.com")
//        val expected = after::class.memberProperties.toList()
//
//        val result = logService.getChanges(before, after)
//
//        Assertions.assertNotNull(result)
//        Assertions.assertEquals(expected, result)
//    }
//
//    @Test
//    fun testGetChanges_ObjectDeleted_ReturnKPropertyList()
//    {
//        val before = ChookUser(id = "id1", username = "username1", email = "example@example.com")
//        val after: ChookUser? = null
//        val expected = before::class.memberProperties.toList()
//
//        val result = logService.getChanges(before, after)
//
//        Assertions.assertNotNull(result)
//        Assertions.assertEquals(expected, result)
//    }
//
//    @Test
//    fun testGetChanges_Normal_ReturnKPropertyList()
//    {
//        val before = ChookUser(id = "id1", username = "username1", email = "example@example.com")
//        val after = ChookUser(id = "id1", username = "username2", email = "some_other_example@example.com")
//        val propertyName1 = "username"
//        val propertyName2 = "email"
//
//        val spy = Mockito.spy(logService)
//        Mockito.lenient().doReturn(before.username).`when`(spy).getProperty(MockitoHelper.safeEq(before), MockitoHelper.safeEq(propertyName1))
//        Mockito.lenient().doReturn(after.username).`when`(spy).getProperty(MockitoHelper.safeEq(after), MockitoHelper.safeEq(propertyName1))
//        Mockito.lenient().doReturn(before.email).`when`(spy).getProperty(MockitoHelper.safeEq(before), MockitoHelper.safeEq(propertyName2))
//        Mockito.lenient().doReturn(after.email).`when`(spy).getProperty(MockitoHelper.safeEq(after), MockitoHelper.safeEq(propertyName2))
//
//        val result = spy.getChanges(before, after)
//
//        Assertions.assertNotNull(result)
//        Assertions.assertTrue(result.isNotEmpty())
//        Assertions.assertEquals(2, result.size)
//        val simplified = result.map { it.name }
//        Assertions.assertTrue(simplified.contains(propertyName1))
//        Assertions.assertTrue(simplified.contains(propertyName2))
//    }
//    // endregion
//
//    // region getOperation
//    @Test
//    fun testGetOperation_Added_ReturnLogOperation()
//    {
//        val before: ChookUser? = null
//        val after = ChookUser(id = "id1", username = "username1")
//
//        val result = logService.getOperation(before, after)
//
//        Assertions.assertNotNull(result)
//        Assertions.assertEquals(LogOperation.ADDED, result)
//    }
//
//    @Test
//    fun testGetOperation_Deleted_ReturnLogOperation()
//    {
//        val before = ChookUser(id = "id1", username = "username1")
//        val after: ChookUser? = null
//
//        val result = logService.getOperation(before, after)
//
//        Assertions.assertNotNull(result)
//        Assertions.assertEquals(LogOperation.REMOVED, result)
//    }
//
//    @Test
//    fun testGetOperation_Edited_ReturnLogOperation()
//    {
//        val before = ChookUser(id = "id1", username = "username1")
//        val after = ChookUser(id = "id1", username = "username2")
//
//        val result = logService.getOperation(before, after)
//
//        Assertions.assertNotNull(result)
//        Assertions.assertEquals(LogOperation.EDITED, result)
//    }
//    // endregion
//
//    // region getLogItemType
//    @Test
//    fun testGetLogItemType_BothNull_ThrowArgumentException()
//    {
//        val before: ChookUser? = null
//        val after: ChookUser? = null
//
//        try
//        {
//            logService.getLogItemType(before, after)
//            Assertions.fail() // Fail here
//        }
//        catch(e: ArgumentException)
//        {
//            Assertions.assertTrue(true)
//        }
//    }
//
//    @Test
//    fun testGetLogItemType_BothNull_ThrowNotImplementedException()
//    {
//        val before = "test1"
//        val after = "test1"
//
//        try
//        {
//            logService.getLogItemType(before, after)
//            Assertions.fail() // Fail here
//        }
//        catch(e: NotImplementedException)
//        {
//            Assertions.assertTrue(true)
//        }
//    }
//
//    @Test
//    fun testGetLogItemType_AfterNull_ReturnLogItemType()
//    {
//        val before = ChookUser(id = "id1", username = "username1")
//        val after: ChookUser? = null
//
//        val result = logService.getLogItemType(before, after)
//
//        Assertions.assertNotNull(result)
//        Assertions.assertEquals(LogItemType.USER, result)
//    }
//
//    @Test
//    fun testGetLogItemType_BeforeNull_ReturnLogItemType()
//    {
//        val before: ChookUser? = null
//        val after = ChookUser(id = "id1", username = "username2")
//
//        val result = logService.getLogItemType(before, after)
//
//        Assertions.assertNotNull(result)
//        Assertions.assertEquals(LogItemType.USER, result)
//    }
//
//    @Test
//    fun testGetLogItemType_Normal_ReturnLogItemType()
//    {
//        val before = ChookUser(id = "id1", username = "username1")
//        val after = ChookUser(id = "id1", username = "username2")
//
//        val result = logService.getLogItemType(before, after)
//
//        Assertions.assertNotNull(result)
//        Assertions.assertEquals(LogItemType.USER, result)
//    }
//    // endregion
//
//    // region getPropertiesUpdated
//    @Test
//    fun testGetPropertiesUpdated_ChangesEmpty_ReturnNull()
//    {
//        val changes = emptyList<KProperty<*>>()
//
//        val result = logService.getPropertiesUpdated(changes)
//
//        Assertions.assertNull(result)
//    }
//
//    @Test
//    fun testGetPropertiesUpdated_NormalSingle_ReturnString()
//    {
//        val before = ChookUser(id = "id1", username = "username1")
//        val propertyName = "username"
//        val changes = before::class.declaredMemberProperties.filter { it.name == propertyName }
//
//        val result = logService.getPropertiesUpdated(changes)
//
//        Assertions.assertNotNull(result)
//        Assertions.assertEquals(result, propertyName)
//    }
//
//    @Test
//    fun testGetPropertiesUpdated_NormalMultiple_ReturnString()
//    {
//        val before = ChookUser(id = "id1", username = "username1", email = "example@example.com", recipesRegistered = 99)
//        val propertyName1 = "email"
//        val propertyName2 = "recipesRegistered"
//        val propertyName3 = "username"
//        val changes = before::class.declaredMemberProperties.filter { it.name == propertyName1 || it.name == propertyName2 || it.name == propertyName3 }
//
//        val result = logService.getPropertiesUpdated(changes)
//
//        Assertions.assertNotNull(result)
//        Assertions.assertEquals(result, "${propertyName1}, ${propertyName2}, ${propertyName3}")
//    }
//    // endregion
//
//    // region getLogProperty
//    @Test
//    fun testGetLogProperty_NullObject_ReturnNull()
//    {
//        val result = logService.getLogProperty(null, "id")
//
//        Assertions.assertNull(result)
//    }
//
//    @Test
//    fun testGetLogProperty_NullProperty_ReturnNull()
//    {
//        val before = ChookUser(id = "id1", username = null)
//
//        val spy = Mockito.spy(logService)
//        Mockito.lenient().doReturn(before.username).`when`(spy).getProperty(MockitoHelper.anyObject(), Mockito.anyString())
//
//        val result = spy.getLogProperty(before, "username")
//
//        Assertions.assertNull(result)
//    }
//
//    @Test
//    fun testGetLogProperty_SensitiveString_ReturnString()
//    {
//        val before = ChookUser(id = "id1", password = "password1")
//        val propertyName = "password"
//
//        val spy = Mockito.spy(logService)
//        Mockito.lenient().doReturn(before.password).`when`(spy).getProperty(MockitoHelper.anyObject(), Mockito.anyString())
//
//        val annotation = before::class.memberProperties.first { it.name == propertyName }.javaField!!.getDeclaredAnnotation(Sensitive::class.java)
//        Assertions.assertNotNull(annotation)
//
//        val result = spy.getLogProperty(before, propertyName)
//
//        Assertions.assertNotNull(result)
//        Assertions.assertNotEquals(before.password, result)
//        Assertions.assertTrue(result!!.contains("sensitive"))
//    }
//
//    @Test
//    fun testGetLogProperty_NormalString_ReturnString()
//    {
//        val before = ChookUser(id = "id1", username = "username1")
//
//        val spy = Mockito.spy(logService)
//        Mockito.lenient().doReturn(before.username).`when`(spy).getProperty(MockitoHelper.anyObject(), Mockito.anyString())
//
//        val result = spy.getLogProperty(before, "username")
//
//        Assertions.assertNotNull(result)
//        Assertions.assertEquals(before.username, result)
//    }
//
//    @Test
//    fun testGetLogProperty_NormalInt_ReturnIntString()
//    {
//        val before = ChookUser(id = "id1", recipesRegistered = 42)
//
//        val spy = Mockito.spy(logService)
//        Mockito.lenient().doReturn(before.recipesRegistered).`when`(spy).getProperty(MockitoHelper.anyObject(), Mockito.anyString())
//
//        val result = spy.getLogProperty(before, "recipesRegistered")
//
//        Assertions.assertNotNull(result)
//        Assertions.assertEquals(before.recipesRegistered.toString(), result)
//    }
//    // endregion
//
//    // region getProperty
//    @Test
//    fun testGetProperty_NullProperty_ReturnNull()
//    {
//        val before = ChookUser(id = "id1", username = null)
//
//        val result = logService.getProperty(before, "username")
//
//        Assertions.assertNull(result)
//    }
//
//    @Test
//    fun testGetProperty_Normal_ReturnString()
//    {
//        val before = ChookUser(id = "id1", username = "username1")
//
//        val result = logService.getProperty(before, "username")
//
//        Assertions.assertNotNull(result)
//        Assertions.assertEquals(before.username, result)
//    }
//    // endregion
}
