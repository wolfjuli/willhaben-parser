package solutions.lykos.willhaben.parser.backend.api.wh

import com.fasterxml.jackson.annotation.JsonIgnore
import io.ktor.util.*
import solutions.lykos.willhaben.parser.backend.importer.Hash
import solutions.lykos.willhaben.parser.backend.importer.basedata.Listing

abstract class WHAdvertSpecification(
    val id: Int,
    private val attributes: WHAttributes
) {
    var attributeMap: MutableMap<String, List<String?>?> =
        attributes.attribute.associate { it.name to it.values }.toMutableMap()

    fun addAttribute(key: String, values: List<String>) {
        attributes.attribute.add(WHAttributes.AttributeEntry(key, values))
        attributeMap[key] = values
    }

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
    val url get() = (attributeMap["SEO_URL"] ?: attributeMap["PROJECT_SEO_URL"])?.firstOrNull()

    @get:JsonIgnore
    val published get() = attributeMap["PUBLISHED_String"]?.firstOrNull()

    @get:JsonIgnore
    val changed get() = attributeMap["CHANGED_DATE"]?.firstOrNull()

    @OptIn(ExperimentalStdlibApi::class)
    fun toNode(additionalWHAttributes: WHAttributes? = null, hash: Hash? = null, duplicateHash: Hash? = null) =
        with(additionalWHAttributes?.let {
            this.also {
                val attrs = additionalWHAttributes.attribute
                attrs.addAll(it.attributes.attribute)
                it.attributes.attribute.addAll(attrs)
                attributeMap = attributes.attribute.associate { it.name to it.values }.toMutableMap()
            }
        } ?: this) {
            Listing(
                id,
                hash ?: sha1(
                    """$id$changed$state$location$address$price$isHouse$isFlat$rooms$url""".toByteArray()
                ).joinToString("") { it.toHexString() },
                duplicateHash ?: sha1(
                    """$state$location$address$price$isHouse$isFlat$rooms""".toByteArray()
                ).joinToString("") { it.toHexString() },
                url ?: error("Could not retrieve url from Listing id $id"),
                this
            )
        }
}
