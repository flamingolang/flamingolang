package objects.base.collections

import objects.base.FlamingoClass
import objects.base.FlamingoObject
import objects.base.TrustedFlamingoClass
import java.util.*

class FlamingoDictionaryObject(
    val dictionary: SequencedMap<String, FlamingoObject>,
    cls: FlamingoClass = FlamingoDictionaryClass,
    readOnly: Boolean = true
) : FlamingoObject(cls, readOnly = readOnly)

val FlamingoDictionaryClass = TrustedFlamingoClass("Dictionary")