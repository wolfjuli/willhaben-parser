package solutions.lykos.willhaben.parser.backend.api.messages.rcv


data class GetAttributes( val ids: Set<Int> ) : WSMessage<List<Map<String, Any?>>>()