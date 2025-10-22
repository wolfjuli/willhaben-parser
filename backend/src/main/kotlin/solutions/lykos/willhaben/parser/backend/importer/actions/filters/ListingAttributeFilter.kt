package solutions.lykos.willhaben.parser.backend.importer.actions.filters

import solutions.lykos.willhaben.parser.backend.database.postgresql.Transaction
import solutions.lykos.willhaben.parser.backend.importer.HashMapping
import solutions.lykos.willhaben.parser.backend.importer.basedata.ListingAttribute
import solutions.lykos.willhaben.parser.backend.importer.pipelines.PipelineMessage
import java.util.*

class ListingAttributeFilter(toggleFlags: EnumSet<PipelineMessage.Flags> = EnumSet.of(PipelineMessage.Flags.WRITE)) :
    Filter.FilterElements<ListingAttribute>(toggleFlags) {

    private val hashObjects: HashMapping = mutableMapOf()

    override fun checkElement(message: PipelineMessage.Payload<ListingAttribute>) {
        TODO("Not yet implemented")
    }

    override fun initResolving(transaction: Transaction) {
        super.initResolving(transaction)

    }

    override fun updateResolving(transaction: Transaction) {


    }
}
