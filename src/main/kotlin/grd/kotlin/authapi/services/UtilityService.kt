package grd.kotlin.authapi.services

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.github.fge.jsonpatch.JsonPatch
import com.github.fge.jsonpatch.JsonPatchException
import grd.kotlin.authapi.enums.*
import grd.kotlin.authapi.exceptions.*
import grd.kotlin.authapi.extensions.isNull
import org.springframework.stereotype.Service
import java.time.*
import java.util.*

@Service
class UtilityService
{
    /**
     * A regex capable of finding most number formats ("n", "n,n", "n,", "n.n", "n.", "n/n", "n/")
     * @return Regex for finding numbers
     * @throws none
     **/
    val numberRegex = """\d+[.,/]?\d*""".toRegex()

    /**
     * Returns Float (as "d.d") of value, including fractions
     * @param value Any value parsable to float
     * @return Float of [value]
     * @throws none
     **/
    fun floatOf(value: Any): Float?
    {
        val fractionRegex = """\d+/\d+""".toRegex()
        val split = value.toString().replace(",", ".").trim().split(" ")
        var result = 0F
        var isNull = true

        split.forEach {
            if(numberRegex.containsMatchIn(it))
            {
                isNull = false

                result += if(fractionRegex.containsMatchIn(it)) // String has a fraction, ex. 1/4
                {
                    val fraction = fractionRegex.find(it)!!.value.split("/")
                    fraction[0].toFloat().div(fraction[1].toFloat())
                }
                else
                    numberRegex.find(it)!!.value.toFloat()
            }
        }

        return if(isNull)
            null
        else
            result
    }

    /**
     * Validates ID for usage in database (not looking for existing in DB)
     * @param id String id to check
     * @return true = ID is ok to use | false = there was an issue with the ID
     * @throws none
     **/
    fun checkIdValidity(id: String?): Boolean
    {
        if(id == null || id.length < 8 || id.length > 64)
            return false

        // Illegal symbols: NOT a-zA-Z0-9
        val illegal = arrayListOf("[^a-zA-Z0-9-]+".toRegex())
        for(s in illegal)
            if(s.containsMatchIn(id))
                return false

        return true
    }

    /**
     * Generates a random string for IDs.
     * @return String like GUID
     * @throws none
     **/
    fun getRandomString(): String
    {
        return UUID.randomUUID().toString()
    }

    /**
     * Convert yyyy-MM-dd'T'HH:mm:ss.SSS'Z' String to Instant
     * @param datetimeString String datetime, either yyyy-MM-dd or yyyy-MM-dd'T'HH:mm:ss.SSS'Z'
     * @return Instant of string
     * @throws none
     **/
    fun stringToDatetime(datetimeString: String): Instant?
    {
        return try
        {
            var adjustedDatetimeString: String = datetimeString
            if(datetimeString.length < 11) // Only date, add midnight time
                adjustedDatetimeString += "T00:00:00.000Z"

            Instant.parse(adjustedDatetimeString)
        }
        catch(e: Exception)
        {
            e.printStackTrace()
            null
        }
    }

    /**
     * Uses reflection to find out if [fieldName] exists in [this]
     * @param this Object with potential field
     * @param fieldName Name of field to check
     * @return Boolean if field exists
     * @throws none
     **/
    fun hasField(parent: Any, fieldName: String): Boolean
    {
        return (parent as Class<*>).fields.map { it.toString().split(".").last() }.contains(fieldName)
    }

    /**
     * Uses reflection to get a field with name [fieldName] form object [parent]
     * @param parent Object with potential field name
     * @param fieldName Name of field to get
     * @return Any value or null
     * @throws NoSuchFieldException if field does not exist
     **/
    @Throws(NoSuchFieldException::class)
    fun getReflection(parent: Any, fieldName: String): Any?
    {
        return parent.javaClass.getDeclaredField(fieldName).get(parent)
    }

    /**
     * Uses reflection to set a field with name [fieldName] of object [parent]
     * @param parent object to change
     * @param fieldName field to set
     * @param newValue to set, nullable
     * @return none
     * @throws ArgumentException if value is not null and field and value types do not match
     **/
    fun setReflection(parent: Any, fieldName: String, newValue: Any?)
    {
        val oldValue = getReflection(parent, fieldName)
        if(newValue == oldValue)
            return

        if(oldValue != null && newValue != null && oldValue::class.java != newValue::class.java)
            throw ArgumentException("setReflection: The new value type does not match the field type (\"$fieldName\", \"$newValue\")")

        parent::class.java.getDeclaredField(fieldName).set(parent, newValue)
    }

    /**
     * Convert plain JSON to JsonPatch to use in merge patch
     * @param json to convert
     * @return JsonPatch converted
     * @throws ArgumentException on bad JSON
     **/
    @Throws(ArgumentException::class)
    fun convertJsonToJsonPatch(json: String, ignoreFields: Iterable<String>): JsonPatch
    {
        val mapper = ObjectMapper()
        val jsonMap: Map<String, String>
        try
        {
            @Suppress("UNCHECKED_CAST")
            jsonMap = mapper.readValue(json, Map::class.java) as Map<String, String>
        }
        catch(e: Exception)
        {
            e.printStackTrace()
            throw ArgumentException("Invalid JSON.")
        }

        val jsonPatchObjects = mutableListOf<String>()
        jsonMap.forEach { e ->
            if(e.key in ignoreFields)
                return@forEach

            jsonPatchObjects.add((getJsonPatch(JsonPatchOperation.REPLACE, e.key, e.value)))
        }

        val jsonPatchString = "[ ${jsonPatchObjects.joinToString(",")} ]"

        val jsonPatchNode: JsonNode
        try
        {
            jsonPatchNode = mapper.readValue(jsonPatchString, JsonNode::class.java)
        }
        catch(e: Exception)
        {
            e.printStackTrace()
            throw ArgumentException("Invalid JsonPatch.")
        }

        return JsonPatch.fromJson(jsonPatchNode)
    }

