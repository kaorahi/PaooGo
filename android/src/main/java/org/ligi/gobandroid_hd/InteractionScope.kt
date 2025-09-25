package org.ligi.gobandroid_hd

import androidx.annotation.StringRes
import org.ligi.gobandroid_hd.logic.Cell

class InteractionScope {

    enum class Mode {
        RECORD,
        EDIT,
        COUNT,
        REVIEW;

        @StringRes
        fun getStringRes(): Int {
            return when (this) {
                RECORD -> R.string.play
                COUNT -> R.string.count
                EDIT -> R.string.edit
                REVIEW -> R.string.review
                else -> R.string.empty_str
            }
        }
    }


    var touchCell: Cell? = null
    var mode = Mode.RECORD

    var ask_variant_session = true

    fun hasTouchCell(): Boolean {
        return touchCell != null
    }
}
