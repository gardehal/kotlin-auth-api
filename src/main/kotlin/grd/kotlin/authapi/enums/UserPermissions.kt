package grd.kotlin.authapi.enums

import com.fasterxml.jackson.annotation.JsonValue

enum class UserPermissions(@JsonValue val value: Int)
{
    NONE(0),
    CREATE(1),
    READ_SINGLE(2),
    READ_LIST(3),
    UPDATE(4),
    DELETE(5),
    RESTORE(6),
    PURGE(7),
    ;

    companion object
    {
        fun getByValue(value: Int) = values().firstOrNull { it.value == value }
    }
}
