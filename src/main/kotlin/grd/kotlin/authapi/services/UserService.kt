package grd.kotlin.authapi.services

import com.google.cloud.firestore.Query
import grd.kotlin.authapi.Log
import grd.kotlin.authapi.dto.AUserDto
import grd.kotlin.authapi.dto.Converter
import grd.kotlin.authapi.enums.UserRole
import grd.kotlin.authapi.exceptions.*
import grd.kotlin.authapi.extensions.censureEmail
import grd.kotlin.authapi.extensions.censureString
import grd.kotlin.authapi.extensions.isNotNull
import grd.kotlin.authapi.extensions.isNull
import grd.kotlin.authapi.jwt.JwtUtil
import grd.kotlin.authapi.models.AUser
import grd.kotlin.authapi.settings.Settings
import io.jsonwebtoken.ExpiredJwtException
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.DependsOn
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import java.time.Instant
import java.time.temporal.ChronoUnit

@Service
@DependsOn("FirebaseInitialize")
class UserService : BaseService<AUser>(AUser::class.java, true)
{
    @Autowired
    lateinit var settings: Settings

    @Autowired
    private lateinit var jwtUtil: JwtUtil

    @Bean
    fun passwordEncoder(): PasswordEncoder = BCryptPasswordEncoder()

    /**
     * Authenticate that user logging in is who they claim they are
     * @param user User user
     * @param inputPassword String user password
     * @return Boolean true/false success
     * @throws none
     **/
    fun authenticate(user: AUser, inputPassword: String): Boolean
    {
        return passwordEncoder().matches(inputPassword, user.password)
    }

    /**
     * Authenticate and generate new token for user
     * @param username String username
     * @param email String user email
     * @param password String user password
     * @return Boolean true/false success
     * @throws NotFoundException if user not found
     * @throws NotAuthorizedException if login is not valid or user is locked
     **/
    @Throws(NotFoundException::class, NotAuthorizedException::class)
    suspend fun authenticateGenerateToken(username: String?, email: String?, password: String): String
    {
        val user = findByUsernameEmail(username, email)

        val userLockedCheck = isUserLocked(user, true)
        if(userLockedCheck.first)
            throw NotAuthorizedException(userLockedCheck.second)

        if(settings.userModeration.disableInactiveAccounts == true
            && user.lastActiveTime.isNotNull()
            && utilityService.stringToDatetime(user.lastActiveTime!!)!!
                .isAfter(Instant.now().plus(settings.userModeration.disableInactiveAccountsAfterMonths!!.toLong(), ChronoUnit.MONTHS)))
        {
//            user.lockedUntilTime = Instant.now().toString()
//            update(user, "System",false, true) // Necessary?

            throw NotAuthorizedException("Account locked due to ${settings.userModeration.disableInactiveAccountsAfterMonths ?: "unknown"} month(s) of inactivity.")
        }

        if(authenticate(user, password))
            return jwtUtil.generateToken(user)

        throw NotAuthorizedException((if(!username.isNullOrEmpty()) "Username" else "Email") + " or password was not valid.")
    }

    /**
     * Checks tokens values
     * @param token String JWT token
     * @return String of token values
     * @throws none
     **/
    suspend fun checkToken(token: String): String
    {
        try
        {
            val user = getUserFromToken(token)
            var content = "Token:"
            content += " User ID: " + jwtUtil.getIdFromToken(token)
            content += " Username: " + jwtUtil.getUsernameFromToken(token)
            content += ", Claims: " + jwtUtil.getAllClaimsFromToken(token)
            content += ", IssuedAt: " + jwtUtil.getIssuedAtDateFromToken(token)
            content += ", ExpirationDate: " + jwtUtil.getExpirationDateFromToken(token)
            content += ", Validate: " + jwtUtil.validateToken(token, user)

            content += " - SecurityContextHolder:"
            content += " Authority (UserRole): " + SecurityContextHolder.getContext().authentication.authorities.first().authority
            content += ", Credentials (ID): " + SecurityContextHolder.getContext().authentication.credentials
            content += ", IsAuthenticated: " + SecurityContextHolder.getContext().authentication.isAuthenticated
            // content += " Name: " + SecurityContextHolder.getContext().authentication.name // DANGER - contains full user data including password
            return content
        }
        catch(e: ExpiredJwtException)
        {
            // Possible TODO: https://stackoverflow.com/questions/35791465/is-there-a-way-to-parse-claims-from-an-expired-jwt-token/35791515
            Log.main.info("{function}, {message}", this.toString(), e.message)
            return "Token expired"
        }
    }

