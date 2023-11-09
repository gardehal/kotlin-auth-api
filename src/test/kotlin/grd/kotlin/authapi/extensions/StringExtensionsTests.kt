package grd.kotlin.authapi.extensions

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.junit.jupiter.SpringExtension

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ExtendWith(SpringExtension::class, MockitoExtension::class)
class StringExtensionsTests
{
    // region String?.join
    @Test
    fun testJoin_ANull_ReturnString()
    {
        val a: String? = null
        val b = "b"
        val separator = ","

        val result = a.join(separator, b)

        assertNotNull(result)
        assertEquals(b, result)
    }

    @Test
    fun testJoin_BNull_ReturnString()
    {
        val a = "a"
        val b: String? = null
        val separator = ","

        val result = a.join(separator, b)

        assertNotNull(result)
        assertEquals(a, result)
    }

    @Test
    fun testJoin_Normal_ReturnString()
    {
        val a = "a"
        val b = "b"
        val separator = ","

        val result = a.join(separator, b)

        assertNotNull(result)
        assertEquals("$a$separator$b", result)
    }
    // endregion

    // region Iterable<String?>.join
    @Test
    fun testIterableJoin_Empty_ReturnNull()
    {
        val iterable = listOf<String?>()
        val separator = ","

        val result = iterable.join(separator)

        assertNull(result)
    }

    @Test
    fun testIterableJoin_NullEntry_ReturnString()
    {
        val a = "a"
        val b = null
        val iterable = listOf(a, b)
        val separator = ","

        val result = iterable.join(separator)

        assertNotNull(result)
        assertEquals(a, result)
    }

    @Test
    fun testIterableJoin_Normal_ReturnString()
    {
        val a = "a"
        val b = "b"
        val c = "c"
        val iterable = listOf(a, b, c)
        val separator = ","

        val result = iterable.join(separator)

        assertNotNull(result)
        assertEquals("$a$separator$b$separator$c", result)
    }
    // endregion

    // region String?.censureString(): String?
    @Test
    fun testCensureString_Null_ReturnString()
    {
        val value: String? = null

        val result = value.censureString()

        assertNull(result)
    }

    @Test
    fun testCensureString_OneCharacter_ReturnString()
    {
        val value = "s"

        val result = value.censureString()

        assertNotNull(result)
        assertEquals(value, result)
    }

    @Test
    fun testCensureString_TwoCharacters_ReturnString()
    {
        val value = "se"

        val result = value.censureString()

        assertNotNull(result)
        assertEquals(value, result)
    }

    @Test
    fun testCensureString_Normal_ReturnString()
    {
        val value = "secrets"
        val expected = "s*****s"

        val result = value.censureString()

        assertNotNull(result)
        assertEquals(expected, result)
    }
    // endregion

    // region String?.censureEmail(): String?
    @Test
    fun testCensureEmail_Null_ReturnNull()
    {
        val value: String? = null

        val result = value.censureEmail()

        assertNull(result)
    }

    @Test
    fun testCensureEmail_MalformedEmail_ReturnString()
    {
        val value = "not_an_email.com"

        val result = value.censureEmail()

        assertNotNull(result)
        assertEquals(value, result)
    }

    @Test
    fun testCensureEmail_ShortEmail_ReturnString()
    {
        val value = "abc@example.com"
        val expected = "a*c@example.com"

        val result = value.censureEmail()

        assertNotNull(result)
        assertEquals(expected, result)
    }

    @Test
    fun testCensureEmail_ShorterEmail_ReturnString()
    {
        val value = "ab@example.com"

        val result = value.censureEmail()

        assertNotNull(result)
        assertEquals(value, result)
    }

    @Test
    fun testCensureEmail_ShortestEmail_ReturnString()
    {
        val value = "a@example.com"

        val result = value.censureEmail()

        assertNotNull(result)
        assertEquals(value, result)
    }

    @Test
    fun testCensureEmail_Normal_ReturnString()
    {
        val value = "secret@example.com"
        val expected = "s****t@example.com"

        val result = value.censureEmail()

        assertNotNull(result)
        assertEquals(expected, result)
    }
    // endregion

    // region String?.withMaxLength
    @Test
    fun testWithMaxLength_Null_ReturnNull()
    {
        val length = 5
        val value: String? = null

        val result = value.withMaxLength(length)

        assertNull(result)
    }

    @Test
    fun testWithMaxLength_TooLong_ReturnMax()
    {
        val length = 500
        val value = "short string"

        val result = value.withMaxLength(length)

        assertNotNull(result)
        assertEquals(value.length, result!!.length)
    }

    @Test
    fun testWithMaxLength_Short_ReturnLength()
    {
        val length = 5
        val value = "short string"

        val result = value.withMaxLength(length)

        assertNotNull(result)
        assertEquals(length, result!!.length)
    }
    // endregion
}
