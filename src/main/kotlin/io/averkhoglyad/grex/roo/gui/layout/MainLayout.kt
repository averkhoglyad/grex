package io.averkhoglyad.grex.roo.gui.layout

import io.averkhoglyad.grex.framework.*
import io.averkhoglyad.grex.roo.core.QuarterDirection
import io.averkhoglyad.grex.roo.core.Roo
import io.averkhoglyad.grex.roo.core.RooState
import io.averkhoglyad.grex.roo.gui.view.BoardView
import io.averkhoglyad.grex.roo.gui.view.ProgramView
import javafx.geometry.Pos
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.javafx.JavaFx
import kotlinx.coroutines.launch
import tornadofx.*

class MainLayout : View("Roo") {

    val programView by inject<ProgramView>()
    val boardView by inject<BoardView>()

    override val root = borderpane {
        top {
            toolbar {
                button("Run") {
                    action {
                        boardView.board = createEmptyBoard()
                        val code = compile()
                        GlobalScope.launch(Dispatchers.JavaFx) {
                            code.asIterator(board = boardView.board).forEach {
                                delay(500)
                                boardView.applyAct(it)
                            }
                        }
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

    private fun createEmptyBoard() = BoardImpl(21 to 17, Roo(Point(0, 0), QuarterDirection.RIGHT, RooState.JUMP))

    private fun compile(): Program {
        return program {
            programView.program
                    .filterNotNull()
                    .forEach { cmd -> apply(cmd.compile) }
        }
    }

    init {
        primaryStage.apply {
            minWidth = 1024.0
            minHeight = 720.0
            width = 1024.0
            height = 720.0
//            consumeCloseRequest { window ->
//                confirm("Do you sure want to close program?") {
//                    window.close()
//                }
//            }
        }
    }

}
