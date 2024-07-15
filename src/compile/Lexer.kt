package compile

import objects.base.FlCompilerErrorObj
import objects.base.FlThrowableObj

enum class TokenType {
    // Single-character tokens.
    TOKEN_LEFT_PAREN, TOKEN_RIGHT_PAREN, TOKEN_LEFT_BRACE, TOKEN_RIGHT_BRACE, TOKEN_LEFT_BRACKET, TOKEN_RIGHT_BRACKET, TOKEN_COMMA, TOKEN_DOT, TOKEN_MINUS, TOKEN_PLUS, TOKEN_SEMICOLON, TOKEN_COLON, TOKEN_SLASH, TOKEN_STAR, TOKEN_CARET, TOKEN_PERCENT, TOKEN_AT,

    // One or two character tokens.
    TOKEN_BANG, TOKEN_BANG_EQUAL, TOKEN_EQUAL, TOKEN_EQUAL_EQUAL, TOKEN_GREATER, TOKEN_GREATER_EQUAL, TOKEN_LESS, TOKEN_LESS_EQUAL, TOKEN_OPEN_PIPE, TOKEN_CLOSE_PIPE, TOKEN_QUESTION_DOT, TOKEN_QUESTION_COLON, TOKEN_DOT_DOT,

    // Literals.
    TOKEN_IDENTIFIER, TOKEN_STRING, TOKEN_NUMBER, TOKEN_ATOMIC_NUM, TOKEN_RAW_STRING,

    // Keywords.
    TOKEN_AND, TOKEN_ELSE, TOKEN_FALSE, TOKEN_FOR, TOKEN_FUN, TOKEN_IF, TOKEN_NULL, TOKEN_OR, TOKEN_RETURN, TOKEN_TRUE, TOKEN_VAR, TOKEN_VAL, TOKEN_WHILE, TOKEN_END_LINE, TOKEN_TRY, TOKEN_CATCH, TOKEN_RIGHT_ARROW, TOKEN_CONTINUE, TOKEN_GEN, TOKEN_IS, TOKEN_NOT, TOKEN_BREAK, TOKEN_AS, TOKEN_CLASS, TOKEN_IN, TOKEN_SELF, TOKEN_SUPER, TOKEN_IMPORT,

    // other
    TOKEN_SOF, TOKEN_EOF, TOKEN_ERROR, TOKEN_COMMENT,
}

abstract class AbstractLexer {
    abstract val name: String
    abstract val source: String
}

data class Token(
    val lexer: AbstractLexer,
    val lineStart: Int,
    val lineEnd: Int,
    val columnStart: Int,
    val columnEnd: Int,
    val lexeme: String,
    val type: TokenType
) {
    fun lineString() = lexer.source.split('\n')[lineStart].trim(' ')
    fun underlineString() = " ".repeat(columnStart - trimLength()) + "^".repeat(lexeme.length)
    private fun trimLength(): Int {
        val lineString = lexer.source.split('\n')[lineStart]
        return lineString.length - lineString.trim(' ').length
    }
}


