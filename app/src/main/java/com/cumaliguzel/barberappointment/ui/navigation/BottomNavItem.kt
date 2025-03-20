package com.cumaliguzel.barberappointment.ui.navigation

import com.cumaliguzel.barberappointment.R

sealed class BottomNavItem(
    val route: String,
    val title: String,
    val iconRes: Int
) {
    data object Appointments : BottomNavItem(
        route = "appointments",
        title = "Randevular ",
        iconRes = R.drawable.baseline_date_range_24
    )

    data object DailyEarnings : BottomNavItem(
        route = "daily-earnings",
        title = "Kazanç ",
        iconRes = R.drawable.baseline_money_24
    )

    data object Statistics : BottomNavItem(
        route = "statistics",
        title = "İstatistikler",
        iconRes = R.drawable.baseline_bar_chart_24
    )

    data object Pricing : BottomNavItem(
        route = "operation-prices",
        title = "Fiyatlar ",
        iconRes = R.drawable.baseline_attach_money_24
    )
}
