package io.element.android.features.home.impl.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import io.element.android.compound.theme.ElementTheme
import io.element.android.features.home.api.HamGapUiBus
import io.element.android.features.home.impl.HomeNavigationBarItem
import io.element.android.libraries.designsystem.atomic.atoms.RedIndicatorAtom
import io.element.android.libraries.designsystem.components.avatar.Avatar
import io.element.android.libraries.designsystem.components.avatar.AvatarSize
import io.element.android.libraries.designsystem.components.avatar.AvatarType
import io.element.android.libraries.designsystem.theme.components.HorizontalFloatingToolbar
import io.element.android.libraries.designsystem.theme.components.HorizontalFloatingToolbarItem
import io.element.android.libraries.designsystem.theme.components.HorizontalFloatingToolbarSeparator
import io.element.android.libraries.matrix.ui.model.getAvatarData
import io.element.android.libraries.ui.strings.CommonStrings

/**
 * Persistent HamGap bottom bar: avatar + Chats + Spaces.
 * It is rendered above the current screen and stays visible on the Settings flow.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun HamGapGlobalBottomBar(
    visible: Boolean,
    avatarSelected: Boolean,
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
            // Only render the avatar once user data is available.
            if (users.isNotEmpty()) {
                val currentUser = users.getOrElse(1) { users.first() }
                val avatarData by remember(currentUser) {
                    derivedStateOf { currentUser.getAvatarData(size = AvatarSize.CurrentUserTopBar) }
                }
                Box {
                    // Same pill style as the Chats/Spaces buttons.
                    FilledIconButton(
                        modifier = Modifier.widthIn(min = 56.dp),
                        colors = if (avatarSelected) {
                            IconButtonDefaults.filledIconButtonColors().copy(
                                containerColor = ElementTheme.colors.bgCanvasDefault,
                                contentColor = ElementTheme.colors.iconPrimary,
                            )
                        } else {
                            IconButtonDefaults.filledIconButtonColors().copy(
                                containerColor = Color.Transparent,
                                contentColor = ElementTheme.colors.iconSecondary,
                            )
                        },
                        onClick = onAvatarClick,
                    ) {
                        Avatar(
                            avatarData = avatarData,
                            avatarType = AvatarType.User,
                            contentDescription = stringResource(CommonStrings.common_settings),
                        )
                    }
                    if (showAvatarIndicator) {
                        RedIndicatorAtom(
                            modifier = Modifier.align(Alignment.TopEnd)
                        )
                    }
                }
                HorizontalFloatingToolbarSeparator()
            }
            HomeNavigationBarItem.entries.forEachIndexed { index, item ->
                if (index > 0) {
                    HorizontalFloatingToolbarSeparator()
                }
                val isSelected = !avatarSelected && selectedTabIndex == index
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
