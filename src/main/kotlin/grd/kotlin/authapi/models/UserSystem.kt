package grd.kotlin.authapi.models

import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import jakarta.validation.constraints.NotNull
import java.time.Instant
import java.util.*

@Entity
@Table(name = "USER_SYSTEM")
data class UserSystem(
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
    @NotNull
    var systemId: String,

    @JvmField
    @NotNull
    var userId: String,

    @JvmField
    @NotNull
    var chmod: String = "000",
)
