package solutions.lykos.willhaben.parser.backend.api.messages.snd

import solutions.lykos.willhaben.parser.backend.api.messages.rcv.WSMessage

data class ErrorMessage(
    val message: String,
    val request: WSMessage<*>,
    val details: String = ""
) : WSMessage<ErrorMessage>() {
    init {
        type = "error"
    }
}