package objects.methods

import objects.base.*
import objects.base.collections.FlArrayObj
import objects.base.collections.FlDictionaryObj
import objects.base.collections.FlListObj
import objects.callable.*
import objects.libraries.peekCall
import runtime.*

class ErrWrapperExst(val name: String) :
    KtFunction(ParameterSpec(name, varargs = "args", varkwargs = "kwargs")) {
    override fun accept(callContext: KtCallContext): FlObject? {
        val self = callContext.getObjContext() ?: return null
        throwObj("%s type object doesn't support %s()".format(self.cls.name, name), TypeError)
        return null
    }
}

object BuiltinFunObjDisplayObj : KtFunction(ParameterSpec("Obj.displayObj")) {
    override fun accept(callContext: KtCallContext): FlObject? {
        val self = callContext.getObjContext() ?: return null
        return stringOf("<%s 0x%x>".format(self.cls.name, self.hashCode()))
    }
}

object BuiltinFunObjToString : KtFunction(ParameterSpec("Obj.toString")) {
    override fun accept(callContext: KtCallContext): FlObject? {
        val self = callContext.getObjContext() ?: return null
        return self.callAttribute("meta\$displayObject")
    }
}


object BuiltinFunObjEq : KtFunction(ParameterSpec("Obj.eq", listOf("other"))) {
    override fun accept(callContext: KtCallContext): FlObject? {
        val self = callContext.getObjContext() ?: return null
        val other = callContext.getLocal("other") ?: return null
        return booleanOf(self == other)
    }
}


object BuiltinFunObjNeq : KtFunction(ParameterSpec("Obj.neq", listOf("other"))) {
    override fun accept(callContext: KtCallContext): FlObject? {
        val self = callContext.getObjContext() ?: return null
        val other = callContext.getLocal("other") ?: return null
        val eq = self.eq(other)?.truthy() ?: return null
        return if (eq) False else True
    }
}

object BuiltinFunObjTruthy : KtFunction(ParameterSpec("Obj.truthy")) {
    override fun accept(callContext: KtCallContext) = True
}

object BuiltinFunObjNot : KtFunction(ParameterSpec("Obj.truthy")) {
    override fun accept(callContext: KtCallContext): FlObject? {
        val truthy = callContext.getObjContext()?.truthy() ?: return null
        return booleanOf(!truthy)
    }
}

object BuiltinFunObjIsIter : KtFunction(ParameterSpec("Obj.isIterable")) {
    override fun accept(callContext: KtCallContext) = False
}


object BuiltinFunObjGetClass : KtFunction(ParameterSpec("Obj.getClass")) {
    override fun accept(callContext: KtCallContext): FlObject? {
        val self = callContext.getObjContext() ?: return null
        return self.cls.reflectObj
    }
}

class BuiltinFunObjLetReturns(private val escapeFrame: Frame, private val actualFrame: Frame) :
    KtFunction(ParameterSpec("Obj.letReturns", listOf("item"))) {
    var enabled = true

    override fun accept(callContext: KtCallContext): FlObject? {
        if (!enabled) {
            throwObj("disabled use of return from function", SyntaxError)
            return null
        }

        val item = callContext.getLocal("item") ?: return null
        actualFrame.returnBuffer = item
        escapeFrame.returnBuffer = item
        return Null
    }
}

object BuiltinFunObjLet : KtFunction(ParameterSpec("Obj.let", listOf("lambda"))) {
    override fun accept(callContext: KtCallContext): FlObject? {
        val self = callContext.getObjContext() ?: return null
        val lambda = callContext.getLocalOfType("lambda", FlCodeObj::class) ?: return null

        val letFrame = lambda.getFrame()

        val peekCall = peekCall() ?: return null
        val letReturns = BuiltinFunObjLetReturns(peekCall, letFrame)

        letFrame.locals.setAll(mapOf(Pair("it", self), Pair("returns", FlBuiltinObj(letReturns))))

        val result = execute(letFrame)

        letReturns.enabled = false

        return result.result
    }
}


class SubjectNameTable(name: String, superTable: NameTable?, context: FlObject) : NameTable(name, superTable, context) {
    override fun getOrDefault(name: String, default: FlObject?, checkBuiltins: Boolean): FlObject? {
        getContextObjOrNull()?.getAttributeOrNull(name)?.let { return it }
        return super.getOrDefault(name, default, checkBuiltins)
    }
}


object BuiltinFunObjLetContext : KtFunction(ParameterSpec("Obj.let", listOf("lambda"))) {
    override fun accept(callContext: KtCallContext): FlObject? {
        val self = callContext.getObjContext() ?: return null
        val lambda = callContext.getLocalOfType("lambda", FlCodeObj::class) ?: return null
        val frame = OperationalFrame(
            lambda.name,
            lambda.operations,
            closure = SubjectNameTable(lambda.name, lambda.nativeClosure, self),
            filePath = lambda.filePath
        )
        return execute(frame).result
    }
}


object BuiltinFunObjLetIf : KtFunction(ParameterSpec("Obj.letIf", listOf("condition", "lambda"))) {
    override fun accept(callContext: KtCallContext): FlObject? {
        val self = callContext.getObjContext() ?: return null
        val condition = callContext.getLocalOfType("condition", FlCodeObj::class)
            ?.callLetting(mapOf(Pair("it", self)))
            ?.truthy() ?: return null
        if (condition)
            return callContext.getLocalOfType("lambda", FlCodeObj::class)
                ?.callLetting(mapOf(Pair("it", self)))
        return self
    }
}

object BuiltinFunObjInstanceOf : KtFunction(ParameterSpec("Obj.instanceOf", varargs = "classes")) {
    override fun accept(callContext: KtCallContext): FlObject? {
        val self = callContext.getObjContext() ?: return null
        val classes = callContext.getLocalOfType("classes", FlListObj::class) ?: return null

        for (cls in classes.list) {
            val clsClass = cls.assertCast("classes parameter item", FlReflectObj::class) ?: return null
            if (self.isOfClass(clsClass)) return True
        }

        return False
    }
}


object BuiltinFunObjAro : KtFunction(ParameterSpec("Obj.aro")) {
    override fun accept(callContext: KtCallContext): FlObject? {
        val self = callContext.getObjContext() ?: return null
        return FlArrayObj(self.cls.aro.map { it.reflectObj }.toTypedArray())
    }
}


object BuiltinFunObjExplicitCall : KtFunction(ParameterSpec("Obj.explicitCall", listOf("arguments", "keywords"))) {
    override fun accept(callContext: KtCallContext): FlObject? {
        val self = callContext.getObjContext() ?: return null
        val arguments = callContext.getLocalOfType("arguments", FlListObj::class)?.list ?: return null
        val keywords =
            callContext.getLocalOfType("keywords", FlDictionaryObj::class)?.dictionary ?: return null
        return self.call(arguments, keywords)
    }
}

class ErrWrapperNew(private val cls: FlClass, private val replacement: String? = null) :
    KtFunction(ParameterSpec("new")) {
    override fun accept(callContext: KtCallContext): FlObject? {
        throwObj(
            "%s type objects can't be created through constructor%s".format(
                cls.name,
                replacement?.let { ", use '$it' instead" } ?: ""), TypeError)
        return null
    }
}