package objects.methods

import objects.base.*
import objects.base.collections.FlDictionaryObj
import objects.base.collections.FlListObj
import objects.callable.FlCallableObj
import objects.callable.KtCallContext
import objects.callable.KtFunction
import objects.callable.ParameterSpec

object BuiltinFunSuperDisplayObj : KtFunction(ParameterSpec("Super.displayObj")) {
    override fun accept(callContext: KtCallContext): FlObject? {
        val self = callContext.getObjContextOfType(FlSuperObj::class) ?: return null
        return stringOf("<%s '%s'>".format(self.cls.name, self.self.cls.name))
    }
}


object BuiltinFunSuperCall : KtFunction(ParameterSpec("Super.call", listOf("class"), varargs = "args", varkwargs = "kwargs")) {
    override fun accept(callContext: KtCallContext): FlObject? {
        val self = callContext.getObjContextOfType(FlSuperObj::class) ?: return null
        val args = callContext.getLocalOfType("args", FlListObj::class) ?: return null
        val kwargs = callContext.getLocalOfType("kwargs", FlDictionaryObj::class) ?: return null
        val cls = callContext.getLocalOfType("class", FlReflectObj::class) ?: return null

        val clsInit = cls.getAttributeOfType("class\$meta\$init", FlCallableObj::class, bind = false) ?: return null
        val clsBoundInit = self.self.bindSomeCallableAttribute(clsInit)

        return clsBoundInit.call(args.list, kwargs.dictionary)
    }
}

