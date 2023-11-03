package grd.kotlin.authapi

import io.klogging.logger

object Log
{
    val main = logger("main")

    /**
     * {function}, {code}, {message}
     */
    fun controllerMsg(): String
    {
        return "{function}, {code}, {message}"
    }

    /**
     * {function}, {code}, {message}, {editor}
     */
    fun controllerEditorMsg(): String
    {
        return "{function}, {code}, {message}, {editor}"
    }
}