package org.ligi.gobandroid_hd

import androidx.annotation.StringRes
import org.ligi.gobandroid_hd.logic.Cell

class InteractionScope {

    enum class Mode {
        RECODRD,
        TSUMEGO,
        REVIEW,
        RECORD,
        EDIT,
        COUNT,
        SETUP;

        @StringRes
        fun getStringRes(): Int {
            return when (this) {
                TSUMEGO -> R.string.tsumego
                REVIEW -> R.string.review
                RECORD -> R.string.play
                COUNT -> R.string.count
                EDIT -> R.string.edit
                SETUP -> R.string.setup
                else -> R.string.empty_str
            }
        }
    }


    var touchCell: Cell? = null
    var mode = Mode.SETUP
    var is_in_noif_mode = false

    var ask_variant_session = true

    fun hasTouchCell(): Boolean {
        return touchCell != null
    }
}
