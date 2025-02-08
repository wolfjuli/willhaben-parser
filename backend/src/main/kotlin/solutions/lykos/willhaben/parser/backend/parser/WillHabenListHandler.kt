package solutions.lykos.willhaben.parser.backend.parser

import com.mohamedrejeb.ksoup.html.parser.KsoupHtmlHandler
import solutions.lykos.willhaben.parser.backend.api.PageListEntry

class WillHabenListHandler(val list: MutableList<PageListEntry>) : KsoupHtmlHandler {

    override fun onOpenTag(name: String, attributes: Map<String, String>, isImplied: Boolean) {
        if (name.lowercase() == "a" && attributes["id"]?.startsWith("search-result-entry-header") == true)
            list.add(
                PageListEntry(
                    attributes["id"]!!.substringAfterLast("-").toInt(),
                    attributes["href"]!!
                )
            )
    }
}
