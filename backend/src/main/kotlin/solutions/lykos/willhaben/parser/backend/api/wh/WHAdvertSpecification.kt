package solutions.lykos.willhaben.parser.backend.api.wh

import com.fasterxml.jackson.annotation.JsonIgnore
import io.ktor.util.*
import solutions.lykos.willhaben.parser.backend.importer.Hash
import solutions.lykos.willhaben.parser.backend.importer.basedata.Listing
import solutions.lykos.willhaben.parser.backend.normalized

abstract class WHAdvertSpecification(
    val id: Int,
    private val attributes: WHAttributes
) {
    var attributeMap: MutableMap<String, Any?> =
        associateAttributes()

    private fun associateAttributes(): MutableMap<String, Any?> =
        attributes.attribute.associate { e ->
            e.name.normalized() to (e.values?.takeIf { it.size > 1 } ?: e.values?.firstOrNull())
        }.toMutableMap()


    fun addAttribute(key: String, values: List<String>) {
        attributes.attribute.add(WHAttributes.AttributeEntry(key, values))
        attributeMap = associateAttributes()
    }

    fun merge(other: WHAdvertSpecification, vararg additionalAttributes: Pair<String, List<String?>?>) {
        attributes.attribute.addAll(
            other.attributes.attribute
                .filter { !attributeMap.containsKey(it.name.normalized()) }
        )
        additionalAttributes.asList()
            .forEach { new ->
                attributes.attribute.removeIf { it.name == new.first }
                attributes.attribute.add(WHAttributes.AttributeEntry(new.first, new.second))
            }
        attributeMap = associateAttributes()

    }

    @get:JsonIgnore
    val postalCode get() = attributeMap["postcode"]?.toString()?.toInt()

    @get:JsonIgnore
    val state get() = attributeMap["state"]?.toString()

    @get:JsonIgnore
    val location get() = attributeMap["location"]?.toString()

    @get:JsonIgnore
    val address get() = attributeMap["address"]?.toString()

    @get:JsonIgnore
    val coordinates get() = attributeMap["coordinates"]?.toString()

    @get:JsonIgnore
    val price get() = attributeMap["price"]?.toString()

    @get:JsonIgnore
    val livingArea get() = attributeMap["estateSizeLivingArea"]?.toString()?.toInt()

    @get:JsonIgnore
    val isHouse get() = (attributeMap["propertyTypeHouse"]?.toString()?.toBoolean() ?: false)

    @get:JsonIgnore
    val isFlat get() = (attributeMap["propertyTypeFlat"]?.toString()?.toBoolean() ?: false)

    @get:JsonIgnore
    val rooms get() = attributeMap["rooms"]?.toString()

    @get:JsonIgnore
    val url get() = (attributeMap["seoUrl"] ?: attributeMap["projectSeoUrl"])?.let { if (it is List<*>) it.firstOrNull() else it }?.toString()

    @get:JsonIgnore
    val published get() = attributeMap["publishedString"]?.toString()

    @get:JsonIgnore
    val changed get() = attributeMap["changedDate"]?.toString()

    @OptIn(ExperimentalStdlibApi::class)
    fun toNode(additionalWHAttributes: WHAttributes? = null, hash: Hash? = null, duplicateHash: Hash? = null) =
        with(additionalWHAttributes?.let {
            this.also {
                val attrs = additionalWHAttributes.attribute
                attrs.addAll(it.attributes.attribute)
                it.attributes.attribute.addAll(attrs)
                attributeMap = associateAttributes()
            }
        } ?: this) {
            Listing(
                id,
                hash ?: sha1(
                    """$id$changed$state$location$address$price$isHouse$isFlat$rooms$url""".toByteArray()
                ).joinToString("") { it.toHexString() },
                duplicateHash ?: sha1(
                    """$state$location$address$isHouse$isFlat$rooms""".toByteArray()
                ).joinToString("") { it.toHexString() },
                url ?: error("Could not retrieve url from Listing id $id"),
                this
            )
        }
}
