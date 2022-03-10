package net.averkhoglyad.grex.arrow.gui.fragment.line

import net.averkhoglyad.grex.arrow.gui.data.ProcedureAwareLine
import tornadofx.*

class DefineBlockLabelFragment : BaseLabelFragment() {
    override fun createRoot() = textflow {
        label("Процедура")
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