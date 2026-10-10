package com.example.sportprog3.presentation.dashboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
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
import com.example.sportprog3.presentation.components.Eyebrow
import com.example.sportprog3.presentation.components.MockCard
import com.example.sportprog3.presentation.components.ScreenFrame
import com.example.sportprog3.presentation.components.SecondaryAction
import com.example.sportprog3.ui.theme.Forest
import com.example.sportprog3.ui.theme.Mint

@Composable
fun PlayerHomeScreen(
    userName: String = "Jugador",
    onLogout: () -> Unit = {}
) {
    ScreenFrame {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Avatar(initials = "JG")
            Eyebrow("SPORTPRO G3")
            Text(
                "¡Bienvenido, $userName!",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Black,
                color = Forest
            )
            Badge("ROL: JUGADOR", color = Mint, textColor = Forest)

            MockCard {
                Eyebrow("ESTADO DE MI CUENTA")
                Text(
                    "Perfil activo · Estado: Aprobado",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Body("Tu ficha deportiva y estadísticas del jugador se activarán en el próximo módulo.")
            }

            MockCard {
                Eyebrow("PRÓXIMAS ACTIVIDADES")
                Body("No tienes convocatorias pendientes por el momento.")
            }

            SecondaryAction("Cerrar sesión", onClick = onLogout)
        }
    }
}

@Composable
fun ParentHomeScreen(
    userName: String = "Padre de familia",
    onLogout: () -> Unit = {}
) {
    ScreenFrame {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Avatar(initials = "PF")
            Eyebrow("SPORTPRO G3")
            Text(
                "¡Bienvenido, $userName!",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Black,
                color = Forest
            )
            Badge("ROL: PADRE DE FAMILIA", color = Mint, textColor = Forest)

            MockCard {
                Eyebrow("PANEL FAMILIAR")
                Text(
                    "Supervisión de deportistas · Estado: Aprobado",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Body("Desde aquí podrás vincular a tus hijos, revisar asistencias y gestionar pagos.")
            }

            MockCard {
                Eyebrow("HIJOS VINCULADOS")
                Body("Sin deportistas vinculados actualmente. Comunícate con tu academia para la vinculación.")
            }

            SecondaryAction("Cerrar sesión", onClick = onLogout)
        }
    }
}

@Composable
fun AdminHomeScreen(
    userName: String = "Administrador",
    onLogout: () -> Unit = {}
) {
    ScreenFrame {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Avatar(initials = "AD")
            Eyebrow("SPORTPRO G3")
            Text(
                "¡Bienvenido, $userName!",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Black,
                color = Forest
            )
            Badge("ROL: ADMINISTRADOR", color = Mint, textColor = Forest)

            MockCard {
                Eyebrow("PANEL DE ADMINISTRACIÓN")
                Text(
                    "Gestión de Academia · Estado: Pendiente de aprobación",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Body("Tu solicitud de rol de administrador está en revisión por la directiva de la academia.")
            }

            MockCard {
                Eyebrow("MÓDULOS DE GESTIÓN")
                Body("La asignación de academia y permisos avanzados estará disponible tras la aprobación de tu rol.")
            }

            SecondaryAction("Cerrar sesión", onClick = onLogout)
        }
    }
}
