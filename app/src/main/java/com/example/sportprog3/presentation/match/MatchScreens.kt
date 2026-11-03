package com.example.sportprog3.presentation.match

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.key
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.sportprog3.data.model.MatchEventModel
import com.example.sportprog3.data.model.MatchLiveModel
import com.example.sportprog3.data.model.PlayerModel
import com.example.sportprog3.data.remote.FirebaseMatchManager
import com.example.sportprog3.data.remote.MatchContext
import com.example.sportprog3.data.remote.MatchSelection
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
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun MatchScreens(screen: Int, onNavigate: (Int) -> Unit = {}) {
    val context by MatchSelection.current.collectAsState()
    key(context) {
      ScreenFrame {
        when (screen) {
            10 -> CallupView()
            11 -> LineupView()
            12 -> LiveOperatorView(context, onNavigate)
            13 -> EventFormView(context, onNavigate)
            14 -> LiveEventsView(context)
            15 -> StatsView()
            16 -> MatchSummaryView()
            21 -> EventCatalogView()
        }
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
private fun LiveOperatorView(context: MatchContext, onNavigate: (Int) -> Unit) {
    val matchId = context.partidoId
    val scope = rememberCoroutineScope()
    var live by remember { mutableStateOf<MatchLiveModel?>(null) }
    var events by remember { mutableStateOf(emptyList<MatchEventModel>()) }
    var selectedTeamId by remember { mutableStateOf("los-cedros") }
    var minuteInput by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("Conectando con Cloud Firestore…") }
    var saving by remember { mutableStateOf(false) }
    var now by remember { mutableLongStateOf(0L) }
    var confirmRestart by remember { mutableStateOf(false) }

    DisposableEffect(matchId) {
        val stopObserving = FirebaseMatchManager.observeMatch(
            matchId = matchId,
            onLiveChanged = {
                live = it
                if (it != null && (message == "Conectando con Cloud Firestore…" || message.startsWith("Error de carga:"))) message = ""
            },
            onEventsChanged = { events = it },
            onError = { message = "Error de carga: $it" },
            academiaId = context.academiaId,
            equipoContextoId = context.equipoId
        )
        onDispose { stopObserving() }
    }

    val homeTeamId = live?.equipoLocalId ?: "los-cedros"
    val awayTeamId = live?.equipoVisitanteId ?: "deportivo-surco"
    val homeTeamName = live?.equipoLocalNombre ?: "Los Cedros"
    val awayTeamName = live?.equipoVisitanteNombre ?: "Deportivo Surco"
    LaunchedEffect(live?.equipoLocalId, live?.equipoVisitanteId) {
        if (selectedTeamId !in listOf(homeTeamId, awayTeamId)) selectedTeamId = homeTeamId
    }
    val currentEvents = events.filter { it.ejecucionId == live?.ejecucionId }
    val goals = currentEvents.filter { it.tipo == "Gol" && !it.anulado }
    val homeScore = goals.count { it.equipoId == homeTeamId }
    val awayScore = goals.count { it.equipoId == awayTeamId }
    val isLive = live?.estado in listOf("EN_VIVO", "PROGRAMADO", "PRIMER_TIEMPO", "ENTRETIEMPO", "SEGUNDO_TIEMPO")
    val phase = live?.fase ?: "SIN_INICIAR"
    val clockRunning = isLive && (phase == "PRIMER_TIEMPO" || phase == "SEGUNDO_TIEMPO")

    LaunchedEffect(clockRunning, live?.relojInicioEn) {
        while (clockRunning) {
            now = FirebaseMatchManager.deviceNow()
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
    val canRegister = clockRunning && minuteValid && !saving
    val canStartHalf = isLive && !saving && (phase == "SIN_INICIAR" || phase == "ENTRETIEMPO")

    fun changePhase(
        newPhase: String, period: Int, baseMs: Long, type: String, doneMessage: String,
        action: String, finish: Boolean = false, hint: String = ""
    ) {
        saving = true
        message = "Sincronizando…"
        scope.launch {
            val result = FirebaseMatchManager.cambiarFase(
                matchId, newPhase, period, baseMs, type, finish,
                context.academiaId, context.equipoId, live?.ejecucionId ?: "inicial"
            )
            saving = false
            message = if (result.isSuccess) doneMessage
            else matchActionError(action, result.exceptionOrNull(), hint)
        }
    }

    SecondaryAction("Seleccionar / programar partido", onClick = { onNavigate(36) })
    Notice(COACH_ONLY_NOTICE)
    message.takeIf(String::isNotBlank)?.let { Body(it, if (isMatchError(it)) Rose else Muted) }
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
                live == null -> "Cargando partido. Revisa la conexion y los permisos."
                live?.estado == "CANCELADO" -> "Partido cancelado"
                !isLive -> "Partido finalizado"
                phase == "SIN_INICIAR" -> "Pulsa «Iniciar partido» para arrancar el reloj"
                phase == "PRIMER_TIEMPO" -> "1.er tiempo · reloj en marcha"
                phase == "ENTRETIEMPO" -> "Reloj detenido · listo para el 2.º tiempo"
                else -> "2.º tiempo · reloj en marcha"
            },
            Color.White.copy(alpha = .8f)
        )
    }
    SectionTitle("Registrar evento rápido", "Cloud Firestore")
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
                    "Inicio 2PT", "Segundo tiempo iniciado.", "iniciar el segundo tiempo"
                )
            } else {
                changePhase(
                    "PRIMER_TIEMPO", 1, 0L, "Inicio", "Partido iniciado.", "iniciar el partido",
                    hint = "Si ya eres el Entrenador asignado, confirma antes la alineación inicial en US-036."
                )
            }
        }
    )
    message.takeIf { isMatchError(it) }?.let { Body(it, Rose) }
    PrimaryAction(
        text = "Finalizar 1PT",
        enabled = isLive && !saving && phase == "PRIMER_TIEMPO",
        onClick = {
            changePhase(
                "ENTRETIEMPO", 1, elapsedMs(FirebaseMatchManager.deviceNow()),
                "Fin 1PT", "Primer tiempo finalizado.", "finalizar el primer tiempo"
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
                            (elapsedMs(FirebaseMatchManager.deviceNow()) / 60_000).toInt()
                        } else effectiveMinute
                        saving = true
                        message = "Registrando $label…"
                        scope.launch {
                            val result = FirebaseMatchManager.registerEvent(
                                matchId = matchId,
                                tipo = label,
                                minuto = minute,
                                periodo = live?.periodo ?: 1,
                                equipoId = selectedTeamId,
                                academiaId = context.academiaId,
                                equipoContextoId = context.equipoId,
                                ejecucionId = live?.ejecucionId ?: "inicial"
                            )
                            saving = false
                            message = if (result.isSuccess) "$label registrado en el minuto $minute′."
                            else matchActionError("registrar eventos", result.exceptionOrNull())
                        }
                    },
                    enabled = canRegister,
                    modifier = Modifier.weight(1f)
                ) { Text(label) }
            }
        }
    }
    SectionTitle("Cronología en vivo")
    MockCard {
        val activeEvents = currentEvents.filterNot { it.anulado }.asReversed().take(8)
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
    SecondaryAction("Registrar evento con detalle", onClick = { onNavigate(13) })
    SecondaryAction("Ver y editar todos los eventos  →", onClick = { onNavigate(14) })
    PrimaryAction(
        text = if (isLive) "Finalizar partido" else "Partido finalizado",
        enabled = isLive && !saving && phase == "SEGUNDO_TIEMPO",
        onClick = {
            changePhase(
                "FINALIZADO", live?.periodo ?: 2, elapsedMs(FirebaseMatchManager.deviceNow()),
                "Fin del partido", "Partido finalizado.", "finalizar el partido", finish = true
            )
        }
    )
    PrimaryAction(
        text = "Reiniciar partido",
        enabled = live != null && live?.estado != "CANCELADO" && !saving,
        onClick = { confirmRestart = true }
    )
    if (confirmRestart) {
        AlertDialog(
            onDismissRequest = { confirmRestart = false },
            title = { Text("Reiniciar partido") },
            text = { Text("Se conservaran la ejecucion y todos sus eventos en el historial. La nueva ejecucion comenzara con marcador 0-0 y reloj 00:00. No se borran jugadores ni alineacion.") },
            confirmButton = {
                TextButton(onClick = {
                    confirmRestart = false
                    saving = true
                    scope.launch {
                        val result = FirebaseMatchManager.reiniciarPartido(
                            matchId, live?.ejecucionId ?: "inicial", context.academiaId, context.equipoId
                        )
                        saving = false
                        minuteInput = ""
                        message = if (result.isSuccess) "Partido reiniciado. Pulsa Iniciar partido."
                        else matchActionError("reiniciar el partido", result.exceptionOrNull())
                    }
                }) { Text("Conservar historial y reiniciar") }
            },
            dismissButton = { TextButton(onClick = { confirmRestart = false }) { Text("Cancelar") } }
        )
    }
    message.takeIf { isMatchError(it) }?.let { Body(it, Rose) }
}

