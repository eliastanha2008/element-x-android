package io.element.android.features.home.api

import io.element.android.libraries.matrix.api.user.MatrixUser
import kotlinx.coroutines.flow.MutableStateFlow

/**
 * Shared UI state so the HamGap bottom bar can persist above the Settings flow.
 * The Home screen publishes its state here; the persistent bar reads it.
 */
object HamGapUiBus {
    val currentUserAndNeighbors = MutableStateFlow<List<MatrixUser>>(emptyList())
    val showAvatarIndicator = MutableStateFlow(false)
    val selectedTabIndex = MutableStateFlow(0)

    /** Pair of (tabIndex, requestSequence). The Home screen applies each new request. */
    val tabRequest = MutableStateFlow(0 to 0)

    fun requestTab(index: Int) {
        tabRequest.value = index to tabRequest.value.second + 1
    }
}
