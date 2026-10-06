package com.example.sportprog3.presentation.training

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.sportprog3.presentation.components.Badge
import com.example.sportprog3.presentation.components.Body
import com.example.sportprog3.presentation.components.Eyebrow
import com.example.sportprog3.presentation.components.Metric
import com.example.sportprog3.presentation.components.MockCard
import com.example.sportprog3.presentation.components.Notice
import com.example.sportprog3.presentation.components.PlayerRow
import com.example.sportprog3.presentation.components.PrimaryAction
import com.example.sportprog3.presentation.components.ScreenFrame
import com.example.sportprog3.presentation.components.SectionTitle
import com.example.sportprog3.presentation.components.SoftDivider
import com.example.sportprog3.presentation.components.toneForState
import com.example.sportprog3.ui.theme.Forest
import com.example.sportprog3.ui.theme.Mint

@Composable
fun TrainingScreens(screen: Int) {
    ScreenFrame {
        if (screen == 8) TrainingPlan() else Attendance()
    }
}

@Composable
private fun TrainingPlan() {
    Eyebrow("SÁBADO · 19 SEPTIEMBRE")
    SectionTitle("Sesión de entrenamiento", "Borrador")
    MockCard(containerColor = Forest) {
        Text("Sub-15 A  ·  Cancha principal", color = androidx.compose.ui.graphics.Color.White, fontWeight = FontWeight.Bold)
        Body("17:00–18:30  ·  Duración 90 min", androidx.compose.ui.graphics.Color.White.copy(alpha = .8f))
        Badge("RESISTENCIA + DEFINICIÓN", color = Mint)
    }
    SectionTitle("Objetivo de la sesión")
    MockCard {
        Text("Mejorar transición rápida y definición en el último tercio.", style = MaterialTheme.typography.bodyLarge)
    }
    SectionTitle("Bloques de trabajo", "＋ Biblioteca")
    MockCard {
        PlayerRow("15'", "Activación dinámica", "Movilidad y rondos de posesión", "Calentamiento")
        SoftDivider()
        PlayerRow("25'", "Rondo de transición", "6 vs 3 · presión tras pérdida", "Técnica")
        SoftDivider()
        PlayerRow("35'", "Ataque 4 vs 4 + porteros", "Finalización en zona ofensiva", "Táctica")
        SoftDivider()
        PlayerRow("15'", "Vuelta a la calma", "Estiramiento y feedback grupal", "Cierre")
    }
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        MockCard(modifier = Modifier.weight(1f)) { Metric("4", "ejercicios") }
        MockCard(modifier = Modifier.weight(1f)) { Metric("90'", "duración") }
    }
    PrimaryAction("Guardar sesión")
    Badge("TOMAR ASISTENCIA  →")
}

@Composable
private fun Attendance() {
    Eyebrow("HOY · 17:00 · SUB-15 A")
    SectionTitle("Control de asistencia", "Editar")
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        MockCard(modifier = Modifier.weight(1f)) { Metric("14", "presentes") }
        MockCard(modifier = Modifier.weight(1f)) { Metric("2", "tarde") }
        MockCard(modifier = Modifier.weight(1f)) { Metric("2", "ausentes") }
    }
    Notice("Marca un estado por jugador. Puedes corregirlo antes de guardar.")
    MockCard {
        listOf(
            Triple("1", "Mateo Rivas", "Presente"),
            Triple("4", "Diego Torres", "Presente"),
            Triple("8", "Andrés Flores", "Tarde"),
            Triple("9", "Thiago Ramos", "Ausente"),
            Triple("10", "Nicolás Paredes", "Presente"),
            Triple("11", "Gabriel Soto", "Tarde"),
            Triple("15", "Bruno León", "Ausente")
        ).forEach { (number, name, state) ->
            PlayerRow(number, name, "Sub-15 A", state, toneForState(state))
        }
    }
    Body("Último cambio local · hace un momento")
    PrimaryAction("Guardar asistencia")
}
