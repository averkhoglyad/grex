package net.averkhoglyad.grex.arrow.gui.fragment.line

import net.averkhoglyad.grex.arrow.gui.data.ProcedureAwareLine
import tornadofx.*

abstract class BaseProcedureAwareFragment : BaseLabelFragment() {

    override fun createRoot() = textflow {
        label(labelText())
        label("(") {
            paddingLeft = 10
            paddingRight = 5
        }
        textfield((line as ProcedureAwareLine).nameProperty)
        label(")") {
            paddingLeft = 5
        }
    }

    abstract fun labelText(): String

}