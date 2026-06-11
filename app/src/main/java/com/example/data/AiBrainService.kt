package com.example.data

import android.util.Log
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.TimeUnit

class AiBrainService {

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    companion object {
        const val MODEL_GEMINI = "gemini-3.5-flash"
    }

    /**
     * Generates persistent sales scripts or Q&A replies using the configured LLM API.
     */
    suspend fun generateText(
        prompt: String,
        systemInstruction: String,
        engine: String,
        geminiKey: String,
        openaiKey: String
    ): String = withContext(Dispatchers.IO) {
        try {
            if (engine.lowercase() == "gemini") {
                if (geminiKey.isBlank()) {
                    return@withContext "[MOCK FLOW - DAFTARKAN API KEY DI PENGATURAN]\n\n" + generateFallbackResponse(prompt)
                }
                return@withContext callGeminiApi(prompt, systemInstruction, geminiKey)
            } else {
                if (openaiKey.isBlank()) {
                    return@withContext "[MOCK FLOW - DAFTARKAN API KEY DI PENGATURAN]\n\n" + generateFallbackResponse(prompt)
                }
                return@withContext callOpenAiApi(prompt, systemInstruction, openaiKey)
            }
        } catch (e: Exception) {
            Log.e("AiBrainService", "Error generating response: ${e.message}", e)
            return@withContext "Error: ${e.localizedMessage}. Menjalankan respon standar...\n\n" + generateFallbackResponse(prompt)
        }
    }

    private fun callGeminiApi(prompt: String, systemInstruction: String, apiKey: String): String {
        // Correct endpoint according to skill:
        // https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=$apiKey
        val url = "https://generativelanguage.googleapis.com/v1beta/models/$MODEL_GEMINI:generateContent?key=$apiKey"

        val json = JSONObject()
        val contentsArray = JSONArray()
        val contentObj = JSONObject()
        val partsArray = JSONArray()
        val partObj = JSONObject()
        partObj.put("text", prompt)
        partsArray.put(partObj)
        contentObj.put("parts", partsArray)
        contentsArray.put(contentObj)
        json.put("contents", contentsArray)

        if (systemInstruction.isNotBlank()) {
            val systemInstructionObj = JSONObject()
            val sysPartsArray = JSONArray()
            val sysPartObj = JSONObject()
            sysPartObj.put("text", systemInstruction)
            sysPartsArray.put(sysPartObj)
            systemInstructionObj.put("parts", sysPartsArray)
            json.put("systemInstruction", systemInstructionObj)
        }

        // Add temperature configuration
        val genConfig = JSONObject()
        genConfig.put("temperature", 0.7)
        json.put("generationConfig", genConfig)

        val mediaType = "application/json; charset=utf-8".toMediaType()
        val body = json.toString().toRequestBody(mediaType)

        val request = Request.Builder()
            .url(url)
            .post(body)
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                val errBody = response.body?.string() ?: ""
                Log.e("AiBrainService", "Gemini API failure: Code ${response.code}, message: $errBody")
                throw Exception("API Error Code: ${response.code}. Details: $errBody")
            }

