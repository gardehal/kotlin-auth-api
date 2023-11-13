package grd.kotlin.authapi.services

import grd.kotlin.authapi.MockitoHelper
import grd.kotlin.authapi.exceptions.ArgumentException
import grd.kotlin.authapi.exceptions.DatabaseErrorException
import grd.kotlin.authapi.exceptions.DuplicateException
import grd.kotlin.authapi.exceptions.NotFoundException
import grd.kotlin.authapi.logging.TextLogService
import grd.kotlin.authapi.models.AUser
import grd.kotlin.authapi.repositories.FirebaseRepository
import grd.kotlin.authapi.testdata.TestEntities
import com.github.fge.jsonpatch.JsonPatchException
import com.google.cloud.firestore.Query
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
import org.springframework.test.context.junit.jupiter.SpringExtension

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ExtendWith(SpringExtension::class, MockitoExtension::class)
class BaseServiceUnitTests
{
    @InjectMocks
    private lateinit var service: BaseService<AUser>

    @Mock
    private lateinit var utilityService: UtilityService // Note: For BaseService

    @Mock
    private lateinit var logService: LogService // Note: For BaseService

    @Mock
    private lateinit var iLogService: TextLogService // Note: For BaseService

    @Mock
    private lateinit var repository: FirebaseRepository<AUser>

    private final val testEntities = TestEntities()
    val entity = testEntities.userOther

    @BeforeEach
    fun setup()
    {
        utilityService = mock(UtilityService::class.java)
        iLogService = mock(TextLogService::class.java)
        logService = mock(LogService::class.java)
        @Suppress("UNCHECKED_CAST")
        repository = mock(FirebaseRepository::class.java) as FirebaseRepository<AUser>
        MockitoAnnotations.openMocks(this)

        service.disableLogs = true
        service.repository = repository
        service.validateClass = true

        lenient().`when`(service.logService.logger).thenReturn(iLogService) // Return mocked interface directly
    }

    // region getNewId
    @Test
    fun testGetNewId_GetId_ReturnString()
    {
        val result = service.getNewId()

        assertNotNull(result)
        assertFalse(result.isBlank())
    }
    // endregion

    // region add
    @Test
    fun testAdd_AddDuplicate_ThrowDuplicateException()
    {
        val entity = entity.copy()

        val spy = spy(service)
        lenient().`when`(utilityService.getReflection(MockitoHelper.anyObject(), anyString())).thenReturn(entity.id)
        lenient().doReturn(true).`when`(spy).exists(anyString())
        lenient().doThrow(DuplicateException::class.java).`when`(spy).add(MockitoHelper.anyObject(), anyString(), anyBoolean())

        try
        {
            spy.add(entity, entity.id, false)
            fail() // Fail here
        }
        catch(e: DuplicateException)
        {
            assertTrue(true)
        }
    }

    @Test
    fun testAdd_FailSaving_ThrowDatabaseErrorException()
    {
        val entity = entity.copy()

        val spy = spy(service)
        lenient().`when`(utilityService.getReflection(MockitoHelper.anyObject(), anyString())).thenReturn(entity.id)
        lenient().doReturn(false).`when`(spy).exists(anyString())
        lenient().doReturn(null).`when`(spy).updateProperty(MockitoHelper.anyObject(), anyString(), anyString()) // Return value not important
        lenient().`when`(repository.save(anyString(), MockitoHelper.anyObject(), anyBoolean())).thenReturn(null)

        try
        {
            spy.add(entity, entity.id, false)
            fail() // Fail here
        }
        catch(e: DatabaseErrorException)
        {
            assertTrue(true)
        }
    }

    @Test
    fun testAdd_Normal_ReturnEntity()
    {
        val entity = entity.copy()

        val spy = spy(service)
        lenient().`when`(utilityService.getReflection(MockitoHelper.anyObject(), anyString())).thenReturn(entity.id)
        lenient().doReturn(false).`when`(spy).exists(anyString())
        lenient().doReturn(null).`when`(spy).updateProperty(MockitoHelper.anyObject(), anyString(), anyString()) // Return value not important
        lenient().`when`(repository.save(anyString(), MockitoHelper.anyObject(), anyBoolean())).thenReturn(entity)

        val result = spy.add(entity, entity.id, false)

        assertEquals(result.id, entity.id)
    }
    // endregion

    // region exists
    @Test
    fun testExists_EntityExists_ReturnTrue()
    {
        val entity = entity.copy()

        val spy = spy(service)
        lenient().doReturn(entity).`when`(spy).get(anyString(), anyBoolean())

        val result = spy.exists(entity.id)

        assertEquals(true, result)
    }

