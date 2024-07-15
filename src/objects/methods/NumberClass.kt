package objects.methods

import objects.base.*
import objects.callable.KtCallContext
import objects.callable.KtFunction
import objects.callable.ParameterSpec
import runtime.throwObj
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.pow


fun simplifyNumber(number: Double) = if (number % 1.0 == 0.0) "%.0f".format(number) else number.toString()


object BuiltinFunNumberDisplayObj : KtFunction(ParameterSpec("Number.displayObj")) {
    override fun accept(callContext: KtCallContext): FlObject? {
        val self = callContext.getObjContextOfType(FlNumberObj::class) ?: return null
        return stringOf(simplifyNumber(self.number))
    }
}


object BuiltinFunNumberAdd : KtFunction(ParameterSpec("Number.add", listOf("operand"))) {
    override fun accept(callContext: KtCallContext): FlObject? {
        val self = callContext.getObjContextOfType(FlNumberObj::class) ?: return null
        val operand = callContext.getLocalOfType("operand", FlNumberObj::class) ?: return null
        return numberOf(self.number + operand.number)
    }
}


object BuiltinFunNumberSub : KtFunction(ParameterSpec("Number.sub", listOf("operand"))) {
    override fun accept(callContext: KtCallContext): FlObject? {
        val self = callContext.getObjContextOfType(FlNumberObj::class) ?: return null
        val operand = callContext.getLocalOfType("operand", FlNumberObj::class) ?: return null
        return numberOf(self.number - operand.number)
    }
}


object BuiltinFunNumberMul : KtFunction(ParameterSpec("Number.mul", listOf("operand"))) {
    override fun accept(callContext: KtCallContext): FlObject? {
        val self = callContext.getObjContextOfType(FlNumberObj::class) ?: return null
        val operand = callContext.getLocalOfType("operand", FlNumberObj::class) ?: return null
        return numberOf(self.number * operand.number)
    }
}


object BuiltinFunNumberDiv : KtFunction(ParameterSpec("Number.div", listOf("operand"))) {
    override fun accept(callContext: KtCallContext): FlObject? {
        val self = callContext.getObjContextOfType(FlNumberObj::class) ?: return null
        val operand = callContext.getLocalOfType("operand", FlNumberObj::class) ?: return null

        if (operand.number == 0.0) {
            throwObj("can't divide by zero", ZeroDivisionException)
            return null
        }

        return numberOf(self.number / operand.number)
    }
}


object BuiltinFunNumberPow : KtFunction(ParameterSpec("Number.pow", listOf("operand"))) {
    override fun accept(callContext: KtCallContext): FlObject? {
        val self = callContext.getObjContextOfType(FlNumberObj::class) ?: return null
        val operand = callContext.getLocalOfType("operand", FlNumberObj::class) ?: return null
        return numberOf(self.number.pow(operand.number))
    }
}


object BuiltinFunNumberMod : KtFunction(ParameterSpec("Number.mod", listOf("operand"))) {
    override fun accept(callContext: KtCallContext): FlObject? {
        val self = callContext.getObjContextOfType(FlNumberObj::class) ?: return null
        val operand = callContext.getLocalOfType("operand", FlNumberObj::class) ?: return null

        if (operand.number == 0.0) {
            throwObj("can't modulo divide by zero", ZeroDivisionException)
            return null
        }

        return numberOf(self.number.mod(operand.number))
    }
}


object BuiltinFunNumberMinus : KtFunction(ParameterSpec("Number.minus")) {
    override fun accept(callContext: KtCallContext): FlObject? {
        val self = callContext.getObjContextOfType(FlNumberObj::class) ?: return null
        return numberOf(-self.number)
    }
}


object BuiltinFunNumberPlus : KtFunction(ParameterSpec("Number.plus")) {
    override fun accept(callContext: KtCallContext): FlObject? {
        val self = callContext.getObjContextOfType(FlNumberObj::class) ?: return null
        return if (self.number < 0) numberOf(-self.number) else numberOf(self.number)
    }
}


object BuiltinFunNumberEq : KtFunction(ParameterSpec("Number.eq", listOf("operand"))) {
    override fun accept(callContext: KtCallContext): FlObject? {
        val self = callContext.getObjContextOfType(FlNumberObj::class) ?: return null
        val operand = callContext.getLocal("operand") ?: return null
        if (operand is FlNumberObj) return booleanOf(self.number == operand.number)
        return False
    }
}


