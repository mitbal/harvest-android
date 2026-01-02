package com.panendividen

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import com.panendividen.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        configureWebView(binding.webView)
        binding.webView.loadUrl(PANEN_DIVIDEN_URL)

        onBackPressedDispatcher.addCallback(
            this,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    if (binding.webView.canGoBack()) {
                        binding.webView.goBack()
                    } else {
                        isEnabled = false
                        onBackPressedDispatcher.onBackPressed()
                    }
                }
            }
        )
    }

    private fun configureWebView(webView: WebView) {
        webView.settings.javaScriptEnabled = true
        webView.settings.domStorageEnabled = true
        webView.settings.loadsImagesAutomatically = true
        webView.settings.useWideViewPort = true
        webView.settings.loadWithOverviewMode = true

        webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(
                view: WebView,
                request: WebResourceRequest
            ): Boolean {
                val url = request.url.toString()
                if (url.startsWith("http://") || url.startsWith("https://")) {
                    return false
                }

                return try {
                    val intent = if (url.startsWith("intent:")) {
                        Intent.parseUri(url, Intent.URI_INTENT_SCHEME).also { parsed ->
                            parsed.`package`?.let { packageName ->
                                if (packageManager.resolveActivity(parsed, 0) == null) {
                                    parsed.getStringExtra("browser_fallback_url")?.let { fallback ->
                                        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(fallback)))
                                        return true
                                    }
                                }
                            }
                        }
                    } else {
                        Intent(Intent.ACTION_VIEW, Uri.parse(url))
                    }

                    startActivity(intent)
                    true
                } catch (e: ActivityNotFoundException) {
                    true
                } catch (e: Exception) {
                    true
                }
            }
        }
    }

    companion object {
        private const val PANEN_DIVIDEN_URL = "https://panendividen.com/?utm_source=android_app"
    }
}
