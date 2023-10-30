package grd.kotlin.authapi.controllers

import grd.kotlin.authapi.Log
import grd.kotlin.authapi.dto.*
import grd.kotlin.authapi.exceptions.*
import grd.kotlin.authapi.models.ApiMetadata
import grd.kotlin.authapi.models.AUser
import grd.kotlin.authapi.services.ApiMetadataService
import grd.kotlin.authapi.services.UserService
import grd.kotlin.authapi.services.ControllerUtilityService
import grd.kotlin.authapi.dto.Converter
import grd.kotlin.authapi.dto.WrappedResponse
import io.swagger.v3.oas.annotations.Operation
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.CacheControl
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.concurrent.TimeUnit

@RequestMapping("/meta")
@RestController
class ApiMetadataController
{
    @Autowired
    lateinit var controllerUtilityService: ControllerUtilityService

    @Autowired
    private lateinit var apiMetadataService: ApiMetadataService

    @Autowired
    private lateinit var userService: UserService

    private val apiMetadataId = "content"

    @Operation(summary = "Get health status")
    @GetMapping(
        path = ["/health"],
        produces = [(MediaType.APPLICATION_JSON_VALUE)])
    suspend fun getHealth(): ResponseEntity<WrappedResponse<HealthDataDto>>
    {
        return try
        {
            val healthData = apiMetadataService.getHealthData()

            val code = 200
            Log.main.info("{function}, {code}, {message}", this.toString(), code, null)
            ResponseEntity.status(code).body(
                WrappedResponse(code = code, data = healthData).validated())
        }
        catch(e: Exception)
        {
            val code = 500
            val message = e.message.toString()

            Log.main.info("{function}, {code}, {message}", this.toString(), code, message)
            ResponseEntity.status(code).body(
                WrappedResponse<HealthDataDto>(code = code, message = message).validated())
        }
    }

    @Operation(summary = "Get metadata")
    @GetMapping(
        path = [""],
        produces = [(MediaType.APPLICATION_JSON_VALUE)])
    suspend fun getMetadata(): ResponseEntity<WrappedResponse<ApiMetadataDto?>>
    {
        return try
        {
            val result = apiMetadataService.get(apiMetadataId)

            val code = 200
            Log.main.info("{function}, {code}, {message}", this.toString(), code, null)
            return ResponseEntity.status(code).cacheControl(CacheControl.maxAge(3600, TimeUnit.SECONDS).cachePublic()).body(
                WrappedResponse<ApiMetadataDto?>(code = code, data = Converter.convert(result, ApiMetadataDto::class.java)).validated())
        }
        catch(e: Exception)
        {
            val httpReturn = controllerUtilityService.getErrorHttpReturn(e, this.toString())
            ResponseEntity.status(httpReturn.code).body(WrappedResponse<ApiMetadataDto?>(code = httpReturn.code, message = httpReturn.message).validated())
        }
    }

    @Operation(summary = "Update metadata, requires role MODERATOR")
    @GetMapping(
        path = ["/update"],
        produces = [(MediaType.APPLICATION_JSON_VALUE)])
    suspend fun updateMetadata(
        @RequestHeader headers: Map<String, String>,
    ): ResponseEntity<WrappedResponse<ApiMetadataDto?>>
    {
        var editor: AUser? = null

        return try
        {
            editor = userService.getUserFromHeaders(headers)
            userService.actionAllowed(controllerUtilityService.minimumStaffRole, editor)

            var apiMetadata = ApiMetadata(id = apiMetadataId)
            apiMetadata = apiMetadataService.setData(apiMetadata, editor.id)
            val entity = apiMetadataService.update(apiMetadata, editor.id, true)

            val code = 200
            Log.main.info("{function}, {editor}, {code}, {message}", this.toString(), editor.id, 200, null)
            ResponseEntity.status(code).body(
                WrappedResponse<ApiMetadataDto?>(code = code, data = Converter.convert(entity, ApiMetadataDto::class.java)).validated())
        }
        catch(e: Exception)
        {
            val httpReturn = controllerUtilityService.getErrorHttpReturn(e, this.toString(), editor?.id)
            ResponseEntity.status(httpReturn.code).body(WrappedResponse<ApiMetadataDto?>(code = httpReturn.code, message = httpReturn.message).validated())
        }
    }
}
