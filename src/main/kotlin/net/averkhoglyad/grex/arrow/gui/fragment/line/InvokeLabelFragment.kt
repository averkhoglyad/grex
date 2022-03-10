package net.averkhoglyad.grex.arrow.gui.fragment.line

import net.averkhoglyad.grex.arrow.gui.data.ProcedureAwareLine
import tornadofx.*

class InvokeLabelFragment : BaseLabelFragment() {
    override fun createRoot() = textflow {
        label("Вызвать")
        label("(") {
            paddingLeft = 10
            paddingRight = 5
        }
        textfield((line as ProcedureAwareLine).nameProperty)
        label(")") {
            paddingLeft = 5
        }
    }
}