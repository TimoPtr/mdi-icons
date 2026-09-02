package io.github.timoptr.mdiicons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.addPathNodes
import androidx.compose.ui.unit.dp

/** Width and height of the viewport all MDI icons are drawn in. */
internal const val MDI_VIEWPORT_SIZE = 24f

/**
 * A Material Design Icons (MDI) icon: the [name] of the icon,
 * and its single SVG [pathData] in a square viewport of [MDI_VIEWPORT_SIZE].
 *
 * Instances come from the [Mdi] catalog.
 */
data class MdiIcon(val name: String, val pathData: String) {

    /**
     * Builds the icon as an [ImageVector] filled in black, tintable like the Material icons.
     * [autoMirror] flips the icon horizontally in right-to-left layouts, for directional icons
     * such as arrows. Composables use [rememberImageVector] instead, which caches the result for
     * the lifetime of the composition.
     */
    internal fun toImageVector(autoMirror: Boolean = false): ImageVector = ImageVector
        .Builder(
            name = name,
            defaultWidth = MDI_VIEWPORT_SIZE.dp,
            defaultHeight = MDI_VIEWPORT_SIZE.dp,
            viewportWidth = MDI_VIEWPORT_SIZE,
            viewportHeight = MDI_VIEWPORT_SIZE,
            autoMirror = autoMirror,
        ).addPath(
            pathData = addPathNodes(pathData),
            fill = SolidColor(Color.Black),
        ).build()
}
