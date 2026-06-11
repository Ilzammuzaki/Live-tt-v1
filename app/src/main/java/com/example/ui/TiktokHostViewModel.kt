package com.example.ui

import android.app.Application
import android.media.MediaPlayer
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import java.io.File
import java.util.*

data class ChatComment(
    val id: String = UUID.randomUUID().toString(),
    val username: String,
    val content: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isPinned: Boolean = false,
    val senderColor: Int = (0xFF000000.toInt() or (Math.random() * 0xFFFFFF).toInt())
)

class TiktokHostViewModel(application: Application) : AndroidViewModel(application), TextToSpeech.OnInitListener {

    private val context = application.applicationContext
    private val database = AppDatabase.getDatabase(context)
    private val repository = ProductRepository(database.productDao())
    val settings = SettingsManager(context)
    private val aiService = AiBrainService()

    // UI States
    val products = repository.allProducts.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    private val _activeProduct = MutableStateFlow<Product?>(null)
    val activeProduct: StateFlow<Product?> = _activeProduct.asStateFlow()

    private val _subtitle = MutableStateFlow("Selamat datang Kakak! Toni siap memandu siaran langsung 24/7 Toko Herbal.")
    val subtitle: StateFlow<String> = _subtitle.asStateFlow()

    private val _isStreaming = MutableStateFlow(false)
    val isStreaming: StateFlow<Boolean> = _isStreaming.asStateFlow()

    private val _streamMode = MutableStateFlow("Idle Loop (Promo Otomatis)")
    val streamMode: StateFlow<String> = _streamMode.asStateFlow()

    private val _isSpeaking = MutableStateFlow(false)
    val isSpeaking: StateFlow<Boolean> = _isSpeaking.asStateFlow()

    private val _chatComments = MutableStateFlow<List<ChatComment>>(emptyList())
    val chatComments: StateFlow<List<ChatComment>> = _chatComments.asStateFlow()

    private val _inputText = MutableStateFlow("")
    val inputText: StateFlow<String> = _inputText.asStateFlow()

    // Native TextToSpeech Engine
    private var tts: TextToSpeech? = null
    private var isTtsInitialized = false

    // MediaPlayer for external TTS audio playing
    private var mediaPlayer: MediaPlayer? = null

    // Job control for automated stream looping and comment generation
    private var streamJob: Job? = null
    private var simulatedCommentsJob: Job? = null
    private var speakingCompletable: CompletableDeferred<Unit>? = null

