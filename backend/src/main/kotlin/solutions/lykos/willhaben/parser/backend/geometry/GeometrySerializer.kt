package solutions.lykos.willhaben.parser.backend.geometry

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.SerializerProvider
import org.locationtech.jts.geom.Geometry

class GeometrySerializer : JsonSerializer<Geometry>() {
    override fun serialize(geometry: Geometry?, generator: JsonGenerator, provider: SerializerProvider) {
        geometry?.let { g ->
            generator.writeRawValue(GeoJSONConverter.toGeoJSON(g))
        }
    }
}
