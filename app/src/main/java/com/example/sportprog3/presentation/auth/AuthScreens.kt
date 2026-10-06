package com.example.sportprog3.presentation.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.sportprog3.presentation.components.Avatar
import com.example.sportprog3.presentation.components.Badge
import com.example.sportprog3.presentation.components.Body
import com.example.sportprog3.presentation.components.MockField
import com.example.sportprog3.presentation.components.MockCard
import com.example.sportprog3.presentation.components.Notice
import com.example.sportprog3.presentation.components.PrimaryAction
import com.example.sportprog3.presentation.components.ScreenFrame
import com.example.sportprog3.presentation.components.SecondaryAction
import com.example.sportprog3.ui.theme.Forest

@Composable
fun AuthScreens(screen: Int, onNavigate: (Int) -> Unit) {
    ScreenFrame {
        Column(
            modifier = Modifier.fillMaxSize().padding(top = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            Avatar("SP", modifier = Modifier.padding(top = 8.dp))
            Text("SportPro G3", style = MaterialTheme.typography.headlineMedium, color = Forest, fontWeight = FontWeight.Black)
            Body("Tu equipo. Tu juego. Un solo lugar.")

            MockCard {
                if (screen == 1) {
                    MockField("Correo electrónico", "carlos.medina@loscedros.pe")
                    MockField("Contraseña", "••••••••")
                    PrimaryAction("Ingresar")
                    SecondaryAction("¿Olvidaste tu contraseña?")
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Body("¿Primera vez en SportPro?")
                        SecondaryAction("Crear cuenta", onClick = { onNavigate(2) })
                    }
                    Badge("ACCESO SEGURO POR ROL")
                } else {
                    MockField("Nombres y apellidos", "Carlos Medina")
                    MockField("Correo electrónico", "carlos.medina@email.com")
                    MockField("Contraseña", "Mínimo 8 caracteres")
                    Text("¿Cómo participarás?", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Badge("Entrenador")
                        Badge("Jugador")
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Badge("Padre de familia")
                        Badge("Administrador")
                    }
                    Notice("Los menores de 14 años deben registrarse desde la cuenta de su padre o apoderado.", warning = true)
                    PrimaryAction("Crear mi cuenta")
                    SecondaryAction("Ya tengo cuenta · Iniciar sesión", onClick = { onNavigate(1) })
                }
            }
            Body(if (screen == 1) "Continuar implica aceptar nuestros términos y política de privacidad." else "Tus datos se usan solo para administrar tu experiencia deportiva.")
        }
    }
}
