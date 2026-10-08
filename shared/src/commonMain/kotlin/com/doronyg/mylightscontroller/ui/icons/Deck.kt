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

val Deck: ImageVector
    get() {
        if (_deck != null) {
            return _deck!!
        }
        _deck = materialIcon(name = "Filled.Deck") {
            materialPath {
                moveTo(22.0f, 9.0f)
                lineToRelative(-10.0f, -7.0f)
                lineToRelative(-10.0f, 7.0f)
                lineToRelative(9.0f, 0.0f)
                lineToRelative(0.0f, 13.0f)
                lineToRelative(2.0f, 0.0f)
                lineToRelative(0.0f, -13.0f)
                close()
            }
            materialPath {
                moveTo(4.14f, 12.0f)
                lineToRelative(-1.96f, 0.37f)
                lineToRelative(0.82f, 4.37f)
                lineToRelative(0.0f, 5.26f)
                lineToRelative(2.0f, 0.0f)
                lineToRelative(0.02f, -4.0f)
                lineToRelative(1.98f, 0.0f)
                lineToRelative(0.0f, 4.0f)
                lineToRelative(2.0f, 0.0f)
                lineToRelative(0.0f, -6.0f)
                lineToRelative(-4.1f, 0.0f)
                close()
            }
            materialPath {
                moveTo(19.1f, 16.0f)
                lineToRelative(-4.1f, 0.0f)
                lineToRelative(0.0f, 6.0f)
                lineToRelative(2.0f, 0.0f)
                lineToRelative(0.0f, -4.0f)
                lineToRelative(1.98f, 0.0f)
                lineToRelative(0.02f, 4.0f)
                lineToRelative(2.0f, 0.0f)
                lineToRelative(0.0f, -5.26f)
                lineToRelative(0.82f, -4.37f)
                lineToRelative(-1.96f, -0.37f)
                close()
            }
        }
        return _deck!!
    }

private var _deck: ImageVector? = null
