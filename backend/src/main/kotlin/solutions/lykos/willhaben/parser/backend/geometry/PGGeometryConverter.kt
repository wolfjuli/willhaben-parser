package solutions.lykos.willhaben.parser.backend.geometry

import net.postgis.jdbc.PGgeo
import org.locationtech.jts.geom.*
import net.postgis.jdbc.geometry.Geometry as PGGeometry
import net.postgis.jdbc.geometry.GeometryCollection as PGGeometryCollection
import net.postgis.jdbc.geometry.LineString as PGLineString
import net.postgis.jdbc.geometry.MultiLineString as PGMultiLineString
import net.postgis.jdbc.geometry.MultiPoint as PGMultiPoint
import net.postgis.jdbc.geometry.MultiPolygon as PGMultiPolygon
import net.postgis.jdbc.geometry.Point as PGPoint
import net.postgis.jdbc.geometry.Polygon as PGPolygon

/**
 * @author Gunnar Schulze
 */
object PGGeometryConverter {

    private val geometryFactory = GeometryFactory()

    fun toGeometry(geometry: Any): Geometry = toGeometry((geometry as PGgeo).geometry)

    fun toGeometry(geometry: PGGeometry): Geometry = when (geometry) {
        is PGPoint -> toGeometry(geometry)
        is PGLineString -> toGeometry(geometry)
        is PGPolygon -> toGeometry(geometry)
        is PGMultiPoint -> toGeometry(geometry)
        is PGMultiLineString -> toGeometry(geometry)
        is PGMultiPolygon -> toGeometry(geometry)
        is PGGeometryCollection -> toGeometry(geometry)
        else -> throw IllegalArgumentException("Unsupported geometry type '${geometry.javaClass.simpleName}'")
    }

    private fun toGeometry(point: PGPoint): Point {
        return geometryFactory.createPoint(Coordinate(point.x, point.y))
    }

    private fun toGeometry(lineString: PGLineString): LineString {
        val coordinates = lineString.points.map { Coordinate(it.x, it.y) }.toTypedArray()
        return geometryFactory.createLineString(coordinates)
    }

    private fun toGeometry(polygon: PGPolygon): Polygon {
        val numRings = polygon.numRings()
        val coordinates = (0 until numRings).map { index ->
            geometryFactory.createLinearRing(
                polygon.getRing(index).points.map { Coordinate(it.x, it.y) }.toTypedArray()
            )
        }
        return if (coordinates.size == 1) {
            geometryFactory.createPolygon(coordinates.first())
        } else {
            geometryFactory.createPolygon(coordinates.first(), coordinates.subList(1, coordinates.size).toTypedArray())
        }
    }

    private fun toGeometry(multiPoint: PGMultiPoint): MultiPoint {
        val coordinates = multiPoint.points.map { toGeometry(it) }.toTypedArray()
        return geometryFactory.createMultiPoint(coordinates)
    }

    private fun toGeometry(multiLineString: PGMultiLineString): MultiLineString {
        val coordinates = multiLineString.lines.map { toGeometry(it) }.toTypedArray()
        return geometryFactory.createMultiLineString(coordinates)
    }

    private fun toGeometry(multiPolygon: PGMultiPolygon): MultiPolygon {
        val coordinates = multiPolygon.polygons.map { toGeometry(it) }.toTypedArray()
        return geometryFactory.createMultiPolygon(coordinates)
    }

    private fun toGeometry(geometryCollection: PGGeometryCollection): GeometryCollection {
        val geometries = geometryCollection.geometries.map { toGeometry(it) }.toTypedArray()
        return geometryFactory.createGeometryCollection(geometries)
    }
}
