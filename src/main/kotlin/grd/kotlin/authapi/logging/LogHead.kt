package grd.kotlin.authapi.logging

import grd.kotlin.authapi.annotations.NotLogged
import javax.persistence.ElementCollection
import javax.persistence.Entity
import javax.persistence.Id
import javax.validation.constraints.NotNull

@Entity
@NotLogged
data class LogHead(
    @JvmField
    @NotNull
    @Id
    var id: String? = null,

    @JvmField
    @NotNull
    var operation: LogOperation? = null,

    @JvmField
    @NotNull
    var itemType: LogItemType? = null,

    @JvmField
    @NotNull
    var itemId: String? = null,

    @JvmField
    @NotNull
    var registered: String? = null,

    @JvmField
    @NotNull
    var editorId: String? = null,

    @JvmField
    @NotNull
    var automatedChange: Boolean? = null,

    @JvmField
    @NotNull
    var fieldsUpdated: String? = null,

    @JvmField
    @NotNull
    var comment: String? = null,

    @JvmField
    @NotNull
    @ElementCollection
    var logLines: MutableList<LogLine>? = null,
)
