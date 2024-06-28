package runtime

import breakpoint
import compile.BinaryOperationType
import compile.Jump
import compile.Token
import compile.UnaryOperationType
import objects.base.*
import objects.base.callable.FlFunctionObj
import objects.base.collections.FlArrayObj
import objects.base.collections.FlListObj
import objects.base.collections.FlRangeObj
import objects.callable.*
import objects.libraries.MetaSentinel
import runtime.OpCode.*

/**
 * KDoc for opcode includes the opcode name and operands
 *
 * @property RETURN_VALUE
 * @property THROW_ERROR
 * @property POP_TOP
 * @property FINISH_TRY
 * @property STORE_INDEX
 * @property POP_GET_ITER
 * @property BUILD_RANGE
 * @property LOAD_CTX
 * @property LOAD_CTX_SUP
 *
 * @property LOAD_CONST (obj: FlObj)
 * @property JUMP_ABSOLUTE (to: Int)
 * @property LOAD_NAME (name: String)
 * @property JUMP_IF_TRUE (to: Jump)
 * @property JUMP_IF_FALSE (to: Jump)
 * @property STORE_NAME_LAZY (name: String)
 * @property STORE_NAME (name: String)
 * @property STORE_CONST (name: String)
 * @property BUILD_LIST (size: Int)
 * @property BUILD_ARRAY (size: Int)
 * @property GET_ATTR (name: String)
 * @property BUILD_STRING (size: Int)
 * @property SETUP_TRY (to: Jump)
 * @property STORE_ATTR (name: String)
 * @property BUILD_FUNCTION (partialFunction: PartialFunction)
 * @property ITER_NEXT (to: Jump)
 * @property BUILD_CODE (obj: FlCodeObj)
 * @property BINARY_OPERATION (type: BinaryOperationType)
 * @property UNARY_OPERATION (type: UnaryOperationType)
 *
 * @property CALL (arguments: Int, keywords: Int)
 * @property INDEX_TOP (arguments: Int, keywords: Int)
 * @property SETUP_TRY_AS (to: Jump, name: String?)
 * @property BUILD_CLASS (name: String, packages: Int, namespace: codeObj)
 *
 *
 * @property POP_FRAME ; for debugging purposes
 * @property BREAKPOINT ; for debugging purposes
 * @property ADD_FRAME (frame: Frame) ; for debugging purposes
 */
enum class OpCode {
    // take no operands
    RETURN_VALUE, THROW_ERROR, POP_TOP, FINISH_TRY, STORE_INDEX, POP_GET_ITER, BUILD_RANGE, LOAD_CTX, LOAD_CTX_SUP,

    // take 1 operand
    LOAD_CONST, JUMP_ABSOLUTE, LOAD_NAME, JUMP_IF_TRUE, JUMP_IF_FALSE, STORE_NAME_LAZY, STORE_NAME, STORE_CONST, BUILD_LIST, BUILD_ARRAY, GET_ATTR, POP_JUMP_IF_FALSE,
    BINARY_OPERATION, UNARY_OPERATION, JUMP_IF_NULL, BUILD_STRING, SETUP_TRY, STORE_ATTR, ITER_NEXT, BUILD_FUNCTION, BUILD_CODE,

    // take 2+ operands
    CALL, INDEX_TOP, SETUP_TRY_AS, BUILD_CLASS, CREATE_ATOM_NUM,

    // debugging
    POP_FRAME, BREAKPOINT, ADD_FRAME, // (frame: Frame)
}

/**
 * Operation which exists in the OperationalFrame's operation list. Each operation in the list should be executed
 * sequentially
 *
 * @param opCode the opcode of the operation which is referenced by the execute method
 * @param operands the operands of the operation which must be strictly tied to the OpCode
 */
