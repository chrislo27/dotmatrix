package io.github.chrislo27.dotmatrix

import com.madgag.gif.fmsware.AnimatedGifEncoder
import java.awt.AlphaComposite
import java.awt.Color
import java.awt.Image
import java.awt.image.BufferedImage
import java.awt.image.ImageObserver
import java.io.File
import java.io.OutputStream
import kotlin.math.roundToInt


class DestSign(val width: Int, val height: Int,
               val ledSize: Int = 3, val ledSpacing: Int = 1, val borderSize: Int = 4,
               val ledColor: Color = ORANGE, val offColor: Color = DARK_GREY, val borderColor: Color = Color.BLACK,
               val scrollTime: Float = 1.75f, val scrollAnimation: Float = 0f) {

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
        return currentDest.generateMatrix(this.width, this.height, currentDest.frames[if (onPr) (state - numDestFrames) else state])
    }

    fun generateImageForState(state: Int): BufferedImage {
        return generateImageForMatrix(generateMatrixForState(state).changeToColor(ledColor))
    }

    fun generateImageForFrame(destination: Destination, destFrame: DestinationFrame): BufferedImage {
        return generateImageForMatrix(destination.generateMatrix(this.width, this.height, destFrame).changeToColor(ledColor))
    }

    fun generateGif(os: OutputStream) {
        val e = AnimatedGifEncoder()
        e.start(os)
        e.setSize(outputWidth, outputHeight)
        e.setRepeat(0)
        val numDestFrames = destination?.frames?.size ?: 0
        data class StateImage(val dest: Destination, val frame: DestinationFrame, val img: BufferedImage,
                              val stateTime: Float)
        val stateImages: List<StateImage> = (0 until stateCount).map { state ->
            val onPr = state >= numDestFrames
            val currentDest = (if (onPr) pr else destination)!!
            val index = if (onPr) (state - numDestFrames) else state
            val currentFrame = currentDest.frames[index]
            StateImage(currentDest, currentFrame, generateImageForFrame(currentDest, currentFrame), currentDest.screenTimes.getOrElse(index) { scrollTime }.let { if (it <= 0f) scrollTime else it })
        }
        val framerate = height.coerceIn(1, 10)
        if (scrollAnimation <= 0f || stateCount == 1 || framerate <= 1) {
            for (i in 0 until stateCount) {
                e.setDelay((stateImages[i].stateTime * 1000f).roundToInt())
                e.addFrame(stateImages[i].img)
            }
        } else {
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
                e.setDelay((stateImages[i].stateTime * 1000f).roundToInt())
                e.addFrame(stateImages[i].img)
                val keepRouteNum = prevDest === nextDest && prevDest.routeGL.width > 0
                val routeNumImg: BufferedImage? = if (keepRouteNum) prevMtx.getSubimage(0, 0, prevDest.routeGL.width, prevMtx.height) else null
                for (f in 0 until framerate) {
                    val progress = f / framerate.toFloat()
                    val yOffset = (progress * mtx.height).toInt()
                    e.setDelay(((1000f * scrollAnimation) / framerate).roundToInt().coerceAtLeast(3))
                    g.composite = AlphaComposite.Clear
                    g.fillRect(0, 0, mtx.width, mtx.height)
                    g.composite = AlphaComposite.SrcOver
                    g.drawImage(prevMtx, 0, yOffset, null)
                    g.drawImage(nextMtx, 0, -mtx.height + yOffset, null)
                    if (routeNumImg != null) {
                        // Keep route number stationary
                        val routeWidth = prevDest.routeGL.width
                        g.composite = AlphaComposite.Clear
                        g.fillRect(0, 0, routeWidth, mtx.height)
                        g.composite = AlphaComposite.SrcOver
                        g.drawImage(routeNumImg, 0, 0, null)
                    }
                    mtx.changeToColor(ledColor)
                    e.addFrame(generateImageForMatrix(mtx))
                }
            }
            g.dispose()
        }
        e.finish()
        os.close()
    }

    fun generateGif(file: File) {
        file.createNewFile()
        generateGif(file.outputStream())
    }

    private fun BufferedImage.changeToColor(color: Color): BufferedImage {
        for (x in 0 until width) {
            for (y in 0 until height) {
                val rgb = getRGB(x, y)
                val alpha = rgb and 0xFF000000.toInt()
                if (alpha ushr 24 >= 80)
                    setRGB(x, y, (0xFF000000.toInt()) or color.rgb)
                else
                    setRGB(x, y, 0)
            }
        }
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

    data class Destination(val route: String, val routeFont: DotMtxFont,
                           val frames: List<DestinationFrame>,
                           val screenTimes: List<Float> = emptyList()) {

        val routeGL: GlyphLayout = GlyphLayout(routeFont, route)

        fun generateMatrix(width: Int, height: Int, frame: DestinationFrame): BufferedImage {
            return BufferedImage(width, height, BufferedImage.TYPE_4BYTE_ABGR).apply {
                val g = createGraphics()
                val hasRoute = route.isNotEmpty()
                if (hasRoute) {
                    val routeImg = routeGL.toBufferedImage()
                    g.drawImage(routeImg, 0, height / 2 - routeImg.height / 2, null as ImageObserver?)
                }
                val destWidth: Int = if (hasRoute) (width - routeGL.width) else width
                if (frame.dest2.isNotEmpty()) {
                    // Double line
                    val dest1 = GlyphLayout(frame.dest1Font, frame.dest1)
                    val dest1Img = dest1.toBufferedImage()
                    g.drawImage(dest1Img, (width - destWidth / 2f).toInt() - dest1Img.width / 2, 0, null as ImageObserver?)
                    val dest2 = GlyphLayout(frame.dest2Font, frame.dest2)
                    val dest2Img = dest2.toBufferedImage()
                    g.drawImage(dest2Img, (width - destWidth / 2f).toInt() - dest2Img.width / 2, height - dest2Img.height, null as ImageObserver?)
                } else {
                    // Single line
                    val dest1 = GlyphLayout(frame.dest1Font, frame.dest1)
                    val dest1Img = dest1.toBufferedImage()
                    g.drawImage(dest1Img, (width - destWidth / 2f).toInt() - dest1Img.width / 2, (height / 2 - dest1Img.height / 2f).roundToInt(), null as ImageObserver?)
                }
                g.dispose()
            }
        }
    }

    data class DestinationFrame(val dest1: String, val dest1Font: DotMtxFont,
                                val dest2: String = "", val dest2Font: DotMtxFont = dest1Font)

}