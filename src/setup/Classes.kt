package setup

import objects.base.*
import objects.base.collections.*
import objects.callable.FlBuiltinObj
import objects.callable.FlCallableClass
import objects.callable.FlCodeObjClass
import objects.libraries.*
import objects.methods.*

fun initClasses() {
    // object
    FlObjClass.let {
        it.setClassAttribute("meta\$init", FlBuiltinObj(BuiltinFunObjInit))
        it.setClassAttribute("meta\$displayObject", FlBuiltinObj(BuiltinFunObjDisplayObj))
        it.setClassAttribute("meta\$toString", FlBuiltinObj(BuiltinFunObjToString))
        it.setClassAttribute("meta\$isIterable", FlBuiltinObj(BuiltinFunObjIsIter))
        it.setClassAttribute("meta\$eq", FlBuiltinObj(BuiltinFunObjEq))
        it.setClassAttribute("meta\$neq", FlBuiltinObj(BuiltinFunObjNeq))
        it.setClassAttribute("meta\$truthy", FlBuiltinObj(BuiltinFunObjTruthy))
        it.setClassAttribute("meta\$not", FlBuiltinObj(BuiltinFunObjNot))
        listOf(
            "add",
            "sub",
            "mul",
            "div",
            "pow",
            "mod",
            "lt",
            "gt",
            "lteq",
            "gteq",
            "iter",
            "index",
            "indexSet",
            "contains",
            "call",
            "minus",
            "plus"
        ).forEach { mn -> it.setClassAttribute("meta\$$mn", FlBuiltinObj(ErrWrapperExst(mn))) }

        it.setClassAttribute("meta\$getter\$type", FlBuiltinObj(BuiltinFunObjGetClass))
        it.setClassAttribute("instanceOf", FlBuiltinObj(BuiltinFunObjInstanceOf))
        it.setClassAttribute("meta\$getter\$aro", FlBuiltinObj(BuiltinFunObjAro))

        it.setClassAttribute("let", FlBuiltinObj(BuiltinFunObjLet))
        it.setClassAttribute("letIf", FlBuiltinObj(BuiltinFunObjLetIf))
        it.setClassAttribute("letContext", FlBuiltinObj(BuiltinFunObjLetContext))

        it.setClassAttribute("explicitCall", FlBuiltinObj(BuiltinFunObjExplicitCall))
    }
    // super objects
    FlSuperClass.let {
        it.setClassAttribute("meta\$new", FlBuiltinObj(ErrWrapperNew(it)))

        it.setClassAttribute("meta\$displayObject", FlBuiltinObj(BuiltinFunSuperDisplayObj))
        it.setClassAttribute("meta\$call", FlBuiltinObj(BuiltinFunSuperCall))
    }
    // reflect objects
    FlReflectClass.let {
        it.setClassAttribute("meta\$new", FlBuiltinObj(BuiltinFunClsNew))

        it.setClassAttribute("meta\$call", FlBuiltinObj(BuiltinFunClsCall))
        it.setClassAttribute("meta\$displayObject", FlBuiltinObj(BuiltinFunClsDisplayObj))

        it.setClassAttribute("meta\$getter\$name", FlBuiltinObj(BuiltinFunClsGetName))
        it.setClassAttribute("meta\$getter\$bases", FlBuiltinObj(BuiltinFunClsGetBases))
        it.setClassAttribute("meta\$getter\$attributes", FlBuiltinObj(BuiltinFunClsGetClsAttrs))
    }
    // callable
    FlCallableClass.let {
        it.setClassAttribute("meta\$new", FlBuiltinObj(ErrWrapperNew(it)))

        it.setClassAttribute("meta\$call", FlBuiltinObj(BuiltinFunCallableCall))
        it.setClassAttribute("meta\$displayObject", FlBuiltinObj(BuiltinFunCallableDisplayObj))
    }
    // code object
    FlCodeObjClass.let {
        it.setClassAttribute("meta\$new", FlBuiltinObj(ErrWrapperNew(it)))

        it.setClassAttribute("meta\$displayObject", FlBuiltinObj(BuiltinFunCodeObjDisplayObj))

        it.setClassAttribute("callLetting", FlBuiltinObj(BuiltinFunCodeObjCallLetting))
        it.setClassAttribute("callLettingIgnoreThrow", FlBuiltinObj(BuiltinFunCodeObjCallLettingIgnore))
        it.setClassAttribute("meta\$getter\$name", FlBuiltinObj(BuiltinFunCodeObjGetName))
    }
    // generic iterator
    FlGenericIteratorClass.setClassAttribute("hasNextObj", FlBuiltinObj(BuiltinFunGenIterHasNextObj))
    FlGenericIteratorClass.setClassAttribute("nextObj", FlBuiltinObj(BuiltinFunGenIterNextObj))
    // list
    FlListClass.let {
        it.setClassAttribute("meta\$new", FlBuiltinObj(BuiltinFunListNew))

        it.setClassAttribute("meta\$displayObject", FlBuiltinObj(BuiltinFunListDisplayObj))
        it.setClassAttribute("meta\$iter", FlBuiltinObj(BuiltinFunListIter))
        it.setClassAttribute("meta\$isIterable", FlBuiltinObj(BuiltinFunListIsIter))
        it.setClassAttribute("meta\$index", FlBuiltinObj(BuiltinFunListIndex))

        it.setClassAttribute("add", FlBuiltinObj(BuiltinFunListAdd))
        it.setClassAttribute("addFirst", FlBuiltinObj(BuiltinFunListAddFirst))
        it.setClassAttribute("insert", FlBuiltinObj(BuiltinFunListInsert))
        it.setClassAttribute("remove", FlBuiltinObj(BuiltinFunListRemove))
        it.setClassAttribute("removeObjects", FlBuiltinObj(BuiltinFunListRemoveObjs))
        it.setClassAttribute("clear", FlBuiltinObj(BuiltinFunListClear))
        it.setClassAttribute("meta\$getter\$size", FlBuiltinObj(BuiltinFunListSize))

        it.setClassAttribute("map", FlBuiltinObj(BuiltinFunListMap))
        it.setClassAttribute("mapped", FlBuiltinObj(BuiltinFunListMapped))
        it.setClassAttribute("filter", FlBuiltinObj(BuiltinFunListFilter))
        it.setClassAttribute("filtered", FlBuiltinObj(BuiltinFunListFiltered))
        it.setClassAttribute("where", FlBuiltinObj(BuiltinFunListWhere))
    }
    // array
    FlArrayClass.let {
        it.setClassAttribute("meta\$new", FlBuiltinObj(ErrWrapperNew(it, "arrayOf")))

        it.setClassAttribute("meta\$displayObject", FlBuiltinObj(BuiltinFunArrayDisplayObj))
        it.setClassAttribute("meta\$iter", FlBuiltinObj(BuiltinFunArrayIter))
        it.setClassAttribute("meta\$isIterable", FlBuiltinObj(BuiltinFunArrayIsIter))
    }
    // range
    FlRangeClass.setClassAttribute("meta\$displayObject", FlBuiltinObj(BuiltinFunRangeDisplayObj))
    FlRangeClass.setClassAttribute("meta\$iter", FlBuiltinObj(BuiltinFunRangeIter))
    FlRangeClass.setClassAttribute("meta\$isIterable", FlBuiltinObj(BuiltinFunRangeIsIter))
    FlRangeIterClass.setClassAttribute("hasNextObj", FlBuiltinObj(BuiltinFunRangeIterHasNextObj))
    FlRangeIterClass.setClassAttribute("nextObj", FlBuiltinObj(BuiltinFunRangeIterNextObj))
    // dictionary
    FlDictionaryClass.let {
        it.setClassAttribute("meta\$new", FlBuiltinObj(ErrWrapperNew(it, "dictOf")))
        it.setClassAttribute("meta\$displayObject", FlBuiltinObj(BuiltinFunDictDisplayObj))
    }
    // number
    FlNumberClass.let {
        it.setClassAttribute("meta\$new", FlBuiltinObj(ErrWrapperNew(it)))

        it.setClassAttribute("meta\$displayObject", FlBuiltinObj(BuiltinFunNumberDisplayObj))
        it.setClassAttribute("meta\$add", FlBuiltinObj(BuiltinFunNumberAdd))
        it.setClassAttribute("meta\$sub", FlBuiltinObj(BuiltinFunNumberSub))
        it.setClassAttribute("meta\$mul", FlBuiltinObj(BuiltinFunNumberMul))
        it.setClassAttribute("meta\$div", FlBuiltinObj(BuiltinFunNumberDiv))
        it.setClassAttribute("meta\$pow", FlBuiltinObj(BuiltinFunNumberPow))
        it.setClassAttribute("meta\$mod", FlBuiltinObj(BuiltinFunNumberMod))

        it.setClassAttribute("meta\$minus", FlBuiltinObj(BuiltinFunNumberMinus))
        it.setClassAttribute("meta\$plus", FlBuiltinObj(BuiltinFunNumberPlus))

        it.setClassAttribute("meta\$eq", FlBuiltinObj(BuiltinFunNumberEq))
        it.setClassAttribute("meta\$lt", FlBuiltinObj(BuiltinFunNumberLt))
        it.setClassAttribute("meta\$gt", FlBuiltinObj(BuiltinFunNumberGt))
        it.setClassAttribute("meta\$lteq", FlBuiltinObj(BuiltinFunNumberLtEq))
        it.setClassAttribute("meta\$gteq", FlBuiltinObj(BuiltinFunNumberGtEq))

        it.setClassAttribute("isInteger", FlBuiltinObj(BuiltinFunNumberIsInteger))
        it.setClassAttribute("isEven", FlBuiltinObj(BuiltinFunNumberIsEven))
        it.setClassAttribute("isOdd", FlBuiltinObj(BuiltinFunNumberIsOdd))

        it.setClassAttribute("floor", FlBuiltinObj(BuiltinFunNumberFloor))
        it.setClassAttribute("ceil", FlBuiltinObj(BuiltinFunNumberCeil))

        it.setClassAttribute("meta\$getter\$atom", FlBuiltinObj(BuiltinFunNumberToAtom))
    }
    // Atomic number
    FlAtomicNumberClass.let {
        it.setClassAttribute("iadd", FlBuiltinObj(BuiltinFunAtomicNumIAdd))
        it.setClassAttribute("isub", FlBuiltinObj(BuiltinFunAtomicNumISub))
        it.setClassAttribute("imul", FlBuiltinObj(BuiltinFunAtomicNumIMul))
        it.setClassAttribute("idiv", FlBuiltinObj(BuiltinFunAtomicNumIDiv))
        it.setClassAttribute("ipow", FlBuiltinObj(BuiltinFunAtomicNumIPow))
        it.setClassAttribute("imod", FlBuiltinObj(BuiltinFunAtomicNumIMod))

        it.setClassAttribute("iminus", FlBuiltinObj(BuiltinFunAtomicNumIMinus))
        it.setClassAttribute("iplus", FlBuiltinObj(BuiltinFunAtomicNumIPlus))

        it.setClassAttribute("ifloor", FlBuiltinObj(BuiltinFunAtomicNumIFloor))
        it.setClassAttribute("iceil", FlBuiltinObj(BuiltinFunAtomicNumICeil))

        it.setClassAttribute("incr", FlBuiltinObj(BuiltinFunAtomicNumIncr))
        it.setClassAttribute("decr", FlBuiltinObj(BuiltinFunAtomicNumDecr))
    }
    // string
    FlStringClass.let {
        it.setClassAttribute("meta\$new", FlBuiltinObj(ErrWrapperNew(it, "stringOf")))

        it.setClassAttribute("meta\$displayObject", FlBuiltinObj(BuiltinFunStringDisplayObj))
        it.setClassAttribute("meta\$toString", FlBuiltinObj(BuiltinFunStringToString))
        it.setClassAttribute("meta\$add", FlBuiltinObj(BuiltinFunStringAdd))
        it.setClassAttribute("meta\$mul", FlBuiltinObj(BuiltinFunStringMul))
        it.setClassAttribute("meta\$mod", FlBuiltinObj(BuiltinFunStringFormat))

        it.setClassAttribute("format", FlBuiltinObj(BuiltinFunStringFormat))
        it.setClassAttribute("toNumberOrNull", FlBuiltinObj(BuiltinFunStringToNumOrNull))

        it.setClassAttribute("meta\$getter\$atom", FlBuiltinObj(BuiltinFunStringToAtom))
    }
    // atomic string
    FlAtomicStringClass.let {
        it.setClassAttribute("iadd", FlBuiltinObj(BuiltinFunAtomicStrIAdd))
        it.setClassAttribute("iformat", FlBuiltinObj(BuiltinFunAtomicStrIFormat))
    }
    // boolean
    FlBooleanClass.let {
        it.setClassAttribute("meta\$new", FlBuiltinObj(ErrWrapperNew(it, "booleanOf")))

        it.setClassAttribute("meta\$displayObject", FlBuiltinObj(BuiltinFunBoolDisplayObj))
        it.setClassAttribute("meta\$truthy", FlBuiltinObj(BuiltinFunBoolTruthy))
    }
    // null
    FlNullClass.let {
        it.setClassAttribute("meta\$new", FlBuiltinObj(ErrWrapperNew(it)))

        it.setClassAttribute("meta\$displayObject", FlBuiltinObj(BuiltinFunNullDisplayObj))
        it.setClassAttribute("meta\$truthy", FlBuiltinObj(BuiltinFunNullTruthy))
    }
    // module
    FlModuleClass.let {
        it.setClassAttribute("meta\$new", FlBuiltinObj(ErrWrapperNew(it, "moduleOf")))
        it.setClassAttribute("meta\$displayObject", FlBuiltinObj(BuiltinFunModDisplayObj))

        it.setClassAttribute("meta\$getter\$all", FlBuiltinObj(BuiltinFunModAll))

        it.setClassAttribute("export", FlBuiltinObj(BuiltinFunModExport))
        it.setClassAttribute("getPath", FlBuiltinObj(BuiltinFunModGetPath))
    }
    // Throwable
    Throwable.let {
        it.setClassAttribute("meta\$new", FlBuiltinObj(ErrWrapperNew(it)))
    }
}