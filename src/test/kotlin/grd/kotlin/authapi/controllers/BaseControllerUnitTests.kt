package grd.kotlin.authapi.controllers

import com.google.gson.Gson
import grd.kotlin.authapi.MockitoHelper
import grd.kotlin.authapi.dto.Converter
import grd.kotlin.authapi.dto.SystemDto
import grd.kotlin.authapi.enums.UserRole
import grd.kotlin.authapi.exceptions.NotFoundException
import grd.kotlin.authapi.models.AUser
import grd.kotlin.authapi.models.HttpReturn
import grd.kotlin.authapi.models.System
import grd.kotlin.authapi.services.BaseService
import grd.kotlin.authapi.services.ControllerUtilityService
import grd.kotlin.authapi.services.SystemService
import grd.kotlin.authapi.services.UserService
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
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.test.context.junit.jupiter.SpringExtension

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ExtendWith(SpringExtension::class, MockitoExtension::class)
class BaseControllerUnitTests
{
    @InjectMocks
    private lateinit var controller: BaseController<System, SystemDto, SystemService>

    @Mock
    private lateinit var controllerUtilityService: ControllerUtilityService

    @Mock
    private lateinit var userService: UserService

    @Mock
    private lateinit var entityService: SystemService

    @Mock
    private lateinit var gson: Gson

    @Autowired
    private lateinit var injectedSettings: Settings

    private lateinit var authorizationFieldName: String

    private val user = AUser(id = "TestId01", username = "TestUser01", password = "pass", role = UserRole.MODERATOR)
    private val system = System(id = "TestId01", name = "TestSystem01", active = true)

    @BeforeEach
    fun setup()
    {
        controllerUtilityService = mock(ControllerUtilityService::class.java)
        userService = mock(UserService::class.java)
        entityService = mock(SystemService::class.java)
        gson = mock(Gson::class.java)
        MockitoAnnotations.openMocks(this)

        controller.tClass = System::class.java
        controller.tDtoClass = SystemDto::class.java
        authorizationFieldName = injectedSettings.server.authenticationHeader!!
    }

    // POST /x
    // region add
    @Test
    fun testAdd_Normal_Return201() = runBlocking {
        val headers = mapOf(Pair(authorizationFieldName, "valid-token"))
        val entity = system.copy()
        val dto = Converter.convert(entity, SystemDto::class.java)
        val editor = user.copy(id = "editor")
        val expectedCode = 201

        lenient().`when`(userService.getUserFromHeaders(MockitoHelper.anyObject())).thenReturn(editor)
        lenient().`when`(userService.actionAllowed(MockitoHelper.anyObject(), MockitoHelper.anyObject())).thenReturn(true)
        lenient().`when`(entityService.add(MockitoHelper.anyObject(), MockitoHelper.anyObject(), anyBoolean())).thenReturn(entity)

        val result = controller.add(headers, dto)

        assertNotNull(result)
        assertNotNull(result.body)
        assertEquals(expectedCode, result.body!!.code)
        assertNotNull(result.body!!.data)
        assertNull(result.body!!.message)
    }

    @Test
    fun testAdd_ServerError_Return500() = runBlocking {
        val headers = mapOf(Pair(authorizationFieldName, "valid-token"))
        val entity = system.copy()
        val dto = Converter.convert(entity, SystemDto::class.java)
        val expectedCode = 500

        lenient().`when`(userService.getUserFromHeaders(MockitoHelper.anyObject())).thenThrow(NullPointerException("mocked"))
        lenient().`when`(controllerUtilityService.getErrorHttpReturn(MockitoHelper.anyObject(), MockitoHelper.anyObject(), MockitoHelper.anyObject(), anyInt()))
            .thenReturn(HttpReturn(expectedCode, "mocked"))

        val result = controller.add(headers, dto)

        assertNotNull(result)
        assertNotNull(result.body)
        assertEquals(expectedCode, result.body!!.code)
        assertNull(result.body!!.data)
        assertNotNull(result.body!!.message)
    }
    // endregion

