package net.averkhoglyad.grex.arrow.gui.fragment.line

import net.averkhoglyad.grex.arrow.gui.data.*
import tornadofx.label

class SimpleCommandLabelFragment : BaseLabelFragment() {

    override fun createRoot() = label {
        text = when (line) {
            is JumpLine -> "Прыжок"
            is StepLine -> "Шаг"
            is TurnLine -> "Поворот"
            is ElseLine -> "Иначе"
            is EndBlockLine -> "Конец блока"
            else -> throw IllegalStateException()
        }
    }
}