package grd.kotlin.authapi.logging

import com.fasterxml.jackson.annotation.JsonValue

enum class LogOperation(@JsonValue val value: Int)
{
    ADDED(1),
    EDITED(2),
    DELETED(3), // Soft deleted, no used
    REMOVED(4), // Permanently deleted
    ;

    companion object
    {
        fun getByValue(value: Int) = values().firstOrNull { it.value == value }
    }
}
