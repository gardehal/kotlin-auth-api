package grd.kotlin.authapi.services

import grd.kotlin.authapi.MockitoHelper
import grd.kotlin.authapi.enums.JsonPatchOperation
import grd.kotlin.authapi.exceptions.ArgumentException
import grd.kotlin.authapi.testdata.TestEntities
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.github.fge.jsonpatch.JsonPatch
import grd.kotlin.authapi.dto.AUserDto
import grd.kotlin.authapi.models.AUser
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mockito.*
import org.mockito.MockitoAnnotations
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.junit.jupiter.SpringExtension
import java.time.Instant
import java.time.ZoneId

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ExtendWith(SpringExtension::class, MockitoExtension::class)
class UtilityServiceTests
{
    @InjectMocks
    private lateinit var utilityService: UtilityService

    private final val testEntities = TestEntities()
    private final val user = testEntities.userNormal

    @BeforeEach
    fun setup()
    {
        MockitoAnnotations.openMocks(this)
    }

    // region numberRegex
    @Test
    fun testNumberRegex_NotNull_ReturnValue()
    {
        val regex = utilityService.numberRegex

        assertNotNull(regex)
        assertEquals(regex::javaClass.name, Regex::javaClass.name)
    }

    @Test
    fun testNumberRegex_GetInt_ReturnValue()
    {
        val regex = utilityService.numberRegex
        val haystack = "abc123xyz"
        val expected = "123"

        val result = regex.findAll(haystack)

        assertEquals(result.count(), 1)
        assertEquals(result.first().value, expected)
    }

    @Test
    fun testNumberRegex_GetDecimal_ReturnValue()
    {
        val regex = utilityService.numberRegex
        val haystackDot = "abc1.1xyz"
        val expectedDot = "1.1"
        val haystackComma = "abc2,22xyz"
        val expectedComma = "2,22"

        val resDot = regex.findAll(haystackDot)
        val resComma = regex.findAll(haystackComma)

        assertEquals(resDot.count(), 1)
        assertEquals(resDot.first().value, expectedDot)
        assertEquals(resComma.count(), 1)
        assertEquals(resComma.first().value, expectedComma)
    }

    @Test
    fun testNumberRegex_GetFraction_ReturnValue()
    {
        val regex = utilityService.numberRegex
        val haystackSlash = "abc3/6xyz"
        val expectedSlash = "3/6"
        val haystackCombined = "abc4 1/2xyz"
        val expectedCombinedFirst = "4"
        val expectedCombinedSecond = "1/2"

        val resSlash = regex.findAll(haystackSlash)
        val resCombined = regex.findAll(haystackCombined)

        assertEquals(resSlash.count(), 1)
        assertEquals(resSlash.first().value, expectedSlash)
        assertEquals(resCombined.count(), 2)
        assertEquals(resCombined.first().value, expectedCombinedFirst)
        assertEquals(resCombined.last().value, expectedCombinedSecond)
    }
    // endregion

    // region floatOf
    @Test
    fun testFloatOf_Null_ReturnValue()
    {
        val haystack = ""

        val result = utilityService.floatOf(haystack)

        assertNull(result)
    }

    @Test
    fun testFloatOf_FromStringInt_ReturnValue()
    {
        val haystack = "abc123xyz"
        val expected = 123F

        val result = utilityService.floatOf(haystack)

        assertNotNull(result)
        assertEquals(result!!, expected)
    }

    @Test
    fun testFloatOf_FromStringDecimal_ReturnValue()
    {
        val haystackDot = "abc1.1xyz"
        val expectedDot = 1.1F
        val haystackComma = "abc2,22xyz"
        val expectedComma = 2.22F

        val resDot = utilityService.floatOf(haystackDot)
        val resComma = utilityService.floatOf(haystackComma)

        assertNotNull(resDot)
        assertEquals(resDot!!, expectedDot)
        assertNotNull(resComma)
        assertEquals(resComma!!, expectedComma)
    }

