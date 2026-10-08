package com.doronyg.mylightscontroller.desktop

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.size
import androidx.compose.ui.ImageComposeScene
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import com.doronyg.mylightscontroller.ha.HaIcon
import com.doronyg.mylightscontroller.ui.icons.ArrowBack
import com.doronyg.mylightscontroller.ui.icons.Redo
import com.doronyg.mylightscontroller.ui.toImageVector
import org.jetbrains.skia.Bitmap
import kotlin.test.Test
import kotlin.test.assertTrue

/** The Material icons copied into ui/icons must still draw something (no icon library involved). */
class IconsRenderTest {

    @Test
    fun everyIconDraws() {
        val vectors = HaIcon.entries.associate { it.name to it.toImageVector() } +
            mapOf("ArrowBack" to ArrowBack, "Redo" to Redo)

        val empty = vectors.filter { (_, vector) ->
            val size = 48
            val scene = ImageComposeScene(size, size, Density(1f)) {
                Image(
                    imageVector = vector,
                    contentDescription = null,
                    colorFilter = ColorFilter.tint(Color.Black),
                    modifier = Modifier.size(size.dp).background(Color.White)
                )
            }
            val bitmap = Bitmap.makeFromImage(scene.render())
            scene.close()
            val dark = (0 until size).sumOf { x ->
                (0 until size).count { y -> (bitmap.getColor(x, y) and 0xFF) < 0x80 }
            }
            dark < 20 // essentially blank
        }.keys

        assertTrue(empty.isEmpty(), "Icons that draw nothing: $empty")
    }
}

