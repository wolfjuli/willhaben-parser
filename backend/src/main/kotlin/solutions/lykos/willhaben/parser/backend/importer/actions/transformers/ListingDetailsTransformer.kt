package solutions.lykos.willhaben.parser.backend.importer.actions.transformers

import com.fasterxml.jackson.module.kotlin.readValue
import com.mohamedrejeb.ksoup.html.parser.KsoupHtmlParser
import solutions.lykos.willhaben.parser.backend.api.wh.WHSiteDetails
import solutions.lykos.willhaben.parser.backend.config.CrawlerConfiguration
import solutions.lykos.willhaben.parser.backend.database.postgresql.Transaction
import solutions.lykos.willhaben.parser.backend.importer.basedata.Listing
import solutions.lykos.willhaben.parser.backend.jsonObjectMapper
import solutions.lykos.willhaben.parser.backend.parser.WillHabenScriptsHandler
import java.net.URL

class ListingDetailsTransformer(
    private val configuration: CrawlerConfiguration
) : Transformer<Listing>() {
    private val jsons = mutableMapOf<String, String>()
    private val parser = KsoupHtmlParser(handler = WillHabenScriptsHandler(jsons))
    private val mapper = jsonObjectMapper()

    private val urlAttrs = setOf("SEO_URL", "PROJECT_SEO_URL")
    override fun transformEntry(entry: Listing): Listing {
        val url = entry.raw.attributes.attribute
            .firstOrNull { it.name in urlAttrs }
            ?.values
            ?.firstOrNull()
            ?.let { configuration.listingBaseUrl + it } ?: return entry

        jsons.clear()
        parser.write(URL(url).readText())
        return jsons["__NEXT_DATA__"]?.let { json ->
            mapper.readValue<WHSiteDetails>(json)
                .props.pageProps.advertDetails.toNode(entry.raw.attributes)
        } ?: entry
    }

    override fun updateResolving(transaction: Transaction) {

    }
}
