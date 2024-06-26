import compile.BinaryOperationType
import compile.Jump
import objects.base.FlamingoObject
import objects.callable.CallSpec
import objects.callable.PartialCodeObject
import objects.callable.PartialFunction
import runtime.CompiledOperation
import runtime.Frame
import runtime.Operation

fun disOperands(operands: Array<Any>): String {
    val disassembly = ArrayList<String>()
    for (operand in operands) {
        disassembly.add(
            when (operand) {
                is FlamingoObject -> operand.displaySafe()
                is Frame -> "%s(%s)".format(operand::class.simpleName, operand.name)
                is Jump -> "to %d".format(operand.to)
                is String -> "\"%s\"".format(operand)
                is PartialFunction -> "(%d, %d)".format(
                    operand.positionals?.size ?: 0,
                    operand.defaults?.size ?: 0,
                )

                is PartialCodeObject -> "(code '%s')".format(operand.scope.name)
                is CallSpec -> "(%d, %d)".format(operand.arguments, operand.keywords.size)
                // binary operation types
                is BinaryOperationType -> when (operand) {
                    BinaryOperationType.ADD -> "+"
                    BinaryOperationType.SUB -> "-"
                    BinaryOperationType.MUL -> "*"
                    BinaryOperationType.DIV -> "/"
                    BinaryOperationType.POW -> "^"
                    BinaryOperationType.MOD -> "%"
                    BinaryOperationType.COMP_EQUAL -> "=="
                    BinaryOperationType.COMP_NOT_EQUAL -> "!="
                    BinaryOperationType.COMP_LESS -> "<"
                    BinaryOperationType.COMP_GREATER -> ">"
                    BinaryOperationType.COMP_LESS_EQUAL -> "<="
                    BinaryOperationType.COMP_GREATER_EQUAL -> ">="
                    BinaryOperationType.COMP_IS -> "is"
                    BinaryOperationType.COMP_IS_NOT -> "is not"
                }

                else -> operand.toString()
            }
        )
    }
    return disassembly.joinToString(", ")
}

fun disOperations(name: String, operations: Collection<Operation>, offset: Int = 0): String {
    if (offset >= operations.size) return "disassembly of %s is empty".format(name)
    val disassembly = StringBuilder("disassembly of %s:\n".format(name))
    val jumps = HashSet<Int>()
    val otherCodeDisassemblies = HashSet<PartialCodeObject>()

    for ((i, operation) in operations.withIndex()) {
        for (operand in operation.operands) {
            when (operand) {
                is Jump -> jumps.add(operand.to)
                is PartialCodeObject -> {
                    if (i >= offset) otherCodeDisassemblies.add(operand)
                }
            }
        }
    }

    for (otherCodeDisassembly in otherCodeDisassemblies) {
        disassembly.insert(
            0, disOperations(
                "%s (%s)".format(otherCodeDisassembly.name, otherCodeDisassembly.scope.name),
                otherCodeDisassembly.scope.operations,
                0
            ) + '\n'
        )
    }

    // list.filter { type == number and it < 3 }
    // [_ for _ in list if isinstance(_, number) and _ < 3]

    var line = 0

    for (i in offset..<operations.size) {
        val operation = operations.elementAt(i)
        disassembly.append("  ")
        if (operation is CompiledOperation) {
            // adding 1 to the token line because it's first line is 0
            if (operation.token.lineStart + 1 != line) {
                line = operation.token.lineStart + 1
                disassembly.append("%4d".format(line))
            } else {
                disassembly.append("    ")
            }
        } else {
            disassembly.append("    ")
        }

        if (jumps.contains(i)) disassembly.append("  >>  ")
        else disassembly.append("      ")

        disassembly.append(" %4d ".format(i))

        disassembly.append("%-20s".format(operation.opCode.name))
        if (operation.operands.isNotEmpty()) disassembly.append('(').append(disOperands(operation.operands)).append(')')

        disassembly.append('\n')
    }

    return disassembly.toString()
}

fun breakpoint() {
    println("==( Flamingo Debugger )==")
}
