package objects.libraries

import objects.base.FlamingoObject
import objects.base.FlamingoStringObject
import objects.base.Null
import objects.callable.KtCallContext
import objects.callable.KtFunction
import objects.callable.ParameterSpec


object BuiltinFunImport : KtFunction(ParameterSpec("import", listOf("path"))) {
    override fun accept(callContext: KtCallContext): FlamingoObject? {
        val path = callContext.getLocalOfType("path", FlamingoStringObject::class)?.string ?: return null
        return Null
    }
}