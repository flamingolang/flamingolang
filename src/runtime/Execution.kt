package runtime

import objects.base.FlObject
import objects.base.FlThrowableObj

data class ExecutionResult(
    val frame: Frame?,
    val result: FlObject?,
    val thrown: FlThrowableObj?,
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
