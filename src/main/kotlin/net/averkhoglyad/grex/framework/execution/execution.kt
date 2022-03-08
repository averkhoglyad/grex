package net.averkhoglyad.grex.framework.execution

import net.averkhoglyad.grex.framework.board.Board
import net.averkhoglyad.grex.framework.code.*

fun Program.execution(): Executor = ExecutorImpl(board, code)

interface Executor {
    fun controlBy(fn: ExecutionController): Executor
    fun onEach(handler: (ExecutionPoint) -> Unit): Executor = this
    fun onCatch(handler: (ExecutionException) -> Unit): Executor = this
    fun onFinish(handler: () -> Unit): Executor = this
    fun execute(): Executor
}

typealias ExecutionController = (ControlledExecution) -> Unit

interface ControlledExecution {
    val current: ExecutionPoint?
    val finished: Boolean

    fun advanceNext(): ExecutionPoint?
    fun finish()
}

private class ExecutorImpl(private val board: Board, private val code: List<CodePoint>) : Executor {

    private var onEachFn: (ExecutionPoint) -> Unit = {}
    private var onCatchFn: (ExecutionException) -> Unit = {}
    private var onFinishFn: () -> Unit = {}
    private var executionControllerFn: ExecutionController = { while (!it.finished) it.advanceNext() }

    override fun controlBy(fn: ExecutionController): Executor {
        executionControllerFn = fn
        return this
    }

    override fun onEach(handler: (ExecutionPoint) -> Unit): Executor {
        onEachFn = handler
        return this
    }

    override fun onCatch(handler: (ExecutionException) -> Unit): Executor {
        onCatchFn = handler
        return this
    }

    override fun onFinish(handler: () -> Unit): Executor {
        onFinishFn = handler
        return this
    }

    override fun execute(): Executor {
        try {
            val iterator = ExecutionIterator(board, code)
            val controlledExecution = object : ControlledExecution {
                override var current: ExecutionPoint?
                    private set
                override var finished = false
                    private set
                private var handle: () -> Unit = noop

                init {
                    val (executionPoint, fn) = iterator.next()
                    current = executionPoint
                    handle = fn
                }

                override fun advanceNext(): ExecutionPoint? {
                    if (finished) {
                        throw IllegalStateException("Execution is finished")
                    }
                    handle()
                    onEachFn(current!!)
                    if (iterator.hasNext()) {
                        val (executionPoint, nextHandle) = iterator.next()
                        current = executionPoint
                        handle = nextHandle
                        return executionPoint
                    } else {
                        current = null
                        handle = noop
                        finish()
                        return null
                    }
                }

                override fun finish() {
                    finished = true
                    onFinishFn()
                }

            }
            executionControllerFn(controlledExecution)

        } catch (ex: ExecutionException) {
            onCatchFn(ex)
        }
        return this
    }
}

private val noop: () -> Unit = {}

private class ExecutionIterator(board: Board, code: List<CodePoint>) : Iterator<Pair<ExecutionPoint, () -> Unit>> {

    private var position = 0
    private val code: List<CodePoint>
    private val frames = ArrayDeque<ExecutionFrameInternal>()

    init {
        var endPoint = code.last().takeIf { it is EmptyPoint }
        if (endPoint == null) {
            endPoint = EmptyPoint()
            this.code = code + endPoint
        } else {
            this.code = code
        }
        frames += ExecutionFrameImpl(board, endPoint)
    }

    override fun hasNext(): Boolean {
        return position in code.indices
    }

    override fun next(): Pair<ExecutionPoint, () -> Unit> {
        val frame = frames.last()
        val cp: CodePoint = code[position++]
        val executionPoint = ExecutionPoint(frame, cp::class)
        val handle: () -> Unit = { executeCodePoint(frame, cp) }
        return executionPoint to handle
    }

    private fun executeCodePoint(frame: ExecutionFrameInternal, cp: CodePoint) {
        when (cp) {
            is ExecPoint -> {
                cp.exec(frame)
            }
            is GoTo -> {
                goTo(cp.point)
            }
            is ConditionalGoTo -> {
                cp.ifTrue(frame) { goTo(cp.point) }
            }
            is Invoke -> {
                frames.addLast(frame.child(cp.returnPoint))
                goTo(cp.invokePoint)
            }
            is Return -> {
                goTo(frames.removeLast().returnPoint)
            }
            is EmptyPoint -> {}
        }
    }

    private fun goTo(point: CodePoint) {
        position = code.indexOf(point)
            .takeUnless { it < 0 }
            ?: throw ExecutionException("Unreachable pointer")
    }

}

private fun ConditionalGoTo.ifTrue(frame: ExecutionFrame, fn: () -> Unit) {
    if (condition.test(frame)) {
        fn()
    }
}

