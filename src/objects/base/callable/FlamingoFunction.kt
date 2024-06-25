package objects.callable

import objects.base.FlamingoClass
import objects.base.FlamingoObject
import objects.base.TrustedFlamingoClass
import runtime.Frame
import runtime.OperationalFrame


class FlamingoFunctionObject(
    val codeObject: FlamingoCodeObject,
    parameters: ParameterSpec,
    cls: FlamingoClass = FlamingoFunctionClass,
    readOnly: Boolean = true
) : FlamingoCallableObject<OperationalFrame>(parameters, cls, readOnly = readOnly) {
    override fun makeFrame(locals: HashMap<String, FlamingoObject>): OperationalFrame {
        val frame = OperationalFrame(
            parameters.name,
            codeObject.operations,
            closure = codeObject.nativeClosure,
            initLocals = locals,
            filePath = codeObject.filePath
        )
        return frame
    }

    override fun performCall(callContext: KtCallContext): FlamingoObject? = runtime.call()
}

object FlamingoFunctionClass : TrustedFlamingoClass("Function", listOf(FlamingoCallableClass))

class FlamingoBoundMethodObject(
    val self: FlamingoObject, val callable: FlamingoCallableObject<*>, readOnly: Boolean = true
) : FlamingoCallableObject<Frame>(callable.parameters, FlamingoBoundMethodClass, readOnly = readOnly) {
    override fun makeFrame(locals: HashMap<String, FlamingoObject>): Frame {
        val frame = callable.makeFrame(locals)
        frame.locals.context = self
        return frame
    }

    override fun performCall(callContext: KtCallContext): FlamingoObject? = callable.performCall(callContext)
}

object FlamingoBoundMethodClass : TrustedFlamingoClass("Bound Method", listOf(FlamingoCallableClass))