package grd.kotlin.authapi.enums

import com.fasterxml.jackson.annotation.JsonValue

enum class JsonPatchOperation(@JsonValue val value: Int)
{
    // https://jsonpatch.com/#operations

    ADD(1),
    REMOVE(2),
    REPLACE(3),
    COPY(4),
    MOVE(5),
    TEST(6),
    ;

    companion object
    {
        fun getByValue(value: Int) = values().firstOrNull { it.value == value }
    }
}
