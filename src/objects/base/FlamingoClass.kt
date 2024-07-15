package objects.base

import runtime.*


data class Dependency(private val types: MutableList<FlClass>) {
    val head get(): FlClass? = if (types.isNotEmpty()) types[0] else null
    val tail get(): List<FlClass?> = if (types.isEmpty()) listOf() else types.subList(1, types.size)
    val size get() = types.size
    fun removeFirst(): FlClass = types.removeFirst()
}

fun dependencyListOf(lists: Collection<List<FlClass>>): List<Dependency> =
    lists.map { Dependency(it.toMutableList()) }

class DependencyList(private val dependencies: List<Dependency>) {
    /**
     * Return true if at least 1 dependency contains the type
     */
    fun contains(type: FlClass) = dependencies.map { it.tail.contains(type) }.reduce { acc, b -> acc || b }

    val size get() = dependencies.size
    val heads get() = dependencies.map { it.head }

    /**
     * Only return true if all lists are exhausted
     */
    val exhausted get() = dependencies.map { it.size == 0 }.reduce { acc, b -> acc && b }
    fun remove(type: FlClass) {
        for (d in dependencies) {
            if (d.size != 0 && d.head == type) d.removeFirst()
        }
    }
}


fun merge(lists: Collection<List<FlClass>>): MutableList<FlClass>? {
    val result = mutableListOf<FlClass>()
    val linearizations = DependencyList(dependencyListOf(lists))
    while (true) {
        if (linearizations.exhausted) return result

        var found = false
        for (head in linearizations.heads) {
            if (head != null && (!linearizations.contains(head))) {
                result.add(head)
                linearizations.remove(head)
                found = true
                break
            }
        }

        if (!found) return null
    }
}

/**
 * https://blog.pilosus.org/posts/2019/05/02/python-mro/
 */
fun aro(bases: List<FlClass>): List<FlClass>? {
    val clsOrders = bases.map { it.aro }.toMutableList()
    clsOrders.add(bases)
    val aro = merge(clsOrders) ?: return null
    return aro
}

fun aroOf(cls: FlClass, bases: List<FlClass>): List<FlClass> {
    val aro = aro(bases)!!.toMutableList()
    aro.addFirst(cls)
    return aro
}

abstract class FlClass(val name: String, val bases: List<FlClass>) {
    abstract val aro: List<FlClass>
    abstract val reflectObj: FlReflectObj
    val classAttributes = HashMap<String, AttributeEntry>()

    fun setClassAttribute(name: String, value: FlObject, constant: Boolean = true) {
        classAttributes[name] = AttributeEntry(value, constant)
        val entry = AttributeEntry(value, constant)
        reflectObj.attributes["class\$$name"] = entry
        reflectObj.attributes[".class\$$name"] = entry
    }

    fun getClassAttribute(name: String): FlObject? {
        classAttributes[name]?.let { return it.value }
        return null
    }

    fun getClassAttributeRe(name: String): FlObject? {
        for (cls in aro) {
            cls.classAttributes[name]?.let { return it.value }
        }
        return null
    }
}


class ResolvedFlClass(
    name: String,
    bases: List<FlClass>,
    override val aro: List<FlClass>,
    reflectReadable: Boolean = false,
) :
    FlClass(name, bases) {
    override val reflectObj = FlReflectObj(this, readOnly = reflectReadable)
}

open class TrustedFlClass(name: String, bases: List<FlClass> = listOf(FlObjClass)) :
    FlClass(name, bases) {
    override val reflectObj = FlReflectObj(this)
    override val aro = aroOf(this, bases)
}

fun createUserDefinedFlClass(
    name: String,
    bases: List<FlClass>,
    attributes: ClassNameTable? = null,
): ResolvedFlClass? {
    val classBases = bases.ifEmpty { listOf(FlObjClass) }
    val aroOfClass = aro(classBases)
    if (aroOfClass == null) {
        throwObj(
            "could not create consistent method resolution order for '%s' with bases: %s".format(name,
                bases.joinToString(", ") { "'%s'".format(it.name) }), TypeError
        )
        return null
    }
    val finalClassOrder = aroOfClass.toMutableList()
    val finalClass = ResolvedFlClass(name, classBases, finalClassOrder)
    finalClassOrder.addFirst(finalClass)

    if (attributes != null) {
        for (entry in attributes.entries) {
            finalClass.setClassAttribute(
                entry.key,
                entry.value.value,
                entry.value.constant
            )
        }

        for (entry in attributes.metaMethods) {
            finalClass.setClassAttribute("meta\$${entry.key}", entry.value.value, entry.value.constant)
        }

        for (entry in attributes.staticMethods) {
            finalClass.setClassAttribute("meta\$static\$${entry.key}", entry.value.value, entry.value.constant)
        }

        for (entry in attributes.getters) {
            finalClass.setClassAttribute("meta\$getter\$${entry.key}", entry.value.value, entry.value.constant)
        }

        for (entry in attributes.setters) {
            finalClass.setClassAttribute("meta\$setter\$${entry.key}", entry.value.value, entry.value.constant)
        }
    }

    return finalClass
}

object FlObjClass : FlClass("Obj", listOf()) {
    override val aro = listOf(this)
    override val reflectObj = FlReflectObj(this)
}

object FlReflectClass : FlClass("Class", listOf(FlObjClass)) {
    override val aro = listOf(this, FlObjClass)
    override val reflectObj = FlReflectObj(this)

}

fun testCreateClass(name: String, bases: List<FlClass>): FlClass? {
    val cls = createUserDefinedFlClass(name, bases) ?: return null
    println(
        "successfully created class '%s' with bases: (%s), with aro: (%s)".format(name,
            cls.bases.joinToString(", ") { "'%s'".format(it.name) },
            cls.aro.joinToString(", ") { "'%s'".format(it.name) })
    )
    return cls
}

fun test(): Unit? {
    val aClass = testCreateClass("A", listOf()) ?: return null
    val bClass = testCreateClass("B", listOf()) ?: return null
    val dClass = testCreateClass("D", listOf(bClass)) ?: return null
    val cClass = testCreateClass("C", listOf(aClass, dClass)) ?: return null
    val testObj = FlObject(cClass).cls.reflectObj
    println(listOf(
        testObj.isOfClass(aClass),
        testObj.isOfClass(bClass),
        testObj.isOfClass(cClass),
        testObj.isOfClass(dClass),
        testObj.isOfClass(FlReflectClass),
        testObj.isOfClass(FlObjClass)
    ).joinToString(", ") { it.toString() })

    FlObjClass.reflectObj.attributes["balls"] = AttributeEntry(Null, true)

    println(testObj.getAttribute("balls"))

    return Unit
}


fun main() {
    test()
    val error = vmThrown
    if (error != null) printError(error, vmCallStack)
}