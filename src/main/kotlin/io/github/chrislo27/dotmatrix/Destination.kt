package io.github.chrislo27.dotmatrix

import java.awt.image.BufferedImage
import java.awt.image.ImageObserver


data class Destination(val route: LayoutLines, val frames: List<DestinationFrame>,
                       val screenTimes: List<Float> = emptyList(),
                       val routeAlignment: TextAlignment = TextAlignment.LEFT) {
    
    fun generateMatrix(width: Int, height: Int, frame: DestinationFrame): BufferedImage {
        if (routeAlignment == TextAlignment.CENTRE) error("Route alignment cannot be centre")
        return BufferedImage(width, height, BufferedImage.TYPE_4BYTE_ABGR).apply {
            val g = createGraphics()
            val hasRoute = route.lines.isNotEmpty()
            if (hasRoute) {
                val routeImg = route.toBufferedImage(route.totalWidth, height)
                g.drawImage(routeImg, if (routeAlignment == TextAlignment.RIGHT) (this.width - routeImg.width) else 0, 0, null as ImageObserver?)
            }
            val destWidth = if (hasRoute) (width - route.totalWidth) else width

            var x = (if (routeAlignment == TextAlignment.RIGHT) 0 else route.totalWidth) + (when (frame.alignment) {
                TextAlignment.CENTRE -> (destWidth - frame.totalWidth) / 2
                TextAlignment.LEFT -> 0
                TextAlignment.RIGHT -> destWidth - frame.totalWidth
            })
            frame.layoutLines.forEach { ll ->
                g.drawImage(ll.toBufferedImage(ll.totalWidth, height), x, 0, null)
                x += ll.totalWidth + frame.spacingBetweenLayouts
            }

            g.dispose()
        }
    }
}

data class DestinationFrame(val layoutLines: List<LayoutLines>,
                            val alignment: TextAlignment = TextAlignment.CENTRE, val screenTime: Float = -1f,
                            val spacingBetweenLayouts: Int = 2) {
    val totalWidth: Int = layoutLines.sumBy { it.totalWidth } + (layoutLines.size - 1).coerceAtLeast(0) * spacingBetweenLayouts
}

//data class Destination(val route: String, val routeFont: DotMtxFont,
//                       val frames: List<DestinationFrame>,
//                       val screenTimes: List<Float> = emptyList(),
//                       val defaultTextAlignment: TextAlignment = TextAlignment.CENTRE,
//                       val routeAlignment: TextAlignment = TextAlignment.LEFT) {
//
//    val routeGL: GlyphRun = GlyphRun(routeFont, route)
//
//    fun generateMatrix(width: Int, height: Int, frame: DestinationFrame, defaultTextAlignment: TextAlignment): BufferedImage {
//        if (routeAlignment == TextAlignment.CENTRE) error("Route alignment cannot be centre")
//        return BufferedImage(width, height, BufferedImage.TYPE_4BYTE_ABGR).apply {
//            val g = createGraphics()
//            val hasRoute = route.isNotEmpty()
//            if (hasRoute) {
//                val routeImg = routeGL.toBufferedImage()
//                g.drawImage(routeImg, if (routeAlignment == TextAlignment.RIGHT) (this.width - routeImg.width) else 0, height / 2 - routeImg.height / 2, null as ImageObserver?)
//            }
//            val destWidth: Int = if (hasRoute) (width - routeGL.width) else width
//            fun BufferedImage.drawAsText(y: Int) {
//                val x: Int = when (frame.textAlignment ?: defaultTextAlignment) {
//                    TextAlignment.CENTRE -> if (hasRoute && routeAlignment == TextAlignment.RIGHT) (destWidth / 2 - this.width / 2) else ((width - destWidth / 2f).toInt() - this.width / 2)
//                    TextAlignment.LEFT -> if (hasRoute && routeAlignment == TextAlignment.LEFT) routeGL.width else 0
//                    TextAlignment.RIGHT -> if (hasRoute && routeAlignment == TextAlignment.RIGHT) (width - this.width - routeGL.width) else (width - this.width)
//                }
//                g.drawImage(this, x, y, null as ImageObserver?)
//            }
//            if (frame.lines.size == 1) {
//                // Single line
//                val dest1 = frame.lines.first()
//                dest1.toBufferedImage().drawAsText((height / 2 - dest1.height / 2f).roundToInt())
//            } else {
//                val totalHeight = frame.lines.sumBy { it.height }.coerceAtLeast(1)
//                val spacing: Float = (height - totalHeight).toFloat() / (frame.lines.size - 1).coerceAtLeast(1)
//                frame.lines.fold(0f) { acc, layout ->
//                    val y = acc.roundToInt()
//                    layout.toBufferedImage().drawAsText(y)
//                    acc + layout.height + spacing
//                }
//            }
//            g.dispose()
//        }
//    }
//}
