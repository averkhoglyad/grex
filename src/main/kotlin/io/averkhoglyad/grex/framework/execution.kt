package io.averkhoglyad.grex.framework

import java.util.*

fun <H : Hero<S>, S> Program.execute(board: Board<H, S>) {
    this.asIterator(board).forEach { board.applyAct(it) }
}

fun <H : Hero<S>, S> Program.asIterator(board: Board<H, S>): Iterator<BoardAct<H, S>> {
    return Execution(board, this.code)
}

private class Execution<H : Hero<S>, S>(board: Board<H, S>, code: List<CodePoint>) : Iterator<BoardAct<H, S>> {

    var i = 0
    val frames = ArrayDeque<ExecutionFrame>()
    val code: List<CodePoint>

    init {
        var endPoint = code.last().takeIf { it is EmptyPoint }
        if (endPoint == null) {
            endPoint = EmptyPoint()
            this.code = code + endPoint
        } else {
            this.code = code
        }
        frames += ExecutionFrame(board, endPoint)
    }

    override fun hasNext(): Boolean {
        return i in code.indices
    }

    override fun next(): BoardAct<H, S> {
        val cp: CodePoint = code[i++]
        when (cp) {
            is BoardModify<*, *> -> {
                return (cp as BoardModify<H, S>).exec(frames.last)
            }
            is ExecPoint -> {
                cp.exec(frames.last)
            }
            is GoTo -> goTo(cp.point)
            is ConditionalGoTo -> {
                if (cp.condition.test(frames.last)) {
                    goTo(cp.point)
                }
            }
            is Invoke -> {
                goTo(cp.invokePoint)
                frames.addLast(frames.last.child(cp.returnPoint))
            }
            is Return -> goTo(frames.pollLast().returnPoint)
            is EmptyPoint -> {}

            else -> throw IllegalStateException("Unexpected CodePoint $cp")
        }
        return BoardAct.NoAct()
    }

    private fun goTo(point: CodePoint) {
        val target = code.indexOf(point) 
        if (target < 0) {
            throw ExecutionException("Unreachable pointer")
        }
        i = target
    }

}

class ExecutionFrame private constructor(val board: Board<*, *>, val returnPoint: CodePoint, private val parent: ExecutionFrame?) {

    constructor(board: Board<*, *>, returnLabel: CodePoint) : this(board, returnLabel, null)

//    private val procedures: MutableMap<String, Iterable<CodePoint>> = mutableMapOf()
    private val variables: MutableMap<String, Int> = mutableMapOf()

    fun child(returnPoint: CodePoint): ExecutionFrame = ExecutionFrame(board, returnPoint, this)

//    fun addProcedure(name: String, cmd: Iterable<CodePoint>) {
//        procedures[name] = cmd
//    }
//
//    fun getProcedure(name: String): Iterable<CodePoint>? {
//        return procedures[name] ?: parent?.getProcedure(name)
//    }

    fun getVariable(name: String): Int? {
        return variables[name] ?: parent?.getVariable(name)
    }

    fun setVariable(name: String, value: Int) {
        variables[name] = value
    }

}

sealed class CodePoint

class EmptyPoint() : CodePoint() // Usually used as GoTo target point

class GoTo(val point: CodePoint) : CodePoint()

class ConditionalGoTo(val condition: Condition<ExecutionFrame>, val point: CodePoint) : CodePoint()

class ExecPoint(val exec: ExecutionFrame.() -> Unit) : CodePoint()

class BoardModify<H : Hero<S>, S>(val exec: ExecutionFrame.() -> BoardAct<H, S>) : CodePoint()

class Invoke(val invokePoint: CodePoint, val returnPoint: CodePoint) : CodePoint()

object Return : CodePoint()

interface Condition<in O> {
    fun test(arg: O): Boolean
    fun invert(): Condition<in O> = object : Condition<O> {
        override fun test(arg: O) = !this@Condition.test(arg)
    }
}
