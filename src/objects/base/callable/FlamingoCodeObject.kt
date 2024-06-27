package objects.callable

import objects.base.FlClass
import objects.base.FlObject
import objects.base.TrustedFlClass
import runtime.NameTable
import runtime.Operation
import runtime.OperationalFrame
import runtime.execute

class FlCodeObj(
    val name: String,
    val operations: Collection<Operation>,
    var filePath: String? = null,
    var nativeClosure: NameTable? = null,
    cls: FlClass = FlCodeObjClass,
    readOnly: Boolean = true
) : FlObject(cls, readOnly) {
    fun callLetting(initLocals: Map<String, FlObject>? = null): FlObject? {
        val execution = execute(getFrame(initLocals))
        execution.result?.let { return it }
        return null
    }
    fun getFrame(initLocals: Map<String, FlObject>? = null) = OperationalFrame(name, operations, closure = nativeClosure, initLocals = initLocals, filePath = filePath)
}

object FlCodeObjClass : TrustedFlClass("Code")