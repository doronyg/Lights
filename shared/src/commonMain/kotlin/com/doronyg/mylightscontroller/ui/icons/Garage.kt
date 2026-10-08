/*
 * Copyright 2024 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

// Copied from androidx.compose.material:material-icons 1.7.3 (Apache 2.0).

package com.doronyg.mylightscontroller.ui.icons

import androidx.compose.ui.graphics.vector.ImageVector

val Garage: ImageVector
    get() {
        if (_garage != null) {
            return _garage!!
        }
        _garage = materialIcon(name = "Filled.Garage") {
            materialPath {
                moveTo(15.0f, 13.0f)
                moveToRelative(-1.0f, 0.0f)
                arcToRelative(1.0f, 1.0f, 0.0f, true, true, 2.0f, 0.0f)
                arcToRelative(1.0f, 1.0f, 0.0f, true, true, -2.0f, 0.0f)
            }
            materialPath {
                moveTo(9.0f, 13.0f)
                moveToRelative(-1.0f, 0.0f)
                arcToRelative(1.0f, 1.0f, 0.0f, true, true, 2.0f, 0.0f)
                arcToRelative(1.0f, 1.0f, 0.0f, true, true, -2.0f, 0.0f)
            }
            materialPath {
                moveTo(8.33f, 7.5f)
                lineToRelative(-0.66f, 2.0f)
                lineToRelative(8.66f, 0.0f)
                lineToRelative(-0.66f, -2.0f)
                close()
            }
            materialPath {
                moveTo(20.0f, 2.0f)
                horizontalLineTo(4.0f)
                curveTo(2.9f, 2.0f, 2.0f, 2.9f, 2.0f, 4.0f)
                verticalLineToRelative(16.0f)
                curveToRelative(0.0f, 1.1f, 0.9f, 2.0f, 2.0f, 2.0f)
                horizontalLineToRelative(16.0f)
                curveToRelative(1.1f, 0.0f, 2.0f, -0.9f, 2.0f, -2.0f)
                verticalLineTo(4.0f)
                curveTo(22.0f, 2.9f, 21.1f, 2.0f, 20.0f, 2.0f)
                close()
                moveTo(19.0f, 17.69f)
                curveToRelative(0.0f, 0.45f, -0.35f, 0.81f, -0.78f, 0.81f)
                horizontalLineToRelative(-0.44f)
                curveToRelative(-0.44f, 0.0f, -0.78f, -0.36f, -0.78f, -0.81f)
                verticalLineTo(16.5f)
                horizontalLineTo(7.0f)
                verticalLineToRelative(1.19f)
                curveToRelative(0.0f, 0.45f, -0.35f, 0.81f, -0.78f, 0.81f)
                horizontalLineTo(5.78f)
                curveTo(5.35f, 18.5f, 5.0f, 18.14f, 5.0f, 17.69f)
                verticalLineToRelative(-6.5f)
                curveTo(5.82f, 8.72f, 6.34f, 7.16f, 6.56f, 6.5f)
                curveToRelative(0.05f, -0.16f, 0.12f, -0.29f, 0.19f, -0.4f)
                curveTo(6.77f, 6.08f, 6.78f, 6.06f, 6.8f, 6.04f)
                curveTo(7.18f, 5.51f, 7.72f, 5.5f, 7.72f, 5.5f)
                horizontalLineToRelative(8.56f)
                curveToRelative(0.0f, 0.0f, 0.54f, 0.01f, 0.92f, 0.53f)
                curveToRelative(0.02f, 0.03f, 0.03f, 0.05f, 0.05f, 0.07f)
                curveToRelative(0.07f, 0.11f, 0.14f, 0.24f, 0.19f, 0.4f)
                curveToRelative(0.22f, 0.66f, 0.74f, 2.23f, 1.56f, 4.69f)
                verticalLineTo(17.69f)
                close()
            }
        }
        return _garage!!
    }

private var _garage: ImageVector? = null