    @Test
    fun testFloatOf_FromStringFraction_ReturnValue()
    {
        val haystackSlash = "abc3/6xyz"
        val expectedSlash = 0.5F
        val haystackCombined = "abc4 1/2xyz"
        val expectedCombined = 4.5F

        val resSlash = utilityService.floatOf(haystackSlash)
        val resCombined = utilityService.floatOf(haystackCombined)

        assertNotNull(resSlash)
        assertEquals(resSlash!!, expectedSlash)
        assertNotNull(resCombined)
        assertEquals(resCombined!!, expectedCombined)
    }
    // endregion

    // region checkIdValidity
    @Test
    fun testCheckIdValidity_ValidId_ReturnValue()
    {
        val id = "SomeLongIdWithNumbers123"

        val result = utilityService.checkIdValidity(id)

        assertTrue(result)
    }

    @Test
    fun testCheckIdValidity_InvalidLength_ReturnValue()
    {
        val idShort = "short"
        val idLong = "TooLongWayyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyTooLong"

        val resultShort = utilityService.checkIdValidity(idShort)
        val resultLong = utilityService.checkIdValidity(idLong)

        assertFalse(resultShort)
        assertFalse(resultLong)
    }

    @Test
    fun testCheckIdValidity_InvalidSpace_ReturnValue()
    {
        val idSpace = "LongWithSomeSpaces ShouldBeInvalid"
        val idNewline = "LongWithSomeSpaces\nShouldBeInvalid"
        val idTab = "LongWithSomeSpaces\tShouldBeInvalid"

        val resultSpace = utilityService.checkIdValidity(idSpace)
        val resultNewline = utilityService.checkIdValidity(idNewline)
        val resultTab = utilityService.checkIdValidity(idTab)

        assertFalse(resultSpace)
        assertFalse(resultNewline)
        assertFalse(resultTab)
    }

    @Test
    fun testCheckIdValidity_InvalidLetters_ReturnValue()
    {
        val idAe = "LongInvalidIdWithSomeOddLetterså"
        val idCaret = "LongInvalidIdWithSomeOddLettersâ"
        val idDiaeresis = "LongInvalidIdWithSomeOddLettersä"
        val idCedilla = "LongInvalidIdWithSomeOddLettersç"
        val idVisigoth = "LongInvalidIdWithSomeOddLettersꝣ"
        val idSigma = "LongInvalidIdWithSomeOddLettersσ"

        val resultAe = utilityService.checkIdValidity(idAe)
        val resultCaret = utilityService.checkIdValidity(idCaret)
        val resultDiaeresis = utilityService.checkIdValidity(idDiaeresis)
        val resultCedilla = utilityService.checkIdValidity(idCedilla)
        val resultVisigoth = utilityService.checkIdValidity(idVisigoth)
        val resultSigma = utilityService.checkIdValidity(idSigma)

        assertFalse(resultAe)
        assertFalse(resultCaret)
        assertFalse(resultDiaeresis)
        assertFalse(resultCedilla)
        assertFalse(resultVisigoth)
        assertFalse(resultSigma)
    }

    @Test
    fun testCheckIdValidity_InvalidChars_ReturnValue()
    {
        val idStar = "LongInvalidIdWithSomeOddChars*"
        val idDollar = "LongInvalidIdWithSomeOddChars$"
        val idAnd = "LongInvalidIdWithSomeOddChars&"
        val idPlus = "LongInvalidIdWithSomeOddChars+"
//        val idDash = "LongInvalidIdWithSomeOddChars-"
        val idUnderline = "LongInvalidIdWithSomeOddChars_"
        val idPipe = "LongInvalidIdWithSomeOddChars|"
        val idSingle = "LongInvalidIdWithSomeOddChars'"
        val idDouble = "LongInvalidIdWithSomeOddChars\""
        val idFslash = "LongInvalidIdWithSomeOddChars/"
        val idBslash = "LongInvalidIdWithSomeOddChars\\"

        val resultStar = utilityService.checkIdValidity(idStar)
        val resultDollar = utilityService.checkIdValidity(idDollar)
        val resultAnd = utilityService.checkIdValidity(idAnd)
        val resultPlus = utilityService.checkIdValidity(idPlus)
//        val resultDash = utilityService.checkIdValidity(idDash)
        val resultUnderline = utilityService.checkIdValidity(idUnderline)
        val resultPipe = utilityService.checkIdValidity(idPipe)
        val resultSingle = utilityService.checkIdValidity(idSingle)
        val resultDouble = utilityService.checkIdValidity(idDouble)
        val resultFslash = utilityService.checkIdValidity(idFslash)
        val resultBslash = utilityService.checkIdValidity(idBslash)

        assertFalse(resultStar)
        assertFalse(resultDollar)
        assertFalse(resultAnd)
        assertFalse(resultPlus)
//        assertFalse(resultDash)
        assertFalse(resultUnderline)
        assertFalse(resultPipe)
        assertFalse(resultSingle)
        assertFalse(resultDouble)
        assertFalse(resultFslash)
        assertFalse(resultBslash)
    }
    // endregion

