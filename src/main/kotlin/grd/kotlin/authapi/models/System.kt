package grd.kotlin.authapi.models

import jakarta.persistence.Entity
import jakarta.persistence.Table
import jakarta.validation.constraints.NotNull
import java.time.Instant
import java.util.*

@Entity
@Table(name = "SYSTEM")
data class System(
    @JvmField
    @NotNull
    var id: String = UUID.randomUUID().toString(),

    @JvmField
    @NotNull
    var added: String = Instant.now().toString(), // Instant as String

    @JvmField
    var deleted: String? = null, // Instant as String

    @JvmField
    @NotNull
    var name: String,

    @JvmField
    @NotNull
    var active: Boolean,
)
