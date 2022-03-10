package net.averkhoglyad.grex.arrow.gui.data

import javafx.beans.property.ReadOnlyBooleanWrapper
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.value.ObservableBooleanValue
import net.averkhoglyad.grex.arrow.core.program.BorderCondition
import tornadofx.getValue
import tornadofx.property
import tornadofx.setValue

// Base
private val TRUE_WRAPPER = ReadOnlyBooleanWrapper(true).readOnlyProperty
abstract sealed class ProgramLine(var level: Int) {
    open val isValidProperty: ObservableBooleanValue = TRUE_WRAPPER
    val deletionProperty = SimpleBooleanProperty(false)
    var deletion by deletionProperty
}

// Marker interface
interface BlockStart

// Commands
class JumpLine(level: Int) : ProgramLine(level)

class StepLine(level: Int) : ProgramLine(level)

class TurnLine(level: Int) : ProgramLine(level)

// Control instructions
class IfLine(condition: BorderCondition?, level: Int) : ConditionalLine(condition, level), BlockStart

class ElseLine(val start: IfLine) : ProgramLine(start.level), BlockStart

class EndBlockLine(val start: ProgramLine) : ProgramLine(start.level), BlockStart

class WhileLine(condition: BorderCondition?, level: Int) : ConditionalLine(condition, level), BlockStart

abstract class ConditionalLine(condition: BorderCondition?, level: Int) : ProgramLine(level) {
    val conditionProperty = SimpleObjectProperty<BorderCondition?>(condition)
    var condition: BorderCondition? by property { conditionProperty }
    override val isValidProperty: ObservableBooleanValue = conditionProperty.isNotNull()
}

// Procedures
class Define(val name: String, level: Int) : ProgramLine(level), BlockStart

class Invoke (val name: String, level: Int) : ProgramLine(level), BlockStart
