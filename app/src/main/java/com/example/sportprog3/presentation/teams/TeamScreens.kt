package com.example.sportprog3.presentation.teams

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.sportprog3.data.remote.AcademyTeam
import com.example.sportprog3.data.remote.TeamDirectoryState
import com.example.sportprog3.data.remote.TeamPlayer
import com.example.sportprog3.data.remote.TeamRepository
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
import com.example.sportprog3.presentation.components.SecondaryAction
import com.example.sportprog3.presentation.components.SoftDivider
import com.example.sportprog3.presentation.components.toneForState
import com.example.sportprog3.ui.theme.Forest
import com.example.sportprog3.ui.theme.Mint
import com.example.sportprog3.ui.theme.Rose
import kotlinx.coroutines.launch

@Composable
fun TeamScreens(screen: Int, onNavigate: (Int) -> Unit = {}) {
    var selectedTeam by remember { mutableStateOf<AcademyTeam?>(null) }
    ScreenFrame {
        when (screen) {
            4 -> TeamsView(onOpenTeam = {
                selectedTeam = it
                onNavigate(5)
            })
            5 -> RosterView(selectedTeam, onBack = { onNavigate(4) })
            6 -> PlayerProfileView()
            7 -> DuesView()
        }
    }
}

@Composable
private fun TeamsView(onOpenTeam: (AcademyTeam) -> Unit) {
    var directory by remember { mutableStateOf(TeamDirectoryState()) }
    var showCreateDialog by remember { mutableStateOf(false) }
    var newTeamName by remember { mutableStateOf("") }
    var newTeamSeason by remember { mutableStateOf("2026") }
    var createError by remember { mutableStateOf<String?>(null) }
    var creating by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    DisposableEffect(Unit) {
        val stopObserving = TeamRepository.observeTeams { directory = it }
        onDispose { stopObserving() }
    }

    Eyebrow(directory.academyName.ifBlank { "ACADEMIA" }.uppercase())
    SectionTitle("Equipos y categorías")
    Body(
        when {
            directory.loading -> "Cargando equipos de tu academia…"
            directory.academyId.isBlank() -> "Los equipos se consultan únicamente dentro de la academia asociada a tu usuario."
            else -> "${directory.teams.size} categorías · ${directory.academyName}"
        }
    )
    directory.error?.let { Notice(it, warning = true) }

    if (directory.loading && directory.teams.isEmpty()) {
        Notice("Conectando con la base de datos…")
    } else if (directory.teams.isEmpty() && directory.error == null) {
        Notice("Todavía no hay equipos registrados en esta academia.")
    }

    directory.teams.forEach { team ->
        MockCard {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(team.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Body(
                        "${team.playerCount} jugadores" +
                            team.season.takeIf(String::isNotBlank)?.let { " · Temporada $it" }.orEmpty()
                    )
                }
                TextButton(onClick = { onOpenTeam(team) }) {
                    Text("Ver")
                }
            }
        }
    }

    if (directory.canCreate) {
        com.example.sportprog3.presentation.components.PrimaryAction(
            text = "＋ Nuevo equipo o categoría",
            enabled = directory.academyId.isNotBlank(),
            onClick = {
                createError = null
                showCreateDialog = true
            }
        )
    } else if (directory.academyId.isNotBlank()) {
        Notice("Tu rol puede consultar los equipos, pero no tiene permiso para registrar categorías.")
    }

    if (showCreateDialog) {
        AlertDialog(
            onDismissRequest = { if (!creating) showCreateDialog = false },
            title = { Text("Nuevo equipo o categoría") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = newTeamName,
                        onValueChange = { newTeamName = it },
                        label = { Text("Nombre de la categoría") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = newTeamSeason,
                        onValueChange = { newTeamSeason = it },
                        label = { Text("Temporada") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    createError?.let { Body(it, Rose) }
                }
            },
            confirmButton = {
                Button(
                    enabled = !creating && newTeamName.isNotBlank(),
                    onClick = {
                        creating = true
                        createError = null
                        scope.launch {
                            val result = TeamRepository.createTeam(newTeamName, newTeamSeason)
                            creating = false
                            if (result.isSuccess) {
                                newTeamName = ""
                                showCreateDialog = false
                            } else {
                                createError = result.exceptionOrNull()?.localizedMessage
                                    ?: "No se pudo registrar la categoría."
                            }
                        }
                    }
                ) { Text(if (creating) "Guardando…" else "Crear") }
            },
            dismissButton = {
                TextButton(enabled = !creating, onClick = { showCreateDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
private fun RosterView(team: AcademyTeam?, onBack: () -> Unit) {
    var players by remember(team) { mutableStateOf(emptyList<TeamPlayer>()) }
    var loading by remember(team) { mutableStateOf(team != null) }
    var error by remember(team) { mutableStateOf<String?>(null) }

    DisposableEffect(team) {
        if (team == null) {
            onDispose {}
        } else {
            val stopObserving = TeamRepository.observePlayers(
                team = team,
                onChanged = {
                    players = it
                    loading = false
                },
                onError = {
                    error = it
                    loading = false
                }
            )
            onDispose { stopObserving() }
        }
    }

    if (team == null) {
        Eyebrow("EQUIPO")
        SectionTitle("Plantel")
        Notice("Abre una categoría desde Equipos y categorías para consultar su plantel.", warning = true)
        SecondaryAction("Volver a equipos", onClick = onBack)
        return
    }

    Eyebrow("${team.name} · ${team.season.ifBlank { "TEMPORADA" }}".uppercase())
    SectionTitle("Plantel", "${players.size} jugadores")
    error?.let { Notice(it, warning = true) }
    if (loading) {
        Body("Cargando plantel…")
    } else if (players.isEmpty() && error == null) {
        Notice("Esta categoría todavía no tiene jugadores registrados.")
    } else {
        MockCard {
            players.forEachIndexed { index, player ->
                PlayerRow(player.number, player.name, player.detail)
                if (index < players.lastIndex) SoftDivider()
            }
        }
    }
    SecondaryAction("Volver a equipos", onClick = onBack)
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
