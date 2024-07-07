import compile.*
import objects.base.*
import objects.libraries.*
import runtime.*
import setup.builtins
import setup.getStandardBuiltins
import setup.initClasses
import setup.initLibraries
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


fun initFl() {
    builtins = getStandardBuiltins()
    initClasses()
    initLibraries()
}

fun main(args: Array<String>) {
    initFl()

    val operations = ArrayList<Operation>()
    val frame: OperationalFrame

    if (args.isNotEmpty()) args[0].let {
        // val runArgs = args.toList().subList(1, args.size).toMutableList()
        // runArgs.addFirst("\"%s\"".format(args[0]))
        // val runArgsCall = runArgs.joinToString(", ")


        val file = Path.of(it).toFile()
        frame = OperationalFrame("module", operations, filePath = file.absolutePath)

        mainFrame = frame

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
        mainFrame = frame
    }

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