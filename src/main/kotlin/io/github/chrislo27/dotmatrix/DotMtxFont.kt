package io.github.chrislo27.dotmatrix

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
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
    
    constructor(jsonFile: File) : this(jsonFile.inputStream(), jsonFile.resolveSibling("${jsonFile.nameWithoutExtension}.json").inputStream())
    
    init {
        val jsonObj: JsonNode = ObjectMapper().readTree(jsonStream)
        height = jsonObj["height"].asInt()
        desc = jsonObj["desc"]?.asText("") ?: ""
        advance = jsonObj["advance"].asInt()
        val glyphsNode = jsonObj["glyphs"]
        glyphs = glyphsNode.map { gNode ->
            Glyph(gNode["char"].asText()[0], gNode["x"].asInt(), gNode["y"]?.asInt(0) ?: 0, gNode["w"].asInt(), gNode["h"]?.asInt(height) ?: height, gNode["advance"]?.asInt(advance) ?: advance)
        }.associateBy { it.character }
        image = ImageIO.read(imgStream)
        jsonStream.close()
        imgStream.close()
        
        // Check bounds for each glyph
        glyphs.values.forEach { g ->
            if (g.x < 0 || (g.x + g.w) > image.width || g.y < 0 || (g.y + g.h) > image.height) {
                error("Glyph '${g.character}' is out of bounds: {${g.x}, ${g.y}, ${g.w}, ${g.h}}, image size ${image.width} x ${image.height}")
            }
        }
    }
    
}
