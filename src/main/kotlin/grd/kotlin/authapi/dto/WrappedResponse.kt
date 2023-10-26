package grd.kotlin.authapi.dto

import io.swagger.v3.oas.annotations.media.Schema

/**
 * Wrapper DTO for REST responses.
 *
 * Somehow based on JSend : https://labs.omniti.com/labs/jsend
 */
open class WrappedResponse<T>(
    @Schema(description = "The HTTP status code of the response")
    var code: Int? = null,

    @Schema(description = "The wrapped payload")
    var data: T? = null,

    @Schema(description = "Error message in case where was an error")
    var message: String? = null,

    @Schema(description = "String representing either 'success', user error ('error') or server failure ('fail')")
    var status: ResponseStatus? = null,
)
{
    /**
     * Useful method when marshalling from Kotlin to JSON.
     * Will set the "status" if missing, based on "code".
     *
     * Note: validation is not done on constructor because, when unmarshalling
     * from JSON, the empty constructor is called, and then only afterwards
     * the fields are set with method calls
     *
     * @throws IllegalStateException if validation fails
     */
    fun validated(): WrappedResponse<T>
    {

        val c: Int = code ?: throw IllegalStateException("Missing HTTP code")

        if(c !in 100..599)
        {
            throw  IllegalStateException("Invalid HTTP code: $code")
        }

        if(status == null)
        {
            status = when(c)
            {
                in 100..399 -> ResponseStatus.SUCCESS
                in 400..499 -> ResponseStatus.ERROR
                in 500..599 -> ResponseStatus.FAIL
                else -> throw  IllegalStateException("Invalid HTTP code: $code")
            }
        }
        else
        {
            val wrongSuccess = (status == ResponseStatus.SUCCESS && c !in 100..399)
            val wrongError = (status == ResponseStatus.ERROR && c !in 400..499)
            val wrongFail = (status == ResponseStatus.FAIL && c !in 500..599)

            val wrong = wrongSuccess || wrongError || wrongFail
            if(wrong)
            {
                throw IllegalArgumentException("Status $status is not correct for HTTP code $c")
            }
        }

        if(status != ResponseStatus.SUCCESS && message == null)
        {
            throw IllegalArgumentException("Failed response, but with no describing 'message' for it")
        }

        return this
    }

    enum class ResponseStatus(val value: Int)
    {
        INFO(1),
        SUCCESS(2),
        REDIRECT(3),
        FAIL(4),
        ERROR(5),
    }
}
