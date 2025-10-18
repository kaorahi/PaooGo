package io.github.karino2.paoogo.ui

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.accessibility.AccessibilityEvent.INVALID_POSITION
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Spinner
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.chibatching.kotpref.bulk
import com.github.salomonbrys.kodein.instance
import com.github.salomonbrys.kodein.lazy
import org.greenrobot.eventbus.EventBus
import org.ligi.gobandroid_hd.App
import org.ligi.gobandroid_hd.R
import org.ligi.gobandroid_hd.events.GameChangedEvent
import org.ligi.gobandroid_hd.logic.GoGame
import org.ligi.gobandroid_hd.model.GameProvider
import org.ligi.gobandroid_hd.ui.GoPrefs
import io.github.karino2.paoogo.ui.vs_engine.PlayAgainstEngineActivity
import io.github.karino2.paoogo.ui.AboutActivity
import kotlin.getValue

class GameStartActivity : AppCompatActivity() {
    val levelSpinner by lazy { findViewById<Spinner>(R.id.level_spinner) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        /*
        val katago = KataGoNative()
        val setup = KataGoSetup(this, assets)
        setup.extractFiles()
        katago.initNative(Runtime.getRuntime().availableProcessors(), setup.configFile.absolutePath, setup.modelFile.absolutePath)
        */
        /*
        val ray = RayNative()
        ray.initNative(Runtime.getRuntime().availableProcessors(), 1.0)
        ray.setupAssetParams(assets)
         */

        enableEdgeToEdge()
        setContentView(R.layout.activity_game_start)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }



        val levelAdapter = ArrayAdapter.createFromResource(this, R.array.level_array, android.R.layout.simple_spinner_item)
        levelAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        levelSpinner.adapter = levelAdapter

        with(GoPrefs) {
            engineLevel = engineLevel.coerceIn(1, levelAdapter.count)
            if(lastBoardSize == 13) findViewById<RadioButton>(R.id.board_size_13).isChecked = true
            if(engineLevel != 1) levelSpinner.setSelection(engineLevel - 1)
        }

        findViewById<Button>(R.id.start_button).setOnClickListener {
            val boardSize = if(findViewById<RadioGroup>(R.id.board_size_group).checkedRadioButtonId == R.id.board_size_9) 9 else 13
            val comLevel = levelSpinner.selectedItemPosition.let { if(it == INVALID_POSITION) 1 else it+1 }
            GoPrefs.bulk {
                lastBoardSize = boardSize
                engineLevel = comLevel
            }
            clearGame(boardSize)
            Intent(this@GameStartActivity, PlayAgainstEngineActivity::class.java).let { startActivity(it) }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?) =  super.onCreateOptionsMenu(menu.apply {
            menuInflater.inflate(R.menu.game_start, this)
        })

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId) {
            R.id.menu_about -> Intent(this, AboutActivity::class.java).let { startActivity(it) }
        }
        return super.onOptionsItemSelected(item)
    }

    val gameProvider: GameProvider by App.kodein.lazy.instance()
    fun clearGame(boardSize: Int) {
        gameProvider.set(
            GoGame(
                boardSize,
                0
            )
        )
        EventBus.getDefault().post(GameChangedEvent)
    }
}
