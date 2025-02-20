package solutions.lykos.willhaben.parser.backend.importer.basedata

import solutions.lykos.willhaben.parser.backend.geometry.GeoJSONConverter
import solutions.lykos.willhaben.parser.backend.importer.annotations.HashField
import solutions.lykos.willhaben.parser.backend.importer.annotations.IdField
import solutions.lykos.willhaben.parser.backend.importer.hashString
import java.lang.reflect.Method
import kotlin.reflect.full.memberProperties
import kotlin.reflect.full.superclasses
import kotlin.reflect.jvm.javaGetter
import org.locationtech.jts.geom.Geometry as JTSGeometry

interface Node {
    companion object {
        private inline fun <reified T : Node> T.getAllMemberPropertyAnnotations(name: String) =
            (
                    this::class.superclasses.flatMap { it.memberProperties.filter { it.name == name } } +
                            this::class.memberProperties.filter { it.name == name }
                    ).flatMap { it.annotations }

        private val identityFields: MutableMap<String, List<Method>> = mutableMapOf()
        private val hashFields: MutableMap<String, List<Method>> = mutableMapOf()

        private val whitespaceRegex = "[^0-9]".toRegex()
    }

    fun mergeValues(identityValues: Boolean, delimiter: String = ","): String {
        if (identityFields[this::class.java.name] == null) {
            val hasIdFields = this::class.memberProperties.any { property ->
                this.getAllMemberPropertyAnnotations(property.name).any { it is IdField }
            }

            identityFields[this::class.java.name] = this::class
                .memberProperties
                .asSequence()
                .filter { property ->
                    if (hasIdFields) {
                        this.getAllMemberPropertyAnnotations(property.name).any { it is IdField }
                    } else {
                        this.getAllMemberPropertyAnnotations(property.name).none { it is HashField }
                    }
                }
                .map { it.javaGetter!! }
                .sortedBy { it.name }
                .toList()

            hashFields[this::class.java.name] = this::class
                .memberProperties
                .asSequence()
                .filter { property ->
                    this.getAllMemberPropertyAnnotations(property.name).any { it is HashField }
                }
                .map { it.javaGetter!! }
                .sortedBy { it.name }
                .toList()
        }

        return (identityFields[this::class.java.name].takeIf { identityValues } ?: hashFields[this::class.java.name])!!
            .joinToString(delimiter) { curr ->
                curr.invoke(this).let { value ->
                    when (value) {
                        //is DateRange -> value.toPGString()
                        is JTSGeometry -> GeoJSONConverter.toGeoJSON(value).substringBefore("crs").replace(
                            whitespaceRegex,
                            ""
                        )

                        is Collection<*> -> value.joinToString(",", "{", "}") {
                            if (it is String && it.contains(",")) {
                                "\"$it\""
                            } else {
                                "$it"
                            }
                        }

                        else -> value?.toString()?.takeIf { it != "null" } ?: ""
                    }
                }
            }
    }

    fun toIdentityObject(): HashedObject {
        return HashedObject(
            { mergeValues(true) },
            { hashString(mergeValues(false), HashType.SHA512) }
        )
    }
}
