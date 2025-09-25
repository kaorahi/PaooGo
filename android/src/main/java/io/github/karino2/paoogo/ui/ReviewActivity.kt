package io.github.karino2.paoogo.ui

import android.os.Bundle
import android.view.WindowManager
import androidx.fragment.app.Fragment
import org.ligi.gobandroid_hd.ui.GoActivity

class ReviewActivity: GoActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN)
    }

    override val gameExtraFragment: Fragment
        get() = ReviewFragment()

}