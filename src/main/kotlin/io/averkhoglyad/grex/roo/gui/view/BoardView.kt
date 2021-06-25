package io.averkhoglyad.grex.roo.gui.view

import io.averkhoglyad.grex.framework.BoardAct
import io.averkhoglyad.grex.framework.BoardImpl
import io.averkhoglyad.grex.framework.Hero
import io.averkhoglyad.grex.framework.Point
import io.averkhoglyad.grex.roo.core.*
import javafx.beans.binding.DoubleBinding
import javafx.beans.property.SimpleObjectProperty
import javafx.geometry.Pos
import javafx.scene.Node
import javafx.scene.image.ImageView
import javafx.scene.layout.GridPane
import javafx.scene.paint.Color
import javafx.scene.shape.Rectangle
import tornadofx.*
import tornadofx.controlsfx.borders
import javafx.scene.shape.Line as LineFx

private const val CELL_SIZE = 35.0
private const val HERO_SIZE = 20.0

// TODO: Implement Board interface instead of keeping it as model
class BoardView : View() {

    val boardProperty = SimpleObjectProperty<RooBoard>(BoardImpl(21 to 17, Roo(Point(0, 0), QuarterDirection.RIGHT, RooState.JUMP)))
    var board: RooBoard by boardProperty

    private val heroImage: ImageView = imageview(resources.image("/img/arrow.png")) {
        fitWidth = HERO_SIZE
        fitHeight = HERO_SIZE
    }

    private val boardPane: GridPane = gridpane {
        alignment = Pos.CENTER
        generateCells(board)
        style {
            backgroundColor += Color.ANTIQUEWHITE
            borderColor += box(Color.LIGHTGRAY)
        }
    }

    private val unitsPane = anchorpane {}

    override val root = stackpane {
        this += boardPane
        this += unitsPane
        this += heroImage
    }

    init {
        positionHero()
        boardProperty.onChange {
            it ?: throw NullPointerException()
            unitsPane.children.clear()
            boardPane.children.clear()
            boardPane.generateCells(it)
            this += boardPane
            this += heroImage
            positionHero()
        }
    }

    fun applyAct(act: BoardAct<Hero<RooState>, RooState>) {
        addLines(act)
        board.applyAct(act)
        positionHero()
    }

    private fun addLines(act: BoardAct<Hero<RooState>, RooState>) {
        when (act) {
            is BoardAct.CompoundAct -> act.acts.forEach { addLines(it) }
            is BoardAct.PlaceAct -> {
                (act.unit as? Line)?.let {
                    unitsPane.line {
                        val start = boardPane.findCell(it.from)
                        val end = boardPane.findCell(it.to)
                        positionInto(start, end)
                    }
                }
            }
        }
    }

    private fun positionHero() {
        val cell: Node = boardPane.findCell(board.hero.position)
        heroImage.positionInto(cell)
        heroImage.rotate = board.hero.direction.value.toDouble().rad.value
    }

    private fun GridPane.findCell(point: Point): Node {
        val rectangles = userData as List<Rectangle>
        val target = rectangles.find { it.userData == point }
        return target!!
    }
}

private fun GridPane.generateCells(board: RooBoard) {
    val cells = mutableListOf<Node>()
    for (row in 0 until board.size.second) {
        row {
            for (col in 0 until board.size.first) {
                rectangle {
                    cells.add(this)
                    width = CELL_SIZE
                    height = CELL_SIZE
                    fill = Color.TRANSPARENT
                    stroke = Color.LIGHTYELLOW
                    userData = Point(col, row)
                }
            }
        }
    }
    this.userData = cells
}

private fun Node.positionInto(cell: Node) {
    val currentCenter = center()
    val cellCenter = cell.center()
    this.translateXProperty().bind(cellCenter.first - currentCenter.first)
    this.translateYProperty().bind(cellCenter.second - currentCenter.second)
}

private fun LineFx.positionInto(start: Node, end: Node) {
    val from = start.center()
    startXProperty().bind(from.first)
    startYProperty().bind(from.second)

    val to = end.center()
    endXProperty().bind(to.first)
    endYProperty().bind(to.second)
}

private fun Node.center(): Pair<DoubleBinding, DoubleBinding> = (layoutXProperty() + boundsInParent.width / 2) to (layoutYProperty() + boundsInParent.height / 2)
