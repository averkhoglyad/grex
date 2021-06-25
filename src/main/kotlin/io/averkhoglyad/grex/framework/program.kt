package io.averkhoglyad.grex.framework

import java.util.*

fun program(fn: ProgramBuilder.() -> Unit): Program {

    val builder = ProgramBuilderImpl()
    builder.fn()

    val (code, procedurePoints, procedureCodes) = builder.result()

    if (procedurePoints.isEmpty()) {
        return Program(code.toList())
    }

    val endPoint = EmptyPoint()
    code += GoTo(endPoint)

    procedurePoints.forEach { (name, point) ->
        code += point
        code += procedureCodes[name] ?: throw IllegalStateException("Procedure $name not defined")
        code += Return
    }

    code += endPoint

    return Program(code)
}

private fun subprogram(procedurePoints: MutableMap<String, CodePoint>, fn: SubprogramBuilder.() -> Unit): List<CodePoint> {
    return SubprogramBuilderImpl(procedurePoints).apply(fn).code
}

private class ProgramBuilderImpl : SubprogramBuilderImpl(mutableMapOf()), ProgramBuilder {

    private val procedureCodes = mutableMapOf<String, List<CodePoint>>()

    fun result() = Triple(code, procedurePoints, procedureCodes)

    override fun define(name: String, fn: SubprogramBuilder.() -> Unit) {
        procedureCodes[name] = subprogram(procedurePoints, fn)
    }
}

private open class SubprogramBuilderImpl(val procedurePoints: MutableMap<String, CodePoint>) : SubprogramBuilder {

    val code: MutableList<CodePoint> = mutableListOf()

    override fun eval(fn: () -> Iterable<CodePoint>) {
        code += fn()
    }

    override fun invoke(name: String) {
        val invokePoint = procedurePoints.computeIfAbsent(name) { EmptyPoint() }
        val returnPoint = EmptyPoint()
        code += Invoke(invokePoint, returnPoint)
        code += returnPoint
    }

    override fun doIf(condition: Condition<ExecutionFrame>, trueFn: SubprogramBuilder.() -> Unit): DoOtherwise {
        val falsePoint = EmptyPoint()
        code += ConditionalGoTo(condition.invert(), falsePoint)
        code += subprogram(procedurePoints, trueFn)
        code += falsePoint

        return object : DoOtherwise {
            override fun otherwise(falseFn: SubprogramBuilder.() -> Unit) {
                val endPoint = EmptyPoint()
                code.add(code.lastIndex, GoTo(endPoint))
                code += subprogram(procedurePoints, falseFn)
                code += endPoint
            }
        }
    }

    override fun repeatWhile(condition: Condition<ExecutionFrame>, fn: SubprogramBuilder.() -> Unit) {
        val falsePoint = EmptyPoint()
        val ifCode = ConditionalGoTo(condition.invert(), falsePoint)
        code += ifCode
        code += subprogram(procedurePoints, fn)
        code += GoTo(ifCode)
        code += falsePoint
    }

    override fun repeat(times: Int, fn: SubprogramBuilder.() -> Unit) {
        val indexName = "\$_${UUID.randomUUID()}"
        code += ExecPoint {
            setVariable(indexName, 0)
        }

        val falsePoint = EmptyPoint()
        val condition = object : Condition<ExecutionFrame> {
            override fun test(frame: ExecutionFrame): Boolean = frame.getVariable(indexName)!! < times
        }
        val conditionPoint = ConditionalGoTo(condition.invert(), falsePoint)
        code += conditionPoint

        code += ExecPoint {
            setVariable(indexName, getVariable(indexName)!! + 1)
        }

        val bodyCode = subprogram(procedurePoints, fn)

        code += bodyCode
        code += GoTo(conditionPoint)
        code += falsePoint
    }
}

interface ProgramBuilder : SubprogramBuilder {
    fun define(name: String, fn: SubprogramBuilder.() -> Unit)
}

interface SubprogramBuilder {

    fun eval(fn: () -> Iterable<CodePoint>)
    fun invoke(name: String)

    fun doIf(condition: Condition<ExecutionFrame>, trueFn: SubprogramBuilder.() -> Unit): DoOtherwise
    fun repeatWhile(condition: Condition<ExecutionFrame>, fn: SubprogramBuilder.() -> Unit)
    fun repeat(times: Int, fn: SubprogramBuilder.() -> Unit)
}

interface DoOtherwise {
    infix fun otherwise(falseFn: SubprogramBuilder.() -> Unit)
}
