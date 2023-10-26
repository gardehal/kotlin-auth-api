package grd.kotlin.authapi.models

import com.chook.api.annotations.NotLogged
import com.chook.api.annotations.Sensitive
import com.chook.api.enums.UserRole
import java.time.Instant
import java.util.*
import javax.persistence.ElementCollection
import javax.persistence.Entity
import javax.persistence.Id
import javax.validation.constraints.Email
import javax.validation.constraints.NotNull
import javax.validation.constraints.Size

@Entity
data class AUser(
    @JvmField
    @NotNull
    @Id
    var id: String = UUID.randomUUID().toString(),

    @JvmField
    @NotNull
    var added: String = Instant.now().toString(), // Instance as String

    @JvmField
    var deleted: String? = null, // Instance as String

    @JvmField
    @NotNull
    @Sensitive
    @Email
    @Size(min = 6, max = 100)
    var email: String? = null,

    @JvmField
    @NotNull
    @Size(min = 2, max = 32)
    var username: String = id,

    @JvmField
    @NotNull
    @Sensitive
    var password: String = Instant.now().toString(), // As encrypted

    @JvmField
    @Size(max = 256)
    var about: String? = null,

    @JvmField
    @NotNull
    var role: UserRole = UserRole.USER,

    @JvmField
    var expirationTime: String? = null, // Instance as String

    @JvmField
    @NotNull
    var usersRecruited: Int = 0,

    @JvmField
    @NotNull
    @ElementCollection
    @NotLogged
    var moderatorComments: MutableMap<String, String> = mutableMapOf(),

    @JvmField
    var lastActiveTime: String? = null, // Instance as String

    @JvmField
    var lockedUntilTime: String? = null, // Instance as String

    @JvmField
    @NotNull
    @ElementCollection
    var previousUsernames: MutableMap<String, String> = mutableMapOf(),

    @JvmField
    @NotNull
    var usernameChangeTime: String? = null, // Instance as String

    @JvmField
    // @Size(max = 100) // Max size for base64?
    var profilePicture: String? = null,

    @JvmField
    var acceptedTerms: String? = null, // Instance as String
)