@Composable
private fun EventFormView(context: MatchContext, onNavigate: (Int) -> Unit) {
    val matchId = context.partidoId
    val scope = rememberCoroutineScope()
    var live by remember { mutableStateOf<MatchLiveModel?>(null) }
    var players by remember { mutableStateOf(emptyList<PlayerModel>()) }
    var selectedTeamId by remember { mutableStateOf("los-cedros") }
    var selectedPlayerId by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf("Gol") }
    var minuteInput by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("Conectando con Cloud Firestore…") }
    var saving by remember { mutableStateOf(false) }
    var now by remember { mutableLongStateOf(0L) }

    DisposableEffect(matchId) {
        val stopMatch = FirebaseMatchManager.observeMatch(
            matchId = matchId,
            onLiveChanged = {
                live = it
                if (it != null && (message == "Conectando con Cloud Firestore…" || message.startsWith("Error de carga:"))) message = ""
            },
            onEventsChanged = {},
            onError = { message = "Error de carga: $it" },
            academiaId = context.academiaId,
            equipoContextoId = context.equipoId
        )
        val stopPlayers = FirebaseMatchManager.observePlayers(
            matchId = matchId,
            onPlayersChanged = { players = it },
            onError = { message = "Error de plantel: $it" },
            academiaId = context.academiaId,
            equipoContextoId = context.equipoId
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
    val isLive = live?.estado in listOf("EN_VIVO", "PRIMER_TIEMPO", "ENTRETIEMPO", "SEGUNDO_TIEMPO", "PROGRAMADO")
    val phase = live?.fase ?: "SIN_INICIAR"
    val clockRunning = isLive && (phase == "PRIMER_TIEMPO" || phase == "SEGUNDO_TIEMPO")
    val selectedPlayer = players.firstOrNull {
        it.jugadorId == selectedPlayerId && it.equipoId == selectedTeamId
    }
    val teamPlayers = players.filter { it.equipoId == selectedTeamId }

    LaunchedEffect(live?.equipoLocalId, live?.equipoVisitanteId) {
        if (selectedTeamId !in listOf(homeTeamId, awayTeamId)) {
            selectedTeamId = homeTeamId
            selectedPlayerId = ""
        }
    }
    LaunchedEffect(clockRunning, live?.relojInicioEn) {
        while (clockRunning) {
            now = FirebaseMatchManager.deviceNow()
            delay(250)
        }
    }

    val elapsed = (live?.relojBaseMs ?: 0L) +
        if (clockRunning) (now - (live?.relojInicioEn ?: 0L)).coerceAtLeast(0L) else 0L
    val clockMinute = (elapsed / 60_000).toInt()
    val clockText = "%02d:%02d".format(clockMinute, (elapsed / 1000 % 60).toInt())
    val typedMinute = minuteInput.toIntOrNull()
    val minuteValid = minuteInput.isBlank() || (typedMinute != null && typedMinute in 0..130)
    val effectiveMinute = if (minuteInput.isBlank()) clockMinute else typedMinute ?: clockMinute
    val canSave = clockRunning && minuteValid && !saving
    val eventTypes = listOf(
        "Gol", "Tarjeta", "Falta", "Penal", "Cambio",
        "Tiro de esquina", "Fuera de juego", "Saque lateral", "Saque de meta"
    )

    message.takeIf(String::isNotBlank)?.let { Body(it, if (isMatchError(it)) Rose else Forest) }
    SecondaryAction("Volver al operador / iniciar partido", onClick = { onNavigate(12) })
    Notice(COACH_ONLY_NOTICE)
    if (!clockRunning) {
        Notice(when {
            live == null -> "Esperando datos del partido. No se puede registrar sin cargar el partido."
            live?.estado == "FINALIZADO" -> "Partido finalizado: el Entrenador asignado puede reiniciarlo desde el operador o corregir los eventos existentes."
            live?.estado == "CANCELADO" -> "El partido esta cancelado."
            phase == "ENTRETIEMPO" -> "El Entrenador asignado debe iniciar el segundo tiempo desde el operador para registrar eventos."
            else -> "El Entrenador asignado debe iniciar primero el partido desde el operador (no existe un rol operador)."
        }, warning = true)
    }
    MockCard(containerColor = ForestDeep) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Eyebrow(
                when {
                    !isLive -> "ESTADO DEL PARTIDO"
                    phase == "ENTRETIEMPO" -> "ENTRETIEMPO"
                    else -> "PARTIDO EN CURSO"
                },
                Lime
            )
            Badge(if (isLive) "● EN VIVO" else live?.estado?.replace("_", " ") ?: "CARGANDO", color = Lime)
        }
        Text(
            "$homeTeamName — $awayTeamName",
            color = Color.White,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Black
        )
        Text(clockText, color = Lime, style = MaterialTheme.typography.displayMedium, fontWeight = FontWeight.Black)
        Body(
            if (clockRunning) "Reloj en marcha · ${live?.periodo ?: 1}.er tiempo"
            else "Inicia el partido desde el operador en vivo para registrar eventos.",
            Color.White.copy(alpha = .8f)
        )
    }

    SectionTitle("Nuevo evento", "PARTIDO EN VIVO")
    MockCard {
        Text("Tipo de evento", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
        eventTypes.chunked(2).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                row.forEach { type ->
                    Button(
                        onClick = { selectedType = type },
                        enabled = selectedType != type,
                        modifier = Modifier.weight(1f)
                    ) { Text(type, textAlign = TextAlign.Center) }
                }
                if (row.size == 1) Box(modifier = Modifier.weight(1f))
            }
        }
        OutlinedTextField(
            value = minuteInput,
            onValueChange = { minuteInput = it.filter(Char::isDigit).take(3) },
            label = { Text("Minuto del partido") },
            supportingText = {
                Text(
                    if (minuteInput.isBlank()) "Vacío: se usa el minuto del reloj (${clockMinute}′)"
                    else if (minuteValid) "Se registrará en el minuto $effectiveMinute′"
                    else "Ingresa un minuto entre 0 y 130"
                )
            },
            isError = !minuteValid,
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )
        Text("Equipo", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(
                onClick = { selectedTeamId = homeTeamId; selectedPlayerId = "" },
                enabled = selectedTeamId != homeTeamId,
                modifier = Modifier.weight(1f)
            ) { Text(homeTeamName) }
            Button(
                onClick = { selectedTeamId = awayTeamId; selectedPlayerId = "" },
                enabled = selectedTeamId != awayTeamId,
                modifier = Modifier.weight(1f)
            ) { Text(awayTeamName) }
        }
        Text("Jugador (opcional)", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
        MatchPlayerOption("Sin jugador", selected = selectedPlayerId.isBlank()) {
            selectedPlayerId = ""
        }
        teamPlayers.forEach { player ->
            MatchPlayerOption(
                label = "#${player.numero} · ${player.nombre}",
                selected = player.jugadorId == selectedPlayerId
            ) { selectedPlayerId = player.jugadorId }
        }
        if (teamPlayers.isEmpty()) {
            Body("No hay jugadores registrados para este equipo.")
        }
        OutlinedTextField(
            value = notes,
            onValueChange = { notes = it.take(140) },
            label = { Text("Observaciones (opcional)") },
            singleLine = false,
            modifier = Modifier.fillMaxWidth()
        )
        Notice("Si no conoces al jugador, puedes guardar el evento como «Sin jugador».")
        PrimaryAction(
            text = if (saving) "Guardando…" else "Guardar evento en cronología",
            enabled = canSave,
            onClick = {
                saving = true
                message = "Guardando evento…"
                val type = selectedType
                val minute = effectiveMinute
                val executionId = live?.ejecucionId ?: "inicial"
                scope.launch {
                    val result = FirebaseMatchManager.registerEvent(
                        matchId = matchId,
                        tipo = type,
                        minuto = minute,
                        periodo = live?.periodo ?: 1,
                        equipoId = selectedTeamId,
                        jugadorId = selectedPlayer?.jugadorId.orEmpty(),
                        jugadorNombre = selectedPlayer?.let { "#${it.numero} ${it.nombre}" }.orEmpty(),
                        observaciones = notes.trim(),
                        academiaId = context.academiaId,
                        equipoContextoId = context.equipoId,
                        ejecucionId = executionId
                    )
                    saving = false
                    message = if (result.isSuccess) {
                        minuteInput = ""
                        selectedPlayerId = ""
                        notes = ""
                        "$type registrado en el minuto $minute′. Compruebalo en Eventos registrados."
                    } else {
                        matchActionError("registrar eventos", result.exceptionOrNull())
                    }
                }
            }
        )
        message.takeIf(String::isNotBlank)?.let {
            Body(it, if (isMatchError(it)) Rose else Forest)
        }
    }
    SecondaryAction("Ver eventos registrados", onClick = { onNavigate(14) })
}

@Composable
private fun MatchPlayerOption(label: String, selected: Boolean, onSelect: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onSelect).padding(vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(selected = selected, onClick = onSelect)
        Text(label, style = MaterialTheme.typography.bodyMedium)
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
