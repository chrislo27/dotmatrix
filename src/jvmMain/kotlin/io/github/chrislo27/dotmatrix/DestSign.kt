package io.github.chrislo27.dotmatrix

import com.madgag.gif.fmsware.AnimatedGifEncoder
import io.github.chrislo27.dotmatrix.img.Image
import io.github.chrislo27.dotmatrix.img.toAWTColor
import java.awt.AlphaComposite

import java.awt.Rectangle
import java.awt.geom.Area
import java.awt.geom.Ellipse2D
import java.awt.image.BufferedImage
import java.awt.image.ImageObserver
import java.io.ByteArrayOutputStream
import kotlin.math.roundToInt

actual fun DestSign.createLedSpacingGrid(): Image {
    return Image(BufferedImage(outputWidth, outputHeight, BufferedImage.TYPE_4BYTE_ABGR).apply {
        val g = createGraphics()
        g.color = java.awt.Color.BLACK
        for (x in (borderSize + ledSize) until (width - borderSize) step (ledSize + ledSpacing)) {
            g.fillRect(x, borderSize, ledSpacing, height - borderSize * 2)
        }
        for (y in (borderSize + ledSize) until (height - borderSize) step (ledSize + ledSpacing)) {
            g.fillRect(borderSize, y, width - borderSize * 2, ledSpacing)
        }
        if (circles && ledSize >= 4) {
            val circleTemplate = BufferedImage(ledSize, ledSize, BufferedImage.TYPE_4BYTE_ABGR).also { c ->
                val gr = c.createGraphics()
                gr.color = g.color
                gr.fill(Area(Rectangle(0, 0, c.width, c.height)).apply {
                    subtract(Area(Ellipse2D.Float(-0.5f, -0.5f, c.width.toFloat(), c.height.toFloat())))
                })
                gr.dispose()
            }
            for (x in 0 until width) {
                for (y in 0 until height) {
                    g.drawImage(circleTemplate, borderSize + (ledSize + ledSpacing) * x, borderSize + (ledSize + ledSpacing) * y, null)
                }
            }
        }
        g.color = this@createLedSpacingGrid.borderColor.toAWTColor()
        g.fillRect(0, 0, width, borderSize)
        g.fillRect(0, height - borderSize, width, borderSize)
        g.fillRect(0, 0, borderSize, height)
        g.fillRect(width - borderSize, 0, borderSize, height)
        g.dispose()
    })
}

private fun java.awt.Image.toBufferedImage(): BufferedImage {
    if (this is BufferedImage) {
        return this
    }
    val bufferedImage = BufferedImage(this.getWidth(null), this.getHeight(null), BufferedImage.TYPE_INT_ARGB)
    val graphics2D = bufferedImage.createGraphics()
    graphics2D.drawImage(this, 0, 0, null)
    graphics2D.dispose()
    return bufferedImage
}

/**
 * Upscales the matrix image and applies the LED spacing grid on top.
 */
actual fun DestSign.generateImageForMatrix(matrixState: Image): Image {
    return Image(BufferedImage(outputWidth, outputHeight, BufferedImage.TYPE_4BYTE_ABGR).apply {
        val g = createGraphics()
        g.color = offColor.toAWTColor()
        g.fillRect(0, 0, width, height)
        val matrix = matrixState.backing.getScaledInstance(outputWidth - borderSize * 2, outputHeight - borderSize * 2, java.awt.Image.SCALE_FAST).toBufferedImage()
        g.drawImage(matrix, borderSize, borderSize, null as ImageObserver?)
        g.drawImage(ledSpacingGrid.backing, 0, 0, null as ImageObserver?)
        g.dispose()
    })
}

actual fun DestSign.generateGif(): ByteArray {
    val e: AnimatedGifEncoder = AnimatedGifEncoder()
    val os: ByteArrayOutputStream = ByteArrayOutputStream()
    e.start(os)
    e.setSize(outputWidth, outputHeight)
    e.setRepeat(0)
    val frames = generateAnimatedFrames()
    frames.forEach { (img, ms) ->
        e.setDelay(ms)
        e.addFrame(img.backing)
    }
    e.finish()
    os.close()
    return os.toByteArray()
}

