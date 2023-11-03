package grd.kotlin.authapi.logging

import grd.kotlin.authapi.annotations.NotLogged

@NotLogged
data class LogHead(
    @JvmField
    var id: String? = null,

    @JvmField
    var operation: LogOperation? = null,

    @JvmField
    var itemType: LogItemType? = null,

    @JvmField
    var itemId: String? = null,

    @JvmField
    var registered: String? = null,

    @JvmField
    var editorId: String? = null,

    @JvmField
    var automatedChange: Boolean? = null,

    @JvmField
    var fieldsUpdated: String? = null,

    @JvmField
    var comment: String? = null,

    @JvmField
    var logLines: MutableList<LogLine>? = null,
)
