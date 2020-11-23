package io.github.chrislo27.dotmatrix

import io.github.chrislo27.dotmatrix.img.Color
import io.github.chrislo27.dotmatrix.img.Image


data class AnimatedFrame(val image: Image, val ms: Int)

expect fun DestSign.createLedSpacingGrid(): Image

/**
 * Upscales the matrix image and applies the LED spacing grid on top.
 */
expect fun DestSign.generateImageForMatrix(matrixState: Image): Image

expect fun DestSign.generateGif(): ByteArray

expect fun DestSign.generateAnimatedFrames(): List<AnimatedFrame>

open class DestSign(val width: Int, val height: Int,
                    val ledSize: Int = 3, val ledSpacing: Int = 1, val borderSize: Int = 4,
                    val ledColor: Color = ORANGE, val offColor: Color = DARK_GREY, val borderColor: Color = Color.BLACK,
                    val scrollTime: Float = 1.75f, defaultAnimation: AnimationType = AnimationType.NoAnimation,
                    val circles: Boolean = false, val maxScrollFramerate: Int = 30) {

    companion object {
        val ORANGE: Color = Color(255, 144, 0, 255)
        val DARK_GREY: Color = Color(64, 64, 64, 255)
    }

    val defaultAnimation: AnimationType = if (defaultAnimation == AnimationType.Inherit) AnimationType.NoAnimation else defaultAnimation
    val outputWidth: Int = width * ledSize + (width - 1) * ledSpacing + borderSize * 2
    val outputHeight: Int = height * ledSize + (height - 1) * ledSpacing + borderSize * 2
    var destination: Destination? = null
    var pr: Destination? = null
    val stateCount: Int get() = ((destination?.frames?.size ?: 0) + (pr?.frames?.size ?: 0))
    val ledSpacingGrid: Image by lazy {
        this.createLedSpacingGrid()
    }

    fun generateMatrixForState(state: Int): Image {
        if (state !in 0 until stateCount)
            error("State ($state) is out of bounds (max $stateCount)")
        val numDestFrames = destination?.frames?.size ?: 0
        val onPr = state >= numDestFrames
        val currentDest = (if (onPr) pr else destination)!!
        return currentDest.generateMatrix(this.width, this.height, currentDest.frames[if (onPr) (state - numDestFrames) else state]).apply {
            afterMatrixGenerated(this)
        }
    }

    fun generateImageForState(state: Int): Image {
        return generateImageForMatrix(generateMatrixForState(state))
    }

    fun generateImageForFrame(destination: Destination, destFrame: DestinationFrame): Image {
        return generateImageForMatrix(destination.generateMatrix(this.width, this.height, destFrame).apply {
            afterMatrixGenerated(this)
        })
    }

    open fun afterMatrixGenerated(mtx: Image) {
    }

    fun getInheritedAnimation(destFrame: DestinationFrame): AnimationType =
        if (destFrame.animation == AnimationType.Inherit) defaultAnimation else destFrame.animation

}