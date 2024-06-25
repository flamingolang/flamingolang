package objects.base

import runtime.throwObject

class FlamingoNumberObject(val number: Double, cls: FlamingoClass = FlamingoNumberClass, readOnly: Boolean = true) :
    FlamingoObject(cls, readOnly = readOnly) {
    fun assertGetInteger(what: String): Int? {
        if (number % 1.0 == 0.0) return number.toInt()
        throwObject(
            "%s %s type object was expected to be an whole number, not %s".format(what, cls.name, number),
            ValueError
        )
        return null
    }
}

val FlamingoNumberClass = TrustedFlamingoClass("Number")


fun numberOf(number: Double): FlamingoNumberObject {
    return FlamingoNumberObject(number)
}
