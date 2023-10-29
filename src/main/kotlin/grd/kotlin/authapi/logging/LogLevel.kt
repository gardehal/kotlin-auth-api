package grd.kotlin.authapi.logging

import com.fasterxml.jackson.annotation.JsonValue

enum class LogLevel(@JsonValue val value: Int)
{
    DISABLED(0),
    NONE(0),
    TRACE(1),
    TRACING(1),
    DEBUG(2),
    INFO(3),
    INFORMATION(3),
    WARNING(4),
    ERROR(5),
    CRITICAL(6),
    FATAL(6),
    ;

    companion object
    {
        fun getByValue(value: Int) = values().firstOrNull { it.value == value }
    }
}
