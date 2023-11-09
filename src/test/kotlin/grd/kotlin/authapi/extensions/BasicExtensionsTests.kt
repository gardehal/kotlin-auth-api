package grd.kotlin.authapi.extensions

import grd.kotlin.authapi.annotations.NotLogged
import grd.kotlin.authapi.logging.LogHead
import grd.kotlin.authapi.models.AUser
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.junit.jupiter.SpringExtension
import kotlin.reflect.full.memberProperties

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ExtendWith(SpringExtension::class, MockitoExtension::class)
class BasicExtensionsTests
{
    // region Any.isNull
    @Test
    fun testIsNull_NotNull_ReturnFalse()
    {
        val user = AUser(id = "id1", username = "username1", password = "pass")

        val result = user.isNull()

        assertNotNull(result)
        assertFalse(result)
    }

    @Test
    fun testIsNull_NullObject_ReturnTrue()
    {
        val user: AUser? = null

        val result = user.isNull()

        assertNotNull(result)
        assertTrue(result)
    }

    @Test
    fun testIsNull_NullNothing_ReturnTrue()
    {
        val nothing = null

        val result = nothing.isNull()

        assertNotNull(result)
        assertTrue(result)
    }
    // endregion

    // region Any.isNotNull
    @Test
    fun testIsNotNull_NotNull_ReturnTrue()
    {
        val user = AUser(id = "id1", password = "pass", username = "username1")

        val result = user.isNotNull()

        assertNotNull(result)
        assertTrue(result)
    }

    @Test
    fun testIsNotNull_NullObject_ReturnFalse()
    {
        val user: AUser? = null

        val result = user.isNotNull()

        assertNotNull(result)
        assertFalse(result)
    }

    @Test
    fun testIsNotNull_NullNothing_ReturnFalse()
    {
        val nothing = null

        val result = nothing.isNotNull()

        assertNotNull(result)
        assertFalse(result)
    }
    // endregion

    // region Any.hasAnnotation
    @Test
    fun testHasAnnotation_PropertyLevelFalse_ReturnFalse()
    {
        val user = AUser(id = "id1", username = "username1", password = "pass", moderatorComments = mutableMapOf(Pair("datetime", "comment")))

        val result = user.username.hasAnnotation(NotLogged::class.java)

        assertNotNull(result)
        assertFalse(result)
    }

    @Test
    fun testHasAnnotation_PropertyLevelTrue_ReturnFalse()
    {
        val user = AUser(id = "id1", username = "username1", password = "pass", moderatorComments = mutableMapOf(Pair("datetime", "comment")))

        val result = user.moderatorComments.hasAnnotation(NotLogged::class.java)

        assertNotNull(result)
        assertFalse(result) // Note, this method is for class-level only
    }

    @Test
    fun testHasAnnotation_ClassLevelFalse_ReturnFalse()
    {
        val user = AUser(id = "id1", username = "username1", password = "pass", moderatorComments = mutableMapOf(Pair("datetime", "comment")))

        val result = user.hasAnnotation(NotLogged::class.java)

        assertNotNull(result)
        assertFalse(result)
    }

    @Test
    fun testHasAnnotation_ClassLevel_ReturnTrue()
    {
        val user = LogHead(id = "id1")

        val result = user.hasAnnotation(NotLogged::class.java)

        assertNotNull(result)
        assertTrue(result)
    }
    // endregion

    // region KProperty.hasAnnotation
    @Test
    fun testHasAnnotation_Property_ReturnFalse()
    {
        val user = AUser(id = "id1", username = "username1", password = "pass")
        val property = user::class.memberProperties.first { e -> e.name == "username" }

        val result = property.hasAnnotation(NotLogged::class.java)

        assertNotNull(result)
        assertFalse(result)
    }

    @Test
    fun testHasAnnotation_Property_ReturnTrue()
    {
        val user = AUser(id = "id1", username = "username1", password = "pass", moderatorComments = mutableMapOf(Pair("datetime", "comment")))
        val property = user::class.memberProperties.first { e -> e.name == "username" }

        val result = property.hasAnnotation(NotLogged::class.java)

        assertNotNull(result)
        assertFalse(result)
    }
    // endregion
}
