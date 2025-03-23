package com.cumaliguzel.barberappointment.ui.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.navigation.NavController
import com.cumaliguzel.barberappointment.ui.navigation.BottomNavItem
import com.cumaliguzel.barberappointment.viewmodel.GuidanceViewModel
import kotlinx.coroutines.launch

@Composable
fun GuidanceHandler(
    navController: NavController,
    guidanceViewModel: GuidanceViewModel,
    currentRoute: String?,
    onFabClick: () -> Unit
) {
    val showWelcomeDialog by guidanceViewModel.showWelcomeDialog.collectAsState()
    val showServicesDialog by guidanceViewModel.showServicesDialog.collectAsState()
    val triggerFabClick by guidanceViewModel.triggerFabClick.collectAsState()
    val scope = rememberCoroutineScope()
    
    // Başlangıç hoşgeldin dialog'u
    if (showWelcomeDialog && currentRoute == BottomNavItem.Appointments.route) {
        AlertDialog(
            onDismissRequest = { /* Boş bırakılacak */ },
            title = { Text("Berber Uygulamasına Hoş Geldiniz") },
            text = { Text("Hizmet eklemeden uygulamayı kullanamazsınız. Devam etmek için Hizmetler sayfasına geçin.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        // Önce diyaloğu kapatın, sonra navigasyon yapın
                        guidanceViewModel.onWelcomeDialogDismissed()
                        
                        // Navigasyon kontrolü için scope kullanın
                        scope.launch {
                            // Basit navigasyon - direkt olarak pricing sayfasına git
                            navController.navigate(BottomNavItem.Pricing.route) {
                                // Gereksiz back stack girişlerini temizle
                                popUpTo(BottomNavItem.Appointments.route) {
                                    saveState = true 
                                }
                                launchSingleTop = true
                            }
                            
                            // 300ms bekleyelim ve ikinci diyaloğu gösterelim
                            guidanceViewModel.showServicesDialog()
                        }
                    }
                ) {
                    Text("Tamam")
                }
            }
        )
    }
    
    // Servis ekleme dialog'u
    if (showServicesDialog && currentRoute == BottomNavItem.Pricing.route) {
        AlertDialog(
            onDismissRequest = { /* Boş bırakılacak */ },
            title = { Text("Hizmetlerinizi Ekleyin") },
            text = { 
                Text("""
                    Hizmetlerinizi ve ücretlerini şimdi ekleyin.
                    Eğer birden fazla hizmetiniz varsa hepsini tek tek tanımlamayı unutmayın.
                    
                    Örnek:
                    • Saç Tıraşı – 500₺
                    • Saç & Sakal – 700₺
                    • Sakal – 200₺
                """.trimIndent())
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        // Diyaloğu kapat ve FAB'ı tetikle
                        guidanceViewModel.onServicesDialogDismissed()
                    }
                ) {
                    Text("Tamam")
                }
            }
        )
    }
    
    // FAB butonunu tetikle
    LaunchedEffect(triggerFabClick) {
        if (triggerFabClick) {
            onFabClick()
            guidanceViewModel.onFabClicked() 
        }
    }
} 