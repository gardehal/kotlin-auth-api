package grd.kotlin.authapi.controllers

import grd.kotlin.authapi.MockitoHelper
import grd.kotlin.authapi.dto.AUserDto
import grd.kotlin.authapi.dto.Converter
import grd.kotlin.authapi.exceptions.NotFoundException
import grd.kotlin.authapi.models.AUser
import grd.kotlin.authapi.models.HttpReturn
import grd.kotlin.authapi.enums.UserRole
import grd.kotlin.authapi.services.ControllerUtilityService
import grd.kotlin.authapi.services.UserService
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
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.test.context.junit.jupiter.SpringExtension

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ExtendWith(SpringExtension::class, MockitoExtension::class)
class AUserControllerUnitTests
{
    @InjectMocks
    private lateinit var userController: UserController

    @Mock
    private lateinit var controllerUtilityService: ControllerUtilityService

    @Mock
    private lateinit var userService: UserService

    val authorizationFieldName = "authorization"

    @BeforeEach
    fun setup()
    {
        controllerUtilityService = mock(ControllerUtilityService::class.java)
        userService = mock(UserService::class.java)
        MockitoAnnotations.openMocks(this)

        SecurityContextHolder.getContext().authentication = null
    }

    val user = AUser(id = "AUserControllerUnitTestsUser01",
        about = null,
        email = "example@example.com",
        expirationTime = null,
        usersRecruited = 0,
        lastActiveTime = null,
        lockedUntilTime = null,
        moderatorComments = mutableMapOf(Pair("2021-03-26T22:00:00.000Z", "Created for testing")),
        password = "\$2a\$10\$OlxiBA58Aaj0uwRzR6ZMV.AZ6/bRQT91GsAc24G5NKZF6LkBYqzV.",
        previousUsernames = mutableMapOf(),
        profilePicture = null,
        role = UserRole.OTHER,
        username = "User01",
        usernameChangeTime = "2021-03-26T22:00:00.000Z")
    val clearTextPassword = "password"

    // POST /users/register
    // region registerNew
    @Test
    fun testRegisterNew_Normal_Return201() = runBlocking {
        val headers = mapOf<String, String>()
        val entity = user.copy()
        val dto = Converter.Companion.convert(entity, AUserDto::class.java)
        val editor = user.copy(id = "editor")
        val expectedCode = 201

        lenient().`when`(userService.getUserFromHeaders(MockitoHelper.anyObject())).thenReturn(editor)
        lenient().`when`(userService.actionAllowed(MockitoHelper.anyObject(), MockitoHelper.anyObject())).thenReturn(true)
        lenient().`when`(userService.registerNew(MockitoHelper.anyObject(), anyString(), anyString())).thenReturn(entity)

        val result = userController.registerNew(headers, dto, clearTextPassword)

        assertNotNull(result)
        assertNotNull(result.body)
        assertEquals(expectedCode, result.body!!.code)
        assertNotNull(result.body!!.data)
        assertNull(result.body!!.message)
    }

    @Test
    fun testRegisterNew_ServerError_Return500() = runBlocking {
        val headers = mapOf(Pair(authorizationFieldName, "valid-token"))
        val entity = user.copy()
        val dto = Converter.Companion.convert(entity, AUserDto::class.java)
        val expectedCode = 500

        lenient().`when`(userService.getUserFromHeaders(MockitoHelper.anyObject())).thenThrow(NullPointerException("mocked"))
        lenient().`when`(controllerUtilityService.getErrorHttpReturn(MockitoHelper.anyObject(), MockitoHelper.anyObject(), MockitoHelper.anyObject(), anyInt()))
            .thenReturn(HttpReturn(expectedCode, "mocked"))

        val result = userController.registerNew(headers, dto, clearTextPassword)

        assertNotNull(result)
        assertNotNull(result.body)
        assertEquals(expectedCode, result.body!!.code)
        assertNull(result.body!!.data)
        assertNotNull(result.body!!.message)
    }
    // endregion

