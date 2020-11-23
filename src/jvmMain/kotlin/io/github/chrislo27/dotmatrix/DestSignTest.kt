package io.github.chrislo27.dotmatrix

import java.awt.Image
import java.io.InputStream
import java.net.URL
import javax.imageio.ImageIO
import javax.swing.ImageIcon
import javax.swing.JLabel
import javax.swing.JOptionPane


fun classpathResource(path: String): URL = DestSignTest::class.java.classLoader.getResource(path) ?: error("Could not find classpath resource $path")

fun classpathResourceStream(path: String): InputStream = DestSignTest::class.java.classLoader.getResourceAsStream(path) ?: error("Could not find classpath resource $path")

fun readClasspathResourceAsString(path: String): String = classpathResourceStream(path).bufferedReader().let { 
    val s = it.readText()
    it.close()
    s
}

object DestSignTest {
    
    fun readClasspathResAsImage(path: String): io.github.chrislo27.dotmatrix.img.Image {
        return io.github.chrislo27.dotmatrix.img.Image(ImageIO.read(classpathResourceStream(path)))
    }

    val fonts: Map<String, DotMtxFont> by lazy {
        linkedMapOf(
            "5" to DotMtxFont(readClasspathResourceAsString("dotmatrix/variable/5VW.json"), readClasspathResAsImage("dotmatrix/variable/5VW.png")),
            "5d" to DotMtxFont(readClasspathResourceAsString("dotmatrix/variable/5DVW.json"), readClasspathResAsImage("dotmatrix/variable/5DVW.png")),
            "6" to DotMtxFont(readClasspathResourceAsString("dotmatrix/variable/6VW.json"), readClasspathResAsImage("dotmatrix/variable/6VW.png")),
            "6d" to DotMtxFont(readClasspathResourceAsString("dotmatrix/variable/6DVW.json"), readClasspathResAsImage("dotmatrix/variable/6DVW.png")),
            "7" to DotMtxFont(readClasspathResourceAsString("dotmatrix/variable/7VW.json"), readClasspathResAsImage("dotmatrix/variable/7VW.png")),
            "7d" to DotMtxFont(readClasspathResourceAsString("dotmatrix/variable/7DVW.json"), readClasspathResAsImage("dotmatrix/variable/7DVW.png")),
            "8" to DotMtxFont(readClasspathResourceAsString("dotmatrix/variable/8VW.json"), readClasspathResAsImage("dotmatrix/variable/8VW.png")),
            "8d" to DotMtxFont(readClasspathResourceAsString("dotmatrix/variable/8DVW.json"), readClasspathResAsImage("dotmatrix/variable/8DVW.png")),
            "9d" to DotMtxFont(readClasspathResourceAsString("dotmatrix/variable/9DVW.json"), readClasspathResAsImage("dotmatrix/variable/9DVW.png")),
            "10d" to DotMtxFont(readClasspathResourceAsString("dotmatrix/variable/10DVW.json"), readClasspathResAsImage("dotmatrix/variable/10DVW.png")),
            "11d" to DotMtxFont(readClasspathResourceAsString("dotmatrix/variable/11DVW.json"), readClasspathResAsImage("dotmatrix/variable/11DVW.png")),
            "12d" to DotMtxFont(readClasspathResourceAsString("dotmatrix/variable/12DVW.json"), readClasspathResAsImage("dotmatrix/variable/12DVW.png")),
            "13d" to DotMtxFont(readClasspathResourceAsString("dotmatrix/variable/13DVW.json"), readClasspathResAsImage("dotmatrix/variable/13DVW.png")),
            "14d" to DotMtxFont(readClasspathResourceAsString("dotmatrix/variable/14DVW.json"), readClasspathResAsImage("dotmatrix/variable/14DVW.png")),
            "15d" to DotMtxFont(readClasspathResourceAsString("dotmatrix/variable/15DVW.json"), readClasspathResAsImage("dotmatrix/variable/15DVW.png")),
            "16d" to DotMtxFont(readClasspathResourceAsString("dotmatrix/variable/16DVW.json"), readClasspathResAsImage("dotmatrix/variable/16DVW.png")),
            "16t" to DotMtxFont(readClasspathResourceAsString("dotmatrix/variable/16TVW.json"), readClasspathResAsImage("dotmatrix/variable/16TVW.png")),
            "22t" to DotMtxFont(readClasspathResourceAsString("dotmatrix/variable/22TVW.json"), readClasspathResAsImage("dotmatrix/variable/22TVW.png")),
            "24q" to DotMtxFont(readClasspathResourceAsString("dotmatrix/variable/24QVW.json"), readClasspathResAsImage("dotmatrix/variable/24QVW.png")),
            "5x7" to DotMtxFont(readClasspathResourceAsString("dotmatrix/fixed/5X7.json"), readClasspathResAsImage("dotmatrix/fixed/5X7.png")),
            "8x14" to DotMtxFont(readClasspathResourceAsString("dotmatrix/fixed/8X14.json"), readClasspathResAsImage("dotmatrix/fixed/8X14.png"))
        )
    }

