package grd.kotlin.authapi.settings

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration
@ConfigurationProperties(prefix = "endpoint")
open class Endpoint
{
    var convert: String? = null
    var cookingMethodModel: String? = null
    var detailed: String? = null
    var difficultyModel: String? = null
    var freetext: String? = null
    var getToken: String? = null
    var health: String? = null
    var ingredient: String? = null
    var ingredientTypeModel: String? = null
    var cookingMethodMap: String? = null
    var difficultyMap: String? = null
    var ingredientTypeMap: String? = null
    var preparationMap: String? = null
    var proteinMap: String? = null
    var quantityUnitMap: String? = null
    var recipeTypeMap: String? = null
    var temperatureUnitMap: String? = null
    var menu: String? = null
    var model: String? = null
    var preparationModel: String? = null
    var proteinModel: String? = null
    var quantityUnitModel: String? = null
    var random: String? = null
    var recipe: String? = null
    var recipeIngredient: String? = null
    var recipeTypeModel: String? = null
    var remove: String? = null
    var temperatureUnitModel: String? = null
    var update: String? = null
    var username: String? = null
    var validate: String? = null
    var setIngredientFields: String? = null
}