    // GET /users/{id}
    // region getById
    @Test
    fun testGetById_NoSuchEditor_Return400() = runBlocking {
        val headers = mapOf(Pair(authorizationFieldName, "not-a-valid-token"))
        val entity = user.copy()
        val expectedCode = 400

        lenient().`when`(userService.getUserFromHeaders(MockitoHelper.anyObject())).thenThrow(NotFoundException("mocked"))

        val result = userController.getById(headers, entity.id)

        assertNotNull(result)
        assertNotNull(result.body)
        assertEquals(expectedCode, result.body!!.code)
        assertNull(result.body!!.data)
        assertNotNull(result.body!!.message)
    }

    @Test
    fun testGetById_Normal_Return200() = runBlocking {
        val headers = mapOf(Pair(authorizationFieldName, "valid-token"))
        val entity = user.copy()
        val editor = user.copy(id = "editor")
        val expectedCode = 200

        lenient().`when`(userService.getUserFromHeaders(MockitoHelper.anyObject())).thenReturn(editor)
        lenient().`when`(userService.get(anyString(), anyBoolean())).thenReturn(entity)
        lenient().`when`(userService.actionAllowedUser(MockitoHelper.anyObject(), MockitoHelper.anyObject(), MockitoHelper.anyObject(), anyBoolean()))
            .thenReturn(true)

        val result = userController.getById(headers, entity.id)

        assertNotNull(result)
        assertNotNull(result.body)
        assertEquals(expectedCode, result.body!!.code)
        assertNotNull(result.body!!.data)
        assertNull(result.body!!.message)
    }

    @Test
    fun testGetById_ServerError_Return500() = runBlocking {
        val headers = mapOf(Pair(authorizationFieldName, "valid-token"))
        val entity = user.copy()
        val expectedCode = 500

        lenient().`when`(userService.getUserFromHeaders(MockitoHelper.anyObject())).thenThrow(NullPointerException("mocked"))
        lenient().`when`(controllerUtilityService.getErrorHttpReturn(MockitoHelper.anyObject(), MockitoHelper.anyObject(), MockitoHelper.anyObject(), anyInt()))
            .thenReturn(HttpReturn(expectedCode, "mocked"))

        val result = userController.getById(headers, entity.id)

        assertNotNull(result)
        assertNotNull(result.body)
        assertEquals(expectedCode, result.body!!.code)
        assertNull(result.body!!.data)
        assertNotNull(result.body!!.message)
    }
    // endregion

    // GET /users/detailed/{id}
    // region getDetailedById
    @Test
    fun testGetDetailedById_NoSuchEditor_Return400() = runBlocking {
        val headers = mapOf(Pair(authorizationFieldName, "valid-token"))
        val entity = user.copy()
        val expectedCode = 400

        lenient().`when`(userService.getUserFromHeaders(MockitoHelper.anyObject())).thenThrow(NotFoundException("mocked"))

        val result = userController.getDetailedById(headers, entity.id)

        assertNotNull(result)
        assertNotNull(result.body)
        assertEquals(expectedCode, result.body!!.code)
        assertNull(result.body!!.data)
        assertNotNull(result.body!!.message)
    }
    @Test
    fun testGetByDetailedId_Normal_Return200() = runBlocking {
        val headers = mapOf(Pair(authorizationFieldName, "valid-token"))
        val editor = user.copy(id = "editor", role = UserRole.MODERATOR)
        val entity = user.copy()
        val expectedCode = 200

        lenient().`when`(userService.getUserFromHeaders(MockitoHelper.anyObject())).thenReturn(editor)
        lenient().`when`(userService.get(anyString(), anyBoolean())).thenReturn(entity)
        lenient().`when`(userService.actionAllowedUser(MockitoHelper.anyObject(), MockitoHelper.anyObject(), MockitoHelper.anyObject(), anyBoolean())).thenReturn(true)

        val result = userController.getDetailedById(headers, entity.id)

        assertNotNull(result)
        assertNotNull(result.body)
        assertEquals(expectedCode, result.body!!.code)
        assertNotNull(result.body!!.data)
        assertNull(result.body!!.message)
    }

