package com.alhuda.app.intro

import android.net.Uri
import com.alhuda.app.core.presentation.navigation.Route

sealed interface IntroUiAction {
    data object OnBackClick : IntroUiAction
    data object OnNextClick : IntroUiAction
    data object OnSkipClick : IntroUiAction
    data object OnSkipConfirmed : IntroUiAction
    data object OnSkipDismiss : IntroUiAction
    data object OnFinishClick : IntroUiAction

    data class OnRouteVisible(
        val route: Route,
    ) : IntroUiAction

    data class OnRestoreBackup(
        val uri: Uri,
    ) : IntroUiAction
}
