package solutions.lykos.willhaben.parser.backend.api

//Special listing search endpoint
data class SearchParams(
    val page: Int? = null,
    val viewAttributes: List<String>? = null,
    val searchString: String? = null,
    val searchAttributes: List<String>? = null,
    val sortCol: String = "points",
    val sortDir: SortDir = SortDir.DESC
) {
    fun toMap() = mapOf(
        "page" to page,
        "viewAttributes" to (viewAttributes.orEmpty().toSet() + setOf(
            "willhabenId",
            "points",
            "heading",
            "listingId"
        )).toList(),
        "searchString" to (searchString?.trim()?.split(" ")?.mapNotNull { it.takeUnless { it.isBlank() } }
            ?: emptyList()),
        "searchAttributes" to searchAttributes,
        "sortCol" to sortCol,
        "sortDir" to sortDir,
    )

    init {
        sortCol.trim().takeIf {
            it.contains("--") ||
                    it.contains("//") ||
                    it.contains("/*") ||
                    it.contains(" ")
        }?.let {
            error("Invalid sort column")
        }
    }
}
