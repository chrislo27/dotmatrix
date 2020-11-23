package io.github.chrislo27.dotmatrix

import io.github.chrislo27.dotmatrix.img.Color
import io.github.chrislo27.dotmatrix.img.Image
import kotlinx.serialization.json.*



data class SubimageCacheObj(val image: Image, var color: Int = 0xFFFFFF)

class DotMtxFont(jsonData: String, inputImg: Image) {
    
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
        this.image = dotMtxFontConvertImage(inputImg)
        
        // Check bounds for each glyph
        glyphs.values.forEach { g ->
            if (g.x < 0 || (g.x + g.w) > inputImg.width || g.y < 0 || (g.y + g.h) > inputImg.height) {
                error("Glyph '${g.character}' is out of bounds: {${g.x}, ${g.y}, ${g.w}, ${g.h}}, image size ${inputImg.width} x ${inputImg.height}")
            }
        }
    }
    
}

expect fun dotMtxFontConvertImage(img: Image): Image

expect fun DotMtxFont.getGlyphSubimage(glyph: Glyph, color: Color): Image
