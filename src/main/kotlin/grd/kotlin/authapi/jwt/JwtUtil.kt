package grd.kotlin.authapi.jwt

import grd.kotlin.authapi.Log
import grd.kotlin.authapi.enums.UserRole
import grd.kotlin.authapi.exceptions.ArgumentException
import grd.kotlin.authapi.models.AUser
import grd.kotlin.authapi.settings.Settings
import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import java.io.Serializable
import java.util.*
import java.util.function.Function

@Component
class JwtUtil : Serializable
{
    @Autowired
    private lateinit var settings: Settings

    val idFieldName = "USERID"
    val usernameFieldName = "USERNAME"
    val roleFieldName = "ROLE"

    fun getIdFromToken(token: String): String
    {
        return getClaimFromToken(token) { obj: Claims -> obj.subject }
    }

    suspend fun getUsernameFromToken(token: String): String
    {
        return getCustomTokenClaim(token, usernameFieldName)
    }

    suspend fun getRoleFromToken(token: String): UserRole
    {
        val roleValue = getCustomTokenClaim(token, roleFieldName).toInt()
        return UserRole.getByValue(roleValue)?: throw ArgumentException("Invalid role, value: $roleValue")
    }

    suspend fun getCustomTokenClaim(token: String, key: String): String
    {
        val claims: Claims? = try
        {
            Jwts.parser()
                .setSigningKey(settings.env.jwtSign)
                .parseClaimsJws(token)
                .body
        }
        catch(e: Exception)
        {
            Log.main.info("Failed to get custom token claims, {message} , {function}", e.message, this.toString())
            throw ArgumentException("Invalid JWT token.")
        }
        return claims!![key].toString()
    }

    fun getIssuedAtDateFromToken(token: String?): Date
    {
        return getClaimFromToken(token) { obj: Claims -> obj.issuedAt }
    }

    fun getExpirationDateFromToken(token: String?): Date
    {
        return getClaimFromToken(token) { obj: Claims -> obj.expiration }
    }

    fun <T> getClaimFromToken(token: String?, claimsResolver: Function<Claims, T>): T
    {
        val claims = getAllClaimsFromToken(token)
        return claimsResolver.apply(claims)
    }

    fun getAllClaimsFromToken(token: String?): Claims
    {
        return Jwts.parser().setSigningKey(settings.env.jwtSign!!).parseClaimsJws(token).body
    }

    private fun isTokenExpired(token: String): Boolean
    {
        val expiration = getExpirationDateFromToken(token)
        return expiration.before(Date())
    }

    fun generateToken(user: AUser): String
    {
        val claims: MutableMap<String, String> = mutableMapOf()
        claims[idFieldName] = user.id
        claims[usernameFieldName] = user.username
        claims[roleFieldName] = (user.role).toString()

        return doGenerateToken(claims, user.id)
    }

    private fun doGenerateToken(claims: Map<String, Any>, subject: String): String
    {
        return Jwts.builder()
            .setIssuer(settings.jwt.issuer!!)
            .setClaims(claims)
            .setSubject(subject)
            .setIssuedAt(Date(System.currentTimeMillis()))
            .setExpiration(Date(System.currentTimeMillis() + settings.jwt.validity!!.toLong()))
            .signWith(SignatureAlgorithm.HS512, settings.env.jwtSign!!)
            .compact()
    }

    suspend fun validateToken(token: String, user: AUser): Boolean
    {
        val username = getUsernameFromToken(token)
        return username == user.username && !isTokenExpired(token)
    }
}
