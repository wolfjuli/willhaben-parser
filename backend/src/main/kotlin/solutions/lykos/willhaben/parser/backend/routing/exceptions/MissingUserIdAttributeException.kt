package solutions.lykos.willhaben.parser.backend.routing.exceptions

class MissingUserIdAttributeException :
    NoSuchElementException("User ID attribute not set") {
    override val message: String
        get() = super.message!!
}
