package io.github.chrislo27.dotmatrix

import io.github.chrislo27.dotmatrix.img.Color
import io.github.chrislo27.dotmatrix.img.Image
import io.github.chrislo27.dotmatrix.*
import java.awt.image.BufferedImage


actual fun dotMtxFontConvertImage(img: Image): Image {
    return Image(BufferedImage(img.width, img.height, BufferedImage.TYPE_INT_ARGB).apply {
        val g = createGraphics()
        g.drawImage(img.backing, 0, 0, null)
        g.dispose()
    })
}

actual fun DotMtxFont.getGlyphSubimage(glyph: Glyph, color: Color): Image {
    val c = glyph.character
    val subimage = if (c in '\uE000'..'\uE00F') (glyphRunCustomLines[c - '\uE000']) else subimageCache.getOrPut(glyph) {
        if (glyph !in allGlyphs) error("Glyph is not in font")
        SubimageCacheObj(Image(image.backing.getSubimage(glyph.x, glyph.y, glyph.w, glyph.h)), 0x00FFFFFF)
    }
    // Recolor if necessary
    val colorRgb: Int = color.rgb and 0x00FFFFFF
    if (colorRgb != subimage.color) {
        val replaceRGB: Int = subimage.color and 0x00FFFFFF
        val recolorRgb: Int = colorRgb or 0xFF000000.toInt()
        val w: Int = subimage.image.width
        val h: Int = subimage.image.height
        val rgb: IntArray = subimage.image.backing.getRGB(0, 0, w, h, null, 0, w)
        for (i in rgb.indices) {
            val px = rgb[i]
            if (px and 0x00FFFFFF == replaceRGB && ((px shr 24) and 0xFF) == 0xFF) {
                rgb[i] = recolorRgb
            }
        }
        subimage.image.backing.setRGB(0, 0, w, h, rgb, 0, w)
        subimage.color = colorRgb
    }
    return subimage.image
}