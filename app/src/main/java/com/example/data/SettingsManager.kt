package com.example.data

import android.content.Context
import android.content.SharedPreferences

class SettingsManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("tiktok_host_settings", Context.MODE_PRIVATE)

    companion object {
        // Core API Keys
        const val KEY_BRAIN_ENGINE = "brain_engine"
        const val KEY_GEMINI_KEY = "gemini_key"
        const val KEY_OPENAI_KEY = "openai_key"

        const val KEY_VOICE_ENGINE = "voice_engine"
        const val KEY_ELEVENLABS_KEY = "elevenlabs_key"
        const val KEY_ELEVENLABS_VOICE_ID = "elevenlabs_voice_id"
        const val KEY_GCLOUD_TTS_KEY = "gcloud_tts_key"

        const val KEY_AVATAR_ENGINE = "avatar_engine"
        const val KEY_DID_KEY = "did_key"
        const val KEY_HEYGEN_KEY = "heygen_key"

        // State items
        const val KEY_AVATAR_URI = "avatar_uri"
        const val KEY_COMPLIANCE_PROMPT = "compliance_prompt"
        const val KEY_SELLER_NAME = "seller_name"

        // Default compliance instruction (TikTok Shop Indonesia Guardrail for Health/Kesehatan Products)
        val DEFAULT_COMPLIANCE_PROMPT = """
You are "Toni", a high-energy and persuasive AI Live Stream Host for TikTok Shop Indonesia.
You are promoting Indonesian health, beauty, or herbal wellness products.
YOUR HIGHEST PRIORITY IS STRICT TIKTOK ADVERTISING COMPLIANCE.

RULES FOR TOXIC OR OVERCLAIM TERMINOLOGY (BAHASA INDONESIA):
1. NEVER claim a product can "menyembuhkan" (cure), "pasti sembuh" (guaranteed recovery), "mengobati penyakit" (treat diseases), "bebas penyakit selamanya" (disease-free forever), "obat ajaib" (magic medicine/drug).
2. Use educational, supportive, and nourishing phrases instead:
   - "membantu memelihara kesehatan tubuh"
   - "menjaga stamina dan kondisi badan tetap bugar"
   - "mendukung proses pemulihan alami secara teratur"
3. Emphasize that all items in our TikTok Shop are "Sudah terdaftar BPOM dan bersertifikat Halal" so customers can check out with absolute peace of mind.
4. Keep scripts short, snappy, entertaining, and highly interactive. Talk directly to our viewers.
5. End every presentation with a soft disclaimer: "Ingat ya Kak, hasil bervariasi tergantung kondisi fisik masing-masing, selalu jaga pola makan sehat dan banyak minum air putih!"
        """.trimIndent()
    }

    var brainEngine: String
        get() = prefs.getString(KEY_BRAIN_ENGINE, "Gemini") ?: "Gemini"
        set(value) = prefs.edit().putString(KEY_BRAIN_ENGINE, value).apply()

    var geminiKey: String
        get() = prefs.getString(KEY_GEMINI_KEY, "") ?: ""
        set(value) = prefs.edit().putString(KEY_GEMINI_KEY, value).apply()

    var openaiKey: String
        get() = prefs.getString(KEY_OPENAI_KEY, "") ?: ""
        set(value) = prefs.edit().putString(KEY_OPENAI_KEY, value).apply()

    var voiceEngine: String
        get() = prefs.getString(KEY_VOICE_ENGINE, "Native TTS") ?: "Native TTS"
        set(value) = prefs.edit().putString(KEY_VOICE_ENGINE, value).apply()

    var elevenLabsKey: String
        get() = prefs.getString(KEY_ELEVENLABS_KEY, "") ?: ""
        set(value) = prefs.edit().putString(KEY_ELEVENLABS_KEY, value).apply()

    var elevenLabsVoiceId: String
        get() = prefs.getString(KEY_ELEVENLABS_VOICE_ID, "21m00Tcm4TlvDq8ikWAM") ?: "21m00Tcm4TlvDq8ikWAM" // Rachel default
        set(value) = prefs.edit().putString(KEY_ELEVENLABS_VOICE_ID, value).apply()

    var gcloudTtsKey: String
        get() = prefs.getString(KEY_GCLOUD_TTS_KEY, "") ?: ""
        set(value) = prefs.edit().putString(KEY_GCLOUD_TTS_KEY, value).apply()

    var avatarEngine: String
        get() = prefs.getString(KEY_AVATAR_ENGINE, "Simulation") ?: "Simulation"
        set(value) = prefs.edit().putString(KEY_AVATAR_ENGINE, value).apply()

    var didKey: String
        get() = prefs.getString(KEY_DID_KEY, "") ?: ""
        set(value) = prefs.edit().putString(KEY_DID_KEY, value).apply()

    var heygenKey: String
        get() = prefs.getString(KEY_HEYGEN_KEY, "") ?: ""
        set(value) = prefs.edit().putString(KEY_HEYGEN_KEY, value).apply()

    var avatarUri: String
        get() = prefs.getString(KEY_AVATAR_URI, "") ?: ""
        set(value) = prefs.edit().putString(KEY_AVATAR_URI, value).apply()

    var compliancePrompt: String
        get() = prefs.getString(KEY_COMPLIANCE_PROMPT, DEFAULT_COMPLIANCE_PROMPT) ?: DEFAULT_COMPLIANCE_PROMPT
        set(value) = prefs.edit().putString(KEY_COMPLIANCE_PROMPT, value).apply()

    var sellerName: String
        get() = prefs.getString(KEY_SELLER_NAME, "Toko Herbal Berkah") ?: "Toko Herbal Berkah"
        set(value) = prefs.edit().putString(KEY_SELLER_NAME, value).apply()

    fun clearAllSecrets() {
        prefs.edit()
            .remove(KEY_GEMINI_KEY)
            .remove(KEY_OPENAI_KEY)
            .remove(KEY_ELEVENLABS_KEY)
            .remove(KEY_GCLOUD_TTS_KEY)
            .remove(KEY_DID_KEY)
            .remove(KEY_HEYGEN_KEY)
            .apply()
    }
}
