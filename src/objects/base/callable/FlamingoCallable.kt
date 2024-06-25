package objects.callable

import compile.Scope
import objects.base.ArgumentError
import objects.base.FlamingoClass
import objects.base.FlamingoObject
import objects.base.TrustedFlamingoClass
import objects.base.collections.FlamingoDictionaryObject
import objects.base.collections.FlamingoListObject
import runtime.*
import java.util.*
import kotlin.reflect.KClass

data class PartialFunction(
    val isGenerator: Boolean,
    val positionals: List<String>?,
    val defaults: List<String>?,
    val varargs: String?,
    val varkwargs: String?,
)

data class CallSpec(val arguments: Int, val keywords: List<String>)
data class PartialCodeObject(
    val name: String,
    val scope: Scope,
    val filePath: String? = null
)

data class ParameterSpec(
    val name: String,
    val positionals: Collection<String>? = null,
    val defaults: HashMap<String, FlamingoObject>? = null,
    val varargs: String? = null,
    val varkwargs: String? = null
) {
    fun parseLocals(
        arguments: Collection<FlamingoObject>, keywords: SequencedMap<String, FlamingoObject>
    ): HashMap<String, FlamingoObject>? {
        val positionalsSize = positionals?.size ?: 0

        if (arguments.size < positionalsSize) {
            throwObject(
                "$name given only ${arguments.size} positional arguments, but expected $positionalsSize", ArgumentError
            )
            return null
        }

        // now check if varargs is going to affect it
        if (varargs == null && arguments.size > positionalsSize) {
            throwObject(
                "$name given too many positional arguments (${arguments.size}), but expected only $positionalsSize",
                ArgumentError
            )
            return null
        }

        val locals = HashMap<String, FlamingoObject>()
        if (defaults != null) locals.putAll(defaults)

        // get defaults out of the way

        val variadicKeywords = if (varkwargs != null) LinkedHashMap<String, FlamingoObject>() else null

        for ((key, value) in keywords) {
            if (locals[key] != null) locals[key] = value
            else if (variadicKeywords != null) variadicKeywords[key] = value
            else {
                throwObject("$name has keyword argument $key", ArgumentError)
                return null
            }
        }

        val variadicArguments = if (varargs != null) mutableListOf<FlamingoObject>() else null

        for ((i, argument) in arguments.withIndex()) {
            if (i < positionalsSize) locals[positionals!!.elementAt(i)] = argument
            else variadicArguments!!.add(argument)
        }


        if (variadicArguments != null) locals[varargs!!] = FlamingoListObject(variadicArguments)
        if (variadicKeywords != null) locals[varkwargs!!] = FlamingoDictionaryObject(variadicKeywords)

        return locals
    }
}


class KtCallContext(val frame: Frame) {
    fun getLocal(name: String): FlamingoObject? = frame.locals.get(name)

    fun <T : FlamingoObject> getLocalOfType(name: String, type: KClass<T>): T? {
        val local = getLocal(name) ?: return null
        return local.assertCast(name, type)
    }

    fun getObjectContext() = frame.locals.getContextObject()
    fun <T : FlamingoObject> getObjectContextOfType(type: KClass<T>): T? {
        val self = getObjectContext() ?: return null
        return self.assertCast("self", type)
    }
}

abstract class KtFunction(val parameters: ParameterSpec) {
    abstract fun accept(callContext: KtCallContext): FlamingoObject?
}

abstract class FlamingoCallableObject<T : Frame>(
    val parameters: ParameterSpec, cls: FlamingoClass, readOnly: Boolean = true
) : FlamingoObject(cls, readOnly = readOnly) {
    abstract fun makeFrame(locals: HashMap<String, FlamingoObject>): T
    abstract fun performCall(callContext: KtCallContext): FlamingoObject?
    fun handleCall(
        arguments: Collection<FlamingoObject>, keywords: SequencedMap<String, FlamingoObject>
    ): FlamingoObject? {
        val locals = parameters.parseLocals(arguments, keywords) ?: return null
        val frame = makeFrame(locals)
        addCall(frame)
        val result = performCall(KtCallContext(frame)) ?: return null
        popCall()
        return result
    }
}

object FlamingoCallableClass : TrustedFlamingoClass("Callable")

class FlamingoBuiltinFun(
    val callable: KtFunction, cls: FlamingoClass = FlamingoBuiltinClass, readOnly: Boolean = true
) : FlamingoCallableObject<BuiltinFunctionFrame>(callable.parameters, cls, readOnly = readOnly) {
    override fun makeFrame(locals: HashMap<String, FlamingoObject>): BuiltinFunctionFrame {
        val frame = BuiltinFunctionFrame(callable.parameters.name, callable)
        frame.locals.setAll(locals)
        return frame
    }

    override fun performCall(callContext: KtCallContext) = callable.accept(callContext)
}

object FlamingoBuiltinClass : TrustedFlamingoClass("Builtin", listOf(FlamingoCallableClass))