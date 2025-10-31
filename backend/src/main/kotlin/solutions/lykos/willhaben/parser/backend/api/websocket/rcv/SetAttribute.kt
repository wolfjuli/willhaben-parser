package solutions.lykos.willhaben.parser.backend.api.websocket.rcv

data class SetAttribute( val attribute: String, val label: String?, val dataType: String , val sortBy: Int? ) : WSSingleMessage()
