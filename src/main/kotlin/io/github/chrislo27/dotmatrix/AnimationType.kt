package io.github.chrislo27.dotmatrix


sealed class AnimationType(val delay: Float) {
    object NoAnimation : AnimationType(0f)
    class Falldown(delay: Float) : AnimationType(delay)
    class Sidewipe(delay: Float) : AnimationType(delay)
}