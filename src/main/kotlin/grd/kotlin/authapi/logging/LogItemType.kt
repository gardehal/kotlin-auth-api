package grd.kotlin.authapi.logging

import com.fasterxml.jackson.annotation.JsonValue

enum class LogItemType(@JsonValue val value: Int)
{
    DB_METADATA(1),
    USER(2),
    ;

    companion object
    {
        fun getByValue(value: Int) = values().firstOrNull { it.value == value }
    }
}
