package compile

import objects.base.FlamingoCompilerErrorObject

abstract class AbstractParser(protected val lexer: Lexer) {
    protected var next: Token
    protected var current: Token
    private var last: Token
    private var scoped = 0

    init {
        current = lexer.nextToken()
        last = current
        next = lexer.nextToken()
        if (current.type == TokenType.TOKEN_END_LINE) advance()
    }

    fun match(type: TokenType): Boolean {
        if (current.type === type) {
            advance()
            return true
        }
        return false
    }

    @Throws(CompilerEscape::class)
    open fun cantParse(message: String, token: Token? = null) {
        throw CompilerEscape(FlamingoCompilerErrorObject(message, token ?: current))
    }

    fun peekNext(type: TokenType): Boolean {
        return next.type === type
    }

    fun eat(type: TokenType, message: String): Token {
        val t = current
        if (!match(type)) cantParse(message, t)
        return t
    }

    fun advance() {
        do {
            last = current
            current = next
            next = lexer.nextToken()
        } while (current.type == TokenType.TOKEN_END_LINE)
    }

    private fun atEndOfStatement() = current.type == TokenType.TOKEN_SEMICOLON || current.type == TokenType.TOKEN_EOF


    fun endStatement() {
        if (last.type == TokenType.TOKEN_END_LINE) return
        else if (scoped > 0 && current.type == TokenType.TOKEN_RIGHT_BRACE) return
        else if (atEndOfStatement()) advance()
        else cantParse("expected end of statement, not: '%s'".format(reveal(current.lexeme)))
    }

    fun finished() = current.type == TokenType.TOKEN_EOF

    fun assertFinished() {
        if (!finished()) cantParse("expected end of file")
    }

    fun scoped(parser: () -> Node): Node {
        scoped++
        val node = parser()
        scoped--
        return node
    }
}

class Parser(lexer: Lexer) : AbstractParser(lexer) {
    fun statements(end: TokenType, firstToken: Token? = null): Node {
        val first = firstToken ?: current
        val nodes = ArrayList<Node>()
        while (!finished() && current.type != end) {
            nodes.add(statement())
        }
        eat(end, "expected EOF")
        return if (nodes.size == 1) nodes[0] else NodeCollection(first, nodes)
    }

    private fun parseDefaults(
        positionals: List<String>,
        defaults: MutableList<String>,
        defaultValues: MutableList<Node>,
        varkwargs: StringBuilder
    ): Boolean {
        if (peekNext(TokenType.TOKEN_EQUAL)) {
            do {
                val name = eat(TokenType.TOKEN_IDENTIFIER, "default parameter must start with a name")
                if (peekNext(TokenType.TOKEN_IDENTIFIER)) {
                    if (name.lexeme == "varkwarg") {
                        varkwargs.append(name.lexeme)
                        return true
                    }
                }

                if (defaults.contains(name.lexeme) || positionals.contains(name.lexeme)) cantParse(
                    "duplicate parameter name '%s'".format(name.lexeme), name
                )
                eat(TokenType.TOKEN_EQUAL, "parameter name must be followed by '='")
                defaults.add(name.lexeme)
                defaultValues.add(expression())
                if (!match(TokenType.TOKEN_COMMA)) break
            } while (!finished())
            return true
        }
        return false
    }

    fun eatLexeme(type: TokenType, lexeme: String, message: String) {
        val token = eat(type, message)
        if (token.lexeme != lexeme) cantParse(message, token)
    }

    private fun parseFunctionOrGenerator(head: Token, isGenerator: Boolean, name: String): BuildFunction {
        val positionals = mutableListOf<String>()
        val defaults = mutableListOf<String>()
        val defaultValues = mutableListOf<Node>()
        val varkwargs = StringBuilder()
        val varargs = StringBuilder()

        eat(TokenType.TOKEN_LEFT_PAREN, "function parameter declaration must be enclosed with '('")
        if (current.type != TokenType.TOKEN_RIGHT_PAREN) {
            do {
                if (parseDefaults(
                        positionals, defaults, defaultValues, varkwargs
                    ) || current.type == TokenType.TOKEN_RIGHT_PAREN
                ) break
                if (peekNext(TokenType.TOKEN_IDENTIFIER)) {
                    eatLexeme(TokenType.TOKEN_IDENTIFIER, "vararg", "vararg keyword expected")
                    varargs.append(eat(TokenType.TOKEN_IDENTIFIER, "vararg name expected").lexeme)
                    break
                }
                positionals.add(eat(TokenType.TOKEN_IDENTIFIER, "function parameters must have a name").lexeme)
            } while (match(TokenType.TOKEN_COMMA))
        }
        eat(TokenType.TOKEN_RIGHT_PAREN, "function parameter declaration must be enclosed with ')'")
        val body = if (match(TokenType.TOKEN_EQUAL) && !isGenerator) {
            val expression = expression()
            Return(expression.token, expression)
        } else {
            scoped(this::statement)
        }
        return BuildFunction(
            head,
            isGenerator,
            name,
            positionals,
            defaults,
            defaultValues,
            if (varargs.isNotEmpty()) varargs.toString() else null,
            if (varkwargs.isNotEmpty()) varkwargs.toString() else null,
            body
        )
    }

