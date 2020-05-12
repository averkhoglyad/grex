package io.averkhoglyad.grex.framework

interface Direction {
    val value: Int
}

data class Point(val x: Int, val y: Int)

data class Hero<S>(val position: Point, val direction: Direction, override val state: S) : BoardUnit<S> {
    override fun isPlacedOn(point: Point): Boolean = position == point
    override val boundary: Pair<Point, Point> = position to position
}

interface BoardUnit<S> {
    val state: S
    fun isPlacedOn(point: Point): Boolean
    val boundary: Pair<Point, Point>
}