    @Test
    fun testGetByDetailedId_ServerError_Return500() = runBlocking {
        val headers = mapOf(Pair(authorizationFieldName, "valid-token"))
        val entity = user.copy()
        val expectedCode = 500

        lenient().`when`(userService.get(anyString(), anyBoolean())).thenThrow(NullPointerException("mocked"))
        lenient().`when`(controllerUtilityService.getErrorHttpReturn(MockitoHelper.anyObject(), MockitoHelper.anyObject(), MockitoHelper.anyObject(), anyInt()))
            .thenReturn(HttpReturn(expectedCode, "mocked"))

        val result = userController.getDetailedById(headers, entity.id)

        assertNotNull(result)
        assertNotNull(result.body)
        assertEquals(expectedCode, result.body!!.code)
        assertNull(result.body!!.data)
        assertNotNull(result.body!!.message)
    }
    // endregion

    // GET /users/username/{username}
    // region getByUsername
    @Test
    fun testGetByUsername_NoSuchEditor_Return400() = runBlocking {
        val headers = mapOf(Pair(authorizationFieldName, "not-a-valid-token"))
        val entity = user.copy()
        val expectedCode = 400

        lenient().`when`(userService.getUserFromHeaders(MockitoHelper.anyObject())).thenThrow(NotFoundException("mocked"))

        val result = userController.getByUsername(headers, entity.username)

        assertNotNull(result)
        assertNotNull(result.body)
        assertEquals(expectedCode, result.body!!.code)
        assertNull(result.body!!.data)
        assertNotNull(result.body!!.message)
    }
    @Test
    fun testGetByUsername_Normal_Return200() = runBlocking {
        val headers = mapOf(Pair(authorizationFieldName, "valid-token"))
        val entity = user.copy()
        val editor = user.copy(id = "editor")
        val expectedCode = 200

        lenient().`when`(userService.getUserFromHeaders(MockitoHelper.anyObject())).thenReturn(editor)
        lenient().`when`(userService.findByUsernameEmail(anyString(), MockitoHelper.anyObject())).thenReturn(entity)
        lenient().`when`(userService.actionAllowedUser(MockitoHelper.anyObject(), MockitoHelper.anyObject(), MockitoHelper.anyObject(), anyBoolean()))
            .thenReturn(true)

        val result = userController.getByUsername(headers, entity.username)

        assertNotNull(result)
        assertNotNull(result.body)
        assertEquals(expectedCode, result.body!!.code)
        assertNotNull(result.body!!.data)
        assertNull(result.body!!.message)
    }

    @Test
    fun testGetByUsername_ServerError_Return500() = runBlocking {
        val headers = mapOf(Pair(authorizationFieldName, "valid-token"))
        val entity = user.copy()
        val expectedCode = 500

        lenient().`when`(userService.getUserFromHeaders(MockitoHelper.anyObject())).thenThrow(NullPointerException("mocked"))
        lenient().`when`(controllerUtilityService.getErrorHttpReturn(MockitoHelper.anyObject(), MockitoHelper.anyObject(), MockitoHelper.anyObject(), anyInt()))
            .thenReturn(HttpReturn(expectedCode, "mocked"))

        val result = userController.getByUsername(headers, entity.username)

        assertNotNull(result)
        assertNotNull(result.body)
        assertEquals(expectedCode, result.body!!.code)
        assertNull(result.body!!.data)
        assertNotNull(result.body!!.message)
    }
    // endregion

    // GET /users
    // region getAll
    @Test
    fun testGetAll_Normal_Return200() = runBlocking {
        val headers = mapOf(Pair(authorizationFieldName, "valid-token"))
        val editor = user.copy(id = "editor")
        val entity1 = user.copy(id = "entity1")
        val entity2 = user.copy(id = "entity2")
        val entity3 = user.copy(id = "entity3")
        val all = listOf(entity1, entity2, entity3)
        val pageable = PageRequest.of(0, 20)
        val allPage = PageImpl(all, pageable, all.count().toLong())
        val expectedCode = 200

        lenient().`when`(userService.getUserFromHeaders(MockitoHelper.anyObject())).thenReturn(editor)
        lenient().`when`(userService.actionAllowed(MockitoHelper.anyObject(), MockitoHelper.anyObject())).thenReturn(true)
        lenient().`when`(userService.getAll(
            anyBoolean(),
            MockitoHelper.anyObject())).thenReturn(allPage)

        val result = userController.getAll(headers,
            null, null)

        assertNotNull(result)
        assertNotNull(result.body)
        assertEquals(expectedCode, result.body!!.code)
        assertNotNull(result.body!!.data)
        assertNull(result.body!!.message)
    }

