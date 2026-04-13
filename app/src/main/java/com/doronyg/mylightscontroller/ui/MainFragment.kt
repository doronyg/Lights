package com.doronyg.mylightscontroller.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment

class MainFragment : Fragment() {
    private var repoToken: Int? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                MyTheme {
                    MainScreen()
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

