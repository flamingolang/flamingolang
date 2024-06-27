package objects.base.collections

import objects.base.FlClass
import objects.base.FlObject
import objects.base.TrustedFlClass
import java.util.*

class FlDictionaryObj(
    val dictionary: SequencedMap<String, FlObject>,
    cls: FlClass = FlDictionaryClass,
    readOnly: Boolean = true
) : FlObject(cls, readOnly = readOnly) {
    override fun getAttributeOrNull(name: String, aroCheck: Boolean): FlObject? {
        dictionary[name] ?. let { return it }
        return super.getAttributeOrNull(name, aroCheck)
    }
}

val FlDictionaryClass = TrustedFlClass("Dictionary")