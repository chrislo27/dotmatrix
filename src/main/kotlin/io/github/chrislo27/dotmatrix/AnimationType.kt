package io.github.chrislo27.dotmatrix


sealed class AnimationType(val delay: Float, val wide: Boolean) {

    object Inherit : AnimationType(0f, false)

    object NoAnimation : AnimationType(0f, false)

    class Falldown(delay: Float) : AnimationType(delay, false)

    class Sidewipe(delay: Float) : AnimationType(delay, true)

    class HorizontalScroll(delay: Float) : AnimationType(delay, true)

}