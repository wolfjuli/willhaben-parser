package solutions.lykos.willhaben.parser.backend.parser

import com.fasterxml.jackson.module.kotlin.readValue
import com.mohamedrejeb.ksoup.html.parser.KsoupHtmlParser
import solutions.lykos.willhaben.parser.backend.api.WatchList
import solutions.lykos.willhaben.parser.backend.api.wh.WHAdvertSummary
import solutions.lykos.willhaben.parser.backend.api.wh.WHSearch
import solutions.lykos.willhaben.parser.backend.ceilToInt
import solutions.lykos.willhaben.parser.backend.jsonObjectMapper
import java.net.URL


fun List<WatchList>.parse(): Sequence<WHAdvertSummary> = asSequence()
    .flatMap { watchList ->
        var page = 1
        var maxPage = 1
        val jsons = mutableMapOf<String, String>()
        val parser = KsoupHtmlParser(handler = WillHabenScriptsHandler(jsons))
        val mapper = jsonObjectMapper()

        generateSequence {
            jsons.clear()
            if (page > maxPage) return@generateSequence null
            parser.write(URL(watchList.url + "&rows=200&page=$page").readText())
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


