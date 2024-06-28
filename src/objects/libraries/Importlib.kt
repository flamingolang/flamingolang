package objects.libraries

import compile
import objects.base.*
import objects.base.collections.FlDictionaryObj
import objects.callable.*
import readFile
import runtime.*
import java.io.File
import java.net.URI
import java.nio.file.Path
import java.util.*
import kotlin.collections.HashMap
import kotlin.io.path.*


fun peekCall(): Frame? {
    if (vmCallStack.size > 1) return vmCallStack.elementAt(vmCallStack.size - 2)

    throwObj("could not determine import context successfully", ImportFatality)
    return null
}

open class FlModuleObj(val name: String, val filePath: String?, cls: FlClass = FlModuleClass, readOnly: Boolean = true) : FlObject(cls, readOnly = readOnly) {
    val moduleAttributes = HashMap<String, FlObject>()

    override fun getAttributeOrNull(name: String, aroCheck: Boolean, bind: Boolean): FlObject? {
        moduleAttributes[name]?.let { return it }
        return super.getAttributeOrNull(name, aroCheck, bind = bind)
    }
}

val FlModuleClass = TrustedFlClass("module")
val ExporterSentinel = FlObject(FlNullClass)

object BuiltinFunImpLibExporter : KtFunction(ParameterSpec("Module.displayObj", listOf("callable"))) {
    override fun accept(callContext: KtCallContext): FlObject? {
        val callable = callContext.getLocalOfType("callable", FlCallableObj::class) ?: return null
        callable.attributes["<flag:importlib:exporter>"] = AttributeEntry(ExporterSentinel, true)
        return callable
    }
}


object BuiltinFunModDisplayObj : KtFunction(ParameterSpec("Module.displayObj")) {
    override fun accept(callContext: KtCallContext): FlObject? {
        val self = callContext.getObjContextOfType(FlModuleObj::class) ?: return null
        return stringOf("<%s '%s'>".format(self.cls.name, self.name))
    }
}

object BuiltinFunModGetPath : KtFunction(ParameterSpec("Module.getPath")) {
    override fun accept(callContext: KtCallContext): FlObject? {
        val self = callContext.getObjContextOfType(FlModuleObj::class) ?: return null
        self.filePath ?. let { return stringOf(it) }
        return Null 
    }
}


object BuiltinFunModExport : KtFunction(ParameterSpec("Module.export", listOf("namespace"))) {
    override fun accept(callContext: KtCallContext): FlObject? {
        val self = callContext.getObjContextOfType(FlModuleObj::class) ?: return null
        val namespace = callContext.getLocalOfType("namespace", FlCodeObj::class) ?: return null

        val exportTo = peekCall() ?: return null

        val moduleNameTable = NameTable(self.name, namespace.nativeClosure)
        val frame = OperationalFrame(namespace.name, namespace.operations, closure = moduleNameTable)

        for (entry in self.moduleAttributes.entries) {
            moduleNameTable.set(entry.key, entry.value, true) ?: return null
        }

        execute(frame).result ?: return null

        for (entry in frame.locals.entries) {
            exportTo.locals.set(entry.key, entry.value.value, entry.value.constant) ?: return null
        }

        return self
    }
}



val fileModules = HashMap<String, FlObject>()
val builtinModules = HashMap<String, FlObject>()

val importStack = Stack<String>()

val BUILTIN_IMPORT = "^#([a-zA-Z0-9_$]+)$".toRegex()
val RELATIVE_SMART_IMPORT = "(^(\\.\\.)*)([a-zA-Z0-9_\\-\$]+(\\.[a-zA-Z0-9_\\-\$]+)*)\$".toRegex()
val RELATIVE_IMPORT = "^(\\.\\./)*([a-zA-Z0-9_\\-\$] ?)+(/[a-zA-Z0-9_\\-\$] ?)*\$".toRegex()


fun importModuleFromPath(name: String, path: String): FlObject? {
    if (importStack.count { it == path } > 10) {
        throwObj("detected probable circular import (10)", ImportFatality)
        return null
    }
    importStack.push(path)

    val file = File(path)
    if (!file.exists()) {
        throwObj("there is no '%s'".format(path), ImportFatality)
        return null
    }

    val frame = compile(name, readFile(file), filePath = path) ?: return null
    execute(frame).thrown?.let { return null }

    val module = FlModuleObj(name, path)

    var exporterCandidate: FlObject? = null

    for (entry in frame.locals.entries) {
        entry.value.value.attributes["<flag:importlib:exporter>"]?.value?.let {
            if (exporterCandidate != null) {
                throwObj("multiple exporter functions detected", ImportFatality)
                return null
            }
            exporterCandidate = entry.value.value
        }
        module.moduleAttributes[entry.key] = entry.value.value
    }

    importStack.pop()

    return exporterCandidate?.call(listOf(module)) ?: module
}


