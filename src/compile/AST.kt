package compile

import objects.base.FlamingoCompilerErrorObject

open class Node(val token: Token)

// utility nodes

class HangsValue(val node: Node) : Node(node.token)
class NodeCollection(token: Token, val nodes: Collection<Node>) : Node(token)

// Node types

class BuildClass(token: Token, val name: String, val packages: List<Node>, val body: Node) : Node(token)
class BuildFunction(
    token: Token,
    val isGenerator: Boolean,
    val name: String,
    val positionals: List<String>,
    val defaults: List<String>,
    val defaultValues: List<Node>,
    val varargs: String?,
    val varkwargs: String?,
    val body: Node
) : Node(token)

class StringLiteral(token: Token, val string: String) : Node(token)
class BuildString(token: Token, val parts: Collection<Node>) : Node(token)
class NumberLiteral(token: Token, val number: Double) : Node(token)
class Lookup(token: Token, val name: String) : Node(token)
class CodeSnippet(token: Token, val snippet: Node) : Node(token)
class TrueConstant(token: Token) : Node(token)
class FalseConstant(token: Token) : Node(token)
class NullConstant(token: Token) : Node(token)
class ContextObject(token: Token) : Node(token)
class ListConstruct(token: Token, val items: Collection<Node>) : Node(token)
class ArrayConstruct(token: Token, val items: Collection<Node>) : Node(token)
class BuildRange(token: Token, val from: Node, val to: Node) : Node(token)

enum class BinaryOperationType { ADD, SUB, MUL, DIV, POW, MOD, COMP_EQUAL, COMP_NOT_EQUAL, COMP_LESS, COMP_GREATER, COMP_LESS_EQUAL, COMP_GREATER_EQUAL, COMP_IS, COMP_IS_NOT }
class BinaryOperation(token: Token, val type: BinaryOperationType, val left: Node, val right: Node) : Node(token)
enum class UnaryOperationType { NOT, MINUS, PLUS }
class UnaryOperation(token: Token, val type: UnaryOperationType, val expression: Node) : Node(token)

class BinaryAnd(token: Token, val left: Node, val right: Node) : Node(token)
class BinaryOr(token: Token, val left: Node, val right: Node) : Node(token)

class GetAttribute(token: Token, val name: String, val from: Node, val ifObjectNotNull: Boolean = false) : Node(token)
class IndexObject(token: Token, val obj: Node, val index: Node) : Node(token)
class CallObject(
    token: Token,
    val obj: Node,
    val arguments: MutableList<Node>,
    val keywords: List<String>,
    val keywordValues: List<Node>,
) : Node(token)

class CallAttributeIfNotNull(
    token: Token,
    val obj: Node,
    val name: String,
    val arguments: MutableList<Node>,
    val keywords: List<String>,
    val keywordValues: List<Node>,
) : Node(token)

class TryCatch(token: Token, val tryExpression: Node, val catchAs: String?, val catchBranch: Node) : Node(token)

// statement types

class IfThenElse(token: Token, val condition: Node, val thenBranch: Node, val elseBranch: Node?) : Node(token)
class IfTruthyElse(token: Token, val expression: Node, val elseExpression: Node) : Node(token)
class IfNotNullThen(token: Token, val obj: Node, val thenBranch: Node) : Node(token)

class ForDo(token: Token, val name: String, val iterable: Node, val body: Node) : Node(token)
class WhileDo(token: Token, val condition: Node, val body: Node) : Node(token)

class Break(token: Token) : Node(token)
class Continue(token: Token) : Node(token)
class Return(token: Token, val value: Node) : Node(token)

class LazyNameAssignment(token: Token, val name: String, val expression: Node) : Node(token)
class NameAssignment(token: Token, val name: String, val expression: Node, val isConstant: Boolean) : Node(token)
class SetAttribute(token: Token, val obj: Node, val name: String, val value: Node) : Node(token)
class SetAtIndex(token: Token, val obj: Node, val index: Node, val value: Node) : Node(token)

var i = 0
fun printAll(head: Node, vararg nodes: Any?) {
    println("    ".repeat(i) + head::class.simpleName + " {")
    i++
    for (node in nodes) {
        if (node is Node) printNode(node)
        else println("    ".repeat(i) + node.toString())
    }
    i--
    println("    ".repeat(i) + "}")
}

