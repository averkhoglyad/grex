package net.averkhoglyad.grex.arrow.core.model

import net.averkhoglyad.grex.framework.board.Board
import net.averkhoglyad.grex.framework.board.lookup
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

fun Board.writeTo(file: File) {
    writeTo(file.toPath())
}

fun Board.writeTo(path: Path) {
    Files.newOutputStream(path).use {
        writeTo(it)
    }
}

fun Board.writeTo(out: OutputStream) {
    ImageIO.write(render(), "png", out)
}

private fun Board.render(): RenderedImage {
    val width = 2 * PADDING + this.size.first * STEP
    val height = 2 * PADDING + this.size.second * STEP
    val img = BufferedImage(width, height, BufferedImage.TYPE_INT_RGB)
    val graphics: Graphics2D = img.createGraphics()
    this.lookup<Line>()
        .forEach { graphics.drawLine(it) }
    return img
}

private fun Graphics2D.drawLine(line: Line) {
    val (from, to) = line
    drawLine(from.x * STEP + PADDING, from.y * STEP + PADDING, to.x * STEP + PADDING, to.y * STEP + PADDING)
}
