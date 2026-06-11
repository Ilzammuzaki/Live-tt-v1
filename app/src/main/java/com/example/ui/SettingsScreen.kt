package com.example.ui

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.Product
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: TiktokHostViewModel,
    modifier: Modifier = Modifier
) {
    val productsList by viewModel.products.collectAsState()
    val settings = viewModel.settings

    // Local inputs for keys
    var geminiKeyInput by remember { mutableStateOf(settings.geminiKey) }
    var openaiKeyInput by remember { mutableStateOf(settings.openaiKey) }
    var elevenLabsKeyInput by remember { mutableStateOf(settings.elevenLabsKey) }
    var elevenLabsVoiceIdInput by remember { mutableStateOf(settings.elevenLabsVoiceId) }
    var gcloudKeyInput by remember { mutableStateOf(settings.gcloudTtsKey) }
    var didKeyInput by remember { mutableStateOf(settings.didKey) }
    var heygenKeyInput by remember { mutableStateOf(settings.heygenKey) }

    // Selected engines
    var selectedBrain by remember { mutableStateOf(settings.brainEngine) }
    var selectedVoice by remember { mutableStateOf(settings.voiceEngine) }
    var selectedAvatar by remember { mutableStateOf(settings.avatarEngine) }

    // Compliance settings
    var compliancePromptInput by remember { mutableStateOf(settings.compliancePrompt) }
    var sellerNameInput by remember { mutableStateOf(settings.sellerName) }

    // Product Add form state
    var newProdName by remember { mutableStateOf("") }
    var newProdPrice by remember { mutableStateOf("") }
    var newProdDesc by remember { mutableStateOf("") }
    var newProdBpom by remember { mutableStateOf("Sudah terdaftar BPOM & Halal") }
    var selectedPreset by remember { mutableStateOf("preset_telon") }

    var expandedPresetMenu by remember { mutableStateOf(false) }

    val presetOptions = listOf(
        Pair("preset_telon", "Minyak Bayi/Anak"),
        Pair("preset_kunyit", "Kapsul/Tablet Kunyit"),
        Pair("preset_madu", "Madu Hutan Herbal"),
        Pair("preset_collagen", "Minuman Collagen/Berry"),
        Pair("preset_teh", "Teh Hijau/Diet"),
        Pair("preset_general", "Produk Lainnya")
    )

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(TikTokDarkBg),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Toko Name section
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = TikTokCardBg),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, HighDensityBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "🏷️ Profil Toko Seller",
                        color = TikTokTeal,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    OutlinedTextField(
                        value = sellerNameInput,
                        onValueChange = {
                            sellerNameInput = it
                            viewModel.updateSellerName(it)
                        },
                        label = { Text("Nama Toko TikTok Shop Anda", color = Color.White.copy(alpha = 0.6f)) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = TikTokTeal,
                            unfocusedBorderColor = HighDensityBorder
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        // Section 1: BYOK Key Configuration
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = TikTokCardBg),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, HighDensityBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Key, contentDescription = "Keys", tint = TikTokTeal)
                        Text(
                            text = "Kunci API Anda (BYOK)",
                            color = TikTokTeal,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 16.sp
                        )
                    }

                    Text(
                        text = "Gunakan kunci API milik Anda. Data disimpan lokal di browser sehingga aman & gratis biaya server pihak ketiga.",
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 12.sp,
                        modifier = Modifier.padding(vertical = 8.dp),
                        lineHeight = 16.sp
                    )

                    HorizontalDivider(color = Color.White.copy(alpha = 0.1f), modifier = Modifier.padding(bottom = 12.0.dp))

                    // Brain choice
                    Text("Pilih Brain Engine (LLM):", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(top = 4.dp, bottom = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        listOf("Gemini", "OpenAI").forEach { choice ->
                            FilterChip(
                                selected = (selectedBrain == choice),
                                onClick = {
                                    selectedBrain = choice
                                    viewModel.saveConfigChoices(choice, selectedVoice, selectedAvatar)
                                },
                                label = { Text(choice, fontWeight = FontWeight.Bold, color = if (selectedBrain == choice) Color.Black else Color.White) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = TikTokTeal,
                                    containerColor = TikTokCardBg.copy(alpha = 0.7f),
                                )
                            )
                        }
                    }

                    if (selectedBrain == "Gemini") {
                        OutlinedTextField(
                            value = geminiKeyInput,
                            onValueChange = {
                                geminiKeyInput = it
                                viewModel.saveApiKeys(it, openaiKeyInput, elevenLabsKeyInput, gcloudKeyInput, didKeyInput, heygenKeyInput)
                            },
                            label = { Text("Google Gemini API Key (Gemini-3.5-flash)", color = Color.White.copy(alpha = 0.5f)) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = TikTokTeal,
                                unfocusedBorderColor = Color.White.copy(alpha = 0.15f)
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                    } else {
                        OutlinedTextField(
                            value = openaiKeyInput,
                            onValueChange = {
                                openaiKeyInput = it
                                viewModel.saveApiKeys(geminiKeyInput, it, elevenLabsKeyInput, gcloudKeyInput, didKeyInput, heygenKeyInput)
                            },
                            label = { Text("OpenAI API Key (GPT-4o-mini)", color = Color.White.copy(alpha = 0.5f)) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = TikTokTeal,
                                unfocusedBorderColor = Color.White.copy(alpha = 0.15f)
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Voice TTS choice
                    Text("Pilih Voice Engine (TTS):", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(top = 4.dp, bottom = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("Native TTS", "ElevenLabs", "Google Cloud TTS").forEach { choice ->
                            FilterChip(
                                selected = (selectedVoice == choice),
                                onClick = {
                                    selectedVoice = choice
                                    viewModel.saveConfigChoices(selectedBrain, choice, selectedAvatar)
                                },
                                label = { Text(choice, fontWeight = FontWeight.Bold, color = if (selectedVoice == choice) Color.Black else Color.White) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = TikTokTeal,
                                    containerColor = TikTokCardBg.copy(alpha = 0.7f),
                                )
                            )
                        }
                    }

                    if (selectedVoice == "ElevenLabs") {
                        OutlinedTextField(
                            value = elevenLabsKeyInput,
                            onValueChange = {
                                elevenLabsKeyInput = it
                                viewModel.saveApiKeys(geminiKeyInput, openaiKeyInput, it, gcloudKeyInput, didKeyInput, heygenKeyInput)
                            },
                            label = { Text("ElevenLabs API Key", color = Color.White.copy(alpha = 0.5f)) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = TikTokTeal,
                                unfocusedBorderColor = Color.White.copy(alpha = 0.15f)
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = elevenLabsVoiceIdInput,
                            onValueChange = {
                                elevenLabsVoiceIdInput = it
                                settings.elevenLabsVoiceId = it
                            },
                            label = { Text("ElevenLabs Voice ID (ID pembawa acara)", color = Color.White.copy(alpha = 0.5f)) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = TikTokTeal,
                                unfocusedBorderColor = Color.White.copy(alpha = 0.15f)
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                    } else if (selectedVoice == "Google Cloud TTS") {
                        OutlinedTextField(
                            value = gcloudKeyInput,
                            onValueChange = {
                                gcloudKeyInput = it
                                viewModel.saveApiKeys(geminiKeyInput, openaiKeyInput, elevenLabsKeyInput, it, didKeyInput, heygenKeyInput)
                            },
                            label = { Text("Google Cloud Speech-to-Text API Key", color = Color.White.copy(alpha = 0.5f)) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = TikTokTeal,
                                unfocusedBorderColor = Color.White.copy(alpha = 0.15f)
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                    } else {
                        // Native offline Indonesian TTS
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(TikTokDarkBg, RoundedCornerShape(8.dp))
                                .border(0.5.dp, TikTokTeal.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                                .padding(12.dp)
                        ) {
                            Text(
                                text = "💡 Menggunakan Sistem Offline Android: Sangat dianjurkan! Tidak bayar, respons cepat, dan dukungan pelafalan Bahasa Indonesia natural secara gratis.",
                                color = TikTokTeal,
                                fontSize = 11.sp,
                                lineHeight = 15.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Avatar animation
                    Text("Avatar Talking Head Engine:", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(top = 4.dp, bottom = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("Simulation", "D-ID", "HeyGen").forEach { choice ->
                            FilterChip(
                                selected = (selectedAvatar == choice),
                                onClick = {
                                    selectedAvatar = choice
                                    viewModel.saveConfigChoices(selectedBrain, selectedVoice, choice)
                                },
                                label = { Text(choice, fontWeight = FontWeight.Bold, color = if (selectedAvatar == choice) Color.Black else Color.White) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = TikTokTeal,
                                    containerColor = TikTokCardBg.copy(alpha = 0.7f),
                                )
                            )
                        }
                    }

                    if (selectedAvatar == "D-ID") {
                        OutlinedTextField(
                            value = didKeyInput,
                            onValueChange = {
                                didKeyInput = it
                                viewModel.saveApiKeys(geminiKeyInput, openaiKeyInput, elevenLabsKeyInput, gcloudKeyInput, it, heygenKeyInput)
                            },
                            label = { Text("D-ID API Key (Basic Auth Token)", color = Color.White.copy(alpha = 0.5f)) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = TikTokTeal,
                                unfocusedBorderColor = Color.White.copy(alpha = 0.15f)
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                    } else if (selectedAvatar == "HeyGen") {
                        OutlinedTextField(
                            value = heygenKeyInput,
                            onValueChange = {
                                heygenKeyInput = it
                                viewModel.saveApiKeys(geminiKeyInput, openaiKeyInput, elevenLabsKeyInput, gcloudKeyInput, didKeyInput, it)
                            },
                            label = { Text("HeyGen Api Key", color = Color.White.copy(alpha = 0.5f)) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = TikTokTeal,
                                unfocusedBorderColor = Color.White.copy(alpha = 0.15f)
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(TikTokDarkBg, RoundedCornerShape(8.dp))
                                .border(0.5.dp, TikTokPink.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                                .padding(12.dp)
                        ) {
                            Text(
                                text = "📺 Menggunakan Simulasi Render 3D Avatar Toni: Berbicara mengikuti modul frekuensi audio dengan animasi bibir, radar, dan partikel neon hati secara real-time.",
                                color = TikTokPink,
                                fontSize = 11.sp,
                                lineHeight = 15.sp
                            )
                        }
                    }

                    // Clear Keys Action
                    Spacer(modifier = Modifier.height(12.dp))
                    TextButton(
                        onClick = {
                            viewModel.settings.clearAllSecrets()
                            geminiKeyInput = ""
                            openaiKeyInput = ""
                            elevenLabsKeyInput = ""
                            gcloudKeyInput = ""
                            didKeyInput = ""
                            heygenKeyInput = ""
                        },
                        colors = ButtonDefaults.textButtonColors(contentColor = TikTokPink)
                    ) {
                        Text("🗑️ Bersihkan Seluruh Kunci API Tersimpan")
                    }
                }
            }
        }

        // Section 2: Comparison Card/Table (Info Panel)
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = TikTokCardBg),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, HighDensityBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "💳 Panduan Kelas Kunci API: Free vs Paid",
                        color = TikTokPeach,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth().background(TikTokDarkBg).padding(8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Fitur / Lisensi", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp, modifier = Modifier.weight(1.2f))
                        Text("Free-Tier Key", color = TikTokTeal, fontWeight = FontWeight.Bold, fontSize = 11.sp, modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
                        Text("Paid-Tier Key", color = TikTokPink, fontWeight = FontWeight.Bold, fontSize = 11.sp, modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
                    }

                    HorizontalDivider(color = HighDensityBorder)

                    // Row 1
                    ComparisonRow("Kecepatan Respon", "Lambat (Rate limits)", "Instant (Sangat Cepat)", true)
                    // Row 2
                    ComparisonRow("TTS Suara", "Robot / standard", "Ekspresif & Emosional", false)
                    // Row 3
                    ComparisonRow("Avatar Lip-Sync", "Animasi gelombang", "Video Sintesis Nyata", true)
                    // Row 4
                    ComparisonRow("24/7 Autopilot", "Ada Jeda (Cooldown)", "Lancar Tanpa Henti", false)
                }
            }
        }

        // Section 3: Compliance Guardrail Customizer Box
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = TikTokCardBg),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, HighDensityBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Shield, contentDescription = "Guard", tint = TikTokTeal)
                        Text(
                            text = "🛡️ TikTok Compliance Guardrail",
                            color = TikTokTeal,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    }

                    Text(
                        text = "Kebijakan iklan TikTok Shop ketat untuk herbal dan suplemen kesehatan. AI Toni diprogram untuk menghindari overclaims demi keamanan akun Anda.",
                        color = Color.White.copy(alpha = 0.6f),
                        fontSize = 11.sp,
                        lineHeight = 15.sp,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )

                    OutlinedTextField(
                        value = compliancePromptInput,
                        onValueChange = {
                            compliancePromptInput = it
                            viewModel.saveCompliancePrompt(it)
                        },
                        label = { Text("System Instructions (Petunjuk Redaksi AI)", color = Color.White.copy(alpha = 0.5f)) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = TikTokTeal,
                            unfocusedBorderColor = HighDensityBorder
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp),
                        maxLines = 10
                    )
                }
            }
        }

        // Section 4: Product Inventory Manager (Up to 10 products with toggle check)
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "📦 Inventaris Produk Anda (${productsList.size} / 10)",
                    color = Color.White,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 15.sp,
                    modifier = Modifier.padding(bottom = 4.dp)
                )

                // Add Product Card Form
                if (productsList.size < 10) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = TikTokCardBg.copy(alpha = 0.8f)),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, TikTokTeal.copy(alpha = 0.3f))
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text("➕ Tambah Produk atau Varian Baru", color = TikTokTeal, fontWeight = FontWeight.Bold, fontSize = 13.sp)

                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedTextField(
                                    value = newProdName,
                                    onValueChange = { newProdName = it },
                                    label = { Text("Nama Produk", color = Color.White.copy(alpha = 0.5f)) },
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedTextColor = Color.White, unfocusedTextColor = Color.White,
                                        focusedBorderColor = TikTokTeal, unfocusedBorderColor = Color.White.copy(alpha = 0.1f)
                                    ),
                                    modifier = Modifier.weight(1.3f)
                                )
                                OutlinedTextField(
                                    value = newProdPrice,
                                    onValueChange = { newProdPrice = it },
                                    label = { Text("Harga (Rp)", color = Color.White.copy(alpha = 0.5f)) },
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedTextColor = Color.White, unfocusedTextColor = Color.White,
                                        focusedBorderColor = TikTokTeal, unfocusedBorderColor = Color.White.copy(alpha = 0.1f)
                                    ),
                                    modifier = Modifier.weight(1f)
                                )
                            }

                            OutlinedTextField(
                                value = newProdDesc,
                                onValueChange = { newProdDesc = it },
                                label = { Text("Deskripsi Khasiat & Bahan", color = Color.White.copy(alpha = 0.5f)) },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = Color.White, unfocusedTextColor = Color.White,
                                    focusedBorderColor = TikTokTeal, unfocusedBorderColor = Color.White.copy(alpha = 0.1f)
                                ),
                                modifier = Modifier.fillMaxWidth().height(80.dp),
                                maxLines = 3
                            )

                            OutlinedTextField(
                                value = newProdBpom,
                                onValueChange = { newProdBpom = it },
                                label = { Text("Status Izin (BPOM / Halal)", color = Color.White.copy(alpha = 0.5f)) },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = Color.White, unfocusedTextColor = Color.White,
                                    focusedBorderColor = TikTokTeal, unfocusedBorderColor = Color.White.copy(alpha = 0.1f)
                                ),
                                modifier = Modifier.fillMaxWidth()
                            )

                            // Dropdown Picker for visual presets
                            Box {
                                OutlinedButton(
                                    onClick = { expandedPresetMenu = true },
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.15f)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(presetOptions.firstOrNull { it.first == selectedPreset }?.second ?: "Pilih Kategori")
                                        Icon(imageVector = Icons.Default.ArrowDropDown, contentDescription = "Preset picker")
                                    }
                                }

                                DropdownMenu(
                                    expanded = expandedPresetMenu,
                                    onDismissRequest = { expandedPresetMenu = false },
                                    modifier = Modifier.background(TikTokCardBg)
                                ) {
                                    presetOptions.forEach { opt ->
                                        DropdownMenuItem(
                                            text = { Text(opt.second, color = Color.White) },
                                            onClick = {
                                                selectedPreset = opt.first
                                                expandedPresetMenu = false
                                            }
                                        )
                                    }
                                }
                            }

                            Button(
                                onClick = {
                                    if (newProdName.isNotBlank() && newProdPrice.isNotBlank()) {
                                        viewModel.addProduct(
                                            Product(
                                                name = newProdName,
                                                price = if (newProdPrice.lowercase().startsWith("rp")) newProdPrice else "Rp $newProdPrice",
                                                description = newProdDesc,
                                                bpomStatus = newProdBpom,
                                                imageUrl = selectedPreset,
                                                isEnabled = true
                                            )
                                        )
                                        // Reset
                                        newProdName = ""
                                        newProdPrice = ""
                                        newProdDesc = ""
                                        newProdBpom = "Sudah terdaftar BPOM & Halal"
                                        selectedPreset = "preset_telon"
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = TikTokTeal),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Simpan Produk", color = Color.Black, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(TikTokCardBg, RoundedCornerShape(8.dp))
                            .padding(12.dp)
                    ) {
                        Text(
                            text = "⚠️ Maksimal 10 produk tercapai. Hapus beberapa produk jika ingin mendaftarkan varian baru.",
                            color = TikTokPink,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }

        // Render current products inside list for CRUD
        items(productsList) { product ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(TikTokCardBg, RoundedCornerShape(12.dp))
                    .padding(12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(RoundedCornerShape(6.dp))
                    ) {
                        PresetImageRenderer(imageUrl = product.imageUrl)
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(product.name, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Text(product.price, color = TikTokTeal, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        Text(product.bpomStatus, color = Color.White.copy(alpha = 0.5f), fontSize = 10.sp)
                    }

                    // Toggle Include in Idle Loop Rotation
                    Checkbox(
                        checked = product.isEnabled,
                        onCheckedChange = { isChecked ->
                            viewModel.updateProduct(product.copy(isEnabled = isChecked))
                        },
                        colors = CheckboxDefaults.colors(checkedColor = TikTokTeal, checkmarkColor = Color.Black)
                    )

                    // Trash can delete
                    IconButton(onClick = { viewModel.deleteProduct(product) }) {
                        Icon(imageVector = Icons.Default.Delete, contentDescription = "Hapus", tint = TikTokPink.copy(alpha = 0.8f))
                    }
                }
            }
        }
    }
}

@Composable
fun ComparisonRow(feature: String, free: String, paid: String, highlighted: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp, horizontal = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(feature, color = Color.White.copy(alpha = 0.8f), fontSize = 11.sp, modifier = Modifier.weight(1.2f))
        Text(free, color = if (highlighted) TikTokTeal else Color.White, fontSize = 11.sp, modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
        Text(paid, color = if (highlighted) TikTokPink else Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
    }
    HorizontalDivider(color = Color.White.copy(alpha = 0.05f))
}
