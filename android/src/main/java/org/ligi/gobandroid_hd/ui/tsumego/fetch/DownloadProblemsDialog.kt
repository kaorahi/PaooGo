package org.ligi.gobandroid_hd.ui.tsumego.fetch

import android.app.Activity
import android.view.View
import org.ligi.gobandroid_hd.App
import org.ligi.gobandroid_hd.R
import org.ligi.gobandroid_hd.ui.Refreshable
import org.ligi.gobandroid_hd.ui.alerts.ProgressDialog

class DownloadProblemsDialog(context: Activity, refreshable: Refreshable?) : ProgressDialog(context) {

    init {
        setIconResource(R.drawable.ic_navigation_refresh)
        setTitle(R.string.please_stay_patient)
        binding.progressBar.isIndeterminate = true
        binding.message.setText(R.string.downloading_tsumegos_please_wait)

        Thread(Runnable {
            val initList = TsumegoDownloadHelper.getDefaultList(App.env)
            val result = TsumegoDownloadHelper.doDownload(context, initList, {
                context.runOnUiThread {
                    binding.message.text = it
                }
            })

            context.runOnUiThread {

                var msg = context.getString(R.string.no_new_tsumegos_found)

                if (result > 0) {
                    msg = context.getString(R.string.downloaded_n_tsumego, result)
                    refreshable?.refresh()
                }

                setPositiveButton(android.R.string.ok)
                binding.message.text = msg
                binding.progressBar.visibility = View.GONE
            }
        }).start()
    }

}
