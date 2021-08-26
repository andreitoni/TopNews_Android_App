package com.example.assignment2

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View.SCROLLBARS_INSIDE_OVERLAY
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity

/**
 * This class allows to open news inside the app and to share that article
 */
class DetailedNews : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.news_detailed)


        val webView: WebView = findViewById(R.id.webView)
        val intent = intent

        val url = intent.getStringExtra("url")

        webView.settings.domStorageEnabled
        webView.settings.javaScriptEnabled
        webView.settings.loadsImagesAutomatically
        webView.scrollBarStyle = SCROLLBARS_INSIDE_OVERLAY
        webView.webViewClient = WebViewClient()
        webView.loadUrl(url)

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.news_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val intent = intent
        val url = intent.getStringExtra("url")

        when (item.itemId) {

            R.id.nav_share -> {
                try {
                    val sendIntent: Intent = Intent().apply {
                        action = Intent.ACTION_SEND
                        putExtra(Intent.EXTRA_SUBJECT, url)
                        type = "text/plain"
                    }

                    val shareIntent = Intent.createChooser(sendIntent, null)
                    startActivity(shareIntent)

                } catch (e: NullPointerException) {
                    Log.i("nullPointer", "nullpointer exception")
                }
            }
        }
        return super.onOptionsItemSelected(item)
    }


}