package com.cumaliguzel.barberappointment.viewmodel

import android.content.Context
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class OnboardingViewModel : ViewModel() {
    private val _currentPage = mutableStateOf(0)
    val currentPage: State<Int> = _currentPage

    // Sayfa değiştirme işlemleri
    fun nextPage() {
        if (_currentPage.value < 3) {
            _currentPage.value += 1
        }
    }

    fun previousPage() {
        if (_currentPage.value > 0) {
            _currentPage.value -= 1
        }
    }

    fun goToPage(page: Int) {
        if (page in 0..3) {
            _currentPage.value = page
        }
    }

    // Onboarding tamamlandı olarak işaretle
    fun saveOnboardingCompleted(context: Context) {
        viewModelScope.launch {
            context.getSharedPreferences("barber_prefs", Context.MODE_PRIVATE)
                .edit()
                .putBoolean("onboarding_completed", true)
                .apply()
        }
    }

    // Onboarding tamamlanmış mı kontrol et
    fun isOnboardingCompleted(context: Context): Boolean {
        return context.getSharedPreferences("barber_prefs", Context.MODE_PRIVATE)
            .getBoolean("onboarding_completed", false)
    }
} 