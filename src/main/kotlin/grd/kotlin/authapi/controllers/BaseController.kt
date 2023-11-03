package grd.kotlin.authapi.controllers

import grd.kotlin.authapi.Log
import grd.kotlin.authapi.dto.Converter
import grd.kotlin.authapi.dto.WrappedResponse
import grd.kotlin.authapi.exceptions.*
import grd.kotlin.authapi.models.AUser
import grd.kotlin.authapi.services.BaseService
import grd.kotlin.authapi.services.UserService
import grd.kotlin.authapi.services.ControllerUtilityService
import com.google.gson.Gson
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.http.CacheControl
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.concurrent.TimeUnit

open class BaseController<TEntity: Any, TEntityDto : Any, TEntityService: BaseService<TEntity>>(var tClass: Class<TEntity>?, var tDtoClass: Class<TEntityDto>?)
{
    @Autowired
    lateinit var controllerUtilityService: ControllerUtilityService

    @Autowired
    lateinit var userService: UserService

    @Autowired
    lateinit var entityService: TEntityService

    @Autowired
    lateinit var gson: Gson

    @Operation(summary = "Create, requires role MODERATOR")
    @PostMapping(path = [""], produces = [(MediaType.APPLICATION_JSON_VALUE)])
    open suspend fun add(
        @RequestHeader headers: Map<String, String>,
        @Parameter(description = "Ingredient body")
        @RequestBody dto: TEntityDto,
    ): ResponseEntity<WrappedResponse<TEntityDto?>>
    {
        var editor: AUser? = null

        return try
        {
            editor = userService.getUserFromHeaders(headers)
            userService.actionAllowed(controllerUtilityService.minimumStaffRole, editor)

            val addEntity = Converter.convert(dto, tClass!!)
            val entity = entityService.add(addEntity, editor.id)

            val code = 201
            Log.main.info("{function}, {editor}, {code}, {message}, {id}", this.toString(), editor.id, code, null, entity::class.java.getField("id"))
            ResponseEntity.status(code).body(
                WrappedResponse<TEntityDto?>(code = code, data = Converter.convert(entity, tDtoClass!!)).validated())
        }
        catch(e: Exception)
        {
            val httpReturn = controllerUtilityService.getErrorHttpReturn(e, this.toString(), editor?.id)
            ResponseEntity.status(httpReturn.code).body(WrappedResponse<TEntityDto?>(code = httpReturn.code, message = httpReturn.message).validated())
        }
    }

    @Operation(summary = "Get randomly")
    @GetMapping(path = ["/random"], produces = [(MediaType.APPLICATION_JSON_VALUE)])
    open suspend fun getRandom(): ResponseEntity<WrappedResponse<TEntityDto?>>
    {
        return try
        {
            val entity = entityService.getRandom()

            val code = 200
            Log.main.info("{function}, {code}, {message}", this.toString(), code, null)
            ResponseEntity.status(code).body(
                WrappedResponse<TEntityDto?>(code = code, data = Converter.convert(entity, tDtoClass!!)).validated())
        }
        catch(e: Exception)
        {
            val httpReturn = controllerUtilityService.getErrorHttpReturn(e, this.toString(), notFoundCode = 200)
            ResponseEntity.status(httpReturn.code).body(WrappedResponse<TEntityDto?>(code = httpReturn.code, message = httpReturn.message).validated())
        }
    }

    @Operation(summary = "Get by ID")
    @GetMapping(path = ["/{id}"], produces = [(MediaType.APPLICATION_JSON_VALUE)])
    open suspend fun getById(
        @Parameter(description = "ID of target")
        @PathVariable("id")
        id: String,
    ): ResponseEntity<WrappedResponse<TEntityDto?>>
    {
        return try
        {
            val entity = entityService.get(id)

            val code = 200
            Log.main.info("{function}, {code}, {message}, {id}", this.toString(), code, null, id)
            return ResponseEntity.status(code).cacheControl(CacheControl.maxAge(3600, TimeUnit.SECONDS).cachePublic()).body(
                WrappedResponse<TEntityDto?>(code = code, data = Converter.convert(entity, tDtoClass!!)).validated())
        }
        catch(e: Exception)
        {
            val httpReturn = controllerUtilityService.getErrorHttpReturn(e, this.toString(), notFoundCode = 200)
            ResponseEntity.status(httpReturn.code).body(WrappedResponse<TEntityDto?>(code = httpReturn.code, message = httpReturn.message).validated())
        }
    }

