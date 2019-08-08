package com.devheart.geckoviewbrowser

import android.net.Uri
import android.os.Bundle
import android.text.Html
import android.text.Spanned
import android.view.KeyEvent
import android.view.View
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.core.text.HtmlCompat
import org.mozilla.geckoview.ContentBlocking
import org.mozilla.geckoview.GeckoRuntime
import org.mozilla.geckoview.GeckoSession
import org.mozilla.geckoview.GeckoView

class MainActivity : AppCompatActivity() {

    private lateinit var geckoView: GeckoView
    private val geckoSession = GeckoSession()
    private lateinit var urlEditText: EditText
    private lateinit var progressView: ProgressBar
    private lateinit var trackersCount: TextView
    private var trackersBlockedList: List<ContentBlocking.BlockEvent> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setupToolbar()
        setupUrlEditText()
        setupGeckoView()
        progressView = findViewById(R.id.page_progress)
    }

    private fun setupGeckoView() {
        geckoView = findViewById(R.id.geckoview)
        val runtime = GeckoRuntime.create(this)
        geckoSession.open(runtime)
        geckoView.setSession(geckoSession)
        geckoSession.loadUri(INITIAL_URL)
        urlEditText.setText(INITIAL_URL)
        geckoSession.progressDelegate = createProgressDelegate()
        geckoSession.settings.useTrackingProtection = true
        geckoSession.contentBlockingDelegate = createBlockingDelegate()
        setupTrackersCounter()
    }

    private fun setupToolbar() {
        setSupportActionBar(findViewById(R.id.toolbar))
        supportActionBar?.displayOptions = ActionBar.DISPLAY_SHOW_CUSTOM
    }

    private fun setupUrlEditText() {
        urlEditText = findViewById(R.id.location_view)
        urlEditText.setOnEditorActionListener(object : View.OnFocusChangeListener,
            TextView.OnEditorActionListener {

            override fun onFocusChange(view: View?, hasFocus: Boolean) = Unit

            override fun onEditorAction(
                textView: TextView,
                actionId: Int,
                event: KeyEvent?
            ): Boolean {
                onCommit(textView.text.toString())
                textView.hideKeyboard()
                return true
            }

        })
    }

    fun onCommit(text: String) {
        clearTrackersCount()

        if ((text.contains(".") || text.contains(":")) && !text.contains(" ")) {
            geckoSession.loadUri(text)
        } else {
            geckoSession.loadUri(SEARCH_URI_BASE + text)
        }
        geckoView.requestFocus()
    }

    private fun createProgressDelegate(): GeckoSession.ProgressDelegate {
        return object : GeckoSession.ProgressDelegate {
            override fun onPageStop(session: GeckoSession, success: Boolean) = Unit

            override fun onSecurityChange(session: GeckoSession, securityInfo: GeckoSession.ProgressDelegate.SecurityInformation) = Unit

            override fun onPageStart(session: GeckoSession, url: String) = Unit

            override fun onProgressChange(session: GeckoSession, progress: Int) {
                progressView.progress = progress
                if (progress in 1..99) {
                    progressView.visibility = View.VISIBLE
                } else {
                    progressView.visibility = View.GONE
                }
            }
        }
    }

    private fun createBlockingDelegate(): ContentBlocking.Delegate {
        return object : ContentBlocking.Delegate {
            override fun onContentBlocked(session: GeckoSession, event: ContentBlocking.BlockEvent) {
                trackersBlockedList = trackersBlockedList + event
                trackersCount.text = "${trackersBlockedList.size}"
            }
        }
    }

    private fun setupTrackersCounter() {
        trackersCount = findViewById(R.id.trackers_count)
        trackersCount.text = "0"
        trackersCount.setOnClickListener {
            if (trackersBlockedList.isNotEmpty()) {
                val friendlyURLs = getFriendlyTrackersUrls()
                showDialog(friendlyURLs)
            }
        }
    }

    private fun getFriendlyTrackersUrls(): List<Spanned> {
        return trackersBlockedList.map { blockEvent ->
            val host = Uri.parse(blockEvent.uri).host
            val category = blockEvent.categoryToString()
            Html.fromHtml("<b><font color='#D55C7C'>[$category]</font></b> <br/> $host", HtmlCompat.FROM_HTML_MODE_COMPACT)

        }
    }

    private fun ContentBlocking.BlockEvent.categoryToString(): String {
        val stringResource = when (categories) {
            ContentBlocking.NONE -> R.string.none
            ContentBlocking.AT_ANALYTIC -> R.string.analytic
            ContentBlocking.AT_AD -> R.string.ad
            ContentBlocking.AT_TEST -> R.string.test
            ContentBlocking.SB_MALWARE -> R.string.malware
            ContentBlocking.SB_UNWANTED -> R.string.unwanted
            ContentBlocking.AT_SOCIAL -> R.string.social
            ContentBlocking.AT_CONTENT -> R.string.content
            ContentBlocking.SB_HARMFUL -> R.string.harmful
            ContentBlocking.SB_PHISHING -> R.string.phishing
            else -> R.string.none

        }
        return getString(stringResource)
    }

    private fun clearTrackersCount() {
        trackersBlockedList = emptyList()
        trackersCount.text = "0"
    }
}
