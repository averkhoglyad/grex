package io.averkhoglyad.grex.roo.gui.view

import io.averkhoglyad.grex.roo.core.AtomicCommand
import javafx.beans.property.SimpleListProperty
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyEvent
import tornadofx.*

class ProgramView : View() {

    val programProperty = SimpleListProperty<AtomicCommand>(observableListOf())
    var program by programProperty

    override val root = hbox {
        listview(program) {
            prefWidth = 200.0
            val list = this
            cellFormat { cmd ->
                text = when (cmd) {
                    AtomicCommand.JUMP -> "${this.index}. Прыжок"
                    AtomicCommand.STEP -> "${this.index}. Шаг"
                    AtomicCommand.TURN -> "${this.index}. Поворот"
                }
            }
            setOnKeyPressed { keyEvent: KeyEvent ->
                val targetPosition = if (list.selectionModel.selectedIndices.isEmpty()) {
                    program.lastIndex
                } else {
                    list.selectionModel.selectedIndices[0]
                }
                when (keyEvent.code) {
                    KeyCode.F2 -> {
                        program.add(targetPosition, AtomicCommand.JUMP)
                        list.selectionModel.select(targetPosition + 1)
                    }
                    KeyCode.F3 -> {
                        program.add(targetPosition, AtomicCommand.STEP)
                        list.selectionModel.select(targetPosition + 1)
                    }
                    KeyCode.F4 -> {
                        program.add(targetPosition, AtomicCommand.TURN)
                        list.selectionModel.select(targetPosition + 1)
                    }
                    KeyCode.DELETE -> if (targetPosition != program.lastIndex) {
                        program.removeAt(targetPosition)
                        list.selectionModel.select(targetPosition)
                    }
                }
            }
        }
    }

    init {
        program.add(null)
    }

}