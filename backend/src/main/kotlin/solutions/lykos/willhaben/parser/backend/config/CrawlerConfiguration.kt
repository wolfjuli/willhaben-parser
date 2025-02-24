package solutions.lykos.willhaben.parser.backend.config

class CrawlerConfiguration {
    var url: String = "https://willhaben.at"
    var maxTimeout: Int = 60 * 60 * 6 //in seconds
    var maxDirectDownloads: Int = 50
}
