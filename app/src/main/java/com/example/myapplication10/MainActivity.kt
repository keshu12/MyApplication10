package com.example.myapplication10

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.ViewGroup
import android.webkit.CookieManager
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
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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

// ---------------- UI 美化部分 ----------------

// 定义一组柔和的颜色用于区分不同账号
private val SlotColors = listOf(
    Color(0xFF4285F4), // Google Blue
    Color(0xFFDB4437), // Red
    Color(0xFFF4B400), // Yellow
    Color(0xFF0F9D58), // Green
    Color(0xFFAB47BC)  // Purple
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountSwitcherHome(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val slots = (1..5).toList()
    
    // 简单的入场动画状态
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.surface, // 使用纯净背景
        topBar = {
            CenterAlignedTopAppBar(
                title = { 
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Info, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.width(8.dp))
                        Text(
                            "网盘多开助手", 
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleLarge
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(top = 8.dp, bottom = 24.dp, start = 16.dp, end = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                AnimatedVisibility(
                    visible = visible,
                    enter = fadeIn() + slideInVertically(initialOffsetY = { -40 })
                ) {
                    InfoCard()
                }
            }
            
            item {
                Text(
                    text = "选择账号",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(start = 4.dp, top = 8.dp, bottom = 4.dp)
                )
            }

            itemsIndexed(slots) { index, slot ->
                AnimatedVisibility(
                    visible = visible,
                    enter = fadeIn() + slideInVertically(
                        animationSpec = spring(stiffness = Spring.StiffnessLow),
                        initialOffsetY = { 100 * (index + 1) } 
                    )
                ) {
                    AccountSlotCard(
                        slot = slot,
                        color = SlotColors[index % SlotColors.size],
                        onOpen = { MainActivity.openAccount(context, slot) }
                    )
                }
            }
            
            item {
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.outline,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "不同账号运行在完全独立的进程中，数据互不干扰。",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }
        }
    }
}

@Composable
fun InfoCard() {
    Card(
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f)
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.secondaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = "独立进程隔离",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
                Text(
                    text = "Cookie 与 缓存文件 物理隔离",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AccountSlotCard(
    slot: Int,
    color: Color,
    onOpen: () -> Unit
) {
    Card(
        onClick = onOpen,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp, pressedElevation = 0.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 左侧：带颜色的头像
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(
                            listOf(color.copy(alpha = 0.2f), color.copy(alpha = 0.1f))
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "#$slot",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = color
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // 中间：文字信息
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "网盘账号 $slot",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(color)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "独立环境就绪",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // 右侧：进入按钮
            FilledTonalIconButton(
                onClick = onOpen,
                colors = IconButtonDefaults.filledTonalIconButtonColors(
                    containerColor = color.copy(alpha = 0.1f),
                    contentColor = color
                )
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "进入")
            }
        }
    }
}

// ---------------- Activity 与 WebView 逻辑 (保留核心逻辑) ----------------

/**
 * 抽象基类：处理进程数据目录隔离
 */
abstract class AccountSlotActivity : ComponentActivity() {
    abstract val slot: Int

    override fun onCreate(savedInstanceState: Bundle?) {
        setupWebViewDataDirectory()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplication10Theme {
                // 将颜色传递给 WebView 界面，保持风格一致
                val themeColor = SlotColors[(slot - 1) % SlotColors.size]
                AccountWebViewScreen(slot = slot, themeColor = themeColor, onClose = { finish() })
            }
        }
    }

    private fun setupWebViewDataDirectory() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            try {
                val processName = "account_$slot"
                WebView.setDataDirectorySuffix(processName)
            } catch (e: Exception) {
                e.printStackTrace()
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
@Composable
fun AccountWebViewScreen(slot: Int, themeColor: Color, onClose: () -> Unit) {
    val context = LocalContext.current
    var webViewTitle by remember { mutableStateOf("加载中...") }
    var loadProgress by remember { mutableIntStateOf(0) }
    var currentUrl by remember { mutableStateOf("") }
    var webViewRef by remember { mutableStateOf<WebView?>(null) }
    var filePathCallback by remember { mutableStateOf<ValueCallback<Array<Uri>>?>(null) }
    
    val fileLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenMultipleDocuments()
    ) { uris ->
        filePathCallback?.onReceiveValue(uris.toTypedArray())
        filePathCallback = null
    }

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
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = "账号 $slot · ${if (currentUrl.contains("baidu")) "百度网盘" else "外部链接"}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            )
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
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = themeColor.copy(alpha = 0.1f), // 使用淡化的主题色作为背景
                        titleContentColor = MaterialTheme.colorScheme.onSurface,
                        actionIconContentColor = themeColor,
                        navigationIconContentColor = themeColor
                    )
                )
                if (loadProgress in 1..99) {
                    LinearProgressIndicator(
                        progress = { loadProgress / 100f },
                        modifier = Modifier.fillMaxWidth(),
                        color = themeColor,
                        trackColor = themeColor.copy(alpha = 0.2f)
                    )
                }
            }
        }
    ) { padding ->
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
                        filePathCallback?.onReceiveValue(null)
                        filePathCallback = newFilePathCallback
                        try {
                            fileLauncher.launch(arrayOf("*/*"))
                        } catch (e: Exception) {
                            filePathCallback = null
                            return false
                        }
                        return true
                    }
                }
                webView.webViewClient = object : WebViewClient() {
                    override fun onPageFinished(view: WebView?, url: String?) {
                        currentUrl = url ?: ""
                    }
                }
                webView.setDownloadListener { url, _, _, _, _ ->
                    try {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                        context.startActivity(intent)
                    } catch (e: Exception) { }
                }
                if (webView.url == null) {
                    webView.loadUrl(BAIDU_PAN_HOME)
                }
            },
            onRelease = { webView ->
                webView.stopLoading()
                webView.destroy()
            }
        )
    }
}

@SuppressLint("SetJavaScriptEnabled")
private fun WebView.configureWebView(webView: WebView) {
    webView.settings.apply {
        javaScriptEnabled = true
        domStorageEnabled = true
        databaseEnabled = true
        useWideViewPort = true
        loadWithOverviewMode = true
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mixedContentMode = WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE
        }
        setSupportZoom(true)
        builtInZoomControls = true
        displayZoomControls = false
        cacheMode = WebSettings.LOAD_DEFAULT
        userAgentString = USER_AGENT_OVERRIDE
    }
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