    // region getRandomString
    @Test
    fun testGetRandomString_NotNull_ReturnValue()
    {

        val result = utilityService.getRandomString()

        assertNotNull(result)
    }
    // endregion

    // region datetimeStringToDatetime
    @Test
    fun testStringToDatetime_NotFormatted_ReturnValue()
    {
        val string = "not_number_missing_dashes"

        val result = utilityService.stringToDatetime(string)

        assertNull(result)
    }

    @Test
    fun testStringToDatetime_DateOnly_ReturnValue()
    {
        val string = "2021-03-22"

        val result = utilityService.stringToDatetime(string)

        assertNotNull(result)
        val zoned = result!!.atZone(ZoneId.of("UTC"))
        assertEquals(2021, zoned.year)
        assertEquals(3, zoned.monthValue)
        assertEquals(22, zoned.dayOfMonth)
    }

    @Test
    fun testStringToDatetime_DateAndTime_ReturnValue()
    {
        val string = "2021-03-22T04:05:06.007Z"

        val result = utilityService.stringToDatetime(string)

        assertNotNull(result)
        val zoned = result!!.atZone(ZoneId.of("UTC"))
        assertEquals(2021, zoned.year)
        assertEquals(3, zoned.monthValue)
        assertEquals(22, zoned.dayOfMonth)
        assertEquals(4, zoned.hour)
        assertEquals(5, zoned.minute)
        assertEquals(6, zoned.second)
    }
    // endregion

    // region hasField
    @Test
    fun testHasField_NoField_Return()
    {
        val field = "some-not-found-field"
        val tClass = user.copy()::class.java

        val result = utilityService.hasField(tClass, field)

        assertFalse(result)
    }

    @Test
    fun testHasField_Normal_Return()
    {
        val field = "id"
        val tClass = user.copy()::class.java

        val result = utilityService.hasField(tClass, field)

        assertTrue(result)
    }
    // endregion

    // region getReflection
    @Test
    fun testGetReflection_NotNull_ReturnValue()
    {
        val field = "username"
        val entity = user.copy()
        val userName = entity.username

        val result = utilityService.getReflection(entity, field)

        assertNotNull(result)
        assertEquals(result, userName)
    }

    @Test
    fun testGetReflection_NotValidField_ThrowException()
    {
        val entity = user

        try
        {
            utilityService.getReflection(entity, "This field does not exist")
            fail()
        }
        catch(e: NoSuchFieldException)
        {
            assertTrue(true)
        }
    }
    // endregion

    // region setReflection
    @Test
    fun testSetReflection_SetValue_ReturnVoid()
    {
        val field = "username"
        val entity = user.copy()
        val newValue = "CHANGEME"

        utilityService.setReflection(entity, field, newValue)

        assertNotNull(entity.username)
        assertEquals(newValue, entity.username)
    }

    @Test
    fun testSetReflection_SetNull_ReturnVoid()
    {
        val field = "username"
        val entity = user.copy()
        val newValue = null

        utilityService.setReflection(entity, field, newValue)

        assertNull(entity.username)
    }

    @Test
    fun testSetReflection_MismatchValue_ThrowException()
    {
        val field = "username"
        val entity = user.copy()
        val newValue = 123

        try
        {
            utilityService.setReflection(entity, field, newValue) // Name field is String
            fail()
        }
        catch(e: ArgumentException)
        {
            assertTrue(true)
        }
    }

