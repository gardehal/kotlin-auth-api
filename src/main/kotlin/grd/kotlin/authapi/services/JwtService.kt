package grd.kotlin.authapi.services

import com.google.cloud.firestore.Query
import grd.kotlin.authapi.exceptions.ArgumentException
import grd.kotlin.authapi.exceptions.NotFoundException
import grd.kotlin.authapi.models.AUser
import grd.kotlin.authapi.repositories.PostgresRepository
import org.springframework.stereotype.Service

@Service
class JwtService
{
    private  var aUserRepository = PostgresRepository(AUser::class.java)

    /**
     * Find user by email or username
     * @param username String user name
     * @param email String user email
     * @return User user
     * @throws ArgumentException if missing both arguments
     * @throws NotFoundException if user not found
     **/
    @Throws(ArgumentException::class, NotFoundException::class)
    fun findByUsernameEmail(username: String?, email: String? = null): AUser
    {
        var users = emptyList<AUser>()
        if(username.isNullOrEmpty() && email.isNullOrEmpty())
            throw ArgumentException("Enter a username or an email")
        else if(!username.isNullOrEmpty())
            users = aUserRepository.getQueried { e: Query -> e.whereEqualTo("username", username) }
        else if(!email.isNullOrEmpty())
            users = aUserRepository.getQueried { e: Query -> e.whereEqualTo("email", email) }

        when
        {
            users.size > 1 -> // Log error and send email
                throw NotFoundException("Internal error, duplicates found. Please alert a moderator")
            users.isEmpty() ->
                throw NotFoundException("No users found with that " + (if(!username.isNullOrEmpty()) "username" else "email"))
            else ->
                return users.first()
        }
    }
}