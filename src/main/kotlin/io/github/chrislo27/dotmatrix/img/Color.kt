package io.github.chrislo27.dotmatrix.img


/**
 * A simple RGBA8888 class.
 */
class Color(val r: Int, val g: Int, val b: Int, val a: Int = 255) {
    
    companion object {
        val WHITE = Color(255, 255, 255)
        val BLACK = Color(0, 0, 0)
    }
    
    val rgb: Int = ((a and 0xFF) shl 24) or ((r and 0xFF) shl 16) or ((g and 0xFF) shl 8) or (b and 0xFF)
}

fun Color.toAWTColor(): java.awt.Color = java.awt.Color(this.r, this.g, this.b, this.a)

fun java.awt.Color.toDmxColor(): Color = Color(this.red, this.green, this.blue, this.alpha)
