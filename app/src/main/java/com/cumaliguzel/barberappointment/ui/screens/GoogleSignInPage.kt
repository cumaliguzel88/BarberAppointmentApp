package com.cumaliguzel.barberappointment.ui.screens

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.cumaliguzel.barberappointment.R
import com.cumaliguzel.barberappointment.auth.GoogleSignInUtils
import com.cumaliguzel.barberappointment.ui.components.GoogleSignInButton
import com.cumaliguzel.barberappointment.ui.theme.ColorGreen
import com.cumaliguzel.barberappointment.ui.theme.ColorGreenDark
import com.cumaliguzel.barberappointment.ui.theme.ColorWhite
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import kotlinx.coroutines.delay
import android.util.Log
import androidx.compose.ui.res.stringResource

@Composable
fun GoogleSignInPage(navController: NavController, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val currentUser = Firebase.auth.currentUser
    var isLoading by remember { mutableStateOf(false) }
    
    // Animasyon için değişkenler
    var showTitle by remember { mutableStateOf(false) }
    var showSubtitle by remember { mutableStateOf(false) }
    var showButton by remember { mutableStateOf(false) }
    
    // Animasyonları sırayla başlat
    LaunchedEffect(key1 = true) {
        delay(100)
        showTitle = true
        delay(300)
        showSubtitle = true
        delay(300)
        showButton = true
    }
    
    // Launcher tanımla
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        isLoading = false
        GoogleSignInUtils.doGoogleSignIn(
            context = context,
            scope = scope,
            launcher = null,
            login = {
                Toast.makeText(context,R.string.login_toat_message, Toast.LENGTH_SHORT).show()
                navController.navigate("main") {
                    popUpTo("signinpage") { inclusive = true }
                }
            }
        )
    }

    // Kullanıcı zaten giriş yapmışsa ana ekrana yönlendir
    LaunchedEffect(currentUser) {
        if (currentUser != null) {
            Log.d("GoogleSignInPage", "Kullanıcı zaten giriş yapmış: ${currentUser.displayName}")
            navController.navigate("main") {
                popUpTo("signinpage") { inclusive = true }
            }
        } else {
            // Otomatik giriş akışını buradan kaldırdık - kullanıcı artık butonla giriş yapacak
            // Bu şekilde tasarımımızı görebilecek
        }
    }

    // Arka plan gradyant
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        ColorGreen,
                        ColorGreenDark
                    )
                )
            )
    ) {
        // Dekoratif desen
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(250.dp)
                .align(Alignment.TopCenter)
        ) {
            Image(
                painter = painterResource(id = R.drawable.onboaring_first), // Bir onboarding resmini dekoratif olarak kullanıyoruz
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
                alpha = 0.3f
            )
        }
        
        // Ana içerik
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Spacer(modifier = Modifier.height(20.dp))
            
            // Logo ve başlık bölümü
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Logo
                Image(
                    painter = painterResource(id = R.drawable.barber_app_logo), // Kendi uygulamanızın logosunu buraya ekleyin
                    contentDescription = "App Logo",
                    modifier = Modifier
                        .size(150.dp)
                        .clip(RoundedCornerShape(25.dp))

                )
                Spacer(modifier = Modifier.height(24.dp))
                
                // Başlık
                AnimatedVisibility(
                    visible = showTitle,
                    enter = fadeIn(animationSpec = tween(500)) + 
                            slideInVertically(animationSpec = tween(500)) { it }
                ) {
                    Text(
                        text = stringResource(id = R.string.login_title),
                        color = Color.White,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Alt başlık
                AnimatedVisibility(
                    visible = showSubtitle,
                    enter = fadeIn(animationSpec = tween(500)) + 
                            slideInVertically(animationSpec = tween(500)) { it }
                ) {
                    Text(
                        text = stringResource(id = R.string.login_subtitle),
                        color = Color.White.copy(alpha = 0.9f),
                        fontSize = 16.sp,
                        textAlign = TextAlign.Center,
                        lineHeight = 24.sp
                    )
                }
            }
            
            // Google Sign-In bölümü
            AnimatedVisibility(
                visible = showButton,
                enter = fadeIn(animationSpec = tween(500)) + 
                        slideInVertically(animationSpec = tween(500)) { it / 2 }
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 32.dp),
                    colors = CardDefaults.cardColors(containerColor = ColorWhite),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 32.dp, horizontal = 16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = stringResource(R.string.login_login),
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = ColorGreenDark
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Text(
                            text = stringResource(R.string.login_google_login),
                            fontSize = 14.sp,
                            color = Color.Gray,
                            textAlign = TextAlign.Center
                        )
                        
                        Spacer(modifier = Modifier.height(14.dp))
                        
                        if (isLoading) {
                            CircularProgressIndicator(
                                color = ColorGreen,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Giriş yapılıyor...",
                                fontSize = 16.sp,
                                color = ColorGreen
                            )
                        } else {
                            GoogleSignInButton(
                                navController = navController,
                                launcher = launcher,
                                modifier = Modifier.padding(8.dp)
                            )
                        }
                    }
                }
            }
            
            // Alt bilgi
            AnimatedVisibility(
                visible = showButton,
                enter = fadeIn(animationSpec = tween(700))
            ) {
                Text(
                    text = "© 2025 Berber Randevu",
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 12.sp,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }
        }
    }
}