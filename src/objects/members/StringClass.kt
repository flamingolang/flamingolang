package objects.members

import objects.base.*
import objects.base.collections.FlListObj
import objects.callable.KtCallContext
import objects.callable.KtFunction
import objects.callable.ParameterSpec
import runtime.throwObj

object BuiltinFunStringDisplayObj : KtFunction(ParameterSpec("String.displayObj")) {
    override fun accept(callContext: KtCallContext): FlObject? {
        val self = callContext.getObjContextOfType(FlStringObj::class) ?: return null
        return stringOf("'%s'".format(self.string))
    }
}

object BuiltinFunStringToString : KtFunction(ParameterSpec("String.toString")) {
    override fun accept(callContext: KtCallContext): FlObject? {
        return callContext.getObjContextOfType(FlStringObj::class)
    }
}


object BuiltinFunStringAdd : KtFunction(ParameterSpec("String.add", listOf("string"))) {
    override fun accept(callContext: KtCallContext): FlObject? {
        val self = callContext.getObjContextOfType(FlStringObj::class) ?: return null
        val string = callContext.getLocalOfType("string", FlStringObj::class) ?: return null
        return stringOf(self.string + string.string)
    }
}


object BuiltinFunStringMul : KtFunction(ParameterSpec("String.mul", listOf("times"))) {
    override fun accept(callContext: KtCallContext): FlObject? {
        val self = callContext.getObjContextOfType(FlStringObj::class) ?: return null
        val times = callContext.getLocalOfType("times", FlNumberObj::class) ?: return null
        val timesInt = times.assertGetInteger("times") ?: return null
        if (timesInt < 0) {
            throwObj("%s type object can't be repeated less than 0 (%d) times".format(timesInt), ValueError)
            return null
        }
        return stringOf(self.string.repeat(timesInt))
    }
}


object BuiltinFunStringFormat : KtFunction(ParameterSpec("String.format", varargs = "arguments")) {
    override fun accept(callContext: KtCallContext): FlObject? {
        val self = callContext.getObjContextOfType(FlStringObj::class) ?: return null
        val arguments = callContext.getLocalOfType("arguments", FlListObj::class) ?: return null

        val itemStrings = mutableListOf<String>()

        for (item in arguments.list) {
            val itemString = item.stringConcat() ?: return null
            itemStrings.add(itemString)
        }

        val itemStringsSize = itemStrings.size

        val formatString = StringBuilder()

        var n = 0
        var i = 0

        while (i < self.string.length) {
            val char = self.string[i]
            if (char == '%') {
                if (i + 1 < self.string.length && self.string[i + 1] == '%') {
                    if (itemStrings.isNotEmpty()) {
                        formatString.append(itemStrings.removeFirst())
                        n++
                    } else {
                        throwObj(
                            "format replacements don't have enough items, only given %d".format(itemStringsSize),
                            IndexError
                        )
                        return null
                    }
                }
            } else {
                formatString.append(char)
            }
            i++
        }

        if (itemStrings.isNotEmpty()) {
            throwObj(
                "format replacements was given too many items (%d) expected only %d".format(itemStringsSize, n),
                IndexError
            )
            return null
        }

        return stringOf(formatString.toString())
    }
}

object BuiltinFunStringToNumOrNull : KtFunction(ParameterSpec("String.toNumberOrNull")) {
    override fun accept(callContext: KtCallContext): FlObject? {
        val self = callContext.getObjContextOfType(FlStringObj::class) ?: return null
        val converted = self.string.toDoubleOrNull() ?: return Null
        return numberOf(converted)
    }
}
