package solutions.lykos.willhaben.parser.backend

import tech.units.indriya.unit.Units
import javax.measure.Quantity
import javax.measure.quantity.Time

fun toSeconds(duration: Quantity<Time>): Long =
    duration.unit
        .getConverterTo(Units.SECOND)
        .convert(duration.value)
        .toLong()
