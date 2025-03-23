package com.cumaliguzel.barberappointment.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class GuidanceViewModel(application: Application) : AndroidViewModel(application) {
    
    private val sharedPrefs = application.getSharedPreferences("guidance_prefs", Context.MODE_PRIVATE)
    
    private val _showWelcomeDialog = MutableStateFlow(false)
    val showWelcomeDialog: StateFlow<Boolean> = _showWelcomeDialog.asStateFlow()
    
    private val _showServicesDialog = MutableStateFlow(false)
    val showServicesDialog: StateFlow<Boolean> = _showServicesDialog.asStateFlow()
    
    private val _triggerFabClick = MutableStateFlow(false)
    val triggerFabClick: StateFlow<Boolean> = _triggerFabClick.asStateFlow()

    init {
        checkAndInitializeGuidance()
    }

    private fun checkAndInitializeGuidance() {
        val guidanceShown = sharedPrefs.getBoolean("initial_guide_shown", false)
        if (!guidanceShown) {
            _showWelcomeDialog.value = true
        }
    }

    fun onWelcomeDialogDismissed() {
        _showWelcomeDialog.value = false
    }
    
    fun showServicesDialog() {
        viewModelScope.launch {
            delay(1000) // 1 saniye gecikme
            _showServicesDialog.value = true
        }
    }
    
    fun onServicesDialogDismissed() {
        _showServicesDialog.value = false
        _triggerFabClick.value = true
    }
    
    fun resetNavigationState() {
        // Tüm state'leri sıfırla
        _showWelcomeDialog.value = false
        _showServicesDialog.value = false
        _triggerFabClick.value = false
        
        // Rehberliği tamamlandı olarak işaretle
        viewModelScope.launch {
            sharedPrefs.edit()
                .putBoolean("initial_guide_shown", true)
                .putBoolean("guidance_completed_this_session", true)
                .apply()
        }
    }
    
    fun onFabClicked() {
        _triggerFabClick.value = false
        resetNavigationState()
    }
    
    // ✅ Yönlendirmeden sonra yapılacak navigasyonları engelleme
    fun isGuidanceActive(): Boolean {
        // Eğer bu oturumda rehberlik tamamlandıysa, aktif değil olarak işaretle
        if (sharedPrefs.getBoolean("guidance_completed_this_session", false)) {
            return false
        }
        return _showWelcomeDialog.value || _showServicesDialog.value || _triggerFabClick.value
    }
} 