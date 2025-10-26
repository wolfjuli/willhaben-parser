package solutions.lykos.willhaben.parser.backend.api.messages.snd

import solutions.lykos.willhaben.parser.backend.api.messages.rcv.WSMessage

 class Pong(val data: Any) : WSMessage<Pong>() {
    init {
        type = "Pong"
    }
}