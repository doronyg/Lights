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

val Weekend: ImageVector
    get() {
        if (_weekend != null) {
            return _weekend!!
        }
        _weekend = materialIcon(name = "Filled.Weekend") {
            materialPath {
                moveTo(21.0f, 10.0f)
                curveToRelative(-1.1f, 0.0f, -2.0f, 0.9f, -2.0f, 2.0f)
                verticalLineToRelative(3.0f)
                horizontalLineTo(5.0f)
                verticalLineToRelative(-3.0f)
                curveToRelative(0.0f, -1.1f, -0.89f, -2.0f, -2.0f, -2.0f)
                reflectiveCurveToRelative(-2.0f, 0.9f, -2.0f, 2.0f)
                verticalLineToRelative(5.0f)
                curveToRelative(0.0f, 1.1f, 0.9f, 2.0f, 2.0f, 2.0f)
                horizontalLineToRelative(18.0f)
                curveToRelative(1.1f, 0.0f, 2.0f, -0.9f, 2.0f, -2.0f)
                verticalLineToRelative(-5.0f)
                curveTo(23.0f, 10.9f, 22.1f, 10.0f, 21.0f, 10.0f)
                close()
                moveTo(18.0f, 5.0f)
                horizontalLineTo(6.0f)
                curveTo(4.9f, 5.0f, 4.0f, 5.9f, 4.0f, 7.0f)
                verticalLineToRelative(2.15f)
                curveToRelative(1.16f, 0.41f, 2.0f, 1.52f, 2.0f, 2.81f)
                verticalLineTo(14.0f)
                horizontalLineToRelative(12.0f)
                verticalLineToRelative(-2.03f)
                curveToRelative(0.0f, -1.3f, 0.84f, -2.4f, 2.0f, -2.81f)
                verticalLineTo(7.0f)
                curveTo(20.0f, 5.9f, 19.1f, 5.0f, 18.0f, 5.0f)
                close()
            }
        }
        return _weekend!!
    }

private var _weekend: ImageVector? = null
