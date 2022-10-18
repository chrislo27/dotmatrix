package io.github.chrislo27.dotmatrix

import io.github.chrislo27.dotmatrix.img.Color
import io.github.chrislo27.dotmatrix.img.Image
import java.awt.image.BufferedImage
import java.awt.image.ImageObserver
import kotlin.math.absoluteValue
import kotlin.math.roundToInt

val glyphRunCustomLines: Array<SubimageCacheObj> by lazy {
    Array(16) { i ->
        SubimageCacheObj(Image(BufferedImage(1, 4, BufferedImage.TYPE_4BYTE_ABGR).apply {
            for (y in 0 until 4) {
                setRGB(0, y, if ((i ushr y) and 1 == 1) Color.WHITE.rgb else 0)
            }
        }), 0x00FFFFFF)
    }
}

class GlyphRun(val font: DotMtxFont, val text: String, val color: Color) {
    
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
        var hairSpacesAtEnd = 0
        var customY = 0
        glyphPositions = text.mapNotNull { c ->
            val glyph = font.glyphs[c]
            if (c == '\u200A') {
                // Hair space -- advances by one pixel only
                currentX++
                lastAdv = 1
                if (customY == 0) hairSpacesAtEnd++
                customY = 0
                null
            } else {
                hairSpacesAtEnd = 0
                if (c == '\u0015') {
                    // Negative-ack -- advances by negative one pixel only
                    currentX--
                    lastAdv--
                    null
                } else if (c in '\uE000'..'\uE00F') {
                    // These are the first 16 Unicode Private Use Area characters. It represents a 4 bit pattern of on/off
                    // pixels going down
                    customY += 4
                    lastAdv = 0
                    GlyphPosition(Glyph(c, 0, 0, 1, 0, 0), currentX, customY - 4)
                } else if (glyph == null) {
                    missingChars.add(c)
                    null
                } else {
                    GlyphPosition(glyph, currentX, height - glyph.h).apply {
                        lastAdv = glyph.advance
                        currentX += glyph.w + glyph.advance
                    }
                }
            }
        }
        width = (((glyphPositions.maxOfOrNull { it.x + it.glyph.w } ?: 0) - (glyphPositions.minOfOrNull { it.x } ?: 0)).absoluteValue + hairSpacesAtEnd).coerceAtLeast(0)
        lastAdvance = lastAdv
    }
    
    fun toImage(): Image {
        return Image(BufferedImage(width.coerceAtLeast(1), height, BufferedImage.TYPE_4BYTE_ABGR).apply {
            val g = createGraphics()
            glyphPositions.forEach { glyphPos ->
                val glyph = glyphPos.glyph
                g.drawImage(font.getGlyphSubimage(glyph, color).backing, glyphPos.x, glyphPos.y, null as ImageObserver?)
            }
            g.dispose()
        })
    }
}

data class GlyphPosition(val glyph: Glyph, val x: Int, val y: Int)

data class GlyphLayout(val runs: List<GlyphRun>, val glyphAlign: List<VerticalAlignment>, val horizontalAlign: TextAlignment) {

    constructor(runs: List<GlyphRun>, glyphAlign: VerticalAlignment = VerticalAlignment.BOTTOM, horizontalAlign: TextAlignment = TextAlignment.CENTRE)
            : this(runs, if (runs.isEmpty()) emptyList() else List(runs.size) { glyphAlign }, horizontalAlign)

    val width: Int = if (runs.isEmpty()) 0 else (runs.sumOf { r ->
        r.width + r.lastAdvance
    } - runs.last().lastAdvance)
    val height: Int = runs.maxByOrNull { it.height }?.height ?: 0
    
    fun toImage(): Image {
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
}

data class LayoutLines(val lines: List<GlyphLayout>, val lineSpacing: LineSpacing = LineSpacing.FLUSH_TO_EDGES) {

    val totalWidth: Int = lines.maxByOrNull { it.width }?.width ?: 0
    val totalHeight: Int = lines.sumOf { it.height }
    
    fun determineLayoutX(imageWidth: Int, layout: GlyphLayout): Int {
        return when (layout.horizontalAlign) {
            TextAlignment.CENTRE -> (imageWidth - layout.width) / 2
            TextAlignment.LEFT -> 0
            TextAlignment.RIGHT -> imageWidth - layout.width
        }
    }

    fun toImage(width: Int, height: Int): Image {
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
}
