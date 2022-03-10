package net.averkhoglyad.grex.arrow.core.program

import net.averkhoglyad.grex.framework.board.Board
import net.averkhoglyad.grex.framework.code.*
import net.averkhoglyad.grex.framework.execution.Program
import net.averkhoglyad.grex.framework.program.ProgramBuilder
import net.averkhoglyad.grex.framework.program.TopProgramBuilder
import net.averkhoglyad.grex.framework.program.program

fun compile(board: Board, fn: (CodeTreeBuilder) -> Unit): Program {
    val builder = TopLevelCodeTreeBuilderImpl()
    fn(builder)
    val codeTree: List<CodeTreeEntry> = builder.build()
    val program = program(board) {
        codeTree.forEach { it.apply(this@program) }
    }
    return program
}

/**
 * Flat program builder
 */
interface CodeTreeBuilder {

    // Commands
    fun step(): CodeTreeBuilder
    fun jump(): CodeTreeBuilder
    fun turn(): CodeTreeBuilder

    // Procedure aware instructions
    fun define(name: String): CodeTreeBuilder = throw CompilationException("Procedure can't be defined here")
    fun invoke(name: String): CodeTreeBuilder

    // Control blocks
    fun ifBlock(condition: ExecutionFrameCondition): CodeTreeBuilder
    fun elseBlock(): CodeTreeBuilder = throw CompilationException("Else is not supported")
    fun whileBlock(condition: ExecutionFrameCondition): CodeTreeBuilder

    // Close code block
    fun close(): CodeTreeBuilder = throw CompilationException("Block closing is not supported")
}

private interface CodeTreeBuilderInner : CodeTreeBuilder {
    operator fun plusAssign(entry: CodeTreeEntry)
}

private open class CommonProgramBuilderImpl : CodeTreeBuilderInner {

    private val code: MutableList<CodeTreeEntry> = mutableListOf()

    override fun step(): CodeTreeBuilder {
        this += CommandEntry(ArrowCommand.Step)
        return this
    }

    override fun jump(): CodeTreeBuilder {
        this += CommandEntry(ArrowCommand.Jump)
        return this
    }

    override fun turn(): CodeTreeBuilder {
        this += CommandEntry(ArrowCommand.Turn)
        return this
    }

    override fun invoke(name: String): CodeTreeBuilder {
        this += InvokeEntry(name)
        return this
    }

    override fun ifBlock(condition: ExecutionFrameCondition): CodeTreeBuilder = IfBlockBuilder(this, condition)

    override fun whileBlock(condition: ExecutionFrameCondition): CodeTreeBuilder =
        WhileBlockBuilder(this, condition)

    override fun plusAssign(entry: CodeTreeEntry) {
        code += entry
    }

    fun build() = code.toList()
}

private class TopLevelCodeTreeBuilderImpl : CommonProgramBuilderImpl(), CodeTreeBuilderInner  {
    override fun define(name: String): CodeTreeBuilder  = DefineBlockBuilder(this, name)
}

private class DefineBlockBuilder(private val parent: CodeTreeBuilderInner,
                                 private val name: String) : CommonProgramBuilderImpl() {
    private val code: MutableList<CodeTreeEntry> = mutableListOf()

    override fun plusAssign(entry: CodeTreeEntry) {
        code += entry
    }

    override fun close(): CodeTreeBuilder {
        parent += DefineEntry(name, code)
        return parent
    }
}

private class IfBlockBuilder(private val parent: CodeTreeBuilderInner,
                             private val condition: ExecutionFrameCondition) : CommonProgramBuilderImpl() {

    private val trueCode: MutableList<CodeTreeEntry> = mutableListOf()
    private val falseCode: MutableList<CodeTreeEntry> = mutableListOf()
    private var codeState = true

    override fun elseBlock(): CodeTreeBuilder {
        when (codeState) {
            true -> codeState = false
            false -> throw CompilationException("Else is not supported")
        }
        return this
    }

    override fun plusAssign(entry: CodeTreeEntry) {
        when (codeState) {
            true -> trueCode += entry
            false -> falseCode += entry
        }
    }

    override fun close(): CodeTreeBuilder {
        parent += IfEntry(condition, trueCode, falseCode)
        return parent
    }
}

private class WhileBlockBuilder(private val parent: CodeTreeBuilderInner,
                                private val condition: ExecutionFrameCondition) : CommonProgramBuilderImpl() {

    private val code: MutableList<CodeTreeEntry> = mutableListOf()

    override fun plusAssign(entry: CodeTreeEntry) {
        code += entry
    }

    override fun close(): CodeTreeBuilder {
        parent += WhileEntry(condition, code)
        return parent
    }
}

private interface CodeTreeEntry {
    fun apply(sb: ProgramBuilder)
}

private class IfEntry(private val condition: ExecutionFrameCondition,
                      private val trueCode: List<CodeTreeEntry>,
                      private val falseCode: List<CodeTreeEntry>) : CodeTreeEntry {
    override fun apply(sb: ProgramBuilder) {
        sb.doIf(condition) {
            trueCode.forEach { it.apply(this) }
        } otherwise {
            falseCode.forEach { it.apply(this) }
        }
    }
}

private class WhileEntry(private val condition: ExecutionFrameCondition,
                         private val code: List<CodeTreeEntry>) : CodeTreeEntry {
    override fun apply(sb: ProgramBuilder) {
        sb.repeatWhile(condition) {
            code.forEach { it.apply(this) }
        }
    }
}

private class DefineEntry(private val name: String,
                          private val code: List<CodeTreeEntry>) : CodeTreeEntry {
    override fun apply(sb: ProgramBuilder) {
        (sb as TopProgramBuilder).define(name) {
            code.forEach { it.apply(this) }
        }
    }
}

private class InvokeEntry(private val name: String) : CodeTreeEntry {
    override fun apply(sb: ProgramBuilder) {
        sb.invoke(name)
    }
}

private class CommandEntry(private val command: ArrowCommand) : CodeTreeEntry {
    override fun apply(sb: ProgramBuilder) {
        sb.command(command::exec)
    }
}
