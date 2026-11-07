package com.example.sportprog3.data.remote

import com.example.sportprog3.data.model.MatchEventModel
import com.example.sportprog3.data.model.MatchLiveModel
import com.google.firebase.Timestamp
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Date

data class MatchContext(
    val academiaId: String,
    val equipoId: String,
    val partidoId: String
)

object MatchSelection {
    private val selectedMatch = MutableStateFlow(
        MatchContext(
            FirebaseMatchManager.DEMO_ACADEMY_ID,
            FirebaseMatchManager.DEMO_TEAM_ID,
            FirebaseMatchManager.DEMO_MATCH_ID
        )
    )

    val current: StateFlow<MatchContext> = selectedMatch.asStateFlow()

    fun select(context: MatchContext) {
        selectedMatch.value = context
    }
}

object MatchFirestoreMapper {
    val eventOrder: Comparator<MatchEventModel> =
        compareBy<MatchEventModel> { it.periodo }
            .thenBy { it.minuto }
            .thenBy { it.creadoEn }
            .thenBy { it.eventoId }

    fun live(data: Map<String, Any?>): MatchLiveModel = MatchLiveModel(
        equipoLocalId = data.string("equipoLocalId", "los-cedros"),
        equipoLocalNombre = data.string("equipoLocalNombre", "Los Cedros"),
        equipoVisitanteId = data.string("equipoVisitanteId", "deportivo-surco"),
        equipoVisitanteNombre = data.string("equipoVisitanteNombre", "Deportivo Surco"),
        estado = data.string("estado", "PROGRAMADO"),
        periodo = data.int("periodo", 1),
        fase = data.string("fase", "SIN_INICIAR"),
        relojBaseMs = data.long("relojBaseMs", 0L),
        relojInicioEn = data.long("relojInicioEn", 0L),
        ejecucionId = data.string("ejecucionId", "inicial")
    )

    fun event(eventId: String, data: Map<String, Any?>): MatchEventModel = MatchEventModel(
        eventoId = data.string("eventoId", eventId),
        tipo = data.string("tipo", ""),
        minuto = data.int("minuto", 0),
        periodo = data.int("periodo", 1),
        equipoId = data.string("equipoId", ""),
        jugadorId = data.string("jugadorId", ""),
        jugadorNombre = data.string("jugadorNombre", ""),
        observaciones = data.string("observaciones", ""),
        creadoPor = data.string("creadoPor", ""),
        creadoEn = data.long("creadoEn", 0L),
        editadoPor = data.string("editadoPor", ""),
        editadoEn = data.long("editadoEn", 0L),
        anulado = data["anulado"] as? Boolean ?: false,
        ejecucionId = data.string("ejecucionId", "inicial")
    )

    private fun Map<String, Any?>.string(key: String, default: String): String =
        this[key] as? String ?: default

    private fun Map<String, Any?>.int(key: String, default: Int): Int =
        (this[key] as? Number)?.toInt() ?: default

    private fun Map<String, Any?>.long(key: String, default: Long): Long = when (val value = this[key]) {
        is Number -> value.toLong()
        is Timestamp -> value.toDate().time
        is Date -> value.time
        else -> default
    }
}
