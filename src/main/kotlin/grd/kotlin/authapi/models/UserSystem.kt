package grd.kotlin.authapi.models

import java.time.Instant
import java.util.*

class UserSystem(
    @JvmField
    var id: String = UUID.randomUUID().toString(),

    @JvmField
    var added: String = Instant.now().toString(), // Instant as String

    @JvmField
    var deleted: String? = null, // Instant as String

    @JvmField
    var systemId: String,

    @JvmField
    var userId: String,
)