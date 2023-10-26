package grd.kotlin.authapi.dto

import com.chook.api.enums.UserRole
import io.swagger.v3.oas.annotations.media.Schema
import javax.validation.constraints.NotNull

data class AUserDto(
    @Schema(description = "ID")
    @NotNull
    var id: String? = null,

    @Schema(description = "Email")
    @NotNull
    var email: String? = null,

    @Schema(description = "Username to display")
    @NotNull
    var username: String? = null,

    @Schema(description = "A short summary about the user")
    var about: String? = null,

    @Schema(description = "Users designated role")
    @NotNull
    var role: UserRole? = null,

    @Schema(description = "Datetime until account is permanently closed")
    var expirationTime: String? = null,

    @Schema(description = "Number of other users this user has recruited")
    @NotNull
    var usersRecruited: Int? = null,

    @Schema(description = "DateTime user last active")
    var lastActiveTime: String? = null,

    @Schema(description = "Datetime account is banned until")
    var lockedUntilTime: String? = null,

    @Schema(description = "A map of DateTime username changed and from what username")
    var previousUsernames: MutableMap<String, String>? = null,

    @Schema(description = "Users profile picture as Base64")
    var profilePicture: String? = null,
)
