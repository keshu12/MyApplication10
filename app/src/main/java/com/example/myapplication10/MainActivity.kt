package com.example.myapplication10

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.ViewGroup
import android.webkit.CookieManager
import android.webkit.DownloadListener
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.myapplication10.ui.theme.MyApplication10Theme

// 常量定义
private const val BAIDU_PAN_HOME = "https://pan.baidu.com/"
private const val USER_AGENT_OVERRIDE = "Mozilla/5.0 (Linux; Android 10; K) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Mobile Safari/537.36"

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
        fun openAccount(context: Context, slot: Int) {
            val targetClass = when (slot) {
                1 -> Account1Activity::class.java
                2 -> Account2Activity::class.java
                3 -> Account3Activity::class.java
                4 -> Account4Activity::class.java
                5 -> Account5Activity::class.java
                else -> return
            }
            context.startActivity(Intent(context, targetClass))
        }
    }
}

// ---------------- UI 部分 ----------------

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountSwitcherHome(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val slots = (1..5).toList()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text("百度网盘 · 多账号分身") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                InfoCard()
            }
            
            item {
                Text(
                    text = "账号列表",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(vertical = 8.dp),
                    color = MaterialTheme.colorScheme.primary
                )
            }

            items(slots) { slot ->
                AccountSlotCard(
                    slot = slot,
                    onOpen = { MainActivity.openAccount(context, slot) }
                )
            }
            
            item {
                Spacer(modifier = Modifier.height(20.dp))
                Text(
                    text = "注意：为实现完全隔离，请确保在 AndroidManifest.xml 中为每个 AccountActivity 配置不同的 android:process 属性。",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }
        }
    }
}

@Composable
fun InfoCard() {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "✨ 独立进程与数据隔离",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "每个账号槽位拥有独立的 Cookie 和缓存目录。您可以在不同槽位登录不同账号，互不干扰。",
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
private fun AccountSlotCard(
    slot: Int,
    onOpen: () -> Unit
) {
    ElevatedCard(
        onClick = onOpen,
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.AccountCircle,
                contentDescription = null,
                modifier = Modifier.size(40.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "账号槽位 #$slot",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "独立 WebView 环境",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Button(onClick = onOpen) {
                Text("进入")
            }
        }
    }
}

// ---------------- Activity 与 WebView 逻辑 ----------------

/**
 * 抽象基类：处理进程数据目录隔离
 */
abstract class AccountSlotActivity : ComponentActivity() {
    abstract val slot: Int

    override fun onCreate(savedInstanceState: Bundle?) {
        // 核心隔离逻辑：必须在 WebView 实例化前调用
        setupWebViewDataDirectory()
        
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplication10Theme {
                AccountWebViewScreen(slot = slot, onClose = { finish() })
            }
        }
    }

    private fun setupWebViewDataDirectory() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            try {
                // 使用官方 API 设置数据目录后缀
                // 同一个进程中只能设置一次，且必须在创建 WebView 之前
                val processName = "account_$slot"
                WebView.setDataDirectorySuffix(processName)
            } catch (e: Exception) {
                // 如果已经设置过或抛出异常（例如多进程配置错误），打印日志
                e.printStackTrace()
            }
        }
    }
}

// 具体实现类，需在 Manifest 中注册（建议配置 android:process）
class Account1Activity : AccountSlotActivity() { override val slot: Int = 1 }
class Account2Activity : AccountSlotActivity() { override val slot: Int = 2 }
class Account3Activity : AccountSlotActivity() { override val slot: Int = 3 }
class Account4Activity : AccountSlotActivity() { override val slot: Int = 4 }
class Account5Activity : AccountSlotActivity() { override val slot: Int = 5 }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountWebViewScreen(slot: Int, onClose: () -> Unit) {
    val context = LocalContext.current
    // 状态管理
    var webViewTitle by remember { mutableStateOf("加载中...") }
    var loadProgress by remember { mutableIntStateOf(0) }
    var currentUrl by remember { mutableStateOf("") }
    
    // WebView 实例引用，用于控制返回
    var webViewRef by remember { mutableStateOf<WebView?>(null) }

    // 文件选择回调处理
    var filePathCallback by remember { mutableStateOf<ValueCallback<Array<Uri>>?>(null) }
    
