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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.sportprog3.data.model.MatchEventModel
import com.example.sportprog3.data.model.MatchLiveModel
import com.example.sportprog3.data.model.PlayerModel
import com.example.sportprog3.data.remote.FirebaseMatchManager
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
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun MatchScreens(screen: Int, onNavigate: (Int) -> Unit = {}) {
    ScreenFrame {
        when (screen) {
            10 -> CallupView()
            11 -> LineupView()
            12 -> LiveOperatorView(onNavigate)
            13 -> EventFormView()
            14 -> LiveEventsView()
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
private fun LiveOperatorView(onNavigate: (Int) -> Unit) {
    val matchId = FirebaseMatchManager.DEMO_MATCH_ID
    val scope = rememberCoroutineScope()
    var live by remember { mutableStateOf<MatchLiveModel?>(null) }
    var events by remember { mutableStateOf(emptyList<MatchEventModel>()) }
    var selectedTeamId by remember { mutableStateOf("los-cedros") }
    var minuteInput by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("Conectando con Firebase Realtime Database…") }
    var saving by remember { mutableStateOf(false) }
    var now by remember { mutableLongStateOf(0L) }

    DisposableEffect(matchId) {
        val stopObserving = FirebaseMatchManager.observeMatch(
            matchId = matchId,
            onLiveChanged = { live = it },
            onEventsChanged = {
                events = it
                message = ""
            },
            onError = { message = "Error de Firebase: $it" }
        )
        onDispose { stopObserving() }
    }

    val homeTeamId = live?.equipoLocalId ?: "los-cedros"
    val awayTeamId = live?.equipoVisitanteId ?: "deportivo-surco"
    val homeTeamName = live?.equipoLocalNombre ?: "Los Cedros"
    val awayTeamName = live?.equipoVisitanteNombre ?: "Deportivo Surco"
    val goals = events.filter { it.tipo == "Gol" && !it.anulado }
    val homeScore = goals.count { it.equipoId == homeTeamId }
    val awayScore = goals.count { it.equipoId == awayTeamId }
    val isLive = live?.estado == "EN_VIVO"
    val phase = live?.fase ?: "SIN_INICIAR"
    val clockRunning = isLive && (phase == "PRIMER_TIEMPO" || phase == "SEGUNDO_TIEMPO")

    LaunchedEffect(clockRunning, live?.relojInicioEn) {
        while (clockRunning) {
            now = FirebaseMatchManager.serverNow()
            delay(250)
        }
    }
    fun elapsedMs(at: Long): Long {
        val current = live ?: return 0L
        return current.relojBaseMs + if (clockRunning) (at - current.relojInicioEn).coerceAtLeast(0L) else 0L
    }
    val elapsed = elapsedMs(now)
    val clockMinute = (elapsed / 60_000).toInt()
    val clockText = "%02d:%02d".format(clockMinute, (elapsed / 1000 % 60).toInt())

    val typedMinute = minuteInput.toIntOrNull()
    val minuteValid = minuteInput.isBlank() || (typedMinute != null && typedMinute in 0..130)
    val effectiveMinute = if (minuteInput.isBlank()) clockMinute else typedMinute ?: clockMinute
    val canRegister = clockRunning && minuteValid
    val canStartHalf = isLive && !saving && (phase == "SIN_INICIAR" || phase == "ENTRETIEMPO")

    fun changePhase(newPhase: String, period: Int, baseMs: Long, type: String, doneMessage: String, finish: Boolean = false) {
        saving = true
        message = "Sincronizando…"
        scope.launch {
            val result = FirebaseMatchManager.cambiarFase(matchId, newPhase, period, baseMs, type, finish)
            saving = false
            message = if (result.isSuccess) doneMessage
            else "Error de Firebase: ${result.exceptionOrNull()?.message}"
        }
    }

    MockCard(containerColor = ForestDeep) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Eyebrow(
                when {
                    !isLive -> "ESTADO DEL PARTIDO"
                    phase == "SIN_INICIAR" -> "PARTIDO POR INICIAR"
                    phase == "ENTRETIEMPO" -> "ENTRETIEMPO"
                    else -> "PARTIDO EN CURSO"
                },
                Lime
            )
            Badge(if (isLive) "● EN VIVO" else live?.estado?.replace("_", " ") ?: "CARGANDO", color = Lime)
        }
        Text(
            "$homeTeamName  $homeScore — $awayScore  $awayTeamName",
            color = Color.White,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Black
        )
        Text(clockText, color = Lime, style = MaterialTheme.typography.displayMedium, fontWeight = FontWeight.Black)
        Body(
            when {
                !isLive -> "Partido finalizado"
                phase == "SIN_INICIAR" -> "Pulsa «Iniciar partido» para arrancar el reloj"
                phase == "PRIMER_TIEMPO" -> "1.er tiempo · reloj en marcha"
                phase == "ENTRETIEMPO" -> "Reloj detenido · listo para el 2.º tiempo"
                else -> "2.º tiempo · reloj en marcha"
            },
            Color.White.copy(alpha = .8f)
        )
    }
    SectionTitle("Registrar evento rápido", "Firebase RTDB")
    MockCard {
        OutlinedTextField(
            value = minuteInput,
            onValueChange = { input -> minuteInput = input.filter(Char::isDigit).take(3) },
            label = { Text("Minuto del partido") },
            supportingText = {
                Text(
                    if (minuteInput.isBlank()) "Vacío: se usa el minuto del reloj (${clockMinute}′)"
                    else if (minuteValid) "Se usará el minuto ${effectiveMinute}′"
                    else "Ingresa un minuto entre 0 y 130"
                )
            },
            isError = !minuteValid,
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(
                onClick = { selectedTeamId = homeTeamId },
                enabled = selectedTeamId != homeTeamId,
                modifier = Modifier.weight(1f)
            ) { Text(homeTeamName) }
            Button(
                onClick = { selectedTeamId = awayTeamId },
                enabled = selectedTeamId != awayTeamId,
                modifier = Modifier.weight(1f)
            ) { Text(awayTeamName) }
        }
    }
    PrimaryAction(
        text = when (phase) {
            "ENTRETIEMPO" -> "Iniciar 2PT"
            "SEGUNDO_TIEMPO" -> "2PT en juego"
            "PRIMER_TIEMPO" -> "Partido iniciado"
            else -> "Iniciar partido"
        },
        enabled = canStartHalf,
        onClick = {
            if (phase == "ENTRETIEMPO") {
                changePhase(
                    "SEGUNDO_TIEMPO", 2, FirebaseMatchManager.MINUTOS_POR_TIEMPO * 60_000L,
                    "Inicio 2PT", "Segundo tiempo iniciado."
                )
            } else {
                changePhase("PRIMER_TIEMPO", 1, 0L, "Inicio", "Partido iniciado.")
            }
        }
    )
    PrimaryAction(
        text = "Finalizar 1PT",
        enabled = isLive && !saving && phase == "PRIMER_TIEMPO",
        onClick = {
            changePhase(
                "ENTRETIEMPO", 1, elapsedMs(FirebaseMatchManager.serverNow()),
                "Fin 1PT", "Primer tiempo finalizado."
            )
        }
    )
    val actions = listOf(
        "Gol", "Tarjeta", "Falta", "Penal", "Cambio",
        "Tiro de esquina", "Fuera de juego", "Saque lateral", "Saque de meta"
    )
    actions.chunked(2).forEach { row ->
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            row.forEach { label ->
                Button(
                    onClick = {
                        val minute = if (minuteInput.isBlank()) {
                            (elapsedMs(FirebaseMatchManager.serverNow()) / 60_000).toInt()
                        } else effectiveMinute
                        message = "$label agregado; sincronizando…"
                        scope.launch {
                            val result = FirebaseMatchManager.registerEvent(
                                matchId = matchId,
                                tipo = label,
                                minuto = minute,
                                periodo = live?.periodo ?: 1,
                                equipoId = selectedTeamId
                            )
                            message = if (result.isSuccess) "$label registrado en el minuto $minute′."
                            else "Error de Firebase: ${result.exceptionOrNull()?.message}"
                        }
                    },
                    enabled = canRegister,
                    modifier = Modifier.weight(1f)
                ) { Text(label) }
            }
        }
    }
    message.takeIf(String::isNotBlank)?.let { Body(it, if (it.startsWith("Error")) Rose else Muted) }
    SectionTitle("Cronología en vivo")
    MockCard {
        val activeEvents = events.filterNot { it.anulado }.asReversed().take(8)
        if (activeEvents.isEmpty()) {
            Body("Los eventos nuevos aparecerán aquí para todos los dispositivos conectados.")
        } else {
            activeEvents.forEachIndexed { index, event ->
                val teamName = when (event.equipoId) {
                    homeTeamId -> homeTeamName
                    awayTeamId -> awayTeamName
                    else -> "Partido"
                }
                EventLine(
                    "${event.minuto}′",
                    event.tipo,
                    "$teamName · ${event.jugadorNombre.ifBlank { "Sin jugador" }}"
                )
                if (index < activeEvents.lastIndex) SoftDivider()
            }
        }
    }
    SecondaryAction("Ver y editar todos los eventos  →", onClick = { onNavigate(14) })
    PrimaryAction(
        text = if (isLive) "Finalizar partido" else "Partido finalizado",
        enabled = isLive && !saving && phase == "SEGUNDO_TIEMPO",
        onClick = {
            changePhase(
                "FINALIZADO", live?.periodo ?: 2, elapsedMs(FirebaseMatchManager.serverNow()),
                "Fin del partido", "Partido finalizado.", finish = true
            )
        }
    )
}

