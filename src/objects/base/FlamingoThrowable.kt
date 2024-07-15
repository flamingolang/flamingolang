package objects.base

import compile.Token

open class FlThrowableObj(val message: String, cls: FlClass, readOnly: Boolean = false) :
    FlObject(cls, readOnly)

class FlCompilerErrorObj(message: String, val token: Token, cls: FlClass = SyntaxError) :
    FlThrowableObj(message, cls, readOnly = true)

val Throwable = TrustedFlClass("Throwable")

// anything that doesn't have checking as advisory
val Exception = TrustedFlClass("Exception", listOf(Throwable))
val IterationException = TrustedFlClass("IterationException", listOf(Exception))
val ZeroDivisionException = TrustedFlClass("ZeroDivisionException", listOf(Exception))

// anything that should immediately halt execution to be dealt with
val FatalError = TrustedFlClass("Fatality", listOf(Throwable))

// val NotImplementedFatalError = TrustedFlClass("NotImplementedFatality", listOf(FatalError))
val ImportFatality = TrustedFlClass("ImportFatality", listOf(FatalError))
val StackOverflowFatality = TrustedFlClass("StackOverflow", listOf(FatalError))

// anything where checking is advisory
val Error = TrustedFlClass("Error", listOf(Throwable))
val TypeError = TrustedFlClass("TypeError", listOf(Error))
val NameError = TrustedFlClass("NameError", listOf(Error))
val ArgumentError = TrustedFlClass("ArgumentError", listOf(Error))
val AssignmentError = TrustedFlClass("AssignmentError", listOf(Error))
val SyntaxError = TrustedFlClass("SyntaxError", listOf(Error))

val SizeError = TrustedFlClass("SizeError", listOf(Error))
val ValueError = TrustedFlClass("ValueError", listOf(Error))
val IndexError = TrustedFlClass("IndexError", listOf(Error))
val AttributeError = TrustedFlClass("Error", listOf(Error))

