package solutions.lykos.willhaben.parser.backend.api.websocket.rcv


data class GetAttributes( val ids: Set<Int>? ) : WSListMessage()