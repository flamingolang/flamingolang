package setup

import objects.base.*
import objects.base.callable.FlBoundMethodClass
import objects.base.callable.FlFunctionClass
import objects.base.collections.FlArrayClass
import objects.base.collections.FlDictionaryClass
import objects.base.collections.FlListClass
import objects.callable.FlBuiltinObj
import objects.callable.FlCallableClass
import objects.callable.FlCodeObjClass
import objects.libraries.*

fun getStandardBuiltins(): HashMap<String, FlObject> {
    val builtins = HashMap<String, FlObject>()

    // classes

    builtins["Object"] = FlObjClass.reflectObj
    builtins["Class"] = FlReflectClass.reflectObj
    builtins["Boolean"] = FlBooleanClass.reflectObj
    builtins["Callable"] = FlCallableClass.reflectObj
    builtins["Function"] = FlFunctionClass.reflectObj
    builtins["BoundMethod"] = FlBoundMethodClass.reflectObj
    builtins["CodeObj"] = FlCodeObjClass.reflectObj
    builtins["Dictionary"] = FlDictionaryClass.reflectObj
    builtins["List"] = FlListClass.reflectObj
    builtins["Array"] = FlArrayClass.reflectObj
    builtins["NullClass"] = FlNullClass.reflectObj
    builtins["Number"] = FlStringClass.reflectObj
    builtins["String"] = FlNumberClass.reflectObj

    // throwable classes

    builtins["Throwable"] = Throwable.reflectObj
    builtins["Exception"] = Exception.reflectObj
    builtins["IterationException"] = IterationException.reflectObj
    builtins["ZeroDivisionException"] = ZeroDivisionException.reflectObj
    builtins["FatalError"] = FatalError.reflectObj
    builtins["ImportFatality"] = ImportFatality.reflectObj
    builtins["StackOverflowFatality"] = StackOverflowFatality.reflectObj
    builtins["Error"] = Error.reflectObj
    builtins["TypeError"] = TypeError.reflectObj
    builtins["NameError"] = NameError.reflectObj
    builtins["ArgumentError"] = ArgumentError.reflectObj
    builtins["AssignmentError"] = AssignmentError.reflectObj
    builtins["SyntaxError"] = SyntaxError.reflectObj
    builtins["SizeError"] = SizeError.reflectObj
    builtins["ValueError"] = ValueError.reflectObj
    builtins["IndexError"] = IndexError.reflectObj
    builtins["AttributeError"] = AttributeError.reflectObj

    // builtin helper functions

    builtins["println"] = FlBuiltinObj(BuiltinFunPrintln)
    builtins["dis"] = FlBuiltinObj(BuiltinFunDis)
    builtins["import"] = FlBuiltinObj(BuiltinFunImport)

    // builtin helper decorators

    builtins["meta"] = FlBuiltinObj(BuiltinFunMeta)
    builtins["getter"] = FlBuiltinObj(BuiltinFunGetter)
    builtins["setter"] = FlBuiltinObj(BuiltinFunSetter)

    // other builtins

    builtins["listOf"] = FlBuiltinObj(BuiltinFunListOf)
    builtins["arrayOf"] = FlBuiltinObj(BuiltinFunArrayOf)
    builtins["dictOf"] = FlBuiltinObj(BuiltinFunDictOf)
    builtins["functionOf"] = FlBuiltinObj(BuiltinFunFunctionOf)
    builtins["classOf"] = FlBuiltinObj(BuiltinFunClassOf)

    builtins["map"] = FlBuiltinObj(BuiltinFunMap)
    builtins["mapWhere"] = FlBuiltinObj(BuiltinFunMapWhere)
    builtins["filter"] = FlBuiltinObj(BuiltinFunFilter)

    return builtins
}

lateinit var builtins: HashMap<String, FlObject>