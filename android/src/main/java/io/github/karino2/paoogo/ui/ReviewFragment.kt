package io.github.karino2.paoogo.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import com.google.android.material.snackbar.Snackbar
import org.greenrobot.eventbus.EventBus
import org.ligi.gobandroid_hd.App
import org.ligi.gobandroid_hd.R
import org.ligi.gobandroid_hd.databinding.ReviewButtonContainerBinding
import org.ligi.gobandroid_hd.events.GameChangedEvent
import org.ligi.gobandroid_hd.ui.GoPrefs
import org.ligi.gobandroid_hd.ui.alerts.GameForwardAlert
import org.ligi.gobandroid_hd.ui.fragments.GobandroidGameAwareFragment
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

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

    private fun doAnalyze(msec: Int) {
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
            val blueVisits = info.firstOrNull { it.order == 0 }?.visits ?: 0
            // val maxVisits = info.maxOfOrNull { it.visits } ?: 0
            val totalVisits = info.sumOf { it.visits }
            statusText.text = getString(R.string.visits_count, blueVisits, totalVisits)
        }
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
        requireActivity().findViewById<TextView>(R.id.statusText).text = ""
    }

    private fun updateButtonStates() {
        setImageViewState(game.canUndo(), binding.btnFirst, binding.btnPrev)
        setImageViewState(game.canRedo(), binding.btnNext, binding.btnLast)
        binding.btnMainline.isEnabled = game.isInReviewVariation
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
