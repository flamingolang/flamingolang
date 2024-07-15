package objects.methods

import objects.base.*
import objects.base.collections.FlGenericIteratorObj
import objects.base.collections.FlListClass
import objects.base.collections.FlListObj
import objects.callable.FlCodeObj
import objects.callable.KtCallContext
import objects.callable.KtFunction
import objects.callable.ParameterSpec
import runtime.throwObj

object BuiltinFunListDisplayObj : KtFunction(ParameterSpec("List.displayObj")) {
    override fun accept(callContext: KtCallContext): FlObject? {
        val self = callContext.getObjContextOfType(FlListObj::class) ?: return null
        val stringShows = mutableListOf<String>()
        for (item in self.list) {
            val stringShow = item.stringShow() ?: return null
            stringShows.add(stringShow)
        }
        return stringOf("[%s]".format(stringShows.joinToString(", ")))
    }
}


object BuiltinFunListIsIter : KtFunction(ParameterSpec("List.isIterable")) {
    override fun accept(callContext: KtCallContext): FlObject? {
        callContext.getObjContextOfType(FlListObj::class) ?: return null
        return True
    }
}


object BuiltinFunListIter : KtFunction(ParameterSpec("List.iter")) {
    override fun accept(callContext: KtCallContext): FlObject? {
        val self = callContext.getObjContextOfType(FlListObj::class) ?: return null
        return FlGenericIteratorObj(self.list.subList(0, self.list.size).iterator())
    }
}


object BuiltinFunListAdd : KtFunction(ParameterSpec("List.add", varargs = "items")) {
    override fun accept(callContext: KtCallContext): FlObject? {
        val self = callContext.getObjContextOfType(FlListObj::class) ?: return null
        val items = callContext.getLocalOfType("items", FlListObj::class) ?: return null
        for (item in items.list) {
            self.list.add(item)
        }
        return self
    }
}


object BuiltinFunListIndex : KtFunction(ParameterSpec("List.index", listOf("index"))) {
    override fun accept(callContext: KtCallContext): FlObject? {
        val self = callContext.getObjContextOfType(FlListObj::class) ?: return null
        var index = callContext.getLocalOfType("index", FlNumberObj::class)?.assertGetInteger("index") ?: return null
        while (index < 0) index += self.list.size
        if (index > self.list.size) {
            throwObj(
                "index %d is out of range for %s of size %d".format(index, self.cls.name, self.list.size),
                IndexError
            )
            return null
        }
        return self.list[index]
    }
}


object BuiltinFunListAddFirst : KtFunction(ParameterSpec("List.addFirst", listOf("item"))) {
    override fun accept(callContext: KtCallContext): FlObject? {
        val self = callContext.getObjContextOfType(FlListObj::class) ?: return null
        val item = callContext.getLocal("item") ?: return null
        self.list.addFirst(item)
        return self
    }
}


fun listAdjustIndex(self: FlListObj, callContext: KtCallContext): Int? {
    val index = callContext.getLocalOfType("index", FlNumberObj::class) ?: return null
    var indexInt = index.assertGetInteger("index") ?: return null
    while (indexInt < 0) indexInt += self.list.size
    if (indexInt > self.list.size) {
        throwObj("%d is out of bounds for list with size %d".format(indexInt, self.list.size), IndexError)
        return null
    }
    return indexInt
}


object BuiltinFunListInsert : KtFunction(ParameterSpec("List.insert", listOf("item", "index"))) {
    override fun accept(callContext: KtCallContext): FlObject? {
        val self = callContext.getObjContextOfType(FlListObj::class) ?: return null
        val item = callContext.getLocal("item") ?: return null
        val indexInt = listAdjustIndex(self, callContext) ?: return null
        self.list.add(indexInt, item)
        return Null
    }
}


object BuiltinFunListRemove : KtFunction(ParameterSpec("List.remove", listOf("index"))) {
    override fun accept(callContext: KtCallContext): FlObject? {
        val self = callContext.getObjContextOfType(FlListObj::class) ?: return null
        val indexInt = listAdjustIndex(self, callContext) ?: return null
        return self.list.removeAt(indexInt)
    }
}


