package objects.base.collections

import objects.base.FlClass
import objects.base.FlObject
import objects.base.TrustedFlClass

class FlArrayObj(
    val array: Array<FlObject>,
    cls: FlClass = FlArrayClass,
    readOnly: Boolean = true
) : FlObject(cls, readOnly = readOnly)

val FlArrayClass = TrustedFlClass("Array")