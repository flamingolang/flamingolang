package objects.base.collections

import objects.base.FlamingoClass
import objects.base.FlamingoObject
import objects.base.TrustedFlamingoClass

class FlamingoRangeObject(
    val range: Pair<FlamingoObject, FlamingoObject>,
    cls: FlamingoClass = FlamingoRangeClass,
    readOnly: Boolean = true
) : FlamingoObject(cls, readOnly = readOnly)

val FlamingoRangeClass = TrustedFlamingoClass("Range")