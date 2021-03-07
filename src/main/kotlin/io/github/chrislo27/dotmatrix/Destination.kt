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

            var x = (if (routeAlignment == TextAlignment.RIGHT) 0 else route.totalWidth) + (when (frame.alignment) {
                TextAlignment.CENTRE -> (destWidth - frame.totalWidth) / 2
                TextAlignment.LEFT -> 0
                TextAlignment.RIGHT -> destWidth - frame.totalWidth
            })
            frame.layoutLines.forEach { ll ->
                if (ll.totalWidth > 0) {
                    g.drawImage(ll.toImage(ll.totalWidth, height).backing, x, 0, null)
                }
                x += ll.totalWidth + frame.spacingBetweenLayouts
            }

            g.dispose()
        })
    }
}

data class DestinationFrame(val layoutLines: List<LayoutLines>,
                            val alignment: TextAlignment = TextAlignment.CENTRE, val screenTime: Float = -1f,
                            val spacingBetweenLayouts: Int = 2, val animation: AnimationType = AnimationType.Inherit) {
    
    val totalWidth: Int = layoutLines.sumBy { it.totalWidth } + (layoutLines.size - 1).coerceAtLeast(0) * spacingBetweenLayouts
}