    @Test
    fun testExists_EntityDoesNotExist_ReturnFalse()
    {
        val entity = entity.copy()

        val spy = spy(service)
        lenient().doThrow(NotFoundException("mocked")).`when`(spy).get(anyString(), anyBoolean())

        val result = spy.exists(entity.id)

        assertEquals(false, result)
    }
    // endregion

    // region isDeleted
    @Test
    fun testIsDeleted_NotFound_ReturnFalse()
    {
        val entity = entity.copy()

        val spy = spy(service)
        lenient().doThrow(NotFoundException("mocked")).`when`(spy).get(anyString(), anyBoolean())

        val result = spy.isDeleted(entity.id)

        assertEquals(false, result)
    }

    @Test
    fun testIsDeleted_Deleted_ReturnTrue()
    {
        val entity = entity.copy()

        val spy = spy(service)
        lenient().doReturn(null).`when`(spy).get(anyString(), anyBoolean()) // Return value not important
        lenient().doReturn(true).`when`(spy).isSoftDeleted(MockitoHelper.anyObject())

        val result = spy.isDeleted(entity.id)

        assertEquals(true, result)
    }

    @Test
    fun testIsDeleted_NotDeleted_ReturnFalse()
    {
        val entity = entity.copy()

        val spy = spy(service)
        lenient().doReturn(null).`when`(spy).get(anyString(), anyBoolean()) // Return value not important
        lenient().doReturn(false).`when`(spy).isSoftDeleted(MockitoHelper.anyObject())

        val result = spy.isDeleted(entity.id)

        assertEquals(false, result)
    }
    // endregion

    // region get
    @Test
    fun testGet_NotFound_ThrowNotFoundException()
    {
        val entity = entity.copy()

        val spy = spy(service)
        lenient().`when`(repository.findById(anyString())).thenReturn(null)

        try
        {
            spy.get(entity.id, false)
            fail() // Fail here
        }
        catch(e: NotFoundException)
        {
            assertTrue(true)
        }
    }

    @Test
    fun testGet_ExcludeDeleted_ThrowNotFoundException()
    {
        val entity = entity.copy()

        val spy = spy(service)
        lenient().`when`(repository.findById(anyString())).thenReturn(entity)
        lenient().doReturn(true).`when`(spy).isSoftDeleted(MockitoHelper.anyObject())

        try
        {
            spy.get(entity.id, false)
            fail() // Fail here
        }
        catch(e: NotFoundException)
        {
            assertTrue(true)
        }
    }

    @Test
    fun testGet_IncludeDeleted_ReturnEntity()
    {
        val entity = entity.copy()

        val spy = spy(service)
        lenient().`when`(repository.findById(anyString())).thenReturn(entity)
        lenient().doReturn(false).`when`(spy).isSoftDeleted(MockitoHelper.anyObject())

        val result = spy.get(entity.id, true)

        assertEquals(entity, result)
    }
    // endregion

    // region getRandom
    @Test
    fun testGetRandom_GetEmpty_ThrowNotFoundException()
    {
        val all = listOf<AUser>()

        val spy = spy(service)
        lenient().doReturn(all).`when`(spy).getAll()

        try
        {
            spy.getRandom()
            fail() // Fail here
        }
        catch(e: NotFoundException)
        {
            assertTrue(true)
        }
    }

    @Test
    fun testGetRandom_GetSingle_Return()
    {
        val copy1 = entity.copy()
        val all = listOf(copy1)

        val spy = spy(service)
        lenient().doReturn(all).`when`(spy).getAll()

        val res = spy.getRandom()

        assertNotNull(res)
        assertEquals(copy1, res)
    }

    @Test
    fun testGetRandom_Normal_Return()
    {
        val id = "entityId"
        val copy1 = entity.copy(id = "${id}1")
        val copy2 = entity.copy(id = "${id}2")
        val copy3 = entity.copy(id = "${id}3")
        val copy4 = entity.copy(id = "${id}4")
        val all = listOf(copy1, copy2, copy3, copy4)

        val spy = spy(service)
        lenient().doReturn(all).`when`(spy).getAll()

        val res = spy.getRandom()

        assertNotNull(res)
        assertNotNull(res.id)
        assertEquals(res.id.length, id.length + 1)
        assertTrue(res.id.contains(id))
    }
    // endregion

