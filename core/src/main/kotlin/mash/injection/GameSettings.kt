package mash.injection

class GameSettings(
    val gameWidth: Float = 36f, aspectRatio: Float = 16f / 9f, val pixelsPerMeter: Float = 4f,
    val timeStep: Float = 1 / 60f, val velocityIterations: Int = 16, val positionIterations: Int = 6
) {
    val fieldOfView = 60f
    val cameraNear = 0.02f
    val cameraFar = 1000f

    val gameHeight = aspectRatio * gameWidth
    val metersPerPixel = 1f / pixelsPerMeter
}