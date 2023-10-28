package grd.kotlin.authapi.services

import grd.kotlin.authapi.enums.UserRole
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class EnumService
{
    @Autowired
    private lateinit var utilityService: UtilityService

    /**
     * Assemble regex for matching on QuantityUnit like "1 L water" and "1L water"
     * @param contains The string of QuantityUnit to search for
     * @return If contains is "contains", regex of "[getNumberRegex()|\s]contains\s"
     * @throws none
     **/
    private fun getUnitRegex(contains: String): Regex
    {
        return ("[" + utilityService.numberRegex.toString() + "|\\s]" + contains + "\\s").toRegex()
    }

    /**
     * Attempt to find corresponding UserRole of [fromValue], first with common words and phrases, then using generic words with contains if [useContain]
     * @param fromValue Any value to find enum in
     * @param useContain true = use contains for more generic search | false = only use specific phrases
     * @return UserRole if value can be found, otherwise null
     * @throws none
     **/
    fun mapUserRoleValue(fromValue: Any, useContain: Boolean = true): UserRole?
    {
        return when(val from = fromValue.toString().trim().lowercase())
        {
            UserRole.OTHER.value.toString(), UserRole.OTHER.name.lowercase(), "", "annet", "annen" ->
                UserRole.OTHER
            UserRole.BOT.value.toString(), UserRole.BOT.name.lowercase(), "robot", "script", "skript" ->
                UserRole.BOT
            UserRole.USER.value.toString(), UserRole.USER.name.lowercase() ->
                UserRole.USER
            UserRole.SUPERUSER.value.toString(), UserRole.SUPERUSER.name.lowercase(), "su" ->
                UserRole.SUPERUSER
            UserRole.MODERATOR.value.toString(), UserRole.MODERATOR.name.lowercase(), "mod" ->
                UserRole.MODERATOR
            UserRole.ADMINISTRATOR.value.toString(), UserRole.ADMINISTRATOR.name.lowercase(), "admin" ->
                UserRole.ADMINISTRATOR
            UserRole.DEVELOPER.value.toString(), UserRole.DEVELOPER.name.lowercase(), "dev", "utvikler", "utv" ->
                UserRole.DEVELOPER
            UserRole.OWNER.value.toString(), UserRole.OWNER.name.lowercase() ->
                UserRole.OWNER
            else -> // Else, use contains to find a more general spectrum
            {
                if(!useContain)
                    null
                else
                    mapUserRoleContains(from)
            }
        }
    }

    /**
     * Attempt to find corresponding UserRole of [fromValue] using generic words with contains
     * @param fromValue Any value to find enum in
     * @return UserRole if value can be found, otherwise null
     * @throws none
     **/
    fun mapUserRoleContains(fromValue: Any): UserRole?
    {
        val from = fromValue.toString().trim().lowercase()
        return when
        {
            from.contains("other") || from.contains("annen") ->
                UserRole.OTHER
            from.contains("bot") ->
                UserRole.BOT
            (!from.contains("super") && from.contains("user")) -> // Default "user" to this, not SuperUser
                UserRole.USER
            from.contains("super") ->
                UserRole.SUPERUSER
            from.contains("mod") ->
                UserRole.MODERATOR
            from.contains("admin") ->
                UserRole.ADMINISTRATOR
            from.contains("dev") || from.contains("utv") ->
                UserRole.DEVELOPER
            from.contains("owner") || from.contains("eier") ->
                UserRole.OWNER
            else ->
                null
        }
    }
}
