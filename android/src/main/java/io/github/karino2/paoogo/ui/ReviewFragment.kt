package io.github.karino2.paoogo.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.google.android.material.snackbar.Snackbar
import org.greenrobot.eventbus.EventBus
import org.ligi.gobandroid_hd.App
import org.ligi.gobandroid_hd.R
import org.ligi.gobandroid_hd.databinding.ReviewButtonContainerBinding
import org.ligi.gobandroid_hd.events.GameChangedEvent
import org.ligi.gobandroid_hd.ui.GoPrefs
import org.ligi.gobandroid_hd.ui.alerts.GameForwardAlert
import org.ligi.gobandroid_hd.ui.fragments.GobandroidGameAwareFragment

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
            game.clearAnalyzerInfo()
            if (game.isInReviewVariation && game.possibleVariationCount > 1) {
                game.redo(1)
            } else if (GoPrefs.isShowForwardAlertWanted) {
                    GameForwardAlert.showIfNeeded(requireActivity(), game)
            } else {
                game.redo(0)
            }
        }

        binding.btnPrev.setOnClickListener {
            game.clearAnalyzerInfo()
            if (game.canUndo()) {
                game.undo()
            }
        }

        binding.btnFirst.setOnClickListener {
            game.clearAnalyzerInfo()
            val nextJunction = game.findPrevJunction()
            if (nextJunction!!.isFirstMove) {
                game.jump(nextJunction)
            } else {
                showJunctionInfoSnack(R.string.found_junction_snack_for_first)
                game.jump(nextJunction.nextMoveVariations[0])
            }
        }

        binding.btnFirst.setOnLongClickListener {
            game.clearAnalyzerInfo()
            game.jump(game.findFirstMove())
            true
        }

        binding.btnLast.setOnClickListener {
            game.clearAnalyzerInfo()
            val nextJunction = game.findNextJunction()
            if (nextJunction!!.hasNextMove()) {
                showJunctionInfoSnack(R.string.found_junction_snack_for_last)
                game.jump(nextJunction.nextMoveVariations[0])
            } else {
                game.jump(nextJunction)
            }
        }

        binding.btnLast.setOnLongClickListener {
            game.clearAnalyzerInfo()
            game.jump(game.findLastMove())
            true
        }

        binding.btnMainline.setOnClickListener {
            game.clearAnalyzerInfo()
            game.revertToMainLine()
            postGameChangeEvent()
        }

        binding.btnAnalyze.setOnClickListener {
            analyzer.sync(game)
            val info = analyzer.analyzeSituation(game.isBlackToMove, game)
            game.setAnalyzeInfo(info)
            postGameChangeEvent()
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
