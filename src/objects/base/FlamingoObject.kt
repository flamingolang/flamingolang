package objects.base

import objects.base.callable.FlBoundMethodObj
import objects.base.callable.FlFunctionObj
import objects.base.collections.FlArrayObj
import objects.base.collections.FlDictionaryObj
import objects.base.collections.FlListObj
import objects.base.collections.FlRangeObj
import objects.callable.FlBuiltinObj
import objects.callable.FlCallableObj
import objects.callable.FlCodeObj
import objects.libraries.FlModuleObj
import runtime.throwObj
import java.util.*
import kotlin.reflect.KClass
import kotlin.reflect.cast


data class AttributeEntry(var value: FlObject, val constant: Boolean)

fun infraTypeName(type: KClass<out FlObject>) = when (type) {
    FlReflectObj::class -> "class"
    FlStringObj::class -> "string"
    FlListObj::class -> "list"
    FlArrayObj::class -> "array"
    FlDictionaryObj::class -> "dictionary"
    FlCodeObj::class -> "code"
    FlBooleanObj::class -> "boolean"
    FlNumberObj::class -> "number"
    FlAtomicNumObj::class -> "atomic number"
    FlFunctionObj::class -> "function"
    FlBuiltinObj::class -> "builtin"
    FlCallableObj::class -> "callable"
    FlModuleObj::class -> "module"
    FlRangeObj::class -> "range"
    else -> "object"
}


open class FlObject(val cls: FlClass, private val readOnly: Boolean = true) {
    val attributes = HashMap<String, AttributeEntry>()

    fun isOfClass(someClass: FlClass) = cls.aro.contains(someClass)
    fun isOfClass(someClass: FlReflectObj) = cls.aro.contains(someClass.reflectingClass)

    fun isOfType(type: KClass<out FlObject>): Boolean = type.isInstance(this)
    fun <T : FlObject> assertCast(what: String, type: KClass<T>): T? {
        if (type.isInstance(this)) return type.cast(this)
        throwObj(
            "'%s' was expected to be %s, but got %s".format(what, infraTypeName(type), infraTypeName(this::class)),
            TypeError
        )
        return null
    }

    fun bindSomeCallableAttribute(callable: FlObject): FlObject {
        if (callable is FlBoundMethodObj) return callable
        else if (callable is FlCallableObj<*> && callable.getAttributeOrNull(
                "<flag:static>",
                aroCheck = false
            ) == null
        ) return FlBoundMethodObj(this, callable)
        return callable
    }

    open fun getAttributeOrNull(name: String, aroCheck: Boolean = true, bind: Boolean = true): FlObject? {
        if (this is FlReflectObj) attributes[name]?.let { return it.value }
        else attributes[name]?.let { return if (bind) bindSomeCallableAttribute(it.value) else it.value }


        if (aroCheck) for (clsObj in cls.aro) {
            clsObj.getClassAttribute(name)?.let { return if (bind) bindSomeCallableAttribute(it) else it }
        }
        return null
    }

    fun <T : FlObject> getAttributeOfType(
        name: String,
        type: KClass<T>,
        aroCheck: Boolean = true,
        bind: Boolean = true
    ): T? =
        getAttribute(name, aroCheck = aroCheck, bind = bind)?.let { return it.assertCast("%s".format(name), type) }


    fun getAttributeOrDefault(
        name: String,
        default: FlObject,
        aroCheck: Boolean = true,
        bind: Boolean = true
    ): FlObject {
        return getAttributeOrNull(name, aroCheck = aroCheck, bind = bind) ?: default
    }

    fun getAttribute(name: String, aroCheck: Boolean = true, bind: Boolean = true): FlObject? {
        getAttributeOrNull("meta\$getter\$${name}")?.let { return it.call() }
        getAttributeOrNull(name, aroCheck = aroCheck, bind = bind)?.let { return it }
        throwObj("%s type object has no attribute '%s'".format(cls.name, name), AttributeError)
        return null
    }

    fun canSetAttribute(name: String): Boolean {
        if (readOnly) return false
        else attributes[name]?.let { return it.constant }
        return true
    }

    fun setAttribute(name: String, value: FlObject, constant: Boolean = true): Unit? {
        if (readOnly) {
            throwObj("%s type object is a read only object".format(cls.name), AttributeError)
            return null
        }

        getAttributeOrNull("meta\$setter\$${name}")?.let {
            it.call(listOf(value)) ?: return null
            return Unit
        }

        val entry = attributes.getOrPut(name) { AttributeEntry(Null, constant) }
        if (entry.constant) {
            throwObj("'%s' is a constant attribute and can't be changed".format(name), AttributeError)
            return null
        }

        entry.value = value

        return Unit
    }

