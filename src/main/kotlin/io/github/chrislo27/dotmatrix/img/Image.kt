package io.github.chrislo27.dotmatrix.img

import java.awt.image.BufferedImage


class Image {

    val width: Int
    val height: Int
    val backing: BufferedImage

    constructor(width: Int, height: Int) {
        this.width = width
        this.height = height
        this.backing = BufferedImage(width, height, BufferedImage.TYPE_4BYTE_ABGR)
    }

    constructor(backing: BufferedImage) {
        this.backing = backing
        this.width = backing.width
        this.height = backing.height
    }

}
