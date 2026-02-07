package com.example.myapplication10

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.webkit.CookieManager
import android.webkit.DownloadListener
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.view.WindowCompat
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.ui.draw.shadow
import androidx.core.content.ContextCompat
import android.os.Build
import com.example.myapplication10.ui.theme.MyApplication10Theme

private const val BAIDU_PAN_HOME = "https://pan.baidu.com/"

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        setContent {
            MyApplication10Theme {
                AccountSwitcherHome()
            }
        }
    }

    companion object {
        // 打开指定账号
        fun openAccount(context: Context, slot: Int) {
            val intent = when (slot) {
                1 -> Intent(context, Account1Activity::class.java)
                2 -> Intent(context, Account2Activity::class.java)
                3 -> Intent(context, Account3Activity::class.java)
                4 -> Intent(context, Account4Activity::class.java)
                5 -> Intent(context, Account5Activity::class.java)
                else -> return
            }
            context.startActivity(intent)
        }
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun AccountSwitcherHome(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text(text = "百度网盘 · 多账号切换") },
                scrollBehavior = androidx.compose.material3.TopAppBarDefaults.pinnedScrollBehavior(
                    rememberTopAppBarState()
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "5 个账号槽位（互不串号）",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = "每个账号在独立进程里打开网页版网盘，Cookie/缓存完全隔离；你可以分别登录后随时切换回来。",
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.height(4.dp))

            for (slot in 1..5) {
                AccountSlotCard(
                    slot = slot,
                    onOpen = { MainActivity.openAccount(context, slot) }
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "提示：首次进入某个账号需要登录一次；之后会保持登录态。",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun AccountSlotCard(
    slot: Int,
    onOpen: () -> Unit,
    modifier: Modifier = Modifier
) {
    ElevatedCard(
        modifier = modifier.fillMaxWidth(),
        onClick = onOpen
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "账号 $slot",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "独立会话 WebView（不影响其他账号）",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                text = "打开",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

// 外部存储相关代码已移除，不再支持自定义存储路径

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    MyApplication10Theme {
        AccountSwitcherHome()
    }
}

// 账号Activity，每个账号运行在独立进程中
abstract class AccountSlotActivity : ComponentActivity() {
    abstract val slot: Int

    override fun onCreate(savedInstanceState: Bundle?) {
        // 在WebView实例创建之前设置数据目录后缀
        // 注意：此方法必须在WebView实例创建之前调用，且一个进程只能设置一次
        try {
            // 使用账号槽位作为数据目录后缀
            val suffix = "account_$slot"
            // 反射调用setDataDirectorySuffix方法
            val webViewClass = Class.forName("android.webkit.WebView")
            val method = webViewClass.getDeclaredMethod("setDataDirectorySuffix", String::class.java)
            method.invoke(null, suffix)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplication10Theme {
                AccountWebViewScreen(slot = slot)
            }
        }
    }
}

class Account1Activity : AccountSlotActivity() { override val slot: Int = 1 }
class Account2Activity : AccountSlotActivity() { override val slot: Int = 2 }
class Account3Activity : AccountSlotActivity() { override val slot: Int = 3 }
class Account4Activity : AccountSlotActivity() { override val slot: Int = 4 }
class Account5Activity : AccountSlotActivity() { override val slot: Int = 5 }

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("SetJavaScriptEnabled")
@Composable
fun AccountWebViewScreen(slot: Int) {
    val context = LocalContext.current
    var title by remember { mutableStateOf("pan.baidu.com") }
    var pendingFileCallback by remember { mutableStateOf<ValueCallback<Array<Uri>>?>(null) }
    var pendingMimeTypes by remember { mutableStateOf(arrayOf("*/*")) }

    val filePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenMultipleDocuments()
    ) { uris ->
        val callback = pendingFileCallback
        pendingFileCallback = null
        if (callback != null) {
            if (uris.isEmpty()) callback.onReceiveValue(null)
            else callback.onReceiveValue(uris.toTypedArray())
        }
    }

    val webView = remember {
        WebView(context).apply {
            settings.javaScriptEnabled = true
            settings.domStorageEnabled = true
            @Suppress("DEPRECATION")
            runCatching { settings.databaseEnabled = true }
            settings.useWideViewPort = true
            settings.loadWithOverviewMode = true

            CookieManager.getInstance().setAcceptCookie(true)
            CookieManager.getInstance().setAcceptThirdPartyCookies(this, true)

            webViewClient = object : WebViewClient() {
                override fun onPageFinished(view: WebView?, url: String?) {
                    super.onPageFinished(view, url)
                }
            }
            webChromeClient = object : WebChromeClient() {
                override fun onReceivedTitle(view: WebView?, newTitle: String?) {
                    if (!newTitle.isNullOrBlank()) title = newTitle
                }

                override fun onShowFileChooser(
                    webView: WebView?, 
                    filePathCallback: ValueCallback<Array<Uri>>?, 
                    fileChooserParams: WebChromeClient.FileChooserParams?
                ): Boolean {
                    if (filePathCallback == null || fileChooserParams == null) return false
                    pendingFileCallback?.onReceiveValue(null)
                    pendingFileCallback = filePathCallback
                    pendingMimeTypes = fileChooserParams.acceptTypes
                        ?.filter { it.isNotBlank() }
                        ?.toTypedArray()
                        ?: arrayOf("*/*")
                    filePicker.launch(pendingMimeTypes)
                    return true
                }
            }

            setDownloadListener(
                DownloadListener { url, _, _, _, _ ->
                    runCatching {
                        val intent = Intent(Intent.ACTION_VIEW).apply {
                            data = Uri.parse(url)
                        }
                        context.startActivity(intent)
                    }
                }
            )
        }
    }

    DisposableEffect(Unit) {
        if (webView.url.isNullOrBlank()) {
            webView.loadUrl(BAIDU_PAN_HOME)
        }
        onDispose {
            runCatching { webView.stopLoading() }
            runCatching { webView.destroy() }
        }
    }

    BackHandler(enabled = webView.canGoBack()) {
        webView.goBack()
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text(text = title) },
                navigationIcon = {
                    IconButton(onClick = { (context as? Activity)?.finish() }) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "返回")
                    }
                }
            )
        }
    ) {
        AndroidView(
            modifier = Modifier
                .fillMaxSize()
                .padding(it)
                .background(MaterialTheme.colorScheme.surface),
            factory = { webView }
        )
    }
}

/**
 * 应用程序类
 */
class MyApplication10App : Application() {
    override fun onCreate() {
        super.onCreate()
    }
}
