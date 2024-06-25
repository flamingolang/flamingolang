package objects.members

import objects.base.*
import objects.base.collections.FlamingoArrayObject
import objects.base.collections.FlamingoDictionaryObject
import objects.base.collections.FlamingoListObject
import objects.callable.FlamingoCodeObject
import objects.callable.KtCallContext
import objects.callable.KtFunction
import objects.callable.ParameterSpec
import runtime.throwObject

class ErrorWrapperKtFunctionAny(val name: String) :
    KtFunction(ParameterSpec(name, varargs = "args", varkwargs = "kwargs")) {
    override fun accept(callContext: KtCallContext): FlamingoObject? {
        val self = callContext.getObjectContext() ?: return null
        throwObject("%s type object doesn't support %s()".format(self.cls.name, name), TypeError)
        return null
    }
}

object BuiltinFunObjDisplayObject : KtFunction(ParameterSpec("Object.displayObject")) {
    override fun accept(callContext: KtCallContext): FlamingoObject? {
        val self = callContext.getObjectContext() ?: return null
        return stringOf("<%s 0x%x>".format(self.cls.name, self.hashCode()))
    }
}

object BuiltinFunObjToString : KtFunction(ParameterSpec("Object.toString")) {
    override fun accept(callContext: KtCallContext): FlamingoObject? {
        val self = callContext.getObjectContext() ?: return null
        return self.callAttribute("meta\$displayObject")
    }
}


object BuiltinFunObjEq : KtFunction(ParameterSpec("Object.eq", listOf("other"))) {
    override fun accept(callContext: KtCallContext): FlamingoObject? {
        val self = callContext.getObjectContext() ?: return null
        val other = callContext.getLocal("other") ?: return null
        return booleanOf(self == other)
    }
}


object BuiltinFunObjNeq : KtFunction(ParameterSpec("Object.neq", listOf("other"))) {
    override fun accept(callContext: KtCallContext): FlamingoObject? {
        val self = callContext.getObjectContext() ?: return null
        val other = callContext.getLocal("other") ?: return null
        val eq = self.eq(other)?.truthy() ?: return null
        return if (eq) False else True
    }
}

object BuiltinFunObjTruthy : KtFunction(ParameterSpec("Object.truthy")) {
    override fun accept(callContext: KtCallContext) = True
}

object BuiltinFunObjNot : KtFunction(ParameterSpec("Object.truthy")) {
    override fun accept(callContext: KtCallContext): FlamingoObject? {
        val truthy = callContext.getObjectContext()?.truthy() ?: return null
        return booleanOf(!truthy)
    }
}

object BuiltinFunObjIsIter : KtFunction(ParameterSpec("Object.isIterable")) {
    override fun accept(callContext: KtCallContext) = False
}


object BuiltinFunObjGetClass : KtFunction(ParameterSpec("Object.getClass")) {
    override fun accept(callContext: KtCallContext): FlamingoObject? {
        val self = callContext.getObjectContext() ?: return null
        return self.cls.reflectObject
    }
}


object BuiltinFunObjLet : KtFunction(ParameterSpec("Object.let", listOf("lambda"))) {
    override fun accept(callContext: KtCallContext): FlamingoObject? {
        val self = callContext.getObjectContext() ?: return null
        val lambda = callContext.getLocalOfType("lambda", FlamingoCodeObject::class) ?: return null
        return lambda.callLetting(mapOf(Pair("it", self)))
    }
}


object BuiltinFunObjLetIf : KtFunction(ParameterSpec("Object.letIf", listOf("condition", "lambda"))) {
    override fun accept(callContext: KtCallContext): FlamingoObject? {
        val self = callContext.getObjectContext() ?: return null
        val condition = callContext.getLocalOfType("condition", FlamingoCodeObject::class)
            ?.callLetting(mapOf(Pair("it", self)))
            ?.truthy() ?: return null
        if (condition)
            return callContext.getLocalOfType("lambda", FlamingoCodeObject::class)
                ?.callLetting(mapOf(Pair("it", self)))
        return self
    }
}

object BuiltinFunObjInstanceOf : KtFunction(ParameterSpec("Object.instanceOf", varargs = "classes")) {
    override fun accept(callContext: KtCallContext): FlamingoObject? {
        val self = callContext.getObjectContext() ?: return null
        val classes = callContext.getLocalOfType("classes", FlamingoListObject::class) ?: return null

        for (cls in classes.list) {
            val clsClass = cls.assertCast("classes parameter item", FlamingoReflectObject::class) ?: return null
            if (self.isOfClass(clsClass)) return True
        }

        return False
    }
}


object BuiltinFunObjAro : KtFunction(ParameterSpec("Object.aro")) {
    override fun accept(callContext: KtCallContext): FlamingoObject? {
        val self = callContext.getObjectContext() ?: return null
        return FlamingoArrayObject(self.cls.aro.map { it.reflectObject }.toTypedArray())
    }
}


object BuiltinFunObjExplicitCall : KtFunction(ParameterSpec("Object.explicitCall", listOf("arguments", "keywords"))) {
    override fun accept(callContext: KtCallContext): FlamingoObject? {
        val self = callContext.getObjectContext() ?: return null
        val arguments = callContext.getLocalOfType("arguments", FlamingoListObject::class)?.list ?: return null
        val keywords =
            callContext.getLocalOfType("keywords", FlamingoDictionaryObject::class)?.dictionary ?: return null
        return self.call(arguments, keywords)
    }
}