    @Test
    fun testSetReflection_ChangeFromNull_ReturnVoid()
    {
        val field = "deleted"
        val entity = AUser(username = "Tom", password = "password", deleted = null)
        val newValue = "datetime"

        utilityService.setReflection(entity, field, newValue)

        assertNotNull(entity.deleted)
        assertEquals(newValue, entity.deleted)
    }

    @Test
    fun testSetReflection_ChangeToNull_ReturnVoid()
    {
        val field = "deleted"
        val entity = AUser(username = "Tom", password = "password", deleted = "datetime")
        val newValue: String? = null

        utilityService.setReflection(entity, field, newValue)

        assertNull(entity.deleted)
        assertEquals(newValue, entity.deleted)
    }

    @Test
    fun testSetReflection_ChangeNonExistentValue_ThrowNoSuchFieldException()
    {
        val field = "some_field"
        val entity = AUser(username = "Tom", password = "password")
        val newValue: String? = null

        try
        {
            utilityService.setReflection(entity, field, newValue)
            fail()
        }
        catch(e: NoSuchFieldException)
        {
            assertTrue(true)
        }
    }

    @Test
    fun testSetReflection_ChangeSame_ReturnVoid()
    {
        val field = "deleted"
        val entity = AUser(username = "Tom", password = "password", deleted = "datetime")
        val newValue = "datetime"

        utilityService.setReflection(entity, field, newValue)

        assertNotNull(entity.deleted)
        assertEquals(newValue, entity.deleted)
    }
    // endregion

    // region convertJsonToJsonPatch
    @Test
    fun testConvertJsonToJsonPatch_InvalidJson_ThrowArgumentException()
    {
        val json = ""
        val ignoreFields = emptyList<String>()

        try
        {
            utilityService.convertJsonToJsonPatch(json, ignoreFields)
            fail() // Fail here
        }
        catch(e: ArgumentException)
        {
            assertTrue(true)
        }
    }

    @Test
    fun testConvertJsonToJsonPatch_InvalidJsonPatch_ThrowArgumentException()
    {
        val field = "username"
        val newValue = "Peter"
        val jsonPatch = "[[[["
        val json = """{ "$field": "$newValue" }"""
        val ignoreFields = emptyList<String>()

        val spy = spy(utilityService)
        lenient().doReturn(jsonPatch).`when`(spy).getJsonPatch(MockitoHelper.anyObject(), anyString(), anyString())

        try
        {
            spy.convertJsonToJsonPatch(json, ignoreFields)
            fail() // Fail here
        }
        catch(e: ArgumentException)
        {
            assertTrue(true)
        }
    }

    @Test
    fun testConvertJsonToJsonPatch_NormalIgnoreField_Return()
    {
        val field = "id"
        val newValue = "some-id"
        val json = """{ "$field": "$newValue" }"""
        val ignoreFields = listOf(field)

        val spy = spy(utilityService)
        lenient().doThrow(NullPointerException("test-fail-on-throw")).`when`(spy).getJsonPatch(MockitoHelper.anyObject(), anyString(), anyString())

        val result = spy.convertJsonToJsonPatch(json, ignoreFields)

        assertFalse(result.toString().contains(field))
        assertFalse(result.toString().contains(newValue))
        assertEquals("[]", result.toString())
    }

    @Test
    fun testConvertJsonToJsonPatch_Normal_Return()
    {
        val field = "username"
        val newValue = "Tom"
        val jsonPatch = """{ "op": "replace", "path": "/$field", "value": "$newValue" }"""
        val json = """{ "$field": "$newValue" }"""
        val ignoreFields = emptyList<String>()

        val spy = spy(utilityService)
        lenient().doReturn(jsonPatch).`when`(spy).getJsonPatch(MockitoHelper.anyObject(), anyString(), anyString())

        val result = spy.convertJsonToJsonPatch(json, ignoreFields)

        assertTrue(result.toString().contains(field))
        assertTrue(result.toString().contains(newValue))
    }
    // endregion