    // region getQueried
    @Test
    fun testGetQueried_Normal_Return()
    {
        val entity = entity.copy()
        val entityList = listOf(entity)

        lenient().`when`(repository.getQueried<Any, Any>(MockitoHelper.anyObject())).thenReturn(entityList)

        val result = service.getQueried { e: Query -> e.whereEqualTo("id", entity.id) }

        assertNotNull(result)
        assertEquals(entityList, result)
    }
    // endregion

    // region getQueried (paged)
    @Test
    fun testGetQueried_PagedNormal_Return()
    {
        val entity = entity.copy()
        val entityList = listOf(entity)
        val entityPageable = PageRequest.of(0, 20)
        val page = PageImpl(entityList, entityPageable, entityList.count().toLong())

        val spy = spy(service)
        lenient().doReturn(page).`when`(spy).getQueried<Any, Any>(MockitoHelper.anyObject(), MockitoHelper.anyObject())

        val result = spy.getQueried({ e: Query -> e.whereEqualTo("id", entity.id) }, entityPageable)

        assertNotNull(result)
        assertEquals(page, result)
    }
    // endregion

    // region getAll
    @Test
    fun testGetAll_IncludeDeleted_ReturnList()
    {
        val entityA = entity.copy(id = "A")
        val entityB = entity.copy(id = "B")
        val entityC = entity.copy(id = "C")
        val entitiesList = listOf(entityA, entityB, entityC)

        val spy = spy(service)
        lenient().`when`(repository.findAll()).thenReturn(entitiesList)

        val result = spy.getAll(true)

        assertEquals(3, result.count())
    }

    @Test
    fun testGetAll_IncludeDeletedWithDeleted_ReturnList()
    {
        val entityA = entity.copy(id = "A")
        val entityB = entity.copy(id = "B")
        val entityC = entity.copy(id = "C")
        val entitiesList = listOf(entityA, entityB, entityC)

        val spy = spy(service)
        lenient().`when`(repository.findAll()).thenReturn(entitiesList)
        lenient().doReturn(false).`when`(spy).isSoftDeleted(MockitoHelper.safeEq(entityA))
        lenient().doReturn(true).`when`(spy).isSoftDeleted(MockitoHelper.safeEq(entityB)) // Marked as soft-deleted
        lenient().doReturn(false).`when`(spy).isSoftDeleted(MockitoHelper.safeEq(entityC))

        val result = spy.getAll(true)

        assertEquals(3, result.count())
    }

    @Test
    fun testGetAll_ExcludeDeleted_ReturnList()
    {
        val entityA = entity.copy(id = "A")
        val entityB = entity.copy(id = "B")
        val entityC = entity.copy(id = "C")
        val entitiesList = listOf(entityA, entityB, entityC)

        val spy = spy(service)
        lenient().`when`(repository.findAll()).thenReturn(entitiesList)
        lenient().doReturn(false).`when`(spy).isSoftDeleted(MockitoHelper.anyObject())

        val result = spy.getAll(false)

        assertEquals(3, result.count())
    }

    @Test
    fun testGetAll_ExcludeDeletedWithDeleted_ReturnList()
    {
        val entityA = entity.copy(id = "A")
        val entityB = entity.copy(id = "B")
        val entityC = entity.copy(id = "C")
        val entitiesList = listOf(entityA, entityB, entityC)

        val spy = spy(service)
        lenient().`when`(repository.findAll()).thenReturn(entitiesList)
        lenient().doReturn(false).`when`(spy).isSoftDeleted(MockitoHelper.safeEq(entityA))
        lenient().doReturn(true).`when`(spy).isSoftDeleted(MockitoHelper.safeEq(entityB)) // Marked as soft-deleted
        lenient().doReturn(false).`when`(spy).isSoftDeleted(MockitoHelper.safeEq(entityC))

        val result = spy.getAll(false)

        assertEquals(2, result.count())
    }
    // endregion

    // region getAll (paged)
    @Test
    fun testGetAll_PagedNormal_Return()
    {
        val entity = entity.copy()
        val entityList = listOf(entity)
        val entityPageable = PageRequest.of(0, 20)
        val page = PageImpl(entityList, entityPageable, entityList.count().toLong())

        val spy = spy(service)
        lenient().doReturn(entityList).`when`(spy).getAll(anyBoolean())

        val result = spy.getAll(true, entityPageable)

        assertNotNull(result)
        assertEquals(page, result)
    }
    // endregion

