package io.averkhoglyad.grex.roo.core

import io.averkhoglyad.grex.framework.*

typealias Roo = Hero<RooState>
typealias RooBoard = Board<Roo, RooState>

enum class RooState {
    JUMP, STEP
}

enum class QuarterDirection(override val value: Int) : Direction {
    UP(0),
    RIGHT(90),
    DOWN(180),
    LEFT(270)
}

class CollisionException : ExecutionException("Border collision")

data class Line(val from: Point, val to: Point) : BoardUnit<Nothing?> {
    override val state: Nothing? = null

    override fun isPlacedOn(point: Point): Boolean {
        return from == point || to == point // TODO: Implement checking more complex
    }
    override val boundary: Pair<Point, Point>
        get() = TODO("Not yet implemented")
}