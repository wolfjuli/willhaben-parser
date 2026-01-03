package solutions.lykos.willhaben.parser.backend.api.wh

class WHAdvertSummary(
    id: Int, // "1332426850",
    val verticalId: Int, // 2,
    val adTypeId: Int, // 1,
    val productId: Int, // 223,
    val advertStatus: WHAdvertStatus?, // {},
    val description: String?, // "Exklusive Do
    attributes: WHAttributes, // {},
    val advertImageList: WHAdvertImageList?, // {},
    val selfLink: String?, // "https://api.wil
    val contextLinkList: Map<String, Any>?,
    val advertiserInfo: WHAdvertiserInfo?, // {},, // {},
    val upsellingOrganisationLogo: String?, //
    val teaserAttributes: List<WHTeaserAttribute>?, // [],
    val children: List<WHAdvertSummary>?
) : WHAdvertSpecification(id, attributes) {
    companion object {
        fun fromUrl(url: String) = WHAdvertSummary(
            0,
            0,
            0,
            0,
            null,
            null,
            WHAttributes(
                mutableListOf(
                    WHAttributes.AttributeEntry("seo_url", listOf(url))
                )
            ),
            null,
            null,
            null,
            null,
            null,
            null,
            null
        )
    }
}