object BuiltinFunImport : KtFunction(ParameterSpec("import", listOf("path"))) {
    override fun accept(callContext: KtCallContext): FlObject? {
        val path = callContext.getLocalOfType("path", FlStringObj::class)?.string ?: return null

        val importBuiltinPath = BUILTIN_IMPORT.matchEntire(path)?.groups?.get(1)?.value
        if (importBuiltinPath != null) {
            builtinModules[importBuiltinPath] ?. let { return it }

            throwObj("there is no builtin module named '%s'".format(importBuiltinPath), ImportFatality)
            return null
        }

        val importBase = peekCall() ?: return null

        if (importBase !is OperationalFrame || importBase.filePath == null) {
            throwObj("could not automatically resolve path for import, please use a different import function", ImportFatality)
            return null
        }

        val pathCurr = Path.of(importBase.filePath).parent
        if (pathCurr.notExists()) {
            throwObj("base import path '%s' does not exist".format(importBase.filePath), ImportFatality)
            return null
        }

        val finalRelPath = StringBuilder()

        val relSmartPath = RELATIVE_SMART_IMPORT.matchEntire(path)
        if (relSmartPath != null) {
            val relBack = relSmartPath.groups[1]!!.value
            val relPath = relSmartPath.groups[3]!!.value
            if (relBack.isEmpty()) {
                finalRelPath.append("./")
            } else {
                finalRelPath.append(relBack.replace("..", "../"))
            }
            finalRelPath.append(relPath.replace(".", "/"))
        } else {
            val relImportPath = RELATIVE_IMPORT.matchEntire(path)?.groups?.get(1)?.value
            if (relImportPath == null) {
                throwObj("malformed import path '%s'".format(path), ImportFatality)
                return null
            }
            finalRelPath.append(relImportPath)
        }

        val name: String

        val pathFinal = pathCurr.toUri().resolve(finalRelPath.toString()).let {
            name = it.toPath().nameWithoutExtension

            if (it.toPath().isDirectory()) {
                URI.create("$it/module.fl")
            } else {
                URI.create("$it.fl")
            }
        }

        if (pathFinal.toPath().notExists()) {
            throwObj("final import path '%s' does not exist".format(pathFinal.path), ImportFatality)
            return null
        }

        val pathFinalString = pathFinal.toPath().absolutePathString()

        fileModules[pathFinalString]?.let { return it }

        val module = importModuleFromPath(name, pathFinalString) ?: return null
        fileModules[pathFinalString] = module

        return module
    }
}


fun includeModuleFromPath(name: String, path: String, inclusions: Map<String, FlObject>): FlObject? {
    if (importStack.count { it == path } > 10) {
        throwObj("detected probable circular import or inclusion (10)", ImportFatality)
        return null
    }
    importStack.push(path)

    val file = File(path)
    if (!file.exists()) {
        throwObj("there is no '%s'".format(path), ImportFatality)
        return null
    }

    val frame = compile(name, readFile(file), filePath = path) ?: return null
    frame.locals.setAll(inclusions)
    execute(frame).thrown?.let { return null }

    importStack.pop()

    return Null
}


object BuiltinFunInclude : KtFunction(ParameterSpec("include", listOf("path", "inclusions"))) {
    override fun accept(callContext: KtCallContext): FlObject? {
        val path = callContext.getLocalOfType("path", FlStringObj::class)?.string ?: return null
        val inclusions = callContext.getLocalOfType("inclusions", FlDictionaryObj::class)?.dictionary ?: return null

        val importBase = peekCall() ?: return null

        if (importBase !is OperationalFrame || importBase.filePath == null) {
            throwObj("could not automatically resolve path for import, please use a different import function", ImportFatality)
            return null
        }

        val pathCurr = Path.of(importBase.filePath).parent
        if (pathCurr.notExists()) {
            throwObj("base import path '%s' does not exist".format(importBase.filePath), ImportFatality)
            return null
        }

        val finalRelPath = StringBuilder()

        val relSmartPath = RELATIVE_SMART_IMPORT.matchEntire(path)
        if (relSmartPath != null) {
            val relBack = relSmartPath.groups[1]!!.value
            val relPath = relSmartPath.groups[3]!!.value
            if (relBack.isEmpty()) {
                finalRelPath.append("./")
            } else {
                finalRelPath.append(relBack.replace("..", "../"))
            }
            finalRelPath.append(relPath.replace(".", "/"))
        } else {
            val relImportPath = RELATIVE_IMPORT.matchEntire(path)?.groups?.get(1)?.value
            if (relImportPath == null) {
                throwObj("malformed import path '%s'".format(path), ImportFatality)
                return null
            }
            finalRelPath.append(relImportPath)
        }

        val name: String

        val pathFinal = pathCurr.toUri().resolve(finalRelPath.toString()).let {
            name = it.toPath().nameWithoutExtension

            if (it.toPath().isDirectory()) {
                URI.create("$it/module.fl")
            } else {
                URI.create("$it.fl")
            }
        }

        if (pathFinal.toPath().notExists()) {
            throwObj("final import path '%s' does not exist".format(pathFinal.path), ImportFatality)
            return null
        }

        val pathFinalString = pathFinal.toPath().absolutePathString()

        return includeModuleFromPath(name, pathFinalString, inclusions)
    }
}


fun getImportLibrary(): FlModuleObj {
    val importLib = FlModuleObj("importlib", null)

    importLib.moduleAttributes["import"] = FlBuiltinObj(BuiltinFunImport)
    importLib.moduleAttributes["include"] = FlBuiltinObj(BuiltinFunInclude)
    importLib.moduleAttributes["exporter"] = FlBuiltinObj(BuiltinFunImpLibExporter)

    return importLib
}