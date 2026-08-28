package io.github.timoptr.mdiicons

import android.graphics.Color
import io.github.timoptr.mdiicons.generated.Lightbulb
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.GraphicsMode

private const val BITMAP_SIZE = 48

// NATIVE graphics is required: in the legacy mode canvas draws are silent no-ops.
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class MdiIconBitmapTest {
    @Test
    fun `Given an icon when drawing to a bitmap then pixels are filled with the requested color`() {
        val bitmap = Mdi.Lightbulb.toBitmap(size = BITMAP_SIZE, color = Color.RED)

        val pixels =
            IntArray(BITMAP_SIZE * BITMAP_SIZE).also {
                bitmap.getPixels(it, 0, BITMAP_SIZE, 0, 0, BITMAP_SIZE, BITMAP_SIZE)
            }
        assertTrue(pixels.any { it == Color.RED })
    }
}
