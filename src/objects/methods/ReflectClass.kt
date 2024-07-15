package objects.methods

import objects.base.*
import objects.base.collections.FlArrayObj
import objects.base.collections.FlDictionaryObj
import objects.base.collections.FlListObj
import objects.callable.KtCallContext
import objects.callable.KtFunction
import objects.callable.ParameterSpec

object BuiltinFunClsDisplayObj : KtFunction(ParameterSpec("Class.displayObj")) {
    override fun accept(callContext: KtCallContext): FlObject? {
        val self = callContext.getObjContextOfType(FlReflectObj::class) ?: return null
        return stringOf("<%s '%s'>".format(self.cls.name, self.reflectingClass.name))
    }
}


object BuiltinFunClsCall : KtFunction(ParameterSpec("Class.call", varargs = "args", varkwargs = "kwargs")) {
    override fun accept(callContext: KtCallContext): FlObject? {
        val self = callContext.getObjContextOfType(FlReflectObj::class) ?: return null
        val args = callContext.getLocalOfType("args", FlListObj::class) ?: return null
        val kwargs = callContext.getLocalOfType("kwargs", FlDictionaryObj::class) ?: return null

        val metaNew = self.reflectingClass.getClassAttributeRe("meta\$new")
        val instanceObj = if (metaNew != null) {
            self.reflectingClass.reflectObj.bindSomeCallableAttribute(metaNew).call(args.list, kwargs.dictionary)
                ?: return null
        } else {
            FlObject(self.reflectingClass, readOnly = false)
        }

        val metaInit = self.reflectingClass.getClassAttributeRe("meta\$init")
        if (metaInit != null) {
            instanceObj.bindSomeCallableAttribute(metaInit).call(args.list, kwargs.dictionary) ?: return null
        }

        return instanceObj
    }
}


object BuiltinFunClsNew : KtFunction(ParameterSpec("Class.new")) {
    override fun accept(callContext: KtCallContext): FlObject? {
        val self = callContext.getObjContextOfType(FlReflectObj::class) ?: return null
        return FlObject(self.reflectingClass, readOnly = false)
    }
}


object BuiltinFunClsGetName : KtFunction(ParameterSpec("Class.getName")) {
    override fun accept(callContext: KtCallContext): FlObject? {
        val self = callContext.getObjContextOfType(FlReflectObj::class) ?: return null
        return stringOf(self.reflectingClass.name)
    }
}


object BuiltinFunClsGetBases : KtFunction(ParameterSpec("Class.getBases")) {
    override fun accept(callContext: KtCallContext): FlObject? {
        val self = callContext.getObjContextOfType(FlReflectObj::class) ?: return null
        return FlArrayObj(self.reflectingClass.bases.map { it.reflectObj }.toTypedArray())
    }
}


object BuiltinFunClsGetClsAttrs : KtFunction(ParameterSpec("Class.getClassAttributes")) {
    override fun accept(callContext: KtCallContext): FlObject? {
        val self = callContext.getObjContextOfType(FlReflectObj::class) ?: return null
        val attributes = LinkedHashMap<String, FlObject>()
        self.reflectingClass.classAttributes.forEach { (key, value) ->
            attributes[key] =
                FlDictionaryObj(sortedMapOf(Pair("value", value.value), Pair("constant", booleanOf(value.constant))))
        }
        return FlDictionaryObj(attributes)
    }
}

/*
object BuiltinFunClsNewClass : KtFunction(ParameterSpec("Class.new", listOf("name", "bases", "attributes"))) {
    override fun accept(callContext: KtCallContext): FlObject? {
        callContext.getObjContextOfType(FlReflectObj::class)
        val nameString = callContext.getLocalOfType("name", FlStringObj::class) ?: return null
        val basesArray = callContext.getLocalOfType("bases", FlArrayObj::class) ?: return null
        val attributesDictionary =
            callContext.getLocalOfType("attributes", FlDictionaryObj::class) ?: return null

        val name = nameString.string
        val bases = mutableListOf<FlClass>()

        for (base in basesArray.array) {
            val baseClass = base.assertCast("bases class item", FlReflectObj::class) ?: return null
            bases.add(baseClass.reflectingClass)
        }

        val attributes = NameTable("%s.attributes")

        for (entry in attributesDictionary.dictionary) {
            val isConstant = entry.value.getAttributeOfType("isConstant", FlBooleanObj::class) ?: return null
            val attrValue = entry.value.getAttribute("value") ?: return null
            attributes.entries[entry.key] = NameTableEntry(attrValue, isConstant.boolean)
        }

        return createUserDefinedFlClass(name, bases, attributes)?.reflectObj
    }
}
 */


object BuiltinFunObjInit : KtFunction(ParameterSpec("Class.init", varargs = "?", varkwargs = "?")) {
    override fun accept(callContext: KtCallContext): FlObject? {
        callContext.getObjContext() ?: return null
        return Null
    }
}