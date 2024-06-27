package objects.base

class FlBooleanObj(val boolean: Boolean, cls: FlClass = FlBooleanClass, readOnly: Boolean = true) :
    FlObject(cls, readOnly = readOnly)

val FlBooleanClass = TrustedFlClass("Boolean")

val True = FlBooleanObj(true)
val False = FlBooleanObj(false)

fun booleanOf(boolean: Boolean): FlBooleanObj = if (boolean) True else False
