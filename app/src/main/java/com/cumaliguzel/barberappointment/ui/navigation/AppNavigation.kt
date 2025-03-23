package com.cumaliguzel.barberappointment.ui.navigation

import OperationPricesScreen
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import com.cumaliguzel.barberappointment.ui.components.GuidanceHandler
import com.cumaliguzel.barberappointment.ui.screens.*
import com.cumaliguzel.barberappointment.viewmodel.GuidanceViewModel

@Composable
fun AppNavigation(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    // Rehberlik sistemi için ViewModel
    val guidanceViewModel: GuidanceViewModel = viewModel()
    
    // FAB tıklama referansı
    var fabClickAction by remember { mutableStateOf<() -> Unit>({}) }
    
    // Current route
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    
    NavHost(
        navController = navController,
        startDestination = BottomNavItem.Appointments.route,
        modifier = modifier
    ) {
        composable(BottomNavItem.Appointments.route) {
            AppointmentListScreen(
                onAddAppointment = { navController.navigate("appointment/add") },
                onEditAppointment = { id -> navController.navigate("appointment/edit/$id") }
            )
        }
        
        composable(BottomNavItem.DailyEarnings.route) {
            DailyEarningsScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
        
        composable(BottomNavItem.Statistics.route) {
            StatisticsScreen()
        }
        
        composable(BottomNavItem.Pricing.route) {
            OperationPricesScreen(
                onNavigateBack = { navController.popBackStack() },
                onFabClick = { action -> 
                    fabClickAction = action
                }
            )
        }
        
        composable("appointment/add") {
            AddEditAppointmentScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
        
        composable("appointment/edit/{id}") { backStackEntry ->
            val id = backStackEntry.arguments?.getString("id")?.toIntOrNull() ?: -1
            AddEditAppointmentScreen(
                appointmentId = id,
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
    
    // Sadece GuidanceHandler kalsın, gereksiz LaunchedEffect'leri kaldıralım
    if (currentRoute != null) {
        GuidanceHandler(
            navController = navController,
            guidanceViewModel = guidanceViewModel,
            currentRoute = currentRoute,
            onFabClick = { fabClickAction() }
        )
    }
} 