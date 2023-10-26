package grd.kotlin.authapi.enums

import com.fasterxml.jackson.annotation.JsonValue

enum class UserRole(@JsonValue val value: Int)
{
    OTHER(1),
    BOT(2),
    USER(3),
    SUPERUSER(4),
    MODERATOR(5),
    ADMINISTRATOR(6),
    DEVELOPER(7),
    OWNER(8),
    ;

    companion object
    {
        fun getByValue(value: Int) = values().firstOrNull { it.value == value }
    }
}
