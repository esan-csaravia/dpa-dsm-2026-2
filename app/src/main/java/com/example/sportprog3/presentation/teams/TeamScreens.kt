package com.example.sportprog3.presentation.teams

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
import com.example.sportprog3.presentation.components.Avatar
import com.example.sportprog3.presentation.components.Badge
import com.example.sportprog3.presentation.components.Body
import com.example.sportprog3.presentation.components.Eyebrow
import com.example.sportprog3.presentation.components.MockCard
import com.example.sportprog3.presentation.components.MockField
import com.example.sportprog3.presentation.components.Metric
import com.example.sportprog3.presentation.components.Notice
import com.example.sportprog3.presentation.components.PlayerRow
import com.example.sportprog3.presentation.components.PrimaryAction
import com.example.sportprog3.presentation.components.ScreenFrame
import com.example.sportprog3.presentation.components.SectionTitle
import com.example.sportprog3.presentation.components.SoftDivider
import com.example.sportprog3.presentation.components.toneForState
import com.example.sportprog3.ui.theme.Forest
import com.example.sportprog3.ui.theme.Mint
import com.example.sportprog3.ui.theme.Rose

@Composable
fun TeamScreens(screen: Int) {
    ScreenFrame {
        when (screen) {
            4 -> TeamsView()
            5 -> RosterView()
            6 -> PlayerProfileView()
            7 -> DuesView()
        }
    }
}

@Composable
private fun TeamsView() {
    Eyebrow("ACADEMIA LOS CEDROS")
    SectionTitle("Equipos y categorías", "＋ Nuevo")
    Body("6 categorías activas · temporada 2026")
    listOf("Sub-10" to "22", "Sub-13" to "20", "Sub-15 A" to "18", "Sub-15 B" to "16", "Sub-17" to "19", "Primera" to "24").forEach { (name, total) ->
        MockCard {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text(name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Body("$total jugadores · Los Cedros FC")
                }
                Badge("Ver plantel  →")
            }
        }
    }
}

@Composable
private fun RosterView() {
    Eyebrow("EQUIPO · TEMPORADA 2026")
    SectionTitle("Sub-15 A", "＋ Jugador")
    Body("18 jugadores · entrenador Carlos Medina")
    MockField("Buscar jugador", "Nombre o dorsal")
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Badge("Todos · 18")
        Badge("Arqueros · 2")
        Badge("Defensas · 6")
    }
    MockCard {
        listOf(
            Triple("1", "Mateo Rivas", "Arquero · 15 años"),
            Triple("4", "Diego Torres", "Defensa · 14 años"),
            Triple("5", "Santiago Vargas", "Defensa · 15 años"),
            Triple("8", "Andrés Flores", "Mediocampista · 15 años"),
            Triple("10", "Nicolás Paredes", "Mediocampista · 14 años"),
            Triple("9", "Thiago Ramos", "Delantero · 15 años")
        ).forEach { PlayerRow(it.first, it.second, it.third, "Ver") }
    }
}

@Composable
private fun PlayerProfileView() {
    MockCard(containerColor = Forest) {
        Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            Avatar("DT", background = androidx.compose.ui.graphics.Color.White)
            Column {
                Eyebrow("SUB-15 A · LOS CEDROS", androidx.compose.ui.graphics.Color.White.copy(alpha = .75f))
                Text("Diego Torres", color = androidx.compose.ui.graphics.Color.White, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Body("Defensa central  ·  #4", androidx.compose.ui.graphics.Color.White.copy(alpha = .8f))
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(26.dp)) {
            Metric("92%", "asistencia")
            Metric("5", "partidos")
            Metric("2", "goles")
        }
    }
    SectionTitle("Ficha deportiva", "Editar")
    MockCard {
        PlayerRow("↗", "Datos físicos", "1.68 m · 58 kg · Pie derecho")
        SoftDivider()
        PlayerRow("◎", "Fecha de nacimiento", "12 de febrero de 2011")
        SoftDivider()
        PlayerRow("☎", "Contacto", "Visible solo para el equipo autorizado")
        SoftDivider()
        PlayerRow("SOS", "Emergencia", "María Torres · Madre · ••• ••• 482")
    }
    Notice("La información sensible solo es visible para el entrenador, el jugador y su apoderado.")
    SectionTitle("Participación reciente")
    MockCard {
        PlayerRow("12", "Entrenamiento", "Presente · 15 sep", "Presente")
        PlayerRow("09", "Entrenamiento", "Presente · 12 sep", "Presente")
        PlayerRow("05", "Fecha 5 vs. San Borja", "1 gol · 1 asistencia")
    }
}

@Composable
private fun DuesView() {
    Eyebrow("ADMINISTRACIÓN · SEPTIEMBRE 2026")
    SectionTitle("Mensualidades", "Sub-15 A")
    Notice("Vista demostrativa: no procesa pagos ni guarda datos de tarjetas.", warning = true)
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        MockCard(modifier = Modifier.weight(1f)) { Metric("12", "al día") }
        MockCard(modifier = Modifier.weight(1f)) { Metric("3", "pendientes") }
        MockCard(modifier = Modifier.weight(1f)) { Metric("3", "vencidas") }
    }
    MockCard {
        listOf(
            Triple("4", "Diego Torres", "Al día · 05 sep"),
            Triple("8", "Andrés Flores", "Pendiente · vence 20 sep"),
            Triple("9", "Thiago Ramos", "Vencida · 10 sep"),
            Triple("10", "Nicolás Paredes", "Al día · 03 sep"),
            Triple("11", "Gabriel Soto", "Pendiente · vence 20 sep"),
            Triple("15", "Bruno León", "Vencida · 10 sep")
        ).forEach { (num, name, detail) ->
            val status = detail.substringBefore(" ·")
            PlayerRow(num, name, detail, status, toneForState(status))
        }
    }
    Body("Los estados son registros administrativos simulados.")
}
