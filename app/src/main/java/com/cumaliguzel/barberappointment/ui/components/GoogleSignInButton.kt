package com.cumaliguzel.barberappointment.ui.components

import android.content.Intent
import android.widget.Toast
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.result.ActivityResult
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.cumaliguzel.barberappointment.R
import com.cumaliguzel.barberappointment.auth.GoogleSignInUtils


/**
 * Bu composable giriş ekranında google ile giriş yaparken basılan buttondur neden ayrı yerede oluşturdum?
 * Daha modüller bir yapı için bunu yaptım
 */
@Composable
fun GoogleSignInButton(
    navController: NavController,
    launcher: ManagedActivityResultLauncher<Intent, ActivityResult>,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Button(
            onClick = {
                GoogleSignInUtils.doGoogleSignIn(
                    context = context,
                    scope = scope,
                    launcher = launcher,
                    login = {
                        Toast.makeText(context, "Login successful", Toast.LENGTH_SHORT).show()
                        navController.navigate("main") {
                            popUpTo("signinpage") { inclusive = true }
                        }
                    }
                )
            },
            colors = ButtonDefaults.buttonColors(containerColor = Color.White), // Butonu beyaz yap
            modifier = Modifier
                .height(90.dp)
                .width(240.dp)
                .padding(8.dp),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Google Logo from flat icon .com
                Image(
                    painter = painterResource(R.drawable.google),
                    contentDescription = "Google Sign-In",
                    modifier = Modifier
                        .size(48.dp)
                        .padding(end = 8.dp)
                )

                // Buton Metni
                Text(
                    text = "Sign in with Google",
                    color = Color.Black,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}