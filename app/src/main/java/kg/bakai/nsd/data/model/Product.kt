package kg.bakai.nsd.data.model

data class Product(
    val name: String,
    val count: Int,
    val price: Double,
    val addons: List<Addon>
)