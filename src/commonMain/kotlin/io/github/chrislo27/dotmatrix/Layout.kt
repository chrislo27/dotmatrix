package io.github.chrislo27.dotmatrix

import io.github.chrislo27.dotmatrix.img.Color
import io.github.chrislo27.dotmatrix.img.Image
import kotlin.math.absoluteValue

expect val glyphRunCustomLines: Array<SubimageCacheObj>

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
        var hairSpaces = 0
        var customY = 0
        glyphPositions = text.mapNotNull { c ->
            val glyph = font.glyphs[c]
            if (c == '\u200A') {
                // Hair space -- advances by one pixel only
                currentX++
                lastAdv = 1
                if (customY == 0) hairSpaces++
                customY = 0
                null
            } else if (c == '\u0015') {
                // Negative-ack -- advances by negative one pixel only
                currentX--
                lastAdv--
                null
            } else if (c in '\uE000'..'\uE00F') {
                // These are the first 16 Unicode Private Use Area characters. It represents a 4 bit pattern of on/off
                // pixels going down
                customY += 4
                lastAdv = 0
                GlyphPosition(Glyph(c, 0, 0, 0, 0, 0), currentX, customY - 4)
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
        width = (((glyphPositions.map { it.x + it.glyph.w }.maxOrNull() ?: 0) - (glyphPositions.map { it.x }.minOrNull() ?: 0)).absoluteValue + hairSpaces).coerceAtLeast(0)
        lastAdvance = lastAdv
    }
}

expect fun GlyphRun.toImage(): Image

data class GlyphPosition(val glyph: Glyph, val x: Int, val y: Int)

data class GlyphLayout(val runs: List<GlyphRun>, val glyphAlign: List<VerticalAlignment>, val horizontalAlign: TextAlignment) {

    constructor(runs: List<GlyphRun>, glyphAlign: VerticalAlignment = VerticalAlignment.BOTTOM, horizontalAlign: TextAlignment = TextAlignment.CENTRE)
            : this(runs, if (runs.isEmpty()) emptyList() else List(runs.size) { glyphAlign }, horizontalAlign)

    val width: Int = if (runs.isEmpty()) 0 else (runs.sumBy { r ->
        r.width + r.lastAdvance
    } - runs.last().lastAdvance)
    val height: Int = runs.maxByOrNull { it.height }?.height ?: 0
    
}

expect fun GlyphLayout.toImage(): Image

data class LayoutLines(val lines: List<GlyphLayout>, val lineSpacing: LineSpacing = LineSpacing.FLUSH_TO_EDGES) {

    val totalWidth: Int = lines.maxByOrNull { it.width }?.width ?: 0
    val totalHeight: Int = lines.sumBy { it.height }
    
    fun determineLayoutX(imageWidth: Int, layout: GlyphLayout): Int {
        return when (layout.horizontalAlign) {
            TextAlignment.CENTRE -> (imageWidth - layout.width) / 2
            TextAlignment.LEFT -> 0
            TextAlignment.RIGHT -> imageWidth - layout.width
        }
    }

}

expect fun LayoutLines.toImage(width: Int, height: Int): Image