    @Test
    fun testGetAll_NormalSetPage_Return200() = runBlocking {
        val headers = mapOf(Pair(authorizationFieldName, "valid-token"))
        val pageNumber = 1
        val pageSize = 1
        val editor = user.copy(id = "editor")
        val entity1 = user.copy(id = "entity1")
        val entity2 = user.copy(id = "entity2")
        val entity3 = user.copy(id = "entity3")
        val all = listOf(entity1, entity2, entity3)
        val pageable = PageRequest.of(pageNumber, pageSize)
        val allPage = PageImpl(all, pageable, all.count().toLong())
        val expectedCode = 200

        lenient().`when`(userService.getUserFromHeaders(MockitoHelper.anyObject())).thenReturn(editor)
        lenient().`when`(userService.actionAllowed(MockitoHelper.anyObject(), MockitoHelper.anyObject())).thenReturn(true)
        lenient().`when`(userService.getAll(
            anyBoolean(),
            MockitoHelper.anyObject())).thenReturn(allPage)

        val result = userController.getAll(headers,
            null, null)

        assertNotNull(result)
        assertNotNull(result.body)
        assertEquals(expectedCode, result.body!!.code)
        assertNotNull(result.body!!.data)
        assertNull(result.body!!.message)
    }

    @Test
    fun testGetAll_ServerError_Return500() = runBlocking {
        val headers = mapOf(Pair(authorizationFieldName, "valid-token"))
        val expectedCode = 500

        lenient().`when`(userService.getUserFromHeaders(MockitoHelper.anyObject())).thenThrow(NullPointerException("mocked"))
        lenient().`when`(controllerUtilityService.getErrorHttpReturn(MockitoHelper.anyObject(), MockitoHelper.anyObject(), MockitoHelper.anyObject(), anyInt()))
            .thenReturn(HttpReturn(expectedCode, "mocked"))

        val result = userController.getAll(headers,
            null, null)

        assertNotNull(result)
        assertNotNull(result.body)
        assertEquals(expectedCode, result.body!!.code)
        assertNull(result.body!!.data)
        assertNotNull(result.body!!.message)
    }
    // endregion

    // DELETE /users/{id}
    // region softDeleteById
    @Test
    fun testSoftDeleteById_Normal_Return200() = runBlocking {
        val headers = mapOf(Pair(authorizationFieldName, "valid-token"))
        val entity = user.copy()
        val editor = user.copy(id = "editor")
        val expectedCode = 200

        lenient().`when`(userService.getUserFromHeaders(MockitoHelper.anyObject())).thenReturn(editor)
        lenient().`when`(userService.get(anyString(), anyBoolean())).thenReturn(entity)
        lenient().`when`(userService.actionAllowedUser(MockitoHelper.anyObject(), MockitoHelper.anyObject(), MockitoHelper.anyObject(), anyBoolean()))
            .thenReturn(true)
        lenient().`when`(userService.delete(anyString(), anyString(), anyBoolean())).thenReturn(entity)

        val result = userController.softDeleteById(headers, entity.id)

        assertNotNull(result)
        assertNotNull(result.body)
        assertEquals(expectedCode, result.body!!.code)
        assertNotNull(result.body!!.data)
        assertNull(result.body!!.message)
    }