    fun unpackDecorators(decorators: MutableList<Node>, function: BuildFunction): NameAssignment {
        val firstDecorator = decorators.removeLast()
        var decorated = CallObject(firstDecorator.token, firstDecorator, mutableListOf(function), listOf(), listOf())

        while (decorators.isNotEmpty()) {
            val nextDecorator = decorators.removeLast()
            decorated = CallObject(nextDecorator.token, nextDecorator, mutableListOf(decorated), listOf(), listOf())
        }

        return NameAssignment(firstDecorator.token, function.name, decorated, false)
    }

    private fun statement(): Node {
        val first = current
        return when (first.type) {
            TokenType.TOKEN_LEFT_BRACE -> {
                advance()
                scoped { statements(TokenType.TOKEN_RIGHT_BRACE, first) }
            }

            TokenType.TOKEN_WHILE -> {
                advance()

                eat(TokenType.TOKEN_LEFT_PAREN, "while condition must be enclosed in '('")
                val condition = expression()
                eat(TokenType.TOKEN_RIGHT_PAREN, "while condition must be enclosed with ')'")

                WhileDo(first, condition, statement())
            }

            TokenType.TOKEN_FOR -> {
                advance()
                eat(TokenType.TOKEN_LEFT_PAREN, "for loop must be enclosed with '('")
                val name = eat(
                    TokenType.TOKEN_IDENTIFIER, "for loop must have a name for the current iterated variable"
                ).lexeme
                eat(TokenType.TOKEN_IN, "iterated variable must be followed by 'in'")
                val iterable = expression()
                eat(TokenType.TOKEN_RIGHT_PAREN, "for loop must be enclosed with ')'")
                val body = statement()

                ForDo(first, name, iterable, body)
            }

            TokenType.TOKEN_IF -> {
                advance()

                eat(TokenType.TOKEN_LEFT_PAREN, "if condition must be enclosed with '('")
                val condition = expression()
                eat(TokenType.TOKEN_RIGHT_PAREN, "if condition must be enclosed with ')'")

                val thenBranch = statement()
                val elseBranch = if (match(TokenType.TOKEN_ELSE)) statement() else null

                IfThenElse(first, condition, thenBranch, elseBranch)
            }

            TokenType.TOKEN_AT -> {
                advance()

                val decorators = mutableListOf(expression())
                while (true) {
                    val operator = current
                    if (match(TokenType.TOKEN_AT)) {
                        decorators.add(expression())
                    } else if (match(TokenType.TOKEN_GEN)) {
                        eat(TokenType.TOKEN_FUN, "'gen' keyword is a modifier for functions and so must proceed 'fun'")
                        val name = eat(TokenType.TOKEN_IDENTIFIER, "functions must have a name").lexeme
                        return unpackDecorators(decorators, parseFunctionOrGenerator(operator, true, name))
                    } else if (match(TokenType.TOKEN_FUN)) {
                        val name = eat(TokenType.TOKEN_IDENTIFIER, "functions must have a name").lexeme
                        return unpackDecorators(decorators, parseFunctionOrGenerator(operator, false, name))
                    } else break
                }

                cantParse("decorator must decorate a function") as Node
            }

            TokenType.TOKEN_CONTINUE -> {
                advance()
                Continue(first)
            }

            TokenType.TOKEN_TRY -> {
                advance()
                tryLike(first, this::statement)
            }

            TokenType.TOKEN_BREAK -> {
                advance()
                Break(first)
            }

            TokenType.TOKEN_RETURN -> {
                advance()
                val expression = expression()
                Return(expression.token, expression)
            }

            TokenType.TOKEN_GEN, TokenType.TOKEN_FUN -> {
                advance()
                val isGenerator = if (first.type == TokenType.TOKEN_GEN) {
                    eat(TokenType.TOKEN_FUN, "'gen' keyword is a modifier for functions and so must proceed 'fun'")
                    true
                } else false
                val isConstant = if (current.type == TokenType.TOKEN_VAL) {
                    advance()
                    true
                } else false

                val name = eat(TokenType.TOKEN_IDENTIFIER, "functions must have a name").lexeme

                NameAssignment(first, name, parseFunctionOrGenerator(first, isGenerator, name), isConstant)
            }

            TokenType.TOKEN_CLASS -> {
                advance()

                val name = eat(TokenType.TOKEN_IDENTIFIER, "classes must have a name").lexeme
                val isConstant = if (current.type == TokenType.TOKEN_VAL) {
                    advance()
                    true
                } else false
                val packages = mutableListOf<Node>()
                if (match(TokenType.TOKEN_LEFT_PAREN)) {
                    do {
                        packages.add(expression())
                    } while (match(TokenType.TOKEN_COMMA))
                    eat(TokenType.TOKEN_RIGHT_PAREN, "class packages must be enclosed with ')'")
                }
                val body = statement()

                NameAssignment(first, name, BuildClass(first, name, packages, body), isConstant)
            }

            TokenType.TOKEN_VAR, TokenType.TOKEN_VAL -> {
                advance()

                val isConstant = first.type == TokenType.TOKEN_VAL
                val assignments = ArrayList<Node>()
                do {
                    if (match(TokenType.TOKEN_LEFT_PAREN)) {
                        val names = ArrayList<Token>()
                        do {
                            names.add(eat(TokenType.TOKEN_IDENTIFIER, "assignment name expected"))
                        } while (match(TokenType.TOKEN_COMMA))

                        eat(TokenType.TOKEN_RIGHT_PAREN, "multiple assignment must be enclosed with ')'")

                        if (match(TokenType.TOKEN_EQUAL)) {
                            val expression = expression()

                            for (name in names) {
                                assignments.add(NameAssignment(name, name.lexeme, expression, isConstant))
                            }
                        } else {
                            for (name in names) {
                                assignments.add(NameAssignment(name, name.lexeme, NullConstant(name), isConstant))
                            }
                        }
                    } else {
                        val name = eat(TokenType.TOKEN_IDENTIFIER, "assignment name expected")
                        if (match(TokenType.TOKEN_EQUAL)) {
                            assignments.add(NameAssignment(name, name.lexeme, expression(), isConstant))
                        } else {
                            assignments.add(NameAssignment(name, name.lexeme, NullConstant(name), isConstant))
                        }
                    }
                } while (match(TokenType.TOKEN_COMMA))
                endStatement()
                NodeCollection(first, assignments)
            }

            else -> {
                val expression = expression(asStatement = true)
                endStatement()
                expression
            }
        }
    }

