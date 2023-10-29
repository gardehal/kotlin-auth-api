package grd.kotlin.authapi.logging

import com.fasterxml.jackson.annotation.JsonValue

enum class LogSink(@JsonValue val value: Int)
{
    TEXT_FILE(1),
    JSON(2),
    DEFINED(3),
    ;

    companion object
    {
        fun getByValue(value: Int) = values().firstOrNull { it.value == value }
    }
}
