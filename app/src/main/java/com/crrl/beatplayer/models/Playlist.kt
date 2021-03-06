/*
 * Copyright 2019 Carlos René Ramos López. All rights reserved.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.crrl.beatplayer.models

import android.database.Cursor
import com.google.gson.Gson

data class Playlist(
    val id: Long,
    val name: String,
    val songCount: Int
) : MediaItem(id) {
    companion object {
        fun fromCursor(cursor: Cursor, songCount: Int): Playlist {
            return Playlist(
                id = cursor.getLong(0),
                name = cursor.getString(1),
                songCount = songCount
            )
        }
    }

    override fun compare(other: MediaItem): Boolean {
        other as Playlist
        return id == other.id && name == other.name && songCount == other.songCount
    }

    override fun toString(): String {
        return Gson().toJson(this)
    }
}