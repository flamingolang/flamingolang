package objects.members

import objects.base.FlamingoObject
import objects.base.collections.FlamingoDictionaryObject
import objects.base.stringOf
import objects.callable.KtCallContext
import objects.callable.KtFunction
import objects.callable.ParameterSpec

object BuiltinFunDictionaryDisplayObject : KtFunction(ParameterSpec("Dictionary.displayObject")) {
    override fun accept(callContext: KtCallContext): FlamingoObject? {
        val self = callContext.getObjectContextOfType(FlamingoDictionaryObject::class) ?: return null
        val stringShows = mutableListOf<String>()
        for (entry in self.dictionary) {
            val valueStringShow = entry.value.stringShow() ?: return null
            stringShows.add("%s = %s".format(entry.key, valueStringShow))
        }
        return stringOf("[%s]".format(stringShows.joinToString(", ")))
    }
}
