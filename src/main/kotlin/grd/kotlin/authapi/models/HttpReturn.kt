package grd.kotlin.authapi.models

data class HttpReturn(
    var code: Int,
    var message: String? = null,
)
