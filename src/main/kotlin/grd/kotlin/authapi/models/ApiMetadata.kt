package grd.kotlin.authapi.models

import java.time.Instant
import java.util.*

data class ApiMetadata(
    @JvmField
    var id: String = UUID.randomUUID().toString(),

    @JvmField
    var added: String = Instant.now().toString(),

    @JvmField
    var deleted: String? = null,

    @JvmField
    var totalUsers: Int? = null,

    @JvmField
    var updatedTime: String? = null,
)