    // region getJsonPatch
    @Test
    fun testGetJsonPatch_Normal_Return()
    {
        val operation = JsonPatchOperation.REPLACE
        val field = "username"
        val value = "test-username"

        val result = utilityService.getJsonPatch(operation, field, value)

        assertTrue(result.contains(operation.toString().lowercase()))
        assertTrue(result.contains("/$field"))
        assertTrue(result.contains(value))
    }
    // endregion

    // region mergePatch
    @Test
    fun testMergePatch_Normal_Return()
    {
        val copy = AUserDto(id = "some-id", username = "Jon")
        val tClass = AUserDto::class.java
        val newValue = "Jonathan"
        val json = """[{ "op": "replace", "path": "/username", "value": "$newValue" }]"""
        val jsonNode = ObjectMapper().readValue(json, JsonNode::class.java)
        val jsonPatch = JsonPatch.fromJson(jsonNode)

        val result = utilityService.mergePatch(copy, tClass, jsonPatch)

        assertEquals(copy::class.java, result::class.java)
        assertEquals(newValue, result.username)
        assertEquals(copy.id, result.id)
    }
    // endregion

    // region uppercaseFirst
    @Test
    fun testUppercaseFirst_UppercaseText_ReturnValue()
    {
        val lowercase = "lowercase text"
        val expected = "Lowercase text"

        val result = utilityService.uppercaseFirst(lowercase)

        assertEquals(expected, result)
    }

    @Test
    fun testUppercaseFirst_UppercaseNumber_ReturnValue()
    {
        val lowercase = "5hould not uppercase number 5"
        val expected = lowercase

        val result = utilityService.uppercaseFirst(lowercase)

        assertEquals(expected, result)
    }
    // endregion

    // region validateInput
    @Test
    fun testValidateInput_Null_ReturnFalse()
    {
        val input: String? = null
        val expectedFirst = false
        val expectedSecondContains = "null"

        val result = utilityService.validateInput(valueToCheck = input)

        assertNotNull(result)
        assertEquals(expectedFirst, result.first)
        assertTrue(result.second.contains(expectedSecondContains))
    }

    @Test
    fun testValidateInput_Empty_ReturnFalse()
    {
        val input = ""
        val expectedFirst = false
        val expectedSecondContains = "empty"

        val result = utilityService.validateInput(valueToCheck = input)

        assertNotNull(result)
        assertEquals(expectedFirst, result.first)
        assertTrue(result.second.contains(expectedSecondContains))
    }

    @Test
    fun testValidateInput_MinimumLengthFail_ReturnFalse()
    {
        val input = "1"
        val expectedFirst = false
        val expectedSecondContains = "too short"

        val result = utilityService.validateInput(valueToCheck = input, allowNumbers = true, minimumLength = 2)

        assertNotNull(result)
        assertEquals(expectedFirst, result.first)
        assertTrue(result.second.contains(expectedSecondContains))
    }

    @Test
    fun testValidateInput_MinimumLengthSuccess_ReturnTrue()
    {
        val input = "12"
        val expectedFirst = true
        val expectedSecondContains = "valid"

        val result = utilityService.validateInput(valueToCheck = input, allowNumbers = true, minimumLength = 2)

        assertNotNull(result)
        assertEquals(expectedFirst, result.first)
        assertTrue(result.second.contains(expectedSecondContains))
    }

    @Test
    fun testValidateInput_MaximumLengthFail_ReturnFalse()
    {
        val input = "1234567890"
        val expectedFirst = false
        val expectedSecondContains = "too long"

        val result = utilityService.validateInput(valueToCheck = input, allowNumbers = true, maximumLength = 8)

        assertNotNull(result)
        assertEquals(expectedFirst, result.first)
        assertTrue(result.second.contains(expectedSecondContains))
    }

    @Test
    fun testValidateInput_MaximumLengthSuccess_ReturnTrue()
    {
        val input = "12345678"
        val expectedFirst = true
        val expectedSecondContains = "valid"

        val result = utilityService.validateInput(valueToCheck = input, allowNumbers = true, maximumLength = 8)

        assertNotNull(result)
        assertEquals(expectedFirst, result.first)
        assertTrue(result.second.contains(expectedSecondContains))
    }

