package objects.members

import objects.base.FlObject
import objects.base.collections.FlDictionaryObj
import objects.base.stringOf
import objects.callable.KtCallContext
import objects.callable.KtFunction
import objects.callable.ParameterSpec

object BuiltinFunDictionaryDisplayObj : KtFunction(ParameterSpec("Dictionary.displayObj")) {
    override fun accept(callContext: KtCallContext): FlObject? {
        val self = callContext.getObjContextOfType(FlDictionaryObj::class) ?: return null
        val stringShows = mutableListOf<String>()
        for (entry in self.dictionary) {
            val valueStringShow = entry.value.stringShow() ?: return null
            stringShows.add("%s = %s".format(entry.key, valueStringShow))
        }
        return stringOf("[%s]".format(stringShows.joinToString(", ")))
    }
}
