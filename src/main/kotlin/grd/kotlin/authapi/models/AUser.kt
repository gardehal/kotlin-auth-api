package grd.kotlin.authapi.models

import grd.kotlin.authapi.annotations.NotLogged
import grd.kotlin.authapi.annotations.Sensitive
import grd.kotlin.authapi.enums.UserRole
import java.time.Instant
import java.util.*

data class AUser(
    @JvmField
    var id: String = UUID.randomUUID().toString(),

    @JvmField
    var added: String = Instant.now().toString(), // Instant as String

    @JvmField
    var deleted: String? = null, // Instant as String

    @JvmField
    @Sensitive
    var email: String? = null,

    @JvmField
    var username: String = id,

    @JvmField
    @Sensitive
    var password: String, // As encrypted

    @JvmField
    var about: String? = null,

    @JvmField
    var role: UserRole = UserRole.USER,

    @JvmField
    var expirationTime: String? = null, // Instant as String

    @JvmField
    var usersRecruited: Int = 0,

    @JvmField
    @NotLogged
    var moderatorComments: MutableMap<String, String> = mutableMapOf(),

    @JvmField
    var lastActiveTime: String? = null, // Instant as String

    @JvmField
    var lockedUntilTime: String? = null, // Instant as String

    @JvmField
    var previousUsernames: MutableMap<String, String> = mutableMapOf(),

    @JvmField
    var usernameChangeTime: String? = null, // Instant as String

    @JvmField
    // @Size(max = 100) // Max size for base64?
    var profilePicture: String? = null,

    @JvmField
    var acceptedTerms: String? = null, // Instant as String
)
