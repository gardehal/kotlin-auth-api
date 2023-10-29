package grd.kotlin.authapi.logging

data class JsonFileLog(
    var logEvents: MutableList<LogEvent>? = null,
    var logHeads: MutableList<LogHead>? = null,
)
{
    fun initialize(): JsonFileLog
    {
        this.logEvents = mutableListOf()
        this.logHeads = mutableListOf()
        return this
    }
}
