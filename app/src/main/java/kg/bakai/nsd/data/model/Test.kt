package kg.bakai.nsd.data.model

import java.io.Serializable


data class Test(
    var message: String,
    var sender: String
): Serializable

data class OrderProductModel(
    val productName: String,
    val price: String
): Serializable