object BuiltinFunListRemoveObjs : KtFunction(ParameterSpec("List.removeObjs", varargs = "items")) {
    override fun accept(callContext: KtCallContext): FlObject? {
        val self = callContext.getObjContextOfType(FlListObj::class) ?: return null
        val items = callContext.getLocalOfType("items", FlListObj::class) ?: return null
        for (item in items.list) {
            self.list.remove(item)
        }
        return self
    }
}


object BuiltinFunListClear : KtFunction(ParameterSpec("List.clear", listOf("index"))) {
    override fun accept(callContext: KtCallContext): FlObject? {
        val self = callContext.getObjContextOfType(FlListObj::class) ?: return null
        self.list.clear()
        return Null
    }
}


fun listTransformList(self: FlListObj, callContext: KtCallContext): MutableList<FlObject>? {
    val transform = callContext.getLocalOfType("transform", FlCodeObj::class) ?: return null

    val items = mutableListOf<FlObject>()

    for (item in self.list) {
        val transformed = transform.callLetting(mapOf(Pair("it", item))) ?: return null
        items.add(transformed)
    }

    return items
}


object BuiltinFunListMap : KtFunction(ParameterSpec("List.map", listOf("transform"))) {
    override fun accept(callContext: KtCallContext): FlObject? {
        val self = callContext.getObjContextOfType(FlListObj::class) ?: return null

        val items = listTransformList(self, callContext) ?: return null

        self.list.clear()
        self.list.addAll(items)

        return self
    }
}


object BuiltinFunListMapped : KtFunction(ParameterSpec("List.mapped", listOf("transform"))) {
    override fun accept(callContext: KtCallContext): FlObject? {
        val self = callContext.getObjContextOfType(FlListObj::class) ?: return null

        val items = listTransformList(self, callContext) ?: return null

        return FlListObj(items, FlListClass)
    }
}

object BuiltinFunListFilter : KtFunction(ParameterSpec("List.filter", listOf("predicate"))) {
    override fun accept(callContext: KtCallContext): FlObject? {
        val self = callContext.getObjContextOfType(FlListObj::class) ?: return null
        val predicate = callContext.getLocalOfType("predicate", FlCodeObj::class) ?: return null

        val iterator = self.list.subList(0, self.list.size).iterator()

        while (iterator.hasNext()) {
            val item = iterator.next()
            val predicated = predicate.callLetting(mapOf(Pair("it", item))) ?: return null
            val predicatedTruthy = predicated.truthy() ?: return null
            if (!predicatedTruthy) iterator.remove()
        }

        return self
    }
}


object BuiltinFunListWhere : KtFunction(ParameterSpec("List.filter", listOf("predicate", "transform"))) {
    override fun accept(callContext: KtCallContext): FlObject? {
        val self = callContext.getObjContextOfType(FlListObj::class) ?: return null
        val predicate = callContext.getLocalOfType("predicate", FlCodeObj::class) ?: return null
        val transform = callContext.getLocalOfType("transform", FlCodeObj::class) ?: return null

        val iterator = self.list.subList(0, self.list.size).iterator()
        val items = mutableListOf<FlObject>()

        while (iterator.hasNext()) {
            val item = iterator.next()
            val predicated = predicate.callLetting(mapOf(Pair("it", item))) ?: return null
            val predicatedTruthy = predicated.truthy() ?: return null
            if (predicatedTruthy) {
                val transformed = transform.callLetting(mapOf(Pair("it", item))) ?: return null
                items.add(transformed)
            }
        }

        return FlListObj(items)
    }
}


object BuiltinFunListFiltered : KtFunction(ParameterSpec("List.filtered", listOf("predicate"))) {
    override fun accept(callContext: KtCallContext): FlObject? {
        val self = callContext.getObjContextOfType(FlListObj::class) ?: return null
        val predicate = callContext.getLocalOfType("predicate", FlCodeObj::class) ?: return null

        val items = mutableListOf<FlObject>()

        for (item in self.list) {
            val predicated = predicate.callLetting(mapOf(Pair("it", item))) ?: return null
            val predicatedTruthy = predicated.truthy() ?: return null
            if (predicatedTruthy) items.add(item)
        }

        return FlListObj(items, FlListClass)
    }
}


object BuiltinFunListSize : KtFunction(ParameterSpec("List.size")) {
    override fun accept(callContext: KtCallContext): FlObject? {
        val self = callContext.getObjContextOfType(FlListObj::class) ?: return null
        return numberOf(self.list.size.toDouble())
    }
}