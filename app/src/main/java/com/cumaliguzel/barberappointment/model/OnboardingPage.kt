package com.cumaliguzel.barberappointment.model

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.cumaliguzel.barberappointment.R

data class OnboardingPage(
    @DrawableRes val imageRes: Int,
    @StringRes val titleRes: Int,
    @StringRes val descriptionRes: Int
)

// Onboarding sayfalarını tanımlayalım
val onboardingPages = listOf(
    OnboardingPage(
        imageRes = R.drawable.onboaring_first,
        titleRes = R.string.onboarding_title_1,
        descriptionRes = R.string.onboarding_desc_1
    ),
    OnboardingPage(
        imageRes = R.drawable.onboarding_second,
        titleRes = R.string.onboarding_title_2,
        descriptionRes = R.string.onboarding_desc_2
    ),
    OnboardingPage(
        imageRes = R.drawable.onboaring_third,
        titleRes = R.string.onboarding_title_3,
        descriptionRes = R.string.onboarding_desc_3
    ),
    OnboardingPage(
        imageRes = R.drawable.onboaring_forth,
        titleRes = R.string.onboarding_title_4,
        descriptionRes = R.string.onboarding_desc_4
    )
) 