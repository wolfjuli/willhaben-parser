package solutions.lykos.willhaben.parser.backend.importer.exceptions

import java.util.concurrent.CancellationException


class PipelineException(
    val baseException: Exception,
    var handled: Boolean = false
) : CancellationException(baseException.message)
