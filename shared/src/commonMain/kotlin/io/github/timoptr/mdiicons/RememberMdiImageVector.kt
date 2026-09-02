package io.github.timoptr.mdiicons

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Builds the icon as an [ImageVector], kept for the lifetime of the composition: the icon of a
 * visible composable is built once, and released when it leaves the screen.
 */
@Composable
fun MdiIcon.rememberImageVector(): ImageVector = remember(this) { toImageVector() }
