package objects.base.collections

import objects.base.FlamingoClass
import objects.base.FlamingoObject
import objects.base.TrustedFlamingoClass

class FlamingoListObject(
    val list: MutableList<FlamingoObject>,
    cls: FlamingoClass = FlamingoListClass,
    readOnly: Boolean = true
) : FlamingoObject(cls, readOnly = readOnly)

val FlamingoListClass = TrustedFlamingoClass("List")