object BuiltinFunNumberLt : KtFunction(ParameterSpec("Number.lt", listOf("operand"))) {
    override fun accept(callContext: KtCallContext): FlObject? {
        val self = callContext.getObjContextOfType(FlNumberObj::class) ?: return null
        val operand = callContext.getLocalOfType("operand", FlNumberObj::class) ?: return null
        return booleanOf(self.number < operand.number)
    }
}

object BuiltinFunNumberGt : KtFunction(ParameterSpec("Number.gt", listOf("operand"))) {
    override fun accept(callContext: KtCallContext): FlObject? {
        val self = callContext.getObjContextOfType(FlNumberObj::class) ?: return null
        val operand = callContext.getLocalOfType("operand", FlNumberObj::class) ?: return null

        return booleanOf(self.number > operand.number)
    }
}


object BuiltinFunNumberLtEq : KtFunction(ParameterSpec("Number.lteq", listOf("operand"))) {
    override fun accept(callContext: KtCallContext): FlObject? {
        val self = callContext.getObjContextOfType(FlNumberObj::class) ?: return null
        val operand = callContext.getLocalOfType("operand", FlNumberObj::class) ?: return null

        return booleanOf(self.number <= operand.number)
    }
}


object BuiltinFunNumberGtEq : KtFunction(ParameterSpec("Number.gteq", listOf("operand"))) {
    override fun accept(callContext: KtCallContext): FlObject? {
        val self = callContext.getObjContextOfType(FlNumberObj::class) ?: return null
        val operand = callContext.getLocalOfType("operand", FlNumberObj::class) ?: return null

        return booleanOf(self.number >= operand.number)
    }
}


object BuiltinFunNumberIsInteger : KtFunction(ParameterSpec("Number.isInteger")) {
    override fun accept(callContext: KtCallContext): FlObject? {
        val self = callContext.getObjContextOfType(FlNumberObj::class) ?: return null
        return booleanOf(self.number % 1.0 == 0.0)
    }
}


object BuiltinFunNumberIsEven : KtFunction(ParameterSpec("Number.isEven")) {
    override fun accept(callContext: KtCallContext): FlObject? {
        val self = callContext.getObjContextOfType(FlNumberObj::class) ?: return null
        return booleanOf(self.number % 2.0 == 0.0)
    }
}


object BuiltinFunNumberIsOdd : KtFunction(ParameterSpec("Number.isOdd")) {
    override fun accept(callContext: KtCallContext): FlObject? {
        val self = callContext.getObjContextOfType(FlNumberObj::class) ?: return null
        return booleanOf(self.number % 2.0 != 0.0)
    }
}


object BuiltinFunNumberFloor : KtFunction(ParameterSpec("Number.floor")) {
    override fun accept(callContext: KtCallContext): FlObject? {
        val self = callContext.getObjContextOfType(FlNumberObj::class) ?: return null
        return numberOf(floor(self.number))
    }
}


object BuiltinFunNumberCeil : KtFunction(ParameterSpec("Number.ceil")) {
    override fun accept(callContext: KtCallContext): FlObject? {
        val self = callContext.getObjContextOfType(FlNumberObj::class) ?: return null
        return numberOf(ceil(self.number))
    }
}

// atomic number

object BuiltinFunNumberToAtom : KtFunction(ParameterSpec("Number.toAtom")) {
    override fun accept(callContext: KtCallContext): FlObject? {
        val self = callContext.getObjContextOfType(FlNumberObj::class) ?: return null
        return FlAtomicNumObj(self.number)
    }
}

object BuiltinFunAtomicNumIncr : KtFunction(ParameterSpec("Number.incr")) {
    override fun accept(callContext: KtCallContext): FlObject? {
        val self = callContext.getObjContextOfType(FlAtomicNumObj::class) ?: return null
        self.number++
        return self
    }
}

object BuiltinFunAtomicNumDecr : KtFunction(ParameterSpec("Number.decr")) {
    override fun accept(callContext: KtCallContext): FlObject? {
        val self = callContext.getObjContextOfType(FlAtomicNumObj::class) ?: return null
        self.number--
        return self
    }
}

object BuiltinFunAtomicNumIFloor : KtFunction(ParameterSpec("Number.ifloor")) {
    override fun accept(callContext: KtCallContext): FlObject? {
        val self = callContext.getObjContextOfType(FlAtomicNumObj::class) ?: return null
        self.number = floor(self.number)
        return self
    }
}