fun printNode(node: Node) {
    when (node) {
        is NodeCollection -> {
            for (inner in node.nodes) {
                printAll(node, inner)
            }
        }

        is HangsValue -> {
            printAll(node, node.node)

        }

        is BuildClass -> {
            printAll(node, node.name, node.packages, node.body)

        }

        is BuildFunction -> printAll(
            node,
            node.name,
            node.isGenerator,
            node.positionals,
            node.defaults,
            node.defaultValues,
            node.varargs,
            node.varkwargs,
            node.body
        )

        is StringLiteral -> {
            printAll(node, node.string)

        }

        is BuildString -> {
            printAll(node, node.parts)

        }

        is NumberLiteral -> {
            printAll(node, node.number)

        }

        is Lookup -> {
            printAll(node, node.name)

        }

        is CodeSnippet -> {
            printAll(node, node.snippet)

        }

        is TrueConstant -> {
            printAll(node, "true")

        }

        is FalseConstant -> {
            printAll(node, "false")

        }

        is NullConstant -> {
            printAll(node, "null")

        }

        is ContextObject -> {
            printAll(node, "self")

        }

        is BuildRange -> {
            printAll(node, node.from, node.to)

        }

        is ListConstruct -> {
            printAll(node, node.items)

        }

        is ArrayConstruct -> {
            printAll(node, node.items)

        }

        is BinaryOperation -> {
            printAll(node, node.type, node.left, node.right)

        }

        is UnaryOperation -> {
            printAll(node, node.type, node.expression)

        }

        is BinaryAnd -> {
            printAll(node, node.left, node.right)

        }

        is BinaryOr -> {
            printAll(node, node.left, node.right)

        }

        is GetAttribute -> {
            printAll(node, node.name, node.from, node.ifObjectNotNull)

        }

        is IndexObject -> {
            printAll(node, node.obj, node.index)

        }

        is CallObject -> {
            printAll(node, node.obj, node.arguments, node.keywords, node.keywordValues)

        }

        is CallAttributeIfNotNull -> printAll(
            node,
            node.obj,
            node.name,
            node.arguments,
            node.keywords,
            node.keywordValues,
        )

        is TryCatch -> {
            printAll(node, node.tryExpression, node.catchAs, node.catchBranch)

        }

        is IfThenElse -> {
            printAll(node, node.condition, node.thenBranch, node.elseBranch)

        }

        is IfTruthyElse -> {
            printAll(node, node.expression, node.elseExpression)

        }

        is IfNotNullThen -> {
            printAll(node, node.obj, node.thenBranch)

        }

        is ForDo -> {
            printAll(node, node.name, node.iterable, node.body)

        }

        is WhileDo -> {
            printAll(node, node.condition, node.body)

        }

        is Break -> {
            printAll(node, "break")

        }

        is Continue -> {
            printAll(node, "continue")

        }

        is Return -> {
            printAll(node, node.value)

        }

        is LazyNameAssignment -> {
            printAll(node, node.name, node.expression)

        }

        is NameAssignment -> {
            printAll(node, node.name, node.expression, node.isConstant)

        }

        is SetAttribute -> {
            printAll(node, node.obj, node.name, node.value)

        }

        is SetAtIndex -> {
            printAll(node, node.obj, node.index, node.value)

        }
    }
}

abstract class AbstractNodeVisitor {
    var currentToken: Token? = null

    open fun visit(visitor: Node) {
        val initToken = currentToken
        currentToken = visitor.token
        when (visitor) {
            is NodeCollection -> visitNodeCollection(visitor.nodes)
            is HangsValue -> visitHangsValue(visitor.node)

            is BuildClass -> visitBuildClass(visitor.name, visitor.packages, visitor.body)
            is BuildFunction -> visitBuildFunction(
                visitor.name,
                visitor.isGenerator,
                visitor.positionals,
                visitor.defaults,
                visitor.defaultValues,
                visitor.varargs,
                visitor.varkwargs,
                visitor.body
            )

            is StringLiteral -> visitStringLiteral(visitor.string)
            is BuildString -> visitBuildString(visitor.parts)
            is NumberLiteral -> visitNumberLiteral(visitor.number)
            is Lookup -> visitLookup(visitor.name)
            is CodeSnippet -> visitCodeSnippet(visitor.snippet)
            is TrueConstant -> visitTrueConstant()
            is FalseConstant -> visitFalseConstant()
            is NullConstant -> visitNullConstant()
            is ContextObject -> visitContextObject()
            is BuildRange -> visitBuildRange(visitor.from, visitor.to)

            is ListConstruct -> visitList(visitor.items)
            is ArrayConstruct -> visitArray(visitor.items)

            is BinaryOperation -> visitBinaryOperation(visitor.type, visitor.left, visitor.right)
            is UnaryOperation -> visitUnaryOperation(visitor.type, visitor.expression)

            is BinaryAnd -> visitBinaryAnd(visitor.left, visitor.right)
            is BinaryOr -> visitBinaryOr(visitor.left, visitor.right)

            is GetAttribute -> visitGetAttribute(visitor.name, visitor.from, visitor.ifObjectNotNull)
            is IndexObject -> visitIndexObject(visitor.obj, visitor.index)
            is CallObject -> visitCallObject(visitor.obj, visitor.arguments, visitor.keywords, visitor.keywordValues)
            is CallAttributeIfNotNull -> visitCallAttributeIfNotNull(
                visitor.obj,
                visitor.name,
                visitor.arguments,
                visitor.keywords,
                visitor.keywordValues,
            )

            is TryCatch -> visitTryCatch(visitor.tryExpression, visitor.catchAs, visitor.catchBranch)

            is IfThenElse -> visitIfThenElse(visitor.condition, visitor.thenBranch, visitor.elseBranch)
            is IfTruthyElse -> visitIfTruthyElse(visitor.expression, visitor.elseExpression)
            is IfNotNullThen -> visitIfNotNullThen(visitor.obj, visitor.thenBranch)
            is ForDo -> visitForDo(visitor.name, visitor.iterable, visitor.body)
            is WhileDo -> visitWhileDo(visitor.condition, visitor.body)

            is Break -> visitBreak()
            is Continue -> visitContinue()
            is Return -> visitReturn(visitor.value)

            is LazyNameAssignment -> visitLazyNameAssignment(visitor.name, visitor.expression)
            is NameAssignment -> visitNameAssignment(visitor.name, visitor.expression, visitor.isConstant)
            is SetAttribute -> visitSetAttribute(visitor.obj, visitor.name, visitor.value)
            is SetAtIndex -> visitSetAtIndex(visitor.obj, visitor.index, visitor.value)

            else -> throw CompilerEscape(
                FlamingoCompilerErrorObject("unexpected visitor '%s'".format(visitor::class.simpleName), visitor.token)
            )
        }
        currentToken = initToken
    }

