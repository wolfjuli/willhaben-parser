package solutions.lykos.willhaben.parser.backend.api.messages.rcv


data class GetListings(val ids: List<Int>?, val knownMd5: List<String>?) : WSMessage<List<Map<String, Any?>>>()