package grd.kotlin.authapi.logging

import grd.kotlin.authapi.annotations.NotLogged
import javax.persistence.Entity
import javax.persistence.Id
import javax.validation.constraints.NotNull

@Entity
@NotLogged
data class LogEvent(
    @JvmField
    @NotNull
    @Id
    var id: String? = null,

    @JvmField
    @NotNull
    var registered: String? = null,

    @JvmField
    @NotNull
    var event: String? = null,

    @JvmField
    var userId: String? = null,

    @JvmField
    var itemId: String? = null,

    @JvmField
    var caller: String? = null,
)
