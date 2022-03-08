package net.averkhoglyad.grex.arrow.gui.layout

import net.averkhoglyad.grex.arrow.core.model.ArrowDirection
import net.averkhoglyad.grex.arrow.core.model.Arrow
import net.averkhoglyad.grex.arrow.gui.view.BoardView
import net.averkhoglyad.grex.arrow.gui.view.ProgramView
import javafx.geometry.Pos
import javafx.scene.control.Alert
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.channels.ticker
import kotlinx.coroutines.delay
import kotlinx.coroutines.javafx.JavaFx
import kotlinx.coroutines.launch
import net.averkhoglyad.grex.arrow.core.model.lines
import net.averkhoglyad.grex.arrow.core.model.writeTo
import net.averkhoglyad.grex.arrow.core.program.compile
import net.averkhoglyad.grex.arrow.gui.data.*
import net.averkhoglyad.grex.arrow.gui.util.consumeCloseRequest
import net.averkhoglyad.grex.framework.board.BoardImpl
import net.averkhoglyad.grex.framework.board.Point
import net.averkhoglyad.grex.framework.code.CommandPoint
import net.averkhoglyad.grex.framework.execution.execution
import tornadofx.*
import java.nio.file.Paths

private const val DEFAULT_MIN_WIDTH = 1024.0
private const val DEFAULT_MIN_HEIGHT = 720.0

class MainLayout : View("Arrow") {

    private val programView by inject<ProgramView>()
    private val boardView by inject<BoardView>()

    override val root = borderpane {
        top {
            toolbar {
                button("Run") {
                    action {
                        runProgram()
                    }
                }
                button("Reset") {
                    action {
                        boardView.board = createEmptyBoard()
                    }
                }
            }
        }
        left {
            this += programView
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
    }

    private fun runProgram() {
        // TODO: Move compilation logic to controller
        val program = compile(createEmptyBoard()) {
            var cb = it
            programView.program
                .filterNotNull()
                .forEachIndexed { index, line ->
                    require(line.isValidProperty.get()) { "Invalid line ${index + 1}" }
                    cb = when (line) {
                        is IfLine -> cb.ifBlock(line.condition!!)
                        is ElseLine -> cb.elseBlock()
                        is WhileLine -> cb.whileBlock(line.condition!!)
                        is EndBlockLine -> cb.close()
                        is JumpLine -> cb.jump()
                        is StepLine -> cb.step()
                        is TurnLine -> cb.turn()
                        else -> cb
                    }
                }
        }
        boardView.board = program.board

        val channel = Channel<Unit>()
        val executor = program.execution()
            .controlBy { ctrl ->
                GlobalScope.launch(Dispatchers.IO) {
                    ctrl.advanceNext() // Start execution
                    channel.consumeEach { ctrl.advanceNext() } // Handle every next execution point
                }
            }
            .onEach {
                GlobalScope.launch(Dispatchers.JavaFx) {
                    if (it.pointType == CommandPoint::class) {
                        delay(300)
                        boardView.handleTick()
                    }
                    channel.send(Unit)
                }
            }
            .onCatch {
                error(it.message ?: "Unexpected execution error")
            }
            .onFinish {
                channel.cancel()
            }
            .execute()
    }

    // TODO: must be encapsulated because is used in BoardView too!
    private fun createEmptyBoard() = BoardImpl(20 to 16, Arrow(Point(0, 0), ArrowDirection.RIGHT))

}
