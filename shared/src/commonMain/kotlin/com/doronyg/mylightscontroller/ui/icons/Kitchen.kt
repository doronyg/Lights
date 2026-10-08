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

val Kitchen: ImageVector
    get() {
        if (_kitchen != null) {
            return _kitchen!!
        }
        _kitchen = materialIcon(name = "Filled.Kitchen") {
            materialPath {
                moveTo(20.0f, 9.0f)
                verticalLineTo(4.0f)
                curveToRelative(0.0f, -1.1f, -0.9f, -2.0f, -2.0f, -2.0f)
                horizontalLineTo(6.0f)
                curveTo(4.9f, 2.0f, 4.0f, 2.9f, 4.0f, 4.0f)
                verticalLineToRelative(5.0f)
                horizontalLineTo(20.0f)
                close()
                moveTo(8.0f, 5.0f)
                horizontalLineToRelative(2.0f)
                verticalLineToRelative(3.0f)
                horizontalLineTo(8.0f)
                verticalLineTo(5.0f)
                close()
            }
            materialPath {
                moveTo(4.0f, 11.0f)
                verticalLineToRelative(9.0f)
                curveToRelative(0.0f, 1.1f, 0.9f, 2.0f, 2.0f, 2.0f)
                horizontalLineToRelative(12.0f)
                curveToRelative(1.1f, 0.0f, 2.0f, -0.9f, 2.0f, -2.0f)
                verticalLineToRelative(-9.0f)
                horizontalLineTo(4.0f)
                close()
                moveTo(10.0f, 17.0f)
                horizontalLineTo(8.0f)
                verticalLineToRelative(-5.0f)
                horizontalLineToRelative(2.0f)
                verticalLineTo(17.0f)
                close()
            }
        }
        return _kitchen!!
    }

private var _kitchen: ImageVector? = null
