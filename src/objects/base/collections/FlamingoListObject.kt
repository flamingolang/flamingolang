package objects.base.collections

import objects.base.FlClass
import objects.base.FlObject
import objects.base.TrustedFlClass

class FlListObj(
    val list: MutableList<FlObject>,
    cls: FlClass = FlListClass,
    readOnly: Boolean = true
) : FlObject(cls, readOnly = readOnly)

val FlListClass = TrustedFlClass("List")

