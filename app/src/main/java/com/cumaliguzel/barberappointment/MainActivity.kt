package com.cumaliguzel.barberappointment

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
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
import com.cumaliguzel.barberappointment.ui.theme.ColorGray
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.util.Log

class MainActivity : ComponentActivity() {
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (!isGranted) {
            Toast.makeText(
                this,
                "Bildirimleri görebilmek için izin vermeniz gerekiyor",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Android 13+ (TIRAMISU) için POST_NOTIFICATIONS izni kontrolü
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        } 
        // Android 10 ve altı (API 29 ve altı) için
        else {
            // Android 8.0+ için bildirim kanalı izinlerini kontrol et
            checkNotificationPermissions()
        }

        setContent {
            BarberAppointmentTheme {
                MainScreen()
            }
        }
    }
    
    private fun checkNotificationPermissions() {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        
        // Android 8.0+ (API 26+) için bildirim kanalı ayarları kontrolü
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (!notificationManager.areNotificationsEnabled()) {
                // Bildirimler devre dışı bırakılmışsa, kullanıcıyı bildir ve ayarlara yönlendir
                showNotificationPermissionToast()
            }
        } else {
            // Android 8.0 öncesi sürümler için
            try {
                val enabled = notificationManager.areNotificationsEnabled()
                if (!enabled) {
                    showNotificationPermissionToast()
                }
            } catch (e: Exception) {
                Log.e("MainActivity", "Bildirim izni kontrolünde hata: ${e.message}")
            }
        }
    }
    
    private fun showNotificationPermissionToast() {
        val toast = Toast.makeText(
            this,
            "Bildirimlere izin vermeniz gerekiyor. Ayarlar'a gitmek için tıklayın.",
            Toast.LENGTH_LONG
        )
        toast.show()
        
        // Ayrı bir tıklanabilir mekanizma oluştur
        val handler = android.os.Handler(mainLooper)
        handler.postDelayed({
            openNotificationSettings()
        }, 3000) // 3 saniye sonra ayarları aç
    }
    
    private fun openNotificationSettings() {
        val intent = Intent()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            intent.action = Settings.ACTION_APP_NOTIFICATION_SETTINGS
            intent.putExtra(Settings.EXTRA_APP_PACKAGE, packageName)
        } else {
            intent.action = "android.settings.APP_NOTIFICATION_SETTINGS"
            intent.putExtra("app_package", packageName)
            intent.putExtra("app_uid", applicationInfo.uid)
        }
        startActivity(intent)
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