    // region getAllIds
    @Test
    fun testGetAllIds_IncludeDeleted_ReturnList()
    {
        val entityA = entity.copy(id = "A")
        val entityB = entity.copy(id = "B")
        val entityC = entity.copy(id = "C")
        val entitiesList = listOf(entityA, entityB, entityC)

        val spy = spy(service)
        lenient().`when`(repository.findAll()).thenReturn(entitiesList)
        lenient().`when`(utilityService.getReflection(MockitoHelper.safeEq(entityA), anyString())).thenReturn(entityA.id)
        lenient().`when`(utilityService.getReflection(MockitoHelper.safeEq(entityB), anyString())).thenReturn(entityB.id)
        lenient().`when`(utilityService.getReflection(MockitoHelper.safeEq(entityC), anyString())).thenReturn(entityC.id)
        lenient().doReturn(false).`when`(spy).isSoftDeleted(MockitoHelper.safeEq(entityA))
        lenient().doReturn(false).`when`(spy).isSoftDeleted(MockitoHelper.safeEq(entityB))
        lenient().doReturn(false).`when`(spy).isSoftDeleted(MockitoHelper.safeEq(entityC))

        val result = spy.getAllIds(false)

        assertEquals(3, result.count())
    }

    @Test
    fun testGetAllIds_IncludeDeletedWithDeleted_ReturnList()
    {
        val entityA = entity.copy(id = "A")
        val entityB = entity.copy(id = "B")
        val entityC = entity.copy(id = "C")
        val entitiesList = listOf(entityA, entityB, entityC)

        val spy = spy(service)
        lenient().`when`(repository.findAll()).thenReturn(entitiesList)
        lenient().`when`(utilityService.getReflection(MockitoHelper.safeEq(entityA), anyString())).thenReturn(entityA.id)
        lenient().`when`(utilityService.getReflection(MockitoHelper.safeEq(entityB), anyString())).thenReturn(entityB.id)
        lenient().`when`(utilityService.getReflection(MockitoHelper.safeEq(entityC), anyString())).thenReturn(entityC.id)
        lenient().doReturn(false).`when`(spy).isSoftDeleted(MockitoHelper.safeEq(entityA))
        lenient().doReturn(true).`when`(spy).isSoftDeleted(MockitoHelper.safeEq(entityB)) // Marked as soft-deleted
        lenient().doReturn(false).`when`(spy).isSoftDeleted(MockitoHelper.safeEq(entityC))

        val result = spy.getAllIds(true)

        assertEquals(3, result.count())
    }

    @Test
    fun testGetAllIds_ExcludeDeleted_ReturnList()
    {
        val entityA = entity.copy(id = "A")
        val entityB = entity.copy(id = "B")
        val entityC = entity.copy(id = "C")
        val entitiesList = listOf(entityA, entityB, entityC)

        val spy = spy(service)
        lenient().`when`(repository.findAll()).thenReturn(entitiesList)
        lenient().`when`(utilityService.getReflection(MockitoHelper.safeEq(entityA), anyString())).thenReturn(entityA.id)
        lenient().`when`(utilityService.getReflection(MockitoHelper.safeEq(entityB), anyString())).thenReturn(entityB.id)
        lenient().`when`(utilityService.getReflection(MockitoHelper.safeEq(entityC), anyString())).thenReturn(entityC.id)
        lenient().doReturn(false).`when`(spy).isSoftDeleted(MockitoHelper.anyObject())

        val result = spy.getAllIds(false)

        assertEquals(3, result.count())
    }

    @Test
    fun testGetAllIds_ExcludeDeletedWithDeleted_ReturnList()
    {
        val entityA = entity.copy(id = "A")
        val entityB = entity.copy(id = "B")
        val entityC = entity.copy(id = "C")
        val entitiesList = listOf(entityA, entityB, entityC)

        val spy = spy(service)
        lenient().`when`(repository.findAll()).thenReturn(entitiesList)
        lenient().`when`(utilityService.getReflection(MockitoHelper.safeEq(entityA), anyString())).thenReturn(entityA.id)
        lenient().`when`(utilityService.getReflection(MockitoHelper.safeEq(entityB), anyString())).thenReturn(entityB.id)
        lenient().`when`(utilityService.getReflection(MockitoHelper.safeEq(entityC), anyString())).thenReturn(entityC.id)
        lenient().doReturn(false).`when`(spy).isSoftDeleted(MockitoHelper.safeEq(entityA))
        lenient().doReturn(true).`when`(spy).isSoftDeleted(MockitoHelper.safeEq(entityB)) // Marked as soft-deleted
        lenient().doReturn(false).`when`(spy).isSoftDeleted(MockitoHelper.safeEq(entityC))

        val result = spy.getAllIds(false)

        assertEquals(2, result.count())
    }
    // endregion

