package runtime

import objects.base.FlamingoObject
import objects.base.FlamingoThrowableObject

data class ExecutionResult(
    val frame: Frame?,
    val result: FlamingoObject?,
    val thrown: FlamingoThrowableObject?,
    val stackSnapshot: List<Frame>
)

/*
    REPL
    INVOKE_WITH
    (2)
    - ANONYMOUS

 */


fun execute(frame: Frame? = null, propagateError: Boolean = true): ExecutionResult {
    val initCallStackSize = vmCallStack.size
    frame?.let { addCall(it) }
    val callResult = call()
    val executionResult = ExecutionResult(frame, callResult, vmThrown, vmCallStack.toList())

    if (vmThrown != null) {
        if (propagateError) return executionResult
        vmThrown = null
        topCall().stack.clear()
        while (vmCallStack.size > initCallStackSize) vmCallStack.pop()
    } else {
        frame?.let { popCall() }
    }

    // call stack should always be the same size as it was at the start

    return executionResult
}
