package grd.kotlin.authapi.controllers

import grd.kotlin.authapi.Log
import grd.kotlin.authapi.dto.*
import grd.kotlin.authapi.exceptions.*
import grd.kotlin.authapi.models.AUser
import grd.kotlin.authapi.services.UserService
import grd.kotlin.authapi.services.ControllerUtilityService
import grd.kotlin.authapi.dto.Converter
import grd.kotlin.authapi.dto.WrappedResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import javax.validation.Valid

@RequestMapping("/users")
@RestController
class UserController
{
    @Autowired
    lateinit var controllerUtilityService: ControllerUtilityService

    @Autowired
    private lateinit var userService: UserService

    @Operation(summary = "Register new user, requires role MODERATOR")
    @PostMapping(
        path = ["/register"],
        produces = [(MediaType.APPLICATION_JSON_VALUE)])
    suspend fun registerNew(
        @RequestHeader headers: Map<String, String>,
        @Parameter(description = "User body")
        @Valid @RequestBody dto: AUserDto,
        @Parameter(description = "Users password")
        @Valid @RequestParam(value = "password") password: String,
    ): ResponseEntity<WrappedResponse<AUserDto?>>
    {
        var editor: AUser? = null

        // TODO encrypt calls because plaintext passwords?
        return try
        {
            editor = userService.getUserFromHeaders(headers)
            userService.actionAllowed(controllerUtilityService.minimumStaffRole, editor)

            val entity = userService.registerNew(dto, password, editor.id)
            Log.main.info("{function}, {editor}, {code}, {message}, {username}", this.toString(), editor.id, 200, null, dto.username)
            ResponseEntity.status(201).body(
                WrappedResponse<AUserDto?>(code = 201, data = Converter.convert(entity, AUserDto::class.java)).validated())
        }
        catch(e: Exception)
        {
            val httpReturn = controllerUtilityService.getErrorHttpReturn(e, this.toString(), editor?.id)
            ResponseEntity.status(httpReturn.code).body(WrappedResponse<AUserDto?>(code = httpReturn.code, message = httpReturn.message).validated())
        }
    }

    @Operation(summary = "Get a user by ID, requires role MODERATOR to get other users data")
    @GetMapping(
        path = ["/{id}"],
        produces = [(MediaType.APPLICATION_JSON_VALUE)])
    suspend fun getById(
        @RequestHeader headers: Map<String, String>,
        @Parameter(description = "ID")
        @Valid @RequestParam(value = "id") id: String,
    ): ResponseEntity<WrappedResponse<AUserDto?>>
    {
        var editor: AUser? = null

        return try
        {
            try
            {
                editor = userService.getUserFromHeaders(headers)
            }
            catch(e: NotFoundException) // No editor from token = 400, no user from get = 200
            {
                Log.main.info("{function}, {editor}, {code}, {message}, {id}", this.toString(), null, 400, e.message, id)
                return ResponseEntity.status(400).body(
                    WrappedResponse<AUserDto?>(code = 400, message = e.message).validated())
            }

            val entity = userService.get(id)
            userService.actionAllowedUser(controllerUtilityService.minimumStaffRole, editor, entity, true)

            Log.main.info("{function}, {editor}, {code}, {message}, {id}", this.toString(), editor.id, 200, null, id)
            ResponseEntity.status(200).body(
                WrappedResponse<AUserDto?>(code = 200, data = Converter.convert(entity, AUserDto::class.java)).validated())
        }
        catch(e: Exception)
        {
            val httpReturn = controllerUtilityService.getErrorHttpReturn(e, this.toString(), editor?.id, notFoundCode = 200)
            ResponseEntity.status(httpReturn.code).body(WrappedResponse<AUserDto?>(code = httpReturn.code, message = httpReturn.message).validated())
        }
    }