    @Test
    fun testSoftDeleteById_ServerError_Return500() = runBlocking {
        val headers = mapOf(Pair(authorizationFieldName, "valid-token"))
        val entity = user.copy()
        val expectedCode = 500

        lenient().`when`(userService.getUserFromHeaders(MockitoHelper.anyObject())).thenThrow(NullPointerException("mocked"))
        lenient().`when`(controllerUtilityService.getErrorHttpReturn(MockitoHelper.anyObject(), MockitoHelper.anyObject(), MockitoHelper.anyObject(), anyInt()))
            .thenReturn(HttpReturn(expectedCode, "mocked"))

        val result = userController.softDeleteById(headers, entity.id)

        assertNotNull(result)
        assertNotNull(result.body)
        assertEquals(expectedCode, result.body!!.code)
        assertEquals(false, result.body!!.data)
        assertNotNull(result.body!!.message)
    }
    // endregion

    // GET /users/restore/{id}
    // region restoreById
    @Test
    fun testRestoreById_Normal_Return200() = runBlocking {
        val headers = mapOf(Pair(authorizationFieldName, "valid-token"))
        val entity = user.copy()
        val editor = user.copy(id = "editor")
        val expectedCode = 200

        lenient().`when`(userService.getUserFromHeaders(MockitoHelper.anyObject())).thenReturn(editor)
        lenient().`when`(userService.actionAllowed(MockitoHelper.anyObject(), MockitoHelper.anyObject()))
            .thenReturn(true)
        lenient().`when`(userService.restore(anyString(), anyString(), anyBoolean())).thenReturn(entity)

        val result = userController.restoreById(headers, entity.id)

        assertNotNull(result)
        assertNotNull(result.body)
        assertEquals(expectedCode, result.body!!.code)
        assertNotNull(result.body!!.data)
        assertNull(result.body!!.message)
    }

    @Test
    fun testRestoreById_ServerError_Return500() = runBlocking {
        val headers = mapOf(Pair(authorizationFieldName, "valid-token"))
        val entity = user.copy()
        val expectedCode = 500

        lenient().`when`(userService.getUserFromHeaders(MockitoHelper.anyObject())).thenThrow(NullPointerException("mocked"))
        lenient().`when`(controllerUtilityService.getErrorHttpReturn(MockitoHelper.anyObject(), MockitoHelper.anyObject(), MockitoHelper.anyObject(), anyInt()))
            .thenReturn(HttpReturn(expectedCode, "mocked"))

        val result = userController.restoreById(headers, entity.id)

        assertNotNull(result)
        assertNotNull(result.body)
        assertEquals(expectedCode, result.body!!.code)
        assertEquals(false, result.body!!.data)
        assertNotNull(result.body!!.message)
    }
    // endregion

    // DELETE /users/remove/{id}
    // region removeById
    @Test
    fun testRemoveById_Normal_Return200() = runBlocking {
        val headers = mapOf(Pair(authorizationFieldName, "valid-token"))
        val entity = user.copy()
        val editor = user.copy(id = "editor")
        val expectedCode = 200

        lenient().`when`(userService.getUserFromHeaders(MockitoHelper.anyObject())).thenReturn(editor)
        lenient().`when`(userService.get(anyString(), anyBoolean())).thenReturn(entity)
        lenient().`when`(userService.actionAllowedUser(MockitoHelper.anyObject(), MockitoHelper.anyObject(), MockitoHelper.anyObject(), anyBoolean()))
            .thenReturn(true)
        lenient().`when`(userService.remove(anyString(), anyString(), anyBoolean(), anyBoolean())).thenReturn(entity)

        val result = userController.removeById(headers, entity.id)

        assertNotNull(result)
        assertNotNull(result.body)
        assertEquals(expectedCode, result.body!!.code)
        assertNotNull(result.body!!.data)
        assertNull(result.body!!.message)
    }

    @Test
    fun testRemoveById_ServerError_Return500() = runBlocking {
        val headers = mapOf(Pair(authorizationFieldName, "valid-token"))
        val entity = user.copy()
        val expectedCode = 500

        lenient().`when`(userService.getUserFromHeaders(MockitoHelper.anyObject())).thenThrow(NullPointerException("mocked"))
        lenient().`when`(controllerUtilityService.getErrorHttpReturn(MockitoHelper.anyObject(), MockitoHelper.anyObject(), MockitoHelper.anyObject(), anyInt()))
            .thenReturn(HttpReturn(expectedCode, "mocked"))

        val result = userController.removeById(headers, entity.id)

        assertNotNull(result)
        assertNotNull(result.body)
        assertEquals(expectedCode, result.body!!.code)
        assertEquals(false, result.body!!.data)
        assertNotNull(result.body!!.message)
    }
    // endregion

