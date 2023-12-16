package grd.kotlin.authapi.models

import grd.kotlin.authapi.annotations.NotLogged
import grd.kotlin.authapi.annotations.Sensitive
import grd.kotlin.authapi.enums.UserRole
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size
import java.time.Instant
import java.util.*

@Entity
@Table(name = "A_USER")
data class AUser(
    @JvmField
    @NotNull
    @Id
    var id: String = UUID.randomUUID().toString(),

    @JvmField
    @NotNull
    var added: String = Instant.now().toString(), // Instant as String

    @JvmField
    var deleted: String? = null, // Instant as String

    @JvmField
    @Size(max = 100)
    @Sensitive
    var email: String? = null,

    @JvmField
    @NotNull
    @Size(max = 100)
    var username: String = id,

    @JvmField
    @NotNull
    @Sensitive
    var password: String, // As encrypted

    @JvmField
    @Size(max = 1000)
    var about: String? = null,

    @JvmField
    @NotNull
    var role: UserRole = UserRole.USER,

    @JvmField
    var expirationTime: String? = null, // Instant as String

    @JvmField
    @NotNull
    var usersRecruited: Int = 0,

    @JvmField
    @NotNull
    @NotLogged
    var moderatorComments: MutableMap<String, String> = mutableMapOf(),

    @JvmField
    var lastActiveTime: String? = null, // Instant as String

    @JvmField
    var lockedUntilTime: String? = null, // Instant as String

    @JvmField
    @NotNull
    var previousUsernames: MutableMap<String, String> = mutableMapOf(),

    @JvmField
    var usernameChangeTime: String? = null, // Instant as String

    @JvmField
    @Size(max = 1000) // Max size for base64?
    var profilePicture: String? = null,

    @JvmField
    var acceptedTerms: String? = null, // Instant as String
)
