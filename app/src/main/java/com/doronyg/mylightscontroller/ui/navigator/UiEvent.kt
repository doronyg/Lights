package com.doronyg.mylightscontroller.ui.navigator

sealed class UiEvent {
    data class NavigateTo(val screen: Screen) : UiEvent()
    data object NavigateBack : UiEvent()
    data class Replace(val screen: Screen) : UiEvent()
    data class SetRoot(val screen: Screen) : UiEvent()
}