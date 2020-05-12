package io.averkhoglyad.grex.framework

import java.util.*

// Commands
open class ExecutionException(message: String? = null) : Exception(message)

interface Command {

    fun compile(): Iterable<CodePoint>

}

data class Program(val code: List<CodePoint>, val procedures: Map<String, List<CodePoint>>)

fun <H : Hero<S>, S> program(fn: ProgramBuilder<H, S>.() -> Unit): Program {
    val code = mutableListOf<CodePoint>()
    val procedures = mutableMapOf<String, List<CodePoint>>()
    val builder = object : ProgramBuilder<H, S> {

        override fun eval(cmd: Command) {
            code += cmd.compile()
        }

        override fun invoke(name: String) {
            code += Invoke(name)
        }

        override fun define(name: String, fn: SubprogramBuilder<H, S>.() -> Unit) {
            val subprogram = program(fn)
            procedures[name] = subprogram.code
        }

        override fun doIf(condition: Condition<ExecutionFrame<H, S>>, trueFn: SubprogramBuilder<H, S>.() -> Unit): DoOtherwise<H, S> {
            val falseLabel = DoNothing()
            val ifCode = ConditionalGoTo(condition.invert(), falseLabel)
            code += ifCode
            code += program(trueFn).code
            code += falseLabel

            return object : DoOtherwise<H, S> {
                override fun otherwise(falseFn: SubprogramBuilder<H, S>.() -> Unit) {
                    val endLabel = DoNothing()
                    code.add(code.size - 1, GoTo(endLabel))
                    code += program(falseFn).code
                    code += endLabel
                }
            }
        }

        override fun repeatWhile(condition: Condition<ExecutionFrame<H, S>>, fn: SubprogramBuilder<H, S>.() -> Unit) {
            val falseLabel = DoNothing()
            val ifCode = ConditionalGoTo(condition.invert(), falseLabel)
            code += ifCode
            code += program(fn).code
            code += GoTo(ifCode)
        }

        override fun repeat(times: Int, fn: SubprogramBuilder<H, S>.() -> Unit) {
            val indexName = "\$_${UUID.randomUUID()}"
            code += BoardModify<H, S> {
                setVariable(indexName, 0)
                return@BoardModify BoardAct.DoNothing<H, S>()
            }
            val condition = object : Condition<ExecutionFrame<H, S>> {
                override fun test(frame: ExecutionFrame<H, S>): Boolean = frame.getVariable(indexName)!! < times
            }
            val falseLabel = DoNothing()
            val ifCode = ConditionalGoTo(condition.invert(), falseLabel)
            code += ifCode
            code += BoardModify<H, S> {
                setVariable(indexName, getVariable(indexName)!! + 1)
                return@BoardModify BoardAct.DoNothing<H, S>()
            }
            code += program(fn).code
            code += GoTo(ifCode)
            code += falseLabel
        }
    }
    builder.fn()
    return Program(code, procedures)
}

interface ProgramBuilder<H : Hero<S>, S> : SubprogramBuilder<H, S> {

    fun define(name: String, fn: SubprogramBuilder<H, S>.() -> Unit)

}

interface SubprogramBuilder<H : Hero<S>, S> {

    fun eval(cmd: Command)
    fun invoke(name: String)
    
    fun doIf(condition: Condition<ExecutionFrame<H, S>>, trueFn: SubprogramBuilder<H, S>.() -> Unit): DoOtherwise<H, S>
    fun repeatWhile(condition: Condition<ExecutionFrame<H, S>>, fn: SubprogramBuilder<H, S>.() -> Unit)
    fun repeat(times: Int, fn: SubprogramBuilder<H, S>.() -> Unit)
}

interface DoOtherwise<H : Hero<S>, S> {
    infix fun otherwise(falseFn: SubprogramBuilder<H, S>.() -> Unit)
}

//internal val DO_NOTHING = object : Command<*, *> {
//    override fun exec(board: Board<*, *>) = emptyList<BoardAct>()
//}

//internal class CompoundCommand<H : Hero<S>, S>(val body: Iterable<Command<H, S>>) : Command<H, S> {
//
//    constructor(vararg commands: Command<H, S>) : this(commands.toList())
//
//    override fun exec(board: Board<H, S>): BoardAct {
////        body.forEach { it.execute(board) }
//        return TODO()
//    }
//
//}

//internal class IfCommand(private val condition: BoardCondition<Board<*, *>, *, *>, private val trueBlock: Command, private val falseBlock: Command) : Command {
//
//    override fun execute(board: Board<*, *>) {
//        if (condition.test(board)) {
//            trueBlock.execute(board)
//        } else {
//            falseBlock.execute(board)
//        }
//    }
//
//}
//
//internal class WhileCommand(private val condition: BoardCondition<Board<*, *>, *, *>, private val cmd: Command) : Command {
//
//    override fun execute(board: Board<*, *>) {
//        while (condition.test(board)) {
//            cmd.execute(board)
//        }
//    }
//
//}
