package grd.kotlin.authapi.services

import grd.kotlin.authapi.MockitoHelper
import grd.kotlin.authapi.dto.AUserDto
import grd.kotlin.authapi.dto.Converter
import grd.kotlin.authapi.enums.UserRole
import grd.kotlin.authapi.exceptions.*
import grd.kotlin.authapi.jwt.JwtUtil
import grd.kotlin.authapi.models.AUser
import grd.kotlin.authapi.repositories.FirebaseRepository
import grd.kotlin.authapi.settings.Settings
import grd.kotlin.authapi.testdata.TestEntities
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
import java.time.Duration
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.*

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ExtendWith(SpringExtension::class, MockitoExtension::class)
class AUserServiceUnitTests
{
    @InjectMocks
    private lateinit var userService: UserService

    @Mock
    private lateinit var utilityService: UtilityService // Note: For BaseService

    @Mock
    private lateinit var logService: LogService // Note: For BaseService

    @Mock
    private lateinit var userRepository: FirebaseRepository<AUser>

    @Mock
    private lateinit var settings: Settings

    @Mock
    private lateinit var jwtUtil: JwtUtil

    @Autowired
    private lateinit var injectedSettings: Settings

    private lateinit var authorizationFieldName: String
    private final val testEntities = TestEntities()
    val user = testEntities.userOther
    val clearTextPassword = testEntities.clearTextPassword

    @BeforeEach
    fun setup()
    {
        utilityService = mock(UtilityService::class.java)
        logService = mock(LogService::class.java)
        @Suppress("UNCHECKED_CAST")
        userRepository = mock(FirebaseRepository::class.java) as FirebaseRepository<AUser>
        settings = mock(Settings::class.java)
        jwtUtil = mock(JwtUtil::class.java)
        MockitoAnnotations.openMocks(this)

        authorizationFieldName = injectedSettings.server.authenticationHeader!!
        userService.disableLogs = true
    }

    // region passwordEncoder
    @Test
    fun testPasswordEncoder_Get_ReturnPasswordEncoder() = runBlocking {
        val result = userService.passwordEncoder()

        assertNotNull(result)
    }

    @Test
    fun testPasswordEncoder_Encode_ReturnPasswordEncoder() = runBlocking {
        val password = "password"

        val result = userService.passwordEncoder()

        assertNotNull(result)

        val encoded = result.encode(password)
        val matches = result.matches(password, encoded)

        assertTrue(matches)
    }
    // endregion

    // region authenticate
    @Test
    fun testAuthenticate_ComparePasswords_ReturnFalse() = runBlocking {
        val entity = user.copy()

        val result = userService.authenticate(entity, "wrong-password")

        assertNotNull(result)
        assertFalse(result)
    }

    @Test
    fun testAuthenticate_CompareEncodedPasswords_ReturnFalse() = runBlocking {
        val entity = user.copy()

        val result = userService.authenticate(entity, entity.password)

        assertNotNull(result)
        assertFalse(result)
    }

    @Test
    fun testAuthenticate_CompareUpperCaseFirstPasswords_ReturnFalse() = runBlocking {
        val entity = user.copy()
        val firstLetter = clearTextPassword.substring(0, 1).uppercase(Locale.getDefault())
        val restLetters = clearTextPassword.substring(1)
        val upperCasedPassword = firstLetter + restLetters
        println("Correct password:  $clearTextPassword")
        println("Modified password: $upperCasedPassword")

        val result = userService.authenticate(entity, upperCasedPassword)

        assertNotNull(result)
        assertFalse(result)
    }

    @Test
    fun testAuthenticate_ComparePasswords_ReturnTrue() = runBlocking {
        val entity = user.copy()

        val result = userService.authenticate(entity, clearTextPassword)

        assertNotNull(result)
        assertTrue(result)
    }
    // endregion

    // region authenticateGenerateToken
    @Test
    fun testAuthenticateGenerateToken_NoUser_ThrowNotFoundException() = runBlocking {
        val entity = user.copy()

        val spy = spy(userService)
        lenient().doThrow(NotFoundException("mocked")).`when`(spy).findByUsernameEmail(anyString(), anyString())

        try
        {
            spy.authenticateGenerateToken(entity.username, entity.email, clearTextPassword)
            fail() // Fail here
        }
        catch(e: NotFoundException)
        {
            assertTrue(true)
        }
    }

    @Test
    fun testAuthenticateGenerateToken_UserLocked_ThrowNotAuthorizedException() = runBlocking {
        val entity = user.copy()

        val spy = spy(userService)
        lenient().doReturn(entity).`when`(spy).findByUsernameEmail(anyString(), anyString())
        lenient().doReturn(Pair(true, "mocked")).`when`(spy).isUserLocked(MockitoHelper.anyObject(), anyBoolean())

        try
        {
            spy.authenticateGenerateToken(entity.username, entity.email, clearTextPassword)
            fail() // Fail here
        }
        catch(e: NotAuthorizedException)
        {
            assertTrue(true)
        }
    }

    @Test
    fun testAuthenticateGenerateToken_NotAuthenticated_ThrowNotAuthorizedException() = runBlocking {
        val entity = user.copy()

        val spy = spy(userService)
        lenient().doReturn(entity).`when`(spy).findByUsernameEmail(anyString(), anyString())
        lenient().doReturn(false).`when`(spy).authenticate(MockitoHelper.anyObject(), anyString())

        try
        {
            spy.authenticateGenerateToken(entity.username, entity.email, clearTextPassword)
            fail() // Fail here
        }
        catch(e: NotAuthorizedException)
        {
            assertTrue(true)
        }
    }

