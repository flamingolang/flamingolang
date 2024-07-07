package objects.libraries

import disOperations
import objects.base.*
import objects.base.callable.FlFunctionObj
import objects.base.collections.FlArrayObj
import objects.base.collections.FlDictionaryObj
import objects.base.collections.FlListObj
import objects.callable.*
import runtime.NameTable
import runtime.throwObj


val MetaSentinel = FlObject(FlNullClass)


object BuiltinFunMeta : KtFunction(ParameterSpec("meta", listOf("callable"))) {
    override fun accept(callContext: KtCallContext): FlObject? {
        val callable = callContext.getLocalOfType("callable", FlCallableObj::class) ?: return null
        callable.attributes["<flag:meta>"] = AttributeEntry(MetaSentinel, true)
        return callable
    }
}


object BuiltinFunPrintln : KtFunction(ParameterSpec("println", varargs = "items")) {
    override fun accept(callContext: KtCallContext): FlObject? {
        val items = callContext.getLocalOfType("items", FlListObj::class) ?: return null
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
    override fun accept(callContext: KtCallContext): FlObject? {
        val dis = callContext.getLocal("functionOrCode") ?: return null

        when (dis) {
            is FlFunctionObj -> println(disOperations(dis.codeObj.name, dis.codeObj.operations))
            is FlCodeObj -> println(disOperations(dis.name, dis.operations))

            else -> {
                throwObj("can't disassemble %s type object".format(dis.cls.name), TypeError)
                return null
            }
        }

        return Null
    }
}


object BuiltinFunArrayOf : KtFunction(ParameterSpec("arrayOf", varargs = "items")) {
    override fun accept(callContext: KtCallContext): FlObject? {
        val items = callContext.getLocalOfType("items", FlListObj::class) ?: return null
        return FlArrayObj(items.list.toTypedArray())
    }
}


object BuiltinFunListNew : KtFunction(ParameterSpec("new", varargs = "varargs", varkwargs = "varkwargs")) {
    override fun accept(callContext: KtCallContext): FlObject? {
        val self = callContext.getObjContextOfType(FlReflectObj::class) ?: return null
        return FlListObj(mutableListOf(), self.reflectingClass, readOnly = false)
    }
}


object BuiltinFunListOf : KtFunction(ParameterSpec("listOf", varargs = "items")) {
    override fun accept(callContext: KtCallContext): FlObject? {
        return callContext.getLocalOfType("items", FlListObj::class)
    }
}


object BuiltinFunDictOf : KtFunction(ParameterSpec("dictOf", varkwargs = "items")) {
    override fun accept(callContext: KtCallContext): FlObject? {
        return callContext.getLocalOfType("items", FlDictionaryObj::class)
    }
}


object BuiltinFunFunctionOf : KtFunction(ParameterSpec("functionOf", listOf("name", "arguments", "defaults", "code"))) {
    override fun accept(callContext: KtCallContext): FlObject? {
        val name = callContext.getLocalOfType("name", FlStringObj::class)?.string ?: return null
        val arguments = callContext.getLocalOfType("arguments", FlArrayObj::class)?.array ?: return null
        val defaults =
            callContext.getLocalOfType("defaults", FlDictionaryObj::class)?.dictionary ?: return null
        val positionals = mutableListOf<String>()
        arguments.forEach {
            val argName = it.assertCast("function argument name", FlStringObj::class) ?: return null
            positionals.add(argName.string)
        }
        val code = callContext.getLocalOfType("code", FlCodeObj::class) ?: return null
        val defaultsHashMap = HashMap(defaults)
        return FlFunctionObj(code, ParameterSpec(name, positionals, defaultsHashMap, null, null))

    }
}


object BuiltinFunClassOf : KtFunction(ParameterSpec("functionOf", listOf("name", "bases", "attributes"))) {
    override fun accept(callContext: KtCallContext): FlObject? {
        val name = callContext.getLocalOfType("name", FlStringObj::class)?.string ?: return null
        val arguments = callContext.getLocalOfType("bases", FlArrayObj::class)?.array ?: return null
        val attributes =
            callContext.getLocalOfType("attributes", FlDictionaryObj::class)?.dictionary ?: return null

        val classes = mutableListOf<FlClass>()
        arguments.forEach {
            val cls = it.assertCast("bases item", FlReflectObj::class) ?: return null
            classes.add(cls.reflectingClass)
        }

        val nameTable = NameTable(name)
        nameTable.setAll(attributes)

        return createUserDefinedFlClass(name, classes, nameTable)?.reflectObj
    }
}


object BuiltinFunMap : KtFunction(ParameterSpec("map", listOf("produceOut", "iterable", "transformation"))) {
    override fun accept(callContext: KtCallContext): FlObject? {
        val produceOut = callContext.getLocal("produceOut") ?: return null
        val iterable = callContext.getLocal("iterable")?.iter() ?: return null
        val transformation = callContext.getLocalOfType("transformation", FlCodeObj::class) ?: return null

        val transformed = mutableListOf<FlObject>()

        while (true) {
            val hasNextObj =
                iterable.callAttributeAssertCast("hasNextObj", FlBooleanObj::class)?.boolean
                    ?: return null
            if (!hasNextObj) break

            val nextObj = iterable.callAttribute("nextObj") ?: return null
            val nextObjTransformed = transformation.callLetting(mapOf(Pair("it", nextObj))) ?: return null

            transformed.add(nextObjTransformed)
        }

        return produceOut.call(transformed)
    }
}


object BuiltinFunFilter : KtFunction(ParameterSpec("filter", listOf("produceOut", "iterable", "predicate"))) {
    override fun accept(callContext: KtCallContext): FlObject? {
        val produceOut = callContext.getLocal("produceOut") ?: return null
        val iterable = callContext.getLocal("iterable")?.iter() ?: return null
        val predicate = callContext.getLocalOfType("predicate", FlCodeObj::class) ?: return null

        val filtered = mutableListOf<FlObject>()

        while (true) {
            val hasNextObj =
                iterable.callAttributeAssertCast("hasNextObj", FlBooleanObj::class)?.boolean
                    ?: return null
            if (!hasNextObj) break

            val nextObj = iterable.callAttribute("nextObj") ?: return null
            val nextObjFilters = predicate.callLetting(mapOf(Pair("it", nextObj))) ?: return null
            val truthy = nextObjFilters.truthy() ?: return null
            if (truthy) {
                filtered.add(nextObj)
            }
        }

        return produceOut.call(filtered)
    }
}


object BuiltinFunMapWhere :
    KtFunction(ParameterSpec("mapWhere", listOf("produceOut", "iterable", "predicate", "transformation"))) {
    override fun accept(callContext: KtCallContext): FlObject? {
        val produceOut = callContext.getLocal("produceOut") ?: return null
        val iterable = callContext.getLocal("iterable")?.iter() ?: return null
        val predicate = callContext.getLocalOfType("predicate", FlCodeObj::class) ?: return null
        val transformation = callContext.getLocalOfType("transformation", FlCodeObj::class) ?: return null

        val filtered = mutableListOf<FlObject>()

        while (true) {
            val hasNextObj =
                iterable.callAttributeAssertCast("hasNextObj", FlBooleanObj::class)?.boolean
                    ?: return null
            if (!hasNextObj) break

            val nextObj = iterable.callAttribute("nextObj") ?: return null
            val nextObjFilters = predicate.callLetting(mapOf(Pair("it", nextObj))) ?: return null
            val truthy = nextObjFilters.truthy() ?: return null
            if (truthy) {
                val nextObjTransformed = transformation.callLetting(mapOf(Pair("it", nextObj))) ?: return null
                filtered.add(nextObjTransformed)
            }
        }

        return produceOut.call(filtered)
    }
}