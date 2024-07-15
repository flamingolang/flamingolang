package objects.base

open class FlStringObj(open val string: String, cls: FlClass = FlStringClass, readOnly: Boolean = true) :
    FlObject(cls, readOnly = readOnly)

val FlStringClass = TrustedFlClass("String")


class FlAtomicStrObj(string: String, cls: FlClass = FlAtomicStringClass, readOnly: Boolean = true) :
    FlStringObj(string, cls, readOnly) {
    val stringBuilder = StringBuilder(string)

    override val string: String
        get() = stringBuilder.toString()
}

val FlAtomicStringClass = TrustedFlClass("AtomicString", listOf(FlStringClass))


fun stringOf(string: String): FlStringObj {
    return FlStringObj(string)
}