    @Test
    fun testAuthenticateGenerateToken_Normal_ReturnString() = runBlocking {
        val entity = user.copy()

        val spy = spy(userService)
        lenient().doReturn(entity).`when`(spy).findByUsernameEmail(anyString(), anyString())
        lenient().doReturn(true).`when`(spy).authenticate(MockitoHelper.anyObject(), anyString())
        lenient().`when`(jwtUtil.generateToken(entity)).thenReturn("token.mock.")

        val result = spy.authenticateGenerateToken(entity.username, entity.email, clearTextPassword)

        assertNotNull(result)
    }
    // endregion

    // Will not test
    // region checkToken
    // endregion

    // region findByUsernameEmail
    @Test
    fun testFindByUsernameEmail_NullUsernameAndEmail_ThrowArgumentException() = runBlocking {
        try
        {
            userService.findByUsernameEmail(null, null)
            fail() // Fail here
        }
        catch(e: ArgumentException)
        {
            assertTrue(true)
        }
    }

    @Test
    fun testFindByUsernameEmail_ReturnMultiple_ThrowUnexpectedStateException() = runBlocking {
        val entity = user.copy()
        val entityList = listOf(entity, entity)

        val spy = spy(userService)
        lenient().doReturn(entityList).`when`(spy).getQueried<Any, Any>(MockitoHelper.anyObject())

        try
        {
            spy.findByUsernameEmail(entity.username, null)
            fail() // Fail here
        }
        catch(e: UnexpectedStateException)
        {
            assertTrue(true)
        }
    }

    @Test
    fun testFindByUsernameEmail_ReturnNone_ThrowNotFoundException() = runBlocking {
        val entity = user.copy()
        val entityList = listOf<AUser>()

        val spy = spy(userService)
        lenient().doReturn(entityList).`when`(spy).getQueried<Any, Any>(MockitoHelper.anyObject())

        try
        {
            spy.findByUsernameEmail(entity.username, null)
            fail() // Fail here
        }
        catch(e: NotFoundException)
        {
            assertTrue(true)
        }
    }

    @Test
    fun testFindByUsernameEmail_FindUsernameNormal_ReturnUser() = runBlocking {
        val entity = user.copy()
        val entityList = listOf(entity)

        val spy = spy(userService)
        lenient().doReturn(entityList).`when`(spy).getQueried<Any, Any>(MockitoHelper.anyObject())

        val result = spy.findByUsernameEmail(user.username, null)

        assertNotNull(result)
        assertEquals(entity.id, result.id)
        assertEquals(entity.username, result.username)
    }

    @Test
    fun testFindByUsernameEmail_FindEmailNormal_ReturnUser() = runBlocking {
        val entity = user.copy()
        val entityList = listOf(entity)

        val spy = spy(userService)
        lenient().doReturn(entityList).`when`(spy).getQueried<Any, Any>(MockitoHelper.anyObject())

        val result = spy.findByUsernameEmail(null, user.email)

        assertNotNull(result)
        assertEquals(entity.id, result.id)
        assertEquals(entity.username, result.username)
    }

    @Test
    fun testFindByUsernameEmail_FindUsernameAndEmailNormal_ReturnUser() = runBlocking {
        val entity = user.copy()
        val entityList = listOf(entity)

        val spy = spy(userService)
        lenient().doReturn(entityList).`when`(spy).getQueried<Any, Any>(MockitoHelper.anyObject())

        val result = spy.findByUsernameEmail(user.username, user.email)

        assertNotNull(result)
        assertEquals(entity.id, result.id)
        assertEquals(entity.username, result.username)
    }
    // endregion

    // region getUserFromToken
    @Test
    fun testGetUserFromToken_NoSuchUser_ThrowNotFoundException() = runBlocking {
        val entity = user.copy()

        val spy = spy(userService)
        `when`(jwtUtil.getIdFromToken(anyString())).thenReturn(entity.id)
        doThrow(NotFoundException("mocked")).`when`(spy).get(anyString(), anyBoolean())

        try
        {
            spy.getUserFromToken("some-token")
            fail() // Fail here
        }
        catch(e: NotFoundException)
        {
            assertTrue(true)
        }
    }

    @Test
    fun testGetUserFromToken_Normal_ReturnString() = runBlocking {
        val entity = user.copy()

        val spy = spy(userService)
        `when`(jwtUtil.getIdFromToken(anyString())).thenReturn(entity.id)
        doReturn(entity).`when`(spy).get(anyString(), anyBoolean())

        val result = spy.getUserFromToken("some-token")

        assertNotNull(result)
        assertEquals(entity.id, result.id)
    }
    // endregion

    // region getUserFromHeaders
    @Test
    fun testGetUserFromHeaders_NoHeaders_ThrowArgumentException() = runBlocking {
        val headers = mapOf<String, String>()

        userService.settings = injectedSettings

        try
        {
            userService.getUserFromHeaders(headers)
            fail() // Fail here
        }
        catch(e: ArgumentException)
        {
            assertTrue(true)
        }
    }

    @Test
    fun testGetUserFromHeaders_MalformedToken_ThrowArgumentException() = runBlocking {
        val headers = mapOf(Pair(authorizationFieldName, "token-without-bearer"))

        userService.settings = injectedSettings

        try
        {
            userService.getUserFromHeaders(headers)
            fail() // Fail here
        }
        catch(e: ArgumentException)
        {
            assertTrue(true)
        }
    }

