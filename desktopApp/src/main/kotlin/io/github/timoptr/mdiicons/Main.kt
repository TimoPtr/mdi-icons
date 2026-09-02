package io.github.timoptr.mdiicons

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import io.github.timoptr.mdiicons.sample.App

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "mdi-icons",
    ) {
        App()
    }
}
