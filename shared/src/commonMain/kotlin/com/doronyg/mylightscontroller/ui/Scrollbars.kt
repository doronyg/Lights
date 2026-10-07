package com.doronyg.mylightscontroller.ui

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

/*
 * Wrap a scrollable container so pointer-driven platforms (desktop, web) get a visible,
 * draggable scrollbar on the right edge. On touch platforms the scrollbar is a no-op.
 *
 * Usage:
 * ```
 * val state = rememberLazyListState()
 * WithVerticalScrollbar(state) { LazyColumn(state = state) { ... } }
 * ```
 */

@Composable
fun WithVerticalScrollbar(
    state: ScrollState,
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) = ScrollbarLayout(modifier, { LocalPlatformUi.current.VerticalScrollbar(state, it) }, content)

@Composable
fun WithVerticalScrollbar(
    state: LazyListState,
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) = ScrollbarLayout(modifier, { LocalPlatformUi.current.VerticalScrollbar(state, it) }, content)

@Composable
fun WithVerticalScrollbar(
    state: LazyGridState,
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) = ScrollbarLayout(modifier, { LocalPlatformUi.current.VerticalScrollbar(state, it) }, content)

/**
 * Content with the scrollbar in its own gutter on the right, so it never covers content.
 * The gutter width comes from the platform (0 on touch platforms -> layout unchanged).
 */
@Composable
private fun ScrollbarLayout(
    modifier: Modifier,
    scrollbar: @Composable (Modifier) -> Unit,
    content: @Composable BoxScope.() -> Unit
) = Box(modifier) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(end = LocalPlatformUi.current.scrollbarGutter),
        content = content
    )
    scrollbar(Modifier.align(Alignment.CenterEnd).fillMaxHeight())
}

/**
 * A vertically scrollable [Column] with a platform scrollbar.
 * [modifier] applies to the outer container (size, padding, insets).
 */
@Composable
fun ScrollableColumn(
    modifier: Modifier = Modifier,
    scrollState: ScrollState = rememberScrollState(),
    content: @Composable ColumnScope.() -> Unit
) = WithVerticalScrollbar(scrollState, modifier) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState),
        content = content
    )
}