    @Test
    fun testGetUserFromHeaders_Normal_ReturnUser() = runBlocking {
        val entity = user.copy()
        val tokenPrefix = "Bearer "
        val headers = mapOf(Pair(authorizationFieldName, "$tokenPrefix some-token"))

        userService.settings = injectedSettings

        val spy = spy(userService)
        doReturn(entity).`when`(spy).getUserFromToken(anyString())

        val result = spy.getUserFromHeaders(headers)

        assertNotNull(result)
        assertEquals(entity, result)
    }
    // endregion

    // TODO
    // region isUserLocked
    @Test
    fun testIsUserLocked_ExpiredInvalidDateTime_ReturnTrue() = runBlocking {
        val expiresDateTimeString = "2021-01-corrupted-data"
        val entity = user.copy(expirationTime = expiresDateTimeString, lockedUntilTime = null)
        val nowDateTimeString = "2021-03-30T10:00:00.000Z"
        val nowDateTime = Instant.parse(nowDateTimeString)

        lenient().`when`(utilityService.stringToDatetime(nowDateTimeString)).thenReturn(nowDateTime)
        lenient().`when`(utilityService.stringToDatetime(expiresDateTimeString)).thenReturn(null)

        val result = userService.isUserLocked(entity, false)

        assertNotNull(result)
        assertTrue(result.first)
        assertNotNull(result.second)
        assertTrue(result.second.contains("expiration time of was malformed"))
        println("Full isUserLocked message: ${result.second}")
    }

    @Test
    fun testIsUserLocked_Expired_ReturnTrue() = runBlocking {
        val expiresDateTimeString = "2021-01-30T10:00:00.000Z"
        val expiresDateTime = Instant.parse(expiresDateTimeString)
        val entity = user.copy(expirationTime = expiresDateTimeString, lockedUntilTime = null)
        val nowDateTimeString = "2021-03-30T10:00:00.000Z"
        val nowDateTime = Instant.parse(nowDateTimeString)

        lenient().`when`(utilityService.stringToDatetime(nowDateTimeString)).thenReturn(nowDateTime)
        lenient().`when`(utilityService.stringToDatetime(expiresDateTimeString)).thenReturn(expiresDateTime)

        val result = userService.isUserLocked(entity, false)

        assertNotNull(result)
        assertTrue(result.first)
        assertNotNull(result.second)
        assertTrue(result.second.contains("This user expired"))
        assertTrue(result.second.contains(expiresDateTimeString))
        println("Full isUserLocked message: ${result.second}")
    }

    @Test
    fun testIsUserLocked_HasExpireDateNotExpired_ReturnFalse() = runBlocking {
        val expiresDateTimeString = "9999-01-30T10:00:00.000Z"
        val expiresDateTime = Instant.parse(expiresDateTimeString)
        val entity = user.copy(expirationTime = expiresDateTimeString, lockedUntilTime = null)
        val nowDateTimeString = "2021-03-30T10:00:00.000Z"
        val nowDateTime = Instant.parse(nowDateTimeString)

        lenient().`when`(utilityService.stringToDatetime(nowDateTimeString)).thenReturn(nowDateTime)
        lenient().`when`(utilityService.stringToDatetime(expiresDateTimeString)).thenReturn(expiresDateTime)

        val result = userService.isUserLocked(entity, false)

        assertNotNull(result)
        assertFalse(result.first)
        assertNotNull(result.second)
        assertTrue(result.second.contains("This user expires"))
        assertTrue(result.second.contains(expiresDateTimeString))
        println("Full isUserLocked message: ${result.second}")
    }

    @Test
    fun testIsUserLocked_LockedInvalidDateTime_ReturnTrue() = runBlocking {
        val lockedUntilDateTimeString = "9999-03-corrupted-data"
        val entity = user.copy(expirationTime = null, lockedUntilTime = lockedUntilDateTimeString)
        val nowDateTimeString = "2021-03-30T10:00:00.000Z"
        val nowDateTime = Instant.parse(nowDateTimeString)

        lenient().`when`(utilityService.stringToDatetime(nowDateTimeString)).thenReturn(nowDateTime)
        lenient().`when`(utilityService.stringToDatetime(lockedUntilDateTimeString)).thenReturn(null)

        val result = userService.isUserLocked(entity, false)

        assertNotNull(result)
        assertTrue(result.first)
        assertNotNull(result.second)
        assertTrue(result.second.contains("locked until date of was malformed"))
        println("Full isUserLocked message: ${result.second}")
    }

    @Test
    fun testIsUserLocked_Locked_ReturnTrue() = runBlocking {
        val lockedUntilDateTimeString = "9999-03-30T10:00:00.000Z"
        val lockedUntilDateTime = Instant.parse(lockedUntilDateTimeString)
        val entity = user.copy(expirationTime = null, lockedUntilTime = lockedUntilDateTimeString)
        val nowDateTimeString = "2021-03-30T10:00:00.000Z"
        val nowDateTime = Instant.parse(nowDateTimeString)

        lenient().`when`(utilityService.stringToDatetime(nowDateTimeString)).thenReturn(nowDateTime)
        lenient().`when`(utilityService.stringToDatetime(lockedUntilDateTimeString)).thenReturn(lockedUntilDateTime)

        val result = userService.isUserLocked(entity, false)

        assertNotNull(result)
        assertTrue(result.first)
        assertNotNull(result.second)
        assertTrue(result.second.contains("This user is locked until"))
        assertTrue(result.second.contains(lockedUntilDateTimeString))
        println("Full isUserLocked message: ${result.second}")
    }

