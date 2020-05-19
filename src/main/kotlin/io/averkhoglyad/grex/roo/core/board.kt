package io.averkhoglyad.grex.roo.core

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

fun RooBoard.writeTo(file: File) {
    writeTo(file.toPath())
}

fun RooBoard.writeTo(path: Path) {
    Files.newOutputStream(path).use {
        writeTo(it)
    }
}

fun RooBoard.writeTo(out: OutputStream) {
    ImageIO.write(render(), "png", out)
}

private fun RooBoard.render(): RenderedImage {
    val width = 2 * PADDING + this.size.first * STEP
    val height = 2 * PADDING + this.size.second * STEP
    val img = BufferedImage(width, height, BufferedImage.TYPE_INT_RGB)
    val graphics: Graphics2D = img.createGraphics()
    this.units
            .mapNotNull { it as? Line }
            .forEach { graphics.drawLine(it) }
    return img
}

private fun Graphics2D.drawLine(line: Line) {
    val (from, to) = line
    drawLine(from.x * STEP + PADDING, from.y * STEP + PADDING, to.x * STEP + PADDING, to.y * STEP + PADDING)
}
