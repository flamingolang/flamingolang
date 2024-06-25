package objects.members

import objects.base.False
import objects.base.stringOf
import objects.callable.KtCallContext
import objects.callable.KtFunction
import objects.callable.ParameterSpec

object BuiltinFunNullDisplayObject : KtFunction(ParameterSpec("Null.displayObject")) {
    override fun accept(callContext: KtCallContext) = stringOf("null")
}


object BuiltinFunNullTruthy : KtFunction(ParameterSpec("Null.truthy")) {
    override fun accept(callContext: KtCallContext)= False
}