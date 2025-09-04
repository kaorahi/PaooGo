package io.github.karino2.paoogo.ui

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
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
import org.ligi.gobandroid_hd.InteractionScope.Mode.GNUGO2
import org.ligi.gobandroid_hd.R
import org.ligi.gobandroid_hd.events.GameChangedEvent
import org.ligi.gobandroid_hd.logic.GoGame
import org.ligi.gobandroid_hd.model.GameProvider
import org.ligi.gobandroid_hd.ui.GoPrefs
import org.ligi.gobandroid_hd.ui.GoPrefs.lastBoardSize
import org.ligi.gobandroid_hd.ui.recording.GameRecordActivity
import org.ligi.gobandroid_hd.ui.vs_engine.PlayAgainstEngineActivity
import kotlin.getValue

class GameStartActivity : AppCompatActivity() {
    val levelSpinner by lazy { findViewById<Spinner>(R.id.level_spinner) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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

        findViewById<Button>(R.id.start_button).setOnClickListener {
            val boardSize = if(findViewById<RadioGroup>(R.id.board_size_group).checkedRadioButtonId == R.id.board_size_9) 9 else 13
            GoPrefs.bulk {
                lastBoardSize = boardSize
            }
            clearGame(boardSize)
            Intent(this@GameStartActivity, PlayAgainstEngineActivity::class.java).let { startActivity(it) }
        }
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