    @Operation(summary = "Get a user detailed by ID, requires role MODERATOR to get other users data")
    @GetMapping(
        path = ["/detailed/{id}"],
        produces = [(MediaType.APPLICATION_JSON_VALUE)])
    suspend fun getDetailedById(
        @RequestHeader headers: Map<String, String>,
        @Parameter(description = "ID")
        @Valid @RequestParam(value = "id") id: String,
    ): ResponseEntity<WrappedResponse<AUser?>>
    {
        var editor: AUser? = null

        return try
        {
            try
            {
                editor = userService.getUserFromHeaders(headers)
            }
            catch(e: NotFoundException) // No editor from token = 400, no user from get = 200
            {
                Log.main.info("{function}, {editor}, {code}, {message}, {id}", this.toString(), null, 400, e.message, id)
                return ResponseEntity.status(400).body(
                    WrappedResponse<AUser?>(code = 400, message = e.message).validated())
            }

            val entity = userService.get(id)
            entity.password = "[sensitive]"
            userService.actionAllowedUser(controllerUtilityService.minimumStaffRole, editor, entity, true)

            Log.main.info("{function}, {editor}, {code}, {message}, {id}", this.toString(), editor.id, 200, null, id)
            ResponseEntity.status(200).body(
                WrappedResponse<AUser?>(code = 200, data = entity).validated())
        }
        catch(e: Exception)
        {
            val httpReturn = controllerUtilityService.getErrorHttpReturn(e, this.toString(), editor?.id, notFoundCode = 200)
            ResponseEntity.status(httpReturn.code).body(WrappedResponse<AUser?>(code = httpReturn.code, message = httpReturn.message).validated())
        }
    }

    @Operation(summary = "Find a user by username, requires role MODERATOR to get other users data")
    @GetMapping(
        path = ["/username/{username}"],
        produces = [(MediaType.APPLICATION_JSON_VALUE)])
    suspend fun getByUsername(
        @RequestHeader headers: Map<String, String>,
        @Parameter(description = "Username")
        @Valid @RequestParam(value = "username") username: String,
    ): ResponseEntity<WrappedResponse<AUserDto?>>
    {
        var editor: AUser? = null

        return try
        {
            try
            {
                editor = userService.getUserFromHeaders(headers)
            }
            catch(e: NotFoundException) // No editor from token = 400, no user from get = 200
            {
                Log.main.info("{function}, {editor}, {code}, {message}, {username}", this.toString(), null, 400, e.message, username)
                return ResponseEntity.status(400).body(
                    WrappedResponse<AUserDto?>(code = 400, message = e.message).validated())
            }

            val entity = userService.findByUsernameEmail(username)
            userService.actionAllowedUser(controllerUtilityService.minimumStaffRole, editor, entity, true)

            Log.main.info("{function}, {editor}, {code}, {message}, {username}", this.toString(), editor.id, 200, null, username)
            ResponseEntity.status(200).body(
                WrappedResponse<AUserDto?>(code = 200, data = Converter.convert(entity, AUserDto::class.java)).validated())
        }
        catch(e: Exception)
        {
            val httpReturn = controllerUtilityService.getErrorHttpReturn(e, this.toString(), editor?.id, notFoundCode = 200)
            ResponseEntity.status(httpReturn.code).body(WrappedResponse<AUserDto?>(code = httpReturn.code, message = httpReturn.message).validated())
        }
    }

    @Operation(summary = "Get all users (paged), requires role MODERATOR")
    @GetMapping(path = [""], produces = [(MediaType.APPLICATION_JSON_VALUE)])
    suspend fun getAll(
        @RequestHeader
        headers: Map<String, String>,
        @Parameter(description = "Page number")
        @RequestParam(value = "page", required = false)
        page: Int?,
        @Parameter(description = "Items per page")
        @RequestParam(value = "pageSize", required = false)
        pageSize: Int?,
    ): ResponseEntity<WrappedResponse<Page<AUserDto>>>
    {
        var editor: AUser? = null

        return try
        {
            try
            {
                editor = userService.getUserFromHeaders(headers)
            }
            catch(e: NotFoundException) // No editor from token = 400, no user from get = 200
            {
                Log.main.info("{function}, {editor}, {code}, {message}", this.toString(), null, 400, e.message)
                return ResponseEntity.status(400).body(
                    WrappedResponse<Page<AUserDto>>(code = 400, message = e.message).validated())
            }

            val pageable = PageRequest.of(if(page == null || page < 0) 0 else page,
                if(pageSize == null || pageSize < 1) 20 else pageSize)
            val queried = userService.getAll(false, pageable)

            Log.main.info("{function}, {editor}, {code}, {message}, {page}, {pageSize}", this.toString(), editor.id, 200, null, page, pageSize)
            return ResponseEntity.status(200).body(
                WrappedResponse(code = 200, data = Converter.convert(queried, AUserDto::class.java)).validated())
        }
        catch(e: Exception)
        {
            val httpReturn = controllerUtilityService.getErrorHttpReturn(e, this.toString(), editor?.id, notFoundCode = 200)
            ResponseEntity.status(httpReturn.code).body(WrappedResponse<Page<AUserDto>>(code = httpReturn.code, message = httpReturn.message).validated())
        }
    }

