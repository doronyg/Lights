package com.doronyg.mylightscontroller.ui

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.doronyg.mylightscontroller.platform.common.IPlatformQrScanner

/**
 * The few UI hooks that differ per platform. Everything else in this package is
 * platform-independent Compose.
 *
 * The host (Android: MainFragment) provides an implementation via [LocalPlatformUi]:
 * ```
 * CompositionLocalProvider(LocalPlatformUi provides AndroidPlatformUi) { MainScreen() }
 * ```
 */
interface PlatformUi {
    /** System back gesture/button handling. */
    @Composable
    fun BackHandler(enabled: Boolean, onBack: () -> Unit)

    /** Creates (and remembers) the platform QR scanner for the current screen. */
    @Composable
    fun rememberQrScanner(): PlatformQrScanner

    /*
     * Visible vertical scrollbars for pointer-driven platforms (desktop, web).
     * Default: none (touch platforms show their own scroll indicators / don't need one).
     */

    /** Space reserved on the right of scrollable content for the scrollbar (0 = none). */
    val scrollbarGutter: Dp get() = 0.dp

    @Composable
    fun VerticalScrollbar(state: ScrollState, modifier: Modifier) {}

    @Composable
    fun VerticalScrollbar(state: LazyListState, modifier: Modifier) {}

    @Composable
    fun VerticalScrollbar(state: LazyGridState, modifier: Modifier) {}
}

/** A platform QR scanner together with the composable that shows its camera preview. */
class PlatformQrScanner(
    val scanner: IPlatformQrScanner,
    val preview: @Composable (Modifier) -> Unit
)

val LocalPlatformUi = staticCompositionLocalOf<PlatformUi> {
    error("No PlatformUi provided. Wrap the UI in CompositionLocalProvider(LocalPlatformUi provides ...)")
}

/** System back gesture/button handling, delegated to the current [PlatformUi]. */
@Composable
fun PlatformBackHandler(enabled: Boolean, onBack: () -> Unit) {
    LocalPlatformUi.current.BackHandler(enabled, onBack)
}

/** The platform QR scanner for the current screen, delegated to the current [PlatformUi]. */
@Composable
fun rememberPlatformQrScanner(): PlatformQrScanner =
    LocalPlatformUi.current.rememberQrScanner()

