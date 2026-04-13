package com.doronyg.mylightscontroller.ui.Navigator

sealed class UiEvent {
    data class NavigateTo(val screen: Screen) : UiEvent()
    data object NavigateBack : UiEvent()
}