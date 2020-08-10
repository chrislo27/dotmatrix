package io.github.chrislo27.dotmatrix

import java.awt.Image
import java.io.InputStream
import java.net.URL
import javax.swing.ImageIcon
import javax.swing.JLabel
import javax.swing.JOptionPane


fun classpathResource(path: String): URL = DestSignTest::class.java.classLoader.getResource(path) ?: error("Could not find classpath resource $path")

fun classpathResourceStream(path: String): InputStream = DestSignTest::class.java.classLoader.getResourceAsStream(path) ?: error("Could not find classpath resource $path")

object DestSignTest {

    val fonts: Map<String, DotMtxFont> by lazy {
        linkedMapOf(
            "5" to DotMtxFont(classpathResourceStream("dotmatrix/variable/5VW.json"), classpathResourceStream("dotmatrix/variable/5VW.png")),
            "5d" to DotMtxFont(classpathResourceStream("dotmatrix/variable/5DVW.json"), classpathResourceStream("dotmatrix/variable/5DVW.png")),
            "6" to DotMtxFont(classpathResourceStream("dotmatrix/variable/6VW.json"), classpathResourceStream("dotmatrix/variable/6VW.png")),
            "6d" to DotMtxFont(classpathResourceStream("dotmatrix/variable/6DVW.json"), classpathResourceStream("dotmatrix/variable/6DVW.png")),
            "7" to DotMtxFont(classpathResourceStream("dotmatrix/variable/7VW.json"), classpathResourceStream("dotmatrix/variable/7VW.png")),
            "7d" to DotMtxFont(classpathResourceStream("dotmatrix/variable/7DVW.json"), classpathResourceStream("dotmatrix/variable/7DVW.png")),
            "8" to DotMtxFont(classpathResourceStream("dotmatrix/variable/8VW.json"), classpathResourceStream("dotmatrix/variable/8VW.png")),
            "8d" to DotMtxFont(classpathResourceStream("dotmatrix/variable/8DVW.json"), classpathResourceStream("dotmatrix/variable/8DVW.png")),
            "9d" to DotMtxFont(classpathResourceStream("dotmatrix/variable/9DVW.json"), classpathResourceStream("dotmatrix/variable/9DVW.png")),
            "10d" to DotMtxFont(classpathResourceStream("dotmatrix/variable/10DVW.json"), classpathResourceStream("dotmatrix/variable/10DVW.png")),
            "11d" to DotMtxFont(classpathResourceStream("dotmatrix/variable/11DVW.json"), classpathResourceStream("dotmatrix/variable/11DVW.png")),
            "12d" to DotMtxFont(classpathResourceStream("dotmatrix/variable/12DVW.json"), classpathResourceStream("dotmatrix/variable/12DVW.png")),
            "13d" to DotMtxFont(classpathResourceStream("dotmatrix/variable/13DVW.json"), classpathResourceStream("dotmatrix/variable/13DVW.png")),
            "14d" to DotMtxFont(classpathResourceStream("dotmatrix/variable/14DVW.json"), classpathResourceStream("dotmatrix/variable/14DVW.png")),
            "15d" to DotMtxFont(classpathResourceStream("dotmatrix/variable/15DVW.json"), classpathResourceStream("dotmatrix/variable/15DVW.png")),
            "16d" to DotMtxFont(classpathResourceStream("dotmatrix/variable/16DVW.json"), classpathResourceStream("dotmatrix/variable/16DVW.png")),
            "16t" to DotMtxFont(classpathResourceStream("dotmatrix/variable/16TVW.json"), classpathResourceStream("dotmatrix/variable/16TVW.png")),
            "22t" to DotMtxFont(classpathResourceStream("dotmatrix/variable/22TVW.json"), classpathResourceStream("dotmatrix/variable/22TVW.png")),
            "24q" to DotMtxFont(classpathResourceStream("dotmatrix/variable/24QVW.json"), classpathResourceStream("dotmatrix/variable/24QVW.png")),
            "5x7" to DotMtxFont(classpathResourceStream("dotmatrix/fixed/5X7.json"), classpathResourceStream("dotmatrix/fixed/5X7.png")),
            "8x14" to DotMtxFont(classpathResourceStream("dotmatrix/fixed/8X14.json"), classpathResourceStream("dotmatrix/fixed/8X14.png"))
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

        val blackBg = sign.generateImageForState(0)
        val finalImg = blackBg.getScaledInstance(blackBg.width * 2, blackBg.height * 2, Image.SCALE_FAST)

        JOptionPane.showMessageDialog(null, JLabel(ImageIcon(finalImg)))
    }

}