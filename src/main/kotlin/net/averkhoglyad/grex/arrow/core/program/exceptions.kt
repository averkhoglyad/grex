package net.averkhoglyad.grex.arrow.core.program

import net.averkhoglyad.grex.framework.code.ExecutionException

class CollisionException : ExecutionException("Border collision")

class CompilationException(message: String): Exception(message)
