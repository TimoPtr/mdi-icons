package io.github.timoptr.mdiicons.sample

import androidx.compose.ui.window.ComposeUIViewController

// PascalCase is the convention for this factory, which Swift consumes as a view controller.
@Suppress("ktlint:standard:function-naming")
fun MainViewController() = ComposeUIViewController { App() }
