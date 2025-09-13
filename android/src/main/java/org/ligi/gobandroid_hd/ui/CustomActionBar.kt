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
import org.ligi.gobandroid_hd.InteractionScope.Mode.EDIT
import org.ligi.gobandroid_hd.InteractionScope.Mode.GNUGO
import org.ligi.gobandroid_hd.InteractionScope.Mode.GNUGO2
import org.ligi.gobandroid_hd.InteractionScope.Mode.RECORD
import org.ligi.gobandroid_hd.InteractionScope.Mode.REVIEW
import org.ligi.gobandroid_hd.InteractionScope.Mode.SETUP
import org.ligi.gobandroid_hd.InteractionScope.Mode.TSUMEGO
import org.ligi.gobandroid_hd.R
import org.ligi.gobandroid_hd.databinding.DropdownItemBinding
import org.ligi.gobandroid_hd.databinding.TopNavAndExtrasBinding
import org.ligi.gobandroid_hd.events.GameChangedEvent
import org.ligi.gobandroid_hd.model.GameProvider
import org.ligi.gobandroid_hd.ui.gnugo.GnuGoHelper
import org.ligi.gobandroid_hd.ui.ingame_common.SwitchModeHelper
import timber.log.Timber

class CustomActionBar(private val activity: Activity) : LinearLayout(activity) {
    private var binding: TopNavAndExtrasBinding

    private val GooglePlayStorePackageNameOld = "com.google.market"
    private val GooglePlayStorePackageNameNew = "com.android.vending"

    internal val gameProvider: GameProvider by App.kodein.lazy.instance()
    internal val interactionScope: InteractionScope by App.kodein.lazy.instance()

    private val inflater: LayoutInflater = activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
    private val app: App = activity.applicationContext as App

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
        binding.modeTv.setOnClickListener {
            showModePopup(activity)
        }
        binding.moveTv.setOnClickListener {
            showModePopup(activity)
        }
    }

    private fun addItem(container: LinearLayout, image_resId: Int, str_resid: Int, listener: Runnable) {
        val bd = DropdownItemBinding.inflate(inflater,container,false)
        bd.text.setText(str_resid)
        bd.image.setImageResource(image_resId)
        bd.clickContainer.setOnClickListener { listener.run() }
        container.addView(bd.root)
    }

    private fun addModeItem(container: LinearLayout, mode: InteractionScope.Mode, string_res: Int, icon_res: Int, pop: BetterPopupWindow) {
        if (mode === interactionScope.mode) {
            return  // already in this mode - no need to present the user with this option
        }

        addItem(container, icon_res, string_res, Runnable {
            pop.dismiss()

            if (mode === GNUGO && !GnuGoHelper.isGnuGoAvail(activity)) {
                AlertDialog.Builder(activity).setTitle(R.string.install_gnugo)
                        .setMessage(R.string.gnugo_not_installed)
                        .setPositiveButton(android.R.string.ok) { _, _ ->
                            val intent = Intent(Intent.ACTION_VIEW)
                            intent.data = Uri.parse("market://details?id=org.ligi.gobandroidhd.ai.gnugo")
                            val chooser = Intent.createChooser(intent, null)
                            activity.startActivity(chooser)
                        }
                        .setNegativeButton(android.R.string.cancel, null).show()
                return@Runnable
            }
            activity.finish()
            Timber.i("set mode" + mode)
            interactionScope.mode = mode
            val i = SwitchModeHelper.getIntentByMode(app, mode)
            activity.startActivity(i)
        })
    }

    private fun showModePopup(ctx: Context) {

        val pop = BetterPopupWindow(binding.modeTv)

        val scrollView = ScrollView(ctx)
        val contentView = LinearLayout(ctx)
        contentView.orientation = VERTICAL
        val background = BitmapDrawableNoMinimumSize(ctx.resources, R.drawable.wood_bg)
        contentView.background = background

        addModeItem(contentView, SETUP, R.string.setup, R.drawable.ic_action_settings_overscan, pop)

        addModeItem(contentView, RECORD, R.string.play, R.drawable.ic_social_people, pop)

        addModeItem(contentView, EDIT, R.string.edit, R.drawable.ic_editor_mode_edit, pop)

        val actMove = gameProvider.get().actMove
        if (actMove.hasNextMove() || actMove.parent != null)
            addModeItem(contentView, REVIEW, R.string.review, R.drawable.ic_maps_local_movies, pop)

        if (actMove.movePos > 0) {
            // these modes only make sense if there is minimum one
            addModeItem(contentView, COUNT, R.string.count, R.drawable.ic_editor_pie_chart, pop)
        }

        if (actMove.hasNextMove()) {
            addModeItem(contentView, TSUMEGO, R.string.tsumego, R.drawable.ic_action_extension, pop)
        }

        if (isPlayStoreInstalled() || GnuGoHelper.isGnuGoAvail(activity)) {
            addModeItem(contentView, GNUGO, R.string.gnugo, R.drawable.ic_hardware_computer, pop)
        }
        addModeItem(contentView, GNUGO2, R.string.gnugo2, R.drawable.ic_hardware_computer, pop)

        scrollView.addView(contentView)
        pop.setContentView(scrollView)

        pop.showLikePopDownMenu()
    }

    @Subscribe
    fun onGoGameChaged(@Suppress("UNUSED_PARAMETER") event: GameChangedEvent) {
        refresh()
    }

    private fun refresh() {
        post {
            val actMode = interactionScope.mode

            binding.modeTv.setText(actMode.getStringRes())

            val game = gameProvider.get()

            binding.whiteCapturesTv.text = game.capturesWhite.toString()
            binding.blackCapturesTv.text = game.capturesBlack.toString()

            val isWhitesMove = !game.isBlackToMove && !game.isFinished
            binding.whiteInfoContainer.setBackgroundColor(if (isWhitesMove) highlightColor else transparent)
            binding.whiteCapturesTv.setBackgroundColor(if (isWhitesMove) highlightColor else transparent)

            val isBlacksMove = game.isBlackToMove || game.isFinished
            binding.blackStoneImageView.setBackgroundColor(if (isBlacksMove) highlightColor else transparent)
            binding.blackCapturesTv.setBackgroundColor(if (isBlacksMove) highlightColor else transparent)

            binding.moveTv.text = app.resources.getString(R.string.move) + game.actMove.movePos
        }
    }

    private fun isPlayStoreInstalled(): Boolean {
        val packageManager = app.packageManager
        val packages = packageManager.getInstalledPackages(PackageManager.MATCH_UNINSTALLED_PACKAGES)
        return packages.any() { it.packageName == GooglePlayStorePackageNameOld || it.packageName == GooglePlayStorePackageNameNew }
    }
}
