package com.example.sportprog3.presentation.match

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.sportprog3.data.model.MatchEventModel
import com.example.sportprog3.data.model.MatchLiveModel
import com.example.sportprog3.data.model.PlayerModel
import com.example.sportprog3.data.remote.FirebaseMatchManager
import com.example.sportprog3.presentation.components.Body
import com.example.sportprog3.presentation.components.Eyebrow
import com.example.sportprog3.presentation.components.EventLine
import com.example.sportprog3.presentation.components.MockCard
import com.example.sportprog3.presentation.components.Notice
import com.example.sportprog3.presentation.components.SecondaryAction
import com.example.sportprog3.presentation.components.SectionTitle
import com.example.sportprog3.presentation.components.SoftDivider
import com.example.sportprog3.ui.theme.Forest
import com.example.sportprog3.ui.theme.Rose
import kotlinx.coroutines.launch

@Composable
fun LiveEventsView() {
    val matchId = FirebaseMatchManager.DEMO_MATCH_ID
    val scope = rememberCoroutineScope()
    var live by remember { mutableStateOf<MatchLiveModel?>(null) }
    var events by remember { mutableStateOf(emptyList<MatchEventModel>()) }
    var players by remember { mutableStateOf(emptyList<PlayerModel>()) }
    var editing by remember { mutableStateOf<MatchEventModel?>(null) }
    var message by remember { mutableStateOf("") }

    DisposableEffect(matchId) {
        val stopMatch = FirebaseMatchManager.observeMatch(
            matchId = matchId,
            onLiveChanged = { live = it },
            onEventsChanged = { events = it },
            onError = { message = "Error de Firebase: $it" }
        )
        val stopPlayers = FirebaseMatchManager.observePlayers(
            matchId = matchId,
            onPlayersChanged = { players = it },
            onError = { message = "Error de Firebase: $it" }
        )
        onDispose {
            stopMatch()
            stopPlayers()
        }
    }

    val homeTeamId = live?.equipoLocalId ?: "los-cedros"
    val awayTeamId = live?.equipoVisitanteId ?: "deportivo-surco"
    val homeTeamName = live?.equipoLocalNombre ?: "Los Cedros"
    val awayTeamName = live?.equipoVisitanteNombre ?: "Deportivo Surco"
    fun teamName(teamId: String) = when (teamId) {
        homeTeamId -> homeTeamName
        awayTeamId -> awayTeamName
        else -> "Partido"
    }

    val visibleEvents = events.filterNot { it.anulado }.asReversed()
    Eyebrow("${homeTeamName.uppercase()} VS. ${awayTeamName.uppercase()}")
    SectionTitle("Eventos registrados", "${visibleEvents.size} eventos")
    Notice("Toca «Editar» para asignar jugador, cambiar minuto, equipo u observaciones. Los cambios se ven al instante en todos los dispositivos.")
    message.takeIf(String::isNotBlank)?.let { Body(it, if (it.startsWith("Error")) Rose else Forest) }
    MockCard {
        if (visibleEvents.isEmpty()) {
            Body("Aún no hay eventos. Se mostrarán aquí en cuanto se registren desde el partido en vivo.")
        }
        visibleEvents.forEachIndexed { index, event ->
            EventLine(
                "${event.minuto}′",
                event.tipo,
                buildString {
                    append(teamName(event.equipoId))
                    append(" · ")
                    append(event.jugadorNombre.ifBlank { "Sin jugador" })
                    if (event.observaciones.isNotBlank()) append(" · ${event.observaciones}")
                    if (event.editadoEn > 0) append(" · Editado")
                }
            )
            SecondaryAction("Editar", onClick = { editing = event })
            if (index < visibleEvents.lastIndex) SoftDivider()
        }
    }

    editing?.let { event ->
        EditEventDialog(
            event = event,
            players = players,
            homeTeamId = homeTeamId,
            homeTeamName = homeTeamName,
            awayTeamId = awayTeamId,
            awayTeamName = awayTeamName,
            onDismiss = { editing = null },
            onSave = { minuto, equipoId, jugador, observaciones ->
                editing = null
                message = "Guardando cambios…"
                scope.launch {
                    val result = FirebaseMatchManager.actualizarEvento(
                        matchId = matchId,
                        eventoId = event.eventoId,
                        minuto = minuto,
                        equipoId = equipoId,
                        jugadorId = jugador?.jugadorId.orEmpty(),
                        jugadorNombre = jugador?.let { "#${it.numero} ${it.nombre}" }.orEmpty(),
                        observaciones = observaciones
                    )
                    message = if (result.isSuccess) "Evento actualizado."
                    else "Error de Firebase: ${result.exceptionOrNull()?.message}"
                }
            }
        )
    }
}

@Composable
private fun EditEventDialog(
    event: MatchEventModel,
    players: List<PlayerModel>,
    homeTeamId: String,
    homeTeamName: String,
    awayTeamId: String,
    awayTeamName: String,
    onDismiss: () -> Unit,
    onSave: (minuto: Int, equipoId: String, jugador: PlayerModel?, observaciones: String) -> Unit
) {
    var minuteInput by remember(event.eventoId) { mutableStateOf(event.minuto.toString()) }
    var teamId by remember(event.eventoId) { mutableStateOf(event.equipoId.ifBlank { homeTeamId }) }
    var playerId by remember(event.eventoId) { mutableStateOf(event.jugadorId) }
    var notes by remember(event.eventoId) { mutableStateOf(event.observaciones) }

    val minute = minuteInput.toIntOrNull()
    val minuteValid = minute != null && minute in 0..130
    val teamPlayers = players.filter { it.equipoId == teamId }
    val selectedPlayer = teamPlayers.firstOrNull { it.jugadorId == playerId }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Editar · ${event.tipo}") },
        text = {
            Column(
                modifier = Modifier.heightIn(max = 460.dp).verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = minuteInput,
                    onValueChange = { minuteInput = it.filter(Char::isDigit).take(3) },
                    label = { Text("Minuto") },
                    isError = !minuteValid,
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
                Text("Equipo", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = { teamId = homeTeamId },
                        enabled = teamId != homeTeamId,
                        modifier = Modifier.weight(1f)
                    ) { Text(homeTeamName) }
                    Button(
                        onClick = { teamId = awayTeamId },
                        enabled = teamId != awayTeamId,
                        modifier = Modifier.weight(1f)
                    ) { Text(awayTeamName) }
                }
                Text("Jugador", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                PlayerOption("Sin jugador", selected = selectedPlayer == null) { playerId = "" }
                teamPlayers.forEach { player ->
                    PlayerOption("#${player.numero} · ${player.nombre}", selected = player.jugadorId == playerId) {
                        playerId = player.jugadorId
                    }
                }
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it.take(140) },
                    label = { Text("Observaciones") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                enabled = minuteValid,
                onClick = { onSave(minute ?: event.minuto, teamId, selectedPlayer, notes.trim()) }
            ) { Text("Guardar") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}

@Composable
private fun PlayerOption(label: String, selected: Boolean, onSelect: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onSelect).padding(vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(selected = selected, onClick = onSelect)
        Text(label, style = MaterialTheme.typography.bodyMedium)
    }
}
