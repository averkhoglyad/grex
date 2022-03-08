package net.averkhoglyad.grex.arrow.gui.view

import javafx.beans.property.SimpleListProperty
import javafx.collections.ObservableList
import javafx.scene.control.ListView
import javafx.scene.control.SelectionModel
import javafx.scene.input.KeyCode
import net.averkhoglyad.grex.arrow.gui.data.*
import net.averkhoglyad.grex.arrow.gui.fragment.ProgramLineFragment
import tornadofx.*

class ProgramView : View() {

    private val programProperty = SimpleListProperty<ProgramLine>(observableListOf())
    var program: ObservableList<ProgramLine?> by programProperty

    override val root = hbox {
        listview(program) {
            prefWidth = 200.0
            cellFragment(ProgramLineFragment::class)
            setOnKeyPressed { evt ->
                handleKeyPress(selectionModel, evt.code)
            }
        }
    }

    init {
        program.add(null)
    }

    // TODO: Refactoring is needed
    private fun handleKeyPress(selectionModel: SelectionModel<ProgramLine?>, keyCode: KeyCode) {
        val targetPosition = selectionModel.selectedIndex.takeUnless { it < 0 } ?: program.lastIndex
        val currentLine = program[targetPosition]
        val targetLevel: Int = targetLevel(currentLine)

        when (keyCode) {
            KeyCode.F2 -> {
                program.add(targetPosition, JumpLine(targetLevel))
                selectionModel.select(targetPosition + 1)
            }
            KeyCode.F3 -> {
                program.add(targetPosition, StepLine(targetLevel))
                selectionModel.select(targetPosition + 1)
            }
            KeyCode.F4 -> {
                program.add(targetPosition, TurnLine(targetLevel))
                selectionModel.select(targetPosition + 1)
            }
            KeyCode.F5 -> {
                val ifLine = IfLine(null, targetLevel)
                program.add(targetPosition, ifLine)
                program.add(targetPosition + 1, EndBlockLine(ifLine))
                selectionModel.select(targetPosition + 1)
            }

            KeyCode.F6 -> {
                val blockBounds = detectBlockBoundsByPosition(targetPosition, targetLevel) ?: return
                val ifLine = program[blockBounds.first] as? IfLine ?: return
                val elseAlreadyExists = program.slice(blockBounds).any { it is ElseLine && it.start == ifLine }
                if (elseAlreadyExists) return
                program.add(targetPosition, ElseLine(ifLine))
                selectionModel.select(targetPosition + 1)
            }

            KeyCode.F7 -> {
                val whileLine = WhileLine(null, targetLevel)
                program.add(targetPosition, whileLine)
                program.add(targetPosition + 1, EndBlockLine(whileLine))
                selectionModel.select(targetPosition + 1)
            }

            KeyCode.F8 -> {
                // TODO: Define procedure
            }

            KeyCode.F9 -> {
                // TODO: Invoke procedure
            }

            KeyCode.DELETE -> {
                if (targetPosition < program.lastIndex) {
                    requestDeletion(targetPosition) {
                        selectionModel.select(it)
                    }
                }
            }
            KeyCode.BACK_SPACE -> {
                if (targetPosition > 0) {
                    requestDeletion(targetPosition - 1) {
                        selectionModel.select(it)
                    }
                }
            }
        }
    }

    private fun detectBlockBoundsByPosition(position: Int, blockLevel: Int): IntRange? {
        val endLine = program.asSequence()
            .drop(position)
            .filterNotNull()
            .find { (it.level == (blockLevel - 1)) && it is IEndBlockLine }
        return endLine?.let {
            val startLine = (endLine as IEndBlockLine).start
            program.indexOf(startLine)..program.indexOf(it)
        }
    }

    private fun targetLevel(currentLine: ProgramLine?): Int {
        return when (currentLine) {
            is IEndBlockLine -> currentLine.level + 1
            else -> currentLine?.level ?: 0
        }
    }

    private fun requestDeletion(position: Int, fn: (Int) -> Unit = {}) {
        // TODO: Find solution for blocks deletion
        program.removeAt(position)
        fn(position)
    }
}