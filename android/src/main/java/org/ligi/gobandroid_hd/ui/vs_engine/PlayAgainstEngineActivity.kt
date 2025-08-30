package org.ligi.gobandroid_hd.ui.vs_engine

import android.os.SystemClock
import android.view.Menu
import android.view.MotionEvent
import android.widget.Toast
import androidx.fragment.app.Fragment
import io.github.karino2.goengine.gnugo2.GnuGo2Native
import org.ligi.gobandroid_hd.R
import org.ligi.gobandroid_hd.events.GameChangedEvent
import org.ligi.gobandroid_hd.logic.Cell
import org.ligi.gobandroid_hd.logic.GoDefinitions
import org.ligi.gobandroid_hd.logic.GoGame
import org.ligi.gobandroid_hd.logic.GoMove
import org.ligi.gobandroid_hd.ui.GoActivity
import org.ligi.gobandroid_hd.ui.GoPrefs
import org.ligi.gobandroid_hd.ui.recording.RecordingGameExtrasFragment
import timber.log.Timber


/*
    Similar to PlayAgainstGnuGoActivity, but use local engine instead.
 */
class PlayAgainstEngineActivity : GoActivity() {
    private val engineGoGame by lazy { EngineGoGame(false, true, game, getString(R.string.gnugo2)) }
    private val engine by lazy {  GnuGo2Native().apply { initNative() } }

    private var running = false

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

    override fun onResume() {
        super.onResume()

        running = true
        setupEngine()

        Thread( {
            while(running) {
                SystemClock.sleep(100)
                if (engineGoGame.engineNowBlack() || engineGoGame.engineNowWhite() ) {
                    genMove()
                }
            }
        }).start()
    }

    private fun setupEngine() {
        engine.setDepth(0)
        engine.setKomi(game.komi)
        engine.setBoardSize(game.boardSize)
        engine.clearBoard()
    }

    public override fun doMoveWithUIFeedback(cell: Cell?): GoGame.MoveStatus {
        if (cell != null) {
            if (engineGoGame.aiIsThinking) {
                Toast.makeText(this, R.string.ai_is_thinking, Toast.LENGTH_LONG).show()
                return GoGame.MoveStatus.VALID
            }
            manualMove(cell)
        }

        return super.doMoveWithUIFeedback(cell)
    }

    private fun manualMove(cell: Cell) {
        if(!engine.doMove(cell.x, cell.y))
        {
            Timber.w("problem processing move to (%d, %d)", cell.x, cell.y)
        }
    }

    private fun genMove() {
        engineGoGame.aiIsThinking = true
        val move = engine.genMove()
        if (move.pass) {
            game.pass()
        } else {
            val boardCell = game.calcBoard.getCell(move.x, move.y)
            game.do_move(boardCell)
        }
        engineGoGame.aiIsThinking = false
    }

    fun syncFromScratch() {
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
            if (tmp_move.isPassMove) {
                engine.doPass()
            } else {
                engine.doMove(tmp_move.cell!!.x, tmp_move.cell!!.x)
            }
        }

    }

    override fun requestUndo() {
        try {
            var needSync = false
            if (game.canUndo()) {
                game.undo(GoPrefs.isKeepVariantWanted)
                needSync = true
            }

            if (game.canUndo()) {
                game.undo(GoPrefs.isKeepVariantWanted)
            }

            if(needSync) {
                syncFromScratch()
            }
        } catch (e: Exception) {
            Timber.w(e, "RemoteException when undoing")
        }
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
        this.menuInflater.inflate(R.menu.ingame_record, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onGameChanged(gameChangedEvent: GameChangedEvent) {
        super.onGameChanged(gameChangedEvent)

        if (game.isFinished) {
            switchToCounting()
        }

    }

    override val gameExtraFragment: Fragment
        get() = RecordingGameExtrasFragment()


}