package com.example.sportprog3.presentation.auth

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

sealed class RegisterResult {
    object Success : RegisterResult()
    data class DuplicateEmail(val message: String) : RegisterResult()
    data class Error(val message: String) : RegisterResult()
}

sealed class LoginResult {
    data class Success(val uid: String, val role: String, val name: String) : LoginResult()
    data class Error(val message: String) : LoginResult()
}

object AuthRepository {
    private val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
    private val firestore: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }

    suspend fun registerUser(
        nombre: String,
        apellidos: String,
        email: String,
        password: String,
        role: String
    ): RegisterResult {
        return try {
            val authResult = auth.createUserWithEmailAndPassword(email.trim(), password).await()
            val uid = authResult.user?.uid ?: throw Exception("No se pudo obtener el identificador de usuario.")

            val estadoRol = when (role) {
                "Jugador", "Padre de familia" -> "aprobado"
                "Entrenador", "Administrador" -> "pendiente"
                else -> "pendiente"
            }

            val userMap = hashMapOf(
                "uid" to uid,
                "nombre" to nombre.trim(),
                "apellidos" to apellidos.trim(),
                "email" to email.trim(),
                "rol" to role,
                "estadoRol" to estadoRol,
                "academiaId" to ""
            )

            firestore.collection("usuarios").document(uid).set(userMap).await()

            auth.signOut()

            RegisterResult.Success
        } catch (e: FirebaseAuthUserCollisionException) {
            RegisterResult.DuplicateEmail("Ya existe una cuenta registrada con este correo. Puedes iniciar sesión.")
        } catch (e: FirebaseAuthWeakPasswordException) {
            RegisterResult.Error("La contraseña es muy débil. Intenta con una contraseña más segura.")
        } catch (e: FirebaseAuthInvalidCredentialsException) {
            RegisterResult.Error("El formato del correo o credenciales no es válido.")
        } catch (e: Exception) {
            val msg = e.localizedMessage ?: ""
            if (msg.contains("email-already-in-use", ignoreCase = true) || msg.contains("already in use", ignoreCase = true)) {
                RegisterResult.DuplicateEmail("Ya existe una cuenta registrada con este correo. Puedes iniciar sesión.")
            } else {
                RegisterResult.Error("Error al registrar: ${e.localizedMessage ?: "Ocurrió un error inesperado."}")
            }
        }
    }

    suspend fun loginUser(
        email: String,
        password: String
    ): LoginResult {
        return try {
            val authResult = auth.signInWithEmailAndPassword(email.trim(), password).await()
            val uid = authResult.user?.uid ?: throw Exception("No se pudo obtener el identificador de usuario.")

            val docSnapshot = firestore.collection("usuarios").document(uid).get().await()

            if (docSnapshot.exists()) {
                val role = docSnapshot.getString("rol") ?: "Jugador"
                val nombre = docSnapshot.getString("nombre") ?: ""
                val apellidos = docSnapshot.getString("apellidos") ?: ""
                val fullName = if (nombre.isNotEmpty()) "$nombre $apellidos".trim() else "Usuario"

                LoginResult.Success(uid = uid, role = role, name = fullName)
            } else {
                LoginResult.Success(uid = uid, role = "Jugador", name = "Usuario")
            }
        } catch (e: Exception) {
            val msg = e.localizedMessage ?: ""
            val userFriendlyMsg = when {
                msg.contains("user-not-found", ignoreCase = true) || msg.contains("no user record", ignoreCase = true) ->
                    "No existe una cuenta registrada con este correo."
                msg.contains("wrong-password", ignoreCase = true) || msg.contains("invalid-credential", ignoreCase = true) ->
                    "Correo o contraseña incorrectos."
                else -> "Error de inicio de sesión: ${e.localizedMessage ?: "Credenciales inválidas."}"
            }
            LoginResult.Error(userFriendlyMsg)
        }
    }

    fun logout() {
        try {
            auth.signOut()
        } catch (_: Exception) {}
    }
}
