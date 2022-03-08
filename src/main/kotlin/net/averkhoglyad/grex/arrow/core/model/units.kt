package net.averkhoglyad.grex.arrow.core.model

import net.averkhoglyad.grex.framework.board.*
import net.averkhoglyad.grex.framework.code.ExecutionException

class Arrow(var position: Point, var direction: ArrowDirection) : BoardUnit {
    override val boundary: Pair<Point, Point> = position to position
    override fun isPlacedOn(point: Point): Boolean = position == point
}

enum class ArrowDirection(val value: Int) {
    UP(0),
    RIGHT(90),
    DOWN(180),
    LEFT(270);
}

class Line(from: Point, to: Point) : BoardUnit {
    val from: Point
    val to: Point

    init {
        this.from = from.takeIf { from < to } ?: to
        this.to = to.takeIf { from < to } ?: from
    }

    operator fun component1(): Point = from

    operator fun component2(): Point = to

    override fun isPlacedOn(point: Point): Boolean {
        return from == point || to == point // TODO: Implement checking more complex
    }
    override val boundary: Pair<Point, Point> = from to to
}

val Board.arrow: Arrow
    get() = this.lookup<Arrow>().first()

val Board.lines: List<Line>
    get() = this.lookup()
