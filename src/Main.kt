import compile.*
import objects.base.*
import objects.base.callable.FlBoundMethodClass
import objects.base.callable.FlFunctionClass
import objects.base.collections.*
import objects.callable.FlBuiltinObj
import objects.callable.FlCallableClass
import objects.callable.FlCodeObjClass
import objects.libraries.*
import objects.methods.*
import runtime.*
import java.io.File
import java.nio.file.Path

fun getReplInput(): String {
    val sourceOutput = StringBuilder()
    print(">>> ")
    var input = readln()

    var (p, s, b) = arrayOf(0, 0, 0)

    while (true) {
        for (char in input) {
            when (char) {
                '(' -> p++
                '[' -> s++
                '{' -> b++
                ')' -> p--
                ']' -> s--
                '}' -> b--
            }
        }
        sourceOutput.append(input).append('\n')
        if (p <= 0 && s <= 0 && b <= 0) break
        else {
            print("... ")
            input = readln().replace('\t', ' ')
        }
    }
    return sourceOutput.toString()
}

fun compile(
    name: String,
    source: String,
    outFrame: OperationalFrame? = null,
    outFrameOperations: MutableCollection<Operation>? = null,
    filePath: String? = null
): OperationalFrame? {
    val compiler = Compiler(filePath = filePath)
    val operations = outFrameOperations ?: ArrayList()
    compiler.scopeStack.add(Scope(name, operations))
    val frame = outFrame ?: OperationalFrame(name, operations, filePath = filePath)
    val lexer = Lexer(name, source)


    try {
        val parser = Parser(lexer)
        val node = parser.statements(TokenType.TOKEN_EOF)
        parser.assertFinished()
        compiler.visit(node)
    } catch (error: CompilerEscape) {
        throwObj(error.error)
        return null
    }

    return frame
}


fun readFile(file: File): String {
    val inputStream = file.inputStream()
    val fileString = StringBuilder()
    inputStream.bufferedReader().forEachLine { fileString.append(it).append('\n') }
    return fileString.toString()
}


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
    builtins["meta"] = FlBuiltinObj(BuiltinFunMeta)
    builtins["import"] = FlBuiltinObj(BuiltinFunImport)

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

fun initFl() {
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

        it.setClassAttribute("getClass", FlBuiltinObj(BuiltinFunObjGetClass))
        it.setClassAttribute("instanceOf", FlBuiltinObj(BuiltinFunObjInstanceOf))
        it.setClassAttribute("aro", FlBuiltinObj(BuiltinFunObjAro))

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

        it.setClassAttribute("getName", FlBuiltinObj(BuiltinFunClsGetName))
        it.setClassAttribute("getBases", FlBuiltinObj(BuiltinFunClsGetBases))
        it.setClassAttribute("getClassAttributes", FlBuiltinObj(BuiltinFunClsGetClsAttrs))
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
        it.setClassAttribute("getName", FlBuiltinObj(BuiltinFunCodeObjGetName))
    }
    // generic iterator
    FlGenericIteratorClass.setClassAttribute("hasNextObj", FlBuiltinObj(BuiltinFunGenIterHasNextObj))
    FlGenericIteratorClass.setClassAttribute("nextObj", FlBuiltinObj(BuiltinFunGenIterNextObj))
    // list
    FlListClass.let {
        it.setClassAttribute("meta\$new", FlBuiltinObj(ErrWrapperNew(it, "listOf")))

        it.setClassAttribute("meta\$displayObject", FlBuiltinObj(BuiltinFunListDisplayObj))
        it.setClassAttribute("meta\$iter", FlBuiltinObj(BuiltinFunListIter))
        it.setClassAttribute("meta\$isIterable", FlBuiltinObj(BuiltinFunListIsIter))
        it.setClassAttribute("meta\$index", FlBuiltinObj(BuiltinFunListIndex))

        it.setClassAttribute("add", FlBuiltinObj(BuiltinFunListAdd))
        it.setClassAttribute("addFirst", FlBuiltinObj(BuiltinFunListAddFirst))
        it.setClassAttribute("insert", FlBuiltinObj(BuiltinFunListInsert))
        it.setClassAttribute("remove", FlBuiltinObj(BuiltinFunListRemove))
        it.setClassAttribute("removeObjs", FlBuiltinObj(BuiltinFunListRemoveObjs))
        it.setClassAttribute("clear", FlBuiltinObj(BuiltinFunListClear))
        it.setClassAttribute("size", FlBuiltinObj(BuiltinFunListSize))

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

        it.setClassAttribute("toAtom", FlBuiltinObj(BuiltinFunNumberToAtom))
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
    }
    // boolean
    FlBooleanClass.let {
        it.setClassAttribute("meta\$new", FlBuiltinObj(ErrWrapperNew(it, "booleanOf")))

        it.setClassAttribute("meta\$displayObject", FlBuiltinObj(BuiltinFunBoolDisplayObj))
        it.setClassAttribute("meta\$truthy", FlBuiltinObj(BuiltinFunBoolTruthy))
    }
    // null
    FlNullClass.let {
        it.setClassAttribute("meta\$new", FlBuiltinObj(ErrWrapperNew(it, )))

        it.setClassAttribute("meta\$displayObject", FlBuiltinObj(BuiltinFunNullDisplayObj))
        it.setClassAttribute("meta\$truthy", FlBuiltinObj(BuiltinFunNullTruthy))
    }
    // module
    FlModuleClass.let {
        it.setClassAttribute("meta\$new", FlBuiltinObj(ErrWrapperNew(it, "moduleOf")))
        it.setClassAttribute("meta\$displayObject", FlBuiltinObj(BuiltinFunModDisplayObj))

        it.setClassAttribute("export", FlBuiltinObj(BuiltinFunModExport))
        it.setClassAttribute("getPath", FlBuiltinObj(BuiltinFunModGetPath))
    }

    builtins = getStandardBuiltins()
}

