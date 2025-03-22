package com.cumaliguzel.barberappointment.auth

import android.content.Context
import android.content.Intent
import android.provider.Settings
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.result.ActivityResult
import androidx.credentials.CredentialManager
import androidx.credentials.CredentialOption
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import androidx.credentials.exceptions.NoCredentialException
import com.cumaliguzel.barberappointment.R
import com.cumaliguzel.barberappointment.data.User
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions

/***
 * BU sayfanın temel amacı Google sign in ile kullanıcı girişini yönetmektir.
 * Google sign in tek giriş çıkış mekanizmasıdır.Neden? -->
 * Şimdilik özellikle 1.0 için bu özellik yetrli olacaktır.Eger mail and password ile giriş yaparsak işimiz uzar
// * Ayrıca her google account ın bir kullanıcı resimi ve adı var mesajlaşma kısmı için kulanılıyor ayran gibi temiz kafası leziz
 */



class GoogleSignInUtils {
    companion object {
        fun doGoogleSignIn(
            context: Context,
            scope: CoroutineScope,
            launcher: ManagedActivityResultLauncher<Intent, ActivityResult>?,
            login: () -> Unit
        ) {
            val credentialManager = CredentialManager.create(context)
            val request = GetCredentialRequest.Builder()
                .addCredentialOption(getCredentialOptions(context))
                .build()

            scope.launch {
                try {
                    val result = credentialManager.getCredential(context, request)
                    when (result.credential) {
                        is CustomCredential -> {
                            if (result.credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                                val googleIdTokenCredential =
                                    GoogleIdTokenCredential.createFrom(result.credential.data)
                                val googleTokenId = googleIdTokenCredential.idToken
                                val authCredential = GoogleAuthProvider.getCredential(googleTokenId, null)
                                val user = Firebase.auth.signInWithCredential(authCredential).await().user

                                user?.let {
                                    if (!it.isAnonymous) {
                                        saveUserToFirestore(it.uid, it.displayName, it.email, it.photoUrl.toString())
                                        login.invoke()
                                    }
                                }
                            }
                        }
                        else -> {
                            println("🚨 Bilinmeyen kimlik bilgisi tipi: ${result.credential.type}")
                        }
                    }
                } catch (e: NoCredentialException) {
                    launcher?.launch(getIntent()) // Kullanıcı Google hesabı eklememişse yönlendir
                } catch (e: GetCredentialException) {
                    e.printStackTrace()
                }
            }
        }

        private fun getIntent(): Intent {
            return Intent(Settings.ACTION_ADD_ACCOUNT).apply {
                putExtra(Settings.EXTRA_ACCOUNT_TYPES, arrayOf("com.google"))
            }
        }

        private fun getCredentialOptions(context: Context): CredentialOption {
            return GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false)
                .setAutoSelectEnabled(false)
                .setServerClientId(context.getString(R.string.web_client_id))
                .build()
        }

        /**
         * Kullanıcıyı Firestore `users` koleksiyonuna kaydeder.Neden kaydediyoruz chat özelliği için.
         * Profil resmi ve adı bizim için elzem bunları kullanıyoruz direk.
         */
        private fun saveUserToFirestore(userId: String, name: String?, email: String?, photoUrl: String?) {
            val db = FirebaseFirestore.getInstance()
            val userRef = db.collection("users").document(userId)

            userRef.get().addOnSuccessListener { document ->
                if (!document.exists()) {
                    val user = User(
                        userId = userId,
                        name = name.orEmpty(),
                        email = email.orEmpty(),
                        photoUrl = photoUrl.orEmpty(),
                        createdAt = Timestamp.now() // Kullanıcı kayıt zamanı buda önemli ekstra bunu da ekledim
                    )

                    userRef.set(user)
                        .addOnSuccessListener {
                            println("✅ Kullanıcı Firestore'a kaydedildi!")
                        }
                        .addOnFailureListener { e ->
                            println("❌ Kullanıcı kaydedilemedi: ${e.message}")
                        }
                } else {
                    println("ℹ️ Kullanıcı zaten kayıtlı, tekrar eklenmedi.")
                }
            }.addOnFailureListener { e ->
                println("🚨 Firestore'dan kullanıcı bilgisi alınamadı: ${e.message}")
            }
        }

        // Google Sign-In intent'ini döndüren fonksiyon
        fun getGoogleSignInIntent(context: Context): Intent {
            // Google Sign-In istemcisini oluştur
            val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(context.getString(R.string.web_client_id))
                .requestEmail()
                .build()

            val googleSignInClient = GoogleSignIn.getClient(context, gso)

            // Sign-in işlemi için intent'i döndür
            return googleSignInClient.signInIntent
        }
    }
}