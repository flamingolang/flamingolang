package objects.libraries

import disOperations
import objects.base.*
import objects.base.collections.FlamingoListObject
import objects.callable.*
import runtime.throwObject


val FlamingoVariables = HashMap<String, FlamingoObject>()


val MetaString = String()
val MetaSentinel = FlamingoObject(FlamingoNullClass)


object BuiltinFunMeta : KtFunction(ParameterSpec("meta", listOf("callable"))) {
    override fun accept(callContext: KtCallContext): FlamingoObject? {
        val callable = callContext.getLocalOfType("callable", FlamingoCallableObject::class) ?: return null
        callable.attributes[MetaString] = AttributeEntry(MetaSentinel, true)
        return callable
    }
}


object BuiltinFunPrintln : KtFunction(ParameterSpec("println", varargs = "items")) {
    override fun accept(callContext: KtCallContext): FlamingoObject? {
        val items = callContext.getLocalOfType("items", FlamingoListObject::class) ?: return null
        val itemStrings = mutableListOf<String>()

        items.list.forEach {
            val itemString = it.stringConcat() ?: return null
            itemStrings.add(itemString)
        }

        println(itemStrings.joinToString(" "))

        return Null
    }
}


object BuiltinFunDis : KtFunction(ParameterSpec("disassemble", listOf("functionOrCode"))) {
    override fun accept(callContext: KtCallContext): FlamingoObject? {
        val dis = callContext.getLocal("functionOrCode") ?: return null

        when (dis) {
            is FlamingoFunctionObject -> println(disOperations(dis.codeObject.name, dis.codeObject.operations))
            is FlamingoCodeObject -> println(disOperations(dis.name, dis.operations))

            else -> {
                throwObject("can't disassemble %s type object".format(dis.cls.name), TypeError)
                return null
            }
        }

        return Null
    }
}