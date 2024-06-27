package objects.methods

import objects.base.FlObject
import objects.base.True
import objects.base.collections.FlArrayObj
import objects.base.collections.FlGenericIteratorObj
import objects.base.stringOf
import objects.callable.KtCallContext
import objects.callable.KtFunction
import objects.callable.ParameterSpec

object BuiltinFunArrayDisplayObj : KtFunction(ParameterSpec("Array.displayObj")) {
    override fun accept(callContext: KtCallContext): FlObject? {
        val self = callContext.getObjContextOfType(FlArrayObj::class) ?: return null
        val stringShows = mutableListOf<String>()
        for (item in self.array) {
            val stringShow = item.stringShow() ?: return null
            stringShows.add(stringShow)
        }
        return stringOf("(%s)".format(stringShows.joinToString(", ")))
    }
}


object BuiltinFunArrayIsIter : KtFunction(ParameterSpec("Array.isIterable")) {
    override fun accept(callContext: KtCallContext): FlObject? {
        callContext.getObjContextOfType(FlArrayObj::class) ?: return null
        return True
    }
}


object BuiltinFunArrayIter : KtFunction(ParameterSpec("Array.iter")) {
    override fun accept(callContext: KtCallContext): FlObject? {
        val self = callContext.getObjContextOfType(FlArrayObj::class) ?: return null
        return FlGenericIteratorObj(self.array.toList().iterator())
    }
}
