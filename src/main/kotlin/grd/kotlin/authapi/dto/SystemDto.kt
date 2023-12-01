package grd.kotlin.authapi.dto

import grd.kotlin.authapi.enums.UserRole
import io.swagger.v3.oas.annotations.media.Schema
import java.time.Instant
import java.util.*

data class SystemDto(
    @Schema(description = "ID")
    var id: String? = null,

    @Schema(description = "Name of system")
    var name: String,

    @Schema(description = "System is active and in use")
    var active: Boolean,
)
