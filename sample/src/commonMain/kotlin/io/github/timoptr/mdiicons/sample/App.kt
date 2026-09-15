package io.github.timoptr.mdiicons.sample

import androidx.annotation.VisibleForTesting
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import io.github.timoptr.mdiicons.Mdi
import io.github.timoptr.mdiicons.MdiIcon
import io.github.timoptr.mdiicons.generated.Github
import io.github.timoptr.mdiicons.generated.WeatherNight
import io.github.timoptr.mdiicons.generated.WeatherSunny
import io.github.timoptr.mdiicons.rememberImageVector
import io.github.timoptr.mdiicons.sample.resources.Res
import io.github.timoptr.mdiicons.sample.resources.app_name
import io.github.timoptr.mdiicons.sample.resources.app_subtitle
import io.github.timoptr.mdiicons.sample.resources.search_label
import io.github.timoptr.mdiicons.sample.resources.switch_to_dark_theme
import io.github.timoptr.mdiicons.sample.resources.switch_to_light_theme
import io.github.timoptr.mdiicons.sample.resources.view_on_github
import io.github.timoptr.mdiicons.sample.resources.visible_icons
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource

private const val REPOSITORY_URL = "https://github.com/TimoPtr/mdi-icons"

@Composable
fun App() {
    // Follows the system theme until the user picks one with the top bar button.
    var darkThemeOverride by rememberSaveable { mutableStateOf<Boolean?>(null) }
    val darkTheme = darkThemeOverride ?: isSystemInDarkTheme()
    MaterialTheme(colorScheme = if (darkTheme) darkColorScheme() else lightColorScheme()) {
        Scaffold(
            topBar = {
                CatalogTopBar(
                    darkTheme = darkTheme,
                    onToggleTheme = { darkThemeOverride = !darkTheme },
                )
            },
            contentWindowInsets = WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal),
        ) { contentPadding ->
            IconCatalog(
                modifier =
                Modifier
                    .fillMaxSize()
                    .padding(contentPadding)
                    .consumeWindowInsets(contentPadding),
            )
        }
    }
}

/** The sample title and what the library is, with the link to the repository. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CatalogTopBar(darkTheme: Boolean, onToggleTheme: () -> Unit, modifier: Modifier = Modifier) {
    TopAppBar(
        title = {
            Column {
                Text(text = stringResource(Res.string.app_name))
                Text(
                    text = stringResource(Res.string.app_subtitle),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        },
        actions = {
            ThemeToggleButton(darkTheme = darkTheme, onToggleTheme = onToggleTheme)
            GithubButton(modifier = Modifier.padding(end = 8.dp))
        },
        modifier = modifier,
    )
}

/** Switches between the light and dark theme, showing the theme it switches to. */
@Composable
private fun ThemeToggleButton(darkTheme: Boolean, onToggleTheme: () -> Unit, modifier: Modifier = Modifier) {
    IconButton(onClick = onToggleTheme, modifier = modifier) {
        Icon(
            imageVector = (if (darkTheme) Mdi.WeatherSunny else Mdi.WeatherNight).rememberImageVector(),
            contentDescription =
            stringResource(
                if (darkTheme) Res.string.switch_to_light_theme else Res.string.switch_to_dark_theme,
            ),
        )
    }
}

/** Opens the repository of the library. */
@Composable
private fun GithubButton(modifier: Modifier = Modifier) {
    val uriHandler = LocalUriHandler.current
    OutlinedButton(onClick = { uriHandler.openUri(REPOSITORY_URL) }, modifier = modifier) {
        Icon(
            imageVector = Mdi.Github.rememberImageVector(),
            contentDescription = null,
            modifier = Modifier.size(ButtonDefaults.IconSize),
        )
        Spacer(modifier = Modifier.width(ButtonDefaults.IconSpacing))
        Text(stringResource(Res.string.view_on_github))
    }
}

/** Searchable gallery of the complete MDI catalog, the showcase of every sample app. */
@Composable
@VisibleForTesting
internal fun IconCatalog(modifier: Modifier = Modifier) {
    val icons = Mdi.icons
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

    IconCatalogContent(
        iconCount = icons.size,
        query = query,
        onQueryChange = { query = it },
        selectedIcon = selected,
        onIconSelected = { selected = it },
        visibleIcons = filtered,
        modifier = modifier,
    )
}

@Composable
private fun IconCatalogContent(
    iconCount: Int,
    query: String,
    onQueryChange: (String) -> Unit,
    selectedIcon: MdiIcon?,
    onIconSelected: (MdiIcon) -> Unit,
    visibleIcons: List<MdiIcon>,
    modifier: Modifier = Modifier,
) {
    // A narrower column shows fewer icons at once, which keeps scrolling smooth on wide screens.
    // No bottom padding: the grid scrolls behind the navigation bar, edge to edge.
    Column(
        modifier =
        modifier
            .wrapContentWidth()
            .widthIn(max = 960.dp)
            .padding(start = 8.dp, top = 8.dp, end = 8.dp),
    ) {
        SearchBar(
            iconCount = iconCount,
            query = query,
            onQueryChange = onQueryChange,
            modifier = Modifier.fillMaxWidth(),
        )
        SelectionStatus(
            selectedIcon = selectedIcon,
            visibleIconCount = visibleIcons.size,
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        )
        IconGrid(
            icons = visibleIcons,
            onIconSelected = onIconSelected,
            modifier = Modifier.fillMaxSize(),
        )
    }
}

@Composable
private fun SearchBar(iconCount: Int, query: String, onQueryChange: (String) -> Unit, modifier: Modifier = Modifier) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        label = { Text(stringResource(Res.string.search_label, iconCount)) },
        singleLine = true,
        modifier = modifier,
    )
}

/** Shows the selected icon with its name, or how many icons match the search. */
@Composable
private fun SelectionStatus(selectedIcon: MdiIcon?, visibleIconCount: Int, modifier: Modifier = Modifier) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier,
    ) {
        if (selectedIcon != null) {
            Icon(
                imageVector = selectedIcon.rememberImageVector(),
                contentDescription = null,
                modifier = Modifier.size(32.dp),
            )
            Text(text = selectedIcon.name, style = MaterialTheme.typography.titleMedium)
        } else {
            Text(
                text = pluralStringResource(Res.plurals.visible_icons, visibleIconCount, visibleIconCount),
                style = MaterialTheme.typography.titleMedium,
            )
        }
    }
}

@Composable
private fun IconGrid(icons: List<MdiIcon>, onIconSelected: (MdiIcon) -> Unit, modifier: Modifier = Modifier) {
    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 72.dp),
        // Icons scroll behind the navigation bar, while the last row still ends above it.
        contentPadding = WindowInsets.safeDrawing.only(WindowInsetsSides.Bottom).asPaddingValues(),
        modifier = modifier,
    ) {
        items(icons, key = MdiIcon::name) { icon ->
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.aspectRatio(1f).clickable { onIconSelected(icon) },
            ) {
                Icon(
                    imageVector = icon.rememberImageVector(),
                    contentDescription = icon.name,
                    modifier = Modifier.size(36.dp),
                )
            }
        }
    }
}

@PreviewLightDark
@Composable
private fun IconCatalogContentPreview() {
    MaterialTheme {
        App()
    }
}
