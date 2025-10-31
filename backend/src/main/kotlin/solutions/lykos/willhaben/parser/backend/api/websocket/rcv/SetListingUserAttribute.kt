package solutions.lykos.willhaben.parser.backend.api.websocket.rcv

data class SetListingUserAttribute(val attributeId: Int, val listingId: Int, val value: String?  ) : WSSingleMessage()
