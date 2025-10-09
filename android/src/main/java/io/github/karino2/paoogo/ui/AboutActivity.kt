package io.github.karino2.paoogo.ui

import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import org.ligi.gobandroid_hd.R
import org.ligi.gobandroid_hd.ui.links.LinkWithDescription
import org.ligi.gobandroid_hd.ui.links.TwoLineRecyclerAdapter


class AboutActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about)

        if (supportActionBar != null) {
            supportActionBar!!.setDisplayHomeAsUpEnabled(true)
            supportActionBar!!.setDisplayShowTitleEnabled(true)
        }

        findViewById<RecyclerView>(R.id.content_recycler).apply {
            adapter = TwoLineRecyclerAdapter(getData())
            layoutManager = LinearLayoutManager(this@AboutActivity)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId) {
            android.R.id.home -> {
                finish()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    fun getData() = arrayOf(
        LinkWithDescription("http://ligi.de", "gobandroid", "Ligi"),

        LinkWithDescription("http://jakewharton.github.io/butterknife/", "Jake Wharton", "ButterKnife"),
        LinkWithDescription("https://developers.google.com/", "Google", "Android, Dagger, .."),
        LinkWithDescription("http://square.github.io/", "Square", "okhttp, assertj-android"),
        LinkWithDescription("https://kotlinlang.org/", "JetBrains Kotlin", "one awesome langueage used"),
        LinkWithDescription("https://github.com/greenrobot", "GreenRobot", "eventbus"),
        LinkWithDescription("https://github.com/N3TWORK/alphanum", "N3TWORK", "Alphanum comparator"),
        LinkWithDescription("http://jchardet.sourceforge.net/index.html", "jchardet", "jchardet is a java port of the source from mozilla's automatic charset detection algorithm"),
        LinkWithDescription("http://github.com/Zenigata", "French Translation #2", "Zenigata"),
        LinkWithDescription("http://github.com/p3l", "Swedish Translation", "Peter Lundqvist"),
        LinkWithDescription("https://github.com/gthazmatt", "Code contributions", "gthazmatt")
    )
}