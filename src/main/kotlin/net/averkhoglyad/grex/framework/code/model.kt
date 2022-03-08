package net.averkhoglyad.grex.framework.code

import net.averkhoglyad.grex.framework.board.Board

interface Condition<in O> {
    fun test(arg: O): Boolean
    operator fun not(): Condition<in O> = object : Condition<O> {
        override fun test(arg: O) = !this@Condition.test(arg)
    }
}

interface ExecutionFrameCondition : Condition<ExecutionFrame>

interface ExecutionFrame {
    val board: Board
    fun getVariable(name: String): Int?
    fun setVariable(name: String, value: Int)
}

interface ExecutionFrameInternal : ExecutionFrame {
    val returnPoint: CodePoint
    fun child(returnPoint: CodePoint): ExecutionFrameInternal
}

class ExecutionFrameImpl : ExecutionFrameInternal {

    override val board: Board
    override val returnPoint: CodePoint
    private val parent: ExecutionFrame?

    private constructor(board: Board, returnPoint: CodePoint, parent: ExecutionFrame?) {
        this.board = board
        this.returnPoint = returnPoint
        this.parent = parent
        this.variables = mutableMapOf()
    }

    constructor(board: Board, returnLabel: CodePoint) : this(board, returnLabel, null)

    private val variables: MutableMap<String, Int>

    override fun getVariable(name: String): Int? {
        return variables[name] ?: parent?.getVariable(name)
    }

    override fun setVariable(name: String, value: Int) {
        variables[name] = value
    }

    override fun child(returnPoint: CodePoint) = ExecutionFrameImpl(board, returnPoint, this)

}

open class ExecutionException(message: String? = null) : Exception(message)
