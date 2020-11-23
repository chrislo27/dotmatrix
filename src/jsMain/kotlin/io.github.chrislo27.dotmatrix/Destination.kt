package io.github.chrislo27.dotmatrix

import io.github.chrislo27.dotmatrix.img.Image

actual class Destination actual constructor(actual val route: LayoutLines, actual val frames: List<DestinationFrame>,
                                            actual val screenTimes: List<Float>,
                                            actual val routeAlignment: TextAlignment) {

    actual fun generateMatrix(width: Int, height: Int, frame: DestinationFrame, drawRoute: Boolean): Image {
        TODO()
    }

}