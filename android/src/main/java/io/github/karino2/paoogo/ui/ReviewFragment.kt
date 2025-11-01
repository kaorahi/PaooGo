package io.github.karino2.paoogo.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.SeekBar
import android.widget.TextView
import androidx.appcompat.widget.AppCompatSeekBar
import com.google.android.material.snackbar.Snackbar
import org.greenrobot.eventbus.EventBus
import org.ligi.gobandroid_hd.App
import org.ligi.gobandroid_hd.R
import org.ligi.gobandroid_hd.databinding.ReviewButtonContainerBinding
import org.ligi.gobandroid_hd.events.GameChangedEvent
import org.ligi.gobandroid_hd.logic.Cell
import org.ligi.gobandroid_hd.ui.GoPrefs
import org.ligi.gobandroid_hd.ui.alerts.GameForwardAlert
import org.ligi.gobandroid_hd.ui.fragments.GobandroidGameAwareFragment
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.abs

class ReviewFragment : GobandroidGameAwareFragment() {
    private var _binding: ReviewButtonContainerBinding? = null
    private val binding get() = _binding!!

    override fun createView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View
    {
        _binding = ReviewButtonContainerBinding.inflate(inflater, container, false)
        return binding.root
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onStart() {
        super.onStart()
        updateButtonStates()
        updateMovenumBar(true)

        binding.btnNext.setOnClickListener {
            doNext(1)
        }

        binding.btnNext.setOnLongClickListener {
            doNext(10)
            true
        }

        binding.btnPrev.setOnClickListener {
            doPrev(1)
        }

        binding.btnPrev.setOnLongClickListener {
            doPrev(10)
            true
        }

        binding.btnFirst.setOnClickListener {
            doPrev(9999)
        }

        binding.btnLast.setOnClickListener {
            doNext(9999)
        }

        binding.btnMainline.setOnClickListener {
            game.clearAnalyzerInfo()
            game.revertToMainLine()
            postGameChangeEvent()
        }

        binding.btnAnalyze.setOnClickListener {
            doAnalyze(1000)
        }

        binding.btnAnalyze.setOnLongClickListener {
            doAnalyze(3000)
            true
        }

        binding.btnPlayBest.setOnClickListener {
            doPlayBest()
        }
    }

    private fun doPrev(n: Int) {
        game.clearAnalyzerInfo()
        repeat(n) {
            if (game.canUndo()) game.undo() else return@repeat
        }
    }

    private fun doNext(n: Int) {
        game.clearAnalyzerInfo()
        repeat(n) {
            if (!game.canRedo()) {
                return@repeat
            } else if (game.isInReviewVariation && game.possibleVariationCount > 1) {
                game.redo(1)
            } else if (GoPrefs.isShowForwardAlertWanted) {
                GameForwardAlert.showIfNeeded(requireActivity(), game)
            } else {
                game.redo(0)
            }
        }
    }

    private fun doAnalyze(msec: Int, onCompleted: () -> Unit = {}) {
        val activity = requireActivity()
        val busyIndicator = activity.findViewById<ProgressBar>(R.id.busy_indicator)
        val statusText = activity.findViewById<TextView>(R.id.statusText)
        busyIndicator.visibility = View.VISIBLE
        viewLifecycleOwner.lifecycleScope.launch {
            analyzer.sync(game)
            val info = withContext(Dispatchers.IO) {
                analyzer.analyzeSituation(msec, game.isBlackToMove, game)
            }
            busyIndicator.visibility = View.GONE
            game.setAnalyzeInfo(info)
            postGameChangeEvent()
            val blueInfo = info.firstOrNull { it.order == 0 }
            val blueVisits = blueInfo?.visits ?: 0
            // val maxVisits = info.maxOfOrNull { it.visits } ?: 0
            val rootInfo = info.getOrNull(0)?.rootInfo
            val totalVisits = rootInfo?.visits ?: 0
            val playerSign = if (rootInfo?.currentPlayer == "B") +1.0 else -1.0
            val score = (rootInfo?.scoreLead ?: 0.0) * playerSign
            val leadingColor = getString(if (score > 0.0) R.string.leading_black else R.string.leading_white)
            statusText.text = getString(R.string.analysis_summary, leadingColor, abs(score), blueVisits, totalVisits)
            onCompleted()
        }
    }

    private fun doPlayBest(canRetry: Boolean = true) {
        val blueCell = game.analyzeInfo.firstOrNull { it.order == 0 }?.cell
        if (blueCell == null) {
            if (canRetry)
                doAnalyze(1000) { doPlayBest(false) }
            return
        }
        (requireActivity() as ReviewActivity).moveWithFeedback(blueCell)
        doAnalyze(1000)
    }

    private fun postGameChangeEvent() {
        EventBus.getDefault().post(GameChangedEvent)
    }

    private val app : App
        get() = requireActivity().applicationContext as App

    private val analyzer by lazy {
        app.engineRepository.getAnalyzer().apply {
            setKomi(game.komi)
            setBoardSize(game.boardSize)
            clearBoard()
        }
    }

    override fun onGoGameChanged(gameChangedEvent: GameChangedEvent?) {
        super.onGoGameChanged(gameChangedEvent)
        updateButtonStates()
        updateMovenumBar()
        requireActivity().findViewById<TextView>(R.id.statusText).text = ""
    }

    private fun updateButtonStates() {
        setImageViewState(game.canUndo(), binding.btnFirst, binding.btnPrev)
        setImageViewState(game.canRedo(), binding.btnNext, binding.btnLast)
        binding.btnMainline.isEnabled = game.isInReviewVariation
    }

    private var touchingMovenumBar = false

    private fun updateMovenumBar(isInit: Boolean = false) {
        if (touchingMovenumBar) return
        val seek = requireActivity().findViewById<AppCompatSeekBar>(R.id.seek_movenum)
        seek.min = - game.undoableCount()
        seek.max = game.redoableCount()
        seek.progress = 0
        seek.isEnabled = seek.min < seek.max
        if (!isInit) return
        seek.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(sb: SeekBar?, progress: Int, fromUser: Boolean) {
                if (!fromUser || sb == null) return
                var delta = progress - sb.min - game.undoableCount()
                if (delta < 0) doPrev(- delta)
                if (delta > 0) doNext(delta)
            }
            override fun onStartTrackingTouch(sb: SeekBar?) {
                touchingMovenumBar = true
            }
            override fun onStopTrackingTouch(sb: SeekBar?) {
                touchingMovenumBar = false
                updateMovenumBar()
            }
        })
    }

    private fun setImageViewState(state: Boolean, vararg views: ImageView) {
        views.forEach {
            it.isEnabled = state
            it.alpha = if (state) 1f else 0.4f
        }
    }

    private fun showJunctionInfoSnack(found_junction_snack_for_last: Int) {
        if (!GoPrefs.hasAcknowledgedJunctionInfo) {
            Snackbar.make(binding.btnLast, found_junction_snack_for_last, Snackbar.LENGTH_LONG).setAction(android.R.string.ok) { GoPrefs.hasAcknowledgedJunctionInfo = true }.show()
        }
    }

}
