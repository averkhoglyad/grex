package net.averkhoglyad.grex.arrow.gui.fragment.line

import javafx.scene.Node
import javafx.scene.control.ContextMenu
import javafx.scene.control.Label
import net.averkhoglyad.grex.arrow.core.program.BorderCondition
import net.averkhoglyad.grex.arrow.gui.data.ConditionalLine
import net.averkhoglyad.grex.arrow.gui.util.convert
import tornadofx.*

private val BORDER_CONDITION_LABEL = mapOf(
    null to "<Не выбрано>",
    BorderCondition.IS_BORDER to "Край",
    BorderCondition.IS_NOT_BORDER to "Не край"
)

abstract class BaseConditionalLabelFragment : BaseLabelFragment() {

    private val layoutPosition: Pair<Double, Double> by lazy { calculateLayoutPosition() }

    override fun createRoot() = textflow {
        label(labelText())
        label("(") {
            paddingLeft = 10
            paddingRight = 5
        }
        label((line as ConditionalLine).conditionProperty.convert { BORDER_CONDITION_LABEL[it] }) {
            setOnMouseClicked { showMenu() }
        }
        label(")") {
            paddingLeft = 5
        }
    }

    protected abstract fun labelText(): String

    private val conditionMenu: ContextMenu = ContextMenu().apply {
        conditionItem(BorderCondition.IS_BORDER)
        conditionItem(BorderCondition.IS_NOT_BORDER)
    }

    private fun ContextMenu.conditionItem(cond: BorderCondition?) {
        item(BORDER_CONDITION_LABEL[cond]!!) {
            action { (line as ConditionalLine).condition = cond }
        }
    }

    private fun Label.showMenu() {
        var x = scene.window.x + layoutPosition.first + boundsInParent.centerX
        var y = scene.window.y + layoutPosition.second + boundsInParent.centerY + height
        conditionMenu.show(this, x, y)
    }

    private fun calculateLayoutPosition(): Pair<Double, Double> {
        var x = 0.0
        var y = 0.0
        var node: Node? = root
        while (node != null) {
            x += node.boundsInParent.minX
            y += node.boundsInParent.minY
            node = node.parent
        }
        return x to y
    }
}