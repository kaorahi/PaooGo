package org.ligi.gobandroid_hd.ui.vs_engine

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.MotionEvent
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import io.github.karino2.paoogo.goengine.gnugo2.GnuGo2Native
import io.github.karino2.paoogo.ui.GameStartActivity
import org.greenrobot.eventbus.Subscribe
import org.ligi.gobandroid_hd.R
import org.ligi.gobandroid_hd.events.GameChangedEvent
import org.ligi.gobandroid_hd.events.OptionsItemClickedEvent
import org.ligi.gobandroid_hd.logic.Cell
import org.ligi.gobandroid_hd.logic.GoDefinitions
import org.ligi.gobandroid_hd.logic.GoGame
import org.ligi.gobandroid_hd.logic.GoMove
import org.ligi.gobandroid_hd.ui.GoActivity
import org.ligi.gobandroid_hd.ui.GoPrefs
import timber.log.Timber


/*
    Similar to PlayAgainstGnuGoActivity, but use local engine instead.
 */
class PlayAgainstEngineActivity : GoActivity() {
    private val engineGoGame by lazy { EngineGoGame(false, true, game, getString(R.string.gnugo2)) }
    private val engine by lazy {  GnuGo2Native().apply { initNative() } }

    private var running = false
    private var syncing = false;

    override fun doTouch(event: MotionEvent) {
        if (engineGoGame.engineNowBlack() or engineGoGame.engineNowWhite()) {
            showInfoToast(R.string.not_your_turn)
        } else {
            super.doTouch(event)
        }
    }

    override fun onPause() {
        running = false
        super.onPause()
    }
    val handler = Handler(Looper.getMainLooper())

    override fun onResume() {
        super.onResume()

        // Timber.plant(Timber.DebugTree())
        running = true
        setupEngine()

        Thread( {
            while(running) {
                SystemClock.sleep(100)
                handler.post { engineTick() }
            }
        }).start()
    }

    private fun engineTick()
    {
        if (syncing || engineGoGame.aiIsThinking)
            return
        if (engineGoGame.engineNowBlack() || engineGoGame.engineNowWhite() ) {
            genMove()
        }
    }

    private fun setupEngine() {
        val depth = if(GoPrefs.engineLevel == 3) 0 else 14
        engine.setDepth(depth)
        engine.setKomi(game.komi)
        engine.setBoardSize(game.boardSize)
        engine.clearBoard()
    }

    public override fun doMoveWithUIFeedback(cell: Cell?): GoGame.MoveStatus {
        if (cell != null) {
            if (engineGoGame.aiIsThinking) {
                Toast.makeText(this, R.string.ai_is_thinking, Toast.LENGTH_SHORT).show()
                return GoGame.MoveStatus.VALID
            }
            manualMove(cell)
        }

        return super.doMoveWithUIFeedback(cell)
    }

    private fun manualMove(cell: Cell) {
        if(!engine.doMove(cell.x, cell.y, game.isBlackToMove))
        {
            Timber.w("problem processing move to (%d, %d)", cell.x, cell.y)
        }
    }

    private fun genMove() {
        engineGoGame.aiIsThinking = true
        val move = engine.genMove(game.isBlackToMove)
        if (move.pass) {
            game.pass()
            Toast.makeText(this, R.string.pass, Toast.LENGTH_SHORT).show()
            bus.post(Message(getString(R.string.pass)))
        } else {
            val boardCell = game.calcBoard.getCell(move.x, move.y)
            game.do_move(boardCell)
        }
        engineGoGame.aiIsThinking = false
    }

    fun syncFromScratch() {
        Timber.w("sync start")
        engine.clearBoard()
        val replay_moves = ArrayList<GoMove>()
        replay_moves.add(game.actMove)
        var tmp_move: GoMove
        while (true) {
            tmp_move = replay_moves.last()
            if (tmp_move.isFirstMove || tmp_move.parent == null) break
            replay_moves.add(tmp_move.parent)
        }
        for (step in replay_moves.indices.reversed()) {
            tmp_move = replay_moves[step]

            // どうもisFirstMoveがtrueの時は何も無いらしい。
            if (tmp_move.isFirstMove)
                continue

            if (tmp_move.isPassMove) {
                Timber.w("sync: pass")
                engine.doPass()
            } else {
                Timber.w("sync: doMove (%d, %d, %d, %b)", tmp_move.cell!!.x, tmp_move.cell!!.y, tmp_move.player, tmp_move.player == GoDefinitions.PLAYER_BLACK)
                engine.doMove(tmp_move.cell!!.x, tmp_move.cell!!.y, tmp_move.player == GoDefinitions.PLAYER_BLACK)
            }
        }
        syncing = false
    }

    override fun requestUndo() {
        try {
            if (game.canUndo()) {
                syncing = true
                game.undo(GoPrefs.isKeepVariantWanted)
            }

            if (game.canUndo()) {
                syncing = true
                game.undo(GoPrefs.isKeepVariantWanted)
            }

            if(syncing) {
                syncFromScratch()
            }
        } catch (e: Exception) {
            Timber.w(e, "RemoteException when undoing")
        }
    }

    override fun doPass(): Boolean {
        game.pass()
        engine.doPass()

        bus.post(GameChangedEvent)
        return true
    }

    override fun doAutoSave(): Boolean {
        return true
    }

    override fun initializeStoneMove() {
        // we do not want this behaviour so we override and do nothing
    }

    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        super.onPrepareOptionsMenu(menu)
        menu.findItem(R.id.menu_game_pass).isVisible = !game.isFinished
        return true
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        this.menuInflater.inflate(R.menu.ingame_vs, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_back_to_title -> {
                gameProvider.set(GoGame(game.size))
                Intent(this, GameStartActivity::class.java).apply{ flags = Intent.FLAG_ACTIVITY_CLEAR_TOP  }.let { startActivity(it) }
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onGameChanged(gameChangedEvent: GameChangedEvent) {
        super.onGameChanged(gameChangedEvent)

        if (game.isFinished) {
            switchToCounting()
        }

    }

    override val gameExtraFragment: Fragment
        get() = ConsoleGameExtrasFragment().apply {
            askNewInfo = { engine.debugInfo()!! }
            bus.register(this)
        }
}

data class Message(val msg: String)

class ConsoleGameExtrasFragment : Fragment() {
    var askNewInfo : Function0<String> = {""}

    val textView by lazy {
        TextView(this.activity).apply {
            text = "Deb: "
            setOnClickListener { text = askNewInfo() }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?)
            = textView

    @Subscribe
    fun showMessage(msg: Message) {
        textView.text = msg.msg
    }

}
