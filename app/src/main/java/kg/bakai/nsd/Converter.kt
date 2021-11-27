package kg.bakai.nsd

import com.google.gson.Gson
import kg.bakai.nsd.data.model.OrderProductModel

object Converter {
    fun listToJson(list: List<OrderProductModel>): String {
        val gson = Gson()
        return gson.toJson(list)
    }
}