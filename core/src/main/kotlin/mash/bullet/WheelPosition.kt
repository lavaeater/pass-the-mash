package mash.bullet

sealed class WheelPosition(val leftOrRight: LeftRight, val frontOrBack: FrontBack) {
    val isFrontWheel get() = frontOrBack == FrontBack.Front

    object FrontLeft : WheelPosition(LeftRight.Left, FrontBack.Front)
    object FrontRight : WheelPosition(LeftRight.Right, FrontBack.Front)
    object BackLeft : WheelPosition(LeftRight.Left, FrontBack.Back)
    object BackRight : WheelPosition(LeftRight.Right, FrontBack.Back)

    companion object {
        val directions = listOf(FrontLeft, FrontRight, BackLeft, BackRight)
    }
}