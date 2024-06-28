package runtime

import objects.base.*
import java.util.*

val vmCallStack = Stack<Frame>()
var vmThrown: FlThrowableObj? = null

/**
 * @return the top most frame on the call stack
 */
fun topCall(): Frame = vmCallStack.peek()

/**
 * @param frame the frame to add to the top of the call stack
 */
fun addCall(frame: Frame) {
    vmCallStack.push(frame)
}

/**
 * @return the topmost frame on the callstack
 */
fun popCall(): Frame {
    return vmCallStack.pop()
}

/**
 * @return the top most object on the topmost frame's object stack
 */
fun topObj(): FlObject = vmCallStack.peek().stack.peek()

/**
 * @param obj the object to add to the topmost frame's object stack
 */
fun addObj(obj: FlObject) {
    vmCallStack.peek().stack.push(obj)
}

/**
 * @return the topmost object on the topmost frame's object stack
 */
fun popObj(): FlObject = vmCallStack.peek().stack.pop()

val vmThrownStack = Stack<FlThrowableObj>()

fun throwObj(throwable: FlThrowableObj) {
    vmThrown = throwable
    vmThrownStack.push(throwable)
}

fun throwObj(message: String, cls: FlClass) =
    throwObj(FlThrowableObj(message, cls))


/**
 * Completes all instructions on the current frame including when frames are pushed to the stack,
 * when the initial frame has finished, it returns the value.
 * This function should not be called when there are no frames on the call stack.
 * This function should not be used. instead it is advised to use execute
 *
 * @see execute
 */
fun call(): FlObject? {
    if (vmCallStack.isEmpty()) {
        throwObj("Call to vm tried to execute without any frames on the call stack", FatalError)
        return null
    }

    val frame = topCall()

    while (true) {
        if (vmCallStack.size > 1000) {
            throwObj("Call stack exceeded maximum recursion limit (1000)", StackOverflowFatality)
            return null
        }

        val currFrame = topCall()

        if (vmThrown != null) {
            if (currFrame.canHandleError()) {
                if (!currFrame.handleError()) return null
            } else return null
        }

        if (!currFrame.hasFinished()) {
            if (currFrame is OperationalFrame && currFrame.matchOperation(OpCode.RETURN_VALUE)) {
                currFrame.ip++
                val returnValue = popObj()
                if (currFrame == frame) return returnValue
                popCall()
                addObj(returnValue)
            } else {
                currFrame.next()
            }
        } else {
            if (currFrame == frame) {
                return Null
            } else {
                popCall()
                addObj(Null)
            }
        }
    }
}

/*
fun builtinFunctionFrameGlance(frame: BuiltinFunctionFrame): String {
    val glance = StringBuilder("%s(".format(frame.ktFunction.parameters.name))
    val arguments = ArrayList<String>()
    for ((key, entry) in frame.locals.entries) {
        arguments.add("%s: %s".format(key, entry.value.cls.name))
    }
    glance.append(arguments.joinToString(", ")).append(')')
    return glance.toString()
}
 */

fun printError(error: FlThrowableObj, stackSnapshot: Collection<Frame>) {
    if (stackSnapshot.isNotEmpty()) {
        printRedLine("==( Callstack, most recent call last )==")
        for (frame in stackSnapshot) {
            if (frame is OperationalFrame) {
                val operation = if (frame.hasFinished()) frame.lastOperation() else frame.currentOperation()

                if (operation is CompiledOperation) {
                    printRedLine(
                        "    in %s, file \"%s\" at (%d, %d):".format(
                            frame.name,
                            operation.token.lexer.name,
                            operation.token.lineStart + 1,
                            operation.token.columnStart + 1
                        )
                    )
                    printRedLine("        " + operation.token.lineString())
                    printRedLine("        " + operation.token.underlineString())
                    continue
                }
            }
            /* else if (frame is BuiltinFunctionFrame) {
                val contextObj = frame.locals.getContextObjOrNull()
                val context = if (contextObj != null) " of %s".format(contextObj.cls.name) else ""
                printRedLine("    in internal %s%s".format(builtinFunctionFrameGlance(frame), context))
                printRedLine("        ...")
                continue
            }

            printRedLine("    at %s".format(frame.name))
             */
        }
    }
    printRedLine("==( %s : %s )==".format(error.cls.name, error.message))
    if (error is FlCompilerErrorObj) {
        printRedLine(
            "    in \"%s\" at (%s, %s):".format(
                error.token.lexer.name,
                intRangeFormat(error.token.lineStart, error.token.lineEnd),
                intRangeFormat(error.token.columnStart, error.token.columnEnd)
            )
        )
        printRedLine("        " + error.token.lineString())
        printRedLine("        " + error.token.underlineString())
    }
}

private fun intRangeFormat(a: Int, b: Int) = if (a == b) a.toString() else "%d:%d".format(a, b)

const val RESET = "\u001B[0m"
const val RED = "\u001B[31m"
const val BRIGHT_GREEN = "\u001B[32;1m"
const val BRIGHT_BLUE = "\u001B[34;1m"

fun printRedLine(message: String) = println(RED + message + RESET)
fun printGreenLine(message: String) = println(BRIGHT_GREEN + message + RESET)
fun printBlueLine(message: String) = println(BRIGHT_BLUE + message + RESET)