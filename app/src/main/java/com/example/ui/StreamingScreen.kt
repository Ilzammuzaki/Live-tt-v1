package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.data.Product
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

import com.example.ui.theme.*

// Color tokens mapped to High Density Theme
val TikTokDarkBg = HighDensityBg
val TikTokCardBg = HighDensityCardBg
val TikTokTeal = HighDensityPrimary
val TikTokPink = HighDensityWarningText
val TikTokPeach = HighDensitySecondary

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun StreamingScreen(
    viewModel: TiktokHostViewModel,
    modifier: Modifier = Modifier
) {
    val isStreaming by viewModel.isStreaming.collectAsState()
    val streamMode by viewModel.streamMode.collectAsState()
    val activeProduct by viewModel.activeProduct.collectAsState()
    val subtitle by viewModel.subtitle.collectAsState()
    val isSpeaking by viewModel.isSpeaking.collectAsState()
    val chatComments by viewModel.chatComments.collectAsState()
    val inputText by viewModel.inputText.collectAsState()

    val scope = rememberCoroutineScope()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(TikTokDarkBg)
    ) {
        // Upper part: Stream Area (Talking Head)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1.3f)
                .background(Color.Black)
        ) {
            // Simulated Camera Stream / Background
            val gradientBrush = remember {
                Brush.verticalGradient(
                    colors = listOf(Color(0xFF0F2027), Color(0xFF203A43), Color(0xFF2C5364))
                )
            }
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(gradientBrush)
            )

            // Dynamic grid lines background to resemble a sci-fi cyber studio
            Canvas(modifier = Modifier.fillMaxSize()) {
                val gridSpacing = 40.dp.toPx()
                val lineAlpha = 0.08f
                // Vertical lines
                var x = 0f
                while (x < size.width) {
                    drawLine(Color.White, Offset(x, 0f), Offset(x, size.height), strokeWidth = 1.dp.toPx(), alpha = lineAlpha)
                    x += gridSpacing
                }
                // Horizontal lines
                var y = 0f
                while (y < size.height) {
                    drawLine(Color.White, Offset(0f, y), Offset(size.width, y), strokeWidth = 1.dp.toPx(), alpha = lineAlpha)
                    y += gridSpacing
                }
            }

            // Blinking LIVE badge + Viewer counts
            Row(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // LIVE red pill
                val infiniteTransition = rememberInfiniteTransition(label = "pulse")
                val alpha by infiniteTransition.animateFloat(
                    initialValue = 0.4f,
                    targetValue = 1.0f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(800, easing = LinearEasing),
                        repeatMode = RepeatMode.Reverse
                    ), label = ""
                )

                Box(
                    modifier = Modifier
                        .background(
                            color = if (isStreaming) TikTokPink.copy(alpha = alpha) else Color.Gray,
                            shape = RoundedCornerShape(4.dp)
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = if (isStreaming) "LIVE" else "OFFLINE",
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Viewer Badge
                Box(
                    modifier = Modifier
                        .background(Color.Black.copy(alpha = 0.6f), shape = RoundedCornerShape(4.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Visibility,
                            contentDescription = "Viewer Count",
                            tint = Color.White,
                            modifier = Modifier.size(12.dp)
                        )
                        Text(
                            text = if (isStreaming) "1.4K penonton" else "0 penonton",
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            // Mode indicator on Top Right
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp)
                    .background(Color.Black.copy(alpha = 0.6f), shape = RoundedCornerShape(4.dp))
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "Mode: $streamMode",
                    color = if (streamMode.contains("Chat")) TikTokTeal else TikTokPeach,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // Main center Avatar Host Graphic
            LivingAvatarRenderer(
                isStreaming = isStreaming,
                isSpeaking = isSpeaking,
                avatarUri = viewModel.settings.avatarUri,
                modifier = Modifier.align(Alignment.Center)
            )

            // Dynamic Subtitles Card at Bottom of stream box
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.85f))
                        )
                    )
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Host Name
                    Text(
                        text = "🎤 TONI - AI Host Toko",
                        color = TikTokTeal,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 2.dp)
                    )
                    
                    // Speech bubbles
                    Box(
                        modifier = Modifier
                            .background(Color.Black.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                            .padding(vertical = 6.dp, horizontal = 12.dp)
                    ) {
                        Text(
                            text = subtitle,
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            lineHeight = 18.sp,
                            maxLines = 3,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }

        // Highlight Active Product Card Showcase (Middle layer)
        AnimatedVisibility(
            visible = isStreaming && activeProduct != null,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            val prod = activeProduct
            if (prod != null) {
                ActiveProductShowcaseRow(product = prod)
            }
        }

        // Middle Divider
        HorizontalDivider(color = HighDensityBorder)

        // Lower Part: TikTok Live Chat scrolling box + Interrupter bar
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .background(TikTokDarkBg)
                .padding(bottom = 8.dp)
        ) {
            // Live Chat Feed
            val listState = rememberLazyListState()

            // Scroll to top (newest is index 0) whenever size increases
            LaunchedEffect(chatComments.size) {
                if (chatComments.isNotEmpty()) {
                    listState.animateScrollToItem(0)
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 12.dp)
            ) {
                if (chatComments.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Obrolan live sunyi...",
                            color = Color.White.copy(alpha = 0.4f),
                            fontSize = 12.sp
                        )
                    }
                } else {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize(),
                        reverseLayout = true, // We want new comments at the bottom but scrolled automatically
                        contentPadding = PaddingValues(vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        items(chatComments, key = { it.id }) { comment ->
                            ChatCommentItem(comment = comment, onPin = {
                                viewModel.sendCommentAndInterrupt(comment.content)
                            })
                        }
                    }
                }
                
                // Floating Help hint
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                        .background(TikTokTeal.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                        .border(1.dp, TikTokTeal.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "Klik komentar untuk dipotong AI",
                        color = TikTokTeal,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Input comment form (Live Chat Interrupter)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 4.dp)
                    .navigationBarsPadding(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = inputText,
                    onValueChange = { viewModel.updateInputText(it) },
                    placeholder = {
                        Text(
                            text = if (isStreaming) "Tulis komentar menyela AI..." else "Aktifkan Live untuk menyela...",
                            fontSize = 13.sp,
                            color = Color.White.copy(alpha = 0.4f)
                        )
                    },
                    enabled = isStreaming,
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp),
                    shape = RoundedCornerShape(26.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedContainerColor = TikTokCardBg,
                        unfocusedContainerColor = TikTokCardBg,
                        disabledContainerColor = TikTokCardBg.copy(alpha = 0.5f),
                        focusedBorderColor = TikTokTeal,
                        unfocusedBorderColor = Color.White.copy(alpha = 0.15f),
                        disabledBorderColor = Color.Transparent
                    ),
                    singleLine = true,
                    trailingIcon = {
                        IconButton(
                            onClick = {
                                viewModel.sendCommentAndInterrupt(inputText)
                            },
                            enabled = isStreaming && inputText.isNotBlank(),
                            modifier = Modifier.padding(end = 4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Send,
                                contentDescription = "Kirim",
                                tint = if (inputText.isNotBlank() && isStreaming) TikTokTeal else Color.White.copy(alpha = 0.3f),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun LivingAvatarRenderer(
    isStreaming: Boolean,
    isSpeaking: Boolean,
    avatarUri: String,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "avatar")
    
    // Pulse scale for active talk
    val scale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = if (isSpeaking) 1.06f else 1.01f,
        animationSpec = infiniteRepeatable(
            animation = tween(if (isSpeaking) 600 else 1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "voiceScale"
    )

    // Speech waves indicator list
    val waveHeights = List(9) { index ->
        infiniteTransition.animateFloat(
            initialValue = 15f,
            targetValue = if (isSpeaking) (25f + (Math.random() * 55f).toFloat()) else 4f,
            animationSpec = infiniteRepeatable(
                animation = tween(200 + (index * 40), easing = LinearOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ), label = "wave_$index"
        )
    }

    Box(
        modifier = modifier
            .size(190.dp),
        contentAlignment = Alignment.Center
    ) {
        // Multi-level radar pulsing rings under image
        if (isSpeaking) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer(scaleX = scale * 1.15f, scaleY = scale * 1.15f)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(TikTokTeal.copy(alpha = 0.35f), Color.Transparent)
                        ),
                        shape = CircleShape
                    )
            )
            Box(
                modifier = Modifier
                    .size(150.dp)
                    .graphicsLayer(scaleX = scale * 1.08f, scaleY = scale * 1.08f)
                    .border(
                        BorderStroke(1.5.dp, Brush.radialGradient(listOf(TikTokPink, Color.Transparent))),
                        shape = CircleShape
                    )
            )
        }

        // Circular Avatar photo
        Box(
            modifier = Modifier
                .size(130.dp)
                .graphicsLayer(scaleX = scale, scaleY = scale)
                .border(
                    BorderStroke(
                        3.dp,
                        Brush.linearGradient(
                            colors = if (isSpeaking) listOf(TikTokTeal, TikTokPink) else listOf(Color.Gray, Color.DarkGray)
                        )
                    ),
                    shape = CircleShape
                )
                .clip(CircleShape)
                .background(Color.DarkGray)
        ) {
            if (avatarUri.isNotEmpty()) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(avatarUri)
                        .crossfade(true)
                        .build(),
                    contentDescription = "AI Live Host Avatar",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                // Perfect procedural drawing for "Host Toni"
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Brush.linearGradient(listOf(Color(0xFF3A1C71), Color(0xFFD76D77)))),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.SmartToy,
                            contentDescription = "Default Host Toni",
                            tint = Color.White,
                            modifier = Modifier.size(44.dp)
                        )
                        Text(
                            text = "TONI AI",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            letterSpacing = 2.sp
                        )
                    }
                }
            }

            // Blinking status overlay on top of image
            if (isSpeaking) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(TikTokTeal.copy(alpha = 0.05f))
                )
            }
        }

        // Floating procedural sound indicator bars
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .offset(y = 12.dp)
                .background(Color.Black.copy(alpha = 0.7f), RoundedCornerShape(12.dp))
                .border(0.5.dp, TikTokTeal.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                .padding(horizontal = 10.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            waveHeights.forEachIndexed { idx, heightVal ->
                Box(
                    modifier = Modifier
                        .width(3.dp)
                        .height(heightVal.value.dp)
                        .background(
                            Brush.verticalGradient(
                                colors = if (idx % 2 == 0) listOf(TikTokTeal, Color.Blue) else listOf(TikTokPink, Color.Yellow)
                            ),
                            shape = RoundedCornerShape(1.dp)
                        )
                )
            }
        }
    }
}