fun getBuiltinLibrary(): FlModuleObj {
    val builtinsLib = FlModuleObj("builtins", null)
    builtins.entries.forEach { (key, value) -> builtinsLib.moduleAttributes[key] = value }
    return builtinsLib
}

fun initModules() {
    builtinModules["builtins"] = getBuiltinLibrary()
    builtinModules["importlib"] = getImportLibrary()
    builtinModules["stringlib"] = getStringLibrary()
}

fun main(args: Array<String>) {
    initFl()
    initModules()

    val operations = ArrayList<Operation>()
    val frame: OperationalFrame

    if (args.isNotEmpty()) args[0].let {
        // val runArgs = args.toList().subList(1, args.size).toMutableList()
        // runArgs.addFirst("\"%s\"".format(args[0]))
        // val runArgsCall = runArgs.joinToString(", ")


        val file = Path.of(it).toFile()
        frame = OperationalFrame("module", operations, filePath = file.absolutePath)

        compile(
            file.nameWithoutExtension, readFile(file), outFrame = frame, outFrameOperations = operations, filePath = file.absolutePath
        )
        if (vmThrown != null) {
            printError(vmThrown!!, vmCallStack)
            return@let
        }

        val result = execute(frame, propagateError = false)
        if (result.thrown != null) {
            printError(result.thrown, result.stackSnapshot)
        }
    } else {
        frame = OperationalFrame("repl", operations)
    }

    // java -jar /home/shaun/IdeaProjects/flamingo/out/artifacts/flamingo_jar/flamingo.jar ~/Documents/test.fl

    val compiler = Compiler()

    val operationScope = Scope("repl", operations)
    compiler.scopeStack.add(operationScope)

    // ({ it + " world" }).callLetting(it = "hello")

    while (true) {
        // for repl specifically make the disassembly offset and frame ip point to the next instruction

        frame.ip = frame.size

        // Compile input
        val lexer = Lexer("input", getReplInput())

        val start = System.nanoTime()
        try {
            val parser = Parser(lexer)
            val node = parser.statements(TokenType.TOKEN_EOF)
            parser.assertFinished()
            if (node is HangsValue) {
                compiler.visit(node.node)
                operations.add(CompiledOperation(node.token, OpCode.RETURN_VALUE, arrayOf()))
            } else compiler.visit(node)
        } catch (error: CompilerEscape) {
            printError(error.error, vmCallStack)
            continue
        }

        // Execute compilation

        val execution = execute(frame, propagateError = false)

        val time = System.nanoTime() - start

        execution.thrown?.let {
            printError(it, execution.stackSnapshot)
        }

        execution.result?.let {
            if (it != Null) {
                val result = it.stringShow()
                if (result != null) printGreenLine(result)
                else printError(vmThrown!!, vmCallStack)
            }
        }

        printBlueLine("%sms (%sns)".format(time / 1_000_000, time))
    }
}