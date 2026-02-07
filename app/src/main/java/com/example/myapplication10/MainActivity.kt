package com.example.myapplication10

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.ViewGroup
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
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.myapplication10.ui.theme.MyApplication10Theme

// 常量定义
private const val BAIDU_PAN_HOME = "https://pan.baidu.com/"
private val SlotColors = listOf(
    Color(0xFF4285F4), // 科技蓝
    Color(0xFF34A853), // 生机绿
    Color(0xFFFBBC05), // 活力橙
    Color(0xFFEA4335), // 热情红
    Color(0xFFAB47BC)  // 优雅紫
)

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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountSwitcherHome() {
    val context = LocalContext.current
    val slots = (1..5).toList()

    Scaffold(
        topBar = {
            LargeTopAppBar(
                title = { 
                    Column {
                        Text("网盘分身助手", fontWeight = FontWeight.ExtraBold)
                        Text("多账号独立并行 · 进程隔离", style = MaterialTheme.typography.labelSmall)
                    }
                },
                actions = {
                    IconButton(onClick = { /* 清理缓存逻辑 */ }) {
                        Icon(Icons.Rounded.CleaningServices, contentDescription = null)
                    }
                }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier.padding(innerPadding).fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                    ),
                    shape = RoundedCornerShape(24.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Rounded.VerifiedUser, 
                            contentDescription = null, 
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(Modifier.width(16.dp))
                        Column {
                            Text("安全多开模式", fontWeight = FontWeight.Bold)
                            Text("每个容器拥有独立的存储空间", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }

            itemsIndexed(slots) { index, slot ->
                val color = SlotColors[index % SlotColors.size]
                ElevatedCard(
                    onClick = { MainActivity.openAccount(context, slot) },
                    shape = RoundedCornerShape(20.dp),
                    elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp).fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(60.dp)
                                .clip(CircleShape)
                                .background(Brush.linearGradient(listOf(color.copy(0.2f), color.copy(0.05f)))),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Rounded.Cloud, contentDescription = null, tint = color, modifier = Modifier.size(30.dp))
                        }
                        
                        Column(Modifier.weight(1f).padding(horizontal = 16.dp)) {
                            Text("百度网盘 - 槽位 $slot", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Text("环境状态: 独立就绪", color = color, style = MaterialTheme.typography.labelSmall)
                        }

                        FilledIconButton(
                            onClick = { MainActivity.openAccount(context, slot) },
                            colors = IconButtonDefaults.filledIconButtonColors(containerColor = color)
                        ) {
                            Icon(Icons.Rounded.RocketLaunch, contentDescription = null, modifier = Modifier.size(20.dp))
                        }
                    }
                }
            }
        }
    }
}

// ---------------- 核心 Activity 与 WebView 隔离逻辑 ----------------

abstract class AccountSlotActivity : ComponentActivity() {
    abstract val slot: Int
    override fun onCreate(savedInstanceState: Bundle?) {
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
                WebView.setDataDirectorySuffix("account_$slot")
            } catch (e: Exception) { e.printStackTrace() }
        }
    }
}

class Account1Activity : AccountSlotActivity() { override val slot = 1 }
class Account2Activity : AccountSlotActivity() { override val slot = 2 }
class Account3Activity : AccountSlotActivity() { override val slot = 3 }
class Account4Activity : AccountSlotActivity() { override val slot = 4 }
class Account5Activity : AccountSlotActivity() { override val slot = 5 }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountWebViewScreen(slot: Int, onClose: () -> Unit) {
    val context = LocalContext.current
    var webViewTitle by remember { mutableStateOf("加载中...") }
    var loadProgress by remember { mutableIntStateOf(0) }
    var webViewRef by remember { mutableStateOf<WebView?>(null) }
    var filePathCallback by remember { mutableStateOf<ValueCallback<Array<Uri>>?>(null) }
    
    val fileLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenMultipleDocuments()) { uris ->
        filePathCallback?.onReceiveValue(uris.toTypedArray())
        filePathCallback = null
    }

    BackHandler(enabled = webViewRef?.canGoBack() == true) { webViewRef?.goBack() }

    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    title = { Text(webViewTitle, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                    navigationIcon = {
                        IconButton(onClick = onClose) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null) }
                    },
                    actions = {
                        IconButton(onClick = { webViewRef?.reload() }) { Icon(Icons.Rounded.Refresh, null) }
                    }
                )
                if (loadProgress in 1..99) {
                    LinearProgressIndicator(progress = { loadProgress / 100f }, modifier = Modifier.fillMaxWidth())
                }
            }
        }
    ) { padding ->
        AndroidView(
            modifier = Modifier.fillMaxSize().padding(padding),
            factory = { ctx ->
                WebView(ctx).apply {
                    layoutParams = ViewGroup.LayoutParams(-1, -1)
                    settings.apply {
                        javaScriptEnabled = true
                        domStorageEnabled = true
                        databaseEnabled = true
                        useWideViewPort = true
                        loadWithOverviewMode = true
                        cacheMode = WebSettings.LOAD_DEFAULT
                        userAgentString = "Mozilla/5.0 (Linux; Android 10; K) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Mobile Safari/537.36"
                    }
                }
            },
            update = { webView ->
                webViewRef = webView
                webView.webChromeClient = object : WebChromeClient() {
                    override fun onReceivedTitle(view: WebView?, title: String?) { webViewTitle = title ?: "" }
                    override fun onProgressChanged(view: WebView?, newProgress: Int) { loadProgress = newProgress }
                    override fun onShowFileChooser(w: WebView?, f: ValueCallback<Array<Uri>>?, p: FileChooserParams?): Boolean {
                        filePathCallback = f
                        fileLauncher.launch(arrayOf("*/*"))
                        return true
                    }
                }
                webView.webViewClient = object : WebViewClient() {
                    override fun onPageFinished(view: WebView?, url: String?) {
                        // 可以在此处理页面加载完成逻辑
                    }
                }
                if (webView.url == null) webView.loadUrl(BAIDU_PAN_HOME)
            }
        )
    }
}