    /**
     * Find user by username or email, prioritizing arguments in that order
     * @param username String user name
     * @param email String user email
     * @return User user
     * @throws ArgumentException if missing both arguments
     * @throws UnexpectedStateException if multiple users found, expected only 1
     * @throws NotFoundException if user not found
     **/
    @Throws(ArgumentException::class, UnexpectedStateException::class, NotFoundException::class)
    suspend fun findByUsernameEmail(username: String?, email: String? = null): AUser
    {
        var users = emptyList<AUser>()
        if(username.isNullOrEmpty() && email.isNullOrEmpty())
            throw ArgumentException("Enter a username or an email")
        else if(!username.isNullOrEmpty())
            users = getQueried { e: Query -> e.whereEqualTo("username", username) }
        else if(!email.isNullOrEmpty())
            users = getQueried { e: Query -> e.whereEqualTo("email", email) }

        when
        {
            users.size > 1 ->
            {
                Log.main.info("Multiple users found, {function}, {username}, {email}", this.toString(), username, email?.censureEmail())
                throw UnexpectedStateException("Internal error, duplicates users found. Please alert a moderator.")
            }
            users.isEmpty() ->
            {
                Log.main.debug("No users found, {function}, {username}, {email}", this.toString(), username, email?.censureEmail())
                throw NotFoundException("No users found with that " + (if(!username.isNullOrEmpty()) "username" else "email"))
            }
            else ->
            {
                return users.first()
            }
        }
    }

    /**
     * Get user from token.
     * @param token with user details
     * @return AUser found form token
     * @throws NotFoundException if user not found
     **/
    @Throws(ArgumentException::class, NotFoundException::class)
    fun getUserFromToken(token: String): AUser
    {
        val id = jwtUtil.getIdFromToken(token)
        return get(id)
    }

    /**
     * Get user from headers.
     * @param headers raw map of headers
     * @return AUser found form authorizationFieldName header token
     * @throws ArgumentException if authorizationFieldName header is missing or malformed
     **/
    @Throws(ArgumentException::class, NotFoundException::class)
    suspend fun getUserFromHeaders(headers: Map<String, String>): AUser
    {
        val rawToken = headers[settings.server.authenticationHeader]?: throw ArgumentException("Missing ${settings.server.authenticationHeader} header with JWT.")
        val rawTokenSplit = rawToken.split(settings.jwt.tokenPrefix!!)
        if(rawTokenSplit.size != 2)
        {
            Log.main.info("Malformed JWT in header, {function}, {header}, {token}, {tokenSplitSize}", this.toString(), settings.server.authenticationHeader, rawToken.censureString(), rawTokenSplit.size)
            throw ArgumentException("Malformed ${settings.server.authenticationHeader} header, missing token prefix.")
        }

        return getUserFromToken(rawTokenSplit.last())
    }

