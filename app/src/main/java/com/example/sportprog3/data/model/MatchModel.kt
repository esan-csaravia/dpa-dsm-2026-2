package com.example.sportprog3.data.model

data class MatchLiveModel(
    val equipoLocalId: String = "los-cedros",
    val equipoLocalNombre: String = "Los Cedros",
    val equipoVisitanteId: String = "deportivo-surco",
    val equipoVisitanteNombre: String = "Deportivo Surco",
    val estado: String = "EN_VIVO",
    val periodo: Int = 1,
    val fase: String = "SIN_INICIAR",
    val relojBaseMs: Long = 0L,
    val relojInicioEn: Long = 0L,
    val ejecucionId: String = "inicial"
)

data class MatchEventModel(
    val eventoId: String = "",
    val tipo: String = "",
    val minuto: Int = 0,
    val periodo: Int = 1,
    val equipoId: String = "",
    val jugadorId: String = "",
    val jugadorNombre: String = "",
    val observaciones: String = "",
    val creadoPor: String = "",
    val creadoEn: Long = 0L,
    val editadoPor: String = "",
    val editadoEn: Long = 0L,
    val anulado: Boolean = false,
    val ejecucionId: String = "inicial"
)

data class PlayerModel(
    val jugadorId: String = "",
    val nombre: String = "",
    val numero: Int = 0,
    val equipoId: String = ""
)
