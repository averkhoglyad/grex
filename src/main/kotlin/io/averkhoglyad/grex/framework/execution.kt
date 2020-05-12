package io.averkhoglyad.grex.framework

interface Executor {

    fun execute(program: Program)

}

class InMemoryExecutorImpl<B : Board<H, S>, H : Hero<S>, S>(val board: B) : Executor {

    override fun execute(program: Program) {
        val frame = ExecutionFrame(board)
        execute(frame, program.code, program.procedures)
    }

    private fun execute(frame: ExecutionFrame<H, S>, code: List<CodePoint>, procedures: Map<String, List<CodePoint>>) {
        var i = 0
        while (i in code.indices) {
            val cp: CodePoint = code[i++]
            when (cp) {
                is ExecCodePoint<*, *> -> {
                    (cp as ExecCodePoint<H, S>).exec(frame)
                }
                is BoardModify<*, *> -> {
                    val act: BoardAct<H, S> = (cp as BoardModify<H, S>).exec(frame)
                    board.applyAct(act)
                }
                is GoTo -> i = code.indexOf(cp.next)
                is Invoke -> {
                    val procedureCode = procedures[cp.name] ?: throw ExecutionException("Procedure ${cp.name} not defined")
                    execute(frame.child(), procedureCode, procedures)
                }
                is ConditionalGoTo<*, *> -> {
                    (cp as ConditionalGoTo<H, S>).let {
                        if (it.condition.test(frame)) {
                            i = code.indexOf(it.label)
                        }
                    }
                }
                is DoNothing -> {}
                else -> throw IllegalStateException("Unexpected CodePoint cp")
            }
        }
    }
}

class ExecutionFrame<H : Hero<S>, S> constructor(val board: Board<H, S>, private val parent: ExecutionFrame<H, S>? = null) {

    private val procedures: MutableMap<String, Iterable<CodePoint>> = mutableMapOf()
    private val variables: MutableMap<String, Int> = mutableMapOf()

    fun child(): ExecutionFrame<H, S> = ExecutionFrame(board, this)

    fun addProcedure(name: String, cmd: Iterable<CodePoint>) {
        procedures[name] = cmd
    }

    fun getProcedure(name: String): Iterable<CodePoint>? {
        return procedures[name] ?: parent?.getProcedure(name)
    }

    fun getVariable(name: String): Int? {
        return variables[name] ?: parent?.getVariable(name)
    }

    fun setVariable(name: String, value: Int) {
        variables[name] = value
    }

}

sealed class CodePoint

class DoNothing : CodePoint() // Usually used as GoTo target (a.k.a label)

data class GoTo(val next: CodePoint) : CodePoint()

data class ExecCodePoint<H : Hero<S>, S>(val exec: ExecutionFrame<H, S>.() -> Unit) : CodePoint()

data class BoardModify<H : Hero<S>, S>(val exec: ExecutionFrame<H, S>.() -> BoardAct<H, S>) : CodePoint()

data class Invoke(val name: String) : CodePoint()

data class ConditionalGoTo<H : Hero<S>, S>(val condition: Condition<ExecutionFrame<H, S>>, val label: CodePoint) : CodePoint()

interface Condition<in O> {
    fun test(arg: O): Boolean
    fun invert(): Condition<in O> = object : Condition<O> {
        override fun test(arg: O) = !this@Condition.test(arg)
    }
}
