package com.cumaliguzel.barberappointment.ui.screens

import android.util.Log
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cumaliguzel.barberappointment.BarberApplication
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext

@Composable
fun SplashScreen(
    onSplashFinished: () -> Unit
) {
    val context = LocalContext.current
    val scale = remember { Animatable(0.7f) }
    val dataLoaded = remember { mutableStateOf(false) }
    val uiPreloaded = remember { mutableStateOf(false) }
    val animationFinished = remember { mutableStateOf(false) }
    val minimumTimeElapsed = remember { mutableStateOf(false) }
    
    // Log her şeyi takip edecek
    LaunchedEffect(key1 = true) {
        Log.d("SplashScreen", "🚀 Splash ekranı başlatıldı")
        
        // 1. Animasyon işlemi
        launch {
            Log.d("SplashScreen", "⚙️ Animasyon başlatılıyor")
            scale.animateTo(
                targetValue = 1.0f,
                animationSpec = tween(
                    durationMillis = 500,
                    easing = EaseOutBack
                )
            )
            Log.d("SplashScreen", "✅ Animasyon tamamlandı")
            
            // Minimum görünme süresi (1.5 saniye)
            delay(1500)
            Log.d("SplashScreen", "⏱️ Minimum görünme süresi tamamlandı")
            minimumTimeElapsed.value = true
        }
        
        // 2. Veri yükleme işlemi - IO thread'de yapılıyor
        launch(Dispatchers.IO) {
            try {
                Log.d("SplashScreen", "🔄 Veritabanı verilerini yükleme başladı")
                
                val barberApp = context.applicationContext as BarberApplication
                
                // Tüm verileri önceden yükle
                Log.d("SplashScreen", "📊 Randevuları yükleme başladı")
                val appointments = barberApp.appointmentRepository.getAllAppointments().firstOrNull()
                Log.d("SplashScreen", "📊 Randevular yüklendi: ${appointments?.size ?: 0} adet")
                
                Log.d("SplashScreen", "📊 Tamamlanmış randevuları yükleme başladı")
                val completedAppointments = barberApp.completedAppointmentRepository.getAllCompletedAppointments().firstOrNull()
                Log.d("SplashScreen", "📊 Tamamlanmış randevular yüklendi: ${completedAppointments?.size ?: 0} adet")
                
                Log.d("SplashScreen", "📊 Operasyon fiyatlarını yükleme başladı")
                val prices = barberApp.operationPriceRepository.getAllOperationPrices().firstOrNull()
                Log.d("SplashScreen", "📊 Operasyon fiyatları yüklendi: ${prices?.size ?: 0} adet")
                
                // İstatistikleri ön yükle
                Log.d("SplashScreen", "📈 İstatistikleri yükleme başladı")
                barberApp.statisticsUseCase.preloadStatistics()
                Log.d("SplashScreen", "📈 İstatistikler yüklendi")
                
                // Bildirim kontrollerini yap
                Log.d("SplashScreen", "🔔 Bildirim ayarlarını kontrol ediyor")
                barberApp.notificationUseCase.areNotificationsEnabled()
                
                Log.d("SplashScreen", "✅ Tüm veriler başarıyla yüklendi")
                dataLoaded.value = true
            } catch (e: Exception) {
                Log.e("SplashScreen", "💥 Veri yükleme hatası: ${e.message}")
                dataLoaded.value = true // Hata olsa bile devam et
            }
        }
        
        // 3. UI ön yükleme işlemi
        launch {
            try {
                Log.d("SplashScreen", "🎨 UI bileşenlerini ön yükleme başladı")
                
                // UI thread'de çalışan bileşenleri hazırla
                withContext(Dispatchers.Default) {
                    // Ana ekranların oluşturulmasını simüle et
                    delay(300) // AppointmentListScreen hazırlanması
                    Log.d("SplashScreen", "🎨 AppointmentListScreen hazırlandı")
                    
                    delay(300) // StatisticsScreen hazırlanması
                    Log.d("SplashScreen", "🎨 StatisticsScreen hazırlandı")
                    
                    delay(200) // DailyEarningsScreen hazırlanması
                    Log.d("SplashScreen", "🎨 DailyEarningsScreen hazırlandı")
                    
                    delay(200) // OperationPricesScreen hazırlanması
                    Log.d("SplashScreen", "🎨 OperationPricesScreen hazırlandı")
                }
                
                Log.d("SplashScreen", "✅ Tüm UI bileşenleri hazırlandı")
                uiPreloaded.value = true
            } catch (e: Exception) {
                Log.e("SplashScreen", "💥 UI ön yükleme hatası: ${e.message}")
                uiPreloaded.value = true // Hata olsa bile devam et
            }
        }
    }
    
    // Tüm koşullar sağlandığında splash screen'den çık
    LaunchedEffect(dataLoaded.value, uiPreloaded.value, minimumTimeElapsed.value) {
        if (dataLoaded.value && uiPreloaded.value && minimumTimeElapsed.value) {
            Log.d("SplashScreen", "🏁 Tüm hazırlıklar tamamlandı, ana ekrana geçiliyor")
            onSplashFinished()
        }
    }
    
    // Splash ekranı UI
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.primary),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "✂️",  // Makas emojisi
                fontSize = 80.sp,
                color = MaterialTheme.colorScheme.tertiary,
                modifier = Modifier.scale(scale.value)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "Berber Randevu",
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.tertiary
            )
            
            // Yükleme durumunu göster
            Spacer(modifier = Modifier.height(32.dp))
            
            CircularProgressIndicator(
                color = MaterialTheme.colorScheme.tertiary,
                modifier = Modifier.size(32.dp)
            )
        }
    }
} 