    protected fun setAttributeInternal(name: String, value: FlObject, constant: Boolean = true) {
        attributes[name] = AttributeEntry(value, constant)
    }

    // helper shorthand methods

    fun callAttribute(
        name: String,
        arguments: Collection<FlObject> = listOf(),
        keywords: SequencedMap<String, FlObject> = sortedMapOf()
    ): FlObject? {
        val attribute = getAttribute(name) ?: return null
        return attribute.call(arguments, keywords)
    }

    fun <T : FlObject> callAttributeAssertCast(
        name: String,
        type: KClass<T>,
        arguments: Collection<FlObject> = listOf(),
        keywords: SequencedMap<String, FlObject> = sortedMapOf()
    ): T? {
        val callResult = callAttribute(name, arguments = arguments, keywords = keywords) ?: return null
        return callResult.assertCast("return result of %s.%s call".format(cls.name, name), type)
    }

    // outward facing helper shorthand methods

    fun call(
        arguments: Collection<FlObject> = listOf(),
        keywords: SequencedMap<String, FlObject> = sortedMapOf()
    ): FlObject? {
        if (this is FlCallableObj<*>) return handleCall(arguments, keywords)

        val callMethodOfSelf = getAttributeOfType("meta\$call", FlCallableObj::class) ?: return null
        return callMethodOfSelf.handleCall(arguments, keywords)
    }

    fun add(operand: FlObject) = callAttribute("meta\$add", listOf(operand))
    fun sub(operand: FlObject) = callAttribute("meta\$sub", listOf(operand))
    fun mul(operand: FlObject) = callAttribute("meta\$mul", listOf(operand))
    fun div(operand: FlObject) = callAttribute("meta\$div", listOf(operand))
    fun pow(operand: FlObject) = callAttribute("meta\$pow", listOf(operand))
    fun mod(operand: FlObject) = callAttribute("meta\$mod", listOf(operand))

    fun eq(operand: FlObject) = callAttribute("meta\$eq", listOf(operand))
    fun neq(operand: FlObject) = callAttribute("meta\$neq", listOf(operand))
    fun lt(operand: FlObject) = callAttribute("meta\$lt", listOf(operand))
    fun gt(operand: FlObject) = callAttribute("meta\$gt", listOf(operand))
    fun lteq(operand: FlObject) = callAttribute("meta\$lteq", listOf(operand))
    fun gteq(operand: FlObject) = callAttribute("meta\$gteq", listOf(operand))

    fun index(index: FlObject) = callAttribute("meta\$index", listOf(index))
    fun setAtIndex(index: FlObject, value: FlObject) =
        callAttribute("indexSet", listOf(index, value))

    fun iter(): FlObject? {
        val isIterable = callAttributeAssertCast("meta\$isIterable", FlBooleanObj::class) ?: return null
        if (isIterable.boolean) return callAttribute("meta\$iter")
        throwObj("%s type object is not iterable".format(cls.name), TypeError)
        return null
    }

    fun truthy() = callAttributeAssertCast("meta\$truthy", FlBooleanObj::class, listOf())?.boolean

    fun stringConcat() = callAttributeAssertCast("meta\$toString", FlStringObj::class, listOf())?.string
    fun stringShow() = callAttributeAssertCast("meta\$displayObject", FlStringObj::class, listOf())?.string

    open fun displaySafe() = "<%s 0x%x>".format(cls.name, hashCode())

    fun createSuper() = FlSuperObj(this)
}


class FlSuperObj(val self: FlObject) : FlObject(FlSuperClass, true) {
    override fun getAttributeOrNull(name: String, aroCheck: Boolean, bind: Boolean): FlObject? {
        if (aroCheck && self.cls.aro.size > 2) for (clsObj in self.cls.aro.subList(1, self.cls.aro.size - 1)) {
            clsObj.getClassAttribute(name)?.let { return self.bindSomeCallableAttribute(it) }
        }

        return super.getAttributeOrNull(name, aroCheck, bind = bind)
    }
}

val FlSuperClass = TrustedFlClass("super")


class FlReflectObj(val reflectingClass: FlClass, readOnly: Boolean = true) :
    FlObject(FlReflectClass, readOnly = readOnly)

val FlNullClass = TrustedFlClass("null")
val Null = FlObject(FlNullClass)