    @Test
    fun testIsUserLocked_DoUnlockNotUnlockable_ReturnTrue() = runBlocking {
        val lockedUntilDateTimeString = "9999-03-30T10:00:00.000Z"
        val lockedUntilDateTime = Instant.parse(lockedUntilDateTimeString)
        val entity = user.copy(expirationTime = null, lockedUntilTime = lockedUntilDateTimeString)
        val nowDateTimeString = "2021-03-30T10:00:00.000Z"
        val nowDateTime = Instant.parse(nowDateTimeString)

        val spy = spy(userService)
        lenient().`when`(utilityService.stringToDatetime(nowDateTimeString)).thenReturn(nowDateTime)
        lenient().`when`(utilityService.stringToDatetime(lockedUntilDateTimeString)).thenReturn(lockedUntilDateTime)
        lenient().doReturn(null).`when`(spy).update(MockitoHelper.anyObject(), anyString(), anyBoolean(), anyBoolean()) // Return value not important

        val result = spy.isUserLocked(entity, true)

        assertNotNull(result)
        assertTrue(result.first)
        assertNotNull(result.second)
        assertTrue(result.second.contains("This user is locked until"))
        assertTrue(result.second.contains(lockedUntilDateTimeString))
        println("Full isUserLocked message: ${result.second}")
    }

    @Test
    fun testIsUserLocked_LockedDoUnlock_ReturnFalse() = runBlocking {
        val lockedUntilDateTimeString = "2020-03-30T10:00:00.000Z"
        val lockedUntilDateTime = Instant.parse(lockedUntilDateTimeString)
        val entity = user.copy(expirationTime = null, lockedUntilTime = lockedUntilDateTimeString)
        val nowDateTimeString = "2021-03-30T10:00:00.000Z"
        val nowDateTime = Instant.parse(nowDateTimeString)

        val spy = spy(userService)
        lenient().`when`(utilityService.stringToDatetime(nowDateTimeString)).thenReturn(nowDateTime)
        lenient().`when`(utilityService.stringToDatetime(lockedUntilDateTimeString)).thenReturn(lockedUntilDateTime)
        lenient().doReturn(null).`when`(spy).update(MockitoHelper.anyObject(), anyString(), anyBoolean(), anyBoolean()) // Return value not important

        val result = spy.isUserLocked(entity, true)

        assertNotNull(result)
        assertFalse(result.first)
        assertNotNull(result.second)
        assertTrue(result.second.contains("The user was unlocked"))
        assertTrue(result.second.contains(lockedUntilDateTimeString))
        println("Full isUserLocked message: ${result.second}")
    }

    @Test
    fun testIsUserLocked_NoExpireNoLock_ReturnFalse() = runBlocking {
        val entity = user.copy(expirationTime = null, lockedUntilTime = null)
        val nowDateTimeString = "2021-03-30T10:00:00.000Z"
        val nowDateTime = Instant.parse(nowDateTimeString)

        lenient().`when`(utilityService.stringToDatetime(nowDateTimeString)).thenReturn(nowDateTime)

        val result = userService.isUserLocked(entity, false)

        assertNotNull(result)
        assertFalse(result.first)
        assertNotNull(result.second)
        assertTrue(result.second.contains("not set to expire and is not locked"))
        println("Full isUserLocked message: ${result.second}")
    }
    // endregion

    // region actionAllowed
    @Test
    fun testActionAllowed_Deleted_ThrowNotAuthorizedException() = runBlocking {
        val entity = user.copy(role = UserRole.OWNER, deleted = Instant.now().plus(-1L, ChronoUnit.DAYS).toString(), acceptedTerms = Instant.now().toString())
        val operationRole = UserRole.DEVELOPER

        try
        {
            userService.actionAllowed(operationRole, entity)
            fail() // Fail here
        }
        catch(e: NotAuthorizedException)
        {
            assertTrue(true)
        }
    }

    @Test
    fun testActionAllowed_Expired_ThrowNotAuthorizedException() = runBlocking {
        val datetime = Instant.now().plus(-1L, ChronoUnit.DAYS)
        val entity = user.copy(role = UserRole.OWNER, expirationTime = datetime.toString(), acceptedTerms = Instant.now().toString())
        val operationRole = UserRole.DEVELOPER

        `when`(utilityService.stringToDatetime(anyString())).thenReturn(datetime)

        try
        {
            userService.actionAllowed(operationRole, entity)
            fail() // Fail here
        }
        catch(e: NotAuthorizedException)
        {
            assertTrue(true)
        }
    }

    @Test
    fun testActionAllowed_Locked_ThrowNotAuthorizedException() = runBlocking {
        val datetime = Instant.now().plus(1L, ChronoUnit.DAYS)
        val entity = user.copy(role = UserRole.OWNER, lockedUntilTime = datetime.toString(), acceptedTerms = Instant.now().toString())
        val operationRole = UserRole.DEVELOPER

        `when`(utilityService.stringToDatetime(anyString())).thenReturn(datetime)

        try
        {
            userService.actionAllowed(operationRole, entity)
            fail() // Fail here
        }
        catch(e: NotAuthorizedException)
        {
            assertTrue(true)
        }
    }

    @Test
    fun testActionAllowed_TermsAndConditions_ThrowNotAuthorizedException() = runBlocking {
        val entity = user.copy(role = UserRole.OWNER, acceptedTerms = null)
        val operationRole = UserRole.DEVELOPER

        try
        {
            userService.actionAllowed(operationRole, entity)
            fail() // Fail here
        }
        catch(e: NotAuthorizedException)
        {
            assertTrue(true)
        }
    }

