package grd.kotlin.authapi.services

import grd.kotlin.authapi.Log
import grd.kotlin.authapi.dto.HealthDataDto
import grd.kotlin.authapi.exceptions.NotFoundException
import grd.kotlin.authapi.models.ApiMetadata
import grd.kotlin.authapi.settings.Settings
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.DependsOn
import org.springframework.stereotype.Service
import java.time.Duration
import java.time.Instant
import javax.annotation.PostConstruct

@Service
@DependsOn("FirebaseInitialize")
class ApiMetadataService : BaseService<ApiMetadata>(ApiMetadata::class.java, true)
{
    @Autowired
    private lateinit var ingredientService: IngredientService

    @Autowired
    private lateinit var recipeService: RecipeService

    @Autowired
    private lateinit var scraperService: ScraperService

    @Autowired
    lateinit var settings: Settings

    lateinit var apiUpDateTime: Instant

    @PostConstruct
    fun init()
    {
        apiUpDateTime = Instant.now()
    }

    /**
     * Check API and database for metadata. Update entity with these values.
     * @return ApiMetadata with fields set
     * @throws none
     **/
    fun setData(metadata: ApiMetadata, editorId: String): ApiMetadata
    {
        val nowString = Instant.now().toString()

        metadata.updatedTime = nowString
        metadata.totalIngredients = ingredientService.getAll().toList().size
        metadata.totalRecipes = recipeService.getAll().toList().size

        return metadata
    }

    /**
     * Create a HealthData object with API health info
     * @return HealthDataDto? HealthData result
     * @throws none
     **/
    suspend fun getHealthData(): HealthDataDto?
    {
        val messages: MutableList<String> = mutableListOf()
        val nowDateTime = Instant.now()
        val apiUpDateTime = apiUpDateTime
        val uptimeDuration = Duration.between(nowDateTime, apiUpDateTime)
        var responseFirebaseDuration: Duration? = null
        var responseNrkDuration: Duration? = null
        var responseKolonialDuration: Duration? = null

        try
        {
            val responseStart = Instant.now()
            getAll()
            val responseEnd = Instant.now()
            responseFirebaseDuration = Duration.between(responseStart, responseEnd)
        }
        catch(e: Exception)
        {
            Log.main.info("Firebase was not online, {function}", this.toString())
            messages.add("Failed to contact database Firebase: ${e.message}")
        }

        try
        {
            val responseStart = Instant.now()
            val target = settings.env.urlNrk!!
            val isOnline = scraperService.isOnline(target)
            if(!isOnline)
                throw NotFoundException("Response code was not 200.")

            val responseEnd = Instant.now()
            responseNrkDuration = Duration.between(responseStart, responseEnd)
        }
        catch(e: Exception)
        {
            Log.main.info("NRK was not online, {function}", this.toString())
            messages.add("Failed to contact scraper target NRK: ${e.message}")
        }

        try
        {
            val responseStart = Instant.now()
            val target = settings.env.urlKolonial!!
            val isOnline = scraperService.isOnline(target)
            if(!isOnline)
                throw NotFoundException("Response code was not 200.")

            val responseEnd = Instant.now()
            responseKolonialDuration = Duration.between(responseStart, responseEnd)
        }
        catch(e: Exception)
        {
            Log.main.info("Kolonial/Oda was not online, {function}", this.toString())
            messages.add("Failed to contact scraper target Kolonial: ${e.message}")
        }

        if(!messages.any())
            messages.add("No errors.")

        return HealthDataDto(nowDateTime, apiUpDateTime, uptimeDuration, responseFirebaseDuration, responseNrkDuration, responseKolonialDuration, messages)
    }
}