    @Operation(summary = "Get detailed by ID, requires role MODERATOR")
    @GetMapping(path = ["/detailed/{id}"], produces = [(MediaType.APPLICATION_JSON_VALUE)])
    open suspend fun getDetailedById(
        @RequestHeader headers: Map<String, String>,
        @Parameter(description = "ID of target")
        @PathVariable("id")
        id: String,
    ): ResponseEntity<WrappedResponse<TEntity?>>
    {
        var editor: AUser? = null

        return try
        {
            try
            {
                editor = userService.getUserFromHeaders(headers)
            }
            catch(e: NotFoundException) // No editor from token = 400, no entity from get = 200
            {
                val code = 400
                Log.main.info("{function}, {editor}, {code}, {message}, {id}", this.toString(), null, code, e.message, id)
                return ResponseEntity.status(code).body(
                    WrappedResponse<TEntity?>(code = code, message = e.message).validated())
            }

            userService.actionAllowed(controllerUtilityService.minimumStaffRole, editor)
            val entity = entityService.get(id, true)

            val code = 200
            Log.main.info("{function}, {editor}, {code}, {message}, {id}", this.toString(), editor.id, code, null, id)
            return ResponseEntity.status(code).cacheControl(CacheControl.maxAge(3600, TimeUnit.SECONDS).cachePublic()).body(
                WrappedResponse<TEntity?>(code = code, data = entity).validated())
        }
        catch(e: Exception)
        {
            val httpReturn = controllerUtilityService.getErrorHttpReturn(e, this.toString(), editor?.id, notFoundCode = 200)
            ResponseEntity.status(httpReturn.code).body(WrappedResponse<TEntity?>(code = httpReturn.code, message = httpReturn.message).validated())
        }
    }

    @Operation(summary = "Get all (paged)")
    @GetMapping(path = [""], produces = [(MediaType.APPLICATION_JSON_VALUE)])
    open suspend fun getAll(
        @Parameter(description = "Page number")
        @RequestParam(value = "page", required = false)
        page: Int?,
        @Parameter(description = "Items per page")
        @RequestParam(value = "pageSize", required = false)
        pageSize: Int?,
    ): ResponseEntity<WrappedResponse<Page<TEntityDto>>>
    {
        return try
        {
            val pageable = PageRequest.of(if(page == null || page < 0) 0 else page,
                if(pageSize == null || pageSize < 1) 20 else pageSize)
            val result = entityService.getAll(false, pageable)

            val code = 200
            Log.main.info("{function}, {code}, {message}, {page}, {pageSize}", this.toString(), code, null, page, pageSize)
            return ResponseEntity.status(code).body(
                WrappedResponse(code = code, data = Converter.convert(result, tDtoClass!!)).validated())
        }
        catch(e: Exception)
        {
            val httpReturn = controllerUtilityService.getErrorHttpReturn(e, this.toString(), notFoundCode = 200)
            ResponseEntity.status(httpReturn.code).body(WrappedResponse<Page<TEntityDto>>(code = httpReturn.code, message = httpReturn.message).validated())
        }
    }

    @Operation(summary = "Merge patch, requires role MODERATOR")
    @PatchMapping(path = ["/{id}"], produces = [(MediaType.APPLICATION_JSON_VALUE)])
    open suspend fun patchById(
        @RequestHeader headers: Map<String, String>,
        @Parameter(description = "ID of of target")
        @PathVariable("id")
        id: String,
        @Parameter(description = "Values to patch")
        @RequestBody
        json: String,
    ): ResponseEntity<WrappedResponse<TEntityDto?>>
    {
        var editor: AUser? = null

        return try
        {
            editor = userService.getUserFromHeaders(headers)
            userService.actionAllowed(controllerUtilityService.minimumStaffRole, editor)

            val merged = entityService.patch(id, json)
            val entity = entityService.update(merged, editor.id)

            val code = 200
            Log.main.info("{function}, {editor}, {code}, {message}, {id}", this.toString(), editor.id, code, null, id)
            ResponseEntity.status(code).body(
                WrappedResponse<TEntityDto?>(code = code, data = Converter.convert(entity, tDtoClass!!), message = "Ingredient updated.").validated())
        }
        catch(e: Exception)
        {
            val httpReturn = controllerUtilityService.getErrorHttpReturn(e, this.toString(), editor?.id)
            ResponseEntity.status(httpReturn.code).body(WrappedResponse<TEntityDto?>(code = httpReturn.code, message = httpReturn.message).validated())
        }
    }

