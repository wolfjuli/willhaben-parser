import solutions.lykos.willhaben.parser.backend.database.postgresql.Transaction
import solutions.lykos.willhaben.parser.backend.importer.actions.ActionSequence
import solutions.lykos.willhaben.parser.backend.importer.actions.Communicator
import solutions.lykos.willhaben.parser.backend.importer.actions.ResolvingActions
import solutions.lykos.willhaben.parser.backend.importer.actions.filters.UniqueFilter
import solutions.lykos.willhaben.parser.backend.importer.actions.resolvers.ListingResolver
import solutions.lykos.willhaben.parser.backend.importer.actions.transformers.ListingAttributeMultiplier
import solutions.lykos.willhaben.parser.backend.importer.actions.transformers.PipeTo
import solutions.lykos.willhaben.parser.backend.importer.actions.writers.AttributesWriter
import solutions.lykos.willhaben.parser.backend.importer.actions.writers.LastSeenWriter
import solutions.lykos.willhaben.parser.backend.importer.actions.writers.ListingPointWriter
import solutions.lykos.willhaben.parser.backend.importer.actions.writers.ListingWriter
import solutions.lykos.willhaben.parser.backend.importer.basedata.Attribute
import solutions.lykos.willhaben.parser.backend.importer.basedata.ListingAttribute
import solutions.lykos.willhaben.parser.backend.importer.pipelines.Pipeline
import solutions.lykos.willhaben.parser.backend.importer.pipelines.PipelineMessage
import java.time.ZonedDateTime
import java.util.*

class Importer(transaction: Transaction) {

    private val writeFlags = EnumSet.of(PipelineMessage.Flags.WRITE)
    private val resolveFlags = EnumSet.noneOf(PipelineMessage.Flags::class.java)

    private val now = ZonedDateTime.now()

    private  val attributeActions = ActionSequence(
        ListingAttributeMultiplier(),
        AttributesWriter()
    )

    private val listingPointActions = ResolvingActions(
        ListingResolver(),
        ListingPointWriter()
    )

    private val listingActions = ResolvingActions(
        ListingResolver(),
        ListingWriter(),
        PipeTo(
            attributeActions,
            { ListingAttribute(this, Attribute(""), "") }
        ),
        Communicator(
            listingPointActions,
            { this },
            {me, _ -> me},
            writeFlags
        )
    )

    val listingPipeline = Pipeline(
        ActionSequence(
            LastSeenWriter(),
            UniqueFilter(),
            Communicator(
                listingActions,
                { this },
                { me, _ -> me },
                writeFlags
            )
        )
    )

    init {
        listingPipeline.initialize(transaction)
    }
}