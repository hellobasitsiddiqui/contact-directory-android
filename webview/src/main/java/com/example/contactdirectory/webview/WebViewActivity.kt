package com.example.contactdirectory.webview

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback

/**
 * Thin WebView wrapper: the whole UI is the Contact Directory web app loaded in a
 * WebView. A small address bar at the top lets you point it at the server
 * (handy because the LAN IP changes); the URL is remembered between launches.
 *
 * JavaScript and DOM storage are enabled because the web app is a single-page app
 * that keeps its tokens in localStorage.
 */
class WebViewActivity : ComponentActivity() {

    private lateinit var webView: WebView

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val prefs = getSharedPreferences("webview", MODE_PRIVATE)

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            fitsSystemWindows = true
        }

        val bar = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
        val urlInput = EditText(this).apply {
            hint = "http://192.168.x.x:8080"
            setText(prefs.getString("url", "http://10.0.2.2:8080"))
            setSingleLine(true)
            imeOptions = EditorInfo.IME_ACTION_GO
            layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
        }
        val goButton = Button(this).apply { text = "Go" }
        bar.addView(urlInput)
        bar.addView(goButton)

        webView = WebView(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f
            )
            settings.javaScriptEnabled = true
            settings.domStorageEnabled = true
            webViewClient = WebViewClient() // keep navigation inside the WebView
        }

        root.addView(bar)
        root.addView(webView)
        setContentView(root)

        fun load() {
            var url = urlInput.text.toString().trim()
            if (url.isEmpty()) return
            if (!url.startsWith("http://") && !url.startsWith("https://")) url = "http://$url"
            prefs.edit().putString("url", url).apply()
            webView.loadUrl(url)
        }

        goButton.setOnClickListener { load() }
        urlInput.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_GO) { load(); true } else false
        }

        // Auto-load the last server on relaunch.
        prefs.getString("url", null)?.let { webView.loadUrl(it) }

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (webView.canGoBack()) {
                    webView.goBack()
                } else {
                    isEnabled = false
                    onBackPressedDispatcher.onBackPressed()
                }
            }
        })
    }
}
