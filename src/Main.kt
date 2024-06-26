import compile.*
import objects.base.*
import objects.base.callable.FlamingoBoundMethodClass
import objects.base.callable.FlamingoFunctionClass
import objects.base.collections.*
import objects.callable.*
import objects.libraries.BuiltinFunDis
import objects.libraries.BuiltinFunImport
import objects.libraries.BuiltinFunMeta
import objects.libraries.BuiltinFunPrintln
import objects.members.*
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
    compiler.scopeStack.add(Scope("module", operations))
    val frame = outFrame ?: OperationalFrame("repl", operations, filePath = filePath)
    val lexer = Lexer(name, source)


    try {
        val parser = Parser(lexer)
        val node = parser.statements(TokenType.TOKEN_EOF)
        parser.assertFinished()
        compiler.visit(node)
    } catch (error: CompilerEscape) {
        throwObject(error.error)
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


fun getStandardBuiltins(): HashMap<String, FlamingoObject> {
    val builtins = HashMap<String, FlamingoObject>()

    builtins["Object"] = FlamingoObjectClass.reflectObject
    builtins["Class"] = FlamingoReflectClass.reflectObject
    builtins["Boolean"] = FlamingoBooleanClass.reflectObject
    builtins["Callable"] = FlamingoCallableClass.reflectObject
    builtins["Function"] = FlamingoFunctionClass.reflectObject
    builtins["BoundMethod"] = FlamingoBoundMethodClass.reflectObject
    builtins["CodeObject"] = FlamingoCodeObjectClass.reflectObject
    builtins["Dictionary"] = FlamingoDictionaryClass.reflectObject
    builtins["List"] = FlamingoListClass.reflectObject
    builtins["Array"] = FlamingoArrayClass.reflectObject
    builtins["NullClass"] = FlamingoNullClass.reflectObject
    builtins["Number"] = FlamingoStringClass.reflectObject
    builtins["String"] = FlamingoNumberClass.reflectObject

    builtins["println"] = FlamingoBuiltinFun(BuiltinFunPrintln)
    builtins["dis"] = FlamingoBuiltinFun(BuiltinFunDis)
    builtins["meta"] = FlamingoBuiltinFun(BuiltinFunMeta)
    builtins["import"] = FlamingoBuiltinFun(BuiltinFunImport)

    return builtins
}


lateinit var builtins: HashMap<String, FlamingoObject>

fun initFlamingo() {
    // object
    FlamingoObjectClass.let {
        it.setClassAttribute("meta\$init", FlamingoBuiltinFun(BuiltinFunObjInit))
        it.setClassAttribute("meta\$displayObject", FlamingoBuiltinFun(BuiltinFunObjDisplayObject))
        it.setClassAttribute("meta\$toString", FlamingoBuiltinFun(BuiltinFunObjToString))
        it.setClassAttribute("meta\$isIterable", FlamingoBuiltinFun(BuiltinFunObjIsIter))
        it.setClassAttribute("meta\$eq", FlamingoBuiltinFun(BuiltinFunObjEq))
        it.setClassAttribute("meta\$neq", FlamingoBuiltinFun(BuiltinFunObjNeq))
        it.setClassAttribute("meta\$truthy", FlamingoBuiltinFun(BuiltinFunObjTruthy))
        it.setClassAttribute("meta\$not", FlamingoBuiltinFun(BuiltinFunObjNot))
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
            "hasNextObject",
            "nextObject",
            "index",
            "indexSet",
            "call",
            "minus",
            "plus"
        ).forEach { mn -> it.setClassAttribute("meta\$$mn", FlamingoBuiltinFun(ErrorWrapperKtFunctionAny(mn))) }

        it.setClassAttribute("getClass", FlamingoBuiltinFun(BuiltinFunObjGetClass))
        it.setClassAttribute("instanceOf", FlamingoBuiltinFun(BuiltinFunObjInstanceOf))
        it.setClassAttribute("aro", FlamingoBuiltinFun(BuiltinFunObjAro))

        it.setClassAttribute("let", FlamingoBuiltinFun(BuiltinFunObjLet))
        it.setClassAttribute("letIf", FlamingoBuiltinFun(BuiltinFunObjLetIf))

        it.setClassAttribute("explicitCall", FlamingoBuiltinFun(BuiltinFunObjExplicitCall))
    }
    // reflect objects
    FlamingoReflectClass.let {
        it.setClassAttribute("meta\$new", FlamingoBuiltinFun(BuiltinFunClsNew))

        it.setClassAttribute("meta\$call", FlamingoBuiltinFun(BuiltinFunClsCall))
        it.setClassAttribute("meta\$displayObject", FlamingoBuiltinFun(BuiltinFunClsDisplayObject))
    }
    // callable
    FlamingoCallableClass.setClassAttribute("meta\$call", FlamingoBuiltinFun(BuiltinFunCallableCall))
    FlamingoCallableClass.setClassAttribute("meta\$displayObject", FlamingoBuiltinFun(BuiltinFunCallableDisplayObject))
    // code object
    FlamingoCodeObjectClass.setClassAttribute("meta\$displayObject", FlamingoBuiltinFun(BuiltinFunCodeObjDisplayObject))
    FlamingoCodeObjectClass.setClassAttribute("callLetting", FlamingoBuiltinFun(BuiltinFunCodeObjCallLetting))
    // generic iterator
    FlamingoGenericIteratorClass.setClassAttribute("hasNextObject", FlamingoBuiltinFun(BuiltinFunGenIterHasNextObject))
    FlamingoGenericIteratorClass.setClassAttribute("nextObject", FlamingoBuiltinFun(BuiltinFunGenIterNextObject))
    // list
    FlamingoListClass.let {
        it.setClassAttribute("meta\$displayObject", FlamingoBuiltinFun(BuiltinFunListDisplayObject))
        it.setClassAttribute("meta\$iter", FlamingoBuiltinFun(BuiltinFunListIter))
        it.setClassAttribute("meta\$isIterable", FlamingoBuiltinFun(BuiltinFunListIsIter))

        it.setClassAttribute("add", FlamingoBuiltinFun(BuiltinFunListAdd))
        it.setClassAttribute("addFirst", FlamingoBuiltinFun(BuiltinFunListAddFirst))
        it.setClassAttribute("insert", FlamingoBuiltinFun(BuiltinFunListInsert))
        it.setClassAttribute("remove", FlamingoBuiltinFun(BuiltinFunListRemove))
        it.setClassAttribute("removeObjects", FlamingoBuiltinFun(BuiltinFunListRemoveObjs))
        it.setClassAttribute("clear", FlamingoBuiltinFun(BuiltinFunListClear))

        it.setClassAttribute("map", FlamingoBuiltinFun(BuiltinFunListMap))
        it.setClassAttribute("mapped", FlamingoBuiltinFun(BuiltinFunListMapped))
        it.setClassAttribute("filter", FlamingoBuiltinFun(BuiltinFunListFilter))
        it.setClassAttribute("filtered", FlamingoBuiltinFun(BuiltinFunListFiltered))
    }
    // array
    FlamingoArrayClass.setClassAttribute("meta\$displayObject", FlamingoBuiltinFun(BuiltinFunArrayDisplayObject))
    FlamingoArrayClass.setClassAttribute("meta\$iter", FlamingoBuiltinFun(BuiltinFunArrayIter))
    FlamingoArrayClass.setClassAttribute("meta\$isIterable", FlamingoBuiltinFun(BuiltinFunArrayIsIter))
    // range
    FlamingoRangeClass.setClassAttribute("meta\$displayObject", FlamingoBuiltinFun(BuiltinFunRangeDisplayObject))
    FlamingoRangeClass.setClassAttribute("meta\$iter", FlamingoBuiltinFun(BuiltinFunRangeIter))
    FlamingoRangeClass.setClassAttribute("meta\$isIterable", FlamingoBuiltinFun(BuiltinFunRangeIsIter))
    FlamingoRangeIterClass.setClassAttribute("hasNextObject", FlamingoBuiltinFun(BuiltinFunRangeIterHasNextObj))
    FlamingoRangeIterClass.setClassAttribute("nextObject", FlamingoBuiltinFun(BuiltinFunRangeIterNextObj))
    // dictionary
    FlamingoDictionaryClass.setClassAttribute(
        "meta\$displayObject",
        FlamingoBuiltinFun(BuiltinFunDictionaryDisplayObject)
    )
    // number
    FlamingoNumberClass.let {
        it.setClassAttribute("meta\$displayObject", FlamingoBuiltinFun(BuiltinFunNumberDisplayObject))
        it.setClassAttribute("meta\$add", FlamingoBuiltinFun(BuiltinFunNumberAdd))
        it.setClassAttribute("meta\$sub", FlamingoBuiltinFun(BuiltinFunNumberSub))
        it.setClassAttribute("meta\$mul", FlamingoBuiltinFun(BuiltinFunNumberMul))
        it.setClassAttribute("meta\$div", FlamingoBuiltinFun(BuiltinFunNumberDiv))
        it.setClassAttribute("meta\$pow", FlamingoBuiltinFun(BuiltinFunNumberPow))
        it.setClassAttribute("meta\$mod", FlamingoBuiltinFun(BuiltinFunNumberMod))

        it.setClassAttribute("meta\$minus", FlamingoBuiltinFun(BuiltinFunNumberMinus))
        it.setClassAttribute("meta\$plus", FlamingoBuiltinFun(BuiltinFunNumberPlus))

        it.setClassAttribute("meta\$eq", FlamingoBuiltinFun(BuiltinFunNumberEq))
        it.setClassAttribute("meta\$lt", FlamingoBuiltinFun(BuiltinFunNumberLt))
        it.setClassAttribute("meta\$gt", FlamingoBuiltinFun(BuiltinFunNumberGt))
        it.setClassAttribute("meta\$lteq", FlamingoBuiltinFun(BuiltinFunNumberLtEq))
        it.setClassAttribute("meta\$gteq", FlamingoBuiltinFun(BuiltinFunNumberGtEq))

    }
    // string
    FlamingoStringClass.let {
        it.setClassAttribute("meta\$displayObject", FlamingoBuiltinFun(BuiltinFunStringDisplayObject))
        it.setClassAttribute("meta\$toString", FlamingoBuiltinFun(BuiltinFunStringToString))
        it.setClassAttribute("meta\$add", FlamingoBuiltinFun(BuiltinFunStringAdd))
        it.setClassAttribute("meta\$mul", FlamingoBuiltinFun(BuiltinFunStringMul))
        it.setClassAttribute("meta\$mod", FlamingoBuiltinFun(BuiltinFunStringFormat))

        it.setClassAttribute("format", FlamingoBuiltinFun(BuiltinFunStringFormat))
        it.setClassAttribute("toNumberOrNull", FlamingoBuiltinFun(BuiltinFunStringToNumOrNull))
    }
    // boolean
    FlamingoBooleanClass.let {
        it.setClassAttribute("meta\$displayObject", FlamingoBuiltinFun(BuiltinFunBoolDisplayObject))
        it.setClassAttribute("meta\$truthy", FlamingoBuiltinFun(BuiltinFunBoolTruthy))
    }
    // null
    FlamingoNullClass.let {
        it.setClassAttribute("meta\$displayObject", FlamingoBuiltinFun(BuiltinFunNullDisplayObject))
        it.setClassAttribute("meta\$truthy", FlamingoBuiltinFun(BuiltinFunNullTruthy))
    }

    builtins = getStandardBuiltins()
}

fun main(args: Array<String>) {
    initFlamingo()

    val operations = ArrayList<Operation>()
    val frame: OperationalFrame

    if (args.isNotEmpty()) args[0].let {
        val file = Path.of(it).toFile()
        frame = OperationalFrame("module", operations, filePath = file.absolutePath)

        compile(
            file.name, readFile(file), outFrame = frame, outFrameOperations = operations, filePath = file.absolutePath
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

