package io.github.chrislo27.dotmatrix

import java.awt.image.BufferedImage
import java.awt.image.ImageObserver
import kotlin.math.absoluteValue
import kotlin.math.roundToInt


class GlyphRun(val font: DotMtxFont, val text: String) {
    
    val missingChars: Set<Char> = mutableSetOf()
    val glyphPositions: List<GlyphPosition>
    val width: Int
    val height: Int = font.height
    val lastAdvance: Int
    
    init {
        missingChars as MutableSet
        val height = font.height
        var currentX = 0
        var lastAdv = 0
        glyphPositions = text.mapNotNull { c ->
            val glyph = font.glyphs[c]
            if (glyph == null) {
                missingChars.add(c)
                null
            } else {
                GlyphPosition(glyph, currentX, height - glyph.h).apply {
                    lastAdv = glyph.advance
                    currentX += glyph.w + glyph.advance
                }
            }
        }
        width = ((glyphPositions.map { it.x + it.glyph.w }.max() ?: 0) - (glyphPositions.map { it.x }.min() ?: 0)).absoluteValue
        lastAdvance = lastAdv
    }
    
    fun toBufferedImage(): BufferedImage {
        val subimageCache: MutableMap<Glyph, BufferedImage> = mutableMapOf()
        return BufferedImage(width.coerceAtLeast(1), height, BufferedImage.TYPE_4BYTE_ABGR).apply {
            val g = createGraphics()
            glyphPositions.forEach { glyphPos ->
                val glyph = glyphPos.glyph
                val subimage = subimageCache.getOrPut(glyph) {
                    font.image.getSubimage(glyph.x, glyph.y, glyph.w, glyph.h)
                }
                g.drawImage(subimage, glyphPos.x, glyphPos.y, null as ImageObserver?)
            }
            g.dispose()
        }
    }
    
    data class GlyphPosition(val glyph: Glyph, val x: Int, val y: Int)
    
}

data class GlyphLayout(val runs: List<GlyphRun>, val glyphAlign: List<VerticalAlignment>, val horizontalAlign: TextAlignment) {
    
    constructor(runs: List<GlyphRun>, glyphAlign: VerticalAlignment = VerticalAlignment.BOTTOM, horizontalAlign: TextAlignment = TextAlignment.CENTRE)
            : this(runs, if (runs.isEmpty()) emptyList() else List(runs.size) { glyphAlign }, horizontalAlign)
    
    val width: Int = if (runs.isEmpty()) 0 else (runs.sumBy { r ->
        r.width + r.lastAdvance
    } - runs.last().lastAdvance)
    val height: Int = runs.maxBy { it.height }?.height ?: 0
    
    fun toBufferedImage(): BufferedImage {
        if (width == 0 || height == 0) return BufferedImage(1, 1, BufferedImage.TYPE_4BYTE_ABGR)
        return BufferedImage(width, height, BufferedImage.TYPE_4BYTE_ABGR).apply {
            val g = createGraphics()
            var x = 0
            runs.forEachIndexed { i, run ->
                val runImg = run.toBufferedImage()
                val y: Int = when (glyphAlign.getOrElse(i.coerceAtMost(glyphAlign.size - 1)) { VerticalAlignment.CENTRE }) {
                    VerticalAlignment.CENTRE -> (height - runImg.height) / 2
                    VerticalAlignment.TOP -> 0
                    VerticalAlignment.BOTTOM -> height - runImg.height
                }
                g.drawImage(runImg, x, y, null as ImageObserver?)
                x += run.width + run.lastAdvance
            }
            g.dispose()
        }
    }
}

data class LayoutLines(val lines: List<GlyphLayout>, val lineSpacing: LineSpacing = LineSpacing.FLUSH_TO_EDGES) {
    
    val totalWidth: Int = lines.maxBy { it.width }?.width ?: 0
    val totalHeight: Int = lines.sumBy { it.height }
    
    fun toBufferedImage(width: Int, height: Int): BufferedImage {
        if (width == 0 || height == 0) error("Width and height must each be greater than zero")
        return BufferedImage(width, height, BufferedImage.TYPE_4BYTE_ABGR).apply {
            val g = createGraphics()
            fun GlyphLayout.determineX(): Int {
                return when (this.horizontalAlign) {
                    TextAlignment.CENTRE -> (width - this.width) / 2
                    TextAlignment.LEFT -> 0
                    TextAlignment.RIGHT -> width - this.width
                }
            }
            if (lines.size == 1) {
                val layout = lines.first()
                val img = layout.toBufferedImage()
                g.drawImage(img, layout.determineX(), ((height - layout.height) / 2f).toInt(), null)
            } else {
                when (lineSpacing) {
                    LineSpacing.FLUSH_TO_EDGES -> {
                        val totalHeight = totalHeight.coerceAtLeast(1)
                        val spacing: Float = (height - totalHeight).toFloat() / (lines.size - 1).coerceAtLeast(1)
                        lines.fold(0f) { acc, layout ->
                            val y = acc.toInt()
                            g.drawImage(layout.toBufferedImage(), layout.determineX(), y, null)
                            acc + layout.height + spacing
                        }
                    }
                    LineSpacing.EQUISPACED -> {
                        val totalHeight = totalHeight.coerceAtLeast(1)
                        val spacing: Float = (height - totalHeight).toFloat() / (lines.size + 1).coerceAtLeast(1)
                        lines.fold(spacing) { acc, layout ->
                            val y = acc.toInt()
                            g.drawImage(layout.toBufferedImage(), layout.determineX(), y, null)
                            acc + layout.height + spacing
                        }
                    }
                }
            }
            g.dispose()
        }
    }
    
}
