package grd.kotlin.authapi.extensions

import grd.kotlin.authapi.enums.*
import java.time.*
import java.util.*

class StringExtensions

/**
 * Join strings with separator.
 * @param this String? to append to
 * @param separator String separator
 * @param toJoin Strings to join
 * @return String of joined values
 **/
fun String?.join(separator: String, toJoin: String?): String?
{
    if(this.isNullOrEmpty())
        return toJoin
    else if(toJoin.isNullOrEmpty())
        return this

    return this.plus(separator).plus(toJoin)
}

/**
 * Join strings with separator and null checks.
 * @param this List of Strings to join
 * @param separator String separator
 * @return String of joined values
 **/
fun Iterable<String?>.join(separator: String): String?
{
    val list = this.filter { !it.isNullOrEmpty() }
    if(list.isEmpty())
        return null

    return list.joinToString(separator)
}

/**
 * Replace every character except first and last with star (*).
 * @param this String to censure
 * @return String censured result or null if input is null or empty
 */
fun String?.censureString(): String?
{
    if(this.isNullOrEmpty())
        return null

    return this.replace("(?<!^).(?!\$)".toRegex(), "*")
}

/**
 * Replace every character except first and last with star (*), leaving the email domain intact.
 * @param this String email address to censure
 * @return String censured result or null if input is null or empty
 */
fun String?.censureEmail(): String?
{
    if(this.isNullOrEmpty())
        return null

    return this.replace("(?<!^).(?=.*.@)".toRegex(), "*")
}

/**
 * Return the [this] string with a max length of [length]. Uses the shortest length of [this].length and [length].
 * @param this String value
 * @param length Int desired max length
 * @return String [this] with a maximum length of [length]
 */
fun String?.withMaxLength(length: Int): String?
{
    if(this.isNullOrEmpty())
        return null

    return this.substring(0, Math.min(this.length, length))
}
