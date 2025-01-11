package com.cumaliguzel.barberappointment.ui.components

import androidx.compose.foundation.layout.size
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.cumaliguzel.barberappointment.R
import com.cumaliguzel.barberappointment.ui.navigation.BottomNavItem
import com.cumaliguzel.barberappointment.ui.theme.ColorGray

@Composable
fun CustomBottomNavigation(
    currentRoute: String?,
    onNavigate: (BottomNavItem) -> Unit
) {
    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface,
        tonalElevation = 8.dp
    ) {
        val items = listOf(
            BottomNavItem.Appointments,
            BottomNavItem.DailyEarnings,
            BottomNavItem.Pricing
        )

        items.forEach { item ->
            val selected = currentRoute == item.route
            NavigationBarItem(
                icon = {
                    Icon(
                        painter = painterResource(id = item.iconRes),
                        contentDescription = item.title,
                        modifier = Modifier.size(24.dp)
                    )
                },
                label = {
                    Text(
                        text = stringResource(getStringResourceId(item.title)),
                        style = MaterialTheme.typography.labelMedium
                    )
                },
                selected = selected,
                onClick = { onNavigate(item) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.primary,
                    unselectedIconColor = ColorGray,
                    selectedTextColor = MaterialTheme.colorScheme.primary,
                    unselectedTextColor = ColorGray,
                    indicatorColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    }
}

private fun getStringResourceId(resourceName: String): Int {
    return when (resourceName) {
        "nav_appointments" -> R.string.nav_appointments
        "nav_earnings" -> R.string.nav_earnings
        "nav_pricing" -> R.string.nav_pricing
        else -> R.string.app_name
    }
}
