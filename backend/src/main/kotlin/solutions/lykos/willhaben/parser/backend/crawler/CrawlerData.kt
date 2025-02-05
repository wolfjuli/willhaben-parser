package solutions.lykos.willhaben.parser.backend.crawler

import java.time.LocalDateTime

data class CrawlerData(
    val data: List<CrawledEntry>,
    val timestamp: LocalDateTime = LocalDateTime.now()
)
