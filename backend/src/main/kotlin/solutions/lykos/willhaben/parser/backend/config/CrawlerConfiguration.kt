package solutions.lykos.willhaben.parser.backend.config

class CrawlerConfiguration {
    var url: String = "https://willhaben.at"
    var timeout: Int? = null
    var debugAmount: Int? = null
    var imageBaseUrl: String = "https://cache.willhaben.at/mmo"
    var listingBaseUrl: String = "https://www.willhaben.at/iad/"
}
