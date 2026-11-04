package com.example.data.repository

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SettingsRepository(context: Context) {
    private val prefs: SharedPreferences =
        context.applicationContext.getSharedPreferences("deutsch_weg_settings", Context.MODE_PRIVATE)

    companion object {
        const val KEY_MAX_NEW_CARDS_PER_DAY = "key_max_new_cards_per_day"
        const val DEFAULT_MAX_NEW_CARDS_PER_DAY = 20
    }

    private val _maxNewCardsPerDay = MutableStateFlow(
        prefs.getInt(KEY_MAX_NEW_CARDS_PER_DAY, DEFAULT_MAX_NEW_CARDS_PER_DAY)
    )
    val maxNewCardsPerDay: StateFlow<Int> = _maxNewCardsPerDay.asStateFlow()

    fun getMaxNewCardsPerDay(): Int {
        return prefs.getInt(KEY_MAX_NEW_CARDS_PER_DAY, DEFAULT_MAX_NEW_CARDS_PER_DAY)
    }

    fun setMaxNewCardsPerDay(count: Int) {
        val clamped = count.coerceIn(1, 100)
        prefs.edit().putInt(KEY_MAX_NEW_CARDS_PER_DAY, clamped).apply()
        _maxNewCardsPerDay.value = clamped
    }
}