    val fileLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenMultipleDocuments()
    ) { uris ->
        filePathCallback?.onReceiveValue(uris.toTypedArray())
        filePathCallback = null
    }

    // 处理物理返回键
    BackHandler(enabled = webViewRef?.canGoBack() == true) {
        webViewRef?.goBack()
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            Column {
                TopAppBar(
                    title = {
                        Column {
                            Text(
                                text = webViewTitle,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                style = MaterialTheme.typography.titleMedium
                            )
                            if (currentUrl.isNotEmpty()) {
                                Text(
                                    text = "账号 $slot",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = onClose) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, "退出")
                        }
                    },
                    actions = {
                        IconButton(onClick = { webViewRef?.reload() }) {
                            Icon(Icons.Default.Refresh, "刷新")
                        }
                    }
                )
                if (loadProgress in 1..99) {
                    LinearProgressIndicator(
                        progress = { loadProgress / 100f },
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
        }
    ) { padding ->
        // 使用 AndroidView 嵌入 WebView
        AndroidView(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            factory = { ctx ->
                WebView(ctx).apply {
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                    configureWebView(this)
                }
            },
            update = { webView ->
                webViewRef = webView
                
                // 设置 ChromeClient 处理 UI 交互（标题、进度、文件选择）
                webView.webChromeClient = object : WebChromeClient() {
                    override fun onReceivedTitle(view: WebView?, title: String?) {
                        title?.let { webViewTitle = it }
                    }

                    override fun onProgressChanged(view: WebView?, newProgress: Int) {
                        loadProgress = newProgress
                    }

                    override fun onShowFileChooser(
                        webView: WebView?,
                        newFilePathCallback: ValueCallback<Array<Uri>>?,
                        fileChooserParams: FileChooserParams?
                    ): Boolean {
                        // 如果有未处理的回调，先取消
                        filePathCallback?.onReceiveValue(null)
                        filePathCallback = newFilePathCallback
                        
                        try {
                            // 启动文件选择器，这里简单处理，未区分 mimeType
                            // 实际使用可以解析 fileChooserParams?.acceptTypes
                            fileLauncher.launch(arrayOf("*/*"))
                        } catch (e: Exception) {
                            filePathCallback = null
                            return false
                        }
                        return true
                    }
                }

                // 设置 WebViewClient 处理页面加载
                webView.webViewClient = object : WebViewClient() {
                    override fun onPageFinished(view: WebView?, url: String?) {
                        currentUrl = url ?: ""
                        // 注入一些 JS 来优化移动端显示（可选）
                    }
                }

                // 设置下载监听
                webView.setDownloadListener { url, _, _, _, _ ->
                    try {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                        context.startActivity(intent)
                    } catch (e: Exception) {
                        // 处理没有应用能打开此链接的情况
                    }
                }

                // 首次加载
                if (webView.url == null) {
                    webView.loadUrl(BAIDU_PAN_HOME)
                }
            },
            onRelease = { webView ->
                // 组件销毁时清理 WebView
                webView.stopLoading()
                webView.destroy()
            }
        )
    }
}

/**
 * 统一配置 WebView 设置
 */
@SuppressLint("SetJavaScriptEnabled")
private fun WebView.configureWebView(webView: WebView) {
    webView.settings.apply {
        javaScriptEnabled = true
        domStorageEnabled = true // 关键：许多现代网页登录需要 DOM Storage
        databaseEnabled = true
        useWideViewPort = true
        loadWithOverviewMode = true
        
        // 允许混合内容（HTTPS 页面加载 HTTP 资源），避免部分图片不显示
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mixedContentMode = WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE
        }
        
        // 启用缩放
        setSupportZoom(true)
        builtInZoomControls = true
        displayZoomControls = false

        // 缓存设置
        cacheMode = WebSettings.LOAD_DEFAULT
        
        // 模拟 UserAgent，有时可以避免被识别为爬虫或旧版浏览器
        userAgentString = USER_AGENT_OVERRIDE
    }

    // Cookie 设置
    CookieManager.getInstance().apply {
        setAcceptCookie(true)
        setAcceptThirdPartyCookies(webView, true)
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    MyApplication10Theme {
        AccountSwitcherHome()
    }
}