package solutions.lykos.willhaben.parser.backend.crawler

import solutions.lykos.willhaben.parser.backend.importer.pipelines.PipelineMessage
import java.util.*


private val writeFlags = EnumSet.of(PipelineMessage.Flags.WRITE)
private val resolveFlags = EnumSet.noneOf(PipelineMessage.Flags::class.java)
