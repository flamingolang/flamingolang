package runtime

import setup.builtins
import objects.base.AssignmentError
import objects.base.FlObject
import objects.base.FlSuperObj
import objects.base.NameError

/**
 * data class to represent table entries for the name table abstract class
 */
data class NameTableEntry(var value: FlObject, val constant: Boolean = false)


open class NameTable(val name: String, private val superTable: NameTable? = null, private var context: FlObject? = null) {
    val entries = HashMap<String, NameTableEntry>()
    private var contextSuper: FlSuperObj? = null

    init {
        context ?. let { contextSuper = it.createSuper() }
    }

    /**
     * @param name the name to look for in the name table
     *
     * @return if the value was found, the value. Otherwise null
     */
    open fun getOrDefault(name: String, default: FlObject?): FlObject? {
        // println("    ".repeat(id) + "checking %s (0x%x) for %s".format(this.name, this.hashCode(), name))
        // for (entry in entries) {
        //    println("    ".repeat(id + 1) + entry.key + " (0x%x) : ".format(entry.value.value.hashCode()) + entry.value.value.stringShow())
        // }
        entries[name]?.let { return it.value }
        superTable?.let { table -> table.getOrDefault(name, null)?.let { return it } }
        builtins.let { return it.getOrDefault(name, default) }
    }

    /**
     * @param name the name to look for in the name table
     *
     * @return null if an error is set otherwise the object
     */
    open fun get(name: String): FlObject? {
        getOrDefault(name, null)?.let { return it }
        throwObj("there is no name: '%s'".format(name), NameError)
        return null
    }

    open fun getContextSuperObjOrNull(): FlSuperObj? {
        contextSuper?.let { return it }
        superTable?.getContextSuperObjOrNull()?.let { return it }
        return null
    }

    open fun getContextObjOrNull(): FlObject? {
        context?.let { return it }
        superTable?.getContextObjOrNull()?.let { return it }
        return null
    }

    /**
     * @return the context value or sets an error and returns null
     */
    open fun getContextObj(): FlObject? {
        getContextObjOrNull()?.let { return it }
        throwObj("no context set in %s".format(fullName()), NameError)
        return null
    }

    /**
     * @return the context super value or sets an error and returns null
     */
    open fun getContextSuperObj(): FlSuperObj? {
        getContextSuperObjOrNull()?.let { return it }
        throwObj("no context (or super) set in %s".format(fullName()), NameError)
        return null
    }

    /**
     * @param name the name to assign to a value
     * @param value the value of the object
     * @param constant if the value should be mutable if triggering a new entry
     */
    open fun set(name: String, value: FlObject, constant: Boolean = false): Unit? {
        entries[name]?.let {
            if (it.constant) {
                throwObj("'%s' is constant and can't be changed in %s".format(name, fullName()), AssignmentError)
                return null
            } else {
                it.value = value
            }
        }

        entries[name] = NameTableEntry(value, constant)
        return Unit
    }

    fun setAll(entries: Map<String, FlObject>) {
        entries.forEach { (name, entry) -> set(name, entry) }
    }

    /**
     * @return a string representing the name of the table which will be used in errors
     */
    private fun fullName(): String = (superTable?.let { "%s.%s".format(it.fullName(), name) }) ?: name

    fun updateContext(newContext: FlObject) {
        context = newContext
        contextSuper = newContext.createSuper()
    }
}

class ClassNameTable(name: String, superTable: NameTable? = null, context: FlObject? = null) :
    NameTable(name, superTable, context)