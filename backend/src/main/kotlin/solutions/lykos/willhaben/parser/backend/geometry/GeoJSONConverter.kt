package solutions.lykos.willhaben.parser.backend.geometry

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import net.postgis.jdbc.PGgeo
import org.locationtech.jts.geom.Geometry
import org.locationtech.jts.geom.GeometryFactory
import org.locationtech.jts.geom.PrecisionModel
import org.locationtech.jts.io.geojson.GeoJsonReader
import org.locationtech.jts.io.geojson.GeoJsonWriter
import java.io.Reader

/**
 * @author Gunnar Schulze
 */
object GeoJSONConverter {
    private val mapper = jacksonObjectMapper().findAndRegisterModules()
    private val rw = GeoJsonWriter()

    // only precision up to the 10th position, as psql starts rounding (is in the tenth of mm, we should be fine)
    private val rd = GeoJsonReader(GeometryFactory(PrecisionModel(Math.pow(10.0, 10.0))))
    private val zeroRegex = "([^0-9])0\\.0([^0-9])".toRegex()

    fun toGeoJSON(feature: Feature): String = """
            {
            "id": "${feature.id}",
            "geometry": ${feature.geometry?.let { g -> toGeoJSON(g) } ?: "{}"},
            "properties": ${mapper.writeValueAsString(feature.properties?.toSimpleMap()) ?: "{}"}
            }
    """.trimIndent()

    fun toGeoJSON(geometry: Geometry): String {
        var curr = rw.write(geometry.also { it.normalize() })
        var last = ""

        while (last != curr) {
            last = curr
            curr = last.replace(zeroRegex) { "${it.groupValues[1]}0${it.groupValues[2]}" }
        }
        return last
    }

    fun toGeoJSON(sqlObject: PGgeo): String {
        return toGeoJSON(PGGeometryConverter.toGeometry(sqlObject))
    }

    fun fromGeoJSON(geoJSON: String): Geometry =
        rd.read(geoJSON).also { it.normalize() }

    fun fromGeoJSON(reader: Reader): Geometry =
        rd.read(reader).also { it.normalize() }
}
