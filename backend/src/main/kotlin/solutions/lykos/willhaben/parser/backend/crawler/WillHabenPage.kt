package solutions.lykos.willhaben.parser.backend.crawler

data class WillHabenPage(
    var url: String,
    var raw: String,
    val jsons: MutableMap<String, String> = mutableMapOf()

)
