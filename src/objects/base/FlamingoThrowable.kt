package objects.base

import compile.Token

open class FlamingoThrowableObject(val message: String, cls: FlamingoClass, readOnly: Boolean = false) :
    FlamingoObject(cls, readOnly)

class FlamingoCompilerErrorObject(message: String, val token: Token, cls: FlamingoClass = SyntaxError) :
    FlamingoThrowableObject(message, cls, readOnly = true)


val Throwable = TrustedFlamingoClass("Throwable")

// anything that doesn't have checking as advisory
val Exception = TrustedFlamingoClass("Exception", listOf(Throwable))
val IterationException = TrustedFlamingoClass("IterationException", listOf(Exception))
val ZeroDivisionException = TrustedFlamingoClass("ZeroDivisionException", listOf(Exception))

// anything that should immediately halt execution to be dealt with
val FatalError = TrustedFlamingoClass("Fatality", listOf(Throwable))
// val NotImplementedFatalError = TrustedFlamingoClass("NotImplementedFatality", listOf(FatalError))
val ImportFatality = TrustedFlamingoClass("ImportFatality", listOf(FatalError))

// anything where checking is advisory
val Error = TrustedFlamingoClass("Error", listOf(Throwable))
val TypeError = TrustedFlamingoClass("TypeError", listOf(Error))
val NameError = TrustedFlamingoClass("NameError", listOf(Error))
val ArgumentError = TrustedFlamingoClass("ArgumentError", listOf(Error))
val AssignmentError = TrustedFlamingoClass("AssignmentError", listOf(Error))
val SyntaxError = TrustedFlamingoClass("SyntaxError", listOf(Error))

// val SizeError = TrustedFlamingoClass("SizeError", listOf(Error))
val ValueError = TrustedFlamingoClass("ValueError", listOf(Error))
val IndexError = TrustedFlamingoClass("IndexError", listOf(Error))
val AttributeError = TrustedFlamingoClass("Error", listOf(Error))