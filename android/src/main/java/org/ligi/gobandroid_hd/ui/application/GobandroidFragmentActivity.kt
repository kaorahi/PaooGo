package org.ligi.gobandroid_hd.ui.application

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.KeyEvent
import android.view.ViewConfiguration
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import com.github.salomonbrys.kodein.instance
import com.github.salomonbrys.kodein.lazy
import org.ligi.gobandroid_hd.App
import org.ligi.gobandroid_hd.InteractionScope
import org.ligi.gobandroid_hd.databinding.BaseContainerBinding
import org.ligi.gobandroid_hd.logic.GoGame
import org.ligi.gobandroid_hd.model.GameProvider

/*
    Once this was a main drawer related activity.
    But now drawer is removed, this activity is somewhat empty.
 */
open class GobandroidFragmentActivity : AppCompatActivity() {

    val env: GoAndroidEnvironment by App.kodein.lazy.instance()
    val interactionScope: InteractionScope by App.kodein.lazy.instance()
    val gameProvider: GameProvider by App.kodein.lazy.instance()

    //private var drawerLayout: DrawerLayout? = null
    lateinit var pbinding: BaseContainerBinding

    @SuppressLint("SoonBlockedPrivateApi")
    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        pbinding = BaseContainerBinding.inflate(layoutInflater)
        setContentView(pbinding.root)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        // a little hack because I strongly disagree with the style guide here
        // ;-)
        // not having the Actionbar overflow menu also with devices with hardware
        // key really helps discoverability
        // http://stackoverflow.com/questions/9286822/how-to-force-use-of-overflow-menu-on-devices-with-menu-button
        try {
            val config = ViewConfiguration.get(this)
            val menuKeyField =
                ViewConfiguration::class.java.getDeclaredField("sHasPermanentMenuKey")
            menuKeyField.isAccessible = true
            menuKeyField.setBoolean(config, false)
        } catch (ignored: Exception) {
            // Ignore - but at least we tried ;-)
        }

        // we do not want focus on custom views ( mainly for GTV )
        if (supportActionBar != null && supportActionBar!!.customView != null) {
            this.supportActionBar!!.customView.isFocusable = false
        }


    }

    override fun setContentView(layoutResId: Int) {
        layoutInflater.inflate(layoutResId, pbinding.contentFrame)
    }

    open fun doFullScreen(): Boolean {
        return false
    }

    override fun onResume() {
        super.onResume()

        //NaDra mMenuDrawer.refresh();

        if (doFullScreen()) {
            window.setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
            )
        } else {
            window.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
        }
    }

    val app: App
        get() = applicationContext as App

    val game: GoGame
        get() = gameProvider.get()


    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        if (keyCode == KeyEvent.KEYCODE_WINDOW) {
            return false
        }
        return super.onKeyDown(keyCode, event)
    }
}
