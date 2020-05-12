package io.averkhoglyad.grex.framework

/**
 * Abstraction to represent program execution state and result
 */
interface Board<H : Hero<S>, S> {

    val size: Pair<Int, Int>
    val hero: Hero<S>
    val units: Set<BoardUnit<*>>

    operator fun contains(point: Point): Boolean
    fun applyAct(act: BoardAct<H, S>)
}

class BoardImpl<H : Hero<S>, S>(size: Pair<Int, Int>, hero: H): Board<H, S> {

    init {
        require((hero.position.x in 0..size.first) && (hero.position.y in 0..size.second)) { "Position of the hero is outside the board" }
    }

    override val size: Pair<Int, Int> = size
    override var hero: Hero<S> = hero
        internal set(value) {
            require(value.position in this) { "Position of the hero is outside the board" }
            field = value
        }

    private val _units = mutableSetOf<BoardUnit<*>>()
    override val units: Set<BoardUnit<*>> = _units

    override operator fun contains(point: Point): Boolean = (point.x in 0..size.first) && (point.y in 0..size.second)

    override fun applyAct(act: BoardAct<H, S>) {
        when(act) {
            is BoardAct.PlaceAct -> {
                this._units.add(act.unit)
            }
            is BoardAct.MoveAct -> {
                this.hero = this.hero.copy(position = act.point)
            }
            is BoardAct.RotateAct -> {
                this.hero = this.hero.copy(direction = act.direction)
            }
            is BoardAct.StateAct -> {
                this.hero = this.hero.copy(state = act.state)
            }
            is BoardAct.CompoundAct -> act.acts.forEach { applyAct(it) }
        }
    }

}

// TODO: Move builder to ExecutionFrame
fun <H : Hero<S>, S> Board<H, S>.modify(fn: BoardModification<H, S>.() -> Unit): BoardAct<H, S> {
    val acts = arrayListOf<BoardAct<H, S>>()
    val mod = object : BoardModification<H, S> {
        override fun hero(act: HeroModification<H, S>.() -> Unit) {
            val ctx = object : HeroModification<H, S> {
                override fun place(hero: H) {
                    acts.add(BoardAct.PlaceAct(hero))
                }

                override fun state(state: S) {
                    acts.add(BoardAct.StateAct(hero, state))
                }

                override fun rotate(direction: Direction) {
                    acts.add(BoardAct.RotateAct(hero, direction))
                }

                override fun moveTo(point: Point) {
                    acts.add(BoardAct.MoveAct(hero, point))
                }
            }
            ctx.act()
        }

        override fun unit(act: UnitModification.() -> Unit) {
            val ctx = object : UnitModification {
                override fun place(hero: BoardUnit<*>) {
                    acts.add(BoardAct.PlaceAct(hero))
                }
            }
            ctx.act()
        }
    }
    mod.fn()
    return BoardAct.CompoundAct(acts)
}

// TODO: Modify Hero and other BoardUnits using same board instructions (a.k. BoardActs)
interface BoardModification<H : Hero<S>, S> {
    fun hero(act: HeroModification<H, S>.() -> Unit)
    fun unit(act: UnitModification.() -> Unit)
}

interface HeroModification<H : Hero<S>, S> {
    fun place(hero: H)
    fun moveTo(point: Point)
    fun state(state: S)
    fun rotate(direction: Direction)
}

interface UnitModification {
    fun place(unit: BoardUnit<*>)
//    fun lookUp(point: Point, fn: UnitModification.(Array<BoardUnit>) -> Unit = {}): Array<BoardUnit>
}

sealed class BoardAct<H : Hero<S>, S> {

    class DoNothing<H : Hero<S>, S> : BoardAct<H, S>()
    class PlaceAct<H : Hero<S>, S>(val unit: BoardUnit<*>) : BoardAct<H, S>()
    class MoveAct<H : Hero<S>, S>(val unit: BoardUnit<*>, val point: Point) : BoardAct<H, S>()
    class RotateAct<H : Hero<S>, S>(val unit: BoardUnit<*>, val direction: Direction) : BoardAct<H, S>()
    class StateAct<H : Hero<S>, S>(val unit: BoardUnit<S>, val state: S) : BoardAct<H, S>()
    class CompoundAct<H : Hero<S>, S>(val acts: Iterable<BoardAct<H, S>>) : BoardAct<H, S>() {
        constructor(vararg acts: BoardAct<H, S>) : this(listOf(*acts))
    }

}
