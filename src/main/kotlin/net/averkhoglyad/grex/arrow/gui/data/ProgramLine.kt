package net.averkhoglyad.grex.arrow.gui.data

import javafx.beans.property.ReadOnlyBooleanWrapper
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.value.ObservableBooleanValue
import net.averkhoglyad.grex.arrow.core.program.BorderCondition
import tornadofx.property
import tornadofx.toProperty

// Base
private val TRUE_WRAPPER = ReadOnlyBooleanWrapper(true).readOnlyProperty
abstract sealed class ProgramLine(var level: Int) {
    open val isValidProperty: ObservableBooleanValue = TRUE_WRAPPER
}

// Actions
class JumpLine(padding: Int) : ProgramLine(padding)

class StepLine(padding: Int) : ProgramLine(padding)

class TurnLine(padding: Int) : ProgramLine(padding)

// Control Commands

class IfLine(condition: BorderCondition?, padding: Int) : ConditionalLine(condition, padding)

class ElseLine(override val start: IfLine) : ProgramLine(start.level), IEndBlockLine

class EndBlockLine(override val start: ProgramLine) : ProgramLine(start.level), IEndBlockLine

interface IEndBlockLine {
    val start: ProgramLine
}

class WhileLine(condition: BorderCondition?, padding: Int) : ConditionalLine(condition, padding)

abstract class ConditionalLine(condition: BorderCondition?, padding: Int) : ProgramLine(padding) {
    val conditionProperty = SimpleObjectProperty<BorderCondition?>(condition)
    var condition: BorderCondition? by property { conditionProperty }
    override val isValidProperty: ObservableBooleanValue = conditionProperty.isNotNull()
}

//class Define, BlockLine {
//    val procedure: String
//}

//class Invoke {
//    val procedure: String
//}

// TODO: idea must be failed!!!
// Blocks
//sealed class ProgramBlock {
//    abstract val parent: ProgramBlock?
//    abstract val offset: Number
//    fun child(): ProgramBlock = SubProgramBlock(this)
//    fun sibling(): ProgramBlock = SubProgramBlock(this.parent!!)
//}
//
//class SubProgramBlock(parent: ProgramBlock) : ProgramBlock() {
//    private val parentProperty: SimpleObjectProperty<ProgramBlock?> = SimpleObjectProperty<ProgramBlock?>(parent)
//    override val parent: ProgramBlock? by property { parentProperty }
//
//    private val offsetProperty = SimpleIntegerProperty(0)
//    override val offset: Number by property { offsetProperty }
//
//    init {
//        parentProperty.onChange {
//            offsetProperty.set((it?.offset?.toInt() ?: 0) + 1)
//        }
//    }
//}
//
//object RootProgramBlock : ProgramBlock() {
//    override val parent: ProgramBlock? = null
//    override val offset: Number = 0
//}
