package net.averkhoglyad.grex.arrow.gui.view

import net.averkhoglyad.grex.framework.board.BoardImpl
import net.averkhoglyad.grex.framework.board.Point
import javafx.beans.binding.DoubleBinding
import javafx.beans.property.SimpleObjectProperty
import javafx.geometry.Pos
import javafx.scene.Node
import javafx.scene.image.ImageView
import javafx.scene.layout.GridPane
import javafx.scene.paint.Color
import javafx.scene.shape.Rectangle
import net.averkhoglyad.grex.arrow.core.model.Arrow
import net.averkhoglyad.grex.arrow.core.model.ArrowDirection
import net.averkhoglyad.grex.arrow.core.model.arrow
import net.averkhoglyad.grex.arrow.core.model.lines
import net.averkhoglyad.grex.framework.board.Board
import net.averkhoglyad.grex.framework.board.lookup
import tornadofx.*
import javafx.scene.shape.Line as LineFx

private const val CELL_SIZE = 35.0
private const val ARROW_SIZE = 20.0

class BoardView : View() {

    private val boardProperty = SimpleObjectProperty<Board>(BoardImpl(20 to 16, Arrow(Point(0, 0), ArrowDirection.RIGHT)))
    var board: Board by boardProperty

    private val arrowImage: ImageView = imageview(resources.image("/img/arrow.png")) {
        fitWidth = ARROW_SIZE
        fitHeight = ARROW_SIZE
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
        this += arrowImage
    }

    init {
        positionArrow()
        boardProperty.onChange {
            it ?: throw NullPointerException()
            unitsPane.children.clear()
            boardPane.children.clear()
            boardPane.generateCells(it)
            this += boardPane
            this += arrowImage
            positionArrow()
        }
    }

    fun handleTick() {
        positionArrow()
        positionLines()
    }

    private fun positionLines() {
        val lines = board.lines
        if (lines.size > unitsPane.children.size) {
            for (i in unitsPane.children.size until lines.size) {
                val line = lines[i]
                unitsPane.line {
                    stroke = Color.CHOCOLATE
                    val start = boardPane.findCell(line.from)
                    val end = boardPane.findCell(line.to)
                    placeInto(start, end)
                }
            }
        }
    }

    private fun positionArrow() {
        val cell: Node = boardPane.findCell(board.lookup<Arrow>().first().position)
        arrowImage.placeInto(cell)
        arrowImage.rotate = board.arrow.direction.value.toDouble().rad.value
    }

    private fun GridPane.findCell(point: Point): Node {
        val rectangles = userData as List<Rectangle>
        val target = rectangles.find { it.userData == point }
        return target ?: throw IllegalStateException()
    }
}

private fun GridPane.generateCells(board: Board) {
    val cells = mutableListOf<Node>()
    for (row in 0..board.size.second) {
        row {
            for (col in 0..board.size.first) {
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

private fun Node.placeInto(cell: Node) {
    val currentCenter = center()
    val cellCenter = cell.center()
    this.translateXProperty().bind(cellCenter.first - currentCenter.first)
    this.translateYProperty().bind(cellCenter.second - currentCenter.second)
}

private fun LineFx.placeInto(start: Node, end: Node) {
    val from = start.center()
    startXProperty().bind(from.first)
    startYProperty().bind(from.second)

    val to = end.center()
    endXProperty().bind(to.first)
    endYProperty().bind(to.second)
}

private fun Node.center(): Pair<DoubleBinding, DoubleBinding> = (layoutXProperty() + boundsInParent.width / 2) to (layoutYProperty() + boundsInParent.height / 2)
