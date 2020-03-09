package io.github.chrislo27.dotmatrix

import com.madgag.gif.fmsware.AnimatedGifEncoder
import java.awt.AlphaComposite
import java.awt.Color
import java.awt.Image
import java.awt.Rectangle
import java.awt.geom.Area
import java.awt.geom.Ellipse2D
import java.awt.image.BufferedImage
import java.awt.image.ImageObserver
import java.io.File
import java.io.OutputStream
import kotlin.math.roundToInt


open class DestSign(val width: Int, val height: Int,
                    val ledSize: Int = 3, val ledSpacing: Int = 1, val borderSize: Int = 4,
                    val ledColor: Color = ORANGE, val offColor: Color = DARK_GREY, val borderColor: Color = Color.BLACK,
                    val scrollTime: Float = 1.75f, val scrollAnimation: AnimationType = AnimationType.NoAnimation,
                    val circles: Boolean = false, val maxScrollFramerate: Int = 12) {

    companion object {
        val ORANGE: Color = Color(255, 144, 0, 255)
        val DARK_GREY: Color = Color(64, 64, 64, 255)
    }

    val outputWidth: Int = width * ledSize + (width - 1) * ledSpacing + borderSize * 2
    val outputHeight: Int = height * ledSize + (height - 1) * ledSpacing + borderSize * 2
    var destination: Destination? = null
    var pr: Destination? = null
    val stateCount: Int get() = ((destination?.frames?.size ?: 0) + (pr?.frames?.size ?: 0))
    private val ledSpacingGrid: BufferedImage by lazy {
        BufferedImage(outputWidth, outputHeight, BufferedImage.TYPE_4BYTE_ABGR).apply {
            val g = createGraphics()
            g.color = Color.BLACK
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
            g.color = borderColor
            g.fillRect(0, 0, width, borderSize)
            g.fillRect(0, height - borderSize, width, borderSize)
            g.fillRect(0, 0, borderSize, height)
            g.fillRect(width - borderSize, 0, borderSize, height)
            g.dispose()
        }
    }

    fun generateImageForMatrix(matrixState: BufferedImage): BufferedImage {
        return BufferedImage(outputWidth, outputHeight, BufferedImage.TYPE_4BYTE_ABGR).apply {
            val g = createGraphics()
            g.color = offColor
            g.fillRect(0, 0, width, height)
            val matrix = matrixState.getScaledInstance(outputWidth - borderSize * 2, outputHeight - borderSize * 2, Image.SCALE_FAST).toBufferedImage()
            g.drawImage(matrix, borderSize, borderSize, null as ImageObserver?)
            g.drawImage(ledSpacingGrid, 0, 0, null as ImageObserver?)
            g.dispose()
        }
    }

    fun generateMatrixForState(state: Int): BufferedImage {
        if (state !in 0 until stateCount)
            error("State ($state) is out of bounds (max $stateCount)")
        val numDestFrames = destination?.frames?.size ?: 0
        val onPr = state >= numDestFrames
        val currentDest = (if (onPr) pr else destination)!!
        return currentDest.generateMatrix(this.width, this.height, currentDest.frames[if (onPr) (state - numDestFrames) else state]).apply {
            afterMatrixGenerated(this)
        }
    }

    fun generateImageForState(state: Int): BufferedImage {
        return generateImageForMatrix(generateMatrixForState(state).changeToColor(ledColor))
    }

    fun generateImageForFrame(destination: Destination, destFrame: DestinationFrame): BufferedImage {
        return generateImageForMatrix(destination.generateMatrix(this.width, this.height, destFrame).apply {
            afterMatrixGenerated(this)
            changeToColor(ledColor)
        })
    }

    open fun afterMatrixGenerated(mtx: BufferedImage) {
    }

    fun generateAnimatedFrames(): List<Pair<BufferedImage, Int>> {
        val numDestFrames = destination?.frames?.size ?: 0
        val framesList = mutableListOf<Pair<BufferedImage, Int>>()

        data class StateImage(val dest: Destination, val frame: DestinationFrame, val img: BufferedImage,
                              val stateTime: Float)

        val stateImages: List<StateImage> = (0 until stateCount).map { state ->
            val onPr = state >= numDestFrames
            val currentDest = (if (onPr) pr else destination)!!
            val index = if (onPr) (state - numDestFrames) else state
            val currentFrame = currentDest.frames[index]
            StateImage(currentDest, currentFrame, generateImageForFrame(currentDest, currentFrame), currentDest.screenTimes.getOrElse(index) { scrollTime }.let { if (it <= 0f) scrollTime else it })
        }
        if (scrollAnimation == AnimationType.NoAnimation || stateCount == 1) {
            for (i in 0 until stateCount) {
                framesList += stateImages[i].img to (stateImages[i].stateTime * 1000f).roundToInt()
            }
        } else {
            val framerate = (if (scrollAnimation is AnimationType.Sidewipe) width else height).coerceIn(1, maxScrollFramerate)
            // Interpolation
            val mtx = BufferedImage(width, height, BufferedImage.TYPE_4BYTE_ABGR)
            val g = mtx.createGraphics()
            for (i in 0 until stateCount) {
                val nextIndex = (i + 1) % stateImages.size
                val prevDest: Destination = stateImages[i].dest
                val nextDest: Destination = stateImages[nextIndex].dest
                val prevFrame: DestinationFrame = stateImages[i].frame
                val nextFrame: DestinationFrame = stateImages[nextIndex].frame
                val prevMtx: BufferedImage = prevDest.generateMatrix(this.width, this.height, prevFrame)
                val nextMtx: BufferedImage = nextDest.generateMatrix(this.width, this.height, nextFrame)
                framesList += stateImages[i].img to (stateImages[i].stateTime * 1000f).roundToInt()
                val keepRouteNum = prevDest === nextDest && prevDest.route.totalWidth > 0
                val routeNumImg: BufferedImage? = if (keepRouteNum) prevMtx.getSubimage(if (prevDest.routeAlignment == TextAlignment.RIGHT) (mtx.width - prevDest.route.totalWidth) else 0, 0, prevDest.route.totalWidth, prevMtx.height) else null
                for (f in 0 until framerate) {
                    if (i == stateCount - 1 && scrollAnimation is AnimationType.FalldownFrames) break
                    val progress = f / framerate.toFloat()
                    val ms = ((1000f * scrollAnimation.delay) / framerate).roundToInt().coerceAtLeast(3)
                    g.composite = AlphaComposite.Clear
                    g.fillRect(0, 0, mtx.width, mtx.height)
                    g.composite = AlphaComposite.SrcOver
                    when (scrollAnimation) {
                        is AnimationType.Falldown, is AnimationType.FalldownFrames -> {
                            val yOffset = (progress * mtx.height).toInt()
                            g.drawImage(prevMtx, 0, yOffset, null)
                            g.drawImage(nextMtx, 0, -mtx.height + yOffset, null)
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
                            g.drawImage(prevMtx, 0, 0, null)
                            val xOffset = (progress * mtx.width).toInt()
                            if (xOffset > 0) {
                                g.composite = AlphaComposite.Clear
                                g.fillRect(0, 0, xOffset, mtx.height)
                                g.composite = AlphaComposite.SrcOver
                                g.drawImage(nextMtx.getSubimage(0, 0, xOffset, nextMtx.height), 0, 0, null)
                            }
                        }
                        else -> {
                        }
                    }
                    afterMatrixGenerated(mtx)
                    mtx.changeToColor(ledColor)
                    framesList += generateImageForMatrix(mtx) to ms
                }
            }
            g.dispose()
        }
        return framesList
    }

    fun generateGif(os: OutputStream) {
        val e: AnimatedGifEncoder = AnimatedGifEncoder()
        e.start(os)
        e.setSize(outputWidth, outputHeight)
        e.setRepeat(0)
        val frames = generateAnimatedFrames()
        frames.forEach { (img, ms) ->
            e.setDelay(ms)
            e.addFrame(img)
        }
        e.finish()
        os.close()
    }

    fun generateGif(file: File) {
        file.createNewFile()
        generateGif(file.outputStream())
    }

    private fun BufferedImage.changeToColor(color: Color): BufferedImage {
        val rgbMask: Int = 0x00ffffff
        val replaceRGB: Int = 0x00ffffff // white
        val toggleRGB = replaceRGB xor (color.red shl 16 or (color.green shl 8) or color.blue)
        val w: Int = width
        val h: Int = height
        val rgb: IntArray = getRGB(0, 0, w, h, null, 0, w)
        for (i in rgb.indices) {
            if (rgb[i] and rgbMask == replaceRGB) {
                rgb[i] = rgb[i] xor toggleRGB
            }
        }
        setRGB(0, 0, w, h, rgb, 0, w)
        return this
    }

    private fun Image.toBufferedImage(): BufferedImage {
        if (this is BufferedImage) {
            return this
        }
        val bufferedImage = BufferedImage(this.getWidth(null), this.getHeight(null), BufferedImage.TYPE_INT_ARGB)

        val graphics2D = bufferedImage.createGraphics()
        graphics2D.drawImage(this, 0, 0, null)
        graphics2D.dispose()

        return bufferedImage
    }

}