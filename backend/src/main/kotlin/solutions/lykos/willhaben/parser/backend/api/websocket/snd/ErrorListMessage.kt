package solutions.lykos.willhaben.parser.backend.api.websocket.snd

import solutions.lykos.willhaben.parser.backend.api.websocket.WSMessage

data class ErrorListMessage(
    val id : Long,
    val message: String,
    val request: WSMessage,
    val details: String = ""
)  {
    val type = "error"
}