package objects.members

import objects.base.FlamingoObject
import objects.base.collections.FlamingoDictionaryObject
import objects.base.collections.FlamingoListObject
import objects.base.stringOf
import objects.callable.FlamingoCallableObject
import objects.callable.KtCallContext
import objects.callable.KtFunction
import objects.callable.ParameterSpec


object BuiltinFunCallableDisplayObject : KtFunction(ParameterSpec("Callable.displayObject")) {
    override fun accept(callContext: KtCallContext): FlamingoObject? {
        val self = callContext.getObjectContextOfType(FlamingoCallableObject::class) ?: return null
        return stringOf("<%s '%s'>".format(self.cls.name, self.parameters.name))
    }
}


object BuiltinFunCallableCall : KtFunction(ParameterSpec("Callable.call", varargs = "args", varkwargs = "kwargs")) {
    override fun accept(callContext: KtCallContext): FlamingoObject? {
        val self = callContext.getObjectContextOfType(FlamingoCallableObject::class) ?: return null
        val args = callContext.getLocalOfType("args", FlamingoListObject::class) ?: return null
        val kwargs = callContext.getLocalOfType("kwargs", FlamingoDictionaryObject::class) ?: return null
        return self.handleCall(args.list, kwargs.dictionary)
    }
}