@Composable
fun ActiveProductShowcaseRow(product: Product) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                border = BorderStroke(width = 1.dp, brush = Brush.linearGradient(listOf(TikTokTeal, TikTokPink))),
                shape = RoundedCornerShape(12.dp)
            ),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = TikTokCardBg)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Product image area (Preset vs Custom URI)
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .background(Color.DarkGray, RoundedCornerShape(8.dp))
                    .clip(RoundedCornerShape(8.dp))
            ) {
                // Draw a nice placeholder preset based on imageUrl string if not empty, or simple graphic
                PresetImageRenderer(imageUrl = product.imageUrl)
            }

            // Middle product descriptions
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = product.name,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = product.price,
                    color = TikTokTeal,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 13.sp
                )
                Text(
                    text = product.bpomStatus,
                    color = Color.White.copy(alpha = 0.5f),
                    fontSize = 10.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // TikTok Yellow checkout button
            Button(
                onClick = { /* Beli klik action */ },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFE100)),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                shape = RoundedCornerShape(4.dp),
                modifier = Modifier.height(34.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ShoppingCart,
                        contentDescription = "Beli",
                        tint = Color.Black,
                        modifier = Modifier.size(12.dp)
                    )
                    Text(
                        text = "Beli",
                        color = Color.Black,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp
                    )
                }
            }
        }
    }
}