    @Operation(summary = "Soft-delete by ID, requires role MODERATOR")
    @DeleteMapping(path = ["/{id}"], produces = [(MediaType.APPLICATION_JSON_VALUE)])
    open suspend fun softDeleteById(
        @RequestHeader headers: Map<String, String>,
        @Parameter(description = "ID of of target")
        @PathVariable("id")
        id: String,
    ): ResponseEntity<WrappedResponse<Boolean>>
    {
        var editor: AUser? = null

        return try
        {
            editor = userService.getUserFromHeaders(headers)
            userService.actionAllowed(controllerUtilityService.minimumStaffRole, editor)
            entityService.delete(id, editor.id)

            val code = 200
            Log.main.info("{function}, {editor}, {code}, {message}, {id}", this.toString(), editor.id, code, null, id)
            ResponseEntity.status(code).body(
                WrappedResponse(code = code, data = true).validated())
        }
        catch(e: Exception)
        {
            val httpReturn = controllerUtilityService.getErrorHttpReturn(e, this.toString(), editor?.id)
            ResponseEntity.status(httpReturn.code).body(WrappedResponse<Boolean>(code = httpReturn.code, message = httpReturn.message).validated())
        }
    }

    @Operation(summary = "Restore a soft-deleted by ID, requires role MODERATOR")
    @GetMapping(path = ["/restore/{id}"], produces = [(MediaType.APPLICATION_JSON_VALUE)])
    open suspend fun restoreById(
        @RequestHeader headers: Map<String, String>,
        @Parameter(description = "ID of of target")
        @PathVariable("id")
        id: String,
    ): ResponseEntity<WrappedResponse<Boolean>>
    {
        var editor: AUser? = null

        return try
        {
            editor = userService.getUserFromHeaders(headers)
            userService.actionAllowed(controllerUtilityService.minimumStaffRole, editor)
            entityService.restore(id, editor.id)

            val code = 200
            Log.main.info("{function}, {editor}, {code}, {message}, {id}", this.toString(), editor.id, code, null, id)
            ResponseEntity.status(code).body(
                WrappedResponse(code = code, data = true).validated())
        }
        catch(e: Exception)
        {
            val httpReturn = controllerUtilityService.getErrorHttpReturn(e, this.toString(), editor?.id)
            ResponseEntity.status(httpReturn.code).body(WrappedResponse<Boolean>(code = httpReturn.code, message = httpReturn.message).validated())
        }
    }

    @Operation(summary = "Remove (permanently delete) a deleted by ID, requires role MODERATOR")
    @DeleteMapping(path = ["/remove/{id}"], produces = [(MediaType.APPLICATION_JSON_VALUE)])
    open suspend fun removeById(
        @RequestHeader headers: Map<String, String>,
        @Parameter(description = "ID of of target")
        @PathVariable("id")
        id: String,
    ): ResponseEntity<WrappedResponse<Boolean>>
    {
        var editor: AUser? = null

        return try
        {
            editor = userService.getUserFromHeaders(headers)
            userService.actionAllowed(controllerUtilityService.minimumStaffRole, editor)
            entityService.remove(id, editor.id, true)

            val code = 200
            Log.main.info("{function}, {editor}, {code}, {message}, {id}", this.toString(), editor.id, code, null, id)
            ResponseEntity.status(code).body(
                WrappedResponse(code = code, data = true).validated())
        }
        catch(e: Exception)
        {
            val httpReturn = controllerUtilityService.getErrorHttpReturn(e, this.toString(), editor?.id)
            ResponseEntity.status(httpReturn.code).body(WrappedResponse<Boolean>(code = httpReturn.code, message = httpReturn.message).validated())
        }
    }
}
