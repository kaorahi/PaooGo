package io.github.karino2.paoogo.ui.vs_engine

import org.ligi.gobandroid_hd.App
import org.ligi.gobandroid_hd.logic.GoGame
import org.ligi.gobandroid_hd.ui.GoPrefs

class EngineGoGame internal constructor(
    var playingBlack: Boolean,
    var playingWhite: Boolean,
    private val game: GoGame,
    private val engineName: String
) {

    var aiIsThinking = false

    fun engineNowWhite(): Boolean {
        return !game.isBlackToMove && playingWhite
    }

    fun engineNowBlack(): Boolean {
        return game.isBlackToMove && playingBlack
    }

    fun swapPlayerColors() {
        playingBlack = playingWhite.also { playingWhite = playingBlack }
        val metaData = game.metaData
        metaData.blackName = metaData.whiteName.also { metaData.whiteName = metaData.blackName }
        metaData.blackRank = metaData.whiteRank.also { metaData.whiteRank = metaData.blackRank }
    }

    fun setMetaDataForGame(app: App) {
        val metaData = game.metaData
        if (playingBlack) {
            metaData.blackName = engineName
            metaData.blackRank = ""
        } else {
            metaData.blackName = GoPrefs.username
            metaData.blackRank = GoPrefs.rank
        }

        if (playingWhite) {
            metaData.whiteName = engineName
            metaData.whiteRank = ""
        } else {
            metaData.whiteName = GoPrefs.username
            metaData.whiteRank = GoPrefs.rank
        }

    }
}
