package io.github.timoptr.mdiicons

import io.github.timoptr.mdiicons.generated.AccountAlert
import io.github.timoptr.mdiicons.generated.ArrowLeft
import io.github.timoptr.mdiicons.generated.Lightbulb
import io.github.timoptr.mdiicons.generated.mdiAliases
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertSame
import kotlin.test.assertTrue

class MdiIconTest {
    @Test
    fun `Given an MDI name when resolving then the matching icon is returned`() {
        assertEquals(Mdi.AccountAlert, Mdi.fromMdiName("account-alert"))
    }

    @Test
    fun `Given an unknown MDI name when resolving then null is returned`() {
        assertNull(Mdi.fromMdiName("does-not-exist"))
    }

    @Test
    fun `Given an alias when resolving then the canonical icon is returned`() {
        mdiAliases.forEach { (alias, canonical) ->
            assertSame(
                Mdi.fromMdiName(canonical),
                Mdi.fromMdiName(alias),
                "Alias $alias does not resolve to the same icon as $canonical",
            )
        }
    }

    @Test
    fun `Given a static accessor when resolving the same name then the same instance is returned`() {
        assertSame(Mdi.AccountAlert, Mdi.fromMdiName("account-alert"))
    }

    @Test
    fun `Given a historically renamed name when resolving then the current icon is returned`() {
        assertSame(Mdi.fromMdiName("badge-account-alert"), Mdi.fromMdiName("account-badge-alert"))
    }

    @Test
    fun `Given the renamed names table then every entry resolves to an icon in the catalog`() {
        mdiRenamedNames.forEach { (old, new) ->
            assertNotNull(Mdi.fromMdiName(new), "Renamed icon $old points at $new which no longer resolves")
        }
    }

    @Test
    fun `Given an icon when building the image vector then the MDI viewport is kept`() {
        val vector = Mdi.Lightbulb.toImageVector()

        assertEquals(24f, vector.viewportWidth)
        assertEquals(24f, vector.viewportHeight)
    }

    @Test
    fun `Given an icon when building the image vector then it does not auto-mirror by default`() {
        assertFalse(Mdi.Lightbulb.toImageVector().autoMirror)
    }

    @Test
    fun `Given a directional icon when building the image vector with autoMirror then it mirrors in RTL`() {
        assertTrue(Mdi.ArrowLeft.toImageVector(autoMirror = true).autoMirror)
    }
}
