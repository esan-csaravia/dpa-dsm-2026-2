package com.example.sportprog3.presentation.dashboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.clickable
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.sportprog3.presentation.components.Body
import com.example.sportprog3.presentation.components.Eyebrow
import com.example.sportprog3.presentation.components.MockCard
import com.example.sportprog3.presentation.components.Metric
import com.example.sportprog3.presentation.components.PrimaryAction
import com.example.sportprog3.presentation.components.ScreenFrame
import com.example.sportprog3.presentation.components.SectionTitle
import com.example.sportprog3.presentation.components.Badge
import com.example.sportprog3.presentation.components.PlayerRow
import com.example.sportprog3.ui.theme.Forest
import com.example.sportprog3.ui.theme.Lime

@Composable
fun DashboardScreen(onNavigate: (Int) -> Unit) {
    ScreenFrame {
        Eyebrow("Los Cedros FC  ·  Temporada 2026")
        Text("¡Buen día, Carlos!", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Black)
        Body("Sub-15 · Entrenador principal")

        MockCard(containerColor = Forest) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Eyebrow("PRÓXIMO PARTIDO", Lime)
                    Text(
                        "Los Cedros  vs  Dep. Surco",
                        color = androidx.compose.ui.graphics.Color.White,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Body("Sáb. 19 sep · 16:30 · Cancha 2", androidx.compose.ui.graphics.Color.White.copy(alpha = 0.78f))
                }
                Badge("EN 2 DÍAS", color = Lime)
            }
            PrimaryAction("Abrir convocatoria") { onNavigate(10) }
        }

        SectionTitle("Tu equipo esta semana", "Ver equipo")
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            MockCard(modifier = Modifier.weight(1f)) {
                Metric("22", "jugadores")
                Body("Sub-15 A")
            }
            MockCard(modifier = Modifier.weight(1f)) {
                Metric("87%", "asistencia")
                Body("Últimas 4 sesiones")
            }
        }

        SectionTitle("Accesos rápidos")
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            QuickAction("Equipos", "04", Modifier.weight(1f)) { onNavigate(4) }
            QuickAction("Entrenamiento", "08", Modifier.weight(1f)) { onNavigate(8) }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            QuickAction("Convocatoria", "10", Modifier.weight(1f)) { onNavigate(10) }
            QuickAction("Comunidad", "17", Modifier.weight(1f)) { onNavigate(17) }
        }

        SectionTitle("Pendiente para ti", "3 tareas")
        MockCard {
            PlayerRow("AI", "Aprobar crónica de la fecha 5", "Generada con los eventos del partido", "Revisar")
            PlayerRow("18", "Registrar asistencia", "Entrenamiento · Hoy 17:00", "Hoy")
            PlayerRow("$", "Mensualidades por revisar", "3 registros pendientes · Septiembre", "Abrir")
        }
        Text(
            "Cada detalle cuenta. Vamos equipo.",
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
            color = Forest,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun QuickAction(label: String, screen: String, modifier: Modifier = Modifier, onClick: () -> Unit) {
    MockCard(modifier = modifier.clickable { onClick() }) {
        Eyebrow("VISTA $screen")
        Text(label, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Forest)
        Text("Explorar →", color = Forest, style = MaterialTheme.typography.labelMedium)
    }
}
