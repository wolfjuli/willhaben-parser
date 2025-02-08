package solutions.lykos.willhaben.parser.backend.api

data class WHAdvertImageList(
    val advertImage: List<AdvertImage>,
    val floorPlans: List<AdvertImage>
) {
    data class AdvertImage(
        val id: Int, // 1,
        val name: String, // "0/133/242/6850_-1767578652.jpg",
        val selfLink: String, // "https://api.willhaben.at/restapi/v2/atimage/1332426850/1",
        val description: String?, // "Cover Image",
        val mainImageUrl: String?, // "https://cache.willhaben.at/mmo/0/133/242/6850_-1767578652_hoved.jpg",
        val thumbnailImageUrl: String?, // "https://cache.willhaben.at/mmo/0/133/242/6850_-1767578652_thumb.jpg",
        val referenceImageUrl: String?, // "https://cache.willhaben.at/mmo/0/133/242/6850_-1767578652.jpg",
        val similarImageSearchUrl: String?, // null,
        val reference: String?, // "0/133/242/6850_-1767578652.jpg"
    )
}
