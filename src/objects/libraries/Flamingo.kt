package objects.libraries

import objects.base.FlObject
import objects.base.Null
import objects.base.booleanOf
import objects.base.collections.FlDictionaryObj
import objects.base.collections.FlListObj
import objects.base.stringOf
import objects.callable.*
import runtime.Frame
import runtime.OperationalFrame


lateinit var mainFrame: Frame

object BuiltinFunFlIsMain : KtFunction(ParameterSpec("isMain")) {
    override fun accept(callContext: KtCallContext): FlObject? {
        val topFrame = peekCall() ?: return null
        return booleanOf(topFrame == mainFrame)
    }
}

object BuiltinFunFlGetPath : KtFunction(ParameterSpec("getPath")) {
    override fun accept(callContext: KtCallContext): FlObject? {
        val topFrame = peekCall() ?: return null

        if (topFrame is OperationalFrame) topFrame.filePath?.let { return stringOf(it) }

        return Null
    }
}

fun getFlamingoLibrary(): FlModuleObj {
    val flamingoLibrary = FlModuleObj("flamingo", null)

    flamingoLibrary.moduleAttributes["isMain"] = FlBuiltinObj(BuiltinFunFlIsMain)
    flamingoLibrary.moduleAttributes["getPath"] = FlBuiltinObj(BuiltinFunFlGetPath)

    return flamingoLibrary
}