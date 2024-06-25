package objects.base.collections

import objects.base.*
import objects.callable.KtCallContext
import objects.callable.KtFunction
import objects.callable.ParameterSpec
import runtime.throwObject

open class FlamingoGenericIteratorObject(
    val iterator: Iterator<FlamingoObject>,
    cls: FlamingoClass = FlamingoGenericIteratorClass,
    readOnly: Boolean = true
) : FlamingoObject(cls, readOnly = readOnly) {
    open fun hasNextObject(): Boolean? = iterator.hasNext()
    open fun nextObject(): FlamingoObject? {
        if (iterator.hasNext()) return iterator.next()
        else {
            throwObject("%s type object has no next to get".format(cls.name), IterationException)
            return null
        }
    }
}


val FlamingoGenericIteratorClass = TrustedFlamingoClass("iterator")


object BuiltinFunGenIterHasNextObject : KtFunction(ParameterSpec("hasNextObject")) {
    override fun accept(callContext: KtCallContext): FlamingoObject? {
        val self = callContext.getObjectContextOfType(FlamingoGenericIteratorObject::class) ?: return null
        val hasNext = self.hasNextObject() ?: return null
        return booleanOf(hasNext)
    }
}


object BuiltinFunGenIterNextObject : KtFunction(ParameterSpec("nextObject")) {
    override fun accept(callContext: KtCallContext): FlamingoObject? {
        val self = callContext.getObjectContextOfType(FlamingoGenericIteratorObject::class) ?: return null
        val nextObject = self.nextObject() ?: return null
        return nextObject
    }
}