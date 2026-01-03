package solutions.lykos.willhaben.parser.backend.api.websocket.rcv


data class GetListings(val ids: List<Int>?, val knownMd5: List<String>?, val willhabenIds: List<String>?) : WSListMessage()