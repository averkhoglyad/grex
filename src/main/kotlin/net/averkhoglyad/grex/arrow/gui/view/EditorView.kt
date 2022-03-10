package net.averkhoglyad.grex.arrow.gui.view

import javafx.beans.property.SimpleListProperty
import javafx.collections.ObservableList
import javafx.scene.control.SelectionModel
import javafx.scene.input.KeyCode
import net.averkhoglyad.grex.arrow.gui.data.*
import net.averkhoglyad.grex.arrow.gui.fragment.EditorLineFragment
import tornadofx.*

class EditorView : View() {

    private val programProperty = SimpleListProperty<ProgramLine>(observableListOf())
    var program: ObservableList<ProgramLine?> by programProperty

    private var blockDeletionPolicy: DeletionPolicy? = null
    private var blockDeletionRange: IntRange? = null

    override val root = hbox {
        listview(program) {
            prefWidth = 200.0
            cellFragment(EditorLineFragment::class)
            setOnKeyPressed { evt ->
                handleKeyPress(selectionModel, evt.code)
            }
            selectionModel.selectedIndexProperty()
                .onChange { rejectBlockDeletion() }
        }
    }

    init {
        program.add(null)
    }

    // TODO: Refactoring is needed!!!
    private fun handleKeyPress(selectionModel: SelectionModel<ProgramLine?>, keyCode: KeyCode) {
        val targetPosition = selectionModel.selectedIndex.takeUnless { it < 0 } ?: program.lastIndex
        val targetLevel: Int = targetLevel(program[targetPosition])

        if (this.blockDeletionPolicy != null) {
            when (keyCode) {
                KeyCode.ENTER -> {
                    program.slice(blockDeletionRange!!)
                        .asSequence()
                        .filterNotNull()
                        .filterNot { it.deletion }
                        .forEach { it.level -= 1 }
                    val linesToDelete = program.slice(blockDeletionRange!!)
                        .filter { it?.deletion ?: false }
                    rejectBlockDeletion()
                    program.removeAll(linesToDelete)
                }
                KeyCode.ESCAPE -> {
                    rejectBlockDeletion()
                }
                KeyCode.DELETE -> {
                    switchBlockDeletionMode()
                }
                KeyCode.BACK_SPACE -> {
                    switchBlockDeletionMode()
                }
            }
        } else {
            when (keyCode) {
                KeyCode.F2 -> { // Step
                    addProgramLine(targetPosition, StepLine(targetLevel))
                    selectionModel.select(targetPosition + 1)
                }
                KeyCode.F3 -> { // Jump
                    addProgramLine(targetPosition, JumpLine(targetLevel))
                    selectionModel.select(targetPosition + 1)
                }
                KeyCode.F4 -> { // Turn
                    addProgramLine(targetPosition, TurnLine(targetLevel))
                    selectionModel.select(targetPosition + 1)
                }
                KeyCode.F5 -> { // If
                    val ifLine = IfLine(null, targetLevel)
                    addProgramLine(targetPosition, ifLine)
                    addProgramLine(targetPosition + 1, EndBlockLine(ifLine))
                    selectionModel.select(targetPosition + 1)
                }

                KeyCode.F6 -> { // Else
                    val blockBounds = detectBlockBoundsByPosition(targetPosition) ?: return
                    val ifLine = program[blockBounds.first] as? IfLine ?: return
                    val elseAlreadyExists = program.slice(blockBounds).any { it is ElseLine && it.start == ifLine }
                    if (elseAlreadyExists) return
                    addProgramLine(targetPosition, ElseLine(ifLine))
                    selectionModel.select(targetPosition + 1)
                }

                KeyCode.F7 -> { // While
                    val whileLine = WhileLine(null, targetLevel)
                    addProgramLine(targetPosition, whileLine)
                    addProgramLine(targetPosition + 1, EndBlockLine(whileLine))
                    selectionModel.select(targetPosition + 1)
                }

                KeyCode.F8 -> { // Define
                    val defineLine = DefineLine("", targetLevel)
                    addProgramLine(targetPosition, defineLine)
                    addProgramLine(targetPosition + 1, EndBlockLine(defineLine))
                    selectionModel.select(targetPosition + 1)
                }

                KeyCode.F9 -> { // Invoke
                    addProgramLine(targetPosition, InvokeLine("", targetLevel))
                    selectionModel.select(targetPosition + 1)
                }

                KeyCode.DELETE -> { // Delete
                    if (targetPosition < program.lastIndex) {
                        requestDeletion(targetPosition) {
                            selectionModel.select(it)
                        }
                    }
                }
                KeyCode.BACK_SPACE -> { // Backspace
                    if (targetPosition > 0) {
                        requestDeletion(targetPosition - 1) {
                            selectionModel.select(it)
                        }
                    }
                }
            }
        }
    }

    private fun rejectBlockDeletion() {
        blockDeletionPolicy = null
        blockDeletionRange?.let {
            program.slice(it).forEach { l -> l?.deletion = false }
        }
        blockDeletionRange = null
    }

    private fun switchBlockDeletionMode() {
        this.blockDeletionPolicy = this.blockDeletionPolicy?.switch()
        this.blockDeletionPolicy?.markLines(program.slice(blockDeletionRange!!))
    }

    private fun addProgramLine(position: Int, line: ProgramLine) {
        program.add(position, line)
    }

    private fun targetLevel(currentLine: ProgramLine?): Int {
        return when (currentLine) {
            is EndBlockLine -> currentLine.level + 1
            is ElseLine -> currentLine.level + 1
            else -> currentLine?.level ?: 0
        }
    }

    private fun detectBlockBoundsByPosition(position: Int): IntRange? {
        val currentLine = program[position] ?: return null
        val endLine: EndBlockLine?
        if (currentLine is EndBlockLine) {
            endLine = currentLine
        } else {
            val blockLevel = if (currentLine is BlockStart) currentLine.level else (currentLine.level - 1)
            endLine = program.asSequence()
                .drop(position)
                .filterIsInstance<EndBlockLine>()
                .find { it.level == blockLevel }
        }
        return endLine?.let { program.indexOf(it.start)..program.indexOf(it) }
    }

    private fun requestDeletion(position: Int, fn: (Int) -> Unit = {}) {
        if (program[position] is BlockStart) {
            this.blockDeletionRange = detectBlockBoundsByPosition(position)
            this.blockDeletionPolicy = DeletionPolicy.EXCLUDE
            this.blockDeletionPolicy?.markLines(program.slice(blockDeletionRange!!))
        } else {
            program.removeAt(position)
            fn(position)
        }
    }
}

private enum class DeletionPolicy {

    INCLUDE {
        override fun markLines(lines: List<ProgramLine?>) {
            lines.forEach { it?.deletion = true }
        }
    },

    EXCLUDE {
        override fun markLines(lines: List<ProgramLine?>) {
            lines.filterNotNull().let {
                val blockLevel = it.first().level
                it.forEach { line -> line.deletion = line.level == blockLevel }
            }
        }
    };

    abstract fun markLines(lines: List<ProgramLine?>)

    fun switch(): DeletionPolicy = when (this) {
        INCLUDE -> EXCLUDE
        EXCLUDE -> INCLUDE
    }
}
