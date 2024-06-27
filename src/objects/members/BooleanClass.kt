package objects.members

import objects.base.FlBooleanObj
import objects.base.FlObject
import objects.base.stringOf
import objects.callable.KtCallContext
import objects.callable.KtFunction
import objects.callable.ParameterSpec

object BuiltinFunBoolDisplayObj : KtFunction(ParameterSpec("Boolean.displayObj")) {
    override fun accept(callContext: KtCallContext): FlObject? {
        val self = callContext.getObjContextOfType(FlBooleanObj::class) ?: return null
        return stringOf(self.boolean.toString())
    }
}


object BuiltinFunBoolTruthy : KtFunction(ParameterSpec("Obj.truthy")) {
    override fun accept(callContext: KtCallContext) = callContext.getObjContextOfType(FlBooleanObj::class)
}
