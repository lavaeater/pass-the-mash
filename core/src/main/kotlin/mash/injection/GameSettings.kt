package mash.injection

class GameSettings(
    val frameBufferWidth: Float = 640f, aspectRatio: Float = 16f / 9f, val pixelsPerMeter: Float = 4f,
    val timeStep: Float = 1 / 60f, val velocityIterations: Int = 16, val positionIterations: Int = 6
) {
    val fieldOfView = 60f
    val cameraNear = -25f
    val cameraFar = 100f

    val gameHeight = aspectRatio * frameBufferWidth / 2f
    val viewPortWidth = frameBufferWidth / 20f
    val viewPortHeight = gameHeight / 20f
    val metersPerPixel = 1f / pixelsPerMeter
}