package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import com.example.ui.theme.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.LiveTv
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainDashboard(viewModel: TiktokHostViewModel) {
    val configuration = LocalConfiguration.current
    val isWideScreen = configuration.screenWidthDp >= 600

    var selectedMobileTab by remember { mutableIntStateOf(0) }
    val isStreaming by viewModel.isStreaming.collectAsState()
    val sellerName by remember { derivedStateOf { viewModel.settings.sellerName } }
    
    // Help modal state
    var showHelpDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    title = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Column {
                                Text(
                                    text = "AI HOST PRO",
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 15.sp,
                                    color = Color.White,
                                    letterSpacing = androidx.compose.ui.unit.TextUnit.Unspecified
                                )
                                Text(
                                    text = "SELLER: ${sellerName.uppercase()} • AUTOPILOT 24/7",
                                    fontSize = 9.sp,
                                    color = TikTokPeach,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            
                            if (isStreaming) {
                                Row(
                                    modifier = Modifier
                                        .background(HighDensityWarningBg, RoundedCornerShape(12.dp))
                                        .border(1.dp, HighDensityWarningBorder, RoundedCornerShape(12.dp))
                                        .padding(horizontal = 8.dp, vertical = 2.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(6.dp)
                                            .background(HighDensityWarningText, CircleShape)
                                    )
                                    Text(
                                        text = "LIVE",
                                        color = HighDensityWarningText,
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    },
                    actions = {
                        Row(
                            modifier = Modifier.padding(end = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Global Quick Stream Toggle
                            val streamLabel = if (isStreaming) "MATIKAN LIVE" else "MULAI LIVE"
                            val streamColor = if (isStreaming) TikTokPink else TikTokTeal
                            
                            Button(
                                onClick = { viewModel.toggleStreaming() },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = streamColor,
                                    contentColor = if (isStreaming) HighDensityDeepPurple else Color.Black
                                ),
                                shape = RoundedCornerShape(16.dp),
                                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                                modifier = Modifier.height(34.dp)
                            ) {
                                Text(
                                    text = streamLabel,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 10.sp
                                )
                            }

                            IconButton(onClick = { showHelpDialog = true }) {
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = "Panduan",
                                    tint = Color.White.copy(alpha = 0.7f)
                                )
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = TikTokCardBg,
                        titleContentColor = Color.White,
                        actionIconContentColor = Color.White
                    )
                )
                HorizontalDivider(color = HighDensityBorder, thickness = 1.dp)
            }
        },
        bottomBar = {
            // Only show bottom navigation on mobile/compact displays
            if (!isWideScreen) {
                Column {
                    HorizontalDivider(color = HighDensityBorder, thickness = 1.dp)
                    NavigationBar(
                        containerColor = TikTokCardBg,
                        tonalElevation = 8.dp
                    ) {
                    NavigationBarItem(
                        selected = selectedMobileTab == 0,
                        onClick = { selectedMobileTab = 0 },
                        icon = { Icon(imageVector = Icons.Default.LiveTv, contentDescription = "Live") },
                        label = { Text("Live Stream", fontSize = 11.sp) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = TikTokTeal,
                            selectedTextColor = TikTokTeal,
                            indicatorColor = TikTokTeal.copy(alpha = 0.15f),
                            unselectedIconColor = Color.White.copy(alpha = 0.5f),
                            unselectedTextColor = Color.White.copy(alpha = 0.5f)
                        )
                    )
                    NavigationBarItem(
                        selected = selectedMobileTab == 1,
                        onClick = { selectedMobileTab = 1 },
                        icon = { Icon(imageVector = Icons.Default.Inventory, contentDescription = "Produk") },
                        label = { Text("Produk", fontSize = 11.sp) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = TikTokTeal,
                            selectedTextColor = TikTokTeal,
                            indicatorColor = TikTokTeal.copy(alpha = 0.15f),
                            unselectedIconColor = Color.White.copy(alpha = 0.5f),
                            unselectedTextColor = Color.White.copy(alpha = 0.5f)
                        )
                    )
                    NavigationBarItem(
                        selected = selectedMobileTab == 2,
                        onClick = { selectedMobileTab = 2 },
                        icon = { Icon(imageVector = Icons.Default.Settings, contentDescription = "Settings") },
                        label = { Text("Pengaturan", fontSize = 11.sp) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = TikTokTeal,
                            selectedTextColor = TikTokTeal,
                            indicatorColor = TikTokTeal.copy(alpha = 0.15f),
                            unselectedIconColor = Color.White.copy(alpha = 0.5f),
                            unselectedTextColor = Color.White.copy(alpha = 0.5f)
                        )
                    )
                }
            }
        }
    }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(TikTokDarkBg)
                .padding(innerPadding)
        ) {
            if (isWideScreen) {
                // Wide Screen: Split Layout dashboard
                Row(
                    modifier = Modifier.fillMaxSize()
                ) {
                    // Left Control Panel: Settings & Product Inventory (Weight 1f)
                    SettingsScreen(
                        viewModel = viewModel,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                    )

                    // Vertical border divider
                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .fillMaxHeight()
                            .background(HighDensityBorder)
                    )

                    // Right Panel: Talking Head & Chat (Weight 1.2f)
                    StreamingScreen(
                        viewModel = viewModel,
                        modifier = Modifier
                            .weight(1.2f)
                            .fillMaxHeight()
                    )
                }
            } else {
                // Mobile compact screen: Tab-directed
                when (selectedMobileTab) {
                    0 -> StreamingScreen(viewModel = viewModel)
                    // Showing settings page directly with tab navigation handles both products list & key fields
                    1 -> SettingsScreen(viewModel = viewModel)
                    2 -> SettingsScreen(viewModel = viewModel)
                }
            }
        }
    }

    // Informative Guide Dialog
    if (showHelpDialog) {
        AlertDialog(
            onDismissRequest = { showHelpDialog = false },
            title = {
                Text(
                    text = "💡 Panduan Pola Kerja Toni AI",
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    fontSize = 16.sp
                )
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "Aplikasi Live Streaming Host ini berjalan dalam 2 Mode Kerja otomatis:",
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 12.sp,
                        lineHeight = 16.sp
                    )
                    
                    Text(
                        text = "1. Mode Putaran (Idle Loop):\nAI Toni akan bergantian mempromosikan produk aktif di inventaris Anda. Toni membuat script ulasan baru sesuai aturan kepatuhan TikTok Shop agar aman dari pembatasan akun.",
                        color = TikTokTeal,
                        fontSize = 12.sp,
                        lineHeight = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )

                    Text(
                        text = "2. Penyelaan Interaktif (Chat Interrupter):\nKetik pertanyaan pembeli atau klik gelembung obrolan penonton. Toni segera memotong pemaparannya, membaca isi komentar, menjawab pertanyaan dengan cerdas, lalu kembali melanjutkan putaran produk otomatis secara mandiri.",
                        color = TikTokPink,
                        fontSize = 12.sp,
                        lineHeight = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )

                    Text(
                        text = "Penting: Tanpa mendaftarkan kunci API berbayar, aplikasi secara cerdas menggunakan sistem suara offline gratis bawaan Android dan modul jawaban lokal kami, sehingga 100% fungsional dan siap pakai langsung!",
                        color = Color.White.copy(alpha = 0.6f),
                        fontSize = 11.sp,
                        lineHeight = 15.sp
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { showHelpDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = TikTokTeal)
                ) {
                    Text("Mengerti, Lanjutkan!", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            },
            containerColor = TikTokCardBg
        )
    }
}
