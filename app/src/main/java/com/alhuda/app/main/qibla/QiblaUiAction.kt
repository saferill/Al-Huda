package com.alhuda.app.main.qibla

sealed interface QiblaUiAction {
    object OnUnderstoodClick : QiblaUiAction
    object OnUseCompassClick : QiblaUiAction
}
