package compile

import objects.base.*
import objects.callable.CallSpec
import objects.callable.PartialCodeObject
import objects.callable.PartialFunction
import runtime.CompiledOperation
import runtime.OpCode
import runtime.Operation
import java.util.*

data class Jump(var to: Int = 0)
class Scope(
    val name: String,
    val operations: MutableCollection<Operation> = ArrayList(),
    val isFunction: Boolean = false
) {
    val jumpStack = Stack<Pair<Jump, Jump>>()
}


abstract class AbstractCompiler(val filePath: String? = null) : AbstractNodeVisitor() {
    val scopeStack = Stack<Scope>()

    fun addOperation(opCode: OpCode, vararg operands: Any) {
        scopeStack.peek().operations.add(CompiledOperation(currentToken!!, opCode, arrayOf(*operands)))
    }

    fun addNewJumpPair(start: Jump, end: Jump) {
        scopeStack.peek().jumpStack.push(Pair(start, end))
    }

    fun jumpHere(jump: Jump): Jump {
        jump.to = scopeStack.peek().operations.size
        return jump
    }
}

open class Compiler(filePath: String? = null) : AbstractCompiler(filePath = filePath) {
    private fun cantCompile(message: String, token: Token? = null) {
        throw CompilerEscape(FlamingoCompilerErrorObject(message, token ?: currentToken!!))
    }

    private fun addJumpOperation(to: Jump) {
        addOperation(OpCode.JUMP_ABSOLUTE, to)
    }

    private fun addScope(name: String, isFunction: Boolean = false) {
        val scopeName = if (scopeStack.isEmpty()) name else "%s.%s".format(scopeStack.peek().name, name)
        scopeStack.add(Scope(scopeName, isFunction = isFunction))
    }

    // visitors

    override fun visitHangsValue(node: Node) {
        visit(node)
        addOperation(OpCode.POP_TOP)
    }

    override fun visitBuildClass(name: String, packages: List<Node>, body: Node) {
        visitAll(packages)
        addScope(name)
        visit(body)
        addOperation(OpCode.BUILD_CODE, PartialCodeObject(name, scopeStack.pop(), filePath = filePath))
        addOperation(OpCode.BUILD_CLASS, name, packages.size)
    }

    override fun visitBuildFunction(
        name: String,
        isGenerator: Boolean,
        positionals: List<String>,
        defaults: List<String>,
        defaultValues: List<Node>,
        varargs: String?,
        varkwargs: String?,
        body: Node
    ) {
        visitAll(defaultValues)
        addScope(name, isFunction = true)
        visit(body)
        addOperation(OpCode.BUILD_CODE, PartialCodeObject(name, scopeStack.pop(), filePath = filePath))
        addOperation(
            OpCode.BUILD_FUNCTION, PartialFunction(isGenerator, positionals, defaults, varargs, varkwargs)
        )
    }

    override fun visitStringLiteral(string: String) {
        addOperation(OpCode.LOAD_CONST, stringOf(string))
    }

    override fun visitBuildString(parts: Collection<Node>) {
        visitAll(parts)
        addOperation(OpCode.BUILD_STRING, parts.size)
    }

    override fun visitNumberLiteral(number: Double) {
        addOperation(OpCode.LOAD_CONST, numberOf(number))
    }

    override fun visitLookup(name: String) {
        addOperation(OpCode.LOAD_NAME, name)
    }

    override fun visitCodeSnippet(snippet: Node) {
        addScope("anonymous-lambda", isFunction = true)
        visit(snippet)
        addOperation(
            OpCode.BUILD_CODE, PartialCodeObject("anonymous-lambda", scopeStack.pop(), filePath = filePath)
        )
    }

    override fun visitTrueConstant() {
        addOperation(OpCode.LOAD_CONST, True)
    }

    override fun visitFalseConstant() {
        addOperation(OpCode.LOAD_CONST, False)
    }

    override fun visitNullConstant() {
        addOperation(OpCode.LOAD_CONST, Null)
    }

    override fun visitContextObject() {
        addOperation(OpCode.LOAD_CTX)
    }

    override fun visitBuildRange(from: Node, to: Node) {
        visit(from)
        visit(to)
        addOperation(OpCode.BUILD_RANGE)
    }

    override fun visitList(items: Collection<Node>) {
        visitAll(items)
        addOperation(OpCode.BUILD_LIST, items.size)
    }

    override fun visitArray(items: Collection<Node>) {
        visitAll(items)
        addOperation(OpCode.BUILD_ARRAY, items.size)

    }

    override fun visitBinaryOperation(type: BinaryOperationType, left: Node, right: Node) {
        visit(left)
        visit(right)
        addOperation(OpCode.BINARY_OPERATION, type)
    }

    override fun visitUnaryOperation(type: UnaryOperationType, expression: Node) {
        visit(expression)
        addOperation(OpCode.UNARY_OPERATION, type)
    }

    override fun visitBinaryAnd(left: Node, right: Node) {
        val end = Jump()
        visit(left)
        addOperation(OpCode.POP_JUMP_IF_FALSE, end)
        visit(right)
        jumpHere(end)
    }

    override fun visitBinaryOr(left: Node, right: Node) {
        val end = Jump()
        visit(left)
        addOperation(OpCode.JUMP_IF_TRUE, end)
        visit(right)
        jumpHere(end)
    }

