package objects.libraries.flamingodoc

import objects.base.AttributeEntry
import objects.base.FlObject
import objects.base.FlStringObj
import objects.base.collections.FlListObj
import objects.callable.*
import objects.libraries.FlModuleObj
import runtime.NameTable


object BuiltinFunFlDocGetDoc : KtFunction(ParameterSpec("getDocOf", listOf("object"))) {
    override fun accept(callContext: KtCallContext): FlObject? {
        return null
    }
}

class BuiltinFunFlDocAnnotate(private val name: String, private val annotation: FlStringObj) :
    KtFunction(ParameterSpec("author-annotator", listOf("callable"))) {
    override fun accept(callContext: KtCallContext): FlObject? {
        val callable = callContext.getLocalOfType("callable", FlCallableObj::class) ?: return null

        val annot =
            callable.attributes.getOrPut("meta\$doc\$$name") { AttributeEntry(FlListObj(mutableListOf()), false) }
        val annotList = annot.value.assertCast("$name argument", FlListObj::class)?.list ?: return null

        annotList.add(annotation)

        return callable
    }
}

class BuiltinFunFlDocList(private val name: String) : KtFunction(ParameterSpec("meta", listOf("name"))) {
    override fun accept(callContext: KtCallContext): FlObject? {
        val author = callContext.getLocalOfType("name", FlStringObj::class) ?: return null
        return FlBuiltinObj(BuiltinFunFlDocAnnotate(name, author))
    }
}

fun getFlamingoDocLibrary(): FlModuleObj {
    val flamingoDocLibrary = FlModuleObj("flamingodoc", null, NameTable("flamingodoc"))

    flamingoDocLibrary.setValue("getDocOf", FlBuiltinObj(BuiltinFunFlDocGetDoc))
    flamingoDocLibrary.setValue("author", FlBuiltinObj(BuiltinFunFlDocList("authors")))
    flamingoDocLibrary.setValue("annotation", FlBuiltinObj(BuiltinFunFlDocList("annotations")))
    flamingoDocLibrary.setValue("description", FlBuiltinObj(BuiltinFunFlDocList("description")))
    flamingoDocLibrary.setValue("see", FlBuiltinObj(BuiltinFunFlDocList("see")))

    return flamingoDocLibrary
}