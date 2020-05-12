package io.averkhoglyad.grex.roo.core

import io.averkhoglyad.grex.framework.*

typealias RooCommand = Command

enum class AtomicCommand : RooCommand {

    JUMP {
        override fun compile(): Iterable<CodePoint> {
            val code: CodePoint = BoardModify<Roo, RooState> {
                val point = board.hero.step()
                if (point !in board) {
                    throw CollisionException()
                }
                board.modify {
                    hero {
                        state(RooState.JUMP)
                        moveTo(point)
                    }
                }
            }
            return listOf(code)
        }
    },

    STEP {
        override fun compile(): Iterable<CodePoint> {
            val code: CodePoint = BoardModify<Roo, RooState> {
                val point = board.hero.step()
                if (point !in board) {
                    throw CollisionException()
                }
                board.modify {
                    hero {
                        state(RooState.STEP)
                        moveTo(point)
                    }
                    unit {
                        place(Line(board.hero.position, point))
                    }
                }
            }
            return listOf(code)
        }
    },

    TURN {
        override fun compile(): Iterable<CodePoint> {
            val code: CodePoint = BoardModify<Roo, RooState> {
                board.modify {
                    hero {
                        rotate(board.hero.turn())
                    }
                }
            }
            return listOf(code)
        }
    };
}

enum class BorderCondition : Condition<ExecutionFrame<Roo, RooState>> {
    IS_BORDER {
        override fun test(frame: ExecutionFrame<Roo, RooState>): Boolean = frame.board.hero.step() !in frame.board
    },
    IS_NOT_BORDER {
        override fun test(frame: ExecutionFrame<Roo, RooState>): Boolean = frame.board.hero.step() in frame.board
    }
}

// TODO: Abstract implementation
private fun Roo.step(): Point = when (direction) {
    QuarterDirection.UP -> Point(position.x, position.y - 1)
    QuarterDirection.LEFT -> Point(position.x - 1, position.y)
    QuarterDirection.DOWN -> Point(position.x, position.y + 1)
    QuarterDirection.RIGHT -> Point(position.x + 1, position.y)
    else -> throw IllegalStateException()
}

private fun Roo.turn(): Direction = when (direction) {
    QuarterDirection.UP -> QuarterDirection.LEFT
    QuarterDirection.LEFT -> QuarterDirection.DOWN
    QuarterDirection.DOWN -> QuarterDirection.RIGHT
    QuarterDirection.RIGHT -> QuarterDirection.UP
    else -> throw IllegalStateException()
}