    // region update
    @Test
    fun testUpdate_NoEntity_ThrowNotFoundException()
    {
        val entity = entity.copy()

        val spy = spy(service)
        lenient().`when`(utilityService.getReflection(MockitoHelper.anyObject(), anyString())).thenReturn(entity.id)
        lenient().doThrow(NotFoundException("mocked")).`when`(spy).get(anyString(), anyBoolean())

        try
        {
            spy.update(entity, entity.id)
            fail() // Fail here
        }
        catch(e: NotFoundException)
        {
            assertTrue(true)
        }
    }

    @Test
    fun testUpdate_FailSave_ThrowDatabaseErrorException()
    {
        val entity = entity.copy()

        val spy = spy(service)
        lenient().`when`(utilityService.getReflection(MockitoHelper.anyObject(), anyString())).thenReturn(entity.id)
        lenient().doReturn(entity).`when`(spy).get(anyString(), anyBoolean())
        lenient().doReturn(null).`when`(spy).updateProperty(MockitoHelper.anyObject(), anyString(), anyString()) // Return value not important
        lenient().`when`(repository.save(anyString(), MockitoHelper.anyObject(), anyBoolean())).thenReturn(null)

        try
        {
            spy.update(entity, entity.id)
            fail() // Fail here
        }
        catch(e: DatabaseErrorException)
        {
            assertTrue(true)
        }
    }

    @Test
    fun testUpdate_Normal_Return()
    {
        val entity = entity.copy()

        val spy = spy(service)
        lenient().`when`(utilityService.getReflection(MockitoHelper.anyObject(), anyString())).thenReturn(entity.id)
        lenient().doReturn(entity).`when`(spy).get(anyString(), anyBoolean())
        lenient().doReturn(null).`when`(spy).updateProperty(MockitoHelper.anyObject(), anyString(), anyString()) // Return value not important
        lenient().`when`(repository.save(anyString(), MockitoHelper.anyObject(), anyBoolean())).thenReturn(entity)

        val result = spy.update(entity, entity.id)

        assertEquals(result.id, entity.id)
    }
    // endregion

    // region patch
    @Test
    fun testPatch_NoEntity_ThrowNotFoundException()
    {
        val spy = spy(service)
        lenient().doThrow(NotFoundException("mocked")).`when`(spy).get(anyString(), anyBoolean())

        try
        {
            spy.patch("not-found-id", "{'some': 'json'}")
            fail() // Fail here
        }
        catch(e: NotFoundException)
        {
            assertTrue(true)
        }
    }

    @Test
    fun testPatch_BadJson_ThrowArgumentException()
    {
        val copy = entity.copy()

        val spy = spy(service)
        lenient().doReturn(copy).`when`(spy).get(anyString(), anyBoolean())
        lenient().`when`(utilityService.convertJsonToJsonPatch(anyString(), MockitoHelper.anyObject())).thenThrow(ArgumentException("mocked"))

        try
        {
            spy.patch(copy.id, "invalid json")
            fail() // Fail here
        }
        catch(e: ArgumentException)
        {
            assertTrue(true)
        }
    }

    @Test
    fun testPatch_MergeFail_ThrowArgumentException()
    {
        val copy = entity.copy()

        val spy = spy(service)
        lenient().doReturn(copy).`when`(spy).get(anyString(), anyBoolean())
        lenient().`when`(utilityService.convertJsonToJsonPatch(anyString(), MockitoHelper.anyObject())).thenReturn(null) // Return value not important
        lenient().`when`(utilityService.mergePatch(MockitoHelper.anyObject<AUser>(), MockitoHelper.anyObject(), MockitoHelper.anyObject()))
            .thenThrow(JsonPatchException("mocked"))

        try
        {
            spy.patch(copy.id, "{'some': 'json'}")
            fail() // Fail here
        }
        catch(e: ArgumentException)
        {
            assertTrue(true)
        }
    }

    @Test
    fun testPatch_Normal_Return()
    {
        val copy = entity.copy()

        service.tClass = AUser::class.java
        val spy = spy(service)
        lenient().doReturn(copy).`when`(spy).get(anyString(), anyBoolean())
        lenient().`when`(utilityService.convertJsonToJsonPatch(anyString(), MockitoHelper.anyObject())).thenReturn(null) // Return value not important
        lenient().`when`(utilityService.mergePatch(MockitoHelper.anyObject<AUser>(), MockitoHelper.anyObject(), MockitoHelper.anyObject())).thenReturn(copy)

        val res = spy.patch(copy.id, "{'some': 'json'}")

        assertNotNull(res)
        assertEquals(copy, res)
    }
    // endregion

