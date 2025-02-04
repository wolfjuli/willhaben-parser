package solutions.lykos.willhaben.parser.backend.logging

import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.filter.Filter
import ch.qos.logback.core.spi.FilterReply
import org.slf4j.MarkerFactory

class MarkerLoggingFilter(
    private val filters: List<String>
) : Filter<ILoggingEvent>() {
    override fun decide(event: ILoggingEvent): FilterReply {
        if (filters.contains("all")) {
            return FilterReply.ACCEPT
        }

        val markerInFilters = filters.any { MarkerFactory.getMarker(it) in event.markerList }

        if (filters.contains("all-except-filters")) {
            return if (markerInFilters) {
                FilterReply.DENY
            } else {
                FilterReply.ACCEPT
            }
        }

        return if (markerInFilters) {
            FilterReply.ACCEPT
        } else {
            FilterReply.DENY
        }
    }
}
