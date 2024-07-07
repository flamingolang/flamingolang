package objects.base.callable

import objects.base.FlClass
import objects.base.FlObject
import objects.base.TrustedFlClass
import objects.callable.*
import runtime.Frame
import runtime.OperationalFrame


class FlFunctionObj(
    val codeObj: FlCodeObj,
    parameters: ParameterSpec,
    cls: FlClass = FlFunctionClass,
    readOnly: Boolean = true,
    ) : FlCallableObj<OperationalFrame>(parameters, cls, readOnly = readOnly) {
    override fun makeFrame(locals: HashMap<String, FlObject>): OperationalFrame {
        val frame = OperationalFrame(
            parameters.name,
            codeObj.operations,
            closure = codeObj.nativeClosure,
            initLocals = locals,
            filePath = codeObj.filePath
        )
        return frame
    }

    override fun performCall(callContext: KtCallContext): FlObject? {
        val result = runtime.call()
        return result
    }
}

object FlFunctionClass : TrustedFlClass("Function", listOf(FlCallableClass))

class FlBoundMethodObj(
    val self: FlObject, val callable: FlCallableObj<*>, readOnly: Boolean = true
) : FlCallableObj<Frame>(callable.parameters, FlBoundMethodClass, readOnly = readOnly) {
    override fun makeFrame(locals: HashMap<String, FlObject>): Frame {
        val frame = callable.makeFrame(locals)
        frame.locals.updateContext(self)
        return frame
    }

    override fun performCall(callContext: KtCallContext): FlObject? = callable.performCall(callContext)
}

object FlBoundMethodClass : TrustedFlClass("Bound Method", listOf(FlCallableClass))