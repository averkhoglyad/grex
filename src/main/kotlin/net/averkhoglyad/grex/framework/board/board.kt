package net.averkhoglyad.grex.framework.board

import kotlin.reflect.KClass

data class Point(val x: Int, val y: Int): Comparable<Point> {
    constructor(pair: Pair<Int, Int>) : this(pair.first, pair.second)
    override fun compareTo(other: Point): Int {
        when {
            this.y > other.y || this.x > other.x -> return 1
            this.y < other.y || this.x < other.x -> return -1
            else -> return 0
        }
    }
}

interface BoardUnit {
    val boundary: Pair<Point, Point>
    fun isPlacedOn(point: Point): Boolean
}

/**
 * Abstraction to represent board as the state and result of execution
 */
interface Board {
    val size: Pair<Int, Int>
    val units: List<BoardUnit>
    operator fun contains(point: Point) = (point.x in 0..size.first) && (point.y in 0..size.second)
    fun putUnit(unit: BoardUnit)
    fun dropUnit(unit: BoardUnit)
    fun clear()
}

// Lookup helper methods
inline fun <reified U: BoardUnit> Board.lookup(): List<U> = units.filterIsInstance<U>()
fun <U: BoardUnit> Board.lookup(klass: KClass<U>): List<U> = units.filterIsInstance(klass.java)
fun <U: BoardUnit> Board.lookup(klass: Class<U>): List<U> = units.filterIsInstance(klass)
fun Board.lookup(predicate: (BoardUnit) -> Boolean): List<BoardUnit> = units.filter(predicate)
fun Board.lookup(point: Point): List<BoardUnit> = units.filter { it.isPlacedOn(point) }

/**
 * Simple in-memory implementation
 */
class BoardImpl(size: Pair<Int, Int>, vararg units: BoardUnit) : Board {
    init {
        require(size.first > 0) { "Invalid size" }
        require(size.second > 0) { "Invalid size" }
    }

    override val size: Pair<Int, Int> = size

    private val _units = mutableListOf(*units)
    override val units: List<BoardUnit> = _units

    override fun putUnit(unit: BoardUnit) {
        require(unit.boundary.first in this) { "Boundary of the unit is outside the board" }
        require(unit.boundary.second in this) { "Boundary of the unit is outside the board" }
        this._units.add(unit)
    }

    override fun dropUnit(unit: BoardUnit) {
        this._units.remove(unit)
    }

    override fun clear() {
        this._units.clear()
    }
}
