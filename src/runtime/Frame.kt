package runtime

import objects.base.FlamingoObject
import objects.callable.KtFunction
import java.util.*

/**
 * @param name the name of the frame (used by error tracing)
 */
open class Frame(val name: String, closure: NameTable? = null, initLocals: Map<String, FlamingoObject>? = null) {
    val stack = Stack<FlamingoObject>()

    var locals = NameTable(name, closure)

    init {
        initLocals?.let { locals.setAll(it) }
        // println("created %s 0x%x with 0x%x".format(name, this.hashCode(), closure?.hashCode()))
    }

    /**
     * @return if the frame has finished all operations (can be popped from the stack)
     */
    open fun hasFinished(): Boolean = true

    /**
     * if the frame has not finished, this method will be called to finish next operation
     */
    open fun next() {}

    open fun canHandleError(): Boolean = false
    open fun handleError(): Boolean = false
}

class OperationalFrame(
    name: String,
    private val operations: Collection<Operation>,
    closure: NameTable? = null,
    initLocals: Map<String, FlamingoObject>? = null,
    val filePath: String? = null
) : Frame(name, closure, initLocals) {
    var ip = 0
    val size
        get() = operations.size
    val errorJumpStack = Stack<Pair<Int, String?>>()

    override fun hasFinished(): Boolean = ip >= operations.size

    /**
     * executes the instruction at the inner frame ip
     */
    override fun next() {
        operations.elementAt(ip).execute(this)
        if (vmThrown == null) ip++
    }

    fun matchOperation(opCode: OpCode): Boolean {
        return operations.elementAt(ip).opCode == opCode
    }

    fun currentOperation() = operations.elementAt(ip)
    fun lastOperation() = operations.elementAt(ip - 1)

    override fun canHandleError() = errorJumpStack.isNotEmpty()

    override fun handleError(): Boolean {
        val error = vmThrown ?: return false
        vmThrown = null
        val handler = errorJumpStack.pop()
        val name = handler.second
        if (name != null) locals.set(name, error)
        ip = handler.first
        return true
    }
}


class BuiltinFunctionFrame(name: String, val ktFunction: KtFunction) : Frame(name)