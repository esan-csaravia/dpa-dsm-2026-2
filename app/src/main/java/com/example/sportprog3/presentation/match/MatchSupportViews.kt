package com.example.sportprog3.presentation.match

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.sportprog3.data.model.MatchEventModel
import com.example.sportprog3.data.model.MatchLiveModel
import com.example.sportprog3.data.remote.FirebaseMatchManager
import com.example.sportprog3.data.remote.MatchContext
import com.example.sportprog3.data.remote.MatchSelection
import com.example.sportprog3.presentation.components.Body
import com.example.sportprog3.presentation.components.Eyebrow
import com.example.sportprog3.presentation.components.EventLine
import com.example.sportprog3.presentation.components.MockCard
import com.example.sportprog3.presentation.components.Notice
import com.example.sportprog3.presentation.components.PrimaryAction
import com.example.sportprog3.presentation.components.SectionTitle
import com.example.sportprog3.ui.theme.Forest
import com.example.sportprog3.ui.theme.Rose

internal const val COACH_ONLY_NOTICE =
    "Las acciones de control del partido corresponden al entrenador asignado."

internal fun isMatchError(message: String): Boolean {
    val normalized = message.lowercase()
    return normalized.contains("error") ||
        normalized.contains("no se pudo") ||
        normalized.contains("no existe") ||
        normalized.contains("debes iniciar") ||
        normalized.contains("invalido") ||
        normalized.contains("cambio") ||
        normalized.contains("reinici")
}

internal fun matchActionError(action: String, error: Throwable?, hint: String = ""): String {
    val reason = error?.localizedMessage?.takeIf(String::isNotBlank)
        ?: "Ocurrió un error inesperado."
    return buildString {
        append("No se pudo $action: ")
        append(reason)
        if (hint.isNotBlank()) {
            append(" ")
            append(hint)
        }
    }
}

@Composable
fun MatchScheduleScreen(onOpenMatch: () -> Unit) {
    Eyebrow("PARTIDOS · LOS CEDROS")
    SectionTitle("Seleccionar partido", "Fecha 6")
    MockCard {
        Body("Sábado 19 sep · 16:30 · Cancha 2")
        EventLine("16:30", "Los Cedros vs. Deportivo Surco", "Partido de demostración")
    }
    Notice("Selecciona el partido para abrir el operador en vivo.")
    PrimaryAction(
        text = "Abrir partido",
        onClick = {
            MatchSelection.select(
                MatchContext(
                    FirebaseMatchManager.DEMO_ACADEMY_ID,
                    FirebaseMatchManager.DEMO_TEAM_ID,
                    FirebaseMatchManager.DEMO_MATCH_ID
                )
            )
            onOpenMatch()
        }
    )
}

@Composable
fun MatchSummaryView() {
    val context by MatchSelection.current.collectAsState()
    var live by remember(context) { mutableStateOf<MatchLiveModel?>(null) }
    var events by remember(context) { mutableStateOf(emptyList<MatchEventModel>()) }
    var message by remember(context) { mutableStateOf("") }

    DisposableEffect(context) {
        val stopObserving = FirebaseMatchManager.observeMatch(
            matchId = context.partidoId,
            onLiveChanged = { live = it },
            onEventsChanged = { events = it },
            onError = { message = "No se pudo cargar el resumen: $it" },
            academiaId = context.academiaId,
            equipoContextoId = context.equipoId
        )
        onDispose { stopObserving() }
    }

    val currentEvents = events.filter { !it.anulado && it.ejecucionId == live?.ejecucionId }
    val goals = currentEvents.filter { it.tipo.equals("Gol", ignoreCase = true) }
    val homeGoals = goals.count { it.equipoId == live?.equipoLocalId }
    val awayGoals = goals.count { it.equipoId == live?.equipoVisitanteId }

    Eyebrow("CRÓNICA DEL PARTIDO")
    SectionTitle("Resumen", live?.estado?.replace("_", " ") ?: "Cargando partido…")
    message.takeIf(String::isNotBlank)?.let {
        Body(it, if (isMatchError(it)) Rose else Forest)
    }
    MockCard {
        Body("${live?.equipoLocalNombre ?: "Los Cedros"}  $homeGoals — $awayGoals  ${live?.equipoVisitanteNombre ?: "Deportivo Surco"}")
        Body(
            when {
                live == null -> "El resumen estará disponible cuando se carguen los datos del partido."
                currentEvents.isEmpty() -> "Aún no hay eventos registrados para esta ejecución."
                else -> "Se registraron ${currentEvents.size} eventos y ${goals.size} goles en esta ejecución."
            }
        )
    }
    SectionTitle("Momentos del partido", "${currentEvents.size} eventos")
    if (currentEvents.isEmpty()) {
        Notice("Los eventos registrados aparecerán aquí.")
    } else {
        currentEvents.sortedWith(
            compareByDescending<MatchEventModel> { it.periodo }
                .thenByDescending { it.minuto }
                .thenByDescending { it.creadoEn }
        ).forEach { event ->
            MockCard {
                EventLine(
                    "${event.minuto}′",
                    event.tipo,
                    listOfNotNull(
                        event.jugadorNombre.takeIf(String::isNotBlank),
                        event.observaciones.takeIf(String::isNotBlank)
                    ).joinToString(" · ").ifBlank { "Sin detalle adicional" }
                )
            }
        }
    }
}
