package com.example.ui.components

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.util.Log
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import java.net.URLDecoder

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun GoogleOAuthWebViewDialog(
    clientId: String,
    onTokenReceived: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Direct Auth endpoint flow for Google
    val authUrl = remember(clientId) {
        val redirectUri = "http://localhost"
        val scopes = listOf(
            "https://www.googleapis.com/auth/calendar",
            "https://www.googleapis.com/auth/calendar.events",
            "https://www.googleapis.com/auth/userinfo.email",
            "https://www.googleapis.com/auth/userinfo.profile",
            "openid"
        ).joinToString("+") { URLDecoder.decode(it, "UTF-8") }

        "https://accounts.google.com/o/oauth2/v2/auth" +
                "?client_id=$clientId" +
                "&redirect_uri=$redirectUri" +
                "&response_type=token" +
                "&scope=$scopes" +
                "&prompt=select_account"
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = true,
            dismissOnClickOutside = false
        )
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .clip(RoundedCornerShape(24.dp)),
            color = MaterialTheme.colorScheme.background,
            tonalElevation = 8.dp
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Header bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFFFECE6))
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Kết nối Tài khoản Google Thật 🌟",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFFF8A75)
                        )
                        Text(
                            text = "Đăng nhập trực tiếp & an toàn thông qua Google",
                            fontSize = 11.sp,
                            color = Color.Gray
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = Color.Gray)
                    }
                }

                Box(modifier = Modifier.weight(1f)) {
                    AndroidView(
                        factory = { context ->
                            WebView(context).apply {
                                settings.javaScriptEnabled = true
                                settings.domStorageEnabled = true
                                settings.userAgentString = "Mozilla/5.0 (Linux; Android 10; K) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/114.0.0.0 Mobile Safari/537.36"
                                
                                webViewClient = object : WebViewClient() {
                                    override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                                        super.onPageStarted(view, url, favicon)
                                        isLoading = true
                                        url?.let { checkRedirect(it) }
                                    }

                                    override fun onPageFinished(view: WebView?, url: String?) {
                                        super.onPageFinished(view, url)
                                        isLoading = false
                                        url?.let { checkRedirect(it) }
                                    }

                                    override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                                        request?.url?.toString()?.let { url ->
                                            if (checkRedirect(url)) return true
                                        }
                                        return super.shouldOverrideUrlLoading(view, request)
                                    }

                                    private fun checkRedirect(url: String): Boolean {
                                        Log.d("GoogleOAuthWebView", "Loading URL: $url")
                                        if (url.startsWith("http://localhost")) {
                                            // Extract token from fragment/hash parameters
                                            val fragment = url.substringAfter("#", "")
                                            if (fragment.isNotEmpty()) {
                                                val params = fragment.split("&").associate {
                                                    val parts = it.split("=")
                                                    val key = parts.getOrNull(0) ?: ""
                                                    val value = parts.getOrNull(1) ?: ""
                                                    key to URLDecoder.decode(value, "UTF-8")
                                                }
                                                val accessToken = params["access_token"]
                                                if (!accessToken.isNullOrEmpty()) {
                                                    onTokenReceived(accessToken)
                                                    return true
                                                }
                                            }
                                        }
                                        return false
                                    }
                                }
                                loadUrl(authUrl)
                            }
                        },
                        modifier = Modifier.fillMaxSize()
                    )

                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.align(Alignment.Center),
                            color = Color(0xFFFFB3A0)
                        )
                    }
                }
            }
        }
    }
}
