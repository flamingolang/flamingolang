package objects.callable

import objects.base.FlamingoClass
import objects.base.FlamingoObject
import objects.base.TrustedFlamingoClass
import runtime.NameTable
import runtime.Operation
import runtime.OperationalFrame
import runtime.execute

class FlamingoCodeObject(
    val name: String,
    val operations: Collection<Operation>,
    var filePath: String? = null,
    var nativeClosure: NameTable? = null,
    cls: FlamingoClass = FlamingoCodeObjectClass,
    readOnly: Boolean = true
) : FlamingoObject(cls, readOnly) {
    fun callLetting(locals: Map<String, FlamingoObject>? = null): FlamingoObject? {
        val frame = OperationalFrame(name, operations, closure = nativeClosure, initLocals = locals)
        val execution = execute(frame)
        execution.result?.let { return it }
        return null
    }
}

object FlamingoCodeObjectClass : TrustedFlamingoClass("Code")