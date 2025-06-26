package dev.einfantesv.fitnesstracker.Screens.auth

import android.content.Context
import android.content.Intent
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore

object GoogleAuthManager {
    private val auth = FirebaseAuth.getInstance()

    // Devuelve el GoogleSignInClient configurado
    fun getSignInClient(context: Context): GoogleSignInClient {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(context.getString(dev.einfantesv.fitnesstracker.R.string.default_web_client_id))
            .requestEmail()
            .build()
        return GoogleSignIn.getClient(context, gso)
    }

    // Devuelve el intent de inicio de sesión
    fun getSignInIntent(context: Context): Intent {
        return getSignInClient(context).signInIntent
    }

    fun handleSignInResult(
        data: Intent?,
        onRegisteredUser: () -> Unit,
        onNewUser: (String, String, String) -> Unit,
        onFailure: (String) -> Unit
    ) {
        val task = GoogleSignIn.getSignedInAccountFromIntent(data)
        try {
            val account = task.getResult(ApiException::class.java)
            val credential = GoogleAuthProvider.getCredential(account.idToken, null)

            auth.signInWithCredential(credential)
                .addOnSuccessListener { authResult ->
                    val nombre = account.givenName ?: ""
                    val apellido = account.familyName ?: ""
                    val correo = account.email ?: ""

                    val uid = auth.currentUser?.uid ?: return@addOnSuccessListener

                    // Verificar si el usuario ya tiene un documento en Firestore
                    FirebaseFirestore.getInstance().collection("User")
                        .document(uid)
                        .get()
                        .addOnSuccessListener { document ->
                            if (document.exists()) {
                                onRegisteredUser()
                            } else {
                                onNewUser(nombre, apellido, correo)
                            }
                        }
                        .addOnFailureListener {
                            onFailure("Error al verificar el usuario en Firestore")
                        }
                }
                .addOnFailureListener {
                    onFailure(it.message ?: "Fallo al autenticar con Google")
                }

        } catch (e: ApiException) {
            onFailure("Error al iniciar sesión: ${e.statusCode}")
        }
    }
}