    private fun expression(asStatement: Boolean = false): Node {
        var expression = precedence7()
        val post = current

        if (asStatement && match(TokenType.TOKEN_EQUAL)) {
            when (expression) {
                is Lookup -> return NameAssignment(post, expression.name, expression(), false)
                is GetAttribute -> return SetAttribute(post, expression.from, expression.name, expression())
                is IndexObject -> return SetAtIndex(post, expression.obj, expression.index, expression())
                else -> cantParse("expression is not assignable")
            }
        } else if (match(TokenType.TOKEN_DOT_DOT)) {
            expression = BuildRange(post, expression, expression())
        } else if (match(TokenType.TOKEN_QUESTION_COLON)) {
            expression = IfNotNullThen(post, expression, expression())
        } else if (match(TokenType.TOKEN_AS)) {
            expression = LazyNameAssignment(
                post,
                eat(TokenType.TOKEN_IDENTIFIER, "'as' expression must have a name for it to be assigned as").lexeme,
                expression()
            )
        }

        return if (asStatement) HangsValue(expression) else expression
    }

    private fun precedence7(): Node {
        var left = precedence6()
        while (true) {
            val operator = current
            left = if (match(TokenType.TOKEN_AND)) BinaryAnd(
                operator, left, precedence6()
            )
            else if (match(TokenType.TOKEN_OR)) BinaryOr(
                operator, left, precedence6()
            )
            else break
        }
        return left
    }

