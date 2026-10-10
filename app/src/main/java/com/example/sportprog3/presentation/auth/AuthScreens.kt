package com.example.sportprog3.presentation.auth

import android.util.Patterns
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.example.sportprog3.presentation.components.Avatar
import com.example.sportprog3.presentation.components.Badge
import com.example.sportprog3.presentation.components.Body
import com.example.sportprog3.presentation.components.MockCard
import com.example.sportprog3.presentation.components.Notice
import com.example.sportprog3.presentation.components.PrimaryAction
import com.example.sportprog3.presentation.components.ScreenFrame
import com.example.sportprog3.presentation.components.SecondaryAction
import com.example.sportprog3.ui.theme.Forest
import com.example.sportprog3.ui.theme.Ink
import com.example.sportprog3.ui.theme.Line
import com.example.sportprog3.ui.theme.Muted
import com.example.sportprog3.ui.theme.Pitch
import com.example.sportprog3.ui.theme.Rose
import kotlinx.coroutines.launch

@Composable
fun AuthScreens(
    screen: Int,
    onNavigate: (Int) -> Unit,
    onLoginSuccess: (role: String, name: String) -> Unit = { _, _ -> }
) {
    val coroutineScope = rememberCoroutineScope()

    // Registration state
    var selectedRole by remember { mutableStateOf<String?>(null) }
    var nombre by remember { mutableStateOf("") }
    var apellidos by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    var roleError by remember { mutableStateOf<String?>(null) }
    var nombreError by remember { mutableStateOf<String?>(null) }
    var apellidosError by remember { mutableStateOf<String?>(null) }
    var emailError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }

    var isRegisterLoading by remember { mutableStateOf(false) }
    var duplicateEmailError by remember { mutableStateOf<String?>(null) }
    var registerGeneralError by remember { mutableStateOf<String?>(null) }
    var showPasswordDialog by remember { mutableStateOf(false) }

    // Login state
    var loginEmail by remember { mutableStateOf("") }
    var loginPassword by remember { mutableStateOf("") }
    var loginEmailError by remember { mutableStateOf<String?>(null) }
    var loginPasswordError by remember { mutableStateOf<String?>(null) }
    var isLoginLoading by remember { mutableStateOf(false) }
    var loginGeneralError by remember { mutableStateOf<String?>(null) }

    var registrationSuccessMessage by remember { mutableStateOf<String?>(null) }

    ScreenFrame {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            Avatar("SP", modifier = Modifier.padding(top = 8.dp))
            Text(
                "SportPro G3",
                style = MaterialTheme.typography.headlineMedium,
                color = Forest,
                fontWeight = FontWeight.Black
            )
            Body("Tu equipo. Tu juego. Un solo lugar.")

            if (screen == 1 && registrationSuccessMessage != null) {
                Notice(registrationSuccessMessage!!, warning = false)
            }

            MockCard {
                if (screen == 1) {
                    Text(
                        "Iniciar sesión",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Ink
                    )

                    AuthTextField(
                        value = loginEmail,
                        onValueChange = {
                            loginEmail = it
                            loginEmailError = null
                            loginGeneralError = null
                        },
                        label = "Correo electrónico",
                        placeholder = "ejemplo@correo.com",
                        keyboardType = KeyboardType.Email,
                        errorMessage = loginEmailError
                    )

                    AuthTextField(
                        value = loginPassword,
                        onValueChange = {
                            loginPassword = it
                            loginPasswordError = null
                            loginGeneralError = null
                        },
                        label = "Contraseña",
                        placeholder = "••••••••",
                        isPassword = true,
                        errorMessage = loginPasswordError
                    )

                    loginGeneralError?.let {
                        Notice(it, warning = true)
                    }

                    PrimaryAction(
                        text = if (isLoginLoading) "Iniciando sesión..." else "Ingresar",
                        enabled = !isLoginLoading,
                        onClick = {
                            if (isLoginLoading) return@PrimaryAction
                            var valid = true
                            if (loginEmail.isBlank()) {
                                loginEmailError = "El correo es obligatorio."
                                valid = false
                            }
                            if (loginPassword.isBlank()) {
                                loginPasswordError = "La contraseña es obligatoria."
                                valid = false
                            }

                            if (valid) {
                                isLoginLoading = true
                                loginGeneralError = null
                                coroutineScope.launch {
                                    when (val result = AuthRepository.loginUser(loginEmail, loginPassword)) {
                                        is LoginResult.Success -> {
                                            isLoginLoading = false
                                            onLoginSuccess(result.role, result.name)
                                        }
                                        is LoginResult.Error -> {
                                            isLoginLoading = false
                                            loginGeneralError = result.message
                                        }
                                    }
                                }
                            }
                        }
                    )

                    SecondaryAction("¿Olvidaste tu contraseña?")

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Body("¿Primera vez en SportPro?")
                        SecondaryAction("Crear cuenta", onClick = {
                            registrationSuccessMessage = null
                            loginEmail = ""
                            loginPassword = ""
                            onNavigate(2)
                        })
                    }
                    Badge("ACCESO SEGURO POR ROL")

                } else {
                    Text(
                        "Crear cuenta",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Ink
                    )

                    RoleSelector(
                        selectedRole = selectedRole,
                        onRoleSelected = {
                            selectedRole = it
                            roleError = null
                            duplicateEmailError = null
                            registerGeneralError = null
                        },
                        errorMessage = roleError
                    )

                    AuthTextField(
                        value = nombre,
                        onValueChange = {
                            nombre = it
                            nombreError = null
                            duplicateEmailError = null
                            registerGeneralError = null
                        },
                        label = "Nombre",
                        placeholder = "Tu nombre",
                        errorMessage = nombreError
                    )

                    AuthTextField(
                        value = apellidos,
                        onValueChange = {
                            apellidos = it
                            apellidosError = null
                            duplicateEmailError = null
                            registerGeneralError = null
                        },
                        label = "Apellidos",
                        placeholder = "Tus apellidos",
                        errorMessage = apellidosError
                    )

                    AuthTextField(
                        value = email,
                        onValueChange = {
                            email = it
                            emailError = null
                            duplicateEmailError = null
                            registerGeneralError = null
                        },
                        label = "Correo electrónico",
                        placeholder = "ejemplo@correo.com",
                        keyboardType = KeyboardType.Email,
                        errorMessage = emailError
                    )

                    AuthTextField(
                        value = password,
                        onValueChange = {
                            password = it
                            passwordError = null
                            duplicateEmailError = null
                            registerGeneralError = null
                        },
                        label = "Contraseña",
                        placeholder = "Mínimo 8 caracteres",
                        isPassword = true,
                        errorMessage = passwordError
                    )

                    Notice(
                        "Los menores de 14 años deben registrarse mediante la cuenta de su padre o apoderado.",
                        warning = true
                    )

                    duplicateEmailError?.let {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Notice(it, warning = true)
                            SecondaryAction("Ir a Iniciar sesión", onClick = {
                                loginEmail = ""
                                loginPassword = ""
                                onNavigate(1)
                            })
                        }
                    }

                    registerGeneralError?.let {
                        Notice(it, warning = true)
                    }

                    PrimaryAction(
                        text = if (isRegisterLoading) "Creando cuenta..." else "Crear cuenta",
                        enabled = !isRegisterLoading,
                        onClick = {
                            if (isRegisterLoading) return@PrimaryAction
                            var valid = true

                            if (selectedRole == null) {
                                roleError = "Debes seleccionar un rol."
                                valid = false
                            }
                            if (nombre.isBlank()) {
                                nombreError = "El nombre es obligatorio."
                                valid = false
                            }
                            if (apellidos.isBlank()) {
                                apellidosError = "Los apellidos son obligatorios."
                                valid = false
                            }
                            if (email.isBlank()) {
                                emailError = "El correo electrónico es obligatorio."
                                valid = false
                            } else if (!Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches()) {
                                emailError = "Ingresa un correo electrónico válido."
                                valid = false
                            }

                            val isPasswordValid = password.length >= 8 &&
                                    password.any { it.isUpperCase() } &&
                                    password.any { it.isLowerCase() } &&
                                    password.any { it.isDigit() }

                            if (!isPasswordValid) {
                                passwordError = "La contraseña debe tener al menos 8 caracteres e incluir una mayúscula, una minúscula y un número."
                                showPasswordDialog = true
                                valid = false
                            }

                            if (valid) {
                                isRegisterLoading = true
                                duplicateEmailError = null
                                registerGeneralError = null

                                coroutineScope.launch {
                                    when (val result = AuthRepository.registerUser(
                                        nombre = nombre,
                                        apellidos = apellidos,
                                        email = email,
                                        password = password,
                                        role = selectedRole!!
                                    )) {
                                        is RegisterResult.Success -> {
                                            isRegisterLoading = false
                                            loginEmail = ""
                                            loginPassword = ""
                                            loginEmailError = null
                                            loginPasswordError = null
                                            loginGeneralError = null
                                            registrationSuccessMessage = "Cuenta creada correctamente. Inicia sesión con tus credenciales."
                                            onNavigate(1)
                                        }
                                        is RegisterResult.DuplicateEmail -> {
                                            isRegisterLoading = false
                                            duplicateEmailError = result.message
                                        }
                                        is RegisterResult.Error -> {
                                            isRegisterLoading = false
                                            registerGeneralError = result.message
                                        }
                                    }
                                }
                            }
                        }
                    )

                    SecondaryAction(
                        "¿Ya tienes cuenta? Inicia sesión",
                        onClick = {
                            loginEmail = ""
                            loginPassword = ""
                            onNavigate(1)
                        }
                    )
                }
            }
            Body(
                if (screen == 1) "Continuar implica aceptar nuestros términos y política de privacidad."
                else "Tus datos se usan solo para administrar tu experiencia deportiva."
            )
        }
    }

    if (showPasswordDialog) {
        AlertDialog(
            onDismissRequest = { showPasswordDialog = false },
            title = {
                Text(
                    "Contraseña no válida",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium
                )
            },
            text = {
                Text(
                    "La contraseña debe tener al menos 8 caracteres e incluir una mayúscula, una minúscula y un número.",
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                TextButton(onClick = { showPasswordDialog = false }) {
                    Text("Entendido", color = Forest, fontWeight = FontWeight.Bold)
                }
            }
        )
    }
}

