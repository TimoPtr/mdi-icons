package io.github.timoptr.mdiicons.sample

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.github.timoptr.mdiicons.Mdi
import io.github.timoptr.mdiicons.MdiIcon
import io.github.timoptr.mdiicons.rememberImageVector

@Composable
@Preview
fun App() {
    MaterialTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            IconCatalog()
        }
    }
}

/** Searchable gallery of the complete MDI catalog, the showcase of every sample app. */
@Composable
private fun IconCatalog() {
    val icons = remember { Mdi.icons }
    var query by remember { mutableStateOf("") }
    var selected by remember { mutableStateOf<MdiIcon?>(null) }
    val filtered = remember(query) {
        val terms =
            query
                .trim()
                .lowercase()
                .split(' ')
                .filter(String::isNotEmpty)
        if (terms.isEmpty()) icons else icons.filter { icon -> terms.all(icon.name::contains) }
    }

    Column(modifier = Modifier.fillMaxSize().safeContentPadding().padding(8.dp)) {
        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            label = { Text("Search ${icons.size} icons") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        ) {
            val icon = selected
            if (icon != null) {
                Icon(
                    imageVector = icon.rememberImageVector(),
                    contentDescription = null,
                    modifier = Modifier.size(32.dp),
                )
                Text(text = icon.name, style = MaterialTheme.typography.titleMedium)
            } else {
                Text(
                    text = "${filtered.size} icons, tap one to see its name",
                    style = MaterialTheme.typography.titleMedium,
                )
            }
        }
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 44.dp),
            modifier = Modifier.fillMaxSize(),
        ) {
            items(filtered, key = MdiIcon::name) { icon ->
                IconButton(onClick = { selected = icon }) {
                    Icon(
                        imageVector = icon.rememberImageVector(),
                        contentDescription = icon.name,
                    )
                }
            }
        }
    }
}