    /**
     * Check users expires_date and lockedUntilTime, if account has expired or is currently locked, return true with message.
     * Option to unlock user if current datetime is after [user].lockedUntilTime. This will save to database.
     * @param user User to check
     * @param unlockIfEligible Boolean if user should be unbanned if their lockedUntilTime is in the past
     * @return Pair of Boolean false if user may proceed, and String reason
     * @throws none maybe
     **/
    suspend fun isUserLocked(user: AUser, unlockIfEligible: Boolean): Pair<Boolean, String>
    {
        val now = Instant.now()

        if(user.expirationTime != null)
        {
            val expires = utilityService.stringToDatetime(user.expirationTime!!) ?: return Pair(true, "The expiration time of was malformed, please contact a moderator.")
            if(expires.isBefore(now))
            {
                Log.main.info("User expired, {function}, {user}, {expired}", this.toString(), user.id, user.expirationTime)
                return Pair(true, "This user expired ${user.expirationTime}.")
            }

            return Pair(false, "This user expires ${user.expirationTime}.")
        }
        if(user.lockedUntilTime != null)
        {
            val lockedUntil = utilityService.stringToDatetime(user.lockedUntilTime!!) ?: return Pair(true, "The locked until date of was malformed, please contact a moderator.")
            if(unlockIfEligible && lockedUntil.isBefore(now))
            {
                val updatedUser = user.copy(lockedUntilTime = null)
                val editorId = "" // TODO pass editor
                update(updatedUser, editorId)

                Log.main.info("User unlocked, {function}, {editor}, {user}, {expired}", this.toString(), editorId, user.id, user.expirationTime)
                return Pair(false, "The user was unlocked (lock expired: ${user.lockedUntilTime}).")
            }

            Log.main.info("User locked, {function}, {user}, {expired}", this.toString(), user.id, user.expirationTime)
            return Pair(true, "This user is locked until ${user.lockedUntilTime}.")
        }

        return Pair(false, "User is not set to expire and is not locked.")
    }

    /**
     * Checks if [editor] is authorized for action with [operationMinimumRole].
     * @param operationMinimumRole to check against
     * @param editor user that is requesting action
     * @return Boolean action is allowed
     * @throws NotAuthorizedException if action is not allowed
     **/
    @Throws(NotAuthorizedException::class)
    suspend fun actionAllowed(operationMinimumRole: UserRole, editor: AUser): Boolean
    {
        if(editor.deleted.isNotNull())
        {
            Log.main.info("User action not allowed, deleted, {function}, {deleted}, {editor}", this.toString(), editor.deleted, editor.id)
            throw NotAuthorizedException("User does not exist.")
        }
        else if(editor.expirationTime.isNotNull() && utilityService.stringToDatetime(editor.expirationTime!!)?.let { it < Instant.now()} == true)
        {
            Log.main.info("User action not allowed, expired, {function}, {expirationTime}, {editor}", this.toString(), editor.expirationTime, editor.id)
            throw NotAuthorizedException("User expired at ${editor.expirationTime}.")
        }
        else if(editor.lockedUntilTime.isNotNull() && utilityService.stringToDatetime(editor.lockedUntilTime!!)?.let { it > Instant.now()} == true)
        {
            Log.main.info("User action not allowed, locked, {function}, {lockedUntilTime}, {editor}", this.toString(), editor.lockedUntilTime, editor.id)
            throw NotAuthorizedException("User is locked until ${editor.lockedUntilTime}.")
        }
        else if(editor.acceptedTerms.isNull())
        {
            Log.main.info("User action not allowed, not accepted terms and conditions, {function}, {operationMinimumRole}, {editorRole}, {editor}", this.toString(), operationMinimumRole, editor.role, editor.id)
            throw NotAuthorizedException("User has not accepted terms and conditions.")
        }
        else if(operationMinimumRole.value > editor.role.value)
        {
            Log.main.info("User action not allowed, not authorized, {function}, {operationMinimumRole}, {editorRole}, {editor}", this.toString(), operationMinimumRole, editor.role, editor.id)
            throw NotAuthorizedException("Action was not authorized for this user.")
        }

        return true
    }

    /**
     * Checks if [editor] is authorized for action by editing themselves or with [operationMinimumRole]. This method should be used when actions affect users.
     * @param operationMinimumRole to check against
     * @param editor user that is requesting action
     * @param subject user that will be affected
     * @param allowSelf should the user be allowed to call this action themselves even if they do not meet the [operationMinimumRole]?
     * @return Boolean action is allowed
     * @throws NotAuthorizedException if action is not allowed
     **/
    @Throws(NotAuthorizedException::class)
    suspend fun actionAllowedUser(operationMinimumRole: UserRole, editor: AUser, subject: AUser, allowSelf: Boolean = false): Boolean
    {
        if(allowSelf && editor.id == subject.id)
        {
            Log.main.info("User action allowed, {function}, {operationMinimumRole}, {editorRole}, {editor}, {subject}", this.toString(), operationMinimumRole, editor.role, editor.id, subject.id)
            return true
        }

        return actionAllowed(operationMinimumRole, editor)
    }