    init {
        // Initialize Native Speech Engine
        tts = TextToSpeech(context, this)

        // Generate initial welcome chats
        _chatComments.value = listOf(
            ChatComment(username = "Siti_Herbalis", content = "Halo Kak Toni yang selalu ceria! Penonton setia hadirrr"),
            ChatComment(username = "Budi_Santoso", content = "Kak, ini beneran berizin BPOM kan seluruh herbalnya?"),
            ChatComment(username = "GlowUp_Putri", content = "Collagen berry-nya sisa berapa box kak? Pengen checkout")
        )
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val result = tts?.setLanguage(Locale("id", "ID"))
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e("TiktokHostViewModel", "Indonesian language not supported in external engine. Fallback to default.")
                tts?.language = Locale.getDefault()
            }
            isTtsInitialized = true
            setupTtsListeners()
        } else {
            Log.e("TiktokHostViewModel", "Failed to initialize Native TextToSpeech engine.")
        }
    }

    private fun setupTtsListeners() {
        tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) {
                _isSpeaking.value = true
            }

            override fun onDone(utteranceId: String?) {
                _isSpeaking.value = false
                speakingCompletable?.complete(Unit)
            }

            override fun onError(utteranceId: String?) {
                _isSpeaking.value = false
                speakingCompletable?.complete(Unit)
            }
        })
    }

    fun updateInputText(text: String) {
        _inputText.value = text
    }

    // Settings Modification Helpers
    fun saveApiKeys(
        gemini: String,
        openai: String,
        elevenLabs: String,
        gcloud: String,
        did: String,
        heygen: String
    ) {
        settings.geminiKey = gemini
        settings.openaiKey = openai
        settings.elevenLabsKey = elevenLabs
        settings.gcloudTtsKey = gcloud
        settings.didKey = did
        settings.heygenKey = heygen
    }

    fun saveConfigChoices(brain: String, voice: String, avatar: String) {
        settings.brainEngine = brain
        settings.voiceEngine = voice
        settings.avatarEngine = avatar
    }

    fun saveCompliancePrompt(prompt: String) {
        settings.compliancePrompt = prompt
    }

    fun updateSellerName(name: String) {
        settings.sellerName = name
    }

    // CRUD Product operations
    fun addProduct(product: Product) {
        viewModelScope.launch {
            repository.insertProduct(product)
        }
    }

    fun updateProduct(product: Product) {
        viewModelScope.launch {
            repository.updateProduct(product)
        }
    }

    fun deleteProduct(product: Product) {
        viewModelScope.launch {
            repository.deleteProduct(product)
        }
    }

    // Comments simulator
    private val randomUsernames = listOf(
        "Rian_Gamerz", "Ayu_Cantik", "Siska_Skincare", "Hendra_Herbal", "Mumu_Mulyadi",
        "Dewi_Glow", "Agus_Rider", "Indah_Kusuma", "Raka_Wicaksono", "Nur_Hayati", "Adit_Keren"
    )

    private val commentTemplates = listOf(
        "Kak, ready stok nggak?",
        "Diskon hari ini ada apa aja kak?",
        "Madu multifloralnya aman buat lambung?",
        "Bisa kirim hari ini ke Bandung kak?",
        "Bumil busui aman pakai semua?",
        "Ada sertifikat BPOMnya lengkap ya?",
        "Minyak telon lavender baunya seger gaa?",
        "Temulawak kapsul diminum berapa kali sehari?",
        "Check out sekarang dapet bonus apa kakaa?"
    )

    private fun startSimulatedComments() {
        simulatedCommentsJob?.cancel()
        simulatedCommentsJob = viewModelScope.launch {
            while (isActive) {
                delay(8000 + (Math.random() * 5000).toLong())
                val randomUser = randomUsernames.random()
                val randomTxt = commentTemplates.random()
                
                val newComment = ChatComment(
                    username = randomUser,
                    content = randomTxt
                )
                
                val current = _chatComments.value.toMutableList()
                current.add(0, newComment)
                if (current.size > 25) {
                    current.removeAt(current.lastIndex)
                }
                _chatComments.value = current
            }
        }
    }

    // Control Live Streaming
    fun toggleStreaming() {
        if (_isStreaming.value) {
            stopLiveStream()
        } else {
            startLiveStream()
        }
    }

    private fun startLiveStream() {
        _isStreaming.value = true
        _subtitle.value = "Menghubungkan AI Streamer... Memulai Live Host Toni!"
        startSimulatedComments()
        
        // Start script evaluation loop
        streamJob?.cancel()
        streamJob = viewModelScope.launch {
            while (isActive) {
                val activeList = products.value.filter { it.isEnabled }
                if (activeList.isEmpty()) {
                    _subtitle.value = "Daftar produk aktif kosong. Harap tambahkan atau aktifkan produk di inventaris."
                    _activeProduct.value = null
                    delay(3000)
                    continue
                }

                // Loop through each product sequentially (Mode 1 Idle Loop)
                for (prod in activeList) {
                    if (!isActive || !_isStreaming.value) break
                    
                    _activeProduct.value = prod
                    _streamMode.value = "Idle Loop (Promo Otomatis)"
                    
                    val prompt = """
Generate a viral and compliant short pitch (approximately 100-140 words in friendly, high-energy spoken Indonesian language) to promote our product during a TikTok Live stream.

PRODUCT INFORMATION:
Nama: ${prod.name}
Harga: ${prod.price}
Deskripsi: ${prod.description}
BPOM/Halal status: ${prod.bpomStatus}

Follow all core guidelines in our COMPLIANCE GUARDRAIL:
${settings.compliancePrompt}

Ensure you use soft-selling and supportive Indonesian terms. Never use banned terms. Act as our host "Toni", speak passionately directly to the camera!
                    """.trimIndent()

                    _subtitle.value = "Sedang merancang script persuasif untuk ${prod.name}..."
                    
                    val generatedScript = aiService.generateText(
                        prompt = prompt,
                        systemInstruction = settings.compliancePrompt,
                        engine = settings.brainEngine,
                        geminiKey = settings.geminiKey,
                        openaiKey = settings.openaiKey
                    )

                    // Display subtitles and speak
                    speakOut(generatedScript)
                    
                    // Wait for the speaking to finish before moving to next product
                    awaitSpeakingToComplete()
                    
                    // Small delay between rotations
                    delay(5000)
                }
            }
        }
    }

    fun stopLiveStream() {
        _isStreaming.value = false
        _isSpeaking.value = false
        streamJob?.cancel()
        simulatedCommentsJob?.cancel()
        stopSpeakingAudio()
        _activeProduct.value = null
        _streamMode.value = "Offline"
        _subtitle.value = "Siaran dinonaktifkan. Host Toni sedang istirahat."
    }

    /**
     * Triggers simulated Chat Comment Interruption (Mode 2)
     */
    fun sendCommentAndInterrupt(commentText: String) {
        if (commentText.isBlank()) return
        val current = _chatComments.value.toMutableList()
        val userComment = ChatComment(username = "Pembeli_Sabar", content = commentText)
        current.add(0, userComment)
        _chatComments.value = current
        _inputText.value = ""

        if (!_isStreaming.value) return

        // Interrupt active loop and immediately reply to comments
        streamJob?.cancel()
        streamJob = viewModelScope.launch {
            _streamMode.value = "Chat Interrupter (Balas Komentar)"
            stopSpeakingAudio()

            // Find context product based on comment keywords, fallback to first active product
            val activeList = products.value.filter { it.isEnabled }
            var relatedProduct = activeList.firstOrNull()
            for (prod in activeList) {
                if (commentText.lowercase().contains(prod.name.lowercase().substringBefore(" ")) ||
                    commentText.lowercase().contains(prod.description.lowercase().substringBefore(" "))
                ) {
                    relatedProduct = prod
                    break
                }
            }

            _activeProduct.value = relatedProduct
            val productDetailsStr = if (relatedProduct != null) {
                """
PRODUK YANG RELEVAN:
Nama: ${relatedProduct.name}
Harga: ${relatedProduct.price}
Deskripsi: ${relatedProduct.description}
BPOM: ${relatedProduct.bpomStatus}
                """.trimIndent()
            } else {
                "TIDAK ADA PRODUK KHUSUS YANG RELEVAN."
            }

            val prompt = """
PENGGUNA BERTANYA PADA LIVE TIKTOK CHAT:
"${commentText}"

$productDetailsStr

Generate a short, enthusiastic, extremely warm and compliant answer in Indonesian spoken language (about 80-120 words).
You are host "Toni". Answer the customer directly with high empathy, soft-selling tone, and follow the compliance guide below. Do not use banned words (e.g. do not say 'menyembuhkan' or 'pasti sembuh'). Ensure you mention that checking out right now is the best option!

COMPLIANCE GUARDRAIL:
${settings.compliancePrompt}
            """.trimIndent()

            _subtitle.value = "Menjawab pertanyaan: \"$commentText\"..."
            
            val replyText = aiService.generateText(
                prompt = prompt,
                systemInstruction = settings.compliancePrompt,
                engine = settings.brainEngine,
                geminiKey = settings.geminiKey,
                openaiKey = settings.openaiKey
            )

            speakOut(replyText)
            awaitSpeakingToComplete()

            // Small cooldown
            delay(5000)
            
            // Resume the main stream rotation
            startLiveStream()
        }
    }

    /**
     * Executes the speech through available systems (BYOK TTS vs Native offline system).
     */
    private fun speakOut(sentence: String) {
        _isSpeaking.value = true
        _subtitle.value = sentence

        val cleanSentence = sentence.replace("[MOCK FLOW - DAFTARKAN API KEY DI PENGATURAN]", "").trim()

        viewModelScope.launch {
            var audioFile: File? = null
            // Check if user has ElevenLabs / GCloud keys set and designated voice engines
            if (settings.voiceEngine != "Native TTS") {
                val key = if (settings.voiceEngine.contains("Eleven")) settings.elevenLabsKey else settings.gcloudTtsKey
                val cache = File(context.cacheDir, "speech_temp.mp3")
                if (key.isNotBlank()) {
                    audioFile = aiService.generateSpeechAudio(
                        text = cleanSentence,
                        engine = settings.voiceEngine,
                        apiKey = key,
                        voiceIdOrToken = settings.elevenLabsVoiceId,
                        cacheFile = cache
                    )
                }
            }

            if (audioFile != null && audioFile.exists()) {
                playAudioFile(audioFile)
            } else {
                // FALLBACK TO NATIVE SYSTEM SPEECH (Runs offline, completely free, flawless Indonesian!)
                if (isTtsInitialized) {
                    speakingCompletable = CompletableDeferred()
                    val params = Bundle()
                    params.putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "ToniSpeech")
                    tts?.speak(cleanSentence, TextToSpeech.QUEUE_FLUSH, params, "ToniSpeech")
                } else {
                    // Fail-safe mock duration
                    speakingCompletable = CompletableDeferred()
                    val mockDuration = (cleanSentence.split(" ").size * 350).toLong().coerceIn(4000, 15000)
                    delay(mockDuration)
                    _isSpeaking.value = false
                    speakingCompletable?.complete(Unit)
                }
            }
        }
    }

    private suspend fun awaitSpeakingToComplete() {
        speakingCompletable?.await()
    }

    private fun playAudioFile(file: File) {
        stopSpeakingAudio()
        speakingCompletable = CompletableDeferred()
        mediaPlayer = MediaPlayer().apply {
            try {
                setDataSource(file.absolutePath)
                prepare()
                start()
                setOnCompletionListener {
                    _isSpeaking.value = false
                    speakingCompletable?.complete(Unit)
                    release()
                }
                setOnErrorListener { _, _, _ ->
                    _isSpeaking.value = false
                    speakingCompletable?.complete(Unit)
                    release()
                    true
                }
            } catch (e: Exception) {
                Log.e("TiktokHostViewModel", "MediaPlayer play error: ${e.message}")
                _isSpeaking.value = false
                speakingCompletable?.complete(Unit)
            }
        }
    }

    private fun stopSpeakingAudio() {
        try {
            if (mediaPlayer?.isPlaying == true) {
                mediaPlayer?.stop()
            }
            mediaPlayer?.release()
            mediaPlayer = null
        } catch (_: Exception) {}
        
        try {
            tts?.stop()
        } catch (_: Exception) {}
        
        _isSpeaking.value = false
    }

    override fun onCleared() {
        stopLiveStream()
        try {
            tts?.shutdown()
        } catch (_: Exception) {}
        super.onCleared()
    }
}