    // GET /users/getToken
    // region getToken
    @Test
    fun testGetToken_Normal_Return200() = runBlocking {
        val editor = user.copy(id = "editor")
        val token = "some-token"
        val expectedCode = 200

        lenient().`when`(userService.authenticateGenerateToken(anyString(), eq(null), anyString())).thenReturn(token)

        val result = userController.getToken(editor.username, clearTextPassword)

        assertNotNull(result)
        assertNotNull(result.body)
        assertEquals(expectedCode, result.body!!.code)
        assertNotNull(result.body!!.data)
        assertEquals(token, result.body!!.data)
        assertNotNull(result.body!!.message)
    }

    @Test
    fun testGetToken_ServerError_Return500() = runBlocking {
        val editor = user.copy(id = "editor")
        val expectedCode = 500

        lenient().`when`(userService.authenticateGenerateToken(anyString(), eq(null), anyString())).thenThrow(NullPointerException("mocked"))
        lenient().`when`(controllerUtilityService.getErrorHttpReturn(MockitoHelper.anyObject(), MockitoHelper.anyObject(), MockitoHelper.anyObject(), anyInt()))
            .thenReturn(HttpReturn(expectedCode, "mocked"))

        val result = userController.getToken(editor.username, clearTextPassword)

        assertNotNull(result)
        assertNotNull(result.body)
        assertEquals(expectedCode, result.body!!.code)
        assertNull(result.body!!.data)
        assertNotNull(result.body!!.message)
    }
    // endregion

    // GET /users/checkToken
    // region checkToken
    @Test
    fun testCheckToken_Normal_Return200() = runBlocking {
        val headers = mapOf(Pair(authorizationFieldName, "valid-token"))
        val token = "some-token"
        val tokenResult = "token-claims"
        val editor = user.copy(id = "editor")
        val expectedCode = 200

        lenient().`when`(userService.getUserFromHeaders(MockitoHelper.anyObject())).thenReturn(editor)
        lenient().`when`(userService.actionAllowedUser(MockitoHelper.anyObject(), MockitoHelper.anyObject(), MockitoHelper.anyObject(), anyBoolean()))
            .thenReturn(true)
        lenient().`when`(userService.checkToken(anyString())).thenReturn(tokenResult)

        val result = userController.checkToken(headers, token)

        assertNotNull(result)
        assertNotNull(result.body)
        assertEquals(expectedCode, result.body!!.code)
        assertNotNull(result.body!!.data)
        assertNull(result.body!!.message)
    }

    @Test
    fun testCheckToken_ServerError_Return500() = runBlocking {
        val headers = mapOf(Pair(authorizationFieldName, "valid-token"))
        val token = "some-token"
        val expectedCode = 500

        lenient().`when`(userService.getUserFromHeaders(MockitoHelper.anyObject())).thenThrow(NullPointerException("mocked"))
        lenient().`when`(controllerUtilityService.getErrorHttpReturn(MockitoHelper.anyObject(), MockitoHelper.anyObject(), MockitoHelper.anyObject(), anyInt()))
            .thenReturn(HttpReturn(expectedCode, "mocked"))

        val result = userController.checkToken(headers, token)

        assertNotNull(result)
        assertNotNull(result.body)
        assertEquals(expectedCode, result.body!!.code)
        assertNull(result.body!!.data)
        assertNotNull(result.body!!.message)
    }
    // endregion

    // GET /users/getSelf
    // region getSelfFromToken
    @Test
    fun testGetSelfFromToken_Normal_Return200() = runBlocking {
        val headers = mapOf(Pair(authorizationFieldName, "valid-token"))
        val editor = user.copy(id = "editor")
        val expectedCode = 200

        lenient().`when`(userService.getUserFromHeaders(MockitoHelper.anyObject())).thenReturn(editor)

        val result = userController.getSelfFromToken(headers)

        assertNotNull(result)
        assertNotNull(result.body)
        assertEquals(expectedCode, result.body!!.code)
        assertNotNull(result.body!!.data)
        assertNull(result.body!!.message)
    }