    /**
     * Register new user
     * @param dto User to register
     * @param password String password
     * @param editorId String ID of editor
     * @return User created
     * @throws ArgumentException if username or email are already in use, or expirationTime or lockedUntilTime are not valid DateTime
     * @throws DuplicateException on duplicate from adding to DB
     **/
    @Throws(ArgumentException::class, DuplicateException::class)
    suspend fun registerNew(dto: AUserDto, password: String, editorId: String): AUser
    {
        val usernameCheck = utilityService.validateInput(
            dto.username!!,
            allowNorwegianLetters = true,
            allowNumbers = true,
            allowSingleSpace = false,
            allowEmailSymbols = false,
            minimumLength = 2,
            maximumLength = 32)
        if(!usernameCheck.first)
        {
            Log.main.info("User registration not valid: ${usernameCheck.second}, {function}, {username}, {editor}", this.toString(), dto.username, editorId)
            throw ArgumentException("The username is not valid. ${usernameCheck.second}.")
        }

        val emailCheck = utilityService.validateEmail(dto.email)
        if(!emailCheck.first)
        {
            Log.main.info("User registration not valid: ${usernameCheck.second}, {function}, {username}, {editor}", this.toString(), dto.username, editorId)
            throw ArgumentException("The email is not valid. ${emailCheck.second}.")
        }

        val user = Converter.convert(dto, AUser::class.java)
        user.id = utilityService.getRandomString()
        user.previousUsernames = mutableMapOf()
        user.usernameChangeTime = Instant.now().toString()
        user.password = passwordEncoder().encode(password)
        user.moderatorComments = mutableMapOf()

        // username, email must be unique
        var duplicateUsername = true
        var duplicateEmail = true
        try
        {
            findByUsernameEmail(user.username)
        }
        catch(e: NotFoundException)
        {
            duplicateUsername = false
        }
        try
        {
            findByUsernameEmail(null, user.email)
            }
        catch(e: NotFoundException)
        {
            duplicateEmail = false
        }

        if(duplicateUsername || duplicateEmail)
        {
            val duplicateValue = if(duplicateUsername) "Username" else "Email"
            Log.main.info("$duplicateValue duplicate, {function}, {editor}, {username}, {email}", this.toString(), editorId, dto.username, dto.email.censureEmail())
            throw ArgumentException("$duplicateValue is already in use. Please pick another.")
        }

        if(user.expirationTime != null && utilityService.stringToDatetime(user.expirationTime!!) == null)
        {
            Log.main.info("User expirationTime invalid, {function}, {editor}, {username}, {datetime}", this.toString(), editorId, dto.username, user.expirationTime)
            throw ArgumentException("expirationTime (${user.expirationTime}) is not a valid DateTime.")
        }
        if(user.lockedUntilTime != null && utilityService.stringToDatetime(user.lockedUntilTime!!) == null)
        {
            Log.main.info("User lockedUntilTime invalid, {function}, {editor}, {username}, {username}", this.toString(), editorId, dto.username, user.lockedUntilTime)
            throw ArgumentException("lockedUntilTime (${user.lockedUntilTime}) is not a valid DateTime.")
        }

        return add(user, editorId)
    }

    /**
     * Update email for user
     * @param user User to update
     * @param newValue username to set
     * @return User updated user object
     * @throws ArgumentException if email is invalid
     **/
    @Throws(ArgumentException::class)
    suspend fun setUserEmail(user: AUser, newValue: String): AUser
    {
        val emailCheck = utilityService.validateEmail(newValue)
        if(!emailCheck.first)
        {
            Log.main.info("User new email invalid: ${emailCheck.second}, {function}, {subject}, {newValue}", this.toString(), user.id, newValue.censureEmail())
            throw ArgumentException("Invalid email: ${emailCheck.second}")
        }

        user.email = newValue
        return user
    }

