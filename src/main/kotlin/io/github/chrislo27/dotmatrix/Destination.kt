package io.github.chrislo27.dotmatrix

import io.github.chrislo27.dotmatrix.img.Image
import java.awt.image.BufferedImage
import java.awt.image.ImageObserver


data class Destination(
    val route: LayoutLines,
    val frames: List<DestinationFrame>,
    val screenTimes: List<Float> = emptyList(),
    val routeAlignment: TextAlignment = TextAlignment.LEFT
) {
    fun generateMatrix(width: Int, height: Int, frame: DestinationFrame, drawRoute: Boolean = true): Image {
        if (routeAlignment == TextAlignment.CENTRE) error("Route alignment cannot be centre")
        return Image(BufferedImage(width, height, BufferedImage.TYPE_4BYTE_ABGR).apply {
            val g = createGraphics()
            val hasRoute = route.lines.isNotEmpty() && route.totalWidth > 0
            if (hasRoute && drawRoute) {
                val routeImg = route.toImage(route.totalWidth, height).backing
                g.drawImage(routeImg, if (routeAlignment == TextAlignment.RIGHT) (this.width - routeImg.width) else 0, 0, null as ImageObserver?)
            }
            val destWidth = if (hasRoute) (width - route.totalWidth) else width

            val frameOffsetX = (if (routeAlignment == TextAlignment.RIGHT) 0 else route.totalWidth) + (when (frame.alignment) {
                TextAlignment.CENTRE -> (destWidth - frame.totalWidth) / 2
                TextAlignment.LEFT -> 0
                TextAlignment.RIGHT -> destWidth - frame.totalWidth
            })
            val frameImage: Image = frame.toImage(height)
            g.setClip((if (routeAlignment == TextAlignment.RIGHT) 0 else route.totalWidth), 0, destWidth, height)
            g.drawImage(frameImage.backing, frameOffsetX, 0, null as ImageObserver?)
            
            g.dispose()
        })
    }
}

data class DestinationFrame(val layoutLines: List<LayoutLines>,
                            val alignment: TextAlignment = TextAlignment.CENTRE,
                            val spacingBetweenLayouts: Int = 2, val animation: AnimationType = AnimationType.Inherit,
                            val hscroll: FrameHScroll = FrameHScroll.NoScroll
) {
    
    val totalWidth: Int = (layoutLines.sumOf { it.totalWidth } + (layoutLines.size - 1).coerceAtLeast(0) * spacingBetweenLayouts).coerceAtLeast(1)
    
    fun toImage(height: Int): Image {
        return Image(BufferedImage(totalWidth, height, BufferedImage.TYPE_4BYTE_ABGR).apply {
            val g = createGraphics()

            var x = 0
            layoutLines.forEach { ll ->
                if (ll.totalWidth > 0) {
                    g.drawImage(ll.toImage(ll.totalWidth, height).backing, x, 0, null)
                }
                x += ll.totalWidth + spacingBetweenLayouts
            }
            
            g.dispose()
        })
    }
}

sealed class FrameHScroll {
    data object NoScroll : FrameHScroll()
    
    class Scroll(val pixelsPerSecond: Float = 30f, val spacingPercentage: Float = 1f) : FrameHScroll()
}