@Composable
fun ChatCommentItem(
    comment: ChatComment,
    onPin: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(TikTokCardBg.copy(alpha = 0.5f))
            .clickable(onClick = onPin)
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Row(
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // Dummy mini circle color badge
            Box(
                modifier = Modifier
                    .size(18.dp)
                    .offset(y = 2.dp)
                    .background(Color(comment.senderColor), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = comment.username.take(1).uppercase(),
                    color = Color.White,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Column {
                Text(
                    text = comment.username,
                    color = Color(comment.senderColor).copy(alpha = 0.9f),
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp
                )
                Text(
                    text = comment.content,
                    color = Color.White,
                    fontSize = 12.sp,
                    lineHeight = 15.sp
                )
            }
        }
    }
}

@Composable
fun PresetImageRenderer(imageUrl: String) {
    val gradientColors = when (imageUrl) {
        "preset_telon" -> listOf(Color(0xFFE0F7FA), Color(0xFF00ACC1))
        "preset_kunyit" -> listOf(Color(0xFFFFF8E1), Color(0xFFFFB300))
        "preset_madu" -> listOf(Color(0xFFFFF9C4), Color(0xFFFBC02D))
        "preset_collagen" -> listOf(Color(0xFFFCE4EC), Color(0xFFE91E63))
        "preset_teh" -> listOf(Color(0xFFE8F5E9), Color(0xFF43A047))
        else -> listOf(Color(0xFF424242), Color(0xFF212121))
    }

    val presetIcon = when (imageUrl) {
        "preset_telon" -> Icons.Default.ChildCare
        "preset_kunyit" -> Icons.Default.MedicalServices
        "preset_madu" -> Icons.Default.Eco
        "preset_collagen" -> Icons.Default.Face
        "preset_teh" -> Icons.Default.LocalCafe
        else -> Icons.Default.ShoppingBag
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.linearGradient(gradientColors)),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = presetIcon,
            contentDescription = "Produk",
            tint = Color.White,
            modifier = Modifier.size(24.dp)
        )
    }
}
