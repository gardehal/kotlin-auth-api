package grd.kotlin.authapi.settings

import grd.kotlin.authapi.Log
import io.github.cdimascio.dotenv.dotenv
import kotlinx.coroutines.runBlocking
import org.springframework.stereotype.Component
import java.io.File
import java.nio.file.Paths

@Component
class Env
{
    var firebaseUrl: String? = null
    var firebaseType: String? = null
    var firebasePId: String? = null
    var firebasePKeyId: String? = null
    var firebasePKey: String? = null
    var firebaseCEmail: String? = null
    var firebaseCId: String? = null
    var firebaseAUri: String? = null
    var firebaseTUri: String? = null
    var firebaseAuthUrl: String? = null
    var firebaseClientUrl: String? = null

    var jwtSign: String? = null

    fun initialize(resourceDirectory: String) = runBlocking {
        val primaryEnvPath = Paths.get("src", resourceDirectory, "resources", ".env").toString()
        val secondaryEnvPath = Paths.get("config", ".env").toString()
        var envFile = File(primaryEnvPath)

        if(!envFile.exists())
        {
            Log.main.info("primaryEnvPath file not found, checking secondaryEnvPath..., {function}", this.toString())
            envFile = File(secondaryEnvPath)
        }

        val dotEnv = if(envFile.exists()) dotenv() else null
        Log.main.info("Attempting to use .env file, {result}, {function}", envFile.exists(), this.toString())
        Log.main.info("Attempting to use System.getenv(), {result}, {function}", !envFile.exists(), this.toString())

        if(dotEnv != null)
        {
            firebaseUrl = dotEnv["FIREBASE_URL"]!!
            firebaseType = dotEnv["FIREBASE_TYPE"]!!
            firebasePId = dotEnv["FIREBASE_PROJECT_ID"]!!
            firebasePKeyId = dotEnv["FIREBASE_PRIVATE_KEY_ID"]!!
            firebasePKey = dotEnv["FIREBASE_PRIVATE_KEY"]!!
            firebaseCEmail = dotEnv["FIREBASE_CLIENT_EMAIL"]!!
            firebaseCId = dotEnv["FIREBASE_CLIENT_ID"]!!
            firebaseAUri = dotEnv["FIREBASE_AUTH_URI"]!!
            firebaseTUri = dotEnv["FIREBASE_TOKEN_URI"]!!
            firebaseAuthUrl = dotEnv["FIREBASE_AUTH_PROVIDER_X509_CERT_URL"]!!
            firebaseClientUrl = dotEnv["FIREBASE_CLIENT_X509_CERT_URL"]!!

            jwtSign = dotEnv["JWT_SIGN"]!!
        }
        else
        {
            firebaseUrl = System.getenv("FIREBASE_URL")
            firebaseType = System.getenv("FIREBASE_TYPE")
            firebasePId = System.getenv("FIREBASE_PROJECT_ID")
            firebasePKeyId = System.getenv("FIREBASE_PRIVATE_KEY_ID")
            firebasePKey = System.getenv("FIREBASE_PRIVATE_KEY")
            firebaseCEmail = System.getenv("FIREBASE_CLIENT_EMAIL")
            firebaseCId = System.getenv("FIREBASE_CLIENT_ID")
            firebaseAUri = System.getenv("FIREBASE_AUTH_URI")
            firebaseTUri = System.getenv("FIREBASE_TOKEN_URI")
            firebaseAuthUrl = System.getenv("FIREBASE_AUTH_PROVIDER_X509_CERT_URL")
            firebaseClientUrl = System.getenv("FIREBASE_CLIENT_X509_CERT_URL")

            jwtSign = System.getenv("JWT_SIGN")
        }
    }
}
