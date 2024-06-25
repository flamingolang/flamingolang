package objects.members

import objects.base.*
import objects.base.collections.FlamingoRangeObject
import objects.callable.KtCallContext
import objects.callable.KtFunction
import objects.callable.ParameterSpec
import runtime.throwObject

object BuiltinFunRangeDisplayObject : KtFunction(ParameterSpec("Range.displayObject")) {
    override fun accept(callContext: KtCallContext): FlamingoObject? {
        val self = callContext.getObjectContextOfType(FlamingoRangeObject::class) ?: return null
        val fromShow = self.range.first.stringShow() ?: return null
        val toShow = self.range.second.stringShow() ?: return null
        return stringOf("<%s: %s to %s>".format(self.cls.name, fromShow, toShow))
    }
}


object BuiltinFunRangeIsIter : KtFunction(ParameterSpec("Range.isIterable")) {
    override fun accept(callContext: KtCallContext): FlamingoObject? {
        callContext.getObjectContextOfType(FlamingoRangeObject::class) ?: return null
        return True
    }
}


object BuiltinFunRangeIter : KtFunction(ParameterSpec("Range.iter")) {
    override fun accept(callContext: KtCallContext): FlamingoObject? {
        val self = callContext.getObjectContextOfType(FlamingoRangeObject::class) ?: return null
        val from = self.range.first.assertCast("range from argument", FlamingoNumberObject::class) ?: return null
        val to = self.range.second.assertCast("range to argument", FlamingoNumberObject::class) ?: return null
        val fromInt = from.assertGetInteger("range from argument") ?: return null
        val toInt = to.assertGetInteger("range to argument") ?: return null
        if (toInt < fromInt) {
            throwObject(
                "can't create a range where the last number (%d) is less than the first (%d)".format(
                    toInt,
                    fromInt
                ), ValueError
            )
            return null
        }
        return FlamingoRangeIterObject(fromInt, toInt)
    }
}


class FlamingoRangeIterObject(
    var pointer: Int,
    val to: Int,
    cls: FlamingoClass = FlamingoRangeIterClass,
    readOnly: Boolean = true
) : FlamingoObject(cls, readOnly = readOnly)

val FlamingoRangeIterClass = TrustedFlamingoClass("RangeIterator")


object BuiltinFunRangeIterHasNextObj : KtFunction(ParameterSpec("Range.hasNextObject")) {
    override fun accept(callContext: KtCallContext): FlamingoObject? {
        val self = callContext.getObjectContextOfType(FlamingoRangeIterObject::class) ?: return null
        return booleanOf(self.pointer <= self.to)
    }
}


object BuiltinFunRangeIterNextObj : KtFunction(ParameterSpec("Range.nextObject")) {
    override fun accept(callContext: KtCallContext): FlamingoObject? {
        val self = callContext.getObjectContextOfType(FlamingoRangeIterObject::class) ?: return null
        val nextObject = numberOf(self.pointer.toDouble())
        self.pointer++
        return nextObject
    }
}