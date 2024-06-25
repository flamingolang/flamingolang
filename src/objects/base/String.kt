package objects.base

class FlamingoStringObject(val string: String, cls: FlamingoClass = FlamingoStringClass, readOnly: Boolean = true) :
    FlamingoObject(cls, readOnly = readOnly)

val FlamingoStringClass = TrustedFlamingoClass("String")


fun stringOf(string: String): FlamingoStringObject {
    return FlamingoStringObject(string)
}