    @Test
    fun testValidateInput_AllowNorwegianLettersFail_ReturnFalse()
    {
        val input = "123"
        val expectedFirst = false
        val expectedSecondContains = "illegal"

        val result = utilityService.validateInput(valueToCheck = input, allowNorwegianLetters = true)

        assertNotNull(result)
        assertEquals(expectedFirst, result.first)
        assertTrue(result.second.contains(expectedSecondContains))
    }

    @Test
    fun testValidateInput_AllowNorwegianLettersSuccess_ReturnTrue()
    {
        val input = "abcxyzæøåABZXYZÆØÅ"
        val expectedFirst = true
        val expectedSecondContains = "valid"

        val result = utilityService.validateInput(valueToCheck = input, allowNorwegianLetters = true)

        assertNotNull(result)
        assertEquals(expectedFirst, result.first)
        assertTrue(result.second.contains(expectedSecondContains))
    }

    @Test
    fun testValidateInput_AllowNumbersFail_ReturnTrue()
    {
        val input = "nonnumbers"
        val expectedFirst = false
        val expectedSecondContains = "illegal"

        val result = utilityService.validateInput(valueToCheck = input, allowNumbers = true)

        assertNotNull(result)
        assertEquals(expectedFirst, result.first)
        assertTrue(result.second.contains(expectedSecondContains))
    }

    @Test
    fun testValidateInput_AllowNumbersSuccess_ReturnTrue()
    {
        val input = "123"
        val expectedFirst = true
        val expectedSecondContains = "valid"

        val result = utilityService.validateInput(valueToCheck = input, allowNumbers = true)

        assertNotNull(result)
        assertEquals(expectedFirst, result.first)
        assertTrue(result.second.contains(expectedSecondContains))
    }

    @Test
    fun testValidateInput_AllowSingleSpaceFail_ReturnTrue()
    {
        val input = "nospaces"
        val expectedFirst = false
        val expectedSecondContains = "illegal"

        val result = utilityService.validateInput(valueToCheck = input, allowSingleSpace = true)

        assertNotNull(result)
        assertEquals(expectedFirst, result.first)
        assertTrue(result.second.contains(expectedSecondContains))
    }

    @Test
    fun testValidateInput_AllowSingleSpaceSuccess_ReturnTrue()
    {
        val input = " "
        val expectedFirst = true
        val expectedSecondContains = "valid"

        val result = utilityService.validateInput(valueToCheck = input, allowSingleSpace = true)

        assertNotNull(result)
        assertEquals(expectedFirst, result.first)
        assertTrue(result.second.contains(expectedSecondContains))
    }

    @Test
    fun testValidateInput_AllowEmailSymbolsFail_ReturnTrue()
    {
        val input = "noDotUnderscoreAt"
        val expectedFirst = false
        val expectedSecondContains = "illegal"

        val result = utilityService.validateInput(valueToCheck = input, allowEmailSymbols = true)

        assertNotNull(result)
        assertEquals(expectedFirst, result.first)
        assertTrue(result.second.contains(expectedSecondContains))
    }

    @Test
    fun testValidateInput_AllowEmailSymbolsSuccess_ReturnTrue()
    {
        val input = "._@"
        val expectedFirst = true
        val expectedSecondContains = "valid"

        val result = utilityService.validateInput(valueToCheck = input, allowEmailSymbols = true)

        assertNotNull(result)
        assertEquals(expectedFirst, result.first)
        assertTrue(result.second.contains(expectedSecondContains))
    }

    @Test
    fun testValidateInput_LettersNumbersEmailFail_ReturnTrue()
    {
        val input = "this'*invalid@email.com"
        val expectedFirst = false
        val expectedSecondContains = "illegal"

        val result = utilityService.validateInput(valueToCheck = input, allowNorwegianLetters = true, allowNumbers = true, allowEmailSymbols = true)

        assertNotNull(result)
        assertEquals(expectedFirst, result.first)
        assertTrue(result.second.contains(expectedSecondContains))
    }

