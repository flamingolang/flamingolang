package objects.members

import objects.base.*
import objects.base.collections.FlamingoArrayObject
import objects.base.collections.FlamingoDictionaryObject
import objects.base.collections.FlamingoListObject
import objects.callable.KtCallContext
import objects.callable.KtFunction
import objects.callable.ParameterSpec
import runtime.NameTable
import runtime.NameTableEntry

object BuiltinFunClsDisplayObject : KtFunction(ParameterSpec("Class.displayObject")) {
    override fun accept(callContext: KtCallContext): FlamingoObject? {
        val self = callContext.getObjectContextOfType(FlamingoReflectObject::class) ?: return null
        return stringOf("<%s '%s'>".format(self.cls.name, self.reflectingClass.name))
    }
}


object BuiltinFunClsCall : KtFunction(ParameterSpec("Class.call", varargs = "args", varkwargs = "kwargs")) {
    override fun accept(callContext: KtCallContext): FlamingoObject? {
        val self = callContext.getObjectContextOfType(FlamingoReflectObject::class) ?: return null
        val args = callContext.getLocalOfType("args", FlamingoListObject::class) ?: return null
        val kwargs = callContext.getLocalOfType("kwargs", FlamingoDictionaryObject::class) ?: return null

        val metaNew = self.reflectingClass.getClassAttribute("meta\$new")
        val instanceObject = if (metaNew != null) {
            metaNew.call() ?: return null
        } else {
            FlamingoObject(self.reflectingClass, readOnly = false)
        }

        instanceObject.callAttribute("meta\$init", args.list, kwargs.dictionary) ?: return null

        return instanceObject
    }
}


object BuiltinFunClsNew : KtFunction(ParameterSpec("Class.new")) {
    override fun accept(callContext: KtCallContext): FlamingoObject? {
        val self = callContext.getObjectContextOfType(FlamingoReflectObject::class) ?: return null
        return FlamingoObject(self.reflectingClass, readOnly = false)
    }
}


object BuiltinFunClsNewClass : KtFunction(ParameterSpec("Class.new", listOf("name", "bases", "attributes"))) {
    override fun accept(callContext: KtCallContext): FlamingoObject? {
        callContext.getObjectContextOfType(FlamingoReflectObject::class)
        val nameString = callContext.getLocalOfType("name", FlamingoStringObject::class) ?: return null
        val basesArray = callContext.getLocalOfType("bases", FlamingoArrayObject::class) ?: return null
        val attributesDictionary =
            callContext.getLocalOfType("attributes", FlamingoDictionaryObject::class) ?: return null

        val name = nameString.string
        val bases = mutableListOf<FlamingoClass>()

        for (base in basesArray.array) {
            val baseClass = base.assertCast("bases class item", FlamingoReflectObject::class) ?: return null
            bases.add(baseClass.reflectingClass)
        }

        val attributes = NameTable("%s.attributes")

        for (entry in attributesDictionary.dictionary) {
            val isConstant = entry.value.getAttributeOfType("isConstant", FlamingoBooleanObject::class) ?: return null
            val attrValue = entry.value.getAttribute("value") ?: return null
            attributes.entries[entry.key] = NameTableEntry(attrValue, isConstant.boolean)
        }

        return createUserDefinedFlamingoClass(name, bases, attributes)?.reflectObject
    }
}


object BuiltinFunObjInit : KtFunction(ParameterSpec("Class.init", varargs = "?", varkwargs = "?")) {
    override fun accept(callContext: KtCallContext): FlamingoObject? {
        callContext.getObjectContext() ?: return null
        return Null
    }
}