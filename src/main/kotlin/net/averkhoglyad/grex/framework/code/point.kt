package net.averkhoglyad.grex.framework.code

abstract sealed class CodePoint

class EmptyPoint : CodePoint() // Usually used as GoTo target point

class GoTo(val point: CodePoint) : CodePoint()

class ConditionalGoTo(val condition: Condition<ExecutionFrame>, val point: CodePoint) : CodePoint()

// Not Sure is it necessary to divide internal and external exec point and
abstract class ExecPoint(val exec: ExecutionFrame.() -> Unit) : CodePoint()
class InternalExecPoint(exec: ExecutionFrame.() -> Unit) : ExecPoint(exec) // Internal use
class CommandPoint(exec: ExecutionFrame.() -> Unit) : ExecPoint(exec) // External use a.k.a. Commands

class Invoke(val invokePoint: CodePoint, val returnPoint: CodePoint) : CodePoint()

object Return : CodePoint()