class Lexer(
    override val name: String,
    override val source: String,
    private var pos: Int = 0,
    private var lineStart: Int = 0
) : AbstractLexer() {
    private var lexemeStartPos = 0

    private var columnStart = 0
    private var lineEnd = lineStart
    private var columnEnd = 0

    private fun makeToken(type: TokenType) =
        Token(this, lineStart, lineEnd, columnStart, columnEnd, source.substring(lexemeStartPos, pos), type)


    private fun scanIdentifier(): Token {
        while (pos < source.length && (isIdentifier(peek()) || Character.isDigit(peek()))) {
            advance()
        }

        val identifier = source.substring(lexemeStartPos, pos)

        return when (identifier) {
            "if" -> makeToken(TokenType.TOKEN_IF)
            "else" -> makeToken(TokenType.TOKEN_ELSE)
            "true" -> makeToken(TokenType.TOKEN_TRUE)
            "false" -> makeToken(TokenType.TOKEN_FALSE)
            "null" -> makeToken(TokenType.TOKEN_NULL)
            "try" -> makeToken(TokenType.TOKEN_TRY)
            "catch" -> makeToken(TokenType.TOKEN_CATCH)
            "continue" -> makeToken(TokenType.TOKEN_CONTINUE)
            "class" -> makeToken(TokenType.TOKEN_CLASS)
            "break" -> makeToken(TokenType.TOKEN_BREAK)
            "gen" -> makeToken(TokenType.TOKEN_GEN)
            "return" -> makeToken(TokenType.TOKEN_RETURN)
            "fun" -> makeToken(TokenType.TOKEN_FUN)
            "and" -> makeToken(TokenType.TOKEN_AND)
            "or" -> makeToken(TokenType.TOKEN_OR)
            "for" -> makeToken(TokenType.TOKEN_FOR)
            "var" -> makeToken(TokenType.TOKEN_VAR)
            "val" -> makeToken(TokenType.TOKEN_VAL)
            "while" -> makeToken(TokenType.TOKEN_WHILE)
            "is" -> makeToken(TokenType.TOKEN_IS)
            "as" -> makeToken(TokenType.TOKEN_AS)
            "not" -> makeToken(TokenType.TOKEN_NOT)
            "in" -> makeToken(TokenType.TOKEN_IN)
            "self" -> makeToken(TokenType.TOKEN_SELF)
            "super" -> makeToken(TokenType.TOKEN_SUPER)
            "import" -> makeToken(TokenType.TOKEN_IMPORT)
            else -> makeToken(TokenType.TOKEN_IDENTIFIER)
        }
    }

    private fun scanNumber(): Token {
        while (pos < source.length && Character.isDigit(peek())) {
            advance()
        }
        if (pos < source.length && (peek() == '.' && peekNext() != '.')) {
            do {
                advance()
            } while (pos < source.length && Character.isDigit(peek()))
        }
        return makeToken(if (match('a')) TokenType.TOKEN_ATOMIC_NUM else TokenType.TOKEN_NUMBER)
    }

    private fun scanString(type: TokenType): Token {
        while (!match('"')) {
            if (peek() == '\\' && peekNext() == '"') {
                advance()
            }
            advance()
        }
        return makeToken(type)
    }

    fun nextToken(): Token {
        skipSpace()

        lineStart = lineEnd
        columnStart = columnEnd

        if (isAtEnd) return makeToken(TokenType.TOKEN_EOF)

        lexemeStartPos = pos
        val ch = source[pos]

        advance()

        return when (ch) {
            '(' -> makeToken(TokenType.TOKEN_LEFT_PAREN)
            ')' -> makeToken(TokenType.TOKEN_RIGHT_PAREN)
            '{' -> makeToken(if (match('|')) TokenType.TOKEN_OPEN_PIPE else TokenType.TOKEN_LEFT_BRACE)
            '}' -> makeToken(TokenType.TOKEN_RIGHT_BRACE)
            '[' -> makeToken(TokenType.TOKEN_LEFT_BRACKET)
            ']' -> makeToken(TokenType.TOKEN_RIGHT_BRACKET)
            ',' -> makeToken(TokenType.TOKEN_COMMA)
            '.' -> makeToken(if (match('.')) TokenType.TOKEN_DOT_DOT else TokenType.TOKEN_DOT)
            '+' -> makeToken(TokenType.TOKEN_PLUS)
            ';' -> makeToken(TokenType.TOKEN_SEMICOLON)
            '|' -> if (match('}')) makeToken(TokenType.TOKEN_CLOSE_PIPE)
            else {
                cantCompile("lonely pipe is not an operator")
            }

            '@' -> makeToken(TokenType.TOKEN_AT)
            '%' -> makeToken(TokenType.TOKEN_PERCENT)
            ':' -> makeToken(TokenType.TOKEN_COLON)
            '/' -> {
                if (peek() == '*') {
                    while (!isAtEnd) {
                        if (peek() == '*' && peekNext() == '/') {
                            advance()
                            advance()
                            break
                        }
                        advance()
                    }
                    makeToken(TokenType.TOKEN_COMMENT)
                } else {
                    makeToken(TokenType.TOKEN_SLASH)
                }
            }

            '*' -> makeToken(TokenType.TOKEN_STAR)
            '^' -> makeToken(TokenType.TOKEN_CARET)
            '?' -> if (match('.')) makeToken(TokenType.TOKEN_QUESTION_DOT)
            else if (match(':')) makeToken(TokenType.TOKEN_QUESTION_COLON)
            else {
                cantCompile("lonely question mark is not an operator")
            }

            '-' -> makeToken(if (match('>')) TokenType.TOKEN_RIGHT_ARROW else TokenType.TOKEN_MINUS)
            '!' -> if (match('=')) makeToken(TokenType.TOKEN_BANG_EQUAL)
            else if (match('"')) scanString(TokenType.TOKEN_RAW_STRING)
            else makeToken(TokenType.TOKEN_BANG)

            '=' -> makeToken(if (match('=')) TokenType.TOKEN_EQUAL_EQUAL else TokenType.TOKEN_EQUAL)
            '<' -> makeToken(if (match('=')) TokenType.TOKEN_LESS_EQUAL else TokenType.TOKEN_LESS)
            '>' -> makeToken(if (match('=')) TokenType.TOKEN_GREATER_EQUAL else TokenType.TOKEN_GREATER)
            '\n' -> {
                lineEnd++
                columnEnd = 0
                makeToken(TokenType.TOKEN_END_LINE)
            }

            '"' -> scanString(TokenType.TOKEN_STRING)

            else -> {
                if (isIdentifier(ch)) scanIdentifier() else if (Character.isDigit(ch)) scanNumber() else {
                    cantCompile("unexpected character '%c'".format(ch))
                }
            }
        }
    }

    private fun cantCompile(message: String): Token {
        val errorToken = makeToken(TokenType.TOKEN_ERROR)
        throw CompilerEscape(FlCompilerErrorObj(message, errorToken))
    }

    private fun advance() {
        pos++
        columnEnd++
    }

    private fun skipSpace() {
        while (pos < source.length) {
            if (match(' ') || match('\t')) continue
            if (peek() == '/' && peekNext() == '/') {
                while (peek() != '\n') advance()
                continue
            }
            break
        }
    }

    private val isAtEnd: Boolean
        get() = pos >= source.length

    private fun peek() = if (!isAtEnd) source[pos] else 0.toChar()
    private fun peekNext() = if (pos + 1 > source.length) 0.toChar() else source[pos + 1]

    private fun match(character: Char): Boolean {
        if (isAtEnd) return false
        if (peek() != character) return false
        advance()
        return true
    }
}

fun isIdentifier(ch: Char): Boolean {
    return ch == '_' || ch == '$' || Character.isAlphabetic(ch.code)
}

data class CompilerEscape(val error: FlThrowableObj) : Exception()

fun reveal(string: String) =
    string.replace("\u001B\\[([;\\d])*m".toRegex(), "\\\\[ESC;$1]").replace("\\n".toRegex(), "\\\\n")
        .replace("\\r".toRegex(), "\\\\r").replace("\\t".toRegex(), "\\\\t")