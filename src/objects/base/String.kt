package objects.base

class FlStringObj(val string: String, cls: FlClass = FlStringClass, readOnly: Boolean = true) :
    FlObject(cls, readOnly = readOnly)

val FlStringClass = TrustedFlClass("String")


fun stringOf(string: String): FlStringObj {
    return FlStringObj(string)
}