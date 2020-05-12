package io.averkhoglyad.grex.roo.core

import io.averkhoglyad.grex.framework.Executor
import io.averkhoglyad.grex.framework.InMemoryExecutorImpl
import java.awt.Graphics2D
import java.awt.image.BufferedImage
import java.awt.image.RenderedImage
import java.io.File
import java.io.OutputStream
import java.nio.file.Files
import java.nio.file.Path
import javax.imageio.ImageIO

private const val PADDING = 10
private const val STEP = 10

class ImageFileExecutorImpl(private val root: InMemoryExecutorImpl<RooBoard, Roo, RooState>) : Executor by root {

    fun writeTo(file: File) {
        writeTo(file.toPath())
    }

    fun writeTo(path: Path) {
        Files.newOutputStream(path).use {
            writeTo(it)
        }
    }

    fun writeTo(out: OutputStream) {
        ImageIO.write(render(), "png", out)
    }

    private fun render(): RenderedImage {
        val width = 2 * PADDING + root.board.size.first * STEP
        val height = 2 * PADDING + root.board.size.second * STEP
        val img = BufferedImage(width, height, BufferedImage.TYPE_INT_RGB)
        val graphics: Graphics2D = img.createGraphics()
        root.board.units
                .mapNotNull { it as? Line }
                .forEach { graphics.drawLine(it) }
        return img
    }

}

private fun Graphics2D.drawLine(line: Line) {
    val (from, to) = line
    drawLine(from.x * STEP + PADDING, from.y * STEP + PADDING, to.x * STEP + PADDING, to.y * STEP + PADDING)
}
