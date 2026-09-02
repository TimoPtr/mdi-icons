import java.io.File
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue
import org.gradle.api.GradleException

class MdiCatalogGeneratorTest {

    @Test
    fun `Given an svg file when extracting the path then the d attribute is returned`() {
        val svg = """<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24"><path d="M10 4A4 4Z" /></svg>"""

        assertEquals("M10 4A4 4Z", MdiCatalogGenerator.extractPathData(svg))
    }

    @Test
    fun `Given an svg without path data when extracting the path then null is returned`() {
        assertNull(MdiCatalogGenerator.extractPathData("<svg></svg>"))
    }

    @Test
    fun `Given meta entries when building icons then they are joined with their path and sorted by name`() {
        val icons = MdiCatalogGenerator.buildIcons(
            listOf(
                mapOf("name" to "beta", "aliases" to listOf("second")),
                mapOf("name" to "alpha"),
            ),
            mapOf("alpha" to "M1Z", "beta" to "M2Z"),
        )

        assertEquals(
            listOf(
                MdiMetaIcon("alpha", "M1Z", emptyList()),
                MdiMetaIcon("beta", "M2Z", listOf("second")),
            ),
            icons,
        )
    }

    @Test
    fun `Given a meta entry without svg when building icons then it fails naming the icon`() {
        val exception = assertFailsWith<GradleException> {
            MdiCatalogGenerator.buildIcons(listOf(mapOf("name" to "ghost")), emptyMap())
        }

        assertContains(exception.message.orEmpty(), "ghost")
    }

    @Test
    fun `Given icon names when converting to identifiers then pascal case and edge cases are handled`() {
        assertEquals("AccountAlert", "account-alert".toPascalCaseIdentifier())
        assertEquals("_3dRotation", "3d-rotation".toPascalCaseIdentifier())
        assertEquals("Package", "package".toPascalCaseIdentifier())
    }

    @Test
    fun `Given accessors when deriving backing val names then keywords and clashes get an underscore`() {
        assertEquals("accountAlert", "AccountAlert".toBackingValName())
        assertEquals("_package", "Package".toBackingValName())
        assertEquals("__3dRotation", "_3dRotation".toBackingValName())
    }

    @Test
    fun `Given icons colliding case insensitively when generating then it fails naming both icons`() {
        val exception = assertFailsWith<GradleException> {
            generate(icon("ab-c"), icon("a-bc"))
        }

        assertContains(exception.message.orEmpty(), "ab-c")
        assertContains(exception.message.orEmpty(), "a-bc")
    }

    @Test
    fun `Given unsorted icons when generating then it fails naming the offending icons`() {
        val exception = assertFailsWith<GradleException> {
            MdiCatalogGenerator.generate("1.2.3", listOf(icon("beta"), icon("alpha")), createTempDir())
        }

        assertContains(exception.message.orEmpty(), "beta")
        assertContains(exception.message.orEmpty(), "alpha")
    }

    @Test
    fun `Given aliases when generating then canonical names win and the first claim keeps an alias`() {
        val dir = generate(
            icon("alpha", aliases = listOf("beta", "shared-alias")),
            icon("beta"),
            icon("gamma", aliases = listOf("shared-alias")),
        )

        val aliases = dir.resolve("MdiAliases0.kt").readText()
        assertFalse("\"beta\" to" in aliases, "an alias shadowing a canonical name must be dropped")
        assertContains(aliases, "\"shared-alias\" to \"alpha\",")
    }

    @Test
    fun `Given path data with special characters when generating then it is escaped`() {
        val dir = generate(icon("tricky", pathData = """M1"2\3${'$'}4"""))

        assertContains(dir.resolve("MdiIcons0.kt").readText(), """MdiIcon("tricky", "M1\"2\\3${'$'}{'${'$'}'}4")""")
    }

    @Test
    fun `Given more icons than the chunk size when generating then chunks and the routing table match`() {
        val many = (0 until CHUNK_SIZE + 1).map { icon("icon-" + it.toString().padStart(4, '0')) }

        val dir = generate(*many.toTypedArray())

        assertTrue(dir.resolve("MdiIcons1.kt").exists())
        val meta = dir.resolve("MdiMeta.kt").readText()
        assertContains(meta, "MDI_CHUNK_COUNT: Int = 2")
        assertContains(meta, "\"icon-0000\",")
        assertContains(meta, "\"icon-0${CHUNK_SIZE}\",")
    }

    @Test
    fun `Given stale generated files when generating then the whole package is replaced`() {
        val dir = createTempDir().also {
            it.resolve("MdiIcons9.kt").writeText("stale")
            it.resolve("notes.txt").writeText("stale")
            it.resolve("icons").mkdirs()
            it.resolve("icons/Nested.kt").writeText("stale")
        }

        generate(icon("alpha"), outputDir = dir)

        assertFalse(dir.resolve("MdiIcons9.kt").exists())
        assertFalse(dir.resolve("notes.txt").exists())
        assertFalse(dir.resolve("icons").exists())
    }

    @Test
    fun `Given a generated catalog when reading the version then the pinned version is returned`() {
        val dir = generate(icon("alpha"))

        assertEquals("1.2.3", MdiCatalogGenerator.readGeneratedVersion(dir.resolve("MdiMeta.kt")))
        assertNull(MdiCatalogGenerator.readGeneratedVersion(dir.resolve("Missing.kt")))
    }

    private fun icon(name: String, pathData: String = "M0Z", aliases: List<String> = emptyList()) = MdiMetaIcon(name, pathData, aliases)

    private fun generate(vararg icons: MdiMetaIcon, outputDir: File = createTempDir()): File {
        MdiCatalogGenerator.generate("1.2.3", icons.sortedBy(MdiMetaIcon::name), outputDir)
        return outputDir
    }

    private fun createTempDir(): File = File.createTempFile("mdi-test", "").apply {
        delete()
        mkdirs()
        deleteOnExit()
    }
}
