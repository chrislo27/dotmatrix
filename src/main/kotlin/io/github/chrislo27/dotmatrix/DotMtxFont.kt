package io.github.chrislo27.dotmatrix

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import java.awt.Color
import java.awt.image.BufferedImage
import java.io.File
import java.io.InputStream
import javax.imageio.ImageIO


class DotMtxFont(jsonStream: InputStream, imgStream: InputStream) {
    
    val height: Int
    val desc: String
    val advance: Int
    val glyphs: Map<Char, Glyph>
    val image: BufferedImage
    private val allGlyphs: Set<Glyph>
    private val subimageCache: MutableMap<Glyph, SubimageCacheObj> = mutableMapOf()
    
    init {
        val jsonObj: JsonNode = ObjectMapper().readTree(jsonStream)
        height = jsonObj["height"].asInt()
        desc = jsonObj["desc"]?.asText("") ?: ""
        advance = jsonObj["advance"].asInt()
        val glyphsNode = jsonObj["glyphs"]
        glyphs = glyphsNode.map { gNode ->
            Glyph(gNode["char"].asText()[0], gNode["x"].asInt(), gNode["y"]?.asInt(0) ?: 0, gNode["w"].asInt(), gNode["h"]?.asInt(height) ?: height, gNode["advance"]?.asInt(advance) ?: advance)
        }.associateBy { it.character }
        allGlyphs = glyphs.values.toHashSet()
        val readImg = ImageIO.read(imgStream)
        image = BufferedImage(readImg.width, readImg.height, BufferedImage.TYPE_4BYTE_ABGR).apply { 
            val g = createGraphics()
            g.drawImage(readImg, 0, 0, null)
            g.dispose()
        }
        jsonStream.close()
        imgStream.close()
        
        // Check bounds for each glyph
        glyphs.values.forEach { g ->
            if (g.x < 0 || (g.x + g.w) > image.width || g.y < 0 || (g.y + g.h) > image.height) {
                error("Glyph '${g.character}' is out of bounds: {${g.x}, ${g.y}, ${g.w}, ${g.h}}, image size ${image.width} x ${image.height}")
            }
        }
    }
    
    fun getGlyphSubimage(glyph: Glyph, color: Color): BufferedImage {
        val c = glyph.character
        val subimage = if (c in '\uE000'..'\uE00F') (GlyphRun.customLines[c - '\uE000']) else subimageCache.getOrPut(glyph) {
            if (glyph !in allGlyphs) error("Glyph is not in font")
            SubimageCacheObj(image.getSubimage(glyph.x, glyph.y, glyph.w, glyph.h), 0x00FFFFFF)
        }
        // Recolor if necessary
        val colorRgb: Int = color.rgb and 0x00FFFFFF
        if (colorRgb != subimage.color) {
            val replaceRGB: Int = subimage.color and 0x00FFFFFF
            val recolorRgb: Int = colorRgb or 0xFF000000.toInt()
            val w: Int = subimage.image.width
            val h: Int = subimage.image.height
//            println("Replacing glyph '${c}' in font ${this.desc} because its color ${replaceRGB.toUInt().toString(16)} is not the wanted ${recolorRgb.toUInt().toString(16)}  [${glyph}]")
            val rgb: IntArray = subimage.image.getRGB(0, 0, w, h, null, 0, w)
            for (i in rgb.indices) {
                if (rgb[i] and 0x00FFFFFF == replaceRGB) {
                    rgb[i] = recolorRgb
                }
            }
            subimage.image.setRGB(0, 0, w, h, rgb, 0, w)
            subimage.color = colorRgb
        }
        return subimage.image
    }
    
}

data class SubimageCacheObj(val image: BufferedImage, var color: Int = 0xFFFFFF)
