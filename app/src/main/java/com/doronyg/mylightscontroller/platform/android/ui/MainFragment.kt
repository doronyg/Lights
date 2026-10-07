package com.doronyg.mylightscontroller.platform.android.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import com.doronyg.mylightscontroller.ha.HomeAssistantRepository
import com.doronyg.mylightscontroller.ui.LocalPlatformUi
import com.doronyg.mylightscontroller.ui.MainScreen
import com.doronyg.mylightscontroller.ui.MyTheme

class MainFragment : Fragment() {
    private var repoToken: Int? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                CompositionLocalProvider(LocalPlatformUi provides AndroidPlatformUi) {
                    MyTheme {
                        MainScreen()
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        repoToken = HomeAssistantRepository.instance.registerUi()
    }

    override fun onPause() {
        super.onPause()
        repoToken?.let {
            HomeAssistantRepository.instance.unregisterUi(it)
            repoToken = null
        }
    }
}