    private fun precedence6(): Node {
        var left = precedence5()
        while (true) {
            val operator = current
            left = if (match(TokenType.TOKEN_LESS)) BinaryOperation(
                operator, BinaryOperationType.COMP_LESS, left, precedence5()
            )
            else if (match(TokenType.TOKEN_GREATER)) BinaryOperation(
                operator, BinaryOperationType.COMP_GREATER, left, precedence5()
            )
            else if (match(TokenType.TOKEN_LESS_EQUAL)) BinaryOperation(
                operator, BinaryOperationType.COMP_LESS_EQUAL, left, precedence5()
            )
            else if (match(TokenType.TOKEN_GREATER_EQUAL)) BinaryOperation(
                operator, BinaryOperationType.COMP_GREATER_EQUAL, left, precedence5()
            )
            else if (match(TokenType.TOKEN_EQUAL_EQUAL)) BinaryOperation(
                operator, BinaryOperationType.COMP_EQUAL, left, precedence5()
            )
            else if (match(TokenType.TOKEN_BANG_EQUAL)) BinaryOperation(
                operator, BinaryOperationType.COMP_NOT_EQUAL, left, precedence5()
            )
            else break
        }
        return left
    }

    private fun precedence5(): Node {
        var left = precedence4()
        while (true) {
            val operator = current
            left = if (match(TokenType.TOKEN_IS)) {
                if (match(TokenType.TOKEN_NOT)) BinaryOperation(
                    operator, BinaryOperationType.COMP_IS_NOT, left, precedence4()
                )
                else BinaryOperation(operator, BinaryOperationType.COMP_IS, left, precedence4())
            } else break
        }
        return left
    }

    private fun precedence4(): Node {
        var left = precedence3()
        while (true) {
            val operator = current
            left = if (match(TokenType.TOKEN_PLUS)) BinaryOperation(
                operator, BinaryOperationType.ADD, left, precedence3()
            )
            else if (match(TokenType.TOKEN_MINUS)) BinaryOperation(
                operator, BinaryOperationType.SUB, left, precedence3()
            )
            else break
        }
        return left
    }

    private fun precedence3(): Node {
        var left = precedence2()
        while (true) {
            val operator = current
            left = if (match(TokenType.TOKEN_STAR)) BinaryOperation(
                operator, BinaryOperationType.MUL, left, precedence2()
            )
            else if (match(TokenType.TOKEN_SLASH)) BinaryOperation(
                operator, BinaryOperationType.DIV, left, precedence2()
            )
            else if (match(TokenType.TOKEN_PERCENT)) BinaryOperation(
                operator, BinaryOperationType.MOD, left, precedence2()
            )
            else break
        }
        return left
    }

    private fun precedence2(): Node {
        var left = precedence1()
        while (true) {
            val operator = current
            left = if (match(TokenType.TOKEN_CARET)) BinaryOperation(
                operator, BinaryOperationType.POW, left, atom()
            )
            else break
        }
        return left
    }

    private fun parseKeywords(keywords: MutableList<String>, keywordValues: MutableList<Node>): Boolean {
        if (peekNext(TokenType.TOKEN_EQUAL)) {
            do {
                val name = eat(TokenType.TOKEN_IDENTIFIER, "keyword arguments must start with a name")
                if (keywords.contains(name.lexeme)) cantParse(
                    "duplicate argument name '%s'".format(name.lexeme), name
                )
                eat(TokenType.TOKEN_EQUAL, "argument keyword must be followed by '='")
                keywords.add(name.lexeme)
                keywordValues.add(expression())
                if (!match(TokenType.TOKEN_COMMA)) break
            } while (!finished())
            return true
        }
        return false
    }

