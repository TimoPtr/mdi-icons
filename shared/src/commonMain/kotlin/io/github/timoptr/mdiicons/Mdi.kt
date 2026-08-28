package io.github.timoptr.mdiicons

import io.github.timoptr.mdiicons.generated.MDI_CHUNK_COUNT
import io.github.timoptr.mdiicons.generated.mdiAliases
import io.github.timoptr.mdiicons.generated.mdiChunkFirstNames
import io.github.timoptr.mdiicons.generated.mdiIconChunk

/**
 * Catalog of the MDI icons generated from the `@mdi/svg` package by the `updateMdiIcons` task.
 *
 * Statically referenced icons use the generated accessors, for instance `Mdi.AccountAlert`; icon
 * names received at runtime resolve through [fromMdiName]. Both return the same [MdiIcon]
 * instance per icon.
 *
 * The catalog is split in alphabetically sorted chunks and a lookup only loads the chunk holding
 * the requested name, keeping the retained memory proportional to the icons actually used.
 */
object Mdi {
    private val chunkMaps = arrayOfNulls<Map<String, MdiIcon>>(MDI_CHUNK_COUNT)

    /**
     * Every icon of the catalog, for listing purposes such as an icon picker. Loads all chunks
     * on first access; sessions that only resolve individual names never pay for it.
     */
    val icons: List<MdiIcon> by lazy { (0 until MDI_CHUNK_COUNT).flatMap { chunkMap(it).values } }

    /**
     * Resolves an MDI icon name (ie "account-alert") to its icon, following historical
     * renames, or null when the name is unknown or was removed from MDI.
     */
    fun fromMdiName(mdiName: String): MdiIcon? {
        return lookup(mdiName) ?: mdiRenamedNames[mdiName]?.let(::lookup)
    }

    private fun lookup(name: String): MdiIcon? = lookupCanonical(name) ?: mdiAliases[name]?.let(::lookupCanonical)

    private fun lookupCanonical(name: String): MdiIcon? {
        // The chunk that can hold the name is the last one starting at or before it.
        val searchResult = mdiChunkFirstNames.binarySearch(name)
        val chunkIndex = if (searchResult >= 0) searchResult else -searchResult - 2
        if (chunkIndex < 0) return null
        return chunkMap(chunkIndex)[name]
    }

    // Unsynchronized on purpose: a concurrent first access builds the same map twice, which is
    // harmless since both are built from the same generated chunk.
    private fun chunkMap(index: Int): Map<String, MdiIcon> =
        chunkMaps[index] ?: mdiIconChunk(index).associateBy(MdiIcon::name).also { chunkMaps[index] = it }
}
