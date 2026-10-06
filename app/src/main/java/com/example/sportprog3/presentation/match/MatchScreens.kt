package com.example.sportprog3.presentation.match

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.sportprog3.presentation.components.Badge
import com.example.sportprog3.presentation.components.Body
import com.example.sportprog3.presentation.components.Eyebrow
import com.example.sportprog3.presentation.components.EventLine
import com.example.sportprog3.presentation.components.Metric
import com.example.sportprog3.presentation.components.MockCard
import com.example.sportprog3.presentation.components.MockField
import com.example.sportprog3.presentation.components.Notice
import com.example.sportprog3.presentation.components.PlayerRow
import com.example.sportprog3.presentation.components.PrimaryAction
import com.example.sportprog3.presentation.components.ScreenFrame
import com.example.sportprog3.presentation.components.SectionTitle
import com.example.sportprog3.presentation.components.SecondaryAction
import com.example.sportprog3.presentation.components.SoftDivider
import com.example.sportprog3.ui.theme.Amber
import com.example.sportprog3.ui.theme.Forest
import com.example.sportprog3.ui.theme.ForestDeep
import com.example.sportprog3.ui.theme.Lime
import com.example.sportprog3.ui.theme.Mint
import com.example.sportprog3.ui.theme.Muted
import com.example.sportprog3.ui.theme.Pitch
import com.example.sportprog3.ui.theme.Rose

@Composable
fun MatchScreens(screen: Int) {
    ScreenFrame {
        when (screen) {
            10 -> CallupView()
            11 -> LineupView()
            12 -> LiveOperatorView()
            13 -> EventFormView()
            14 -> TimelineView()
            15 -> StatsView()
            16 -> MatchStoryView()
            21 -> EventCatalogView()
        }
    }
}

@Composable
private fun CallupView() {
    Eyebrow("FECHA 6 · LIGA DISTRITAL")
    SectionTitle("Convocatoria", "Borrador")
    MockCard {
        Text("Los Cedros  vs  Deportivo Surco", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Body("Sábado 19 sep · 16:30 · Cancha 2")
        Body("Citación 15:45 · Complejo Los Cedros")
        SoftDivider()
        Row(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
            Metric("12/16", "confirmados")
            Metric("3", "pendientes")
            Metric("1", "no disponible")
        }
    }
    SectionTitle("Jugadores convocados", "Seleccionar todos")
    MockCard {
        listOf(
            Triple("1", "Mateo Rivas", "Confirmó"),
            Triple("4", "Diego Torres", "Confirmó"),
            Triple("5", "Santiago Vargas", "Pendiente"),
            Triple("8", "Andrés Flores", "Confirmó"),
            Triple("9", "Thiago Ramos", "No disponible"),
            Triple("10", "Nicolás Paredes", "Confirmó"),
            Triple("11", "Gabriel Soto", "Pendiente")
        ).forEach { (number, name, state) ->
            PlayerRow(number, name, "Disponible para convocar", state, statusColor(state))
        }
    }
    PrimaryAction("Enviar convocatoria")
    SecondaryAction("Continuar a la alineación  →")
}

@Composable
private fun LineupView() {
    Eyebrow("LOS CEDROS VS. DEPORTIVO SURCO")
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        SectionTitle("Alineación titular")
        Badge("4—3—3")
    }
    MockCard(containerColor = Forest) {
        Column(
            modifier = Modifier.fillMaxWidth().height(300.dp),
            verticalArrangement = Arrangement.SpaceEvenly,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Badge("↑ ATAQUE", color = Lime)
            Row(horizontalArrangement = Arrangement.spacedBy(22.dp)) { FieldPlayer("11", "Ramos"); FieldPlayer("9", "Paredes"); FieldPlayer("7", "León") }
            Row(horizontalArrangement = Arrangement.spacedBy(30.dp)) { FieldPlayer("8", "Flores"); FieldPlayer("10", "Soto"); FieldPlayer("6", "Vargas") }
            Row(horizontalArrangement = Arrangement.spacedBy(20.dp)) { FieldPlayer("3", "Díaz"); FieldPlayer("4", "Torres"); FieldPlayer("5", "Reyes"); FieldPlayer("2", "Mora") }
            FieldPlayer("1", "Rivas")
        }
    }
    SectionTitle("Suplentes", "4 disponibles")
    MockCard {
        PlayerRow("12", "Luis Ponce", "Arquero")
        PlayerRow("14", "Joaquín Ruiz", "Defensa")
        PlayerRow("16", "Marco Rojas", "Mediocampista")
    }
    PrimaryAction("Guardar alineación")
}