    @Test
    fun testActionAllowed_NotAuthorizedRoleLower_ThrowNotAuthorizedException() = runBlocking {
        val entity = user.copy(role = UserRole.USER, acceptedTerms = Instant.now().toString())
        val operationRole = UserRole.DEVELOPER

        try
        {
            userService.actionAllowed(operationRole, entity)
            fail() // Fail here
        }
        catch(e: NotAuthorizedException)
        {
            assertTrue(true)
        }
    }

    @Test
    fun testActionAllowed_AuthorizedRoleSame_ReturnTrue() = runBlocking {
        val entity = user.copy(role = UserRole.MODERATOR, acceptedTerms = Instant.now().toString())
        val operationRole = UserRole.MODERATOR

        val result = userService.actionAllowed(operationRole, entity)

        assertNotNull(result)
        assertTrue(result)
    }

    @Test
    fun testActionAllowed_AuthorizedRoleHigher_ReturnTrue() = runBlocking {
        val entity = user.copy(role = UserRole.ADMINISTRATOR, acceptedTerms = Instant.now().toString())
        val operationRole = UserRole.USER

        val result = userService.actionAllowed(operationRole, entity)

        assertNotNull(result)
        assertTrue(result)
    }
    // endregion

    // region actionAllowedUser
    @Test
    fun testActionAllowedUser_AllowSelfEditOtherLowerRole_ThrowNotAuthorizedException() = runBlocking {
        val entity = user.copy(id = "entity", role = UserRole.USER)
        val subject = user.copy(id = "subject", role = UserRole.USER)
        val operationRole = UserRole.DEVELOPER

        val spy = spy(userService)
        doThrow(NotAuthorizedException("mocked")).`when`(spy).actionAllowed(MockitoHelper.anyObject(), MockitoHelper.anyObject())

        try
        {
            spy.actionAllowedUser(operationRole, entity, subject, true)
            fail() // Fail here
        }
        catch(e: NotAuthorizedException)
        {
            assertTrue(true)
        }
    }

    @Test
    fun testActionAllowedUser_AllowSelfEditSelfLowerRole_ReturnTrue() = runBlocking {
        val entity = user.copy(id = "entity", role = UserRole.USER)
        val subject = entity
        val operationRole = UserRole.DEVELOPER

        val result = userService.actionAllowedUser(operationRole, entity, subject, true)

        assertNotNull(result)
        assertTrue(result)
    }

    @Test
    fun testActionAllowedUser_DisallowSelfEditSelfLowerRole_ThrowNotAuthorizedException() = runBlocking {
        val entity = user.copy(id = "entity", role = UserRole.USER)
        val subject = entity
        val operationRole = UserRole.DEVELOPER

        val spy = spy(userService)
        doThrow(NotAuthorizedException("mocked")).`when`(spy).actionAllowed(MockitoHelper.anyObject(), MockitoHelper.anyObject())

        try
        {
            spy.actionAllowedUser(operationRole, entity, subject, false)
            fail() // Fail here
        }
        catch(e: NotAuthorizedException)
        {
            assertTrue(true)
        }
    }

    @Test
    fun testActionAllowedUser_DisallowSelfEditOtherHigherRole_ThrowNotAuthorizedException() = runBlocking {
        val entity = user.copy(id = "entity", role = UserRole.ADMINISTRATOR, acceptedTerms = Instant.now().toString())
        val subject = user.copy(id = "subject", role = UserRole.USER, acceptedTerms = Instant.now().toString())
        val operationRole = UserRole.MODERATOR

        val spy = spy(userService)
        lenient().doReturn(true).`when`(spy).actionAllowed(MockitoHelper.anyObject(), MockitoHelper.anyObject())

        val result = userService.actionAllowedUser(operationRole, entity, subject, false)

        assertNotNull(result)
        assertTrue(result)
    }
    // endregion

    // region registerNew
    @Test
    fun testRegisterNew_InvalidUsername_ThrowArgumentException() = runBlocking {
        val entity = user.copy(username = "some invalid username")
        val dto = Converter.Companion.convert(entity, AUserDto::class.java)
        val password = clearTextPassword

        `when`(utilityService.validateInput(
            anyString(),
            anyBoolean(),
            anyBoolean(),
            anyBoolean(),
            anyBoolean(),
            anyInt(),
            anyInt())).thenReturn(Pair(false, "mocked"))

        try
        {
            userService.registerNew(dto, password, entity.id)
            fail() // Fail here
        }
        catch(e: ArgumentException)
        {
            assertTrue(true)
        }
    }

    @Test
    fun testRegisterNew_InvalidEmail_ThrowArgumentException() = runBlocking {
        val entity = user.copy(email = "some invalid email")
        val dto = Converter.Companion.convert(entity, AUserDto::class.java)
        val password = clearTextPassword

        `when`(utilityService.validateInput(
            anyString(),
            anyBoolean(),
            anyBoolean(),
            anyBoolean(),
            anyBoolean(),
            anyInt(),
            anyInt())).thenReturn(Pair(true, "mocked"))
        `when`(utilityService.validateEmail(anyString())).thenReturn(Pair(false, "mocked"))

        try
        {
            userService.registerNew(dto, password, entity.id)
            fail() // Fail here
        }
        catch(e: ArgumentException)
        {
            assertTrue(true)
        }
    }