@Composable
fun RoleSelector(
    selectedRole: String?,
    onRoleSelected: (String) -> Unit,
    errorMessage: String? = null
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            "Selecciona tu rol",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = Ink
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            RoleChip(
                role = "Entrenador",
                isSelected = selectedRole == "Entrenador",
                onClick = { onRoleSelected("Entrenador") },
                modifier = Modifier.weight(1f)
            )
            RoleChip(
                role = "Jugador",
                isSelected = selectedRole == "Jugador",
                onClick = { onRoleSelected("Jugador") },
                modifier = Modifier.weight(1f)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            RoleChip(
                role = "Padre de familia",
                isSelected = selectedRole == "Padre de familia",
                onClick = { onRoleSelected("Padre de familia") },
                modifier = Modifier.weight(1f)
            )
            RoleChip(
                role = "Administrador",
                isSelected = selectedRole == "Administrador",
                onClick = { onRoleSelected("Administrador") },
                modifier = Modifier.weight(1f)
            )
        }

        if (errorMessage != null) {
            Text(
                text = errorMessage,
                color = Rose,
                style = MaterialTheme.typography.labelSmall,
                modifier = Modifier.padding(start = 4.dp)
            )
        }
    }
}

@Composable
private fun RoleChip(
    role: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        modifier = modifier.height(48.dp),
        shape = RoundedCornerShape(14.dp),
        color = if (isSelected) Forest else Color.White,
        border = BorderStroke(
            width = if (isSelected) 2.dp else 1.dp,
            color = if (isSelected) Forest else Line
        )
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.padding(horizontal = 4.dp)
        ) {
            Text(
                text = role,
                color = if (isSelected) Color.White else Ink,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Composable
fun AuthTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String = "",
    isPassword: Boolean = false,
    keyboardType: KeyboardType = KeyboardType.Text,
    errorMessage: String? = null,
    modifier: Modifier = Modifier
) {
    var passwordVisible by remember { mutableStateOf(false) }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = { Text(label) },
            placeholder = { if (placeholder.isNotEmpty()) Text(placeholder) },
            singleLine = true,
            isError = errorMessage != null,
            visualTransformation = if (isPassword && !passwordVisible) PasswordVisualTransformation() else VisualTransformation.None,
            keyboardOptions = KeyboardOptions(
                keyboardType = if (isPassword) KeyboardType.Password else keyboardType
            ),
            trailingIcon = if (isPassword) {
                {
                    TextButton(onClick = { passwordVisible = !passwordVisible }) {
                        Text(
                            text = if (passwordVisible) "Ocultar" else "Ver",
                            color = Forest,
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                }
            } else null,
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Pitch,
                unfocusedBorderColor = Line,
                focusedLabelColor = Pitch,
                unfocusedLabelColor = Muted,
                errorBorderColor = Rose,
                errorLabelColor = Rose
            )
        )

        if (errorMessage != null) {
            Text(
                text = errorMessage,
                color = Rose,
                style = MaterialTheme.typography.labelSmall,
                modifier = Modifier.padding(start = 4.dp)
            )
        }
    }
}
