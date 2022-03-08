package net.averkhoglyad.grex.arrow.core.program

import net.averkhoglyad.grex.arrow.core.model.Arrow
import net.averkhoglyad.grex.arrow.core.model.ArrowDirection
import net.averkhoglyad.grex.arrow.core.model.Line
import net.averkhoglyad.grex.arrow.core.model.arrow
import net.averkhoglyad.grex.framework.board.Board
import net.averkhoglyad.grex.framework.board.Point
import net.averkhoglyad.grex.framework.code.ExecutionFrame
import net.averkhoglyad.grex.framework.code.ExecutionFrameCondition

sealed class ArrowCommand {

    abstract fun exec(frame: ExecutionFrame)

    object Jump : ArrowCommand() {
        override fun exec(frame: ExecutionFrame) {
            val arrow = frame.board.arrow
            val point = arrow.step()
            if (point !in frame.board) {
                throw CollisionException()
            }
            arrow.position = point
        }
    }

    object Step : ArrowCommand() {
        override fun exec(frame: ExecutionFrame) {
            val arrow = frame.board.arrow
            val point = arrow.step()
            if (point !in frame.board) {
                throw CollisionException()
            }
            frame.board.putUnit(Line(point, arrow.position))
            arrow.position = point
        }
    }

    object Turn : ArrowCommand() {
        override fun exec(frame: ExecutionFrame) {
            frame.board.arrow.direction = frame.board.arrow.turn()
        }
    }
}

enum class BorderCondition : ExecutionFrameCondition {
    IS_BORDER {
        override fun test(frame: ExecutionFrame): Boolean = frame.board.arrow.step() !in frame.board
    },
    IS_NOT_BORDER {
        override fun test(frame: ExecutionFrame): Boolean = frame.board.arrow.step() in frame.board
    }
}

// TODO: Abstract implementation
private fun Arrow.step(): Point = when (direction) {
    ArrowDirection.UP -> Point(position.x, position.y - 1)
    ArrowDirection.LEFT -> Point(position.x - 1, position.y)
    ArrowDirection.DOWN -> Point(position.x, position.y + 1)
    ArrowDirection.RIGHT -> Point(position.x + 1, position.y)
}

private fun Arrow.turn(): ArrowDirection = when (direction) {
    ArrowDirection.UP -> ArrowDirection.LEFT
    ArrowDirection.LEFT -> ArrowDirection.DOWN
    ArrowDirection.DOWN -> ArrowDirection.RIGHT
    ArrowDirection.RIGHT -> ArrowDirection.UP
}