open class Operation(val opCode: OpCode, val operands: Array<Any>) {
    /**
     * The agreement made when calling this method is that the frame passed must be the frame at the top of the stack
     * at the time.
     *
     * @param frame the instruction frame to manipulate whist executing
     */
    fun execute(frame: OperationalFrame) {
        when (opCode) {
            LOAD_CONST -> {
                frame.stack.add(operands[0] as FlObject)
            }

            JUMP_ABSOLUTE -> {
                // setting the frame ip to the jump - 1 because all non error executions increase ip
                frame.ip = (operands[0] as Jump).to - 1
            }

            RETURN_VALUE -> {
                // setting an error because this opcode should be handled by the Vm's call method
                throwObj(
                    "Operational frame tried to handle returning a value outside of the Vm's call method",
                    FatalError
                )
            }

            ADD_FRAME -> {
                addCall(operands[0] as Frame)
            }

            POP_FRAME -> {
                popCall()
            }

            BREAKPOINT -> {
                breakpoint()
            }

            THROW_ERROR -> {
                val thrown = frame.stack.pop()
                if (thrown is FlThrowableObj) throwObj(thrown)
                else throwObj("%s type object is not throwable".format(thrown.cls.name), TypeError)
            }

            POP_TOP -> {
                popObj()
            }

            LOAD_NAME -> {
                val value = frame.locals.get(operands[0] as String) ?: return
                addObj(value)
            }

            POP_JUMP_IF_FALSE -> {
                val value = frame.stack.pop()
                val truthy = value.truthy() ?: return
                if (!truthy) frame.ip = (operands[0] as Jump).to - 1
            }

            BINARY_OPERATION -> {
                val right = popObj()
                val left = popObj()

                when (operands[0] as BinaryOperationType) {
                    BinaryOperationType.ADD -> {
                        val result = left.add(right) ?: return
                        addObj(result)
                    }

                    BinaryOperationType.SUB -> {
                        val result = left.sub(right) ?: return
                        addObj(result)
                    }

                    BinaryOperationType.MUL -> {
                        val result = left.mul(right) ?: return
                        addObj(result)
                    }

                    BinaryOperationType.DIV -> {
                        val result = left.div(right) ?: return
                        addObj(result)
                    }

                    BinaryOperationType.POW -> {
                        val result = left.pow(right) ?: return
                        addObj(result)
                    }

                    BinaryOperationType.MOD -> {
                        val result = left.mod(right) ?: return
                        addObj(result)
                    }

                    BinaryOperationType.COMP_EQUAL -> {
                        val result = left.eq(right) ?: return
                        addObj(result)
                    }

                    BinaryOperationType.COMP_NOT_EQUAL -> {
                        val result = left.neq(right) ?: return
                        addObj(result)
                    }

                    BinaryOperationType.COMP_LESS -> {
                        val result = left.lt(right) ?: return
                        addObj(result)
                    }

                    BinaryOperationType.COMP_GREATER -> {
                        val result = left.gt(right) ?: return
                        addObj(result)
                    }

                    BinaryOperationType.COMP_LESS_EQUAL -> {
                        val result = left.lteq(right) ?: return
                        addObj(result)
                    }

                    BinaryOperationType.COMP_GREATER_EQUAL -> {
                        val result = left.gteq(right) ?: return
                        addObj(result)
                    }

                    BinaryOperationType.COMP_IS -> { addObj(booleanOf(left == right)) }
                    BinaryOperationType.COMP_IS_NOT -> addObj(booleanOf(left != right))
                }
            }

            JUMP_IF_TRUE -> {
                val truthy = topObj().truthy() ?: return
                if (truthy) frame.ip = (operands[0] as Jump).to - 1
            }

            JUMP_IF_FALSE -> {
                val truthy = topObj().truthy() ?: return
                if (!truthy) frame.ip = (operands[0] as Jump).to - 1
            }

            STORE_NAME_LAZY -> {
                frame.locals.set(operands[0] as String, topObj())
            }

            STORE_NAME -> {
                val obj = popObj()
                val name = operands[0] as String
                if (obj.getAttributeOrDefault(
                        "<flag:meta>",
                        Null
                    ) == MetaSentinel && frame.locals is ClassNameTable
                ) frame.locals.set("meta\$$name", obj)
                else frame.locals.set(name, obj)
            }

            STORE_CONST -> {
                val obj = popObj()
                val name = operands[0] as String
                if (obj.getAttributeOrDefault(
                        "<flag:meta>",
                        Null
                    ) == MetaSentinel && frame.locals is ClassNameTable
                ) frame.locals.set("meta\$$name", obj, constant = true)
                else frame.locals.set(name, obj, constant = true)
            }

            BUILD_LIST -> {
                val items = ArrayList<FlObject>()
                for (i in 0..<operands[0] as Int) items.addFirst(popObj())
                addObj(FlListObj(items))
            }

            BUILD_ARRAY -> {
                val items = ArrayList<FlObject>()
                for (i in 0..<operands[0] as Int) items.addFirst(popObj())
                addObj(FlArrayObj(items.toTypedArray()))
            }

            GET_ATTR -> {
                val attr = popObj().getAttribute(operands[0] as String)
                attr?.let { addObj(it) }
            }

            INDEX_TOP -> {
                val target = popObj()
                val index = popObj()
                target.index(index)?.let { addObj(it) }
            }

            CALL -> {
                val callSpec = operands[0] as CallSpec

                val arguments = ArrayList<FlObject>()
                val keywords = LinkedHashMap<String, FlObject>()

                for (i in 0..<callSpec.arguments) {
                    arguments.addFirst(popObj())
                }

                for (keyword in callSpec.keywords.reversed()) {
                    keywords[keyword] = popObj()
                }

                val target = popObj()

                if (target is FlFunctionObj) {
                    val locals = target.parameters.parseLocals(arguments, keywords.reversed()) ?: return
                    addCall(target.makeFrame(locals))
                } else {
                    val result = target.call(arguments, keywords.reversed())
                    if (result != null) addObj(result)
                }
            }

            JUMP_IF_NULL -> {
                if (topObj() == Null) frame.ip = (operands[0] as Jump).to - 1
            }

            BUILD_STRING -> {
                val string = StringBuilder()
                for (i in 0..<(operands[0] as Int)) {
                    val stringConcat = popObj().stringConcat()
                    stringConcat ?: return

                    string.insert(0, stringConcat)
                }
                addObj(stringOf(string.toString()))
            }

            SETUP_TRY -> {
                frame.errorJumpStack.add(Pair((operands[0] as Jump).to, null))
            }

            SETUP_TRY_AS -> {
                frame.errorJumpStack.add(Pair((operands[0] as Jump).to, (operands[1] as String)))
            }

            FINISH_TRY -> {
                frame.errorJumpStack.pop()
            }

            STORE_INDEX -> {
                val target = popObj()
                val index = popObj()
                val value = popObj()
                target.setAtIndex(index, value)
            }

            STORE_ATTR -> {
                val target = popObj()
                val value = popObj()
                target.setAttribute(operands[0] as String, value, constant = false)
            }

            POP_GET_ITER -> {
                val iterable = popObj().iter() ?: return
                addObj(iterable)
            }

            ITER_NEXT -> {
                val iterator = topObj()
                val hasNext = iterator.callAttributeAssertCast("hasNextObj", FlBooleanObj::class) ?: return
                if (hasNext.boolean) {
                    val next = iterator.callAttribute("nextObj") ?: return
                    addObj(next)
                } else {
                    frame.ip = (operands[0] as Jump).to - 1
                }
            }

            BUILD_FUNCTION -> {
                var defaults: HashMap<String, FlObject>? = null
                val codeObj = popObj() as FlCodeObj
                val partialFunction = operands[0] as PartialFunction
                if (partialFunction.defaults != null) {
                    defaults = LinkedHashMap()
                    for (keyword in partialFunction.defaults.reversed()) {
                        defaults[keyword] = popObj()
                    }
                }

                val parameterSpec = ParameterSpec(
                    codeObj.name,
                    partialFunction.positionals,
                    defaults,
                    partialFunction.varargs,
                    partialFunction.varkwargs
                )
                addObj(FlFunctionObj(codeObj, parameterSpec))
            }

            BUILD_RANGE -> {
                val to = popObj()
                val from = popObj()
                addObj(FlRangeObj(Pair(from, to)))
            }

            BUILD_CLASS -> {
                val name = operands[0] as String

                val codeObj = popObj() as FlCodeObj

                val classFrame = OperationalFrame(
                    "%s.<class '%s'>".format(frame.name, name), codeObj.operations
                )

                classFrame.locals = ClassNameTable(classFrame.name, codeObj.nativeClosure)

                val result = runtime.execute(classFrame)
                if (result.thrown != null) return
                val supers = mutableListOf<FlClass>()
                for (i in 0..<(operands[1] as Int)) {
                    val maybeClass = popObj()
                    if (maybeClass !is FlReflectObj) {
                        throwObj(
                            "%s type object must be a class to be a super class of %s".format(
                                maybeClass.cls.name,
                                name
                            ), TypeError
                        )
                        return
                    }
                    supers.add(maybeClass.reflectingClass)
                }

                val cls = createUserDefinedFlClass(name, supers, classFrame.locals) ?: return

                addObj(cls.reflectObj)
            }

            LOAD_CTX -> {
                val ctx = frame.locals.getContextObj() ?: return
                addObj(ctx)
            }

            BUILD_CODE -> {
                val code = operands[0] as PartialCodeObj
                addObj(FlCodeObj(code.scope.name, code.scope.operations, code.filePath, frame.locals))
            }

            UNARY_OPERATION -> {
                val obj = popObj()
                val type = operands[0] as UnaryOperationType
                when (type) {
                    UnaryOperationType.NOT -> {
                        val result = obj.callAttribute("meta\$not") ?: return
                        addObj(result)
                    }

                    UnaryOperationType.MINUS -> {
                        val result = obj.callAttribute("meta\$minus") ?: return
                        addObj(result)
                    }

                    UnaryOperationType.PLUS -> {
                        val result = obj.callAttribute("meta\$plus") ?: return
                        addObj(result)
                    }
                }
            }

            LOAD_CTX_SUP -> {
                val ctxSuper = frame.locals.getContextSuperObj() ?: return
                addObj(ctxSuper)
            }

            CREATE_ATOM_NUM -> {
                addObj(FlAtomicNumObj(operands[0] as Double))
            }
        }
    }
}

class CompiledOperation(val token: Token, opCode: OpCode, operands: Array<Any>) : Operation(opCode, operands)