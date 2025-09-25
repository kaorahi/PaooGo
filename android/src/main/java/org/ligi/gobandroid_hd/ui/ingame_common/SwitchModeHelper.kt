package org.ligi.gobandroid_hd.ui.ingame_common

import android.content.Context
import android.content.Intent
import io.github.karino2.paoogo.ui.ReviewActivity
import org.ligi.gobandroid_hd.InteractionScope.Mode
import org.ligi.gobandroid_hd.InteractionScope.Mode.*
import org.ligi.gobandroid_hd.ui.application.GobandroidFragmentActivity
import org.ligi.gobandroid_hd.ui.recording.GameRecordActivity
import org.ligi.gobandroid_hd.ui.scoring.GameScoringActivity

object SwitchModeHelper {

    fun getIntentByMode(ctx: Context, mode: Mode): Intent? {
        when (mode) {
            RECORD -> return Intent(ctx, GameRecordActivity::class.java)

            COUNT -> return Intent(ctx, GameScoringActivity::class.java)
            REVIEW -> return Intent(ctx, ReviewActivity::class.java)


            else -> return null
        }
    }

    /**
     * @param activity - context
     * *
     * @param mode     - new mode
     */
    fun startGame(activity: GobandroidFragmentActivity, mode: Mode) {
        activity.interactionScope.mode = mode
        activity.startActivity(getIntentByMode(activity, mode))
    }

    fun startGameWithCorrectMode(activity: GobandroidFragmentActivity) {
        startGame(activity, activity.interactionScope.mode)
    }

}