@Composable
private fun FieldPlayer(number: String, name: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier.background(Lime, RoundedCornerShape(50)).padding(horizontal = 10.dp, vertical = 6.dp),
            contentAlignment = Alignment.Center
        ) { Text(number, color = ForestDeep, fontWeight = FontWeight.Black) }
        Text(name, color = Color.White, style = MaterialTheme.typography.labelSmall)
    }
}

@Composable
private fun LiveOperatorView() {
    MockCard(containerColor = ForestDeep) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Eyebrow("PARTIDO EN CURSO", Lime)
            Badge("● EN VIVO", color = Lime)
        }
        Text(
            "Los Cedros     2  —  1     Dep. Surco",
            color = Color.White,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Black
        )
        Text("63′", color = Lime, style = MaterialTheme.typography.displaySmall, fontWeight = FontWeight.Black)
        Body("Segundo tiempo · Operador: Carlos M.", Color.White.copy(alpha = .8f))
    }
    SectionTitle("Registrar evento rápido", "Catálogo 21")
    val actions = listOf("Gol", "Tarjeta", "Falta", "Penal", "Cambio", "Tiro de esquina", "Fuera de juego", "Saque lateral", "Saque de meta", "Inicio / fin")
    actions.chunked(2).forEach { row ->
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            row.forEach { action -> Badge(action, color = Color.White) }
        }
    }
    SectionTitle("Últimos eventos")
    MockCard {
        EventLine("63′", "Gol · Diego Torres", "Los Cedros · Asistencia A. Flores")
        SoftDivider()
        EventLine("58′", "Tarjeta amarilla", "S. Vargas · Los Cedros")
        SoftDivider()
        EventLine("54′", "Cambio", "Entra A. Flores · Sale K. Ramos")
    }
    PrimaryAction("Finalizar partido")
}

@Composable
private fun EventFormView() {
    Eyebrow("LOS CEDROS 2 — 1 DEPORTIVO SURCO")
    SectionTitle("Nuevo evento", "PARTIDO EN VIVO")
    MockCard {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Badge("GOL · SELECCIONADO")
            Badge("Tarjeta")
            Badge("Cambio")
        }
        MockField("Minuto", "63")
        MockField("Equipo", "Los Cedros")
        MockField("Jugador", "Diego Torres · #4")
        MockField("Asistencia (opcional)", "Andrés Flores · #8")
        MockField("Observaciones", "Remate dentro del área")
        Notice("Si no conoces al jugador, guarda como «Sin jugador» y complétalo después.")
        PrimaryAction("Guardar evento en cronología")
    }
}

@Composable
private fun TimelineView() {
    Eyebrow("FECHA 5 · CERRADO")
    SectionTitle("Cronología del partido", "Trazabilidad")
    Notice("Sin conexión · 2 eventos en cola para sincronizar.", warning = true)
    MockCard {
        listOf(
            Triple("90′", "Fin del partido", "Registró Carlos M. · Sincronizado"),
            Triple("76′", "Gol · Deportivo Surco", "Registró Luis A. · Editado por Carlos M."),
            Triple("63′", "Gol · Diego Torres", "Registró Carlos M. · Sincronizado"),
            Triple("58′", "Tarjeta amarilla · S. Vargas", "Registró Carlos M. · En cola"),
            Triple("54′", "Cambio · A. Flores por K. Ramos", "Registró Carlos M. · En cola"),
            Triple("45′", "Inicio del segundo tiempo", "Registró Luis A. · Sincronizado")
        ).forEach { (time, event, detail) ->
            EventLine(time, event, detail)
            SecondaryAction("Editar · Anular")
            SoftDivider()
        }
    }
    Body("Las anulaciones se conservan en el historial y se excluyen del marcador.")
}

