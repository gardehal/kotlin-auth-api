package grd.kotlin.authapi.models

import java.time.Instant
import java.util.*
import javax.persistence.Entity
import javax.persistence.Id
import javax.validation.constraints.NotNull

@Entity
data class ApiMetadata(
    @JvmField
    @NotNull
    @Id
    var id: String = UUID.randomUUID().toString(),

    @JvmField
    @NotNull
    var added: String = Instant.now().toString(),

    @JvmField
    @NotNull
    var deleted: String? = null,

    @JvmField
    @NotNull
    var totalUsers: Int? = null,

    @JvmField
    @NotNull
    var updatedTime: String? = null,
)