    @Operation(summary = "Soft-delete an user, requires role MODERATOR")
    @DeleteMapping(
        path = ["/{id}"],
        produces = [(MediaType.APPLICATION_JSON_VALUE)])
    suspend fun softDeleteById(
        @RequestHeader headers: Map<String, String>,
        @Parameter(description = "ID")
        @Valid @PathVariable(value = "id") id: String,
    ): ResponseEntity<WrappedResponse<Boolean>>
    {
        var editor: AUser? = null

        return try
        {
            editor = userService.getUserFromHeaders(headers)
            val entity = userService.get(id)
            userService.actionAllowedUser(controllerUtilityService.minimumStaffRole, editor, entity, true)

            userService.delete(entity.id, editor.id)
            Log.main.info("{function}, {editor}, {code}, {message}, {id}", this.toString(), editor.id, 200, null, id)
            ResponseEntity.status(200).body(
                WrappedResponse(code = 200, data = true).validated())
        }
        catch(e: Exception)
        {
            val httpReturn = controllerUtilityService.getErrorHttpReturn(e, this.toString(), editor?.id)
            ResponseEntity.status(httpReturn.code).body(WrappedResponse(code = httpReturn.code, data = false, message = httpReturn.message).validated())
        }
    }

    @Operation(summary = "Restore a soft-deleted user, requires role MODERATOR")
    @GetMapping(
        path = ["/restore/{id}"],
        produces = [(MediaType.APPLICATION_JSON_VALUE)])
    suspend fun restoreById(
        @RequestHeader headers: Map<String, String>,
        @Parameter(description = "ID")
        @Valid @PathVariable(value = "id") id: String,
    ): ResponseEntity<WrappedResponse<Boolean>>
    {
        var editor: AUser? = null

        return try
        {
            editor = userService.getUserFromHeaders(headers)
            userService.actionAllowed(controllerUtilityService.minimumStaffRole, editor)

            userService.restore(id, editor.id)
            Log.main.info("{function}, {editor}, {code}, {message}, {id}", this.toString(), editor.id, 200, null, id)
            ResponseEntity.status(200).body(
                WrappedResponse(code = 200, data = true).validated())
        }
        catch(e: Exception)
        {
            val httpReturn = controllerUtilityService.getErrorHttpReturn(e, this.toString(), editor?.id)
            ResponseEntity.status(httpReturn.code).body(WrappedResponse(code = httpReturn.code, data = false, message = httpReturn.message).validated())
        }
    }

    @Operation(summary = "Remove (permanently delete) a deleted user, requires role MODERATOR")
    @DeleteMapping(
        path = ["/remove/{id}"],
        produces = [(MediaType.APPLICATION_JSON_VALUE)])
    suspend fun removeById(
        @RequestHeader headers: Map<String, String>,
        @Parameter(description = "ID")
        @Valid @PathVariable(value = "id") id: String,
    ): ResponseEntity<WrappedResponse<Boolean>>
    {
        var editor: AUser? = null

        return try
        {
            editor = userService.getUserFromHeaders(headers)
            val entity = userService.get(id, true)
            userService.actionAllowedUser(controllerUtilityService.minimumStaffRole, editor, entity, true)

            userService.remove(entity.id, entity.id, true)
            Log.main.info("{function}, {editor}, {code}, {message}, {id}", this.toString(), editor.id, 200, null, id)
            ResponseEntity.status(200).body(
                WrappedResponse(code = 200, data = true).validated())
        }
        catch(e: Exception)
        {
            val httpReturn = controllerUtilityService.getErrorHttpReturn(e, this.toString(), editor?.id)
            ResponseEntity.status(httpReturn.code).body(WrappedResponse(code = httpReturn.code, data = false, message = httpReturn.message).validated())
        }
    }

    @Operation(summary = "Get a token for user")
    @GetMapping(
        path = ["/getToken"],
        produces = [(MediaType.APPLICATION_JSON_VALUE)])
    suspend fun getToken(
        @Parameter(description = "Username")
        @Valid @RequestParam(value = "username") username: String,
        @Parameter(description = "Password")
        @Valid @RequestParam(value = "password") password: String,
    ): ResponseEntity<WrappedResponse<String?>>
    {
        // TODO encrypt calls because plaintext passwords?
        return try
        {
            val token = userService.authenticateGenerateToken(username, null, password)

            Log.main.info("{function}, {username}, {code}, {message}", this.toString(), username, 200, null)
            ResponseEntity.status(200).body(
                WrappedResponse<String?>(code = 200, data = token,
                    message = "Use token in the Authentication header for all writing requests.").validated())
        }
        catch(e: Exception)
        {
            val httpReturn = controllerUtilityService.getErrorHttpReturn(e, this.toString())
            ResponseEntity.status(httpReturn.code).body(WrappedResponse<String?>(code = httpReturn.code, message = httpReturn.message).validated())
        }
    }

