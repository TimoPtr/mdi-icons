package io.github.timoptr.mdiicons

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.Paint
import android.util.TypedValue
import androidx.annotation.ColorInt
import androidx.annotation.Dimension
import androidx.annotation.Px
import androidx.compose.ui.graphics.asAndroidPath
import androidx.compose.ui.graphics.vector.PathParser

/**
 * Draws the icon into a square [Bitmap] of [size] pixels filled with [color], for surfaces that
 * cannot render Compose such as notifications, quick settings tiles and widgets. Returns a fresh
 * bitmap on each call, safe to draw on; building is cheap compared to posting to a system
 * surface, so callers should not cache it.
 */
fun MdiIcon.toBitmap(@Px size: Int, @ColorInt color: Int): Bitmap {
    val path = PathParser().parsePathString(pathData).toPath().asAndroidPath()
    path.transform(Matrix().apply { setScale(size / MDI_VIEWPORT_SIZE, size / MDI_VIEWPORT_SIZE) })
    val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply { this.color = color }
    return Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888).also {
        Canvas(it).drawPath(path, paint)
    }
}

/** [toBitmap] with the square size given in density-independent pixels. */
fun MdiIcon.toBitmap(context: Context, @Dimension(unit = Dimension.DP) sizeDp: Int, @ColorInt color: Int): Bitmap =
    toBitmap(
        TypedValue
            .applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                sizeDp.toFloat(),
                context.resources.displayMetrics,
            ).toInt(),
        color,
    )
