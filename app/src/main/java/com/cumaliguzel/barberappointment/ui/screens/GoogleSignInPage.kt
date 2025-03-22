package com.cumaliguzel.barberappointment.ui.screens

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.cumaliguzel.barberappointment.auth.GoogleSignInUtils
import com.cumaliguzel.barberappointment.ui.components.GoogleSignInButton
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import android.util.Log

@Composable
fun GoogleSignInPage(navController: NavController, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val currentUser = Firebase.auth.currentUser
    var isLoading by remember { mutableStateOf(false) }
    
    // ✅ Launcher'ı LaunchedEffect'ten ÖNCE tanımla
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        isLoading = false
        GoogleSignInUtils.doGoogleSignIn(
            context = context,
            scope = scope,
            launcher = null,
            login = {
                Toast.makeText(context, "Giriş başarılı", Toast.LENGTH_SHORT).show()
                navController.navigate("main") {
                    popUpTo("signinpage") { inclusive = true } // Geriye dönmeyi engelle
                }
            }
        )
    }

    // LaunchedEffect launcher'ı SONRA kullanır
    LaunchedEffect(currentUser) {
        if (currentUser != null) {
            Log.d("GoogleSignInPage", "Kullanıcı zaten giriş yapmış: ${currentUser.displayName}")
            navController.navigate("main") {
                popUpTo("signinpage") { inclusive = true }
            }
        } else {
            // Kullanıcı giriş yapmamışsa, otomatik olarak Google Sign-In sürecini başlat
            isLoading = true
            val signInIntent = GoogleSignInUtils.getGoogleSignInIntent(context)
            launcher.launch(signInIntent)
        }
    }

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (isLoading) {
                CircularProgressIndicator()
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Google ile giriş yapılıyor...",
                    fontSize = 18.sp,
                    textAlign = TextAlign.Center
                )
            } else {
                // Eğer otomatik giriş başarısız olursa, kullanıcı manuel olarak giriş yapabilir
                Text(
                    text = "Google hesabınızla giriş yapın",
                    fontSize = 20.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                GoogleSignInButton(
                    navController = navController,
                    launcher = launcher,
                    modifier = Modifier.padding(8.dp)
                )
            }
        }
    }
}