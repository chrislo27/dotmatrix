package io.github.chrislo27.dotmatrix

import io.github.chrislo27.dotmatrix.img.Image


data class Destination(
    val route: LayoutLines,
    val frames: List<DestinationFrame>,
    val screenTimes: List<Float> = emptyList(),
    val routeAlignment: TextAlignment = TextAlignment.LEFT
)

expect fun Destination.generateMatrix(width: Int, height: Int, frame: DestinationFrame, drawRoute: Boolean = true): Image

data class DestinationFrame(val layoutLines: List<LayoutLines>,
                            val alignment: TextAlignment = TextAlignment.CENTRE, val screenTime: Float = -1f,
                            val spacingBetweenLayouts: Int = 2, val animation: AnimationType = AnimationType.Inherit) {
    
    val totalWidth: Int = layoutLines.sumBy { it.totalWidth } + (layoutLines.size - 1).coerceAtLeast(0) * spacingBetweenLayouts
}
