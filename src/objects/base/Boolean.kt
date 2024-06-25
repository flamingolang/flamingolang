package objects.base

class FlamingoBooleanObject(val boolean: Boolean, cls: FlamingoClass = FlamingoBooleanClass, readOnly: Boolean = true) :
    FlamingoObject(cls, readOnly = readOnly)

val FlamingoBooleanClass = TrustedFlamingoClass("Boolean")

val True = FlamingoBooleanObject(true)
val False = FlamingoBooleanObject(false)

fun booleanOf(boolean: Boolean): FlamingoBooleanObject = if (boolean) True else False
