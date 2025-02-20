package solutions.lykos.willhaben.parser.backend.geometry

import java.time.Instant

data class Trajectory(
    val name: String? = null,
    val segments: List<TrackSegment>,
    val extensions: Map<String, String>? = null
) {
    data class TrackPoint(
        val lat: Double,
        val lon: Double,
        val time: Instant,
        val extensions: Map<String, String>? = null,
    )

    data class TrackSegment(
        val points: List<TrackPoint>,
        val extensions: Map<String, String>? = null
    )

    val isSegmented: Boolean
        get() = segments.any { it.extensions?.isNotEmpty() == true }
}
