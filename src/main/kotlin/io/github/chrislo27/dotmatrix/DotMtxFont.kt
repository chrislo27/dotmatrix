package io.github.chrislo27.dotmatrix

import io.github.chrislo27.dotmatrix.img.Color
import io.github.chrislo27.dotmatrix.img.Image
import kotlinx.serialization.json.*
import java.awt.image.BufferedImage


data class SubimageCacheObj(val image: Image, var color: Int = 0xFFFFFF)

class DotMtxFont(jsonData: String, inputImg: Image) {
    
    companion object {
        private fun convertImage(img: Image): Image {
            return Image(BufferedImage(img.width, img.height, BufferedImage.TYPE_INT_ARGB).apply {
                val g = createGraphics()
                g.drawImage(img.backing, 0, 0, null)
                g.dispose()
            })
        }
    }
    
    val height: Int
    val desc: String
    val advance: Int
    val image: Image
    val glyphs: Map<Char, Glyph>
    val allGlyphs: Set<Glyph>
    val subimageCache: MutableMap<Glyph, SubimageCacheObj> = mutableMapOf()
    
    init {
        val jsonObj = Json.parseToJsonElement(jsonData).jsonObject
        height = jsonObj["height"]!!.jsonPrimitive.int
        desc = jsonObj["desc"]?.jsonPrimitive?.contentOrNull ?: ""
        advance = jsonObj["advance"]!!.jsonPrimitive.int
        val glyphsNode = jsonObj["glyphs"]!!.jsonArray
        glyphs = glyphsNode.map { element ->
            val o = element.jsonObject
            Glyph(o["char"]!!.jsonPrimitive.content[0], o["x"]!!.jsonPrimitive.int, o["y"]?.jsonPrimitive?.int ?: 0, o["w"]!!.jsonPrimitive.int, o["h"]?.jsonPrimitive?.int ?: height, o["advance"]?.jsonPrimitive?.int ?: advance)
        }.associateBy { it.character }
        allGlyphs = glyphs.values.toHashSet()
        this.image = convertImage(inputImg)
        
        // Check bounds for each glyph
        glyphs.values.forEach { g ->
            if (g.x < 0 || (g.x + g.w) > inputImg.width || g.y < 0 || (g.y + g.h) > inputImg.height) {
                error("Glyph '${g.character}' is out of bounds: {${g.x}, ${g.y}, ${g.w}, ${g.h}}, image size ${inputImg.width} x ${inputImg.height}")
            }
        }
    }

    fun getGlyphSubimage(glyph: Glyph, color: Color): Image {
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
    
}

