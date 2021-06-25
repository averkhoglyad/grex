package io.averkhoglyad.grex.roo.core

import io.averkhoglyad.grex.framework.*

sealed class RooCommand {
    abstract val compile: SubprogramBuilder.() -> Unit

    object Jump : RooCommand() {
        override val compile: SubprogramBuilder.() -> Unit = {
            eval {
                val code: CodePoint = BoardModify {
                    val point = (board as RooBoard).hero.step()
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
                listOf(code)
            }
        }
    }

    object Step : RooCommand() {
        override val compile: SubprogramBuilder.() -> Unit = {
            eval {
                val code: CodePoint = BoardModify {
                    val point = (board as RooBoard).hero.step()
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
                listOf(code)
            }
        }
    }

    object Turn : RooCommand() {
        override val compile: SubprogramBuilder.() -> Unit = {
            eval {
                val code: CodePoint = BoardModify {
                    (board as RooBoard).modify {
                        hero {
                            rotate(board.hero.turn())
                        }
                    }
                }
                listOf(code)
            }
        }
    }
}

enum class BorderCondition : Condition<ExecutionFrame> {
    IS_BORDER {
        override fun test(frame: ExecutionFrame): Boolean = (frame.board as RooBoard).hero.step() !in frame.board
    },
    IS_NOT_BORDER {
        override fun test(frame: ExecutionFrame): Boolean = (frame.board as RooBoard).hero.step() in frame.board
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
