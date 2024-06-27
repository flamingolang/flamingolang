package objects.members

import objects.base.FlObject
import objects.base.collections.FlDictionaryObj
import objects.base.collections.FlListObj
import objects.base.stringOf
import objects.callable.FlCallableObj
import objects.callable.KtCallContext
import objects.callable.KtFunction
import objects.callable.ParameterSpec


object BuiltinFunCallableDisplayObj : KtFunction(ParameterSpec("Callable.displayObj")) {
    override fun accept(callContext: KtCallContext): FlObject? {
        val self = callContext.getObjContextOfType(FlCallableObj::class) ?: return null
        return stringOf("<%s '%s'>".format(self.cls.name, self.parameters.name))
    }
}


object BuiltinFunCallableCall : KtFunction(ParameterSpec("Callable.call", varargs = "args", varkwargs = "kwargs")) {
    override fun accept(callContext: KtCallContext): FlObject? {
        val self = callContext.getObjContextOfType(FlCallableObj::class) ?: return null
        val args = callContext.getLocalOfType("args", FlListObj::class) ?: return null
        val kwargs = callContext.getLocalOfType("kwargs", FlDictionaryObj::class) ?: return null
        return self.handleCall(args.list, kwargs.dictionary)
    }
}