    // GET /x/random
    // region getRandom
    @Test
    fun testGetRandom_Normal_Return200() = runBlocking {
        val entity = system.copy()
        val expectedCode = 200

        // kotlin.UninitializedPropertyAccessException: lateinit property entityService has not been initialized from controller
        lenient().`when`(entityService.getRandom()).thenReturn(entity)

        val result = controller.getRandom()

        assertNotNull(result)
        assertNotNull(result.body)
        assertEquals(expectedCode, result.body!!.code)
        assertNotNull(result.body!!.data)
        assertNull(result.body!!.message)
    }

    @Test
    fun testGetRandom_ServerError_Return500() = runBlocking {
        val expectedCode = 500

        lenient().`when`(userService.getRandom()).thenThrow(NullPointerException("mocked"))
        lenient().`when`(controllerUtilityService.getErrorHttpReturn(MockitoHelper.anyObject(), MockitoHelper.anyObject(), MockitoHelper.anyObject(), anyInt()))
            .thenReturn(HttpReturn(expectedCode, "mocked"))

        val result = controller.getRandom()

        assertNotNull(result)
        assertNotNull(result.body)
        assertEquals(expectedCode, result.body!!.code)
        assertNull(result.body!!.data)
        assertNotNull(result.body!!.message)
    }
    // endregion

    // GET /x/{id}
    // region getById
    @Test
    fun testGetById_Normal_Return200() = runBlocking {
        val entity = user.copy()
        val expectedCode = 200

        lenient().`when`(userService.get(anyString(), anyBoolean())).thenReturn(entity)

        val result = controller.getById(entity.id)

        assertNotNull(result)
        assertNotNull(result.body)
        assertEquals(expectedCode, result.body!!.code)
        assertNotNull(result.body!!.data)
        assertNull(result.body!!.message)
    }

    @Test
    fun testGetById_ServerError_Return500() = runBlocking {
        val entity = user.copy()
        val expectedCode = 500

        lenient().`when`(userService.get(anyString(), anyBoolean())).thenThrow(NullPointerException("mocked"))
        lenient().`when`(controllerUtilityService.getErrorHttpReturn(MockitoHelper.anyObject(), MockitoHelper.anyObject(), MockitoHelper.anyObject(), anyInt()))
            .thenReturn(HttpReturn(expectedCode, "mocked"))

        val result = controller.getById(entity.id)

        assertNotNull(result)
        assertNotNull(result.body)
        assertEquals(expectedCode, result.body!!.code)
        assertNull(result.body!!.data)
        assertNotNull(result.body!!.message)
    }
    // endregion

    // GET /x/detailed/{id}
    // region getDetailedById
    @Test
    fun testGetDetailedById_NoSuchEditor_Return400() = runBlocking {
        val headers = mapOf(Pair(authorizationFieldName, "valid-token"))
        val entity = user.copy()
        val expectedCode = 400

        lenient().`when`(userService.getUserFromHeaders(MockitoHelper.anyObject())).thenThrow(NotFoundException("mocked"))

        val result = controller.getDetailedById(headers, entity.id)

        assertNotNull(result)
        assertNotNull(result.body)
        assertEquals(expectedCode, result.body!!.code)
        assertNull(result.body!!.data)
        assertNotNull(result.body!!.message)
    }

    @Test
    fun testGetByDetailedId_Normal_Return200() = runBlocking {
        val headers = mapOf(Pair(authorizationFieldName, "valid-token"))
        val entity = user.copy()
        val editor = user.copy(id = "editor", role = UserRole.MODERATOR)
        val expectedCode = 200

        lenient().`when`(userService.getUserFromHeaders(MockitoHelper.anyObject())).thenReturn(editor)
        lenient().`when`(userService.actionAllowed(MockitoHelper.anyObject(), MockitoHelper.anyObject())).thenReturn(true)
        lenient().`when`(userService.get(anyString(), anyBoolean())).thenReturn(entity)

        val result = controller.getDetailedById(headers, entity.id)

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

        val result = controller.getDetailedById(headers, entity.id)

        assertNotNull(result)
        assertNotNull(result.body)
        assertEquals(expectedCode, result.body!!.code)
        assertNull(result.body!!.data)
        assertNotNull(result.body!!.message)
    }
    // endregion

