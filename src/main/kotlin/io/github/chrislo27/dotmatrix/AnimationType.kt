package io.github.chrislo27.dotmatrix


/**
 * This class indicates the transitional animation between frames.
 */
sealed class AnimationType(val delay: Float) {

    /**
     * Indicates that the animation should inherit the animation type from its parent.
     */
    object Inherit : AnimationType(0f)

    /**
     * Indicates that there is no animation.
     */
    object NoAnimation : AnimationType(0f)

    /**
     * This animation has the next frame sliding from the top, displacing the previous frame.
     */
    class Falldown(delay: Float) : AnimationType(delay)

    /**
     * This animation has the next frame sliding from the bottom, displacing the previous frame.
     */
    class Fallup(delay: Float) : AnimationType(delay)

    /**
     * This animation has the next frame appearing left-to-right, like a flipdot display.
     */
    class Sidewipe(delay: Float) : AnimationType(delay)

    class HorizontalScroll(delay: Float) : AnimationType(delay)

}