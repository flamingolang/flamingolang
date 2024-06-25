package objects.base.collections

import objects.base.FlamingoClass
import objects.base.FlamingoObject
import objects.base.TrustedFlamingoClass

class FlamingoArrayObject(
    val array: Array<FlamingoObject>,
    cls: FlamingoClass = FlamingoArrayClass,
    readOnly: Boolean = true
) : FlamingoObject(cls, readOnly = readOnly)

val FlamingoArrayClass = TrustedFlamingoClass("Array")