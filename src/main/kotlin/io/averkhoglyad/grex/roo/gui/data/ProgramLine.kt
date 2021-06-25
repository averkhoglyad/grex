package io.averkhoglyad.grex.roo.gui.data

import io.averkhoglyad.grex.roo.core.BorderCondition

sealed class ProgramLine {
    open fun isValid(): Boolean = true
}

class CommandLine()

object Jump : ProgramLine()

object Step : ProgramLine()

object Turn : ProgramLine()

class IfLine: ProgramLine() {
    var condition: BorderCondition? = null
    var trueBlock: List<ProgramLine> = emptyList()
    var falseBlock: List<ProgramLine> = emptyList()
}

class WhileLine: ProgramLine() {
    var condition: BorderCondition? = null
    var lines: List<ProgramLine> = emptyList()
}

//class Invoke {
//    val procedure: String
//}
//
//class Define {
//    val procedure: String
//    val lines: List<ProgramLine>
//}
