package com.doronyg.mylightscontroller.ui.navigator

import androidx.compose.runtime.mutableStateListOf

class AppNavigator {

    private val _stack = mutableStateListOf<Screen>()
    val stack: List<Screen> get() = _stack

    val current: Screen?
        get() = _stack.lastOrNull()

    fun handle(event: UiEvent) {
        when (event) {
            is UiEvent.NavigateTo -> push(event.screen)
            UiEvent.NavigateBack -> pop()
            is UiEvent.Replace -> replace(event.screen)
            is UiEvent.SetRoot -> setRoot(event.screen)
        }
    }

    private fun push(screen: Screen) {
        _stack.add(screen)
    }

    private fun pop() {
        if (_stack.size > 1) {
            _stack.removeAt(_stack.lastIndex)
        }
    }

    private fun replace(screen: Screen) {
        if (_stack.isNotEmpty()) {
            _stack[_stack.lastIndex] = screen
        } else {
            _stack.add(screen)
        }
    }

    fun setRoot(screen: Screen) {
        _stack.clear()
        _stack.add(screen)
    }
}