    /**
     * Create a JsonPatch formatted object
     * @param operation to perform
     * @param field to alter, has slash (/) included before any text
     * @param value to apply
     * @return String as JsonPatch object
     * @throws none
     **/
    fun getJsonPatch(operation: JsonPatchOperation, field: String, value: Any?): String
    {
        // """[{ "op": "replace", "path": "/name", "value": "$newValue" }]"""
        return """{ "op": "${operation.toString().lowercase()}", "path": "/$field", "value": "${value.toString()}" }"""
    }

    /**
     * Merge an [entity] and [jsonPatch]
     * @param entity to apply patch to
     * @param tClass class of T to convert merged object to
     * @param jsonPatch of changes to apply
     * @return Entity updated with changes, not saved to database
     * @throws JsonPatchException on jsonPatch errors
     **/
    @Throws(JsonPatchException::class)
    fun <T> mergePatch(entity: T, tClass: Class<T>, jsonPatch: JsonPatch): T
    {
        val mapper = ObjectMapper()
        val patched: JsonNode = jsonPatch.apply(mapper.convertValue(entity, JsonNode::class.java))
        return mapper.treeToValue(patched, tClass)
    }

    /**
     * Upper case the first letter in [String]
     * @param toEdit String to uppercase first of
     * @return String with uppercase first
     * @throws none
     **/
    // https://www.codevscolor.com/kotlin-capitalize-first-character-string
    fun uppercaseFirst(toEdit: String): String
    {
        return (toEdit[0].uppercaseChar() + toEdit.substring(1))
    }

    /**
     * Validate string
     * @param valueToCheck
     * @param allowNorwegianLetters Boolean allow norwegian letters a-åA-Å
     * @param allowNumbers Boolean allow numbers 0-9
     * @param allowSingleSpace Boolean allow single space
     * @param allowEmailSymbols Boolean allow email symbols ._@
     * @param minimumLength Int minimum length, if null: none
     * @param maximumLength Int maximum length, if null: none
     * @return Pair, Boolean true/false valid, String message
     * @throws none
     **/
    fun validateInput(
        valueToCheck: String?,
        allowNorwegianLetters: Boolean = false,
        allowNumbers: Boolean = false,
        allowSingleSpace: Boolean = false,
        allowEmailSymbols: Boolean = false,
        minimumLength: Int? = null,
        maximumLength: Int? = null,
    ): Pair<Boolean, String>
    {
        var value = valueToCheck
        if(value.isNullOrEmpty())
            return Pair(false, "Value is null or empty.")

        if(minimumLength != null && value.length < minimumLength)
            return Pair(false, "Value is too short, minimumLength: $minimumLength.")
        if(maximumLength != null && value.length > maximumLength)
            return Pair(false, "Value is too long, maximumLength: $maximumLength.")

        val norwegianLetters = """[a-zA-ZæøåÆØÅ]*""".toRegex()
        val numbers = """[0-9]*""".toRegex()
        val singleSpace = """ """.toRegex()
        val email = """[._@]*""".toRegex()

        if(allowNorwegianLetters)
            value = value.replace(norwegianLetters, "")
        if(allowNumbers)
            value = value.replace(numbers, "")
        if(allowSingleSpace)
            value = value.replace(singleSpace, "")
        if(allowEmailSymbols)
            value = value.replace(email, "")

        if(value.isEmpty())
            return Pair(true, "String is valid.")

        return Pair(false, "Value contains illegal characters: \"$value\".")
    }
    /**
     * Validate email
     * @param valueToCheck
     * @return Pair, Boolean true/false valid, String message
     * @throws none
     **/
    fun validateEmail(valueToCheck: String?): Pair<Boolean, String>
    {
        if(valueToCheck.isNullOrEmpty()) return Pair(false, "Value is null or empty.")

        val emailRegex = """(?:[a-z0-9!#${'$'}%&'*+/=?^_`{|}~-]+(?:\.[a-z0-9!#${'$'}%&'*+/=?^_`{|}~-]+)*|"(?:[\x01-\x08\x0b\x0c\x0e-\x1f\x21\x23-\x5b\x5d-\x7f]|\\[\x01-\x09\x0b\x0c\x0e-\x7f])*")@(?:(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?|\[(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?|[a-z0-9-]*[a-z0-9]:(?:[\x01-\x08\x0b\x0c\x0e-\x1f\x21-\x5a\x53-\x7f]|\\[\x01-\x09\x0b\x0c\x0e-\x7f])+))""".toRegex()

        if(valueToCheck.matches(emailRegex))
            return Pair(true, "Email is valid.")

        return Pair(false, "The email is invalidly formatted.")
    }

    /**
     * Combine two nullable float values, option to ignore null values.
     * @param a Float? to combine
     * @param b Float? to combine
     * @param ignoreNullValues bool for ignoring ArgumentException conditions. When true, value of items will then not be entirely accurate
     * @return Float? result
     * @throws ArgumentException when ANY nutritional values are null and ignoreNullValues is false
     **/
    @Throws(ArgumentException::class)
    fun accumulate(a: Float?, b: Float?, ignoreNullValues: Boolean): Float?
    {
        if(!ignoreNullValues && (a.isNull() || b.isNull()))
            throw ArgumentException("A variable has no value, the result will not be accurate. You can ignore this error by ignoring null.")

        return (a ?: 0F) + (b ?: 0F)
    }
}
