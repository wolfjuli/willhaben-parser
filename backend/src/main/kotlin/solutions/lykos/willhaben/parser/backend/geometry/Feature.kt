package solutions.lykos.willhaben.parser.backend.geometry

import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import org.locationtech.jts.geom.Geometry

/**
 * @author Gunnar Schulze
 */
data class Feature(
    val id: String,
    @JsonSerialize(using = GeometrySerializer::class)
    @JsonDeserialize(using = GeometryDeserializer::class)
    val geometry: Geometry?,
    val properties: MutableMap<String, TranslatedField>?
) {
    override fun toString(): String {
        val geomString = geometry?.let { g -> "${g.geometryType}(${g.coordinates.size} coordinates)" }
        return "Feature(id=$id, geometry=$geomString, properties=$properties)"
    }
}
