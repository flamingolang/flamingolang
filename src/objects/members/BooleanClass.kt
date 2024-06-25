package objects.members

import objects.base.FlamingoBooleanObject
import objects.base.FlamingoObject
import objects.base.stringOf
import objects.callable.KtCallContext
import objects.callable.KtFunction
import objects.callable.ParameterSpec

object BuiltinFunBoolDisplayObject : KtFunction(ParameterSpec("Boolean.displayObject")) {
    override fun accept(callContext: KtCallContext): FlamingoObject? {
        val self = callContext.getObjectContextOfType(FlamingoBooleanObject::class) ?: return null
        return stringOf(self.boolean.toString())
    }
}


object BuiltinFunBoolTruthy : KtFunction(ParameterSpec("Object.truthy")) {
    override fun accept(callContext: KtCallContext) = callContext.getObjectContextOfType(FlamingoBooleanObject::class)
}
