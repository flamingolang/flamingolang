package objects.base

import objects.base.callable.FlamingoBoundMethodObject
import objects.callable.FlamingoCallableObject
import runtime.throwObject
import java.util.*
import kotlin.reflect.KClass
import kotlin.reflect.cast


data class AttributeEntry(var value: FlamingoObject, val constant: Boolean)


open class FlamingoObject(val cls: FlamingoClass, private val readOnly: Boolean = true) {
    val attributes = HashMap<String, AttributeEntry>()

    fun isOfClass(someClass: FlamingoClass) = cls.aro.contains(someClass)
    fun isOfClass(someClass: FlamingoReflectObject) = cls.aro.contains(someClass.reflectingClass)

    fun isOfType(type: KClass<out FlamingoObject>): Boolean = type.isInstance(this)
    fun <T : FlamingoObject> assertCast(what: String, type: KClass<T>): T? {
        if (type.isInstance(this)) return type.cast(this)
        throwObject("%s (%s) could not be cast into an acceptable type".format(what, this.cls.name), TypeError)
        return null
    }

    private fun bindSomeCallableAttribute(callable: FlamingoObject): FlamingoObject {
        if (callable is FlamingoBoundMethodObject) return callable
        else if (callable is FlamingoCallableObject<*> && callable.getAttributeOrNull(
                "flag:static",
                aroCheck = false
            ) == null
        ) return FlamingoBoundMethodObject(this, callable)
        return callable
    }

    open fun getAttributeOrNull(name: String, aroCheck: Boolean = true): FlamingoObject? {
        if (this is FlamingoReflectObject) attributes[name]?.let { return it.value }
        else attributes[name]?.let { return bindSomeCallableAttribute(it.value) }

        if (aroCheck) for (clsObject in cls.aro) {
            clsObject.getClassAttribute(name)?.let { return bindSomeCallableAttribute(it) }
        }
        return null
    }

    fun <T : FlamingoObject> getAttributeOfType(name: String, type: KClass<T>, aroCheck: Boolean = true): T? =
        getAttribute(name, aroCheck = aroCheck)?.let { return it.assertCast("%s".format(name), type) }


    fun getAttributeOrDefault(name: String, default: FlamingoObject, aroCheck: Boolean = true): FlamingoObject {
        return getAttributeOrNull(name, aroCheck = aroCheck) ?: default
    }

    fun getAttribute(name: String, aroCheck: Boolean = true): FlamingoObject? {
        getAttributeOrNull(name, aroCheck = aroCheck)?.let { return it }
        throwObject("%s type object has no attribute '%s'".format(cls.name, name), AttributeError)
        return null
    }

    fun canSetAttribute(name: String): Boolean {
        if (readOnly) return false
        else attributes[name]?.let { return it.constant }
        return true
    }

    fun setAttribute(name: String, value: FlamingoObject, constant: Boolean = true): Unit? {
        if (readOnly) {
            throwObject("%s type object is a read only object".format(cls.name), AttributeError)
            return null
        }

        val entry = attributes.getOrPut(name) { AttributeEntry(Null, constant) }
        if (entry.constant) {
            throwObject("'%s' is a constant attribute and can't be changed".format(name), AttributeError)
            return null
        }

        entry.value = value

        return Unit
    }

    protected fun setAttributeInternal(name: String, value: FlamingoObject, constant: Boolean = true) {
        attributes[name] = AttributeEntry(value, constant)
    }

    // helper shorthand methods

    fun callAttribute(
        name: String,
        arguments: Collection<FlamingoObject> = listOf(),
        keywords: SequencedMap<String, FlamingoObject> = sortedMapOf()
    ): FlamingoObject? {
        val attribute = getAttribute(name) ?: return null
        return attribute.call(arguments, keywords)
    }

    fun <T : FlamingoObject> callAttributeAssertCast(
        name: String,
        type: KClass<T>,
        arguments: Collection<FlamingoObject> = listOf(),
        keywords: SequencedMap<String, FlamingoObject> = sortedMapOf()
    ): T? {
        val callResult = callAttribute(name, arguments = arguments, keywords = keywords) ?: return null
        return callResult.assertCast("return result of %s.%s call".format(cls.name, name), type)
    }

    // outward facing helper shorthand methods

    fun call(
        arguments: Collection<FlamingoObject> = listOf(),
        keywords: SequencedMap<String, FlamingoObject> = sortedMapOf()
    ): FlamingoObject? {
        if (this is FlamingoCallableObject<*>) return handleCall(arguments, keywords)

        val callMethodOfSelf = getAttributeOfType("meta\$call", FlamingoCallableObject::class) ?: return null
        return callMethodOfSelf.handleCall(arguments, keywords)
    }

    fun add(operand: FlamingoObject) = callAttribute("meta\$add", listOf(operand))
    fun sub(operand: FlamingoObject) = callAttribute("meta\$sub", listOf(operand))
    fun mul(operand: FlamingoObject) = callAttribute("meta\$mul", listOf(operand))
    fun div(operand: FlamingoObject) = callAttribute("meta\$div", listOf(operand))
    fun pow(operand: FlamingoObject) = callAttribute("meta\$pow", listOf(operand))
    fun mod(operand: FlamingoObject) = callAttribute("meta\$mod", listOf(operand))

    fun eq(operand: FlamingoObject) = callAttribute("meta\$eq", listOf(operand))
    fun neq(operand: FlamingoObject) = callAttribute("meta\$neq", listOf(operand))
    fun lt(operand: FlamingoObject) = callAttribute("meta\$lt", listOf(operand))
    fun gt(operand: FlamingoObject) = callAttribute("meta\$gt", listOf(operand))
    fun lteq(operand: FlamingoObject) = callAttribute("meta\$lteq", listOf(operand))
    fun gteq(operand: FlamingoObject) = callAttribute("meta\$gteq", listOf(operand))

    fun index(index: FlamingoObject) = callAttribute("meta\$index", listOf(index))
    fun setAtIndex(index: FlamingoObject, value: FlamingoObject) =
        callAttribute("indexSet", listOf(index, value))

    fun iter(): FlamingoObject? {
        val isIterable = callAttributeAssertCast("meta\$isIterable", FlamingoBooleanObject::class) ?: return null
        if (isIterable.boolean) return callAttribute("meta\$iter")
        throwObject("%s type object is not iterable".format(cls.name), TypeError)
        return null
    }

    fun truthy() = callAttributeAssertCast("meta\$truthy", FlamingoBooleanObject::class, listOf())?.boolean

    fun stringConcat() = callAttributeAssertCast("meta\$toString", FlamingoStringObject::class, listOf())?.string
    fun stringShow() = callAttributeAssertCast("meta\$displayObject", FlamingoStringObject::class, listOf())?.string

    open fun displaySafe() = "<%s 0x%x>".format(cls.name, hashCode())
}

class FlamingoReflectObject(val reflectingClass: FlamingoClass, readOnly: Boolean = true) :
    FlamingoObject(FlamingoReflectClass, readOnly = readOnly)

val FlamingoNullClass = TrustedFlamingoClass("null")
val Null = FlamingoObject(FlamingoNullClass)