            val responseBodyString = response.body?.string() ?: throw Exception("Empty response from Gemini")
            val respJson = JSONObject(responseBodyString)
            val candidates = respJson.getJSONArray("candidates")
            val firstCandidate = candidates.getJSONObject(0)
            val content = firstCandidate.getJSONObject("content")
            val parts = content.getJSONArray("parts")
            return parts.getJSONObject(0).getString("text")
        }
    }

    private fun callOpenAiApi(prompt: String, systemInstruction: String, apiKey: String): String {
        val url = "https://api.openai.com/v1/chat/completions"

        val json = JSONObject()
        json.put("model", "gpt-4o-mini")

        val messages = JSONArray()
        
        // System message if present
        if (systemInstruction.isNotBlank()) {
            val systemMsg = JSONObject()
            systemMsg.put("role", "system")
            systemMsg.put("content", systemInstruction)
            messages.put(systemMsg)
        }

        // User message
        val userMsg = JSONObject()
        userMsg.put("role", "user")
        userMsg.put("content", prompt)
        messages.put(userMsg)

        json.put("messages", messages)
        json.put("temperature", 0.7)

        val mediaType = "application/json; charset=utf-8".toMediaType()
        val body = json.toString().toRequestBody(mediaType)

        val request = Request.Builder()
            .url(url)
            .header("Authorization", "Bearer $apiKey")
            .post(body)
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                val errBody = response.body?.string() ?: ""
                Log.e("AiBrainService", "OpenAI API failure: Code ${response.code}")
                throw Exception("API Error Code: ${response.code}. Details: $errBody")
            }

            val responseBodyString = response.body?.string() ?: throw Exception("Empty response from OpenAI")
            val respJson = JSONObject(responseBodyString)
            val choices = respJson.getJSONArray("choices")
            val firstChoice = choices.getJSONObject(0)
            val message = firstChoice.getJSONObject("message")
            return message.getString("content")
        }
    }

    /**
     * Synthesizes audio using ElevenLabs or Google Cloud TTS APIs.
     * Returns a File pointing to the generated MP3, or null if mock/error.
     */
    suspend fun generateSpeechAudio(
        text: String,
        engine: String,
        apiKey: String,
        voiceIdOrToken: String,
        cacheFile: File
    ): File? = withContext(Dispatchers.IO) {
        try {
            if (apiKey.isBlank()) return@withContext null

            if (engine.lowercase().contains("eleven")) {
                val vId = if (voiceIdOrToken.isBlank()) "21m00Tcm4TlvDq8ikWAM" else voiceIdOrToken
                val url = "https://api.elevenlabs.io/v1/text-to-speech/$vId"

                val json = JSONObject()
                json.put("text", text)
                json.put("model_id", "eleven_multilingual_v2")
                
                val voiceSettings = JSONObject()
                voiceSettings.put("stability", 0.5)
                voiceSettings.put("similarity_boost", 0.75)
                json.put("voice_settings", voiceSettings)

                val mediaType = "application/json; charset=utf-8".toMediaType()
                val body = json.toString().toRequestBody(mediaType)

                val request = Request.Builder()
                    .url(url)
                    .header("xi-api-key", apiKey)
                    .post(body)
                    .build()

                client.newCall(request).execute().use { response ->
                    if (!response.isSuccessful) {
                        Log.e("AiBrainService", "ElevenLabs failure: Code ${response.code}")
                        return@withContext null
                    }
                    val bodyStream = response.body?.byteStream() ?: return@withContext null
                    FileOutputStream(cacheFile).use { out ->
                        bodyStream.copyTo(out)
                    }
                    return@withContext cacheFile
                }
            } else if (engine.lowercase().contains("gcloud") || engine.lowercase().contains("google")) {
                val url = "https://texttospeech.googleapis.com/v1/text:synthesize?key=$apiKey"

                val json = JSONObject()
                val inputObj = JSONObject()
                inputObj.put("text", text)
                json.put("input", inputObj)

                val voiceObj = JSONObject()
                voiceObj.put("languageCode", "id-ID")
                voiceObj.put("name", "id-ID-Wavenet-A")
                json.put("voice", voiceObj)

                val configObj = JSONObject()
                configObj.put("audioEncoding", "MP3")
                json.put("audioConfig", configObj)

                val mediaType = "application/json; charset=utf-8".toMediaType()
                val body = json.toString().toRequestBody(mediaType)

                val request = Request.Builder()
                    .url(url)
                    .post(body)
                    .build()

                client.newCall(request).execute().use { response ->
                    if (!response.isSuccessful) {
                        Log.e("AiBrainService", "Google Cloud TTS failure: Code ${response.code}")
                        return@withContext null
                    }
                    val respStr = response.body?.string() ?: return@withContext null
                    val respJson = JSONObject(respStr)
                    val base64Audio = respJson.optString("audioContent", "")
                    if (base64Audio.isBlank()) return@withContext null

                    val decodedBytes = android.util.Base64.decode(base64Audio, android.util.Base64.DEFAULT)
                    FileOutputStream(cacheFile).use { out ->
                        out.write(decodedBytes)
                    }
                    return@withContext cacheFile
                }
            }
        } catch (e: Exception) {
            Log.e("AiBrainService", "Error in speech audio generation: ${e.message}")
        }
        return@withContext null
    }

    /**
     * Fallback rules-based responses in clean Indonesian for compliance with TikTok Shop
     * when there is no API Key saved.
     */
    private fun generateFallbackResponse(prompt: String): String {
        val query = prompt.lowercase()
        return when {
            query.contains("telon") || query.contains("anak") || query.contains("bayi") -> {
                "Halo kakak penonton semuanya! Buat si kecil yang sering kembung atau sering digigit nyamuk, kami punya solusinya. Ini dia Minyak Telon Fit Plus! Membantu memelihara kesehatan tubuh dan kehangatan si kecil tersayang. Tenang kak, produk ini 100% aman, sudah bersertifikat BPOM dan Halal! Hangatnya pas, harum lavendernya bikin rileks. Yuk Kak, langsung check out di keranjang kuning sekarang juga, mumpung ada promo gratis ongkir!"
            }
            query.contains("kunyit") || query.contains("temulawak") || query.contains("pencernaan") || query.contains("lambung") -> {
                "Halo Kakakku! Selamat datang di live shopping kami! Kakak punya masalah pencernaan yang sering begah, mual, atau nafsu makan menurun? Kakak wajib coba Kaspul Kunyit Temulawak Fit ini! Formulasi herbal alaminya sangat baik untuk membantu menjaga kesehatan pencernaan Kakak secara menyeluruh. Ingat, ini bukan ramuan sulap ya Kak, melainkan herbal murni berizin BPOM yang membantu menjaga kebugaran alami. Segera klik keranjang kuning Kak, stok sisa sedikit!"
            }
            query.contains("madu") || query.contains("stamina") || query.contains("loyo" ) || query.contains("lemas") -> {
                "Wah, lagi merasa lemas dan kurang stamina karena aktivitas padat harian? Tenang Kak! Langsung konsumsi Madu Hutan Multifloral Berkah! Ini madu murni 100% dari hutan tropis alami tanpa tambahan pemanis buatan. Sangat bermanfaat untuk menjaga kondisi tubuh agar tetap prima, bugar, dan berstamina penuh tiap hari. Sudah BPOM dan dijamin Halal ya Kak! Klik keranjang sekarang sebelum harganya kembali normal!"
            }
            query.contains("kulit") || query.contains("glowing") || query.contains("collagen") || query.contains("keriput") -> {
                "Halo Kakak cantik dan ganteng! Mau kulit tampak sehat, lembab, dan awet muda secara alami? Rahasianya ada di Collagen Glow Berry Drink! Minuman nikmat serbuk kolagen premium ini kaya kandungan Vitamin C seimbang untuk membantu memelihara kelembapan kulit wajah Kakak tercinta dari dalam. Bersih, aman, higienis, dan sudah terdaftar BPOM dan Halal. Yuk, segera check out di keranjang kuning untuk glow up alami!"
            }
            query.contains("diet") || query.contains("teh") || query.contains("lemak") || query.contains("kurus") -> {
                "Untuk Kakak yang mendambakan berat badan ideal secara bugar, mari merapat! Ini dia Teh Hijau Diet Detoks Alami! Membantu melancarkan proses metabolisme tubuh dan menyegarkan pencernaan Kakak dengan racikan teh hijau alami berizin BPOM. Rasanya menyegarkan dan tidak pahit berlebihan. Yuk Kak, klik tombol beli sekarang juga dan nikmati diskon spesial hari ini!"
            }
            else -> {
                "Selamat datang Kakak-kakak di toko kami! Semua produk kami terjamin berkhasiat untuk membantu menjaga kondisi tubuh tetap sehat, prima, serta bugar sepanjang hari. Kami sudah terdaftar resmi di BPOM dan bersertifikat Halal, jadi dijamin sangat aman dikonsumsi. Kakak ada pertanyaan tentang produk yang mana nih? Langsung tulis di kolom komentar ya Kak, nanti Toni bantu rekomendasikan yang terbaik buat Kakak!"
            }
        }
    }
}
