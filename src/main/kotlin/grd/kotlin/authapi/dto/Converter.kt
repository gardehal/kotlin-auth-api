package grd.kotlin.authapi.dto

import com.google.gson.Gson
import org.springframework.data.domain.Page

class Converter
{
    companion object
    {
        private val gson = Gson()

        fun <I, O> convert(toMap: I, targetClass: Class<O>): O
        {
            val json = gson.toJson(toMap)
            return gson.fromJson(json, targetClass)
        }

        fun <I, O> convert(entities: List<I>, tDtoClass: Class<O>): List<O>
        {
            return entities.map { convert(it, tDtoClass) }
        }

        fun <I, O> convert(entities: Page<I>, tDtoClass: Class<O>): Page<O>
        {
            return entities.map { convert(it, tDtoClass) }
        }
    }
}
