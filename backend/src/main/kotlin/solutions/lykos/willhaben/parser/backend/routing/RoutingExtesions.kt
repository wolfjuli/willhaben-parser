package solutions.lykos.willhaben.parser.backend.routing

import io.ktor.server.application.*
import io.ktor.util.*
import solutions.lykos.willhaben.parser.backend.routing.exceptions.MissingUserIdAttributeException


val ADMIN_PORT_KEY = AttributeKey<Int>("Admin Port")
val APPLICATION_PORT_KEY = AttributeKey<Int>("Application Port")
val APPLICATION_ID_KEY = AttributeKey<String>("Application ID")
val APPLICATION_VERSION_KEY = AttributeKey<String>("Application Version")
val BRANDING_ID_KEY = AttributeKey<String>("Branding ID")
val COMPRESS_PRIVILEGE_IDS =
    AttributeKey<(String, Set<String>) -> Pair<Set<String>, Set<String>>>("Compress Privilege IDs")
val CONFIGURATION_KEY = AttributeKey<Any>("configuration")
val CONTEXT_PROVIDER_KEY = AttributeKey<(String) -> Any?>("Context Provider")
val THEME_ID_KEY = AttributeKey<String>("Theme ID")
val USER_ID_KEY = AttributeKey<String>("User ID")


val ApplicationCall.themeId: String
    get() = attributes.computeIfAbsent(THEME_ID_KEY) { DEFAULT_THEME_ID }

val ApplicationCall.userId: String
    get() {
        if (!attributes.contains(USER_ID_KEY)) {
            throw MissingUserIdAttributeException()
        }
        return attributes[USER_ID_KEY]
    }
