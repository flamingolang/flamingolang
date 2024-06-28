package objects.methods

import objects.base.FlObject
import objects.base.Null
import objects.base.collections.FlDictionaryObj
import objects.base.stringOf
import objects.callable.FlCodeObj
import objects.callable.KtCallContext
import objects.callable.KtFunction
import objects.callable.ParameterSpec
import runtime.execute

object BuiltinFunCodeObjDisplayObj : KtFunction(ParameterSpec("Code.displayObj")) {
    override fun accept(callContext: KtCallContext): FlObject? {
        val self = callContext.getObjContextOfType(FlCodeObj::class) ?: return null
        val closure = self.nativeClosure
        return stringOf("<code '%s' closure=%s>".format(self.name, "'%s'".format(closure?.name ?: "null")))
    }
}


object BuiltinFunCodeObjCallLetting : KtFunction(ParameterSpec("Code.callLetting", varkwargs = "locals")) {
    override fun accept(callContext: KtCallContext): FlObject? {
        val self = callContext.getObjContextOfType(FlCodeObj::class) ?: return null
        val locals = callContext.getLocalOfType("locals", FlDictionaryObj::class) ?: return null
        return self.callLetting(initLocals = locals.dictionary)
    }
}


object BuiltinFunCodeObjCallLettingIgnore : KtFunction(ParameterSpec("Code.callLettingIgnoreThrow", varkwargs = "locals")) {
    override fun accept(callContext: KtCallContext): FlObject? {
        val self = callContext.getObjContextOfType(FlCodeObj::class) ?: return null
        val locals = callContext.getLocalOfType("locals", FlDictionaryObj::class) ?: return null
        val execution = execute(self.getFrame(locals.dictionary), propagateError = false)
        execution.result?.let { return it }
        execution.thrown?.let { return it  }
        return Null
    }
}


object BuiltinFunCodeObjGetName : KtFunction(ParameterSpec("Code.getName")) {
    override fun accept(callContext: KtCallContext): FlObject? {
        val self = callContext.getObjContextOfType(FlCodeObj::class) ?: return null
        return stringOf(self.name)
    }
}
