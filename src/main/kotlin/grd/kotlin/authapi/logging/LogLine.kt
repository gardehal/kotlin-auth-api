package grd.kotlin.authapi.logging

import grd.kotlin.authapi.annotations.NotLogged
import javax.persistence.Entity
import javax.persistence.Id
import javax.validation.constraints.NotNull

@Entity
@NotLogged
data class LogLine(
    @JvmField
    @NotNull
    @Id
    var id: String? = null,

    @JvmField
    @NotNull
    var logHeadId: String? = null,

    @JvmField
    @NotNull
    var fieldName: String? = null,

    @JvmField
    @NotNull
    var oldValue: String? = null,

    @JvmField
    @NotNull
    var newValue: String? = null,
)