    @Test
    fun testRegisterNew_DuplicateUsername_ThrowArgumentException() = runBlocking {
        val entity = user.copy()
        val dto = Converter.Companion.convert(entity, AUserDto::class.java)
        val password = clearTextPassword

        val spy = spy(userService)
        lenient().doReturn(null).`when`(spy).findByUsernameEmail(eq(entity.username), eq(null))
        `when`(utilityService.validateInput(
            anyString(),
            anyBoolean(),
            anyBoolean(),
            anyBoolean(),
            anyBoolean(),
            anyInt(),
            anyInt())).thenReturn(Pair(false, "mocked"))

        try
        {
            spy.registerNew(dto, password, entity.id)
            fail() // Fail here
        }
        catch(e: ArgumentException)
        {
            assertTrue(true)
        }
    }

    @Test
    fun testRegisterNew_DuplicateEmail_ThrowArgumentException() = runBlocking {
        val entity = user.copy()
        val dto = Converter.Companion.convert(entity, AUserDto::class.java)
        val password = clearTextPassword

        val spy = spy(userService)
        `when`(utilityService.validateInput(
            anyString(),
            anyBoolean(),
            anyBoolean(),
            anyBoolean(),
            anyBoolean(),
            anyInt(),
            anyInt())).thenReturn(Pair(true, "mocked"))
        `when`(utilityService.validateEmail(anyString())).thenReturn(Pair(true, "mocked"))
        lenient().doReturn(AUser(username = "user", password = "pass")).`when`(spy).findByUsernameEmail(eq(null), eq(entity.email))

        try
        {
            spy.registerNew(dto, password, entity.id)
            fail() // Fail here
        }
        catch(e: ArgumentException)
        {
            assertTrue(true)
        }
    }

    @Test
    fun testRegisterNew_InvalidExpirationTime_ThrowArgumentException() = runBlocking {
        val expirationTime = "2021-03-corrupted-data"
        val entity = user.copy(expirationTime = expirationTime)
        val dto = Converter.Companion.convert(entity, AUserDto::class.java)
        val password = clearTextPassword

        val spy = spy(userService)
        `when`(utilityService.validateInput(
            anyString(),
            anyBoolean(),
            anyBoolean(),
            anyBoolean(),
            anyBoolean(),
            anyInt(),
            anyInt())).thenReturn(Pair(true, "mocked"))
        `when`(utilityService.validateEmail(anyString())).thenReturn(Pair(true, "mocked"))
        lenient().doThrow(NotFoundException("mocked")).`when`(spy).findByUsernameEmail(eq(entity.username), eq(null))
        lenient().doThrow(NotFoundException("mocked")).`when`(spy).findByUsernameEmail(eq(null), eq(entity.email))
        `when`(utilityService.getRandomString()).thenReturn(entity.id)
        `when`(utilityService.stringToDatetime(anyString())).thenReturn(null)

        try
        {
            spy.registerNew(dto, password, entity.id)
            fail() // Fail here
        }
        catch(e: ArgumentException)
        {
            assertTrue(e.message!!.contains("expirationTime"))
            assertTrue(e.message!!.contains(expirationTime))
        }
    }

    @Test
    fun testRegisterNew_InvalidLockedUntilTime_ThrowArgumentException() = runBlocking {
        val lockedUntilTime = "9999-03-corrupted-data"
        val entity = user.copy(lockedUntilTime = lockedUntilTime)
        val dto = Converter.Companion.convert(entity, AUserDto::class.java)
        val password = clearTextPassword

        val spy = spy(userService)
        `when`(utilityService.validateInput(
            anyString(),
            anyBoolean(),
            anyBoolean(),
            anyBoolean(),
            anyBoolean(),
            anyInt(),
            anyInt())).thenReturn(Pair(true, "mocked"))
        `when`(utilityService.validateEmail(anyString())).thenReturn(Pair(true, "mocked"))
        lenient().doThrow(NotFoundException("mocked")).`when`(spy).findByUsernameEmail(eq(entity.username), eq(null))
        lenient().doThrow(NotFoundException("mocked")).`when`(spy).findByUsernameEmail(eq(null), eq(entity.email))
        `when`(utilityService.getRandomString()).thenReturn(entity.id)
        `when`(utilityService.stringToDatetime(anyString())).thenReturn(null)

        try
        {
            spy.registerNew(dto, password, entity.id)
            fail() // Fail here
        }
        catch(e: ArgumentException)
        {
            assertTrue(e.message!!.contains("lockedUntilTime"))
            assertTrue(e.message!!.contains(lockedUntilTime))
        }
    }

    @Test
    fun testRegisterNew_ValidTimes_ReturnUser() = runBlocking {
        val expirationTime = "2021-03-30T10:00:00.000Z"
        val lockedUntilTime = "9999-03-30T10:00:00.000Z"
        val entity = user.copy(expirationTime = expirationTime, lockedUntilTime = lockedUntilTime)
        val dto = Converter.Companion.convert(entity, AUserDto::class.java)
        val password = clearTextPassword
        val mockedDateTime = Instant.parse(entity.expirationTime)

        val spy = spy(userService)
        `when`(utilityService.validateInput(
            anyString(),
            anyBoolean(),
            anyBoolean(),
            anyBoolean(),
            anyBoolean(),
            anyInt(),
            anyInt())).thenReturn(Pair(true, "mocked"))
        `when`(utilityService.validateEmail(anyString())).thenReturn(Pair(true, "mocked"))
        lenient().doThrow(NotFoundException("mocked")).`when`(spy).findByUsernameEmail(eq(entity.username), eq(null))
        lenient().doThrow(NotFoundException("mocked")).`when`(spy).findByUsernameEmail(eq(null), eq(entity.email))
        `when`(utilityService.getRandomString()).thenReturn(entity.id)
        `when`(utilityService.stringToDatetime(anyString())).thenReturn(mockedDateTime)
        lenient().doReturn(entity).`when`(spy).add(MockitoHelper.anyObject(), anyString(), anyBoolean())

        val result = spy.registerNew(dto, password, entity.id)

        assertNotNull(result)
        assertEquals(entity.id, result.id)
        assertEquals(entity.expirationTime, result.expirationTime)
        assertEquals(entity.lockedUntilTime, result.lockedUntilTime)
        assertEquals(entity.username, result.username)
    }