    // GET /x
    // region getAll
    @Test
    fun testGetAll_Normal_Return200() = runBlocking {
        val entity1 = user.copy(id = "entity1")
        val entity2 = user.copy(id = "entity2")
        val entity3 = user.copy(id = "entity3")
        val all = listOf(entity1, entity2, entity3)
        val pageable = PageRequest.of(0, 20)
        val allPage = PageImpl(all, pageable, all.count().toLong())
        val expectedCode = 200

        lenient().`when`(userService.getAll(
            anyBoolean(),
            MockitoHelper.anyObject())).thenReturn(allPage)

        val result = controller.getAll(
            null, null)

        assertNotNull(result)
        assertNotNull(result.body)
        assertEquals(expectedCode, result.body!!.code)
        assertNotNull(result.body!!.data)
        assertNull(result.body!!.message)
    }

    @Test
    fun testGetAll_NormalSetPage_Return200() = runBlocking {
        val pageNumber = 1
        val pageSize = 1
        val entity1 = user.copy(id = "entity1")
        val entity2 = user.copy(id = "entity2")
        val entity3 = user.copy(id = "entity3")
        val all = listOf(entity1, entity2, entity3)
        val pageable = PageRequest.of(pageNumber, pageSize)
        val allPage = PageImpl(all, pageable, all.count().toLong())
        val expectedCode = 200

        lenient().`when`(userService.getAll(
            anyBoolean(),
            MockitoHelper.anyObject())).thenReturn(allPage)

        val result = controller.getAll(
            pageNumber, pageSize)

        assertNotNull(result)
        assertNotNull(result.body)
        assertEquals(expectedCode, result.body!!.code)
        assertNotNull(result.body!!.data)
        assertNull(result.body!!.message)
    }

    @Test
    fun testGetAll_ServerError_Return500() = runBlocking {
        val expectedCode = 500

        lenient().`when`(userService.getAll(anyBoolean(), MockitoHelper.anyObject())).thenThrow(NullPointerException("mocked"))
        lenient().`when`(controllerUtilityService.getErrorHttpReturn(MockitoHelper.anyObject(), MockitoHelper.anyObject(), MockitoHelper.anyObject(), anyInt()))
            .thenReturn(HttpReturn(expectedCode, "mocked"))

        val result = controller.getAll(
            null, null)

        assertNotNull(result)
        assertNotNull(result.body)
        assertEquals(expectedCode, result.body!!.code)
        assertNull(result.body!!.data)
        assertNotNull(result.body!!.message)
    }
    // endregion

    // PATCH /x/{id}
    // region patchById
    @Test
    fun testPatchById_Normal_Return200() = runBlocking {
        val headers = mapOf(Pair(authorizationFieldName, "valid-token"))
        val entity = user.copy()
        val editor = user.copy(id = "editor")
        val expectedCode = 200

        lenient().`when`(userService.getUserFromHeaders(MockitoHelper.anyObject())).thenReturn(editor)
        lenient().`when`(userService.actionAllowed(MockitoHelper.anyObject(), MockitoHelper.anyObject())).thenReturn(true)
        lenient().`when`(userService.patch(anyString(), anyString())).thenReturn(entity)
        lenient().`when`(userService.update(MockitoHelper.anyObject(), anyString(), anyBoolean(), anyBoolean())).thenReturn(entity)

        val result = controller.patchById(headers, entity.id, "{'some': 'json'}")

        assertNotNull(result)
        assertNotNull(result.body)
        assertEquals(expectedCode, result.body!!.code)
        assertNotNull(result.body!!.data)
        assertNotNull(result.body!!.message)
    }