    // region delete
    @Test
    fun testDelete_NotFound_ThrowNotFoundException()
    {
        val entity = entity.copy()

        val spy = spy(service)
        lenient().doThrow(NotFoundException("mocked")).`when`(spy).get(anyString(), anyBoolean())

        try
        {
            spy.delete(entity.id, entity.id)
            fail() // Fail here
        }
        catch(e: NotFoundException)
        {
            assertTrue(true)
        }
    }

    @Test
    fun testDelete_AlreadyDeleted_ThrowArgumentException()
    {
        val entity = entity.copy()

        val spy = spy(service)
        lenient().doReturn(entity).`when`(spy).get(anyString(), anyBoolean())
        lenient().doReturn(true).`when`(spy).isSoftDeleted(MockitoHelper.anyObject())

        try
        {
            spy.delete(entity.id, entity.id)
            fail() // Fail here
        }
        catch(e: ArgumentException)
        {
            assertTrue(true)
        }
    }

    @Test
    fun testDelete_FailSave_ThrowDatabaseErrorException()
    {
        val entity = entity.copy()

        val spy = spy(service)
        lenient().doReturn(entity).`when`(spy).get(anyString(), anyBoolean())
        lenient().doReturn(false).`when`(spy).isSoftDeleted(MockitoHelper.anyObject())
        lenient().doReturn(null).`when`(spy).updateProperty(MockitoHelper.anyObject(), anyString(), anyString()) // Return value not important
        lenient().`when`(repository.save(anyString(), MockitoHelper.anyObject(), anyBoolean())).thenReturn(null)

        try
        {
            spy.delete(entity.id, entity.id)
            fail() // Fail here
        }
        catch(e: DatabaseErrorException)
        {
            assertTrue(true)
        }
    }

    @Test
    fun testDelete_Normal_Return()
    {
        val entity = entity.copy()

        val spy = spy(service)
        lenient().doReturn(entity).`when`(spy).get(anyString(), anyBoolean())
        lenient().doReturn(false).`when`(spy).isSoftDeleted(MockitoHelper.anyObject())
        lenient().doReturn(null).`when`(spy).updateProperty(MockitoHelper.anyObject(), anyString(), anyString()) // Return value not important
        lenient().`when`(repository.save(anyString(), MockitoHelper.anyObject(), anyBoolean())).thenReturn(entity)

        val result = spy.delete(entity.id, entity.id)

        assertEquals(result.id, entity.id)
    }
    // endregion

    // region restore
    @Test
    fun testRestore_NotFound_ThrowNotFoundException()
    {
        val entity = entity.copy()

        val spy = spy(service)
        lenient().doThrow(NotFoundException("mocked")).`when`(spy).get(anyString(), anyBoolean())

        try
        {
            spy.restore(entity.id, entity.id)
            fail() // Fail here
        }
        catch(e: NotFoundException)
        {
            assertTrue(true)
        }
    }

    @Test
    fun testRestore_NotDeleted_ThrowArgumentException()
    {
        val entity = entity.copy()

        val spy = spy(service)
        lenient().doReturn(entity).`when`(spy).get(anyString(), anyBoolean())
        lenient().doReturn(false).`when`(spy).isSoftDeleted(MockitoHelper.anyObject())

        try
        {
            spy.restore(entity.id, entity.id)
            fail() // Fail here
        }
        catch(e: ArgumentException)
        {
            assertTrue(true)
        }
    }

    @Test
    fun testRestore_FailSave_ThrowDatabaseErrorException()
    {
        val entity = entity.copy()

        val spy = spy(service)
        lenient().doReturn(entity).`when`(spy).get(anyString(), anyBoolean())
        lenient().doReturn(true).`when`(spy).isSoftDeleted(MockitoHelper.anyObject())
        lenient().doReturn(null).`when`(spy).updateProperty(MockitoHelper.anyObject(), anyString(), anyString()) // Return value not important
        lenient().`when`(repository.save(anyString(), MockitoHelper.anyObject(), anyBoolean())).thenReturn(null)

        try
        {
            spy.restore(entity.id, entity.id)
            fail() // Fail here
        }
        catch(e: DatabaseErrorException)
        {
            assertTrue(true)
        }
    }

    @Test
    fun testRestore_Normal_Return()
    {
        val entity = entity.copy()

        val spy = spy(service)
        lenient().doReturn(entity).`when`(spy).get(anyString(), anyBoolean())
        lenient().doReturn(true).`when`(spy).isSoftDeleted(MockitoHelper.anyObject())
        lenient().doReturn(null).`when`(spy).updateProperty(MockitoHelper.anyObject(), anyString(), anyString()) // Return value not important
        lenient().`when`(repository.save(anyString(), MockitoHelper.anyObject(), anyBoolean())).thenReturn(entity)

        val result = spy.restore(entity.id, entity.id)

        assertEquals(result.id, entity.id)
    }
    // endregion

