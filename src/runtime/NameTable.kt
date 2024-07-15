package runtime

import objects.base.*
import setup.builtins

/**
 * data class to represent table entries for the name table abstract class
 */
data class NameTableEntry(var value: FlObject, val constant: Boolean = false)

// Need to convert name table into abstract class or interface
open class NameTable(
    val name: String,
    private val superTable: NameTable? = null,
    private var context: FlObject? = null
) {
    val entries = HashMap<String, NameTableEntry>()
    private var contextSuper: FlSuperObj? = null

    init {
        context?.let { contextSuper = it.createSuper() }
    }

    /**
     * @param name the name to look for in the name table
     *
     * @return if the value was found, the value. Otherwise null
     */
    open fun getOrDefault(name: String, default: FlObject?, checkBuiltins: Boolean = true): FlObject? {
        entries[name]?.let { return it.value }
        superTable?.let { table -> table.getOrDefault(name, null, checkBuiltins)?.let { return it } }
        return if (checkBuiltins) builtins.getOrDefault(name, default) else default
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
    NameTable(name, superTable, context) {
    val setters = hashMapOf<String, NameTableEntry>()
    val getters = hashMapOf<String, NameTableEntry>()

    val metaMethods = hashMapOf<String, NameTableEntry>()
    val staticMethods = hashMapOf<String, NameTableEntry>()

    override fun set(name: String, value: FlObject, constant: Boolean): Unit? {
        when (True) {
            value.getAttributeOrNull("<flag:prop:getter>") -> getters[name] = NameTableEntry(value, constant)
            value.getAttributeOrNull("<flag:prop:setter>") -> setters[name] = NameTableEntry(value, constant)
            value.getAttributeOrNull("<flag:meta>") -> metaMethods[name] = NameTableEntry(value, constant)
            value.getAttributeOrNull("<flag:static>") -> staticMethods[name] = NameTableEntry(value, constant)
            else -> {
                return super.set(name, value, constant)
            }
        }
        return null
    }
}


class MultiClosureNameTable(name: String, superTable: NameTable? = null, context: FlObject? = null) :
    NameTable(name, superTable, context) {
    private val multiTables: MutableList<NameTable> = mutableListOf()

    override fun getOrDefault(name: String, default: FlObject?, checkBuiltins: Boolean): FlObject? {
        super.getOrDefault(name, null, false)?.let { return it }
        multiTables.forEach { table -> table.getOrDefault(name, null, false)?.let { return it } }
        return if (checkBuiltins) builtins.getOrDefault(name, default) else default
    }

    /**
     * @return this
     */
    fun extend(table: NameTable): MultiClosureNameTable {
        multiTables.add(table)
        return this
    }
}