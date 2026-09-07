package io.element.android.features.home.impl.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.zIndex
import io.element.android.features.home.api.HamGapUiBus
import io.element.android.features.home.impl.HomeNavigationBarItem
import io.element.android.libraries.designsystem.theme.components.HorizontalFloatingToolbar
import io.element.android.libraries.designsystem.theme.components.HorizontalFloatingToolbarItem
import io.element.android.libraries.designsystem.theme.components.HorizontalFloatingToolbarSeparator
import kotlinx.collections.immutable.toPersistentList

/**
 * Persistent HamGap bottom bar: avatar + Chats + Spaces.
 * It is rendered above the current screen and stays visible on the Settings flow.
 */
@Composable
fun HamGapGlobalBottomBar(
    visible: Boolean,
    onAvatarClick: () -> Unit,
    onTabClick: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (!visible) return
    val users by HamGapUiBus.currentUserAndNeighbors.collectAsState()
    val showAvatarIndicator by HamGapUiBus.showAvatarIndicator.collectAsState()
    val selectedTabIndex by HamGapUiBus.selectedTabIndex.collectAsState()
    Box(modifier = modifier.fillMaxSize()) {
        HorizontalFloatingToolbar(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .windowInsetsPadding(WindowInsets.navigationBars)
                .zIndex(1f),
        ) {
            // Only render the avatar once user data is available: the pager crashes on an empty list.
            if (users.isNotEmpty()) {
                NavigationIcon(
                    currentUserAndNeighbors = users.toPersistentList(),
                    showAvatarIndicator = showAvatarIndicator,
                    onAccountSwitch = { },
                    onClick = onAvatarClick,
                )
                HorizontalFloatingToolbarSeparator()
            }
            HomeNavigationBarItem.entries.forEachIndexed { index, item ->
                if (index > 0) {
                    HorizontalFloatingToolbarSeparator()
                }
                val isSelected = selectedTabIndex == index
                HorizontalFloatingToolbarItem(
                    icon = item.icon(isSelected),
                    tooltipLabel = stringResource(item.labelRes),
                    isSelected = isSelected,
                    onClick = { onTabClick(index) },
                )
            }
        }
    }
}
