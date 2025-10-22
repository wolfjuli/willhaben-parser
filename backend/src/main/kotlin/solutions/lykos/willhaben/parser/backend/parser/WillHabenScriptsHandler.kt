package solutions.lykos.willhaben.parser.backend.parser

import  com.mohamedrejeb.ksoup.html.parser.KsoupHtmlHandler

class WillHabenScriptsHandler(val jsons: MutableMap<String, String>) : KsoupHtmlHandler {

    private var inScriptTag: String? = null
    private var idCounter = 0

    override fun onOpenTag(name: String, attributes: Map<String, String>, isImplied: Boolean) {
        if (name.lowercase() == "script")
            inScriptTag = attributes["id"] ?: "id${idCounter++}"
    }

    override fun onText(text: String) {
        if (inScriptTag != null)
            jsons[inScriptTag!!] = text
    }

    override fun onCloseTag(name: String, isImplied: Boolean) {
        if (name.lowercase() == "script")
            inScriptTag = null
    }
}
