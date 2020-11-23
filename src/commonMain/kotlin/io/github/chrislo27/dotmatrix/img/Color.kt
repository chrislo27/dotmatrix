package io.github.chrislo27.dotmatrix.img


class Color(val r: Int, val g: Int, val b: Int, val a: Int = 255) {
    
    companion object {
        val WHITE = Color(255, 255, 255)
        val BLACK = Color(0, 0, 0)
    }
    
    val rgb: Int = ((a and 0xFF) shl 24) or ((r and 0xFF) shl 16) or ((g and 0xFF) shl 8) or (b and 0xFF)
}