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

class MainActivity : ComponentActivity() {
    private lateinit var notificationUseCase: NotificationUseCase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // WorkManager instance'ını almak
        val workManager = WorkManager.getInstance(applicationContext)
        
        // NotificationUseCase oluştur
        notificationUseCase = NotificationUseCase(applicationContext, workManager)
        
        // Bildirim kanalı oluştur
        notificationUseCase.createNotificationChannel(NotificationUseCase.NOTIFICATION_CHANNEL_ID)
        
        // Bildirim izinlerini kontrol et
        checkNotificationPermissions()

        setContent {
            BarberAppointmentTheme {
                MainScreen()
            }
        }
    }
    
    private fun checkNotificationPermissions() {
        // Android 13+ için POST_NOTIFICATIONS izni gerekir
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            when {
                ContextCompat.checkSelfPermission(
                    this,
                    android.Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED -> {
                    // İzin zaten var
                    Log.d("MainActivity", "✅ Bildirim izni mevcut (Android 13+)")
                }
                shouldShowRequestPermissionRationale(android.Manifest.permission.POST_NOTIFICATIONS) -> {
                    // Kullanıcı daha önce izni reddetti, açıklama göster
                    Log.d("MainActivity", "⚠️ Bildirim izni reddedilmiş (Android 13+)")
                    showNotificationPermissionToast()
                }
                else -> {
                    // İzni iste
                    Log.d("MainActivity", "🔄 Bildirim izni isteniyor (Android 13+)")
                    ActivityCompat.requestPermissions(
                        this,
                        arrayOf(android.Manifest.permission.POST_NOTIFICATIONS),
                        100
                    )
                }
            }
        } else {
            // Android 10 ve altı için
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                if (!notificationManager.areNotificationsEnabled()) {
                    // Bildirimler devre dışı, kullanıcıyı ayarlara yönlendir
                    Log.d("MainActivity", "⚠️ Bildirimler devre dışı (Android 10)")
                    showNotificationPermissionToast()
                } else {
                    Log.d("MainActivity", "✅ Bildirimler etkin (Android 10)")
                }
            } else {
                // Android 7 altı için izin kontrolü yapılamaz
                Log.d("MainActivity", "ℹ️ Bildirim izni kontrol edilemiyor (Android <7)")
            }
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
                Log.d("MainActivity", "Bildirim izni verildi")
            } else {
                // İzin reddedildi
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

    Scaffold(
        bottomBar = {
            if (currentRoute in items.map { it.route }) {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.primary,
                ) {
                    items.forEach { item ->
                        NavigationBarItem(
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.tertiary,
                                unselectedIconColor = MaterialTheme.colorScheme.tertiary,
                                selectedTextColor = MaterialTheme.colorScheme.tertiary,
                                unselectedTextColor = MaterialTheme.colorScheme.tertiary,
                                indicatorColor = MaterialTheme.colorScheme.onSecondary,
                            ),
                            alwaysShowLabel = false,
                            icon = {
                                Icon(
                                    painter = painterResource(id = item.iconRes),
                                    contentDescription = item.title
                                )
                            },
                            label = { Text(item.title) },
                            selected = currentRoute == item.route,
                            onClick = {
                                if (currentRoute != item.route) {
                                    navController.navigate(item.route) {
                                        popUpTo(navController.graph.startDestinationId) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            }
                        )
                    }
                }
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
