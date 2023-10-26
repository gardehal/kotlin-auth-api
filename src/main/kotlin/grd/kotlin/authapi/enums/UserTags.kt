package grd.kotlin.authapi.enums

import com.fasterxml.jackson.annotation.JsonValue

enum class UserTags(@JsonValue val value: Int)
{
    PAYING_MEMBER(1),
    BOT(2),
    INACTIVE(3),
    AUTO_ACCEPT_REGISTRATIONS(4),
    ;

    companion object
    {
        fun getByValue(value: Int) = values().firstOrNull { it.value == value }
    }
}
