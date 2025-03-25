package com.cumaliguzel.barberappointment

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.app.ActivityCompat
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.cumaliguzel.barberappointment.ui.navigation.AppNavigation
import com.cumaliguzel.barberappointment.ui.navigation.BottomNavItem
import com.cumaliguzel.barberappointment.ui.theme.BarberAppointmentTheme
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.view.Gravity
import com.cumaliguzel.barberappointment.usecase.NotificationUseCase
import androidx.work.WorkManager
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.cumaliguzel.barberappointment.ui.screens.GoogleSignInPage
import com.cumaliguzel.barberappointment.ui.screens.OnboardingScreen
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import com.cumaliguzel.barberappointment.viewmodel.GuidanceViewModel
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import com.cumaliguzel.barberappointment.ui.screens.SplashScreen
import com.cumaliguzel.barberappointment.ui.components.AppNavigationBar

class MainActivity : ComponentActivity() {
    private val notificationUseCase: NotificationUseCase by lazy {
        (application as BarberApplication).notificationUseCase
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Bildirim kanalı oluştur
        notificationUseCase.createNotificationChannel()
        
        // Bildirim izinlerini kontrol et
        checkNotificationPermissions()

        setContent {
            BarberAppointmentTheme {
                // Splash Screen state
                var showSplash by remember { mutableStateOf(true) }
                
                // Kullanıcı durumunu kontrol et
                val startDestination = remember {
                    // Firebase kullanıcısını ve onboarding durumunu kontrol et
                    val userLoggedIn = Firebase.auth.currentUser != null
                    val onboardingCompleted = getSharedPreferences("barber_prefs", MODE_PRIVATE)
                        .getBoolean("onboarding_completed", false)
                    
                    when {
                        userLoggedIn -> "main" // Kullanıcı zaten giriş yapmış
                        onboardingCompleted -> "signinpage" // Onboarding tamamlanmış, giriş yapılacak
                        else -> "onboarding" // Yeni kullanıcı, onboarding gösterilecek
                    }
                }
                
                if (showSplash) {
                    SplashScreen(onSplashFinished = { showSplash = false })
                } else {
                    // Ana navController
                    val navController = rememberNavController()
                    
                    NavHost(
                        navController = navController,
                        startDestination = startDestination // Dinamik başlangıç rotası
                    ) {
                        // Onboarding ekranı
                        composable("onboarding") {
                            OnboardingScreen(navController = navController)
                        }
                        // Google Sign-In sayfası
                        composable("signinpage") {
                            GoogleSignInPage(navController = navController)
                        }
                        // Ana ekran
                        composable("main") {
                            MainScreen()
                        }
                        // Diğer rotalar...
                    }
                }
            }
        }
    }
    
    private fun checkNotificationPermissions() {
        if (!notificationUseCase.areNotificationsEnabled()) {
            Handler(Looper.getMainLooper()).postDelayed({
                showNotificationPermissionDialog()
            }, 1000)
        }
    }
    
    private fun showNotificationPermissionDialog() {
        // Kullanıcıya bildirim izni isteme diyaloğu göster
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                100
            )
        } else {
            showNotificationPermissionToast()
        }
    }
    
    private fun showNotificationPermissionToast() {
        Toast.makeText(
            this,
            "Bildirimleri görebilmek için lütfen bildirim izinlerini etkinleştirin",
            Toast.LENGTH_LONG
        ).show()
        
        // Ayarlar butonunu göster
        Handler(Looper.getMainLooper()).postDelayed({
            Toast.makeText(
                this,
                "Ayarları açmak için buraya tıklayın",
                Toast.LENGTH_LONG
            ).apply {
                setGravity(Gravity.CENTER, 0, 0)
                view?.setOnClickListener {
                    openNotificationSettings()
                    cancel()
                }
                show()
            }
        }, 3000)
    }
    
    private fun openNotificationSettings() {
        notificationUseCase.openNotificationSettings()
    }
    
    // İzin sonuçlarını işle
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        
        if (requestCode == 100) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // İzin verildi
                Log.d("MainActivity", "✅ Bildirim izni verildi")
            } else {
                // İzin reddedildi
                Log.d("MainActivity", "⚠️ Bildirim izni reddedildi")
                showNotificationPermissionToast()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen() {
    val navController = rememberNavController()
    val items = listOf(
        BottomNavItem.Appointments,
        BottomNavItem.DailyEarnings,
        BottomNavItem.Statistics,
        BottomNavItem.Pricing
    )

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    
    // GuidanceViewModel
    val guidanceViewModel: GuidanceViewModel = viewModel()
    
    Scaffold(
        bottomBar = {
            if (currentRoute in items.map { it.route }) {
                AppNavigationBar(
                    navController = navController,
                    currentRoute = currentRoute,
                    items = items,
                    guidanceViewModel = guidanceViewModel
                )
            }
        }
    ) { paddingValues ->
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            AppNavigation(
                navController = navController,
                modifier = Modifier.padding(paddingValues)
            )
        }
    }
}
