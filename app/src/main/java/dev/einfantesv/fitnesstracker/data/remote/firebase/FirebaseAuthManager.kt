package dev.einfantesv.fitnesstracker.data.remote.firebase

import android.content.Context
import android.net.Uri
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import com.google.firebase.storage.FirebaseStorage

object FirebaseAuthManager {

    private val auth = FirebaseAuth.getInstance() //Almacena los datos necesarios para logearse
    private val firestore = FirebaseFirestore.getInstance() //Almacena todos los datos que no son sensibles (contraseña)

    suspend fun registerUser(
        name: String = "",
        lastname:String = "",
        email: String = "",
        password:String  ="",
    ) : Result<Unit> {
        return try{
            val authResult = auth.createUserWithEmailAndPassword(email, password).await()

            val uid = authResult.user?.uid?: return Result.failure(Exception("No hay usuario"))

            // Obtener la fecha actual en el formato deseado
            val fechaActual = LocalDate.now()
            val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
            val fechaFormateada = fechaActual.format(formatter)

            //Todos los campos que quiero guardar en Firebase
            val user = hashMapOf(
                "uid" to uid,
                "name" to name,
                "lastname" to lastname,
                "email" to email,
                "UserFriendCode" to generarCodigoSeguro(),
                "RegisterDate" to fechaFormateada,
                "private_account" to false,
                "profileImageUrl" to ""
            )

            firestore.collection("User")
                .document(uid)
                .set(user)
                .await()
            Result.success(Unit)


        } catch (e: Exception){
            Result.failure(e)
        }
    }

    suspend fun loginUser(email: String, password: String): Result<Unit>{
        return try{
            auth.signInWithEmailAndPassword(email, password).await()
            Result.success(Unit)
        }catch (e: Exception){
            Result.failure(e)
        }
    }

    private fun generarCodigoUnico(): String {
        val chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
        return (1..6).map { chars.random() }.joinToString("")
    }

    private suspend fun generarCodigoSeguro(): String {
        var code: String
        var exists: Boolean

        do {
            code = generarCodigoUnico()
            val result = firestore.collection("User")
                .whereEqualTo("UserFriendCode", code)
                .get()
                .await()
            exists = !result.isEmpty
        } while (exists)

        return code
    }

    fun actualizarAvatarSeleccionado(uid: String, avatarName: String, onComplete: (Boolean) -> Unit) {
        val firestore = FirebaseFirestore.getInstance()
        firestore.collection("User").document(uid)
            .update("profileImageUrl", avatarName)
            .addOnSuccessListener { onComplete(true) }
            .addOnFailureListener { onComplete(false) }
    }


    fun uploadProfileImage(context: Context, uri: Uri, onComplete: (Boolean) -> Unit) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val storageRef = FirebaseStorage.getInstance().reference.child("profile_images/$uid.jpg")

        try {
            val inputStream = context.contentResolver.openInputStream(uri)
            if (inputStream == null) {
                Log.e("UPLOAD", "InputStream es null para URI: $uri")
                onComplete(false)
                return
            }

            Log.d("UPLOAD", "Subiendo imagen desde URI: $uri")
            val uploadTask = storageRef.putStream(inputStream)

            uploadTask.continueWithTask { task ->
                if (!task.isSuccessful) {
                    Log.e("UPLOAD", "Fallo al subir: ${task.exception}")
                    task.exception?.let { throw it }
                }
                Log.d("UPLOAD", "Subida exitosa, obteniendo downloadUrl")
                storageRef.downloadUrl
            }.addOnSuccessListener { downloadUrl ->
                Log.d("UPLOAD", "URL obtenida: $downloadUrl")

                FirebaseFirestore.getInstance().collection("User")
                    .document(uid)
                    .update("profileImageUrl", downloadUrl.toString())
                    .addOnSuccessListener {
                        Log.d("UPLOAD", "Imagen actualizada en Firestore")
                        onComplete(true)
                    }
                    .addOnFailureListener {
                        Log.e("UPLOAD", "Fallo al guardar en Firestore", it)
                        onComplete(false)
                    }
            }.addOnFailureListener {
                Log.e("UPLOAD", "Fallo final en subida", it)
                onComplete(false)
            }

        } catch (e: Exception) {
            Log.e("UPLOAD", "Excepción en el try", e)
            onComplete(false)
        }
    }


}