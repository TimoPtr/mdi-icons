import groovy.json.JsonSlurper
import java.io.File
import java.net.URI
import java.security.MessageDigest
import java.util.Base64
import javax.inject.Inject
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.file.ArchiveOperations
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.UntrackedTask

private const val NPM_REGISTRY_URL = "https://registry.npmjs.org/@mdi/svg"

/**
 * Generates the checked-in MDI icon catalog from the `@mdi/svg` npm package.
 *
 * Downloads the tarball of the version pinned in `gradle/libs.versions.toml`, verifies it against
 * the SHA-512 published by the npm registry, and generates Kotlin sources holding every icon's
 * SVG path data and name aliases. The output is committed so that builds never need the network.
 */
@UntrackedTask(because = "Generates checked-in sources from a remote package on demand")
abstract class UpdateMdiIconsTask : DefaultTask() {

    @get:Input
    abstract val mdiVersion: Property<String>

    @get:OutputDirectory
    abstract val outputDirectory: DirectoryProperty

    @get:Inject
    abstract val archiveOperations: ArchiveOperations

    @TaskAction
    fun update() {
        val version = mdiVersion.get()
        val tarball = downloadVerified(version)
        val icons = extractIcons(tarball)
        logger.lifecycle("Generating ${icons.size} MDI icons from @mdi/svg $version")
        MdiCatalogGenerator.generate(version, icons, outputDirectory.get().asFile)
    }

    private fun downloadVerified(version: String): File {
        val metadata = JsonSlurper().parseText(URI("$NPM_REGISTRY_URL/$version").toURL().readText()) as Map<*, *>
        val dist = metadata["dist"] as Map<*, *>
        val integrity = dist["integrity"] as String
        val tarballUrl = dist["tarball"] as String
        if (!integrity.startsWith("sha512-")) {
            throw GradleException("Unsupported integrity algorithm for @mdi/svg $version: $integrity")
        }

        val bytes = URI(tarballUrl).toURL().readBytes()
        val sha512 = Base64.getEncoder().encodeToString(MessageDigest.getInstance("SHA-512").digest(bytes))
        if (sha512 != integrity.removePrefix("sha512-")) {
            throw GradleException("Checksum mismatch for $tarballUrl: expected $integrity, got sha512-$sha512")
        }

        return File(temporaryDir, "mdi-svg-$version.tgz").apply { writeBytes(bytes) }
    }

    private fun extractIcons(tarball: File): List<MdiMetaIcon> {
        var metaJson: String? = null
        val svgPathByName = mutableMapOf<String, String>()

        archiveOperations.tarTree(tarball).visit {
            if (isDirectory) return@visit
            when {
                path == "package/meta.json" -> metaJson = file.readText()
                path.startsWith("package/svg/") && path.endsWith(".svg") -> {
                    val pathData = MdiCatalogGenerator.extractPathData(file.readText())
                        ?: throw GradleException("No path data found in $path")
                    svgPathByName[file.name.removeSuffix(".svg")] = pathData
                }
            }
        }

        val meta = JsonSlurper().parseText(
            metaJson ?: throw GradleException("meta.json not found in the @mdi/svg tarball"),
        ) as List<*>
        return MdiCatalogGenerator.buildIcons(meta.map { it as Map<*, *> }, svgPathByName)
    }
}

/**
 * Fails when the generated MDI catalog was not regenerated after a version bump: the version
 * recorded in the generated sources must match the `@mdi/svg` version pinned in
 * `gradle/libs.versions.toml`. Run `updateMdiIcons` and commit the result to fix a mismatch.
 *
 * This is deliberately the only content check that runs offline; full verification happens in
 * the layers around it: the mdi-update workflow regenerates and diffs the catalog, the compiler
 * proves the generated sources are valid, and the library tests prove their semantics.
 */
@UntrackedTask(because = "Cheap consistency check that should always run")
abstract class VerifyMdiIconsTask : DefaultTask() {

    @get:Input
    abstract val mdiVersion: Property<String>

    @get:InputDirectory
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val generatedDirectory: DirectoryProperty

    @TaskAction
    fun verify() {
        val generatedVersion = MdiCatalogGenerator.readGeneratedVersion(
            generatedDirectory.get().file("MdiMeta.kt").asFile,
        )
        if (generatedVersion != mdiVersion.get()) {
            throw GradleException(
                "The generated MDI catalog (version $generatedVersion) does not match the @mdi/svg version " +
                    "pinned in gradle/libs.versions.toml (${mdiVersion.get()}). " +
                    "Run ./gradlew :shared:updateMdiIcons and commit the regenerated sources.",
            )
        }
    }
}
