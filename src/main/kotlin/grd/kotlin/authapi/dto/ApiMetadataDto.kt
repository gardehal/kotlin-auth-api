package grd.kotlin.authapi.dto

import io.swagger.v3.oas.annotations.media.Schema
import javax.validation.constraints.NotNull

data class ApiMetadataDto(
    @Schema(description = "Number of users in database")
    @NotNull
    var totalUsers: Int? = null,

    @Schema(description = "Updated as UTC")
    @NotNull
    var updatedTime: String? = null,
)