    fun visitAll(visitors: Collection<Node>) {
        visitors.forEach { visit(it) }
    }

    open fun visitNodeCollection(nodes: Collection<Node>) {
        visitAll(nodes)
    }

    abstract fun visitHangsValue(node: Node)

    abstract fun visitBuildClass(name: String, packages: List<Node>, body: Node)
    abstract fun visitBuildFunction(
        name: String,
        isGenerator: Boolean,
        positionals: List<String>,
        defaults: List<String>,
        defaultValues: List<Node>,
        varargs: String?,
        varkwargs: String?,
        body: Node
    )

    abstract fun visitStringLiteral(string: String)
    abstract fun visitBuildString(parts: Collection<Node>)
    abstract fun visitNumberLiteral(number: Double)
    abstract fun visitLookup(name: String)
    abstract fun visitCodeSnippet(snippet: Node)
    abstract fun visitTrueConstant()
    abstract fun visitFalseConstant()
    abstract fun visitNullConstant()
    abstract fun visitContextObject()
    abstract fun visitBuildRange(from: Node, to: Node)

    abstract fun visitList(items: Collection<Node>)
    abstract fun visitArray(items: Collection<Node>)

    abstract fun visitBinaryOperation(type: BinaryOperationType, left: Node, right: Node)
    abstract fun visitUnaryOperation(type: UnaryOperationType, expression: Node)

    abstract fun visitBinaryAnd(left: Node, right: Node)
    abstract fun visitBinaryOr(left: Node, right: Node)

    abstract fun visitGetAttribute(name: String, from: Node, ifObjectNotNull: Boolean)
    abstract fun visitIndexObject(obj: Node, index: Node)
    abstract fun visitCallObject(
        obj: Node,
        arguments: MutableList<Node>,
        keywords: List<String>,
        keywordValues: List<Node>,
    )

    abstract fun visitCallAttributeIfNotNull(
        obj: Node,
        name: String,
        arguments: MutableList<Node>,
        keywords: List<String>,
        keywordValues: List<Node>
    )


    abstract fun visitTryCatch(tryExpression: Node, catchAs: String?, catchBranch: Node)

    abstract fun visitIfThenElse(condition: Node, thenBranch: Node, elseBranch: Node?)
    abstract fun visitIfTruthyElse(expression: Node, elseExpression: Node)
    abstract fun visitIfNotNullThen(condition: Node, thenBranch: Node)
    abstract fun visitForDo(name: String, iterable: Node, body: Node)
    abstract fun visitWhileDo(condition: Node, body: Node)

    abstract fun visitReturn(value: Node)
    abstract fun visitBreak()
    abstract fun visitContinue()

    abstract fun visitLazyNameAssignment(name: String, expression: Node)
    abstract fun visitNameAssignment(name: String, expression: Node, isConstant: Boolean)
    abstract fun visitSetAtIndex(obj: Node, index: Node, value: Node)
    abstract fun visitSetAttribute(obj: Node, name: String, value: Node)
}

