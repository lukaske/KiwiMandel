package n.com.kiwimandel

data class Spot(
    val id: Long = counter++,
    val name: String,
    val city: String,
    val url: String,
    val book: String
) {
    companion object {
        private var counter = 0L
    }
}