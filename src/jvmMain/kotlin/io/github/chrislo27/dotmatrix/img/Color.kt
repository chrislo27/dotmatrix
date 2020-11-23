package io.github.chrislo27.dotmatrix.img


fun Color.toAWTColor(): java.awt.Color = java.awt.Color(this.r, this.g, this.b, this.a)

fun java.awt.Color.toDmxColor(): Color = Color(this.red, this.green, this.blue, this.alpha)
