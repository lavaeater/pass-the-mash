@file:JvmName("Lwjgl3Launcher")

package mash.core.lwjgl3

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration
import mash.core.PassTheMash

/** Launches the desktop (LWJGL3) application. */
fun main() {
    Lwjgl3Application(PassTheMash(), Lwjgl3ApplicationConfiguration().apply {
        setTitle("pass-the-mash")
        setWindowedMode(640, 480)
        setWindowIcon(*(arrayOf(128, 64, 32, 16).map { "libgdx$it.png" }.toTypedArray()))
    })
}