    // region remove
    @Test
    fun testRemove_NotFound_ThrowNotFoundException()
    {
        val entity = entity.copy()

        val spy = spy(service)
        lenient().doThrow(NotFoundException("mocked")).`when`(spy).get(anyString(), anyBoolean())

        try
        {
            spy.remove(entity.id, entity.id, true)
            fail() // Fail here
        }
        catch(e: NotFoundException)
        {
            assertTrue(true)
        }
    }

    @Test
    fun testRemove_SaveFail_ThrowDatabaseErrorException()
    {
        val entity = entity.copy()

        val spy = spy(service)
        lenient().doReturn(entity).`when`(spy).get(anyString(), anyBoolean())
        lenient().`when`(repository.deleteById(anyString())).thenReturn(null)

        try
        {
            spy.remove(entity.id, entity.id, true)
            fail() // Fail here
        }
        catch(e: DatabaseErrorException)
        {
            assertTrue(true)
        }
    }

    @Test
    fun testRemove_Normal_Return()
    {
        val entity = entity.copy()

        val spy = spy(service)
        lenient().doReturn(entity).`when`(spy).get(anyString(), anyBoolean())
        lenient().`when`(repository.deleteById(anyString())).thenReturn("datetime")

        val result = spy.remove(entity.id, entity.id, true)

        assertEquals(result.id, entity.id)
    }
    // endregion

    // region validateEntity
    @Test
    fun testValidateEntity_MissingId_ThrowArgumentException()
    {
        val spy = spy(service)
        lenient().`when`(utilityService.hasField(MockitoHelper.anyObject(), MockitoHelper.safeEq(service.idPropertyName))).thenReturn(false)

        try
        {
            spy.validateEntity(AUser::class.java)
            fail() // Fail here
        }
        catch(e: ArgumentException)
        {
            assertTrue(true)
        }
    }

    @Test
    fun testValidateEntity_MissingAdded_ThrowArgumentException()
    {
        val spy = spy(service)
        lenient().`when`(utilityService.hasField(MockitoHelper.anyObject(), MockitoHelper.safeEq(service.idPropertyName))).thenReturn(true)
        lenient().`when`(utilityService.hasField(MockitoHelper.anyObject(), MockitoHelper.safeEq(service.addedPropertyName))).thenReturn(false)

        try
        {
            spy.validateEntity(AUser::class.java)
            fail() // Fail here
        }
        catch(e: ArgumentException)
        {
            assertTrue(true)
        }
    }

    @Test
    fun testValidateEntity_MissingEdited_ThrowArgumentException()
    {
        val spy = spy(service)
        lenient().`when`(utilityService.hasField(MockitoHelper.anyObject(), MockitoHelper.safeEq(service.idPropertyName))).thenReturn(true)
        lenient().`when`(utilityService.hasField(MockitoHelper.anyObject(), MockitoHelper.safeEq(service.addedPropertyName))).thenReturn(true)

        try
        {
            spy.validateEntity(AUser::class.java)
            fail() // Fail here
        }
        catch(e: ArgumentException)
        {
            assertTrue(true)
        }
    }

    @Test
    fun testValidateEntity_MissingDeleted_ThrowArgumentException()
    {
        val spy = spy(service)
        lenient().`when`(utilityService.hasField(MockitoHelper.anyObject(), MockitoHelper.safeEq(service.idPropertyName))).thenReturn(true)
        lenient().`when`(utilityService.hasField(MockitoHelper.anyObject(), MockitoHelper.safeEq(service.addedPropertyName))).thenReturn(true)
        lenient().`when`(utilityService.hasField(MockitoHelper.anyObject(), MockitoHelper.safeEq(service.deletedPropertyName))).thenReturn(false)

        try
        {
            spy.validateEntity(AUser::class.java)
            fail() // Fail here
        }
        catch(e: ArgumentException)
        {
            assertTrue(true)
        }
    }
    // endregion

    // region updateProperty
    @Test
    fun testUpdateProperty_NoValidation_ReturnNull()
    {
        val entity = entity.copy()
        entity.acceptedTerms = null
        val fieldName = "acceptedTerms"
        val value = "datetime"

        service.validateClass = false

        val result = service.updateProperty(entity, fieldName, value)

        assertNull(result)
    }

