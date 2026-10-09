package com.example.sportprog3.presentation.auth

import android.util.Patterns
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.example.sportprog3.presentation.components.Avatar
import com.example.sportprog3.presentation.components.Body
import com.example.sportprog3.presentation.components.MockCard
import com.example.sportprog3.presentation.components.ScreenFrame
import com.example.sportprog3.ui.theme.Forest
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Locale

@Composable
fun AuthScreens(
    screen: Int,
    onNavigate: (Int) -> Unit,
    onLoginSuccess: (String) -> Unit
) {
    val auth = remember { FirebaseAuth.getInstance() }
    val firestore = remember { FirebaseFirestore.getInstance() }

    var correo by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var cargando by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var mensaje by remember { mutableStateOf<String?>(null) }

    val pantallaActiva = remember { mutableStateOf(true) }

    DisposableEffect(Unit) {
        pantallaActiva.value = true

        onDispose {
            pantallaActiva.value = false
        }
    }

    fun correoValido(): Boolean =
        Patterns.EMAIL_ADDRESS.matcher(correo.trim()).matches()

    fun rechazarPerfil(texto: String) {
        auth.signOut()
        cargando = false
        error = texto
    }

    fun ingresar() {
        if (cargando) return

        error = null
        mensaje = null

        if (correo.isBlank() || password.isEmpty()) {
            error = "Completa el correo y la contraseña."
            return
        }

        if (!correoValido()) {
            error = "Escribe un correo electrónico válido."
            return
        }

        cargando = true

        auth.signInWithEmailAndPassword(correo.trim(), password)
            .addOnSuccessListener login@{ resultado ->
                if (!pantallaActiva.value) return@login

                val uid = resultado.user?.uid

                if (uid == null) {
                    rechazarPerfil("No se pudo obtener el usuario.")
                    return@login
                }

                firestore.collection("usuarios")
                    .document(uid)
                    .get()
                    .addOnSuccessListener perfil@{ documento ->
                        if (!pantallaActiva.value) return@perfil

                        if (!documento.exists()) {
                            rechazarPerfil(
                                "Tu cuenta no tiene un perfil en usuarios."
                            )
                            return@perfil
                        }

                        val academiaId =
                            documento.getString("academiaId")

                        if (academiaId.isNullOrBlank()) {
                            rechazarPerfil(
                                "Tu perfil no tiene una academia asignada."
                            )
                            return@perfil
                        }

                        val rol = documento.getString("rol")
                            ?.trim()
                            ?.lowercase(Locale.ROOT)

                        if (rol.isNullOrBlank()) {
                            rechazarPerfil(
                                "Tu perfil no tiene un rol asignado."
                            )
                            return@perfil
                        }

                        val rolesPermitidos = setOf(
                            "entrenador",
                            "jugador",
                            "padre de familia",
                            "administrador"
                        )

                        if (rol !in rolesPermitidos) {
                            rechazarPerfil(
                                "El rol de tu perfil no es válido."
                            )
                            return@perfil
                        }

                        password = ""
                        cargando = false

                        onLoginSuccess(rol)
                    }
                    .addOnFailureListener {
                        if (pantallaActiva.value) {
                            rechazarPerfil(
                                "No se pudo leer tu perfil. " +
                                        "Revisa tu conexión y los permisos de Firestore."
                            )
                        }
                    }
            }
            .addOnFailureListener { excepcion ->
                if (pantallaActiva.value) {
                    cargando = false

                    error = when (excepcion) {
                        is FirebaseNetworkException ->
                            "Sin conexión. Revisa tu acceso a Internet."

                        is FirebaseAuthInvalidCredentialsException,
                        is FirebaseAuthInvalidUserException ->
                            "Correo o contraseña incorrectos, " +
                                    "o cuenta no disponible."

                        else ->
                            "No se pudo iniciar sesión. Inténtalo nuevamente."
                    }
                }
            }
    }

    fun recuperarPassword() {
        if (cargando) return

        error = null
        mensaje = null

        if (!correoValido()) {
            error = "Primero escribe un correo electrónico válido."
            return
        }

        cargando = true

        auth.sendPasswordResetEmail(correo.trim())
            .addOnSuccessListener {
                if (pantallaActiva.value) {
                    cargando = false

                    mensaje = "Si el correo tiene una cuenta, recibirás " +
                            "instrucciones para recuperar tu contraseña."
                }
            }
            .addOnFailureListener {
                if (pantallaActiva.value) {
                    cargando = false

                    error = "No se pudo solicitar la recuperación. " +
                            "Inténtalo nuevamente."
                }
            }
    }

    ScreenFrame {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            Avatar(
                "SP",
                modifier = Modifier.padding(top = 8.dp)
            )

            Text(
                text = "SportPro G3",
                style = MaterialTheme.typography.headlineMedium,
                color = Forest,
                fontWeight = FontWeight.Black
            )

            Body("Tu equipo. Tu juego. Un solo lugar.")

            MockCard {
                if (screen == 1) {
                    OutlinedTextField(
                        value = correo,
                        onValueChange = {
                            correo = it
                            error = null
                            mensaje = null
                        },
                        label = { Text("Correo electrónico") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        enabled = !cargando,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Email
                        )
                    )

                    OutlinedTextField(
                        value = password,
                        onValueChange = {
                            password = it
                            error = null
                            mensaje = null
                        },
                        label = { Text("Contraseña") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        enabled = !cargando,
                        visualTransformation =
                            PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password
                        )
                    )

                    error?.let {
                        Text(
                            text = it,
                            color = MaterialTheme.colorScheme.error
                        )
                    }

                    mensaje?.let {
                        Text(
                            text = it,
                            color = Forest
                        )
                    }

                    Button(
                        onClick = { ingresar() },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !cargando
                    ) {
                        Text(
                            if (cargando) "Procesando…" else "Ingresar"
                        )
                    }

                    if (cargando) {
                        CircularProgressIndicator()
                    }

                    TextButton(
                        onClick = { recuperarPassword() },
                        enabled = !cargando
                    ) {
                        Text("¿Olvidaste tu contraseña?")
                    }

                    TextButton(
                        onClick = {
                            password = ""
                            error = null
                            mensaje = null
                            onNavigate(2)
                        },
                        enabled = !cargando
                    ) {
                        Text("¿No tienes cuenta? Regístrate")
                    }
                } else {
                    Text(
                        text = "Crear cuenta",
                        style = MaterialTheme.typography.titleLarge
                    )

                    Body(
                        "Por ahora, utiliza la cuenta creada en Firebase."
                    )

                    TextButton(
                        onClick = {
                            error = null
                            mensaje = null
                            onNavigate(1)
                        },
                        enabled = !cargando
                    ) {
                        Text("Volver a iniciar sesión")
                    }
                }
            }
        }
    }
}