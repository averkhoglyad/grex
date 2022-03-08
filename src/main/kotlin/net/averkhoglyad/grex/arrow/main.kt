package net.averkhoglyad.grex.arrow

import javafx.application.Application
import net.averkhoglyad.grex.arrow.core.model.Arrow
import net.averkhoglyad.grex.arrow.core.model.ArrowDirection
import net.averkhoglyad.grex.arrow.core.model.writeTo
import net.averkhoglyad.grex.arrow.core.program.ArrowCommand
import net.averkhoglyad.grex.arrow.core.program.BorderCondition
import net.averkhoglyad.grex.arrow.gui.ArrowApp
import net.averkhoglyad.grex.framework.board.Board
import net.averkhoglyad.grex.framework.board.BoardImpl
import net.averkhoglyad.grex.framework.board.Point
import net.averkhoglyad.grex.framework.execution.Program
import net.averkhoglyad.grex.framework.execution.execution
import net.averkhoglyad.grex.framework.program.program
import java.nio.file.Paths

fun main(args: Array<String>) {
    Application.launch(ArrowApp::class.java, *args)
}

fun main0() {
    val arrow = Arrow(Point(0, 0), ArrowDirection.RIGHT)
    val board = BoardImpl(12 to 16, arrow)
    val program = generateProgram(board)
    program.execution().execute()
    board.writeTo(Paths.get("img.png"))
}

private fun generateProgram(board: Board): Program = program(board) {
    repeat(25) {
        doIf(BorderCondition.IS_NOT_BORDER) {
            command(ArrowCommand.Step::exec)
        } otherwise {
            invoke("turn_right")
        }
    }

    define("turn_right") {
        command(ArrowCommand.Turn::exec)
        command(ArrowCommand.Turn::exec)
        command(ArrowCommand.Turn::exec)
    }

//    repeatWhile(BorderCondition.IS_NOT_BORDER) {
//        exec(ArrowCommand.Step::exec)
//        doIf(BorderCondition.IS_NOT_BORDER) {
//            exec(ArrowCommand.Jump::exec)
//        }
//    }

    define("corner") {
        command(ArrowCommand.Step::exec)
        command(ArrowCommand.Jump::exec)
        command(ArrowCommand.Step::exec)
        command(ArrowCommand.Jump::exec)
        command(ArrowCommand.Step::exec)

        invoke("turn_right")

        command(ArrowCommand.Step::exec)
        command(ArrowCommand.Jump::exec)
        command(ArrowCommand.Step::exec)
        command(ArrowCommand.Jump::exec)
        command(ArrowCommand.Step::exec)
    }

    invoke("turn_right")
    invoke("corner")
    command(ArrowCommand.Jump::exec)
    invoke("corner")
}