object BuiltinFunAtomicNumICeil : KtFunction(ParameterSpec("Number.iceil")) {
    override fun accept(callContext: KtCallContext): FlObject? {
        val self = callContext.getObjContextOfType(FlAtomicNumObj::class) ?: return null
        self.number = ceil(self.number)
        return self
    }
}


object BuiltinFunAtomicNumIAdd : KtFunction(ParameterSpec("AtomicNumber.iadd", listOf("operand"))) {
    override fun accept(callContext: KtCallContext): FlObject? {
        val self = callContext.getObjContextOfType(FlAtomicNumObj::class) ?: return null
        val operand = callContext.getLocalOfType("operand", FlNumberObj::class) ?: return null
        val result = self.callAttributeAssertCast("meta\$add", FlNumberObj::class, listOf(operand)) ?: return null
        self.number = result.number
        return self
    }
}


object BuiltinFunAtomicNumISub : KtFunction(ParameterSpec("AtomicNumber.isub", listOf("operand"))) {
    override fun accept(callContext: KtCallContext): FlObject? {
        val self = callContext.getObjContextOfType(FlAtomicNumObj::class) ?: return null
        val operand = callContext.getLocalOfType("operand", FlNumberObj::class) ?: return null
        val result = self.callAttributeAssertCast("meta\$sub", FlNumberObj::class, listOf(operand)) ?: return null
        self.number = result.number
        return self
    }
}


object BuiltinFunAtomicNumIMul : KtFunction(ParameterSpec("AtomicNumber.imul", listOf("operand"))) {
    override fun accept(callContext: KtCallContext): FlObject? {
        val self = callContext.getObjContextOfType(FlAtomicNumObj::class) ?: return null
        val operand = callContext.getLocalOfType("operand", FlNumberObj::class) ?: return null
        val result = self.callAttributeAssertCast("meta\$mul", FlNumberObj::class, listOf(operand)) ?: return null
        self.number = result.number
        return self
    }
}


object BuiltinFunAtomicNumIDiv : KtFunction(ParameterSpec("AtomicNumber.idiv", listOf("operand"))) {
    override fun accept(callContext: KtCallContext): FlObject? {
        val self = callContext.getObjContextOfType(FlAtomicNumObj::class) ?: return null
        val operand = callContext.getLocalOfType("operand", FlNumberObj::class) ?: return null

        val result = self.callAttributeAssertCast("meta\$div", FlNumberObj::class, listOf(operand)) ?: return null
        self.number = result.number

        return self
    }
}


object BuiltinFunAtomicNumIPow : KtFunction(ParameterSpec("AtomicNumber.ipow", listOf("operand"))) {
    override fun accept(callContext: KtCallContext): FlObject? {
        val self = callContext.getObjContextOfType(FlAtomicNumObj::class) ?: return null
        val operand = callContext.getLocalOfType("operand", FlNumberObj::class) ?: return null
        val result = self.callAttributeAssertCast("meta\$pow", FlNumberObj::class, listOf(operand)) ?: return null
        self.number = result.number
        return self
    }
}


object BuiltinFunAtomicNumIMod : KtFunction(ParameterSpec("AtomicNumber.imod", listOf("operand"))) {
    override fun accept(callContext: KtCallContext): FlObject? {
        val self = callContext.getObjContextOfType(FlAtomicNumObj::class) ?: return null
        val operand = callContext.getLocalOfType("operand", FlNumberObj::class) ?: return null
        val result = self.callAttributeAssertCast("meta\$mod", FlNumberObj::class, listOf(operand)) ?: return null
        self.number = result.number
        return self
    }
}


object BuiltinFunAtomicNumIMinus : KtFunction(ParameterSpec("AtomicNumber.iminus")) {
    override fun accept(callContext: KtCallContext): FlObject? {
        val self = callContext.getObjContextOfType(FlAtomicNumObj::class) ?: return null
        val result = self.callAttributeAssertCast("meta\$minus", FlNumberObj::class) ?: return null
        self.number = result.number
        return self
    }
}


object BuiltinFunAtomicNumIPlus : KtFunction(ParameterSpec("AtomicNumber.iplus")) {
    override fun accept(callContext: KtCallContext): FlObject? {
        val self = callContext.getObjContextOfType(FlAtomicNumObj::class) ?: return null
        val result = self.callAttributeAssertCast("meta\$plus", FlNumberObj::class) ?: return null
        self.number = result.number
        return self
    }
}
