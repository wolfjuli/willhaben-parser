package solutions.lykos.willhaben.parser.backend.api

//Special listing search endpoint
data class SearchParams(
    val searchString: List<String>,
    val sortCol: String,
    val searchAttributes: List<String>?,
    val sortDir: SortDir
) {

    constructor(map: Map<String, List<String>>) : this(
        map["searchString"]?.firstOrNull()?.trim()?.split(" ")?.mapNotNull { it.takeUnless { it.isBlank() } }
            ?: emptyList(),
        map["sortCol"]?.firstOrNull()
            ?.takeIf { it.contains("--") || it.contains("//") || it.contains("/*") || it.contains(" ") } ?: "points",
        map["searchAttributes"],
        SortDir.valueOf(map["sortDir"]?.firstOrNull() ?: "DESC")
    )

    fun toMap() = mapOf(
        "searchString" to searchString,
        "searchAttributes" to searchAttributes,
        "sortCol" to sortCol,
        "sortDir" to sortDir,
    )

}
