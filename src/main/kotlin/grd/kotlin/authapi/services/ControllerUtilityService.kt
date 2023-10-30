package grd.kotlin.authapi.services

import grd.kotlin.authapi.Log
import grd.kotlin.authapi.enums.UserRole
import grd.kotlin.authapi.exceptions.*
import grd.kotlin.authapi.extensions.isNotNull
import grd.kotlin.authapi.models.HttpReturn
import org.springframework.stereotype.Service
import java.net.MalformedURLException

@Service
class ControllerUtilityService
{
    val minimumStaffRole = UserRole.MODERATOR
    val missingTokenErrorMessage = "This operation requires a JWT token."
    val notAuthorizedMessage = "User lacks privileges for this operation."
    val serverErrorMessage = "Internal server error."

    /**
     * Create an HTTP return with code and message depending on the exception thrown.
     * @param exception Exception that was thrown
     * @param callerContext String? (this.toString()) of caller function. If this parameter is null, no logging will be done.
     * @param editorId String? ID of the editor
     * @param notFoundCode Int code to return when exception is NotFoundException
     * @return HttpReturn containing code and message
     * @throws none
     **/
    suspend fun getErrorHttpReturn(exception: Exception, callerContext: String?, editorId: String? = null, notFoundCode: Int = 400): HttpReturn
    {
        var code = 500
        var message = exception.message.toString()
        when(exception)
        {
            is NotFoundException ->
                code = notFoundCode
            is ArgumentException, is DuplicateException, is MalformedURLException ->
                code = 400
            is NotAuthorizedException ->
                code = 401
            is DatabaseErrorException, is LoggingException, is TestEnvironmentException, is UnexpectedStateException ->
                code = 500
            is NotImplementedException ->
                code = 501
            else ->
            {
                message = serverErrorMessage
                if(callerContext.isNotNull())
                    Log.main.info("Returning generic error message to user: {message}, {exceptionMessage}, {function}", message, exception.message.toString(), callerContext)
            }
        }

        if(callerContext.isNotNull())
            Log.main.info("{function}, {editor}, {code}, {message}, {id}", callerContext, editorId, code, message, null)
        return HttpReturn(code, message)
    }
}
