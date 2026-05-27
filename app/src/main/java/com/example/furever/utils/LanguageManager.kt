package com.example.furever.utils

// utils/LanguageManager.kt
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat

object LanguageManager {

    val supportedLanguages = linkedMapOf(
        "Español"    to "es",
        "English"    to "en",
        "Português"  to "pt"
    )

    fun setLanguage(languageCode: String) {
        val locales = LocaleListCompat.forLanguageTags(languageCode)
        AppCompatDelegate.setApplicationLocales(locales)

    }

    fun getCurrentLanguageCode(): String {
        val locales = AppCompatDelegate.getApplicationLocales()
        return if (locales.isEmpty) {
            java.util.Locale.getDefault().language
        } else {
            locales.toLanguageTags()
        }
    }
}