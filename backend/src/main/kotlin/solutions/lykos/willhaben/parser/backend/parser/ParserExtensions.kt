package solutions.lykos.willhaben.parser.backend.parser

import com.fasterxml.jackson.module.kotlin.readValue
import com.mohamedrejeb.ksoup.html.parser.KsoupHtmlParser
import org.slf4j.LoggerFactory
import solutions.lykos.willhaben.parser.backend.api.WatchList
import solutions.lykos.willhaben.parser.backend.api.wh.WHAdvertSpecification
import solutions.lykos.willhaben.parser.backend.api.wh.WHSearch
import solutions.lykos.willhaben.parser.backend.api.wh.WHSiteDetails
import solutions.lykos.willhaben.parser.backend.ceilToInt
import solutions.lykos.willhaben.parser.backend.config.CrawlerConfiguration
import solutions.lykos.willhaben.parser.backend.importer.ImporterFetcher.logger
import solutions.lykos.willhaben.parser.backend.jsonObjectMapper
import java.net.URL

private val logger = LoggerFactory.getLogger("Watchlist.parse()")
private val jsons = mutableMapOf<String, String>()
private val parser = KsoupHtmlParser(handler = WillHabenScriptsHandler(jsons))
private val mapper = jsonObjectMapper()

fun List<WatchList>.parse(): Sequence<WHAdvertSpecification> = asSequence()
    .flatMap { watchList ->
        var page = 1
        var maxPage = 1

        generateSequence {
            jsons.clear()
            if (page > maxPage) return@generateSequence null
            val url = watchList.url + "&rows=200&page=$page"
            logger.debug("Fetching URL $url")
            parser.write(URL(url).readText())
            page++
            jsons["__NEXT_DATA__"]?.let { json ->
                mapper.readValue<WHSearch>(json).let { search ->
                    if (maxPage <= 1)
                        maxPage = (search.props.pageProps.searchResult.rowsFound.toFloat() / 200.0).ceilToInt()

                    search.props.pageProps.searchResult.advertSummaryList.advertSummary
                }
            }
        }.flatten()
    }


fun Sequence<WHAdvertSpecification>.detailed(configuration: CrawlerConfiguration): Sequence<WHAdvertSpecification> =
    mapNotNull { summary ->
        summary.url?.let { subUrl ->
            val url = "${configuration.listingBaseUrl}/$subUrl"
            logger.debug("Fetching URL $url")

            //Sometimes we get a 502 or some other weird error, lets give it 3 trys to fetch it
            (0..2)
                .asSequence()
                .mapNotNull {
                    try {
                        URL(url).readText()
                    } catch (e: Exception) {
                        logger.warn("Failed to fetch URL $url", e)
                        null
                    }
                }.firstOrNull()
                ?.let { text ->
                    parser.write(text)
                    jsons["__NEXT_DATA__"]?.let { json ->
                        mapper.readValue<WHSiteDetails>(json).props.pageProps.advertDetails.also {
                            it.addAttribute("SEO_URL", listOf(subUrl))
                        }
                    }
                }
        } ?: summary
    }