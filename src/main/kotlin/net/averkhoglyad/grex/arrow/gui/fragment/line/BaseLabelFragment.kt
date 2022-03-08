package net.averkhoglyad.grex.arrow.gui.fragment.line

import javafx.scene.layout.Region
import net.averkhoglyad.grex.arrow.gui.data.ProgramLine
import tornadofx.Fragment
import tornadofx.paddingLeft

abstract class BaseLabelFragment : Fragment() {

    val line: ProgramLine by param()

    override val root = createRoot()

    init {
        root.apply {
            paddingLeft = line.level * 15.0
        }
    }

    protected abstract fun createRoot(): Region
}