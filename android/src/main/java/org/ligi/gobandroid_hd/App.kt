package org.ligi.gobandroid_hd

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import com.github.salomonbrys.kodein.Kodein
import com.github.salomonbrys.kodein.bind
import com.github.salomonbrys.kodein.instance
import com.github.salomonbrys.kodein.singleton
import io.github.karino2.paoogo.goengine.EngineRepository
import org.ligi.gobandroid_hd.model.GameProvider
import org.ligi.gobandroid_hd.ui.GoPrefs
import org.ligi.gobandroid_hd.ui.application.GoAndroidEnvironment
import org.ligi.gobandroid_hd.ui.application.GobandroidSettingsTransition
import org.ligi.tracedroid.TraceDroid

/**
 * the central Application-Context
 */
open class App : Application() {

    override fun onCreate() {
        super.onCreate()

        env = GoAndroidEnvironment(this@App)
        kodein = Kodein {
            bind<InteractionScope>() with singleton { InteractionScope() }
            bind<GoAndroidEnvironment>() with singleton { env }
            bind<GameProvider>() with singleton { GameProvider(instance()) }
            bind<App>() with singleton { this@App }
        }

        GobandroidSettingsTransition(this).transition()

        TraceDroid.init(this)

        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)
        AppCompatDelegate.setDefaultNightMode(GoPrefs.getThemeInt())
    }

    open val isTesting = false

    val engineRepository by lazy {
        EngineRepository(this, assets)
    }

    companion object {

        lateinit var kodein: Kodein
        lateinit var env: GoAndroidEnvironment
    }
}
