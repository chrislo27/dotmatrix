package io.github.chrislo27.dotmatrix

import java.awt.image.BufferedImage
import java.awt.image.ImageObserver
import kotlin.math.absoluteValue


class GlyphLayout(val font: DotMtxFont, val text: String) {

    val missingChars: Set<Char> = mutableSetOf()
    val glyphPositions: List<GlyphPosition>
    val width: Int
    val height: Int = font.height

    init {
        missingChars as MutableSet
        val height = font.height
        var currentX = 0
        glyphPositions = text.mapNotNull { c ->
            val glyph = font.glyphs[c]
            if (glyph == null) {
                missingChars.add(c)
                null
            } else {
                GlyphPosition(glyph, currentX, height - glyph.h).apply {
                    currentX += glyph.w + glyph.advance
                }
            }
        }
        width = ((glyphPositions.map { it.x + it.glyph.w }.max() ?: 0) - (glyphPositions.map { it.x }.min() ?: 0)).absoluteValue
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

}

data class GlyphPosition(val glyph: Glyph, val x: Int, val y: Int)