    @Test
    fun testGetSelfFromToken_ServerError_Return500() = runBlocking {
        val headers = mapOf(Pair(authorizationFieldName, "valid-token"))
        val expectedCode = 500

        lenient().`when`(userService.getUserFromHeaders(MockitoHelper.anyObject())).thenThrow(NullPointerException("mocked"))
        lenient().`when`(controllerUtilityService.getErrorHttpReturn(MockitoHelper.anyObject(), MockitoHelper.anyObject(), MockitoHelper.anyObject(), anyInt()))
            .thenReturn(HttpReturn(expectedCode, "mocked"))

        val result = userController.getSelfFromToken(headers)

        assertNotNull(result)
        assertNotNull(result.body)
        assertEquals(expectedCode, result.body!!.code)
        assertNull(result.body!!.data)
        assertNotNull(result.body!!.message)
    }
    // endregion

    // POST /users/lock/{id}
    // region lockUserById
    @Test
    fun testLockUserById_Normal_Return200() = runBlocking {
        val headers = mapOf(Pair(authorizationFieldName, "valid-token"))
        val entity = user.copy()
        val lockUntil = "9999-12-31T23:59:59.999Z"
        val reason = "some reason"
        val editor = user.copy(id = "editor")
        val expectedCode = 200

        lenient().`when`(userService.getUserFromHeaders(MockitoHelper.anyObject())).thenReturn(editor)
        lenient().`when`(userService.actionAllowed(MockitoHelper.anyObject(), MockitoHelper.anyObject()))
            .thenReturn(true)
        lenient().`when`(userService.get(anyString(), anyBoolean())).thenReturn(entity)
        lenient().`when`(userService.setUserLock(MockitoHelper.anyObject(), anyString())).thenReturn(entity)
        lenient().`when`(userService.setModeratorComment(MockitoHelper.anyObject(), anyString())).thenReturn(entity)
        lenient().`when`(userService.update(MockitoHelper.anyObject(), anyString(), anyBoolean(), anyBoolean())).thenReturn(entity)

        val result = userController.lockUserById(headers, entity.id, lockUntil, reason)

        assertNotNull(result)
        assertNotNull(result.body)
        assertEquals(expectedCode, result.body!!.code)
        assertNotNull(result.body!!.data)
        assertNotNull(result.body!!.message)
    }

    @Test
    fun testLockUserById_ServerError_Return500() = runBlocking {
        val headers = mapOf(Pair(authorizationFieldName, "valid-token"))
        val entity = user.copy()
        val lockUntil = "9999-12-31T23:59:59.999Z"
        val reason = "some reason"
        val expectedCode = 500

        lenient().`when`(userService.getUserFromHeaders(MockitoHelper.anyObject())).thenThrow(NullPointerException("mocked"))
        lenient().`when`(controllerUtilityService.getErrorHttpReturn(MockitoHelper.anyObject(), MockitoHelper.anyObject(), MockitoHelper.anyObject(), anyInt()))
            .thenReturn(HttpReturn(expectedCode, "mocked"))

        val result = userController.lockUserById(headers, entity.id, lockUntil, reason)

        assertNotNull(result)
        assertNotNull(result.body)
        assertEquals(expectedCode, result.body!!.code)
        assertNull(result.body!!.data)
        assertNotNull(result.body!!.message)
    }
    // endregion

