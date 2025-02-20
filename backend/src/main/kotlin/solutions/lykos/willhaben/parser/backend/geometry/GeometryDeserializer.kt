package solutions.lykos.willhaben.parser.backend.geometry

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.TreeNode
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import org.locationtech.jts.geom.Geometry

class GeometryDeserializer : JsonDeserializer<Geometry>() {
    override fun deserialize(parser: JsonParser, ctxt: DeserializationContext): Geometry {
        val node = parser.codec.readTree<TreeNode>(parser)
        return GeoJSONConverter.fromGeoJSON(node.toString())
    }
}
