package com.doronyg.mylightscontroller.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.doronyg.mylightscontroller.utils.AppTranslator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class TranslatorViewModel : ViewModel() {

    private val _ready = MutableStateFlow(false)
    val ready: StateFlow<Boolean> = _ready

    init {
        viewModelScope.launch {
            AppTranslator.initialize()

            AppTranslator.preload(
                BottomTab.entries.map { it.title }
            )

            _ready.value = true
        }
    }
}