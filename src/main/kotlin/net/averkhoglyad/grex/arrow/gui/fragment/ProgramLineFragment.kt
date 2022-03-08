package net.averkhoglyad.grex.arrow.gui.fragment

import javafx.beans.property.ReadOnlyBooleanWrapper
import javafx.beans.property.ReadOnlyIntegerWrapper
import javafx.scene.paint.Color
import javafx.scene.text.FontWeight
import net.averkhoglyad.grex.arrow.gui.data.IfLine
import net.averkhoglyad.grex.arrow.gui.data.ProgramLine
import net.averkhoglyad.grex.arrow.gui.data.WhileLine
import net.averkhoglyad.grex.arrow.gui.fragment.line.IfBlockLabelFragment
import net.averkhoglyad.grex.arrow.gui.fragment.line.SimpleCommandLabelFragment
import net.averkhoglyad.grex.arrow.gui.fragment.line.WhileBlockLabelFragment
import net.averkhoglyad.grex.arrow.gui.util.convert
import tornadofx.*

private val TRUE_WRAPPER = ReadOnlyBooleanWrapper(true).readOnlyProperty

/**
 * Line fragment facade
 */
class ProgramLineFragment : ListCellFragment<ProgramLine?>() {

    private val lineNum = cellProperty.select { (it?.indexProperty()?.add(1)) ?: ReadOnlyIntegerWrapper(-1) }
        .convert { line -> line?.toInt()?.takeIf { it > 0 }?.toString() ?: "" }

    override val root = hbox {
        pane {
            label("!") {
                style {
                    fontWeight = FontWeight.BOLD
                    textFill = Color.RED
                }
                itemProperty.select { it?.isValidProperty ?: TRUE_WRAPPER }
                    .onChange {
                        if (it == true) this@label.hide()
                        else this@label.show()
                    }
            }
        }
        pane {
            prefWidth = 30.0
            label(lineNum)
        }
        pane {
            itemProperty.onChange {
                this@pane.children.clear()
                it?.apply {
                    this@pane += lineFragment(this)
                }
            }
        }
    }

    private fun lineFragment(line: ProgramLine): Fragment {
        return when (line) {
            is IfLine -> find<IfBlockLabelFragment>(mapOf(IfBlockLabelFragment::line to line))
            is WhileLine -> find<WhileBlockLabelFragment>(mapOf(WhileBlockLabelFragment::line to line))
            else -> find<SimpleCommandLabelFragment>(mapOf(SimpleCommandLabelFragment::line to line))
        }
    }

}
