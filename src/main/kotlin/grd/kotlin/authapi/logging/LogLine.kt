package grd.kotlin.authapi.logging

import grd.kotlin.authapi.annotations.NotLogged

@NotLogged
data class LogLine(
    @JvmField
    var id: String? = null,

    @JvmField
    var logHeadId: String? = null,

    @JvmField
    var fieldName: String? = null,

    @JvmField
    var oldValue: String? = null,

    @JvmField
    var newValue: String? = null,
)
