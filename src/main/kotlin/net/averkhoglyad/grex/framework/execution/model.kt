package net.averkhoglyad.grex.framework.execution

import net.averkhoglyad.grex.framework.board.Board
import net.averkhoglyad.grex.framework.code.CodePoint
import net.averkhoglyad.grex.framework.code.ExecutionFrame
import kotlin.reflect.KClass

class Program(val board: Board, val code: List<CodePoint>)

data class ExecutionPoint(val frame: ExecutionFrame, val pointType: KClass<out CodePoint>, val metadata: Any? = null)
