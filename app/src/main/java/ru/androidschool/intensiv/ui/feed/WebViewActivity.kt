package ru.androidschool.intensiv.ui.feed

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.fragment_web_view.*
import ru.androidschool.intensiv.R
import timber.log.Timber

class WebViewActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_web_view)

        val param1 = intent.extras?.getString(FeedFragment.KEY_URL)



        val webSettings = web_view.settings
        webSettings.javaScriptEnabled = true
        Timber.d("Link123" + param1!!)
        web_view.loadUrl(param1!!)
    }
}