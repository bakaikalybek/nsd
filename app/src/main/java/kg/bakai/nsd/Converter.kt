package kg.bakai.nsd

import com.google.gson.Gson
import kg.bakai.nsd.data.model.Product

object Converter {
    fun listToJson(list: List<Product>): String {
        val gson = Gson()
        return gson.toJson(list)
    }
}