@Composable
private fun StatsView() {
    Eyebrow("TEMPORADA 2026 · SUB-15 A")
    SectionTitle("Números que hablan")
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        MockCard(modifier = Modifier.weight(1f)) { Metric("12", "partidos") }
        MockCard(modifier = Modifier.weight(1f)) { Metric("8–2–2", "G–E–P") }
        MockCard(modifier = Modifier.weight(1f)) { Metric("27", "goles") }
    }
    MockCard(containerColor = Forest) {
        Eyebrow("RENDIMIENTO DEL EQUIPO", Lime)
        Text("Racha positiva", color = Color.White, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Body("66% de victorias · 2.25 goles por partido", Color.White.copy(alpha = .78f))
        Row(horizontalArrangement = Arrangement.spacedBy(18.dp)) {
            Metric("27", "a favor", valueColor = Color.White, labelColor = Color.White.copy(alpha = .75f))
            Metric("13", "en contra", valueColor = Color.White, labelColor = Color.White.copy(alpha = .75f))
            Metric("+14", "diferencia", valueColor = Color.White, labelColor = Color.White.copy(alpha = .75f))
        }
    }
    SectionTitle("Goleadores", "Ver equipo")
    MockCard {
        PlayerRow("9", "Thiago Ramos", "Delantero · 10 partidos", "8 goles")
        PlayerRow("10", "Nicolás Paredes", "Mediocampista · 12 partidos", "6 goles")
        PlayerRow("4", "Diego Torres", "Defensa · 11 partidos", "3 goles")
    }
    Body("Estadísticas calculadas desde los eventos vigentes de cada partido.")
}

@Composable
private fun MatchStoryView() {
    Eyebrow("FECHA 5 · LOS CEDROS 2 — 1 DEPORTIVO SURCO")
    SectionTitle("Crónica del partido", "Borrador IA")
    Notice("Fuente: 18 eventos no anulados · 0 datos personales · Generada hoy 17:42")
    MockCard {
        Badge("HECHOS DEL PARTIDO")
        Text(
            "Los Cedros venció 2-1 a Deportivo Surco. Diego Torres abrió el marcador a los 23′ y Thiago Ramos anotó el gol decisivo a los 76′. El rival descontó en el minuto 68′.",
            style = MaterialTheme.typography.bodyLarge
        )
        Badge("LECTURA DEL ENTRENADOR")
        Text(
            "El equipo sostuvo la presión tras recuperar el balón y encontró espacios por las bandas. Esta lectura es una interpretación para revisión del cuerpo técnico.",
            style = MaterialTheme.typography.bodyMedium,
            color = Muted
        )
        SoftDivider()
        Body("Revisa el texto: cada jugador, minuto y gol debe coincidir con la cronología.")
    }
    MockField("Editar crónica antes de publicar", "Texto revisado por el entrenador…")
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Badge("Ver eventos fuente")
        Badge("Registrar evaluación")
    }
    PrimaryAction("Aprobar y publicar")
}

@Composable
private fun EventCatalogView() {
    Eyebrow("CONFIGURACIÓN DEL PARTIDO")
    SectionTitle("Catálogo de eventos", "＋ Crear tipo")
    MockCard(containerColor = Forest) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Column {
                Eyebrow("VERSIÓN ACTUAL", Lime)
                Text("Catálogo v1.8", color = Color.White, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            }
            Badge("SINCRONIZADO", color = Lime)
        }
        Body("Cambios visibles al instante para todos los operadores.", Color.White.copy(alpha = .8f))
    }
    Notice("Los tipos utilizados en partidos no se eliminan; puedes desactivarlos sin perder la trazabilidad.")
    SectionTitle("Tipos de evento", "10 activos")
    MockCard {
        val eventTypes = listOf(
            "Gol" to "Minuto · equipo · jugador* · asistencia",
            "Tarjeta" to "Minuto · equipo · jugador* · color",
            "Cambio" to "Minuto · equipo · sale · entra",
            "Penal" to "Minuto · equipo · resultado · jugador",
            "Falta" to "Minuto · equipo · jugador · observación",
            "Tiro de esquina" to "Minuto · equipo · observación",
            "Fuera de juego" to "Minuto · equipo · jugador",
            "Saque lateral" to "Minuto · equipo",
            "Saque de meta" to "Minuto · equipo",
            "Inicio / fin" to "Minuto · observación"
        )
        eventTypes.forEach { (type, fields) ->
            PlayerRow("●", type, fields, "Activo")
            SoftDivider()
        }
    }
    Notice("*El jugador puede guardarse como «Sin jugador» y completarse después.")
    Body("Cada evento guarda la versión del catálogo con que fue registrado.")
}

private fun statusColor(state: String): Color = when (state) {
    "No disponible" -> Rose
    "Pendiente" -> Amber
    else -> Mint
}
