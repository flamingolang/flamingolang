package objects.members

import objects.base.*
import objects.callable.KtCallContext
import objects.callable.KtFunction
import objects.callable.ParameterSpec
import runtime.throwObject
import kotlin.math.pow

fun simplifyNumber(number: Double): String = if (number % 1.0 == 0.0) number.toInt().toString() else number.toString()

object BuiltinFunNumberDisplayObject : KtFunction(ParameterSpec("Number.displayObject")) {
    override fun accept(callContext: KtCallContext): FlamingoObject? {
        val self = callContext.getObjectContextOfType(FlamingoNumberObject::class) ?: return null
        return stringOf(simplifyNumber(self.number))
    }
}


object BuiltinFunNumberAdd : KtFunction(ParameterSpec("Number.add", listOf("operand"))) {
    override fun accept(callContext: KtCallContext): FlamingoObject? {
        val self = callContext.getObjectContextOfType(FlamingoNumberObject::class) ?: return null
        val operand = callContext.getLocalOfType("operand", FlamingoNumberObject::class) ?: return null
        return numberOf(self.number + operand.number)
    }
}


object BuiltinFunNumberSub : KtFunction(ParameterSpec("Number.sub", listOf("operand"))) {
    override fun accept(callContext: KtCallContext): FlamingoObject? {
        val self = callContext.getObjectContextOfType(FlamingoNumberObject::class) ?: return null
        val operand = callContext.getLocalOfType("operand", FlamingoNumberObject::class) ?: return null
        return numberOf(self.number - operand.number)
    }
}


object BuiltinFunNumberMul : KtFunction(ParameterSpec("Number.mul", listOf("operand"))) {
    override fun accept(callContext: KtCallContext): FlamingoObject? {
        val self = callContext.getObjectContextOfType(FlamingoNumberObject::class) ?: return null
        val operand = callContext.getLocalOfType("operand", FlamingoNumberObject::class) ?: return null
        return numberOf(self.number * operand.number)
    }
}


object BuiltinFunNumberDiv : KtFunction(ParameterSpec("Number.div", listOf("operand"))) {
    override fun accept(callContext: KtCallContext): FlamingoObject? {
        val self = callContext.getObjectContextOfType(FlamingoNumberObject::class) ?: return null
        val operand = callContext.getLocalOfType("operand", FlamingoNumberObject::class) ?: return null

        if (operand.number == 0.0) {
            throwObject("can't divide by zero", ZeroDivisionException)
            return null
        }

        return numberOf(self.number / operand.number)
    }
}


object BuiltinFunNumberPow : KtFunction(ParameterSpec("Number.pow", listOf("operand"))) {
    override fun accept(callContext: KtCallContext): FlamingoObject? {
        val self = callContext.getObjectContextOfType(FlamingoNumberObject::class) ?: return null
        val operand = callContext.getLocalOfType("operand", FlamingoNumberObject::class) ?: return null
        return numberOf(self.number.pow(operand.number))
    }
}


object BuiltinFunNumberMod : KtFunction(ParameterSpec("Number.mod", listOf("operand"))) {
    override fun accept(callContext: KtCallContext): FlamingoObject? {
        val self = callContext.getObjectContextOfType(FlamingoNumberObject::class) ?: return null
        val operand = callContext.getLocalOfType("operand", FlamingoNumberObject::class) ?: return null

        if (operand.number == 0.0) {
            throwObject("can't modulo divide by zero", ZeroDivisionException)
            return null
        }

        return numberOf(self.number.mod(operand.number))
    }
}


object BuiltinFunNumberMinus : KtFunction(ParameterSpec("Number.minus")) {
    override fun accept(callContext: KtCallContext): FlamingoObject? {
        val self = callContext.getObjectContextOfType(FlamingoNumberObject::class) ?: return null
        return numberOf(-self.number)
    }
}


object BuiltinFunNumberPlus : KtFunction(ParameterSpec("Number.plus")) {
    override fun accept(callContext: KtCallContext): FlamingoObject? {
        val self = callContext.getObjectContextOfType(FlamingoNumberObject::class) ?: return null
        return numberOf(+self.number)
    }
}


object BuiltinFunNumberEq : KtFunction(ParameterSpec("Number.eq", listOf("operand"))) {
    override fun accept(callContext: KtCallContext): FlamingoObject? {
        val self = callContext.getObjectContextOfType(FlamingoNumberObject::class) ?: return null
        val operand = callContext.getLocal("operand") ?: return null
        if (operand is FlamingoNumberObject) return booleanOf(self.number == operand.number)
        return False
    }
}


object BuiltinFunNumberLt : KtFunction(ParameterSpec("Number.lt", listOf("operand"))) {
    override fun accept(callContext: KtCallContext): FlamingoObject? {
        val self = callContext.getObjectContextOfType(FlamingoNumberObject::class) ?: return null
        val operand = callContext.getLocalOfType("operand", FlamingoNumberObject::class) ?: return null
        return booleanOf(self.number < operand.number)
    }
}

object BuiltinFunNumberGt : KtFunction(ParameterSpec("Number.gt", listOf("operand"))) {
    override fun accept(callContext: KtCallContext): FlamingoObject? {
        val self = callContext.getObjectContextOfType(FlamingoNumberObject::class) ?: return null
        val operand = callContext.getLocalOfType("operand", FlamingoNumberObject::class) ?: return null

        return booleanOf(self.number > operand.number)
    }
}


object BuiltinFunNumberLtEq : KtFunction(ParameterSpec("Number.lteq", listOf("operand"))) {
    override fun accept(callContext: KtCallContext): FlamingoObject? {
        val self = callContext.getObjectContextOfType(FlamingoNumberObject::class) ?: return null
        val operand = callContext.getLocalOfType("operand", FlamingoNumberObject::class) ?: return null

        return booleanOf(self.number <= operand.number)
    }
}


object BuiltinFunNumberGtEq : KtFunction(ParameterSpec("Number.gteq", listOf("operand"))) {
    override fun accept(callContext: KtCallContext): FlamingoObject? {
        val self = callContext.getObjectContextOfType(FlamingoNumberObject::class) ?: return null
        val operand = callContext.getLocalOfType("operand", FlamingoNumberObject::class) ?: return null

        return booleanOf(self.number >= operand.number)
    }
}