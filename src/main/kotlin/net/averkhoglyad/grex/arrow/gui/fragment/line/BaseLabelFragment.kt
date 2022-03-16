package net.averkhoglyad.grex.arrow.gui.fragment.line

import javafx.scene.layout.Region
import net.averkhoglyad.grex.arrow.gui.data.ProgramLine
import tornadofx.Fragment
import tornadofx.paddingLeft

abstract class BaseLabelFragment : Fragment() {

    val line: ProgramLine by param()

    // Must be created here to apply padding based on line's level
    override val root = createRoot()

    init {
        root.paddingLeft = line.level * 15.0
    }

    protected abstract fun createRoot(): Region

}