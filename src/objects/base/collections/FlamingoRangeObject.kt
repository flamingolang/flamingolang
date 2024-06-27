package objects.base.collections

import objects.base.FlClass
import objects.base.FlObject
import objects.base.TrustedFlClass

class FlRangeObj(
    val range: Pair<FlObject, FlObject>,
    cls: FlClass = FlRangeClass,
    readOnly: Boolean = true
) : FlObject(cls, readOnly = readOnly)

val FlRangeClass = TrustedFlClass("Range")