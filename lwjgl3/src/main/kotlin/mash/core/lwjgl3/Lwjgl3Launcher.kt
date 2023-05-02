@file:JvmName("Lwjgl3Launcher")

package mash.core.lwjgl3

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration
import mash.core.PassTheMash
import mash.tester.bullet.VehicleFilterTest
import mash.tester.bullet.VehicleTest

/** Launches the desktop (LWJGL3) application. */
fun main() {
    Lwjgl3Application(PassTheMash(), Lwjgl3ApplicationConfiguration().apply {
        setTitle("pass-the-mash")
        setWindowedMode(1920, 1080)
        setWindowIcon(*(arrayOf(128, 64, 32, 16).map { "libgdx$it.png" }.toTypedArray()))
    })
}
