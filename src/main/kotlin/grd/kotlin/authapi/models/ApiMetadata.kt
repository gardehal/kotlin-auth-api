package grd.kotlin.authapi.models

import jakarta.persistence.Entity
import jakarta.persistence.Table
import jakarta.validation.constraints.NotNull
import java.time.Instant
import java.util.*

@Entity
@Table(name = "API_METADATA")
data class ApiMetadata(
    @JvmField
    @NotNull
    var id: String = UUID.randomUUID().toString(),

    @JvmField
    @NotNull
    var added: String = Instant.now().toString(),

    @JvmField
    var deleted: String? = null,

    @JvmField
    var totalUsers: Int? = null,

    @JvmField
    var updatedTime: String? = null,
)