    private fun operationals(start: Node): Node {
        var left = start

        while (true) {
            val operator = current
            if (match(TokenType.TOKEN_DOT)) left = GetAttribute(
                current, eat(TokenType.TOKEN_IDENTIFIER, "attribute must be a name").lexeme, left
            )
            else if (match(TokenType.TOKEN_QUESTION_DOT)) {
                val name = eat(TokenType.TOKEN_IDENTIFIER, "attribute must be a name").lexeme
                if (match(TokenType.TOKEN_LEFT_PAREN)) {
                    val arguments = ArrayList<Node>()
                    val keywords = mutableListOf<String>()
                    val keywordValues = mutableListOf<Node>()

                    do {
                        if (parseKeywords(keywords, keywordValues) || current.type == TokenType.TOKEN_RIGHT_PAREN) break
                        val expression = expression()
                        arguments.add(expression)
                    } while (match(TokenType.TOKEN_COMMA))

                    eat(TokenType.TOKEN_RIGHT_PAREN, "attribute call arguments must be enclosed with ')'")
                    left = CallAttributeIfNotNull(operator, left, name, arguments, keywords, keywordValues)
                } else if (current.type == TokenType.TOKEN_LEFT_BRACE) {
                    left = CallAttributeIfNotNull(
                        operator,
                        left,
                        name,
                        mutableListOf(codeSnippet(operator)),
                        listOf(),
                        listOf()
                    )
                } else {
                    left = GetAttribute(operator, name, left, ifObjectNotNull = true)
                }
            } else if (current.type == TokenType.TOKEN_LEFT_BRACE) {
                if (left is CallObject) {
                    left.arguments.add(codeSnippet(operator))
                } else {
                    left = CallObject(operator, left, arrayListOf(codeSnippet(operator)), listOf(), listOf())
                }
            } else if (match(TokenType.TOKEN_LEFT_PAREN)) {
                val arguments = ArrayList<Node>()
                val keywords = mutableListOf<String>()
                val keywordValues = mutableListOf<Node>()

                do {
                    if (parseKeywords(keywords, keywordValues) || current.type == TokenType.TOKEN_RIGHT_PAREN) break
                    val expression = expression()
                    arguments.add(expression)
                } while (match(TokenType.TOKEN_COMMA))

                eat(TokenType.TOKEN_RIGHT_PAREN, "object call arguments must be enclosed with ')'")

                left = CallObject(operator, left, arguments, keywords, keywordValues)

            } else if (match(TokenType.TOKEN_LEFT_BRACKET)) {
                val index = expression()
                eat(TokenType.TOKEN_RIGHT_BRACKET, "object index must be enclosed with ']'")
                left = IndexObject(operator, left, index)
            } else break
        }

        return left
    }

    private fun precedence1(): Node {
        val first = current
        return if (match(TokenType.TOKEN_NOT)) UnaryOperation(first, UnaryOperationType.NOT, precedence1())
        else if (match(TokenType.TOKEN_MINUS)) UnaryOperation(first, UnaryOperationType.MINUS, precedence1())
        else if (match(TokenType.TOKEN_PLUS)) UnaryOperation(first, UnaryOperationType.PLUS, precedence1())
        else operationals(atom())
    }

    private fun codeSnippet(token: Token): Node {
        var codeSnippet = statement()
        if (codeSnippet is HangsValue) codeSnippet = Return(token, codeSnippet.node)
        return CodeSnippet(token, codeSnippet)
    }

    private fun parseString(stringToken: Token): Node {
        val string = StringBuilder()
        val parts = ArrayList<Node>()
        var i = 1

        while (i < stringToken.lexeme.length - 1) {
            if (stringToken.lexeme[i] == '\\') {
                i++
                if (i >= stringToken.lexeme.length - 1) cantParse("unescaped escape character", stringToken)
                when (stringToken.lexeme[i]) {
                    'n' -> string.append('\n')
                    't' -> string.append('\t')
                    'r' -> string.append('\r')
                    '\\' -> string.append('\\')
                    '"' -> string.append('"')
                    '{' -> string.append('{')

                    else -> cantParse("unrecognized escape character '%c'".format(stringToken.lexeme[i]), stringToken)
                }
                i++
            } else if (stringToken.lexeme[i] == '{') {
                i++
                if (string.isNotEmpty()) {
                    parts.add(StringLiteral(stringToken, string.toString()))
                    string.clear()
                }
                val formatString = StringBuilder()
                while (i < stringToken.lexeme.length - 1) {
                    if (stringToken.lexeme[i] == '}') break
                    else formatString.append(stringToken.lexeme[i])
                    i++
                }

                if (i >= stringToken.lexeme.length - 1 || stringToken.lexeme[i] != '}') cantParse(
                    "unclosed format string", stringToken
                )
                i++

                parts.add(
                    Parser(
                        Lexer(
                            "string at %d:%d in %s".format(
                                stringToken.lineStart + 1,
                                stringToken.columnStart + 1,
                                lexer.name,
                            ), formatString.toString()
                        )
                    ).expression()
                )
            } else {
                string.append(stringToken.lexeme[i])
                i++
            }
        }

        if (parts.isNotEmpty()) {
            if (string.isNotEmpty()) parts.add(StringLiteral(stringToken, string.toString()))
            return BuildString(stringToken, parts)
        } else {
            return StringLiteral(stringToken, string.toString())
        }
    }

