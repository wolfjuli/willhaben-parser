package solutions.lykos.willhaben.parser.backend.api.wh

import com.fasterxml.jackson.annotation.JsonIgnore
import io.ktor.util.*
import solutions.lykos.willhaben.parser.backend.importer.basedata.Listing

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

    @get:JsonIgnore
    val postalCode get() = attributeMap["POSTCODE"]?.firstOrNull()?.toInt()
    @get:JsonIgnore
    val state get() = attributeMap["STATE"]?.firstOrNull()
    @get:JsonIgnore
    val location get() = attributeMap["LOCATION"]?.firstOrNull()
    @get:JsonIgnore
    val address get() = attributeMap["ADDRESS"]?.firstOrNull()
    @get:JsonIgnore
    val coordinates get() = attributeMap["COORDINATES"]?.firstOrNull()
    @get:JsonIgnore
    val price get() = attributeMap["PRICE"]?.firstOrNull()
    @get:JsonIgnore
    val livingArea get() = attributeMap["ESTATE_SIZE/LIVING_AREA"]?.firstOrNull()?.toInt()
    @get:JsonIgnore
    val isHouse get() = (attributeMap["PROPERTY_TYPE_HOUSE"]?.firstOrNull()?.toBoolean() ?: false)
    @get:JsonIgnore
    val isFlat get() = (attributeMap["PROPERTY_TYPE_FLAT"]?.firstOrNull()?.toBoolean() ?: false)
    @get:JsonIgnore
    val rooms get() = attributeMap["ROOMS"]?.firstOrNull()
    @get:JsonIgnore
    val url get() = attributeMap["SEO_URL"]?.firstOrNull()

    @OptIn(ExperimentalStdlibApi::class)
    fun toNode() = Listing(
        id,
        sha1(
            """$id$state$location$address$price$isHouse$isFlat$rooms$url""".toByteArray()
        ).joinToString("") { it.toHexString() },
        sha1(
            """$state$location$address$price$isHouse$isFlat$rooms""".toByteArray()
        ).joinToString("") { it.toHexString() },
        url ?: error("Could not retrieve url from Listing id $id"),
        this
    )
}