    @Operation(summary = "Returns token claim values, requires role MODERATOR")
    @GetMapping(
        path = ["/checkToken"],
        produces = [(MediaType.APPLICATION_JSON_VALUE)])
    suspend fun checkToken(
        @RequestHeader headers: Map<String, String>,
        @Parameter(description = "Token")
        @Valid @RequestParam(value = "token") token: String,
    ): ResponseEntity<WrappedResponse<String?>>
    {
        var editor: AUser? = null
        val logSafeToken = "[..]${token.substring((token.length - 8) until token.length)}"

        return try
        {
            editor = userService.getUserFromHeaders(headers)
            userService.actionAllowed(controllerUtilityService.minimumStaffRole, editor)

            val result = userService.checkToken(token)
            Log.main.info("{function}, {editor}, {code}, {message}, {token}", this.toString(), editor.id, 200, null, logSafeToken)
            return ResponseEntity.status(200).body(
                WrappedResponse<String?>(code = 200, data = result).validated())
        }
        catch(e: Exception)
        {
            val httpReturn = controllerUtilityService.getErrorHttpReturn(e, this.toString(), editor?.id)
            ResponseEntity.status(httpReturn.code).body(WrappedResponse<String?>(code = httpReturn.code, message = httpReturn.message).validated())
        }
    }

    @Operation(summary = "Get a your own user from token")
    @GetMapping(
        path = ["/getSelf"],
        produces = [(MediaType.APPLICATION_JSON_VALUE)])
    suspend fun getSelfFromToken(
        @RequestHeader headers: Map<String, String>,
    ): ResponseEntity<WrappedResponse<AUserDto?>>
    {
        var editor: AUser? = null

        return try
        {
            editor = userService.getUserFromHeaders(headers)

            Log.main.info("{function}, {editor}, {code}, {message}", this.toString(), editor.id, 200, null)
            ResponseEntity.status(200).body(
                WrappedResponse<AUserDto?>(code = 200, data = Converter.convert(editor, AUserDto::class.java)).validated())
        }
        catch(e: Exception)
        {
            val httpReturn = controllerUtilityService.getErrorHttpReturn(e, this.toString(), editor?.id, notFoundCode = 200)
            ResponseEntity.status(httpReturn.code).body(WrappedResponse<AUserDto?>(code = httpReturn.code, message = httpReturn.message).validated())
        }
    }

    @Operation(summary = "Lock a user by ID until datetime given, requires role MODERATOR")
    @PostMapping(
        path = ["/lock/{id}"],
        produces = [(MediaType.APPLICATION_JSON_VALUE)])
    suspend fun lockUserById(
        @RequestHeader headers: Map<String, String>,
        @Parameter(description = "ID")
        @Valid @RequestParam(value = "id") id: String,
        @Parameter(description = "Lock until DateTime")
        @Valid @RequestParam(value = "lockUntil") lockUntil: String,
        @Parameter(description = "Lock reason (optional)")
        @Valid @RequestBody(required = false) reason: String,
    ): ResponseEntity<WrappedResponse<AUserDto?>>
    {
        var editor: AUser? = null

        return try
        {
            editor = userService.getUserFromHeaders(headers)
            userService.actionAllowed(controllerUtilityService.minimumStaffRole, editor)
            val entity = userService.get(id)

            var updateEntity = userService.setUserLock(entity, lockUntil)
            updateEntity = userService.setModeratorComment(updateEntity, "Locked until $lockUntil, reason: $reason")
            val result = userService.update(updateEntity, editor.id)
            Log.main.info("{function}, {editor}, {code}, {message}, {id}, {lockUntil}", this.toString(), editor.id, 200, null, id, lockUntil)
            ResponseEntity.status(200).body(
                WrappedResponse<AUserDto?>(code = 200, data = Converter.convert(result, AUserDto::class.java), message = "User locked until $lockUntil.").validated())
        }
        catch(e: Exception)
        {
            val httpReturn = controllerUtilityService.getErrorHttpReturn(e, this.toString(), editor?.id)
            ResponseEntity.status(httpReturn.code).body(WrappedResponse<AUserDto?>(code = httpReturn.code, message = httpReturn.message).validated())
        }
    }