    override fun visitGetAttribute(name: String, from: Node, ifObjectNotNull: Boolean) {
        visit(from)
        val end = Jump()
        if (ifObjectNotNull) addOperation(OpCode.JUMP_IF_NULL, end)
        addOperation(OpCode.GET_ATTR, name)
        jumpHere(end)
    }

    override fun visitIndexObject(obj: Node, index: Node) {
        visit(index)
        visit(obj)
        addOperation(OpCode.INDEX_TOP)
    }

    override fun visitCallObject(
        obj: Node, arguments: MutableList<Node>, keywords: List<String>, keywordValues: List<Node>
    ) {
        visit(obj)
        visitAll(keywordValues)
        visitAll(arguments)
        addOperation(OpCode.CALL, CallSpec(arguments.size, keywords))
    }

    override fun visitCallAttributeIfNotNull(
        obj: Node,
        name: String,
        arguments: MutableList<Node>,
        keywords: List<String>,
        keywordValues: List<Node>
    ) {
        val end = Jump()
        visit(obj)
        addOperation(OpCode.JUMP_IF_NULL, end)
        addOperation(OpCode.GET_ATTR, name)
        visitAll(keywordValues)
        visitAll(arguments)
        addOperation(OpCode.CALL, CallSpec(arguments.size, keywords))
        jumpHere(end)
    }

    override fun visitTryCatch(tryExpression: Node, catchAs: String?, catchBranch: Node) {
        val catchJump = Jump()
        val okJump = Jump()
        if (catchAs != null) addOperation(OpCode.SETUP_TRY_AS, catchJump, catchAs) else addOperation(
            OpCode.SETUP_TRY, catchJump
        )
        visit(tryExpression)
        addOperation(OpCode.FINISH_TRY)
        addJumpOperation(okJump)
        jumpHere(catchJump)
        visit(catchBranch)
        jumpHere(okJump)
    }

    override fun visitIfThenElse(condition: Node, thenBranch: Node, elseBranch: Node?) {
        val end = Jump()

        visit(condition)
        addOperation(OpCode.POP_JUMP_IF_FALSE, end)
        visit(thenBranch)

        if (elseBranch != null) {
            val finalEnd = Jump()
            // because there's an else branch, an absolute jump is needed by the main if body to the end of the branch
            addJumpOperation(finalEnd)
            // because of else branch, the falsy jumps to the else
            jumpHere(end)
            visit(elseBranch)

            // the absolute jump jumps to the very end of the else branch
            jumpHere(finalEnd)
        } else {
            jumpHere(end)
        }
    }

    override fun visitIfTruthyElse(expression: Node, elseExpression: Node) {
        val end = Jump()
        visit(expression)
        addOperation(OpCode.JUMP_IF_TRUE, end)
        visit(elseExpression)
        jumpHere(end)
    }

    override fun visitIfNotNullThen(condition: Node, thenBranch: Node) {
        val end = Jump()
        visit(condition)
        addOperation(OpCode.JUMP_IF_NULL, end)
        addOperation(OpCode.POP_TOP)
        visit(thenBranch)
        jumpHere(end)
    }

    override fun visitForDo(name: String, iterable: Node, body: Node) {
        val start = Jump()
        val end = Jump()
        visit(iterable)
        addOperation(OpCode.POP_GET_ITER)
        jumpHere(start)
        addOperation(OpCode.ITER_NEXT, end)
        addOperation(OpCode.STORE_NAME, name)
        addNewJumpPair(start, end)
        visit(body)
        addJumpOperation(start)
        jumpHere(end)
        addOperation(OpCode.POP_TOP)
    }

    override fun visitWhileDo(condition: Node, body: Node) {
        val start = jumpHere(Jump())
        val end = Jump()
        addNewJumpPair(start, end)

        visit(condition)
        addOperation(OpCode.POP_JUMP_IF_FALSE, end)


        visit(body)
        addJumpOperation(start)
        jumpHere(end)
    }

    override fun visitReturn(value: Node) {
        if (!scopeStack.peek().isFunction) cantCompile("return outside of function")
        visit(value)
        addOperation(OpCode.RETURN_VALUE)
    }

    override fun visitBreak() {
        if (scopeStack.peek().jumpStack.isNotEmpty()) {
            addJumpOperation(scopeStack.peek().jumpStack.peek().second)
        } else cantCompile("break outside of loop")
    }

    override fun visitContinue() {
        if (scopeStack.peek().jumpStack.isNotEmpty()) {
            addJumpOperation(scopeStack.peek().jumpStack.peek().first)
        } else cantCompile("continue outside of loop")
    }

    override fun visitLazyNameAssignment(name: String, expression: Node) {
        visit(expression)
        addOperation(OpCode.STORE_NAME_LAZY, name)
    }

    override fun visitNameAssignment(name: String, expression: Node, isConstant: Boolean) {
        visit(expression)
        addOperation(if (isConstant) OpCode.STORE_CONST else OpCode.STORE_NAME, name)
    }

    override fun visitSetAtIndex(obj: Node, index: Node, value: Node) {
        visit(value)
        visit(index)
        visit(obj)
        addOperation(OpCode.STORE_INDEX)
    }

    override fun visitSetAttribute(obj: Node, name: String, value: Node) {
        visit(value)
        visit(obj)
        addOperation(OpCode.STORE_ATTR, name)
    }
}