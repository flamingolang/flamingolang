package objects.members

import objects.base.*
import objects.base.collections.FlamingoGenericIteratorObject
import objects.base.collections.FlamingoListClass
import objects.base.collections.FlamingoListObject
import objects.callable.FlamingoCodeObject
import objects.callable.KtCallContext
import objects.callable.KtFunction
import objects.callable.ParameterSpec
import runtime.throwObject

object BuiltinFunListDisplayObject : KtFunction(ParameterSpec("List.displayObject")) {
    override fun accept(callContext: KtCallContext): FlamingoObject? {
        val self = callContext.getObjectContextOfType(FlamingoListObject::class) ?: return null
        val stringShows = mutableListOf<String>()
        for (item in self.list) {
            val stringShow = item.stringShow() ?: return null
            stringShows.add(stringShow)
        }
        return stringOf("[%s]".format(stringShows.joinToString(", ")))
    }
}


object BuiltinFunListIsIter : KtFunction(ParameterSpec("List.isIterable")) {
    override fun accept(callContext: KtCallContext): FlamingoObject? {
        callContext.getObjectContextOfType(FlamingoListObject::class) ?: return null
        return True
    }
}


object BuiltinFunListIter : KtFunction(ParameterSpec("List.iter")) {
    override fun accept(callContext: KtCallContext): FlamingoObject? {
        val self = callContext.getObjectContextOfType(FlamingoListObject::class) ?: return null
        return FlamingoGenericIteratorObject(self.list.iterator())
    }
}


object BuiltinFunListAdd : KtFunction(ParameterSpec("List.add", varargs = "items")) {
    override fun accept(callContext: KtCallContext): FlamingoObject? {
        val self = callContext.getObjectContextOfType(FlamingoListObject::class) ?: return null
        val items = callContext.getLocalOfType("items", FlamingoListObject::class) ?: return null
        for (item in items.list) {
            self.list.add(item)
        }
        return self
    }
}


object BuiltinFunListAddFirst : KtFunction(ParameterSpec("List.addFirst", listOf("item"))) {
    override fun accept(callContext: KtCallContext): FlamingoObject? {
        val self = callContext.getObjectContextOfType(FlamingoListObject::class) ?: return null
        val item = callContext.getLocal("item") ?: return null
        self.list.addFirst(item)
        return self
    }
}


fun listAdjustIndex(self: FlamingoListObject, callContext: KtCallContext): Int? {
    val index = callContext.getLocalOfType("index", FlamingoNumberObject::class) ?: return null
    var indexInt = index.assertGetInteger("index") ?: return null
    while (indexInt < 0) indexInt += self.list.size
    if (indexInt > self.list.size) {
        throwObject("%d is out of bounds for list with size %d".format(indexInt, self.list.size), IndexError)
        return null
    }
    return indexInt
}


object BuiltinFunListInsert : KtFunction(ParameterSpec("List.insert", listOf("item", "index"))) {
    override fun accept(callContext: KtCallContext): FlamingoObject? {
        val self = callContext.getObjectContextOfType(FlamingoListObject::class) ?: return null
        val item = callContext.getLocal("item") ?: return null
        val indexInt = listAdjustIndex(self, callContext) ?: return null
        self.list.add(indexInt, item)
        return Null
    }
}


object BuiltinFunListRemove : KtFunction(ParameterSpec("List.remove", listOf("index"))) {
    override fun accept(callContext: KtCallContext): FlamingoObject? {
        val self = callContext.getObjectContextOfType(FlamingoListObject::class) ?: return null
        val indexInt = listAdjustIndex(self, callContext) ?: return null
        return self.list.removeAt(indexInt)
    }
}


object BuiltinFunListRemoveObjs : KtFunction(ParameterSpec("List.removeObjects", varargs = "items")) {
    override fun accept(callContext: KtCallContext): FlamingoObject? {
        val self = callContext.getObjectContextOfType(FlamingoListObject::class) ?: return null
        val items = callContext.getLocalOfType("items", FlamingoListObject::class) ?: return null
        for (item in items.list) {
            self.list.remove(item)
        }
        return self
    }
}


object BuiltinFunListClear : KtFunction(ParameterSpec("List.clear", listOf("index"))) {
    override fun accept(callContext: KtCallContext): FlamingoObject? {
        val self = callContext.getObjectContextOfType(FlamingoListObject::class) ?: return null
        self.list.clear()
        return Null
    }
}


fun listTransformList(self: FlamingoListObject, callContext: KtCallContext): MutableList<FlamingoObject>? {
    val transform = callContext.getLocalOfType("transform", FlamingoCodeObject::class) ?: return null

    val items = mutableListOf<FlamingoObject>()

    for (item in self.list) {
        val transformed = transform.callLetting(mapOf(Pair("it", item))) ?: return null
        items.add(transformed)
    }

    return items
}


object BuiltinFunListMap : KtFunction(ParameterSpec("List.map", listOf("transform"))) {
    override fun accept(callContext: KtCallContext): FlamingoObject? {
        val self = callContext.getObjectContextOfType(FlamingoListObject::class) ?: return null

        val items = listTransformList(self, callContext) ?: return null

        self.list.clear()
        self.list.addAll(items)

        return self
    }
}


object BuiltinFunListMapped : KtFunction(ParameterSpec("List.mapped", listOf("transform"))) {
    override fun accept(callContext: KtCallContext): FlamingoObject? {
        val self = callContext.getObjectContextOfType(FlamingoListObject::class) ?: return null

        val items = listTransformList(self, callContext) ?: return null

        return FlamingoListObject(items, FlamingoListClass)
    }
}

object BuiltinFunListFilter : KtFunction(ParameterSpec("List.filter", listOf("predicate"))) {
    override fun accept(callContext: KtCallContext): FlamingoObject? {
        val self = callContext.getObjectContextOfType(FlamingoListObject::class) ?: return null
        val predicate = callContext.getLocalOfType("predicate", FlamingoCodeObject::class) ?: return null

        val iterator = self.list.iterator()

        while (iterator.hasNext()) {
            val item = iterator.next()
            val predicated = predicate.callLetting(mapOf(Pair("it", item))) ?: return null
            val predicatedTruthy = predicated.truthy() ?: return null
            if (!predicatedTruthy) iterator.remove()
        }

        return self
    }
}


object BuiltinFunListFiltered : KtFunction(ParameterSpec("List.filtered", listOf("predicate"))) {
    override fun accept(callContext: KtCallContext): FlamingoObject? {
        val self = callContext.getObjectContextOfType(FlamingoListObject::class) ?: return null
        val predicate = callContext.getLocalOfType("predicate", FlamingoCodeObject::class) ?: return null

        val items = mutableListOf<FlamingoObject>()

        for (item in self.list) {
            val predicated = predicate.callLetting(mapOf(Pair("it", item))) ?: return null
            val predicatedTruthy = predicated.truthy() ?: return null
            if (predicatedTruthy) items.add(item)
        }

        return FlamingoListObject(items, FlamingoListClass)
    }
}