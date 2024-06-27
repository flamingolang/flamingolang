package objects.base

import runtime.throwObj

class FlNumberObj(val number: Double, cls: FlClass = FlNumberClass, readOnly: Boolean = true) :
    FlObject(cls, readOnly = readOnly) {
    fun assertGetInteger(what: String): Int? {
        if (number % 1.0 == 0.0) return number.toInt()
        throwObj(
            "%s %s type object was expected to be an whole number, not %s".format(what, cls.name, number),
            ValueError
        )
        return null
    }
}

val FlNumberClass = TrustedFlClass("Number")


fun numberOf(number: Double): FlNumberObj {
    return FlNumberObj(number)
}
