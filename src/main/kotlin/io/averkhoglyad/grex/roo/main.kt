package io.averkhoglyad.grex.roo

import io.averkhoglyad.grex.framework.*
import io.averkhoglyad.grex.roo.core.*
import io.averkhoglyad.grex.roo.gui.RooApp
import javafx.application.Application
import java.nio.file.Paths

fun main(args: Array<String>) {
    Application.launch(RooApp::class.java, *args)
}

fun main0() {
    val hero = Roo(Point(0, 0), QuarterDirection.RIGHT, RooState.JUMP)
    val board = BoardImpl(12 to 16, hero)
    val program = generateProgram()
    program.execute(board)
    board.writeTo(Paths.get("img.png"))
}

private fun generateProgram(): Program = program {
    repeat(25) {
        doIf(BorderCondition.IS_NOT_BORDER) {
            apply(RooCommand.Step.compile)
        } otherwise {
            invoke("turn_right")
        }
    }

    define("turn_right") {
        apply(RooCommand.Turn.compile)
        apply(RooCommand.Turn.compile)
        apply(RooCommand.Turn.compile)
    }

//    repeatWhile(BorderCondition.IS_NOT_BORDER) {
//        eval(AtomicCommand.STEP)
//        doIf(BorderCondition.IS_NOT_BORDER) {
//            eval(AtomicCommand.JUMP)
//        }
//    }

    define("corner") {
        apply(RooCommand.Step.compile)
        apply(RooCommand.Jump.compile)
        apply(RooCommand.Step.compile)
        apply(RooCommand.Jump.compile)
        apply(RooCommand.Step.compile)

        invoke("turn_right")

        apply(RooCommand.Step.compile)
        apply(RooCommand.Jump.compile)
        apply(RooCommand.Step.compile)
        apply(RooCommand.Jump.compile)
        apply(RooCommand.Step.compile)
    }

    invoke("turn_right")
    invoke("corner")
    apply(RooCommand.Jump.compile)
    invoke("corner")
}