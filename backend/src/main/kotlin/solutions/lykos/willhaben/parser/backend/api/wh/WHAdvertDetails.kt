package solutions.lykos.willhaben.parser.backend.api.wh

class WHAdvertDetails(
    id: Int,
    val uuid: String?,
    val verticalId: Int?,
    val adTypeId: Int?,
    val productId: Int?,
    val parentAdId: Int?,
    val description: String?,
    val startDate: String?,
    val endDate: String?,
    val publishedDate: String?,
    val firstPublishedDate: String?,
    val createdDate: String?,
    val changedDate: String?,
    val advertiserReferenceNumber: String?,
    attributes: WHAttributes,
    val advertImageList: WHAdvertImageList?,
    val advertStatus: WHAdvertStatus?, // {},
) : WHAdvertSpecification(id, attributes)

