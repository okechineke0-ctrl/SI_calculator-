package com.example.data

import android.content.Context
import android.content.SharedPreferences
import com.example.math.AngleMode
import com.example.math.NumberNotation
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class AppPreferences(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("ocean_math_prefs", Context.MODE_PRIVATE)

    private val _angleMode = MutableStateFlow(loadAngleMode())
    val angleMode: StateFlow<AngleMode> = _angleMode.asStateFlow()

    private val _precision = MutableStateFlow(loadPrecision())
    val precision: StateFlow<Int> = _precision.asStateFlow()

    private val _notation = MutableStateFlow(loadNotation())
    val notation: StateFlow<NumberNotation> = _notation.asStateFlow()

    private val _hapticEnabled = MutableStateFlow(loadHaptic())
    val hapticEnabled: StateFlow<Boolean> = _hapticEnabled.asStateFlow()

    private val _customApiKey = MutableStateFlow(loadCustomApiKey())
    val customApiKey: StateFlow<String> = _customApiKey.asStateFlow()

    private val _supabaseUrl = MutableStateFlow(loadSupabaseUrl())
    val supabaseUrl: StateFlow<String> = _supabaseUrl.asStateFlow()

    private val _supabaseAnonKey = MutableStateFlow(loadSupabaseAnonKey())
    val supabaseAnonKey: StateFlow<String> = _supabaseAnonKey.asStateFlow()

    private val _pythonBackendUrl = MutableStateFlow(loadPythonBackendUrl())
    val pythonBackendUrl: StateFlow<String> = _pythonBackendUrl.asStateFlow()

    private fun loadAngleMode(): AngleMode {
        val str = prefs.getString("angle_mode", AngleMode.DEG.name) ?: AngleMode.DEG.name
        return try { AngleMode.valueOf(str) } catch (e: Exception) { AngleMode.DEG }
    }

    fun setAngleMode(mode: AngleMode) {
        prefs.edit().putString("angle_mode", mode.name).apply()
        _angleMode.value = mode
    }

    private fun loadPrecision(): Int {
        return prefs.getInt("precision", 10)
    }

    fun setPrecision(p: Int) {
        val clamped = p.coerceIn(2, 12)
        prefs.edit().putInt("precision", clamped).apply()
        _precision.value = clamped
    }

    private fun loadNotation(): NumberNotation {
        val str = prefs.getString("notation", NumberNotation.STANDARD.name) ?: NumberNotation.STANDARD.name
        return try { NumberNotation.valueOf(str) } catch (e: Exception) { NumberNotation.STANDARD }
    }

    fun setNotation(n: NumberNotation) {
        prefs.edit().putString("notation", n.name).apply()
        _notation.value = n
    }

    private fun loadHaptic(): Boolean {
        return prefs.getBoolean("haptic_enabled", true)
    }

    fun setHaptic(enabled: Boolean) {
        prefs.edit().putBoolean("haptic_enabled", enabled).apply()
        _hapticEnabled.value = enabled
    }

    private fun loadCustomApiKey(): String {
        return prefs.getString("custom_gemini_api_key", "") ?: ""
    }

    fun setCustomApiKey(key: String) {
        prefs.edit().putString("custom_gemini_api_key", key.trim()).apply()
        _customApiKey.value = key.trim()
    }

    private fun loadSupabaseUrl(): String {
        return prefs.getString("supabase_url", "") ?: ""
    }

    fun setSupabaseUrl(url: String) {
        prefs.edit().putString("supabase_url", url.trim()).apply()
        _supabaseUrl.value = url.trim()
    }

    private fun loadSupabaseAnonKey(): String {
        return prefs.getString("supabase_anon_key", "") ?: ""
    }

    fun setSupabaseAnonKey(key: String) {
        prefs.edit().putString("supabase_anon_key", key.trim()).apply()
        _supabaseAnonKey.value = key.trim()
    }

    private fun loadPythonBackendUrl(): String {
        return prefs.getString("python_backend_url", "") ?: ""
    }

    fun setPythonBackendUrl(url: String) {
        prefs.edit().putString("python_backend_url", url.trim()).apply()
        _pythonBackendUrl.value = url.trim()
    }
}
