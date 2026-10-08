package com.doronyg.mylightscontroller.ui

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.ScrollbarStyle
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.v2.ScrollbarAdapter
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.abs

/**
 * [PlatformUi] base for mouse-driven platforms rendered by Skia (desktop and web):
 * visible, draggable scrollbars (wheel/trackpad scrolling works regardless).
 * They are shown while the content is taller than the viewport, in their own gutter.
 */
abstract class SkikoPlatformUi : PlatformUi {

    private val scrollbarThickness = 8.dp
    private val scrollbarMargin = 2.dp

    override val scrollbarGutter: Dp = scrollbarThickness + scrollbarMargin * 2

    /** Strong app-yellow thumb: the Compose default (12% black) is invisible in dark mode. */
    private val scrollbarStyle = ScrollbarStyle(
        minimalHeight = 24.dp,
        thickness = scrollbarThickness,
        shape = RoundedCornerShape(scrollbarThickness / 2),
        hoverDurationMillis = 200,
        unhoverColor = AppYellow.copy(alpha = 0.75f),
        hoverColor = AppYellow
    )

    @Composable
    private fun Scrollbar(adapter: ScrollbarAdapter, modifier: Modifier) =
        androidx.compose.foundation.VerticalScrollbar(
            adapter = adapter,
            modifier = modifier.padding(horizontal = scrollbarMargin),
            style = scrollbarStyle
        )

    @Composable
    override fun VerticalScrollbar(state: ScrollState, modifier: Modifier) =
        Scrollbar(rememberScrollbarAdapter(state), modifier)

    @Composable
    override fun VerticalScrollbar(state: LazyListState, modifier: Modifier) =
        Scrollbar(rememberScrollbarAdapter(state), modifier)

    @Composable
    override fun VerticalScrollbar(state: LazyGridState, modifier: Modifier) =
        Scrollbar(remember(state) { SpanAwareGridScrollbarAdapter(state) }, modifier)
}

/**
 * Scrollbar adapter for [LazyGridState] that handles items spanning several columns.
 *
 * The Compose adapter assumes `rows = itemCount / columns`, so full-width items
 * (e.g. dimmable lights that are on) make it underestimate the content height. The thumb
 * then fills the track and is drawn transparent. Here the content height is estimated
 * from the average pixels per item of the visible items, which accounts for spans and
 * for varying row heights.
 */
internal class SpanAwareGridScrollbarAdapter(private val state: LazyGridState) : ScrollbarAdapter {

    private val info get() = state.layoutInfo

    /** Average main-axis size per item (including spacing), from the visible items. */
    private val pixelsPerItem: Double
        get() {
            val visible = info.visibleItemsInfo
            if (visible.isEmpty()) return 0.0
            val first = visible.minBy { it.index }
            val last = visible.maxBy { it.offset.y + it.size.height }
            val extent = (last.offset.y + last.size.height) - first.offset.y + info.mainAxisItemSpacing
            val count = visible.maxOf { it.index } - first.index + 1
            return extent.toDouble() / count
        }

    override val viewportSize: Double
        get() = info.viewportSize.height.toDouble()

    override val contentSize: Double
        get() {
            val total = info.totalItemsCount
            if (total == 0) return 0.0
            return pixelsPerItem * total - info.mainAxisItemSpacing +
                info.beforeContentPadding + info.afterContentPadding
        }

    override val scrollOffset: Double
        get() = state.firstVisibleItemIndex * pixelsPerItem + state.firstVisibleItemScrollOffset

    override suspend fun scrollTo(scrollOffset: Double) {
        val target = scrollOffset.coerceIn(0.0, (contentSize - viewportSize).coerceAtLeast(0.0))
        val distance = target - this.scrollOffset
        if (abs(distance) <= viewportSize) {
            state.scrollBy(distance.toFloat())
        } else {
            val ppi = pixelsPerItem.takeIf { it > 0 } ?: return
            val index = (target / ppi).toInt().coerceIn(0, (info.totalItemsCount - 1).coerceAtLeast(0))
            state.scrollToItem(index, (target - index * ppi).toInt().coerceAtLeast(0))
        }
    }
}
