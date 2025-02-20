package solutions.lykos.willhaben.parser.backend.crawler

import solutions.lykos.willhaben.parser.backend.api.wh.WHAdvertSummary

import java.time.LocalDateTime

data class CrawlerData(
    val data: Sequence<WHAdvertSummary>,
    val timestamp: LocalDateTime = LocalDateTime.now()
)
