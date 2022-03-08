package net.averkhoglyad.grex.framework.program

import net.averkhoglyad.grex.framework.board.Board
import net.averkhoglyad.grex.framework.code.*
import net.averkhoglyad.grex.framework.execution.Program
import java.util.*

fun program(board: Board, fn: TopProgramBuilder.() -> Unit): Program {

    val builder = TopProgramBuilderImpl()
    builder.fn()

    val (code, procedurePoints, procedureCodes) = builder.result()

    if (procedurePoints.isEmpty()) {
        return Program(board, code.toList())
    }

    val endPoint = EmptyPoint()
    code += GoTo(endPoint)

    procedurePoints.forEach { (name, point) ->
        val procedureCode = procedureCodes[name]
        requireNotNull(procedureCode) { "Procedure $name not defined" }
        code += point
        code += procedureCode
        code += Return
    }

    code += endPoint

    return Program(board, code.toList())
}

private fun subprogram(procedurePoints: MutableMap<String, CodePoint>, fn: ProgramBuilder.() -> Unit): List<CodePoint> {
    return ProgramBuilderImpl(procedurePoints).apply(fn).code
}

interface TopProgramBuilder : ProgramBuilder {
    fun define(name: String, fn: ProgramBuilder.() -> Unit)
}

interface ProgramBuilder {

    fun doIf(condition: Condition<ExecutionFrame>, trueFn: ProgramBuilder.() -> Unit): DoOtherwise
    fun repeatWhile(condition: Condition<ExecutionFrame>, fn: ProgramBuilder.() -> Unit)
    fun repeat(times: Int, fn: ProgramBuilder.() -> Unit)

    fun invoke(name: String)

    fun command(fn: (ExecutionFrame) -> Unit)

}

interface DoOtherwise {
    infix fun otherwise(falseFn: ProgramBuilder.() -> Unit)
}

private class TopProgramBuilderImpl : ProgramBuilderImpl(mutableMapOf()), TopProgramBuilder {

    private val procedureCodes = mutableMapOf<String, List<CodePoint>>()

    fun result() = Triple(code, procedurePoints, procedureCodes)

    override fun define(name: String, fn: ProgramBuilder.() -> Unit) {
        require(!procedureCodes.containsKey(name)) { "Procedure $name already defined"}
        procedureCodes[name] = subprogram(procedurePoints, fn)
    }
}

private open class ProgramBuilderImpl(val procedurePoints: MutableMap<String, CodePoint>) : ProgramBuilder {

    val code: MutableList<CodePoint> = mutableListOf()

    override fun command(fn: (ExecutionFrame) -> Unit) {
        code += CommandPoint(fn)
    }

    override fun invoke(name: String) {
        val invokePoint = procedurePoints.computeIfAbsent(name) { EmptyPoint() }
        val returnPoint = EmptyPoint()
        code += Invoke(invokePoint, returnPoint)
        code += returnPoint
    }

    override fun doIf(condition: Condition<ExecutionFrame>, trueFn: ProgramBuilder.() -> Unit): DoOtherwise {
        val falsePoint = EmptyPoint()
        code += ConditionalGoTo(condition.not(), falsePoint)
        code += subprogram(procedurePoints, trueFn)
        code += falsePoint
        return DoOtherwiseImpl()
    }

    private inner class DoOtherwiseImpl : DoOtherwise {
        override fun otherwise(falseFn: ProgramBuilder.() -> Unit) {
            val endPoint = EmptyPoint()
            code.add(code.lastIndex, GoTo(endPoint))
            code += subprogram(procedurePoints, falseFn)
            code += endPoint
        }
    }

    override fun repeatWhile(condition: Condition<ExecutionFrame>, fn: ProgramBuilder.() -> Unit) {
        val falsePoint = EmptyPoint()
        val ifCode = ConditionalGoTo(condition.not(), falsePoint)
        code += ifCode
        code += subprogram(procedurePoints, fn)
        code += GoTo(ifCode)
        code += falsePoint
    }

    override fun repeat(times: Int, fn: ProgramBuilder.() -> Unit) {
        val indexName = "\$_${UUID.randomUUID()}"
        code += InternalExecPoint {
            setVariable(indexName, 0)
        }

        val falsePoint = EmptyPoint()
        val condition = object : Condition<ExecutionFrame> {
            override fun test(arg: ExecutionFrame): Boolean = arg.getVariable(indexName)!! < times
        }
        val conditionPoint = ConditionalGoTo(condition.not(), falsePoint)
        code += conditionPoint

        code += InternalExecPoint {
            setVariable(indexName, getVariable(indexName)!! + 1)
        }

        val bodyCode = subprogram(procedurePoints, fn)

        code += bodyCode
        code += GoTo(conditionPoint)
        code += falsePoint
    }
}
