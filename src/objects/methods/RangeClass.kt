package objects.methods

import objects.base.*
import objects.base.collections.FlRangeObj
import objects.callable.KtCallContext
import objects.callable.KtFunction
import objects.callable.ParameterSpec
import runtime.throwObj

object BuiltinFunRangeDisplayObj : KtFunction(ParameterSpec("Range.displayObj")) {
    override fun accept(callContext: KtCallContext): FlObject? {
        val self = callContext.getObjContextOfType(FlRangeObj::class) ?: return null
        val fromShow = self.range.first.stringShow() ?: return null
        val toShow = self.range.second.stringShow() ?: return null
        return stringOf("<%s: %s to %s>".format(self.cls.name, fromShow, toShow))
    }
}


object BuiltinFunRangeIsIter : KtFunction(ParameterSpec("Range.isIterable")) {
    override fun accept(callContext: KtCallContext): FlObject? {
        callContext.getObjContextOfType(FlRangeObj::class) ?: return null
        return True
    }
}


object BuiltinFunRangeIter : KtFunction(ParameterSpec("Range.iter")) {
    override fun accept(callContext: KtCallContext): FlObject? {
        val self = callContext.getObjContextOfType(FlRangeObj::class) ?: return null
        val from = self.range.first.assertCast("range from argument", FlNumberObj::class) ?: return null
        val to = self.range.second.assertCast("range to argument", FlNumberObj::class) ?: return null
        val fromInt = from.assertGetInteger("range from argument") ?: return null
        val toInt = to.assertGetInteger("range to argument") ?: return null
        if (toInt < fromInt) {
            throwObj(
                "can't create a range where the last number (%d) is less than the first (%d)".format(
                    toInt,
                    fromInt
                ), ValueError
            )
            return null
        }
        return FlRangeIterObj(fromInt, toInt)
    }
}


class FlRangeIterObj(
    var pointer: Int,
    val to: Int,
    cls: FlClass = FlRangeIterClass,
    readOnly: Boolean = true
) : FlObject(cls, readOnly = readOnly)

val FlRangeIterClass = TrustedFlClass("RangeIterator")


object BuiltinFunRangeIterHasNextObj : KtFunction(ParameterSpec("Range.hasNextObj")) {
    override fun accept(callContext: KtCallContext): FlObject? {
        val self = callContext.getObjContextOfType(FlRangeIterObj::class) ?: return null
        return booleanOf(self.pointer <= self.to)
    }
}


object BuiltinFunRangeIterNextObj : KtFunction(ParameterSpec("Range.nextObj")) {
    override fun accept(callContext: KtCallContext): FlObject? {
        val self = callContext.getObjContextOfType(FlRangeIterObj::class) ?: return null
        val nextObj = numberOf(self.pointer.toDouble())
        self.pointer++
        return nextObj
    }
}