    @Test
    fun testPatchById_ServerError_Return500() = runBlocking {
        val headers = mapOf(Pair(authorizationFieldName, "valid-token"))
        val entity = user.copy()
        val expectedCode = 500

        lenient().`when`(userService.getUserFromHeaders(MockitoHelper.anyObject())).thenThrow(NullPointerException("mocked"))
        lenient().`when`(controllerUtilityService.getErrorHttpReturn(MockitoHelper.anyObject(), MockitoHelper.anyObject(), MockitoHelper.anyObject(), anyInt()))
            .thenReturn(HttpReturn(expectedCode, "mocked"))

        val result = controller.patchById(headers, entity.id, "{'some': 'json'}")

        assertNotNull(result)
        assertNotNull(result.body)
        assertEquals(expectedCode, result.body!!.code)
        assertNull(result.body!!.data)
        assertNotNull(result.body!!.message)
    }
    // endregion

    // DELETE /x/{id}
    // region softDeleteById
    @Test
    fun testSoftDeleteById_Normal_Return200() = runBlocking {
        val headers = mapOf(Pair(authorizationFieldName, "valid-token"))
        val entity = user.copy()
        val editor = user.copy(id = "editor")
        val expectedCode = 200

        lenient().`when`(userService.getUserFromHeaders(MockitoHelper.anyObject())).thenReturn(editor)
        lenient().`when`(userService.actionAllowed(MockitoHelper.anyObject(), MockitoHelper.anyObject())).thenReturn(true)
        lenient().`when`(userService.delete(anyString(), anyString(), anyBoolean())).thenReturn(entity)

        val result = controller.softDeleteById(headers, entity.id)

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

        val result = controller.softDeleteById(headers, entity.id)

        assertNotNull(result)
        assertNotNull(result.body)
        assertEquals(expectedCode, result.body!!.code)
        assertNull(result.body!!.data)
        assertNotNull(result.body!!.message)
    }
    // endregion

    // GET /x/restore/{id}
    // region restoreById
    @Test
    fun testRestoreById_Normal_Return200() = runBlocking {
        val headers = mapOf(Pair(authorizationFieldName, "valid-token"))
        val entity = user.copy()
        val editor = user.copy(id = "editor")
        val expectedCode = 200

        lenient().`when`(userService.getUserFromHeaders(MockitoHelper.anyObject())).thenReturn(editor)
        lenient().`when`(userService.actionAllowed(MockitoHelper.anyObject(), MockitoHelper.anyObject())).thenReturn(true)
        lenient().`when`(userService.restore(anyString(), anyString(), anyBoolean())).thenReturn(entity)

        val result = controller.restoreById(headers, entity.id)

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

        val result = controller.restoreById(headers, entity.id)

        assertNotNull(result)
        assertNotNull(result.body)
        assertEquals(expectedCode, result.body!!.code)
        assertNull(result.body!!.data)
        assertNotNull(result.body!!.message)
    }
    // endregion

    // DELETE /x/remove/{id}
    // region removeById
    @Test
    fun testRemoveById_Normal_Return200() = runBlocking {
        val headers = mapOf(Pair(authorizationFieldName, "valid-token"))
        val entity = user.copy()
        val editor = user.copy(id = "editor")
        val expectedCode = 200

        lenient().`when`(userService.getUserFromHeaders(MockitoHelper.anyObject())).thenReturn(editor)
        lenient().`when`(userService.actionAllowed(MockitoHelper.anyObject(), MockitoHelper.anyObject())).thenReturn(true)
        lenient().`when`(userService.remove(anyString(), anyString(), anyBoolean(), anyBoolean())).thenReturn(entity)

        val result = controller.removeById(headers, entity.id)

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

        val result = controller.removeById(headers, entity.id)

        assertNotNull(result)
        assertNotNull(result.body)
        assertEquals(expectedCode, result.body!!.code)
        assertNull(result.body!!.data)
        assertNotNull(result.body!!.message)
    }
    // endregion
}
