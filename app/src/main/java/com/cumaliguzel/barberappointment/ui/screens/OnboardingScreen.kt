package com.cumaliguzel.barberappointment.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.lifecycle.viewmodel.compose.viewModel
import com.cumaliguzel.barberappointment.R
import com.cumaliguzel.barberappointment.model.onboardingPages
import com.cumaliguzel.barberappointment.ui.theme.ColorGreen
import com.cumaliguzel.barberappointment.ui.theme.ColorGreenLight
import com.cumaliguzel.barberappointment.ui.theme.ColorWhite
import com.cumaliguzel.barberappointment.viewmodel.OnboardingViewModel

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun OnboardingScreen(
    navController: NavController,
    viewModel: OnboardingViewModel = viewModel()
) {
    val context = LocalContext.current
    val currentPage = viewModel.currentPage.value
    
    // Kullanıcı daha önce onboarding'i tamamlamışsa, doğrudan giriş sayfasına yönlendir
    LaunchedEffect(key1 = true) {
        if (viewModel.isOnboardingCompleted(context)) {
            navController.navigate("signinpage") {
                popUpTo("onboarding") { inclusive = true }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Skip butonu
        if (currentPage < 3) {
            TextButton(
                onClick = {
                    viewModel.saveOnboardingCompleted(context)
                    navController.navigate("signinpage") {
                        popUpTo("onboarding") { inclusive = true }
                    }
                },
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp)
            ) {
                Text(
                    text = stringResource(id = R.string.onboarding_skip),
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Sayfa içeriği
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Spacer(modifier = Modifier.height(40.dp))
            
            // Resim
            AnimatedVisibility(
                visible = true,
                enter = fadeIn() + slideInHorizontally(),
                exit = fadeOut() + slideOutHorizontally()
            ) {
                Image(
                    painter = painterResource(id = onboardingPages[currentPage].imageRes),
                    contentDescription = null,
                    modifier = Modifier
                        .height(300.dp)
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp),
                    contentScale = ContentScale.Fit
                )
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Başlık ve açıklama
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = stringResource(id = onboardingPages[currentPage].titleRes),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.primary
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = stringResource(id = onboardingPages[currentPage].descriptionRes),
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 24.dp),
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f)
                )
            }
            
            Spacer(modifier = Modifier.weight(1f))
            
            // Indicator ve butonlar
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Page Indicator
                Row(
                    modifier = Modifier
                        .padding(bottom = 32.dp),
                    horizontalArrangement = Arrangement.Center
                ) {
                    repeat(onboardingPages.size) { iteration ->
                        val color = if (currentPage == iteration) ColorGreen else ColorGreenLight
                        val width = if (currentPage == iteration) 24.dp else 10.dp
                        
                        Box(
                            modifier = Modifier
                                .padding(2.dp)
                                .clip(CircleShape)
                                .background(color)
                                .width(width)
                                .height(10.dp)
                        )
                    }
                }
                
                // Butonlar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 24.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Geri butonu (ilk sayfada gizli)
                    if (currentPage > 0) {
                        Button(
                            onClick = { viewModel.previousPage() },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = ColorGreenLight
                            ),
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .height(56.dp)
                                .width(120.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = stringResource(id = R.string.onboarding_back),
                                tint = ColorWhite
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = stringResource(id = R.string.onboarding_back),
                                color = ColorWhite,
                                fontSize = 16.sp
                            )
                        }
                    } else {
                        Spacer(modifier = Modifier.width(120.dp)) // Boşluk
                    }
                    
                    // İleri/Başla butonu
                    Button(
                        onClick = {
                            if (currentPage < onboardingPages.size - 1) {
                                viewModel.nextPage()
                            } else {
                                // Son sayfada, onboarding tamamlandı olarak işaretle
                                viewModel.saveOnboardingCompleted(context)
                                navController.navigate("signinpage") {
                                    popUpTo("onboarding") { inclusive = true }
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ColorGreen
                        ),
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .height(56.dp)
                            .width(if (currentPage == onboardingPages.size - 1) 160.dp else 120.dp)
                    ) {
                        if (currentPage == onboardingPages.size - 1) {
                            Text(
                                text = stringResource(id = R.string.onboarding_get_started),
                                color = ColorWhite,
                                fontSize = 16.sp
                            )
                        } else {
                            Text(
                                text = stringResource(id = R.string.onboarding_next),
                                color = ColorWhite,
                                fontSize = 16.sp
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.Default.ArrowForward,
                                contentDescription = stringResource(id = R.string.onboarding_next),
                                tint = ColorWhite
                            )
                        }
                    }
                }
            }
        }
    }
} 