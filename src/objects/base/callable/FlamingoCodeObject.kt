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
    fun callLetting(initLocals: Map<String, FlamingoObject>? = null): FlamingoObject? {
        val execution = execute(getFrame(initLocals))
        execution.result?.let { return it }
        return null
    }
    fun getFrame(initLocals: Map<String, FlamingoObject>? = null) = OperationalFrame(name, operations, closure = nativeClosure, initLocals = initLocals)
}

object FlamingoCodeObjectClass : TrustedFlamingoClass("Code")