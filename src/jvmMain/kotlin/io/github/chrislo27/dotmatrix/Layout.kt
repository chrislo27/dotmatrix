package io.github.chrislo27.dotmatrix

import io.github.chrislo27.dotmatrix.img.Color
import io.github.chrislo27.dotmatrix.img.Image
import io.github.chrislo27.dotmatrix.*
import java.awt.image.BufferedImage
import java.awt.image.ImageObserver
import kotlin.math.roundToInt


actual val glyphRunCustomLines: Array<SubimageCacheObj> by lazy {
    Array(16) { i ->
        SubimageCacheObj(Image(BufferedImage(1, 4, BufferedImage.TYPE_4BYTE_ABGR).apply {
            for (y in 0 until 4) {
                setRGB(0, y, if ((i ushr y) and 1 == 1) Color.WHITE.rgb else 0)
            }
        }), 0x00FFFFFF)
    }
}

actual fun GlyphRun.toImage(): Image {
    return Image(BufferedImage(width.coerceAtLeast(1), height, BufferedImage.TYPE_4BYTE_ABGR).apply {
        val g = createGraphics()
        glyphPositions.forEach { glyphPos ->
            val glyph = glyphPos.glyph
            g.drawImage(font.getGlyphSubimage(glyph, color).backing, glyphPos.x, glyphPos.y, null as ImageObserver?)
        }
        g.dispose()
    })
}

actual fun GlyphLayout.toImage(): Image {
    if (width == 0 || height == 0) return Image(BufferedImage(1, 1, BufferedImage.TYPE_4BYTE_ABGR))
    return Image(BufferedImage(width, height, BufferedImage.TYPE_4BYTE_ABGR).apply {
        val g = createGraphics()
        var x = 0
        runs.forEachIndexed { i, run ->
            val runImg = run.toImage().backing
            val y: Int = when (glyphAlign.getOrElse(i.coerceAtMost(glyphAlign.size - 1)) { VerticalAlignment.CENTRE }) {
                VerticalAlignment.CENTRE -> (height - runImg.height) / 2
                VerticalAlignment.TOP -> 0
                VerticalAlignment.BOTTOM -> height - runImg.height
            }
            g.drawImage(runImg, x, y, null as ImageObserver?)
            x += run.width + run.lastAdvance
        }
        g.dispose()
    })
}

actual fun LayoutLines.toImage(width: Int, height: Int): Image {
    if (width == 0 || height == 0) error("Width and height must each be greater than zero")
    return Image(BufferedImage(width, height, BufferedImage.TYPE_4BYTE_ABGR).apply {
        val g = createGraphics()
        if (lines.size == 1) {
            val layout = lines.first()
            val img = layout.toImage().backing
            g.drawImage(img, determineLayoutX(this.width, layout), ((height - layout.height) / 2f).roundToInt(), null)
        } else {
            when (lineSpacing) {
                LineSpacing.FLUSH_TO_EDGES -> {
                    val totalHeight = totalHeight.coerceAtLeast(1)
                    val spacing: Float = (height - totalHeight).toFloat() / (lines.size - 1).coerceAtLeast(1)
                    lines.fold(0f) { acc, layout ->
                        val y = acc.roundToInt()
                        g.drawImage(layout.toImage().backing, determineLayoutX(this.width, layout), y, null)
                        acc + layout.height + spacing
                    }
                }
                LineSpacing.EQUISPACED -> {
                    val totalHeight = totalHeight.coerceAtLeast(1)
                    val spacing: Float = (height - totalHeight).toFloat() / (lines.size + 1).coerceAtLeast(1)
                    lines.fold(spacing) { acc, layout ->
                        val y = acc.roundToInt()
                        g.drawImage(layout.toImage().backing, determineLayoutX(this.width, layout), y, null)
                        acc + layout.height + spacing
                    }
                }
            }
        }
        g.dispose()
    })
}