@Composable
private fun EventFormView() {
    val matchId = FirebaseMatchManager.DEMO_MATCH_ID
    val eventTypes = listOf(
        "Gol", "Tarjeta", "Falta", "Penal", "Cambio",
        "Tiro de esquina", "Fuera de juego", "Saque lateral", "Saque de meta"
    )
    val scope = rememberCoroutineScope()
    var live by remember { mutableStateOf<MatchLiveModel?>(null) }
    var events by remember { mutableStateOf(emptyList<MatchEventModel>()) }
    var players by remember { mutableStateOf(emptyList<PlayerModel>()) }
    var selectedType by remember { mutableStateOf("") }
    var minuteInput by remember { mutableStateOf("") }
    var selectedTeamId by remember { mutableStateOf("") }
    var selectedPlayerId by remember { mutableStateOf("") }
    var selectedAssistId by remember { mutableStateOf("") }
    var observations by remember { mutableStateOf("") }
    var saving by remember { mutableStateOf(false) }
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

    val homeTeamId = live?.equipoLocalId.orEmpty()
    val awayTeamId = live?.equipoVisitanteId.orEmpty()
    val homeTeamName = live?.equipoLocalNombre.orEmpty()
    val awayTeamName = live?.equipoVisitanteNombre.orEmpty()
    val teamPlayers = players.filter { it.equipoId == selectedTeamId }
    val selectedPlayer = teamPlayers.firstOrNull { it.jugadorId == selectedPlayerId }
    val teamAssists = teamPlayers.filter { it.jugadorId != selectedPlayerId }
    val selectedAssist = teamAssists.firstOrNull { it.jugadorId == selectedAssistId }
    val minute = minuteInput.toIntOrNull()
    val minuteValid = minute != null && minute in 0..130
    val authenticated = FirebaseAuth.getInstance().currentUser?.uid?.isNotBlank() == true
    val teamsLoaded = homeTeamId.isNotBlank() && awayTeamId.isNotBlank() && homeTeamId != awayTeamId
    val canSave = !saving && authenticated && live != null && teamsLoaded &&
        selectedType in eventTypes && selectedTeamId in listOf(homeTeamId, awayTeamId) && minuteValid

    Eyebrow(
        if (teamsLoaded) "${homeTeamName.uppercase()} VS. ${awayTeamName.uppercase()}"
        else "PARTIDO NO DISPONIBLE"
    )
    SectionTitle("Nuevo evento", "PARTIDO EN VIVO")
    MockCard {
        EventFormDropdown(
            label = "Tipo de evento",
            value = selectedType.ifBlank { "Selecciona un tipo" },
            options = eventTypes.map { it to it },
            onSelected = { selectedType = it }
        )
        OutlinedTextField(
            value = minuteInput,
            onValueChange = { minuteInput = it.filter(Char::isDigit).take(3) },
            label = { Text("Minuto") },
            supportingText = {
                Text(if (minuteValid) "Minuto $minute′" else "Ingresa un minuto entre 0 y 130")
            },
            isError = minuteInput.isNotBlank() && !minuteValid,
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )
        Text("Equipo", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(
                onClick = {
                    selectedTeamId = homeTeamId
                    selectedPlayerId = ""
                    selectedAssistId = ""
                },
                enabled = teamsLoaded && selectedTeamId != homeTeamId,
                modifier = Modifier.weight(1f)
            ) { Text(homeTeamName.ifBlank { "Equipo local" }) }
            Button(
                onClick = {
                    selectedTeamId = awayTeamId
                    selectedPlayerId = ""
                    selectedAssistId = ""
                },
                enabled = teamsLoaded && selectedTeamId != awayTeamId,
                modifier = Modifier.weight(1f)
            ) { Text(awayTeamName.ifBlank { "Equipo visitante" }) }
        }
        EventFormDropdown(
            label = "Jugador (opcional)",
            value = selectedPlayer?.let { "#${it.numero} ${it.nombre}" } ?: "Sin jugador / Desconocido",
            options = listOf("Sin jugador / Desconocido" to "") +
                teamPlayers.map { "#${it.numero} ${it.nombre}" to it.jugadorId },
            onSelected = {
                selectedPlayerId = it
                if (it == selectedAssistId) selectedAssistId = ""
            },
            enabled = selectedTeamId.isNotBlank()
        )
        if (selectedType == "Gol") {
            EventFormDropdown(
                label = "Asistencia (opcional)",
                value = selectedAssist?.let { "#${it.numero} ${it.nombre}" } ?: "Sin asistencia",
                options = listOf("Sin asistencia" to "") +
                    teamAssists.map { "#${it.numero} ${it.nombre}" to it.jugadorId },
                onSelected = { selectedAssistId = it },
                enabled = selectedTeamId.isNotBlank()
            )
        }
        OutlinedTextField(
            value = observations,
            onValueChange = { observations = it },
            label = { Text("Observaciones (opcional)") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 2,
            maxLines = 4
        )
        Notice("Si no conoces al jugador, guarda como «Sin jugador» y complétalo después.")
        if (!authenticated) {
            Body("Inicia sesión con una cuenta autorizada para registrar eventos.", Rose)
        }
        if (!teamsLoaded) {
            Body(
                "No se pudieron cargar los equipos del partido. El guardado estará disponible cuando Firebase proporcione sus datos.",
                Rose
            )
        }
        message.takeIf(String::isNotBlank)?.let {
            Body(it, if (it.startsWith("Error")) Rose else Forest)
        }
        Button(
            onClick = {
                if (!canSave || saving) return@Button
                saving = true
                message = "Guardando evento…"
                scope.launch {
                    val result = FirebaseMatchManager.registerEvent(
                        matchId = matchId,
                        tipo = selectedType,
                        minuto = minute,
                        periodo = live?.periodo ?: 1,
                        equipoId = selectedTeamId,
                        jugadorId = selectedPlayer?.jugadorId.orEmpty(),
                        jugadorNombre = selectedPlayer?.let { "#${it.numero} ${it.nombre}" }.orEmpty(),
                        asistenciaId = selectedAssist?.jugadorId.orEmpty(),
                        asistenciaNombre = selectedAssist?.let { "#${it.numero} ${it.nombre}" }.orEmpty(),
                        observaciones = observations.trim()
                    )
                    saving = false
                    if (result.isSuccess) {
                        message = "Evento registrado en la cronología."
                        selectedType = ""
                        minuteInput = ""
                        selectedTeamId = ""
                        selectedPlayerId = ""
                        selectedAssistId = ""
                        observations = ""
                    } else {
                        message = "Error de Firebase: ${result.exceptionOrNull()?.message ?: "No se pudo guardar el evento."}"
                    }
                }
            },
            enabled = canSave,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(if (saving) "Guardando…" else "Guardar evento en cronología")
        }
    }
    SectionTitle("Cronología del partido", "${events.count { !it.anulado }} eventos")
    MockCard {
        val activeEvents = events.filterNot { it.anulado }.asReversed()
        if (activeEvents.isEmpty()) {
            Body("Los eventos del partido aparecerán aquí al sincronizarse con Firebase.")
        }
        activeEvents.take(10).forEachIndexed { index, event ->
            val teamName = when (event.equipoId) {
                homeTeamId -> homeTeamName
                awayTeamId -> awayTeamName
                else -> "Equipo desconocido"
            }
            val detail = buildString {
                append("$teamName · ${event.jugadorNombre.ifBlank { "Sin jugador" }}")
                if (event.asistenciaNombre.isNotBlank()) append(" · Asistencia: ${event.asistenciaNombre}")
                if (event.observaciones.isNotBlank()) append(" · ${event.observaciones}")
            }
            EventLine("${event.minuto}′ · ${event.periodo}T", event.tipo, detail)
            if (index < activeEvents.take(10).lastIndex) SoftDivider()
        }
    }
}

@Composable
private fun EventFormDropdown(
    label: String,
    value: String,
    options: List<Pair<String, String>>,
    onSelected: (String) -> Unit,
    enabled: Boolean = true
) {
    var expanded by remember(label) { mutableStateOf(false) }
    Box(modifier = Modifier.fillMaxWidth()) {
        OutlinedButton(
            onClick = { expanded = true },
            enabled = enabled,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("$label: $value")
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.fillMaxWidth(.9f)
        ) {
            options.forEach { (optionLabel, optionValue) ->
                DropdownMenuItem(
                    text = { Text(optionLabel) },
                    onClick = {
                        onSelected(optionValue)
                        expanded = false
                    }
                )
            }
        }
    }
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
