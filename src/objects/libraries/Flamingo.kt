package objects.libraries

import objects.base.FlObject
import objects.base.Null
import objects.base.booleanOf
import objects.base.stringOf
import objects.callable.FlBuiltinObj
import objects.callable.KtCallContext
import objects.callable.KtFunction
import objects.callable.ParameterSpec
import runtime.Frame
import runtime.NameTable
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

object BuiltinFunFlGetName : KtFunction(ParameterSpec("getName")) {
    override fun accept(callContext: KtCallContext): FlObject? {
        val topFrame = peekCall() ?: return null

        return stringOf(topFrame.name)
    }
}

fun getFlamingoLibrary(): FlModuleObj {
    val flamingoLibrary = FlModuleObj("flamingo", null, NameTable("flamingo"))

    flamingoLibrary.setValue("isMain", FlBuiltinObj(BuiltinFunFlIsMain))
    flamingoLibrary.setValue("getPath", FlBuiltinObj(BuiltinFunFlGetPath))
    flamingoLibrary.setValue("getName", FlBuiltinObj(BuiltinFunFlGetName))

    return flamingoLibrary
}