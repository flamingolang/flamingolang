package objects.libraries

import objects.base.*
import objects.base.callable.FlFunctionObj
import objects.callable.FlBuiltinObj
import objects.callable.KtCallContext
import objects.callable.KtFunction
import objects.callable.ParameterSpec
import runtime.throwObj


val PARAM_REGEX = "@param\\s*([a-zA-Z_$]+[a-zA-Z0-9_$]*)\\s*([\\s\\S]*?)\\s*((?=@)|\\$)".toRegex()
val ATTRIBUTE_REGEX = "@attribute\\s*([a-zA-Z_$]+[a-zA-Z0-9_$]*)\\s*([\\s\\S]*?)\\s*((?=@)|\\$)".toRegex()
val AUTHOR_REGEX = "@author\\s*([a-zA-Z_$]+[a-zA-Z0-9_$]*)\\s*((?=@)|\\$)".toRegex()



object BuiltinFunFlDocGetDoc : KtFunction(ParameterSpec("getDocOf", listOf("object"))) {
    override fun accept(callContext: KtCallContext) =
        callContext.getLocal("object")?.getAttributeOrNull("meta\$doc") ?: Null
}


fun getFlamingoDocLibrary(): FlModuleObj {
    val flamingoDocLibrary = FlModuleObj("flamingodoc", null)

    flamingoDocLibrary.moduleAttributes["getDocOf"] = FlBuiltinObj(BuiltinFunFlDocGetDoc)

    return flamingoDocLibrary
}