package io.github.chrislo27.dotmatrix

import io.github.chrislo27.dotmatrix.img.Image
import java.awt.image.BufferedImage
import java.awt.image.ImageObserver

actual fun Destination.generateMatrix(width: Int, height: Int, frame: DestinationFrame, drawRoute: Boolean): Image {
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