    @Operation(summary = "Unlock a user by ID, requires role MODERATOR")
    @PostMapping(
        path = ["/unlock/{id}"],
        produces = [(MediaType.APPLICATION_JSON_VALUE)])
    suspend fun unlockUserById(
        @RequestHeader headers: Map<String, String>,
        @Parameter(description = "ID")
        @Valid @RequestParam(value = "id") id: String,
        @Parameter(description = "Unlock reason (optional)")
        @Valid @RequestBody(required = false) reason: String,
    ): ResponseEntity<WrappedResponse<AUserDto?>>
    {
        var editor: AUser? = null

        return try
        {
            editor = userService.getUserFromHeaders(headers)
            userService.actionAllowed(controllerUtilityService.minimumStaffRole, editor)
            val entity = userService.get(id)

            var updateEntity = userService.setUserLock(entity, null)
            updateEntity = userService.setModeratorComment(updateEntity, "Unlocked, reason: $reason")
            val result = userService.update(updateEntity, editor.id)
            Log.main.info("{function}, {editor}, {code}, {message}, {id}", this.toString(), editor.id, 200, null, id)
            ResponseEntity.status(200).body(
                WrappedResponse<AUserDto?>(code = 200, data = Converter.convert(result, AUserDto::class.java), message = "User unlocked.").validated())
        }
        catch(e: Exception)
        {
            val httpReturn = controllerUtilityService.getErrorHttpReturn(e, this.toString(), editor?.id)
            ResponseEntity.status(httpReturn.code).body(WrappedResponse<AUserDto?>(code = httpReturn.code, message = httpReturn.message).validated())
        }
    }

    @Operation(summary = "Add a moderator comment to a user by ID, requires role MODERATOR")
    @PostMapping(
        path = ["/addModeratorComment/{id}"],
        produces = [(MediaType.APPLICATION_JSON_VALUE)])
    suspend fun addModeratorCommentById(
        @RequestHeader headers: Map<String, String>,
        @Parameter(description = "ID")
        @Valid @RequestParam(value = "id") id: String,
        @Parameter(description = "Moderation comment")
        @Valid @RequestBody comment: String,
    ): ResponseEntity<WrappedResponse<AUserDto?>>
    {
        var editor: AUser? = null

        return try
        {
            editor = userService.getUserFromHeaders(headers)
            userService.actionAllowed(controllerUtilityService.minimumStaffRole, editor)
            val entity = userService.get(id)

            val updatedEntity = userService.setModeratorComment(entity, comment)
            val result = userService.update(updatedEntity, editor.id)
            Log.main.info("{function}, {editor}, {code}, {message}, {id}", this.toString(), editor.id, 200, null, id)
            ResponseEntity.status(200).body(
                WrappedResponse<AUserDto?>(code = 200, data = Converter.convert(result, AUserDto::class.java), message = "Comment added.").validated())
        }
        catch(e: Exception)
        {
            val httpReturn = controllerUtilityService.getErrorHttpReturn(e, this.toString(), editor?.id)
            ResponseEntity.status(httpReturn.code).body(WrappedResponse<AUserDto?>(code = httpReturn.code, message = httpReturn.message).validated())
        }
    }

    @Operation(summary = "Accept terms for user by ID, requires role MODERATOR")
    @PostMapping(
        path = ["/acceptTerms/{id}"],
        produces = [(MediaType.APPLICATION_JSON_VALUE)])
    suspend fun acceptTerms(
        @RequestHeader headers: Map<String, String>,
        @Parameter(description = "ID")
        @Valid @RequestParam(value = "id") id: String,
    ): ResponseEntity<WrappedResponse<Boolean>>
    {
        var editor: AUser? = null

        return try
        {
            editor = userService.getUserFromHeaders(headers)
            userService.actionAllowed(controllerUtilityService.minimumStaffRole, editor)
            val entity = userService.get(id)

            val result = userService.acceptTerms(entity, editor.id)
            Log.main.info("{function}, {editor}, {code}, {message}, {id}", this.toString(), editor.id, 200, null, id)
            ResponseEntity.status(200).body(
                WrappedResponse(code = 200, data = result).validated())
        }
        catch(e: Exception)
        {
            val httpReturn = controllerUtilityService.getErrorHttpReturn(e, this.toString(), editor?.id)
            ResponseEntity.status(httpReturn.code).body(WrappedResponse(code = httpReturn.code, data = false).validated())
        }
    }
}
