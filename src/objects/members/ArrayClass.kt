package objects.members

import objects.base.FlamingoObject
import objects.base.True
import objects.base.collections.FlamingoArrayObject
import objects.base.collections.FlamingoGenericIteratorObject
import objects.base.stringOf
import objects.callable.KtCallContext
import objects.callable.KtFunction
import objects.callable.ParameterSpec

object BuiltinFunArrayDisplayObject : KtFunction(ParameterSpec("Array.displayObject")) {
    override fun accept(callContext: KtCallContext): FlamingoObject? {
        val self = callContext.getObjectContextOfType(FlamingoArrayObject::class) ?: return null
        val stringShows = mutableListOf<String>()
        for (item in self.array) {
            val stringShow = item.stringShow() ?: return null
            stringShows.add(stringShow)
        }
        return stringOf("(%s)".format(stringShows.joinToString(", ")))
    }
}


object BuiltinFunArrayIsIter : KtFunction(ParameterSpec("Array.isIterable")) {
    override fun accept(callContext: KtCallContext): FlamingoObject? {
        callContext.getObjectContextOfType(FlamingoArrayObject::class) ?: return null
        return True
    }
}


object BuiltinFunArrayIter : KtFunction(ParameterSpec("Array.iter")) {
    override fun accept(callContext: KtCallContext): FlamingoObject? {
        val self = callContext.getObjectContextOfType(FlamingoArrayObject::class) ?: return null
        return FlamingoGenericIteratorObject(self.array.iterator())
    }
}