actual fun DestSign.generateAnimatedFrames(): List<AnimatedFrame> {
    val numDestFrames = destination?.frames?.size ?: 0
    val framesList = mutableListOf<AnimatedFrame>()

    data class StateImage(val dest: Destination, val frame: DestinationFrame, val img: Image,
                          val stateTime: Float)

    val stateImages: List<StateImage> = (0 until stateCount).map { state ->
        val onPr = state >= numDestFrames
        val currentDest = (if (onPr) pr else destination)!!
        val index = if (onPr) (state - numDestFrames) else state
        val currentFrame = currentDest.frames[index]
        StateImage(currentDest, currentFrame, generateImageForFrame(currentDest, currentFrame), currentDest.screenTimes.getOrElse(index) { scrollTime }.let { if (it <= 0f) scrollTime else it })
    }
    val allNoAnimation = listOfNotNull(destination, pr).all { it.frames.all { f -> getInheritedAnimation(f) == AnimationType.NoAnimation } }
    if (allNoAnimation || stateCount == 1) {
        for (i in 0 until stateCount) {
            framesList += AnimatedFrame(stateImages[i].img, (stateImages[i].stateTime * 1000f).roundToInt())
        }
    } else {
        // Interpolation
        val mtx: Image = Image(BufferedImage(width, height, BufferedImage.TYPE_4BYTE_ABGR))
        val g = mtx.backing.createGraphics()
        for (i in 0 until stateCount) {
            val nextIndex = (i + 1) % stateImages.size
            val prevDest: Destination = stateImages[i].dest
            val nextDest: Destination = stateImages[nextIndex].dest
            val prevFrame: DestinationFrame = stateImages[i].frame
            val nextFrame: DestinationFrame = stateImages[nextIndex].frame
            val ani = getInheritedAnimation(prevFrame)
            var stillFrameDelay = (stateImages[i].stateTime * 1000f).roundToInt()
            if (ani.delay <= 0f && stillFrameDelay <= 0) {
                stillFrameDelay = (1000f / maxScrollFramerate).roundToInt()
            }
            if (stillFrameDelay > 0f) {
                framesList += AnimatedFrame(stateImages[i].img, stillFrameDelay)
            }
            if (ani.delay > 0f) {
                val framerate = maxScrollFramerate
                val keepRouteNum = prevDest === nextDest && prevDest.route.totalWidth > 0
                var prevMtx: Image = prevDest.generateMatrix(this.width, this.height, prevFrame)
                var nextMtx: Image = nextDest.generateMatrix(this.width, this.height, nextFrame)
                val routeNumImg: BufferedImage? = if (keepRouteNum) prevMtx.backing.getSubimage(if (prevDest.routeAlignment == TextAlignment.RIGHT) (mtx.width - prevDest.route.totalWidth) else 0, 0, prevDest.route.totalWidth, prevMtx.height) else null
                val numFrames: Int = (framerate * ani.delay).roundToInt()
                if (ani is AnimationType.HorizontalScroll) {
                    prevMtx = prevDest.generateMatrix(this.width, this.height, prevFrame, false)
                    nextMtx = nextDest.generateMatrix(this.width, this.height, nextFrame, false)
                }
                for (f in 0 until numFrames) {
                    val progress = f / numFrames.toFloat()
                    val ms = ((1000f * ani.delay) / numFrames).roundToInt().coerceAtLeast(3)
                    g.composite = AlphaComposite.Clear
                    g.fillRect(0, 0, mtx.width, mtx.height)
                    g.composite = AlphaComposite.SrcOver
                    when (ani) {
                        is AnimationType.Falldown -> {
                            val yOffset = (progress * mtx.height).toInt()
                            g.drawImage(prevMtx.backing, 0, yOffset, null)
                            g.drawImage(nextMtx.backing, 0, -mtx.height + yOffset, null)
                            if (routeNumImg != null) {
                                // Keep route number stationary
                                val routeWidth = prevDest.route.totalWidth
                                val x = if (prevDest.routeAlignment == TextAlignment.RIGHT) (mtx.width - routeWidth) else 0
                                g.composite = AlphaComposite.Clear
                                g.fillRect(x, 0, routeWidth, mtx.height)
                                g.composite = AlphaComposite.SrcOver
                                g.drawImage(routeNumImg, x, 0, null)
                            }
                        }
                        is AnimationType.Fallup -> {
                            val yOffset = (progress * mtx.height).toInt()
                            g.drawImage(prevMtx.backing, 0, -yOffset, null)
                            g.drawImage(nextMtx.backing, 0, mtx.height - yOffset, null)
                            if (routeNumImg != null) {
                                // Keep route number stationary
                                val routeWidth = prevDest.route.totalWidth
                                val x = if (prevDest.routeAlignment == TextAlignment.RIGHT) (mtx.width - routeWidth) else 0
                                g.composite = AlphaComposite.Clear
                                g.fillRect(x, 0, routeWidth, mtx.height)
                                g.composite = AlphaComposite.SrcOver
                                g.drawImage(routeNumImg, x, 0, null)
                            }
                        }
                        is AnimationType.Sidewipe -> {
                            g.drawImage(prevMtx.backing, 0, 0, null)
                            val xOffset = (progress * mtx.width).toInt()
                            if (xOffset > 0) {
                                g.composite = AlphaComposite.Clear
                                g.fillRect(0, 0, xOffset, mtx.height)
                                g.composite = AlphaComposite.SrcOver
                                g.drawImage(nextMtx.backing.getSubimage(0, 0, xOffset, nextMtx.height), 0, 0, null)
                            }
                        }
                        is AnimationType.HorizontalScroll -> {
                            val xOffset = (progress * mtx.width).toInt()
                            g.drawImage(prevMtx.backing, -xOffset, 0, null)
                            g.drawImage(nextMtx.backing, mtx.width - xOffset, 0, null)
                            if (routeNumImg != null) {
                                // Redraw route number
                                val routeWidth = prevDest.route.totalWidth
                                val x = if (prevDest.routeAlignment == TextAlignment.RIGHT) (mtx.width - routeWidth) else 0
                                g.composite = AlphaComposite.Clear
                                g.fillRect(x, 0, routeWidth, mtx.height)
                                g.composite = AlphaComposite.SrcOver
                                g.drawImage(routeNumImg, x, 0, null)
                            }
                        }
                        is AnimationType.Inherit, is AnimationType.NoAnimation -> {
                        }
                    }
                    afterMatrixGenerated(mtx)
                    framesList += AnimatedFrame(generateImageForMatrix(mtx), ms)
                }
            }
        }
        g.dispose()
    }
    return framesList
}
