package grd.kotlin.authapi.logging

import grd.kotlin.authapi.annotations.NotLogged

@NotLogged
data class LogEvent(
    @JvmField
    var id: String? = null,

    @JvmField
    var registered: String? = null,

    @JvmField
    var event: String? = null,

    @JvmField
    var userId: String? = null,

    @JvmField
    var itemId: String? = null,

    @JvmField
    var caller: String? = null,
)
