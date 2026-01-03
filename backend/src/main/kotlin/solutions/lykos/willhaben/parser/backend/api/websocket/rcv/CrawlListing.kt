package solutions.lykos.willhaben.parser.backend.api.websocket.rcv

import solutions.lykos.willhaben.parser.backend.crawler.Crawler
import solutions.lykos.willhaben.parser.backend.database.postgresql.QueryTemplateProvider
import solutions.lykos.willhaben.parser.backend.database.postgresql.Transaction


data class CrawlListing(val url: String) : WSListMessage() {
    override fun respond(transaction: Transaction, templates: QueryTemplateProvider): List<Map<String, Any?>> {
        val ids = Crawler.crawlSingle(url)
        return GetListings(null, null, willhabenIds = ids.map { it.toString()}).respond(transaction, templates)
    }
}