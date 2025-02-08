package solutions.lykos.willhaben.parser.backend.api


data class WHAdvertSummary(
    val id: Int, // "1332426850",
    val verticalId: Int, // 2,
    val adTypeId: Int, // 1,
    val productId: Int, // 223,
    val advertStatus: WHAdvertStatus?, // {},
    val description: String?, // "Exklusive Do
    val attributes: WHAttributes, // {},
    val advertImageList: WHAdvertImageList?, // {},
    val selfLink: String?, // "https://api.wil
    val contextLinkList: Map<String, Any>?,
    val advertiserInfo: WHAdvertiserInfo?, // {},, // {},
    val upsellingOrganisationLogo: String?, //
    val teaserAttributes: List<WHTeaserAttribute>?, // [],
    val children: List<WHAdvertSummary>?
) {
    private val attributeMap: Map<String, List<String?>?> = attributes.attribute.associate { it.name to it.values }
    /* Currently known attributes:
    "LOCATION"
    "FREE_AREA/FREE_AREA_AREA_TOTAL"
    "POSTCODE"
    "STATE"
    "BODY_DYN"
    "ORGNAME"
    "imagedescription"
    "ORG_UUID"
    "ESTATE_SIZE/LIVING_AREA"
    "DISTRICT"
    "HEADING"
    "LOCATION_QUALITY"
    "PUBLISHED"
    "COUNTRY"
    "LOCATION_ID"
    "PROPERTY_TYPE"
    "NUMBER_OF_ROOMS"
    "ADTYPE_ID"
    "PROPERTY_TYPE_ID"
    "ADID"
    "ORGID"
    "SEO_URL"
    "FREE_AREA_TYPE"
    "ALL_IMAGE_URLS"
    "PUBLISHED_String"
    "ESTATE_PREFERENCE"
    "UPSELLING_AD_SEARCHRESULT"
    "categorytreeids"
    "ADVERTISER_REF"
    "PRODUCT_ID"
    "IS_BUMPED"
    "MMO"
    "ROOMS"
    "ESTATE_SIZE/USEABLE_AREA"
    "AD_UUID"
    "ADDRESS"
    "AD_SEARCHRESULT_LOGO"
    "COORDINATES"
    "PRICE"
    "PRICE_FOR_DISPLAY"
    "ESTATE_SIZE"
    "ISPRIVATE"
    "PROPERTY_TYPE_HOUSE"
    "UNIT_TITLE"
    "FREE_AREA_TYPE_NAME"
    "ESTATE_PRICE/PRICE_SUGGESTION"
    "UNIT_NUMBER"
    "VIRTUAL_VIEW_LINK"
    "FLOOR"
    "PROJECT/UNIT_PRICE_FROM"
    "PROJECT_ID"
    "NUMBER_OF_CHILDREN"
    "RESULT_LIST_TOPAD"
    "RESULT_LIST_STYLE2"
    "DISPOSED"
    "REFERER"
    "PROPERTY_TYPE_FLAT"
     */

    val postalCode get() = attributeMap["POSTCODE"]?.firstOrNull()?.toInt()
    val state get() = attributeMap["STATE"]?.firstOrNull()
    val location get() = attributeMap["LOCATION"]?.firstOrNull()
    val address get() = attributeMap["ADDRESS"]?.firstOrNull()
    val coordinates get() = attributeMap["COORDINATES"]?.firstOrNull()
    val price get() = attributeMap["PRICE"]?.firstOrNull()?.toInt()
    val livingArea get() = attributeMap["ESTATE_SIZE/LIVING_AREA"]?.firstOrNull()?.toInt()
    val isHouse get() = (attributeMap["PROPERTY_TYPE_HOUSE"]?.firstOrNull()?.toInt() ?: 0) > 0
    val isFlat get() = (attributeMap["PROPERTY_TYPE_FLAT"]?.firstOrNull()?.toInt() ?: 0) > 0
    val rooms get() = attributeMap["ROOMS"]?.firstOrNull()?.toInt()
    val url get() = attributeMap["SEO_URL"]?.firstOrNull()?.toInt()
}