    private fun atom(): Node {
        val atom = current

        // cheeky interception for anonymous lambdas which knows that it will be handled by statement call
        if (atom.type == TokenType.TOKEN_LEFT_BRACE) {
            return codeSnippet(atom)
        }

        advance()
        return when (atom.type) {
            TokenType.TOKEN_LEFT_PAREN -> {
                val expression = expression()
                if (!match(TokenType.TOKEN_COMMA)) {
                    eat(TokenType.TOKEN_RIGHT_PAREN, "unambiguous expression must be enclosed with ')'")
                    expression
                } else {
                    val items = mutableListOf(expression)
                    if (current.type != TokenType.TOKEN_RIGHT_PAREN) {
                        do {
                            items.add(expression())
                        } while (match(TokenType.TOKEN_COMMA))
                    }
                    eat(TokenType.TOKEN_RIGHT_PAREN, "array must be enclosed with ')'")
                    ArrayConstruct(atom, items)
                }
            }

            TokenType.TOKEN_IDENTIFIER -> {
                Lookup(atom, atom.lexeme)
            }

            TokenType.TOKEN_OPEN_PIPE -> {
                cantParse("deprecated array syntax")

                val items = ArrayList<Node>()

                do {
                    if (current.type == TokenType.TOKEN_CLOSE_PIPE || finished()) break
                    items.add(expression())
                } while (match(TokenType.TOKEN_COMMA))

                eat(TokenType.TOKEN_CLOSE_PIPE, "array definition must be enclosed with '|}'")

                return ArrayConstruct(atom, items)
            }

            TokenType.TOKEN_LEFT_BRACKET -> {
                val items = ArrayList<Node>()

                do {
                    if (current.type == TokenType.TOKEN_RIGHT_BRACKET || finished()) break
                    items.add(expression())
                } while (match(TokenType.TOKEN_COMMA))

                eat(TokenType.TOKEN_RIGHT_BRACKET, "list definition must be enclosed with ']'")

                return ListConstruct(atom, items)
            }


            TokenType.TOKEN_STRING -> parseString(atom)
            TokenType.TOKEN_RAW_STRING -> StringLiteral(atom, atom.lexeme.substring(2, atom.lexeme.length - 1))

            TokenType.TOKEN_NUMBER -> NumberLiteral(atom, atom.lexeme.toDouble())

            TokenType.TOKEN_FALSE -> FalseConstant(atom)
            TokenType.TOKEN_FUN, TokenType.TOKEN_GEN -> {
                val isGenerator = if (atom.type == TokenType.TOKEN_GEN) {
                    eat(TokenType.TOKEN_FUN, "'gen' keyword is a modifier for functions and so must proceed 'fun'")
                    true
                } else false

                var name = "anonymous-function"
                if (current.type == TokenType.TOKEN_IDENTIFIER) {
                    name = current.lexeme
                    advance()
                }

                parseFunctionOrGenerator(atom, isGenerator, name)
            }

            TokenType.TOKEN_NULL -> NullConstant(atom)

            TokenType.TOKEN_TRUE -> TrueConstant(atom)

            TokenType.TOKEN_SELF -> ContextObject(atom)

            TokenType.TOKEN_IF -> {

                eat(TokenType.TOKEN_LEFT_PAREN, "if condition must be enclosed with '('")
                val condition = expression()
                eat(TokenType.TOKEN_RIGHT_PAREN, "if condition must be enclosed with ')'")
                val expression = expression()
                eat(TokenType.TOKEN_ELSE, "'if' as an expression must be coupled with an else expression")
                val elseBranch = expression()
                IfThenElse(atom, condition, expression, elseBranch)
            }

            TokenType.TOKEN_TRY -> {
                tryLike(atom, this::expression)
            }

            TokenType.TOKEN_AT -> {
                val callExpression = expression()
                val callArgument = expression()

                CallObject(atom, callExpression, mutableListOf(callArgument), listOf(), listOf())
            }

            else -> {
                cantParse("expected an expression, got ${atom.type.name}", atom) as Node
            }
        }
    }

    private fun tryLike(first: Token, parser: () -> Node): TryCatch {
        val tryNode = parser()
        eat(TokenType.TOKEN_CATCH, "try must be coupled with a catch")
        val catchAs: String? =
            if (match(TokenType.TOKEN_AS)) eat(TokenType.TOKEN_IDENTIFIER, "catch as requires a name").lexeme else null
        val catchNode = parser()

        return TryCatch(first, tryNode, catchAs, catchNode)
    }
}