    // POST /users/unlock/{id}
    // region unLockUser
    @Test
    fun testUnLockUserById_Normal_Return200() = runBlocking {
        val headers = mapOf(Pair(authorizationFieldName, "valid-token"))
        val entity = user.copy()
        val reason = "some reason"
        val editor = user.copy(id = "editor")
        val expectedCode = 200

        lenient().`when`(userService.getUserFromHeaders(MockitoHelper.anyObject())).thenReturn(editor)
        lenient().`when`(userService.actionAllowed(MockitoHelper.anyObject(), MockitoHelper.anyObject()))
            .thenReturn(true)
        lenient().`when`(userService.get(anyString(), anyBoolean())).thenReturn(entity)
        lenient().`when`(userService.setUserLock(MockitoHelper.anyObject(), anyString())).thenReturn(entity)
        lenient().`when`(userService.setModeratorComment(MockitoHelper.anyObject(), anyString())).thenReturn(entity)
        lenient().`when`(userService.update(MockitoHelper.anyObject(), anyString(), anyBoolean(), anyBoolean())).thenReturn(entity)

        val result = userController.unlockUserById(headers, entity.id, reason)

        assertNotNull(result)
        assertNotNull(result.body)
        assertEquals(expectedCode, result.body!!.code)
        assertNotNull(result.body!!.data)
        assertNotNull(result.body!!.message)
    }

    @Test
    fun testUnLockUserById_ServerError_Return500() = runBlocking {
        val headers = mapOf(Pair(authorizationFieldName, "valid-token"))
        val entity = user.copy()
        val reason = "some reason"
        val expectedCode = 500

        lenient().`when`(userService.getUserFromHeaders(MockitoHelper.anyObject())).thenThrow(NullPointerException("mocked"))
        lenient().`when`(controllerUtilityService.getErrorHttpReturn(MockitoHelper.anyObject(), MockitoHelper.anyObject(), MockitoHelper.anyObject(), anyInt()))
            .thenReturn(HttpReturn(expectedCode, "mocked"))

        val result = userController.unlockUserById(headers, entity.id, reason)

        assertNotNull(result)
        assertNotNull(result.body)
        assertEquals(expectedCode, result.body!!.code)
        assertNull(result.body!!.data)
        assertNotNull(result.body!!.message)
    }
    // endregion

    // POST /users/addModeratorComment/{id}
    // region addModeratorCommentById
    @Test
    fun testAddModeratorCommentById_Normal_Return200() = runBlocking {
        val headers = mapOf(Pair(authorizationFieldName, "valid-token"))
        val entity = user.copy()
        val comment = "some comment"
        val editor = user.copy(id = "editor")
        val expectedCode = 200

        lenient().`when`(userService.getUserFromHeaders(MockitoHelper.anyObject())).thenReturn(editor)
        lenient().`when`(userService.actionAllowed(MockitoHelper.anyObject(), MockitoHelper.anyObject()))
            .thenReturn(true)
        lenient().`when`(userService.get(anyString(), anyBoolean())).thenReturn(entity)
        lenient().`when`(userService.setModeratorComment(MockitoHelper.anyObject(), anyString())).thenReturn(entity)
        lenient().`when`(userService.update(MockitoHelper.anyObject(), anyString(), anyBoolean(), anyBoolean())).thenReturn(entity)

        val result = userController.addModeratorCommentById(headers, entity.id, comment)

        assertNotNull(result)
        assertNotNull(result.body)
        assertEquals(expectedCode, result.body!!.code)
        assertNotNull(result.body!!.data)
        assertNotNull(result.body!!.message)
    }

    @Test
    fun testAddModeratorCommentById_ServerError_Return500() = runBlocking {
        val headers = mapOf(Pair(authorizationFieldName, "valid-token"))
        val entity = user.copy()
        val comment = "some comment"
        val expectedCode = 500

        lenient().`when`(userService.getUserFromHeaders(MockitoHelper.anyObject())).thenThrow(NullPointerException("mocked"))
        lenient().`when`(controllerUtilityService.getErrorHttpReturn(MockitoHelper.anyObject(), MockitoHelper.anyObject(), MockitoHelper.anyObject(), anyInt()))
            .thenReturn(HttpReturn(expectedCode, "mocked"))

        val result = userController.addModeratorCommentById(headers, entity.id, comment)

        assertNotNull(result)
        assertNotNull(result.body)
        assertEquals(expectedCode, result.body!!.code)
        assertNull(result.body!!.data)
        assertNotNull(result.body!!.message)
    }
    // endregion
}
