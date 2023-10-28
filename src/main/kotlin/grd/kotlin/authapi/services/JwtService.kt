package grd.kotlin.authapi.services

import com.chook.api.exceptions.ArgumentException
import com.chook.api.exceptions.NotFoundException
import com.chook.api.models.ChookUser
import com.chook.api.repositories.FirebaseRepository
import com.google.cloud.firestore.Query
import org.springframework.stereotype.Service

@Service
class JwtService
{
    private  var chookUserRepository = FirebaseRepository(ChookUser::class.java)

    /**
     * Find user by email or username
     * @param username String user name
     * @param email String user email
     * @return User user
     * @throws ArgumentException if missing both arguments
     * @throws NotFoundException if user not found
     **/
    @Throws(ArgumentException::class, NotFoundException::class)
    fun findByUsernameEmail(username: String?, email: String? = null): ChookUser
    {
        var users = emptyList<ChookUser>()
        if(username.isNullOrEmpty() && email.isNullOrEmpty())
            throw ArgumentException("Enter a username or an email")
        else if(!username.isNullOrEmpty())
            users = chookUserRepository.getQueried { e: Query -> e.whereEqualTo("username", username) }
        else if(!email.isNullOrEmpty())
            users = chookUserRepository.getQueried { e: Query -> e.whereEqualTo("email", email) }

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