    @JvmStatic
    fun main(args: Array<String>) {
        // Front: 160x16
        // Side: 96x8
        val sign = DestSign(160, 16/*, scrollAnimation = 0.25f * 0, scrollTime = 1.5f*/)
//        sign.destination = Destination(
//            LayoutLines(listOf(
//                GlyphLayout(listOf(GlyphRun(fonts.getValue("16d"), "19")), VerticalAlignment.CENTRE, TextAlignment.CENTRE)
//            ), LineSpacing.FLUSH_TO_EDGES),
//            listOf(
//                DestinationFrame(listOf(
//                    LayoutLines(listOf(
//                        GlyphLayout(listOf(GlyphRun(fonts.getValue("8d"), "UPTOWN CORE ")), VerticalAlignment.CENTRE, TextAlignment.CENTRE)
//                    ), LineSpacing.FLUSH_TO_EDGES),
//                    LayoutLines(listOf(
//                        GlyphLayout(listOf(GlyphRun(fonts.getValue("6"), "V  ")), VerticalAlignment.CENTRE, TextAlignment.LEFT),
//                        GlyphLayout(listOf(GlyphRun(fonts.getValue("6"), " I ")), VerticalAlignment.CENTRE, TextAlignment.CENTRE),
//                        GlyphLayout(listOf(GlyphRun(fonts.getValue("6"), "  A")), VerticalAlignment.CENTRE, TextAlignment.RIGHT)
//                    ), LineSpacing.FLUSH_TO_EDGES),
//                    LayoutLines(listOf(
//                        GlyphLayout(listOf(GlyphRun(fonts.getValue("8d"), "RIVER")), VerticalAlignment.CENTRE, TextAlignment.CENTRE),
//                        GlyphLayout(listOf(GlyphRun(fonts.getValue("7d"), "OAKS")), VerticalAlignment.CENTRE, TextAlignment.CENTRE)
//                    ), LineSpacing.FLUSH_TO_EDGES)
//                ))
//            )
//        )
        sign.destination = Destination(
            LayoutLines(listOf(
                GlyphLayout(listOf(GlyphRun(fonts.getValue("16d"), "410", DestSign.ORANGE)), VerticalAlignment.CENTRE, TextAlignment.CENTRE)
            ), LineSpacing.FLUSH_TO_EDGES),
            listOf(
                DestinationFrame(listOf(
                    LayoutLines(listOf(
                        GlyphLayout(listOf(
                            GlyphRun(fonts.getValue("15d"), "22", DestSign.ORANGE), GlyphRun(fonts.getValue("8d"), "ND", DestSign.ORANGE),
                            GlyphRun(fonts.getValue("15d"), " ST STN", DestSign.ORANGE)
                        ), VerticalAlignment.TOP, TextAlignment.CENTRE)
                    ), LineSpacing.FLUSH_TO_EDGES)
                ))
            )
        )

        val blackBg = sign.generateImageForState(0).backing
        val finalImg = blackBg.getScaledInstance(blackBg.width * 2, blackBg.height * 2, Image.SCALE_FAST)

        JOptionPane.showMessageDialog(null, JLabel(ImageIcon(finalImg)))
    }

}