package io.github.chrislo27.dotmatrix.img



actual class Image {

    actual val width: Int
    actual val height: Int
//    val backing: BufferedImage

    actual constructor(width: Int, height: Int) {
        this.width = width
        this.height = height
//        this.backing = BufferedImage(width, height, BufferedImage.TYPE_4BYTE_ABGR)
    }

//    constructor(backing: BufferedImage) {
//        this.backing = backing
//        this.width = backing.width
//        this.height = backing.height
//    }

}