    @Test
    fun testRegisterNew_Normal_ReturnUser() = runBlocking {
        val entity = user.copy()
        val dto = Converter.Companion.convert(entity, AUserDto::class.java)
        val password = clearTextPassword

        val spy = spy(userService)
        `when`(utilityService.validateInput(
            anyString(),
            anyBoolean(),
            anyBoolean(),
            anyBoolean(),
            anyBoolean(),
            anyInt(),
            anyInt())).thenReturn(Pair(true, "mocked"))
        `when`(utilityService.validateEmail(anyString())).thenReturn(Pair(true, "mocked"))
        lenient().doThrow(NotFoundException("mocked")).`when`(spy).findByUsernameEmail(eq(entity.username), eq(null))
        lenient().doThrow(NotFoundException("mocked")).`when`(spy).findByUsernameEmail(eq(null), eq(entity.email))
        `when`(utilityService.getRandomString()).thenReturn(entity.id)
        lenient().doReturn(entity).`when`(spy).add(MockitoHelper.anyObject(), anyString(), anyBoolean())

        val result = spy.registerNew(dto, password, entity.id)

        assertNotNull(result)
        assertEquals(entity.id, result.id)
    }
    // endregion

    // region setUserEmail
    @Test
    fun testSetUserEmail_NotValidEmail_ThrowArgumentException() = runBlocking {
        val entity = user.copy(email = "old_email@test.com")
        val newEmail = "not an email"

        `when`(utilityService.validateEmail(anyString())).thenReturn(Pair(false, "mocked"))

        try
        {
            userService.setUserEmail(entity, newEmail)
            fail() // Fail here
        }
        catch(e: ArgumentException)
        {
            assertTrue(true)
        }
    }

    @Test
    fun testSetUserEmail_Normal_ReturnUser() = runBlocking {
        val entity = user.copy(email = "old_email@test.com")
        val newEmail = "test@test.com"

        `when`(utilityService.validateEmail(anyString())).thenReturn(Pair(true, "mocked"))

        val result = userService.setUserEmail(entity, newEmail)

        assertNotNull(result)
        assertEquals(newEmail, result.email)
    }
    // endregion

    // region setUserLock
    @Test
    fun testSetUserLock_InvalidDateTime_ThrowArgumentException() = runBlocking {
        val entity = user.copy(lockedUntilTime = null)
        val lockTime = "9999-03-30T10:00:00.000Z"

        lenient().`when`(utilityService.stringToDatetime(anyString())).thenReturn(null)

        try
        {
            userService.setUserLock(entity, lockTime)
            fail() // Fail here
        }
        catch(e: ArgumentException)
        {
            assertTrue(true)
        }
    }

    @Test
    fun testSetUserLock_NormalSetNull_ReturnUser() = runBlocking {
        val entity = user.copy(lockedUntilTime = null)
        val lockTime = "2021-03-30T10:00:00.000Z"
        val lockDateTime = Instant.parse(lockTime)
        val expected = user.copy(lockedUntilTime = lockTime)

        lenient().`when`(utilityService.stringToDatetime(anyString())).thenReturn(lockDateTime)

        val result = userService.setUserLock(entity, lockTime)

        assertNotNull(result)
        assertEquals(expected.lockedUntilTime, result.lockedUntilTime)
    }

    @Test
    fun testSetUserLock_NormalNull_ReturnUser() = runBlocking {
        val entity = user.copy(lockedUntilTime = "2021-03-30T10:00:00.000Z")
        val lockTime = null
        val expected = user.copy(lockedUntilTime = lockTime)

        val result = userService.setUserLock(entity, lockTime)

        assertNotNull(result)
        assertEquals(expected.lockedUntilTime, result.lockedUntilTime)
    }
    // endregion

    // region setModeratorComment
    @Test
    fun testSetModeratorComment_AddCommentNormal_ReturnUser() = runBlocking {
        val pastTimeString = "2020-03-30T10:00:00.000Z"
        val pastComment = "Some existing comment"
        val map = mutableMapOf(Pair(pastTimeString, pastComment))
        val entity = user.copy(moderatorComments = map)
        val comment = "This is a comment"

        val result = userService.setModeratorComment(entity, comment)

        assertNotNull(result)
        assertNotNull(result.moderatorComments)
        assertEquals(2, result.moderatorComments.size)
        assertEquals(pastComment, result.moderatorComments[pastTimeString])
        assertTrue(result.moderatorComments.values.first().contains(pastComment))
        assertTrue(result.moderatorComments.values.last().contains(comment))
    }
    // endregion

    // TODO setUserUsersRecruited
    // region setUserUsersRecruited
    // endregion

