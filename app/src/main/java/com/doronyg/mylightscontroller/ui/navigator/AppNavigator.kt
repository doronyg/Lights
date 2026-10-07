package com.doronyg.mylightscontroller.ui.navigator

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class AppNavigator {

    private val _stack = mutableListOf<Screen>()

    private val _stackState = MutableStateFlow(_stack.toList())
    val stack: StateFlow<List<Screen>> = _stackState.asStateFlow()

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
        updateState()
    }

    private fun pop() {
        if (_stack.size > 1) {
            _stack.removeAt(_stack.lastIndex)
            updateState()
        }
    }

    private fun replace(screen: Screen) {
        if (_stack.isNotEmpty()) {
            _stack[_stack.lastIndex] = screen
        } else {
            _stack.add(screen)
        }
        updateState()
    }

    fun setRoot(screen: Screen) {
        _stack.clear()
        _stack.add(screen)
        updateState()
    }

    private fun updateState() {
        _stackState.value = _stack.toList()
    }
}