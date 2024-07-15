package objects.callable

import compile.Scope
import objects.base.ArgumentError
import objects.base.FlClass
import objects.base.FlObject
import objects.base.TrustedFlClass
import objects.base.collections.FlDictionaryObj
import objects.base.collections.FlListObj
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
data class PartialCodeObj(
    val name: String,
    val scope: Scope,
    val filePath: String? = null,
    val comment: String? = null
)

data class ParameterSpec(
    val name: String,
    val positionals: Collection<String>? = null,
    val defaults: HashMap<String, FlObject>? = null,
    val varargs: String? = null,
    val varkwargs: String? = null
) {
    fun parseLocals(
        arguments: Collection<FlObject>, keywords: SequencedMap<String, FlObject>
    ): HashMap<String, FlObject>? {
        val positionalsSize = positionals?.size ?: 0

        if (arguments.size < positionalsSize) {
            throwObj(
                "$name given only ${arguments.size} positional arguments, but expected $positionalsSize", ArgumentError
            )
            return null
        }

        // now check if varargs is going to affect it
        if (varargs == null && arguments.size > positionalsSize) {
            throwObj(
                "$name given too many positional arguments (${arguments.size}), but expected only $positionalsSize",
                ArgumentError
            )
            return null
        }

        val locals = HashMap<String, FlObject>()
        if (defaults != null) locals.putAll(defaults)

        // get defaults out of the way

        val variadicKeywords = if (varkwargs != null) LinkedHashMap<String, FlObject>() else null

        for ((key, value) in keywords) {
            if (locals[key] != null) locals[key] = value
            else if (variadicKeywords != null) variadicKeywords[key] = value
            else {
                throwObj("$name has keyword argument $key", ArgumentError)
                return null
            }
        }

        val variadicArguments = if (varargs != null) mutableListOf<FlObject>() else null

        for ((i, argument) in arguments.withIndex()) {
            if (i < positionalsSize) locals[positionals!!.elementAt(i)] = argument
            else variadicArguments!!.add(argument)
        }


        if (variadicArguments != null) locals[varargs!!] = FlListObj(variadicArguments)
        if (variadicKeywords != null) locals[varkwargs!!] = FlDictionaryObj(variadicKeywords)

        return locals
    }
}


class KtCallContext(val frame: Frame) {
    fun getLocal(name: String): FlObject? = frame.locals.get(name)

    fun <T : FlObject> getLocalOfType(name: String, type: KClass<T>): T? {
        val local = getLocal(name) ?: return null
        return local.assertCast(name, type)
    }

    fun getObjContext() = frame.locals.getContextObj()
    fun <T : FlObject> getObjContextOfType(type: KClass<T>): T? {
        val self = getObjContext() ?: return null
        return self.assertCast("self", type)
    }
}

abstract class KtFunction(val parameters: ParameterSpec) {
    abstract fun accept(callContext: KtCallContext): FlObject?
}

abstract class FlCallableObj<T : Frame>(
    val parameters: ParameterSpec, cls: FlClass, readOnly: Boolean = true
) : FlObject(cls, readOnly = readOnly) {
    abstract fun makeFrame(locals: HashMap<String, FlObject>): T
    abstract fun performCall(callContext: KtCallContext): FlObject?
    fun handleCall(
        arguments: Collection<FlObject>, keywords: SequencedMap<String, FlObject>
    ): FlObject? {
        val locals = parameters.parseLocals(arguments, keywords) ?: return null
        val frame = makeFrame(locals)
        addCall(frame)
        val result = performCall(KtCallContext(frame)) ?: return null
        popCall()
        return result
    }
}

object FlCallableClass : TrustedFlClass("Callable")

class FlBuiltinObj(
    val callable: KtFunction, cls: FlClass = FlBuiltinClass, readOnly: Boolean = true
) : FlCallableObj<BuiltinFunctionFrame>(callable.parameters, cls, readOnly = readOnly) {
    override fun makeFrame(locals: HashMap<String, FlObject>): BuiltinFunctionFrame {
        val frame = BuiltinFunctionFrame(callable.parameters.name)
        frame.locals.setAll(locals)
        return frame
    }

    override fun performCall(callContext: KtCallContext) = callable.accept(callContext)
}

object FlBuiltinClass : TrustedFlClass("Builtin", listOf(FlCallableClass))