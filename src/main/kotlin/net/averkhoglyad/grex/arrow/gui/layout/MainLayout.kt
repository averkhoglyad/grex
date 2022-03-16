package net.averkhoglyad.grex.arrow.gui.layout

import javafx.application.Application
import javafx.beans.property.SimpleObjectProperty
import net.averkhoglyad.grex.arrow.core.model.ArrowDirection
import net.averkhoglyad.grex.arrow.core.model.Arrow
import net.averkhoglyad.grex.arrow.gui.view.BoardView
import net.averkhoglyad.grex.arrow.gui.view.EditorView
import javafx.geometry.Pos
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.delay
import kotlinx.coroutines.javafx.JavaFx
import kotlinx.coroutines.launch
import net.averkhoglyad.grex.arrow.core.program.CompilationException
import net.averkhoglyad.grex.arrow.core.program.compile
import net.averkhoglyad.grex.arrow.gui.data.*
import net.averkhoglyad.grex.arrow.gui.util.consumeCloseRequest
import net.averkhoglyad.grex.framework.board.BoardImpl
import net.averkhoglyad.grex.framework.board.Point
import net.averkhoglyad.grex.framework.code.CommandPoint
import net.averkhoglyad.grex.framework.execution.Executor
import net.averkhoglyad.grex.framework.execution.execution
import tornadofx.*

private const val DEFAULT_MIN_WIDTH = 1024.0
private const val DEFAULT_MIN_HEIGHT = 720.0

class MainLayout : View("Arrow") {

    private val editorView by inject<EditorView>()
    private val boardView by inject<BoardView>()

    private var executionStateProperty = SimpleObjectProperty(ExecutionState.STOP)
    private var executionState: ExecutionState by executionStateProperty

    private var channel: Channel<Unit>? = null

    override val root = borderpane {
        top {
            toolbar {
                button("Run") {
                    disableWhen(executionStateProperty.isEqualTo(ExecutionState.RUN))
                    action {
                        if (executionState == ExecutionState.STOP) {
                            runProgram()
                        }
                        executionState = ExecutionState.RUN
                    }
                }
                button("Pause") {
                    enableWhen(executionStateProperty.isEqualTo(ExecutionState.RUN))
                    action {
                        executionState = ExecutionState.PAUSE
                    }
                }
                button("Stop") {
                    disableWhen(executionStateProperty.isEqualTo(ExecutionState.STOP))
                    action {
                        executionState = ExecutionState.STOP
                    }
                }
                button("Clear") {
                    enableWhen(executionStateProperty.isEqualTo(ExecutionState.STOP))
                    action {
                        boardView.reset()
                    }
                }
            }
        }
        left {
            this += editorView
        }
        center {
            scrollpane {
                isFitToWidth = true
                isFitToHeight = true
                flowpane {
                    alignment = Pos.CENTER
                    this += boardView
                }
            }
        }
    }

    init {
        primaryStage.apply {
            minWidth = DEFAULT_MIN_WIDTH
            minHeight = DEFAULT_MIN_HEIGHT
            width = DEFAULT_MIN_WIDTH
            height = DEFAULT_MIN_HEIGHT

            consumeCloseRequest { window ->
                confirm("Are you sure you want to exit?") {
                    window.close()
                }
            }
        }

        executionStateProperty.onChange {
            when (it) {
                ExecutionState.STOP -> channel!!.cancel()
                ExecutionState.RUN -> GlobalScope.launch { channel!!.send() }
                ExecutionState.PAUSE -> { /* nothing needed */ }
            }
        }
    }

    private fun runProgram() {
        // TODO: Move compilation logic to controller
        boardView.reset()
        val program = compile(boardView.board) {
            var cb = it
            editorView.program
                .forEachIndexed { index, line ->
                    if (line == null) return@forEachIndexed
                    require(line.isValidProperty.get()) { "Invalid line ${index + 1}" }
                    cb = when (line) {
                        is JumpLine -> cb.jump()
                        is StepLine -> cb.step()
                        is TurnLine -> cb.turn()

                        is IfLine -> cb.ifBlock(line.condition!!)
                        is ElseLine -> cb.elseBlock()
                        is WhileLine -> cb.whileBlock(line.condition!!)

                        is DefineLine -> cb.define(line.name)
                        is InvokeLine -> cb.invoke(line.name)

                        is EndBlockLine -> cb.close()

                        else -> throw CompilationException("Unexpected line type")
                    }
                }
        }

        val channel = Channel<Unit>()
        this.channel = channel
        program.execution()
            .controlBy(channel)
            .onEach {
                GlobalScope.launch(Dispatchers.JavaFx) {
                    if (it.pointType == CommandPoint::class) {
                        delay(500)
                        boardView.handleTick()
                    }
                    if (executionState == ExecutionState.RUN) {
                        channel.send()
                    }
                }
            }
            .onCatch {
                error(it.message ?: "Unexpected execution error")
            }
            .onFinish {
                executionState = ExecutionState.STOP
            }
            .execute()
    }
}

private enum class ExecutionState {
    STOP, RUN, PAUSE
}

private suspend fun Channel<Unit>.send() = this.send(Unit)
