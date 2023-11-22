package grd.kotlin.authapi

import grd.kotlin.authapi.settings.Settings
import com.google.auth.oauth2.GoogleCredentials
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.database.FirebaseDatabase
import jakarta.annotation.PostConstruct
import kotlinx.coroutines.runBlocking
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import java.io.ByteArrayInputStream

@Component("FirebaseInitialize")
class FirebaseInitialize
{
    @Autowired
    private lateinit var settings: Settings

    @PostConstruct
    fun initialize() = runBlocking {
        try
        {
            val json = getJson(settings.env.firebaseType!!,
                settings.env.firebasePId!!,
                settings.env.firebasePKeyId!!,
                settings.env.firebasePKey!!,
                settings.env.firebaseCEmail!!,
                settings.env.firebaseCId!!,
                settings.env.firebaseAUri!!,
                settings.env.firebaseTUri!!,
                settings.env.firebaseAuthUrl!!,
                settings.env.firebaseClientUrl!!)

            val serviceAccount = ByteArrayInputStream(json.toByteArray())

            val options = FirebaseOptions.builder()
                .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                .setDatabaseUrl(settings.env.firebaseUrl)
                .build()

            val app = FirebaseApp.initializeApp(options)
            FirebaseDatabase.getInstance(app)

            Log.main.info("Firebase app initialized {app}, {function}", app.name, this.toString())
        }
        catch(e: IllegalStateException)
        {
            Log.main.info("IllegalStateException, {message}, {function}", e.message, this.toString())
        }
        catch(e: Exception)
        {
            Log.main.info("Exception, {message}, {function}", e.message, this.toString())
        }
    }

    private fun getJson(
        type: String,
        pId: String,
        pKeyId: String,
        pKey: String,
        cEmail: String,
        cId: String,
        aUri: String,
        tUri: String,
        authUrl: String,
        clientUrl: String,
    ): String
    {
        return "{\n" +
            "\"type\": \"$type\",\n" +
            "\"project_id\": \"$pId\",\n" +
            "\"private_key_id\": \"$pKeyId\",\n" +
            "\"private_key\": \"$pKey\",\n" +
            "\"client_email\": \"$cEmail\",\n" +
            "\"client_id\": \"$cId\",\n" +
            "\"auth_uri\": \"$aUri\",\n" +
            "\"token_uri\": \"$tUri\",\n" +
            "\"auth_provider_x509_cert_url\": \"$authUrl\",\n" +
            "\"client_x509_cert_url\": \"$clientUrl\"\n" +
            "}"
    }
}