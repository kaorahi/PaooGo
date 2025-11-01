package org.ligi.gobandroid_hd.ui

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.view.LayoutInflater
import android.widget.LinearLayout
import android.widget.ScrollView
import androidx.appcompat.app.AlertDialog
import androidx.core.content.res.ResourcesCompat
import com.github.salomonbrys.kodein.instance
import com.github.salomonbrys.kodein.lazy
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.ligi.gobandroid_hd.App
import org.ligi.gobandroid_hd.InteractionScope
import org.ligi.gobandroid_hd.InteractionScope.Mode.COUNT
import org.ligi.gobandroid_hd.InteractionScope.Mode.RECORD
import org.ligi.gobandroid_hd.R
import org.ligi.gobandroid_hd.databinding.DropdownItemBinding
import org.ligi.gobandroid_hd.databinding.TopNavAndExtrasBinding
import org.ligi.gobandroid_hd.events.GameChangedEvent
import org.ligi.gobandroid_hd.model.GameProvider
import timber.log.Timber

class CustomActionBar(private val activity: Activity) : LinearLayout(activity) {
    private var binding: TopNavAndExtrasBinding

    internal val gameProvider: GameProvider by App.kodein.lazy.instance()
    internal val interactionScope: InteractionScope by App.kodein.lazy.instance()

    private val inflater: LayoutInflater = activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

    private val highlightColor: Int = ResourcesCompat.getColor(resources, R.color.dividing_color, null)
    private val transparent: Int = ResourcesCompat.getColor(resources, android.R.color.transparent, null)


    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        EventBus.getDefault().register(this)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        EventBus.getDefault().unregister(this)
    }

    init {
        binding = TopNavAndExtrasBinding.inflate(inflater,this,true)

        refresh()
    }

    @Subscribe
    fun onGoGameChaged(@Suppress("UNUSED_PARAMETER") event: GameChangedEvent) {
        refresh()
    }

    private fun refresh() {
        post {
            val game = gameProvider.get()

            binding.whiteCapturesTv.text = game.capturesWhite.toString()
            binding.blackCapturesTv.text = game.capturesBlack.toString()

            val isWhitesMove = !game.isBlackToMove && !game.isFinished
            binding.whiteStoneImageview.setBackgroundColor(if (isWhitesMove) highlightColor else transparent)
            binding.whiteCapturesTv.setBackgroundColor(if (isWhitesMove) highlightColor else transparent)

            val isBlacksMove = game.isBlackToMove || game.isFinished
            binding.blackStoneImageView.setBackgroundColor(if (isBlacksMove) highlightColor else transparent)
            binding.blackCapturesTv.setBackgroundColor(if (isBlacksMove) highlightColor else transparent)
        }
    }
}
