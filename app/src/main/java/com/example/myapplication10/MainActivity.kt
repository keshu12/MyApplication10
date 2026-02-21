package com.example.myapplication10

import android.annotation.SuppressLint
import android.app.Application
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
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import kotlinx.coroutines.delay
import com.example.myapplication10.ui.theme.MyApplication10Theme
import kotlin.math.roundToInt



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
    val scrollState = rememberScrollState()

    // 渐变背景
    val gradientColors = listOf(
        MaterialTheme.colorScheme.primary.copy(alpha = 0.05f),
        MaterialTheme.colorScheme.background,
        MaterialTheme.colorScheme.background
    )

    Scaffold(
        topBar = {
            LargeTopAppBar(
                title = { 
                    Column {
                        Text("网盘分身助手", fontWeight = FontWeight.ExtraBold, style = MaterialTheme.typography.headlineSmall)
                        Text("多账号独立并行 · 进程隔离", style = MaterialTheme.typography.labelSmall, maxLines = 1)
                    }
                },
                actions = {
                    IconButton(onClick = { /* 清理缓存逻辑 */ }) {
                        Icon(Icons.Rounded.CleaningServices, contentDescription = null)
                    }
                },
                colors = TopAppBarDefaults.largeTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.verticalGradient(gradientColors))
                .padding(it)
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                item {
                    // 欢迎卡片
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                        ),
                        shape = RoundedCornerShape(24.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            Modifier.padding(24.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Rounded.VerifiedUser, 
                                    contentDescription = null, 
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                            Spacer(Modifier.width(20.dp))
                            Column(Modifier.weight(1f)) {
                                Text("安全多开模式", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                                Text("每个容器拥有独立的存储空间", style = MaterialTheme.typography.bodySmall, maxLines = 2)
                            }
                            Box(
                                modifier = Modifier
                                    .size(16.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.secondary)
                            )
                        }
                    }
                }

                // 功能说明卡片
                item {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        ),
                        shape = RoundedCornerShape(20.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(Modifier.padding(20.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Rounded.Info, 
                                    contentDescription = null, 
                                    tint = MaterialTheme.colorScheme.tertiary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(Modifier.width(12.dp))
                                Text("功能特点", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                            }
                            Spacer(Modifier.height(12.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.primary)
                                )
                                Spacer(Modifier.width(12.dp))
                                Text("独立WebView环境，账号完全隔离", style = MaterialTheme.typography.bodySmall)
                            }
                            Spacer(Modifier.height(8.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.primary)
                                )
                                Spacer(Modifier.width(12.dp))
                                Text("支持多账号同时在线，互不影响", style = MaterialTheme.typography.bodySmall)
                            }
                            Spacer(Modifier.height(8.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.primary)
                                )
                                Spacer(Modifier.width(12.dp))
                                Text("快速切换，一键启动目标账号", style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }

                // 账号槽位列表
                itemsIndexed(slots) { index, slot ->
                    val color = SlotColors[index % SlotColors.size]
                    var isHovered by remember { mutableStateOf(false) }
                    
                    ElevatedCard(
                        onClick = { MainActivity.openAccount(context, slot) },
                        shape = RoundedCornerShape(20.dp),
                        elevation = CardDefaults.elevatedCardElevation(
                            defaultElevation = 4.dp,
                            pressedElevation = 2.dp
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .padding(20.dp)
                                .fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(64.dp)
                                    .clip(CircleShape)
                                    .background(Brush.linearGradient(listOf(color.copy(0.2f), color.copy(0.05f)))),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Rounded.Cloud, 
                                    contentDescription = null, 
                                    tint = color, 
                                    modifier = Modifier
                                            .size(32.dp)
                                )
                            }
                            
                            Column(Modifier.weight(1f).padding(horizontal = 20.dp)) {
                                Text(
                                    "百度网盘 - 槽位 $slot", 
                                    style = MaterialTheme.typography.titleMedium, 
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1
                                )
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(6.dp)
                                            .clip(CircleShape)
                                            .background(color)
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    Text(
                                        "环境状态: 独立就绪", 
                                        color = color, 
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                }
                            }

                            FilledIconButton(
                                onClick = { MainActivity.openAccount(context, slot) },
                                colors = IconButtonDefaults.filledIconButtonColors(containerColor = color),
                                modifier = Modifier
                            ) {
                                Icon(
                                    Icons.Rounded.RocketLaunch, 
                                    contentDescription = null, 
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                    }
                }

                // 底部提示
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                            .background(
                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                RoundedCornerShape(16.dp)
                            )
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Rounded.Info, 
                            contentDescription = null, 
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "提示: 每个槽位对应独立的登录状态", 
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
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
    var loadProgress by remember { mutableStateOf(0) }
    var webViewRef by remember { mutableStateOf<WebView?>(null) }
    var filePathCallback by remember { mutableStateOf<ValueCallback<Array<Uri>>?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    
    val fileLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenMultipleDocuments()) { uris ->
        filePathCallback?.onReceiveValue(uris.toTypedArray())
        filePathCallback = null
    }

    BackHandler(enabled = webViewRef?.canGoBack() == true) { webViewRef?.goBack() }

    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    title = {
                        Text(
                            webViewTitle, 
                            maxLines = 1, 
                            overflow = TextOverflow.Ellipsis,
                            style = MaterialTheme.typography.titleMedium
                        )
                    },
                    navigationIcon = {
                        IconButton(
                            onClick = onClose,
                            colors = IconButtonDefaults.iconButtonColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                            )
                        ) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack, 
                                contentDescription = "返回",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    },
                    actions = {
                        IconButton(
                            onClick = { 
                                webViewRef?.reload()
                                isLoading = true
                            },
                            colors = IconButtonDefaults.iconButtonColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                            )
                        ) {
                            Icon(
                                Icons.Rounded.Refresh, 
                                contentDescription = "刷新",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        if (webViewRef?.canGoForward() == true) {
                            IconButton(
                                onClick = { webViewRef?.goForward() },
                                colors = IconButtonDefaults.iconButtonColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                )
                            ) {
                                Icon(
                                    Icons.AutoMirrored.Rounded.ArrowForward,
                                    contentDescription = "前进",
                                    tint = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                        scrolledContainerColor = MaterialTheme.colorScheme.surface
                    ),
                    scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
                )
                if (loadProgress in 1..99) {
                    LinearProgressIndicator(
                        progress = { loadProgress / 100f },
                        modifier = Modifier.fillMaxWidth(),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.primaryContainer
                    )
                }
            }
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = { ctx ->
                    WebView(ctx).apply {
                        layoutParams = ViewGroup.LayoutParams(-1, -1)
                        settings.apply {
                            javaScriptEnabled = true
                            domStorageEnabled = true
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
                        override fun onReceivedTitle(view: WebView?, title: String?) {
                            webViewTitle = title ?: ""
                        }
                        override fun onProgressChanged(view: WebView?, newProgress: Int) {
                            loadProgress = newProgress
                            isLoading = newProgress < 100
                        }
                        override fun onShowFileChooser(w: WebView?, f: ValueCallback<Array<Uri>>?, p: FileChooserParams?): Boolean {
                            filePathCallback = f
                            fileLauncher.launch(arrayOf("*/*"))
                            return true
                        }
                    }
                    webView.webViewClient = object : WebViewClient() {
                        override fun onPageFinished(view: WebView?, url: String?) {
                            isLoading = false
                        }
                    }
                    if (webView.url == null) {
                        webView.loadUrl(BAIDU_PAN_HOME)
                    }
                }
            )
            
            // 加载状态指示器
            if (isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.8f))
                        .padding(bottom = 100.dp),
                    contentAlignment = Alignment.BottomCenter
                ) {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        ),
                        shape = RoundedCornerShape(24.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                        modifier = Modifier.padding(20.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = MaterialTheme.colorScheme.primary,
                                strokeWidth = 2.dp
                            )
                            Spacer(Modifier.width(12.dp))
                            Text(
                                "正在加载...",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

class MyApplication10App : Application() {
    override fun onCreate() {
        super.onCreate()
    }
}