    /**
     * Set user lock
     * @param user to set new value in
     * @param newValue value to set
     * @return User updated user object
     * @throws ArgumentException if new value is invalid
     **/
    @Throws(ArgumentException::class)
    suspend fun setUserLock(user: AUser, newValue: String?): AUser
    {
        if(newValue.isNotNull() && utilityService.stringToDatetime(newValue!!) == null)
        {
            Log.main.info("User lockedUntilTime invalid, {function}, {subject}, {newValue}", this.toString(), user.id, newValue)
            throw ArgumentException("Invalid DateTime value.")
        }

        user.lockedUntilTime = newValue
        return user
    }

    /**
     * Add a new moderator comment to user
     * @param user user to update
     * @param newValue comment to add
     * @return User updated user object
     * @throws none
     **/
    fun setModeratorComment(user: AUser, newValue: String): AUser
    {
        val map = user.moderatorComments
        map[Instant.now().toString()] = "${user.id}: $newValue"

        user.moderatorComments = map
        return user
    }

    /**
     * Sets a new username for user, adding old username to previousUsernames
     * @param user user to update
     * @param newValue username to set
     * @return User updated user object
     * @throws ArgumentException if invalid username
     * @throws DatabaseErrorException if issues with usernameChangeTime in database
     * @throws NotAuthorizedException if attempt to change username too soon
     **/
    @Throws(ArgumentException::class, NotAuthorizedException::class, DatabaseErrorException::class)
    suspend fun setUserUsername(user: AUser, newValue: String): AUser
    {
        val usernameCheck = utilityService.validateInput(
            newValue,
            allowNorwegianLetters = true,
            allowNumbers = true,
            allowSingleSpace = false,
            allowEmailSymbols = false,
            minimumLength = 2,
            maximumLength = 32
        )
        if(!usernameCheck.first)
        {
            Log.main.info("User username invalid: ${usernameCheck.second}, {function}, {subject}, {newValue}", this.toString(), user.id, newValue)
            throw ArgumentException("Invalid username: ${usernameCheck.second}")
        }

        // Limit frequency of name change, e.g. only once every few months, lastChange + cool-down must be before current date. If after, return false + message
        val now = Instant.now()
        val lastChange = utilityService.stringToDatetime(user.usernameChangeTime ?: "2000-01-01")
        if(lastChange == null)
        {
            Log.main.warn("User usernameChangeTime is null and invalid, {function}, {subject}, {newValue}", this.toString(), user.id, newValue)
            throw DatabaseErrorException("Internal error, contact a moderator to correct user data for name change.") // usernameChangeTime == null, user never changed name
        }

        val lastChangeTimeout = lastChange.plus(30L, ChronoUnit.DAYS)
        if(lastChangeTimeout.isAfter(now))
        {
            Log.main.info("User changed username too recently, {function}, {subject}, {newValue}, {datetime}", this.toString(), user.id, newValue, lastChangeTimeout)
            throw NotAuthorizedException("User may not change username before $lastChangeTimeout.")
        }

        val previousNames = user.previousUsernames
        previousNames[now.toString()] = user.username

        user.username = newValue
        user.previousUsernames = previousNames
        user.usernameChangeTime = now.toString()
        return user
    }

    /**
     * Set terms and conditions for usage as accepted for the user given by [userId]
     * @param user user to update for
     * @param editorId ID of editor
     * @return User updated user object
     * @throws ArgumentException if invalid username
     * @throws DatabaseErrorException if issues with usernameChangeTime in database
     * @throws NotAuthorizedException if attempt to change username too soon
     **/
    @Throws(DatabaseErrorException::class)
    fun acceptTerms(user: AUser, editorId: String): Boolean
    {
        if(user.acceptedTerms.isNull())
        {
            user.acceptedTerms = Instant.now().toString()
            update(user, editorId)
        }

        return true
    }
}