    // region setUserUsername
    @Test
    fun testSetUserUsername_BadUsername_ThrowArgumentException() = runBlocking {
        val entity = user.copy(usernameChangeTime = null)
        val newUsername = "this is not a valid _u.s\\e-r!n@a&m#e"

        `when`(utilityService.validateInput(
            anyString(),
            anyBoolean(),
            anyBoolean(),
            anyBoolean(),
            anyBoolean(),
            anyInt(),
            anyInt())).thenReturn(Pair(false, "mocked"))

        try
        {
            userService.setUserUsername(entity, newUsername)
            fail() // Fail here
        }
        catch(e: ArgumentException)
        {
            assertTrue(true)
        }
    }

    @Test
    fun testSetUserUsername_DateTimeReturnNull_ThrowDatabaseErrorException() = runBlocking {
        val entity = user.copy(usernameChangeTime = null)
        val newUsername = "NewName"

        `when`(utilityService.validateInput(
            anyString(),
            anyBoolean(),
            anyBoolean(),
            anyBoolean(),
            anyBoolean(),
            anyInt(),
            anyInt())).thenReturn(Pair(true, "mocked"))
        lenient().`when`(utilityService.stringToDatetime(anyString())).thenReturn(null)

        try
        {
            userService.setUserUsername(entity, newUsername)
            fail() // Fail here
        }
        catch(e: DatabaseErrorException)
        {
            assertTrue(true)
        }
    }

    @Test
    fun testSetUserUsername_RecentlyChanged_ThrowNotAuthorizedException() = runBlocking {
        val newUsername = "NewName"
        val nowDateTimeString = Instant.now().minus(2L, ChronoUnit.HOURS).toString()
        val entity = user.copy(usernameChangeTime = nowDateTimeString)
        val nowDateTime = Instant.parse(nowDateTimeString)
        val changeDateTime = Instant.parse(entity.usernameChangeTime)

        `when`(utilityService.validateInput(
            anyString(),
            anyBoolean(),
            anyBoolean(),
            anyBoolean(),
            anyBoolean(),
            anyInt(),
            anyInt())).thenReturn(Pair(true, "mocked"))
        // Datetime for "now"
        lenient().`when`(utilityService.stringToDatetime(nowDateTimeString)).thenReturn(nowDateTime)
        // Datetime for users username_change_date
        lenient().`when`(utilityService.stringToDatetime(entity.usernameChangeTime!!)).thenReturn(changeDateTime)

        try
        {
            userService.setUserUsername(entity, newUsername)
            fail() // Fail here
        }
        catch(e: NotAuthorizedException)
        {
            println("testUpdateUserUsername_RecentlyChanged_ReturnFalse message: ${e.message}")
            assertTrue(true)
        }
    }

    @Test
    fun testSetUserUsername_NormalPreviousChangeDate_ReturnUser() = runBlocking {
        val entity = user.copy(usernameChangeTime = "2020-03-30T10:00:00.000Z")
        val oldUsername = entity.username
        val newUsername = "NewName"
        val nowDateTime = Instant.now()
        val nowDateTimeString = nowDateTime.toString()
        val changeDateTime = Instant.parse(entity.usernameChangeTime)

        `when`(utilityService.validateInput(
            anyString(),
            anyBoolean(),
            anyBoolean(),
            anyBoolean(),
            anyBoolean(),
            anyInt(),
            anyInt())).thenReturn(Pair(true, "mocked"))
        // Datetime for "now"
        lenient().`when`(utilityService.stringToDatetime(nowDateTimeString)).thenReturn(nowDateTime)
        // Datetime for users username_change_date
        lenient().`when`(utilityService.stringToDatetime(entity.usernameChangeTime!!)).thenReturn(changeDateTime)

        val result = userService.setUserUsername(entity, newUsername)

        assertNotNull(result)
        assertTrue(Duration.between(nowDateTime, Instant.parse(result.usernameChangeTime)).toMillis() < 200)
        assertNotNull(result.previousUsernames)
        assertTrue(result.previousUsernames.size == 1)
        assertEquals(oldUsername, result.previousUsernames.values.first())
    }

    @Test
    fun testSetUserUsername_NormalNullChangeDate_ReturnUser() = runBlocking {
        val entity = user.copy(usernameChangeTime = null, previousUsernames = mutableMapOf())
        val oldUsername = entity.username
        val newUsername = "NewName"
        val nowDateTime = Instant.now()
        val nowDateTimeString = nowDateTime.toString()
        val changedDateString = "2000-01-01"
        val changedDateTimeString = "2000-01-01T00:00:00.000Z" // Include time for changeDateTime
        val changeDateTime = Instant.parse(changedDateTimeString)

        `when`(utilityService.validateInput(
            anyString(),
            anyBoolean(),
            anyBoolean(),
            anyBoolean(),
            anyBoolean(),
            anyInt(),
            anyInt())).thenReturn(Pair(true, "mocked"))
        // Datetime for "now"
        lenient().`when`(utilityService.stringToDatetime(nowDateTimeString)).thenReturn(nowDateTime)
        // Datetime for users username_change_date
        lenient().`when`(utilityService.stringToDatetime(changedDateString)).thenReturn(changeDateTime)

        val result = userService.setUserUsername(entity, newUsername)

        assertNotNull(result)
        assertNotNull(result.usernameChangeTime)
        assertTrue(Duration.between(nowDateTime, Instant.parse(result.usernameChangeTime)).toMillis() < 200)
        assertNotNull(result.previousUsernames)
        assertTrue(result.previousUsernames.size == 1)
        assertEquals(oldUsername, result.previousUsernames.values.first())
    }
    // endregion
}
