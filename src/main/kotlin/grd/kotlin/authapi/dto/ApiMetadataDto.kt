package grd.kotlin.authapi.dto

import io.swagger.v3.oas.annotations.media.Schema

data class ApiMetadataDto(
    @Schema(description = "Number of users in database")
    var totalUsers: Int? = null,

    @Schema(description = "Updated as UTC")
    var updatedTime: String? = null,
)