    @Test
    fun testValidateInput_LettersNumbersEmailSuccess_ReturnTrue()
    {
        val input = "this_valid0@email.com"
        val expectedFirst = true
        val expectedSecondContains = "valid"

        val result = utilityService.validateInput(valueToCheck = input, allowNorwegianLetters = true, allowNumbers = true, allowEmailSymbols = true)

        assertNotNull(result)
        assertEquals(expectedFirst, result.first)
        assertTrue(result.second.contains(expectedSecondContains))
    }
    // endregion

    // region validateEmail
    @Test
    fun testValidateEmail_Null_ReturnFalse()
    {
        val input: String? = null
        val expectedFirst = false
        val expectedSecondContains = "null"

        val result = utilityService.validateEmail(input)

        assertNotNull(result)
        assertEquals(expectedFirst, result.first)
        assertTrue(result.second.contains(expectedSecondContains))
    }

    @Test
    fun testValidateEmail_Empty_ReturnFalse()
    {
        val input = ""
        val expectedFirst = false
        val expectedSecondContains = "empty"

        val result = utilityService.validateEmail(input)

        assertNotNull(result)
        assertEquals(expectedFirst, result.first)
        assertTrue(result.second.contains(expectedSecondContains))
    }

    @Test
    fun testValidateEmail_MissingAt_ReturnFalse()
    {
        val input = "some_email_at_somewhere.com"
        val expectedFirst = false
        val expectedSecondContains = "invalidly formatted"

        val result = utilityService.validateEmail(input)

        assertNotNull(result)
        assertEquals(expectedFirst, result.first)
        assertTrue(result.second.contains(expectedSecondContains))
    }

    @Test
    fun testValidateEmail_MissingDomain_ReturnFalse()
    {
        val input = "some_email_@"
        val expectedFirst = false
        val expectedSecondContains = "invalidly formatted"

        val result = utilityService.validateEmail(input)

        assertNotNull(result)
        assertEquals(expectedFirst, result.first)
        assertTrue(result.second.contains(expectedSecondContains))
    }

    @Test
    fun testValidateEmail_DomainDotDomain_ReturnFalse()
    {
        val input = "some_email_@englishurl.co.uk"
        val expectedFirst = true
        val expectedSecondContains = "valid"

        val result = utilityService.validateEmail(input)

        assertNotNull(result)
        assertEquals(expectedFirst, result.first)
        assertTrue(result.second.contains(expectedSecondContains))
    }

    @Test
    fun testValidateEmail_Normal_ReturnTrue()
    {
        val input = "example@example.com"
        val expectedFirst = true
        val expectedSecondContains = "valid"

        val result = utilityService.validateEmail(input)

        assertNotNull(result)
        assertEquals(expectedFirst, result.first)
        assertTrue(result.second.contains(expectedSecondContains))
    }
    // endregion

    // region accumulate
    @Test
    fun testAccumulate_ThrowNull_ThrowArgumentException()
    {
        val current: Float? = null
        val new = 1F

        try
        {
            utilityService.accumulate(current, new, false)
            fail()
        }
        catch(e: ArgumentException)
        {
            assertTrue(true)
        }
    }

    @Test
    fun testAccumulate_NullCurrentNullNew_Return0()
    {
        val current: Float? = null
        val new: Float? = null
        val expected = 0F

        val result = utilityService.accumulate(current, new, true)

        assertNotNull(result)
        assertEquals(expected, result)
    }

    @Test
    fun testAccumulate_NullCurrent_Return1()
    {
        val current: Float? = null
        val new = 1F
        val expected = new

        val result = utilityService.accumulate(current, new, true)

        assertNotNull(result)
        assertEquals(expected, result)
    }

    @Test
    fun testAccumulate_NullNew_Return2()
    {
        val current = 2F
        val new: Float? = null
        val expected = current

        val result = utilityService.accumulate(current, new, true)

        assertNotNull(result)
        assertEquals(expected, result)
    }

    @Test
    fun testAccumulate_Accumulate_Return7()
    {
        val current = 3F
        val new = 4F
        val expected = current + new

        val result = utilityService.accumulate(current, new, true)

        assertNotNull(result)
        assertEquals(expected, result)
    }
    // endregion
}
