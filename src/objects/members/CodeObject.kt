package objects.members

import objects.base.FlamingoObject
import objects.base.collections.FlamingoDictionaryObject
import objects.base.stringOf
import objects.callable.FlamingoCodeObject
import objects.callable.KtCallContext
import objects.callable.KtFunction
import objects.callable.ParameterSpec

object BuiltinFunCodeObjDisplayObject : KtFunction(ParameterSpec("Code.displayObject")) {
    override fun accept(callContext: KtCallContext): FlamingoObject? {
        val self = callContext.getObjectContextOfType(FlamingoCodeObject::class) ?: return null
        val closure = self.nativeClosure
        return stringOf("<code '%s' closure=%s>".format(self.name, "'%s'".format(closure?.name ?: "null")))
    }
}


object BuiltinFunCodeObjCallLetting : KtFunction(ParameterSpec("Code.callLetting", varkwargs = "locals")) {
    override fun accept(callContext: KtCallContext): FlamingoObject? {
        val self = callContext.getObjectContextOfType(FlamingoCodeObject::class) ?: return null
        val locals = callContext.getLocalOfType("locals", FlamingoDictionaryObject::class) ?: return null
        return self.callLetting(locals = locals.dictionary)
    }
}

