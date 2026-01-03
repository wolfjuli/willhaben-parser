package solutions.lykos.willhaben.parser.backend.api.websocket

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import solutions.lykos.willhaben.parser.backend.api.websocket.rcv.*
import solutions.lykos.willhaben.parser.backend.camelCase
import solutions.lykos.willhaben.parser.backend.database.postgresql.QueryTemplateProvider
import solutions.lykos.willhaben.parser.backend.database.postgresql.Transaction

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.EXTERNAL_PROPERTY, property = "type")
@JsonSubTypes(
    JsonSubTypes.Type(Ping::class, name = "ping"),
    JsonSubTypes.Type(GetAttributes::class, name = "getAttributes"),
    JsonSubTypes.Type(GetListings::class, name = "getListings"),
    JsonSubTypes.Type(CrawlListing::class, name = "crawlListing"),
    JsonSubTypes.Type(CreateListing::class, name = "createListing"),
    JsonSubTypes.Type(GetSorting::class, name = "getSorting"),
    JsonSubTypes.Type(SetAttribute::class, name = "setAttribute"),
    JsonSubTypes.Type(SetListingUserAttribute::class, name = "setListingUserAttribute"),
)
@JsonIgnoreProperties(ignoreUnknown = true)
abstract class WSMessage {
    var type: String = this::class.java.simpleName.camelCase()
    var id: Long = 0

    abstract fun respond(transaction: Transaction, templates: QueryTemplateProvider): Any?
}