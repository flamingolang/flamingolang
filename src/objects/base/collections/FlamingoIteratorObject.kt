package objects.base.collections

import objects.base.*
import objects.callable.KtCallContext
import objects.callable.KtFunction
import objects.callable.ParameterSpec
import runtime.throwObj

open class FlGenericIteratorObj(
    val iterator: Iterator<FlObject>,
    cls: FlClass = FlGenericIteratorClass,
    readOnly: Boolean = true
) : FlObject(cls, readOnly = readOnly) {
    open fun hasNextObj(): Boolean? = iterator.hasNext()
    open fun nextObj(): FlObject? {
        if (iterator.hasNext()) return iterator.next()
        else {
            throwObj("%s type object has no next to get".format(cls.name), IterationException)
            return null
        }
    }
}


val FlGenericIteratorClass = TrustedFlClass("iterator")


object BuiltinFunGenIterHasNextObj : KtFunction(ParameterSpec("hasNextObj")) {
    override fun accept(callContext: KtCallContext): FlObject? {
        val self = callContext.getObjContextOfType(FlGenericIteratorObj::class) ?: return null
        val hasNext = self.hasNextObj() ?: return null
        return booleanOf(hasNext)
    }
}


object BuiltinFunGenIterNextObj : KtFunction(ParameterSpec("nextObj")) {
    override fun accept(callContext: KtCallContext): FlObject? {
        val self = callContext.getObjContextOfType(FlGenericIteratorObj::class) ?: return null
        val nextObj = self.nextObj() ?: return null
        return nextObj
    }
}