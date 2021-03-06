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

package com.crrl.beatplayer.ui.fragments.base

import android.graphics.Typeface
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.crrl.beatplayer.R
import com.crrl.beatplayer.alertdialog.AlertDialog
import com.crrl.beatplayer.alertdialog.dialogs.AlertItemAction
import com.crrl.beatplayer.alertdialog.stylers.AlertItemStyle
import com.crrl.beatplayer.alertdialog.stylers.AlertItemTheme
import com.crrl.beatplayer.alertdialog.stylers.AlertType
import com.crrl.beatplayer.alertdialog.stylers.InputStyle
import com.crrl.beatplayer.extensions.getColorByTheme
import com.crrl.beatplayer.extensions.safeActivity
import com.crrl.beatplayer.interfaces.ItemClickListener
import com.crrl.beatplayer.models.Playlist
import com.crrl.beatplayer.models.Song
import com.crrl.beatplayer.repository.PlaylistRepository
import com.crrl.beatplayer.ui.fragments.PlaylistDetailFragment
import com.crrl.beatplayer.utils.PlayerConstants
import com.skydoves.powermenu.MenuAnimation
import com.skydoves.powermenu.OnMenuItemClickListener
import com.skydoves.powermenu.PowerMenu
import com.skydoves.powermenu.PowerMenuItem


open class BaseFragment<T> : Fragment(), ItemClickListener<T> {

    protected lateinit var dialog: AlertDialog
    private lateinit var alertPlaylists: AlertDialog
    private var currentItem: T? = null

    protected var powerMenu: PowerMenu? = null

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        init()
    }

    private fun init() {
        Thread {
            powerMenu = initPopUpMenu().setOnMenuItemClickListener(onMenuItemClickListener).build()
        }.start()
    }

    protected fun buildPlaylistMenu(playlists: List<Playlist>, song: Song) {
        val style = AlertItemStyle().apply {
            textColor = activity?.getColorByTheme(R.attr.titleTextColor, "titleTextColor")!!
            selectedTextColor = activity?.getColorByTheme(R.attr.colorAccent, "colorAccent")!!
            backgroundColor =
                activity?.getColorByTheme(R.attr.colorPrimarySecondary, "colorPrimarySecondary")!!
        }
        val alert = AlertDialog(
            getString(R.string.playlists),
            getString(R.string.choose_playlist),
            style,
            AlertType.BOTTOM_SHEET
        ).apply {
            playlists.forEach { playlist ->
                addItem(AlertItemAction(playlist.name, false) {
                    addToList(playlist.id, song)
                })
            }
            addItem(
                AlertItemAction(
                    getString(R.string.new_playlist),
                    false,
                    AlertItemTheme.ACCEPT
                ) {
                    createPlayList(song)
                })
        }
        alertPlaylists = alert
    }

    private fun createDialog(song: Song): AlertDialog {
        val style = InputStyle(
            activity?.getColorByTheme(R.attr.colorPrimarySecondary, "colorPrimarySecondary")!!,
            activity!!.getColorByTheme(R.attr.colorPrimarySecondary2, "colorPrimarySecondary2"),
            activity!!.getColorByTheme(R.attr.titleTextColor, "titleTextColor"),
            activity!!.getColorByTheme(R.attr.colorPrimaryOpacity, "colorPrimaryOpacity"),
            activity!!.getColorByTheme(R.attr.colorAccent, "colorAccent")
        )
        return AlertDialog(
            getString(R.string.new_playlist),
            getString(R.string.create_playlist),
            style,
            AlertType.INPUT,
            getString(R.string.input_hint)
        ).apply {
            addItem(AlertItemAction("Cancel", false, AlertItemTheme.CANCEL) {
            })
            addItem(AlertItemAction("OK", false, AlertItemTheme.ACCEPT) {
                val id = PlaylistRepository.getInstance(context)!!.createPlaylist(it.input)
                if (id != -1L) {
                    addToList(id, song)
                    Toast.makeText(
                        context,
                        getString(R.string.playlist_added_success),
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    Toast.makeText(
                        context,
                        "${getString(R.string.playlist_added_error)} ${it.input}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            })
        }
    }

    private fun initPopUpMenu(): PowerMenu.Builder {
        return PowerMenu.Builder(context)
            .addItem(PowerMenuItem(getString(R.string.play), false))
            .addItem(
                PowerMenuItem(
                    if (tag == PlayerConstants.PLAY_LIST_DETAIL) getString(R.string.remove) else getString(
                        R.string.add
                    ), false
                )
            )
            .addItem(PowerMenuItem(getString(R.string.share), false))
            .addItem(PowerMenuItem(getString(R.string.delete), false))
            .setAnimation(MenuAnimation.SHOWUP_TOP_RIGHT)
            .setMenuRadius(this.resources.getDimension(R.dimen.popupMenuRadius))
            .setOnBackgroundClickListener { powerMenu!!.dismiss() }
            .setMenuShadow(5f)
            .setShowBackground(false)
            .setTextColor(activity!!.getColorByTheme(R.attr.titleTextColor, "titleTextColor"))
            .setTextGravity(Gravity.CENTER)
            .setTextSize(16)
            .setTextTypeface(Typeface.createFromAsset(context!!.assets, "fonts/rubik.ttf"))
            .setSelectedTextColor(activity!!.getColorByTheme(R.attr.colorAccent, "colorAccent"))
            .setMenuColor(
                activity!!.getColorByTheme(
                    R.attr.colorPrimarySecondary,
                    "colorPrimarySecondary"
                )
            )
            .setSelectedMenuColor(
                activity!!.getColorByTheme(
                    R.attr.colorPrimarySecondary,
                    "colorPrimarySecondary"
                )
            )
    }

    private fun createPlayList(song: Song) {
        createDialog(song).show(activity as AppCompatActivity)
    }

    private val onMenuItemClickListener = OnMenuItemClickListener<PowerMenuItem> { position, _ ->
        when (position) {
            0 -> {
            }
            1 -> {
                try {
                    if (this is PlaylistDetailFragment) removeFromList(
                        binding.playlist!!.id,
                        currentItem
                    ) else alertPlaylists.show(safeActivity as AppCompatActivity)
                } catch (ex: IllegalStateException) {
                    Log.println(Log.ERROR, "Exception", ex.message!!)
                }
            }
            2 -> {
            }
            3 -> {
            }
        }
        powerMenu!!.dismiss()
    }

    open fun onBackPressed(): Boolean {
        return if (powerMenu != null) {
            if (powerMenu!!.isShowing) {
                powerMenu!!.dismiss()
                false
            } else {
                true
            }
        } else {
            true
        }
    }

    open fun addToList(playListId: Long, song: Song) {}
    open fun removeFromList(playListId: Long, item: T?) {}
    override fun onItemClick(view: View, position: Int, item: T) {}
    override fun onPopupMenuClick(view: View, position: Int, item: T) {
        currentItem = item
    }

    override fun onShuffleClick(view: View) {}
    override fun onSortClick(view: View) {}
    override fun onPlayAllClick(view: View) {}
}