    @Test
    fun testUpdateProperty_AddedNullSet_ReturnString()
    {
        val entity = entity.copy()
        entity.acceptedTerms = null
        val fieldName = "acceptedTerms"
        val value = "datetime"

        lenient().doReturn(entity.added).`when`(utilityService).getReflection(MockitoHelper.anyObject(), anyString())
        lenient().doNothing().`when`(utilityService).setReflection(MockitoHelper.anyObject(), anyString(), MockitoHelper.anyObject())

        val result = service.updateProperty(entity, fieldName, value)

        assertNotNull(result)
        assertEquals(result, value)
    }

    @Test
    fun testUpdateProperty_AddedNotNullSet_ReturnString()
    {
        val entity = entity.copy()
        entity.acceptedTerms = null
        val fieldName = "acceptedTerms"
        val value = "datetime"

        lenient().doReturn(entity.added).`when`(utilityService).getReflection(MockitoHelper.anyObject(), anyString())
        lenient().doNothing().`when`(utilityService).setReflection(MockitoHelper.anyObject(), anyString(), MockitoHelper.anyObject())

        val result = service.updateProperty(entity, fieldName, value)

        assertNotNull(result)
        assertEquals(result, value)
    }
    // endregion

    // region isSoftDeleted
    @Test
    fun testIsSoftDeleted_NoValidation_ReturnFalse()
    {
        val entity = entity.copy()
        entity.acceptedTerms = null

        service.validateClass = false

        val result = service.isSoftDeleted(entity)

        assertFalse(result)
    }

    @Test
    fun testIsSoftDeleted_NoSuchFieldDeleted_ThrowNoSuchFieldException()
    {
        val entity = entity.copy()
        entity.acceptedTerms = null
        entity.deleted = null

        lenient().doReturn(entity.added).`when`(utilityService).getReflection(MockitoHelper.anyObject(), MockitoHelper.safeEq(service.addedPropertyName))
        lenient().doThrow(NoSuchFieldException("mocked")).`when`(utilityService).getReflection(MockitoHelper.anyObject(), MockitoHelper.safeEq(service.deletedPropertyName))

        try
        {
            service.isSoftDeleted(entity)
            fail() // Fail here
        }
        catch(e: NoSuchFieldException)
        {
            assertTrue(true)
        }
    }

    @Test
    fun testIsSoftDeleted_NullDeleted_Return()
    {
        val entity = entity.copy()
        entity.acceptedTerms = null
        entity.deleted = null

        lenient().doReturn(entity.added).`when`(utilityService).getReflection(MockitoHelper.anyObject(), MockitoHelper.safeEq(service.addedPropertyName))
        lenient().doReturn(entity.deleted).`when`(utilityService).getReflection(MockitoHelper.anyObject(), MockitoHelper.safeEq(service.deletedPropertyName))

        val result = service.isSoftDeleted(entity)

        assertFalse(result)
    }

    @Test
    fun testIsSoftDeleted_Deleted_Return()
    {
        val entity = entity.copy()
        entity.added = "datetime1"
        entity.deleted = "datetime2"

        lenient().doReturn(entity.added).`when`(utilityService).getReflection(MockitoHelper.anyObject(), MockitoHelper.safeEq(service.addedPropertyName))
        lenient().doReturn(entity.deleted).`when`(utilityService).getReflection(MockitoHelper.anyObject(), MockitoHelper.safeEq(service.deletedPropertyName))

        val result = service.isSoftDeleted(entity)

        assertTrue(result)
    }

    @Test
    fun testIsSoftDeleted_NotDeleted_Return()
    {
        val entity = entity.copy()
        entity.added = "datetime"
        entity.deleted = null

        lenient().doReturn(entity.added).`when`(utilityService).getReflection(MockitoHelper.anyObject(), MockitoHelper.safeEq(service.addedPropertyName))
        lenient().doReturn(entity.deleted).`when`(utilityService).getReflection(MockitoHelper.anyObject(), MockitoHelper.safeEq(service.deletedPropertyName))

        val result = service.isSoftDeleted(entity)

        assertFalse(result)
    }

    @Test
    fun testIsSoftDeleted_Restored_Return()
    {
        val entity = entity.copy()
        entity.added ="datetime"
        entity.deleted = null

        lenient().doReturn(entity.added).`when`(utilityService).getReflection(MockitoHelper.anyObject(), MockitoHelper.safeEq(service.addedPropertyName))
        lenient().doReturn(entity.deleted).`when`(utilityService).getReflection(MockitoHelper.anyObject(), MockitoHelper.safeEq(service.deletedPropertyName))

        val result = service.isSoftDeleted(entity)

        assertFalse(result)
    }
    // endregion
}
