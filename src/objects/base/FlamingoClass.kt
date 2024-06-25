package objects.base

import runtime.*


data class Dependency(private val types: MutableList<FlamingoClass>) {
    val head get(): FlamingoClass? = if (types.isNotEmpty()) types[0] else null
    val tail get(): List<FlamingoClass?> = if (types.isEmpty()) listOf() else types.subList(1, types.size)
    val size get() = types.size
    fun removeFirst(): FlamingoClass = types.removeFirst()
}

fun dependencyListOf(lists: Collection<List<FlamingoClass>>): List<Dependency> =
    lists.map { Dependency(it.toMutableList()) }

class DependencyList(private val dependencies: List<Dependency>) {
    /**
     * Return true if at least 1 dependency contains the type
     */
    fun contains(type: FlamingoClass) = dependencies.map { it.tail.contains(type) }.reduce { acc, b -> acc || b }

    val size get() = dependencies.size
    val heads get() = dependencies.map { it.head }

    /**
     * Only return true if all lists are exhausted
     */
    val exhausted get() = dependencies.map { it.size == 0 }.reduce { acc, b -> acc && b }
    fun remove(type: FlamingoClass) {
        for (d in dependencies) {
            if (d.size != 0 && d.head == type) d.removeFirst()
        }
    }
}


fun merge(lists: Collection<List<FlamingoClass>>): MutableList<FlamingoClass>? {
    val result = mutableListOf<FlamingoClass>()
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
fun aro(bases: List<FlamingoClass>): List<FlamingoClass>? {
    val clsOrders = bases.map { it.aro }.toMutableList()
    clsOrders.add(bases)
    val aro = merge(clsOrders) ?: return null
    return aro
}

fun aroOf(cls: FlamingoClass, bases: List<FlamingoClass>): List<FlamingoClass> {
    val aro = aro(bases)!!.toMutableList()
    aro.addFirst(cls)
    return aro
}

abstract class FlamingoClass(val name: String, val bases: List<FlamingoClass>) {
    abstract val aro: List<FlamingoClass>
    abstract val reflectObject: FlamingoReflectObject
    val classAttributes = HashMap<String, AttributeEntry>()

    fun setClassAttribute(name: String, value: FlamingoObject, constant: Boolean = true) {
        classAttributes[name] = AttributeEntry(value, constant)
        reflectObject.attributes["class\$$name"] = AttributeEntry(value, constant)
    }

    fun getClassAttribute(name: String): FlamingoObject? {
        classAttributes[name]?.let { return it.value }
        return null
    }
}


class ResolvedFlamingoClass(
    name: String,
    bases: List<FlamingoClass>,
    override val aro: List<FlamingoClass>,
    reflectReadable: Boolean = false
) :
    FlamingoClass(name, bases) {
    override val reflectObject = FlamingoReflectObject(this, readOnly = reflectReadable)
}

open class TrustedFlamingoClass(name: String, bases: List<FlamingoClass> = listOf(FlamingoObjectClass)) :
    FlamingoClass(name, bases) {
    override val reflectObject = FlamingoReflectObject(this)
    override val aro = aroOf(this, bases)
}

fun createUserDefinedFlamingoClass(
    name: String,
    bases: List<FlamingoClass>,
    attributes: NameTable? = null
): ResolvedFlamingoClass? {
    val classBases = bases.ifEmpty { listOf(FlamingoObjectClass) }
    val aroOfClass = aro(classBases)
    if (aroOfClass == null) {
        throwObject(
            "could not create consistent method resolution order for '%s' with bases: %s".format(name,
                bases.joinToString(", ") { "'%s'".format(it.name) }), TypeError
        )
        return null
    }
    val finalClassOrder = aroOfClass.toMutableList()
    val finalClass = ResolvedFlamingoClass(name, classBases, finalClassOrder)
    finalClassOrder.addFirst(finalClass)

    if (attributes != null) for (entry in attributes.entries) {
        finalClass.setClassAttribute(
            entry.key,
            entry.value.value,
            entry.value.constant
        )
    }

    return finalClass
}

object FlamingoObjectClass : FlamingoClass("Object", listOf()) {
    override val aro = listOf(this)
    override val reflectObject = FlamingoReflectObject(this)
}

object FlamingoReflectClass : FlamingoClass("Class", listOf(FlamingoObjectClass)) {
    override val aro = listOf(this, FlamingoObjectClass)
    override val reflectObject = FlamingoReflectObject(this)

}

fun testCreateClass(name: String, bases: List<FlamingoClass>): FlamingoClass? {
    val cls = createUserDefinedFlamingoClass(name, bases) ?: return null
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
    val testObject = FlamingoObject(cClass).cls.reflectObject
    println(listOf(
        testObject.isOfClass(aClass),
        testObject.isOfClass(bClass),
        testObject.isOfClass(cClass),
        testObject.isOfClass(dClass),
        testObject.isOfClass(FlamingoReflectClass),
        testObject.isOfClass(FlamingoObjectClass)
    ).joinToString(", ") { it.toString() })

    FlamingoObjectClass.reflectObject.attributes["balls"] = AttributeEntry(Null, true)

    println(testObject.getAttribute("balls"))

    return Unit
}


fun main() {
    test()
    val error = vmThrown
    if (error != null) printError(error, vmCallStack)
}