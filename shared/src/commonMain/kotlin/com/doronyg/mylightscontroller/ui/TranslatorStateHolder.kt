package com.doronyg.mylightscontroller.ui

import com.doronyg.mylightscontroller.platform.common.PlatformBridge
import com.doronyg.mylightscontroller.utils.AppTranslator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/**
 * Multiplatform translator state holder - NOT a ViewModel, works on any platform.
 * This replaces TranslatorViewModel for better KMP compatibility.
 */
class TranslatorStateHolder {

    private val _ready = MutableStateFlow(false)
    val ready: StateFlow<Boolean> = _ready

    init {
        CoroutineScope(PlatformBridge.dispatchers.uiDispatcher()).launch {
            AppTranslator.initialize()

            AppTranslator.preload(
                BottomTab.entries.map { it.title }
            )

            _ready.value = true
        }
    }
}

