package com.example.sportprog3.data.remote

import com.example.sportprog3.data.model.MatchEventModel
import com.example.sportprog3.data.model.MatchLiveModel
import com.example.sportprog3.data.model.PlayerModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.firestore.PersistentCacheSettings
import com.google.firebase.firestore.MetadataChanges
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.tasks.await
import java.util.UUID

object FirebaseMatchManager {
    const val DEMO_ACADEMY_ID = "academia-demo"
    const val DEMO_TEAM_ID = "los-cedros"
    const val DEMO_MATCH_ID = "partido-demo-001"
    const val MINUTOS_POR_TIEMPO = 45

    private val database by lazy { FirebaseFirestore.getInstance() }

    // Firestore no expone serverTimeOffset; el reloj visual requiere hora automatica.
    fun deviceNow(): Long = System.currentTimeMillis()

    private fun matchRef(matchId: String, academiaId: String, equipoId: String) =
        database.collection("academias").document(academiaId)
            .collection("equipos").document(equipoId)
            .collection("partidos").document(matchId)

    fun enableOfflinePersistence() {
        database.firestoreSettings = FirebaseFirestoreSettings.Builder()
            .setLocalCacheSettings(PersistentCacheSettings.newBuilder().build())
            .build()
    }

    private fun currentUid(): String = FirebaseAuth.getInstance().currentUser?.uid
        ?: throw IllegalStateException("Debes iniciar sesion para registrar cambios.")

    fun observeOperatorPermission(
        academiaId: String,
        onChanged: (Boolean, String?) -> Unit
    ): () -> Unit {
        val uid = FirebaseAuth.getInstance().currentUser?.uid
        if (uid == null) {
            onChanged(false, "Debes iniciar sesión para operar el partido.")
            return {}
        }
        val listener = database.collection("usuarios").document(uid)
            .addSnapshotListener { user, error ->
                when {
                    error != null -> onChanged(
                        false,
                        "No se pudo verificar tu permiso: ${error.localizedMessage ?: "error de Firestore"}"
                    )
                    user == null || !user.exists() -> onChanged(false, "No se encontró tu perfil.")
                    user.getString("rol") !in setOf("Entrenador", "Administrador") ->
                        onChanged(false, "Solo Entrenador o Administrador puede operar el partido.")
                    user.getString("academiaId").orEmpty() != academiaId ->
                        onChanged(false, "Tu usuario no pertenece a la academia de este partido.")
                    else -> onChanged(true, null)
                }
            }
        return { listener.remove() }
    }

    private fun eventoMap(
        eventoId: String,
        tipo: String,
        minuto: Int,
        periodo: Int,
        equipoId: String,
        jugadorId: String = "",
        jugadorNombre: String = "",
        observaciones: String = "",
        ejecucionId: String = "inicial"
    ) = mapOf(
        "eventoId" to eventoId,
        "tipo" to tipo,
        "minuto" to minuto,
        "periodo" to periodo,
        "equipoId" to equipoId,
        "jugadorId" to jugadorId,
        "jugadorNombre" to jugadorNombre,
        "observaciones" to observaciones,
        "ejecucionId" to ejecucionId,
        "creadoPor" to currentUid(),
        "creadoEn" to FieldValue.serverTimestamp(),
        "editadoPor" to currentUid(),
        "editadoEn" to FieldValue.serverTimestamp(),
        "anulado" to false
    )

    suspend fun registerEvent(
        matchId: String,
        tipo: String,
        minuto: Int,
        periodo: Int,
        equipoId: String,
        jugadorId: String = "",
        jugadorNombre: String = "",
        observaciones: String = "",
        academiaId: String = DEMO_ACADEMY_ID,
        equipoContextoId: String = DEMO_TEAM_ID,
        ejecucionId: String = "inicial"
    ): Result<Unit> = writeResult {
        require(minuto in 0..180) { "El minuto debe estar entre 0 y 180." }
        require(periodo in 1..2) { "Periodo invalido." }
        require(tipo in MATCH_EVENT_TYPES) { "Tipo de evento no valido." }
        requireMatchOperator(academiaId)
        val ref = matchRef(matchId, academiaId, equipoContextoId)
        val eventRef = ref.collection("eventos").document()
        val uid = currentUid()
        database.runTransaction { transaction ->
            val match = transaction.get(ref)
            check(match.exists()) { "El partido no existe." }
            check((match.getString("ejecucionId") ?: "inicial") == ejecucionId) {
                "El partido fue reiniciado. Recarga antes de continuar."
            }
            check(match.getString("fase") in listOf("PRIMER_TIEMPO", "SEGUNDO_TIEMPO")) {
                "El partido no está en juego."
            }
            check(match.getString("estado") !in listOf("FINALIZADO", "CANCELADO")) {
                "El partido ya no admite eventos."
            }
            check((match.getLong("periodo") ?: 1L).toInt() == periodo) {
                "El periodo del partido cambió. Actualiza la pantalla."
            }
            check(equipoId == match.getString("equipoLocalId") ||
                equipoId == match.getString("equipoVisitanteId")) {
                "El equipo seleccionado no pertenece a este partido."
            }
            val eventData = eventoMap(
                eventRef.id, tipo, minuto, periodo, equipoId, jugadorId, jugadorNombre,
                observaciones, ejecucionId
            ).toMutableMap()
            eventData["creadoPor"] = uid
            eventData["editadoPor"] = uid
            transaction.update(ref, mapOf(
                "revisionEventos" to FieldValue.increment(1),
                "editadoPor" to uid,
                "editadoEn" to FieldValue.serverTimestamp()
            ))
            transaction.set(eventRef, eventData)
        }.await()
    }

    suspend fun actualizarEvento(
        matchId: String,
        eventoId: String,
        minuto: Int,
        equipoId: String,
        jugadorId: String,
        jugadorNombre: String,
        observaciones: String,
        academiaId: String = DEMO_ACADEMY_ID,
        equipoContextoId: String = DEMO_TEAM_ID
    ): Result<Unit> = writeResult {
        requireMatchOperator(academiaId)
        val ref = matchRef(matchId, academiaId, equipoContextoId)
        database.batch().update(ref, mapOf(
            "revisionEventos" to FieldValue.increment(1),
            "editadoPor" to currentUid(), "editadoEn" to FieldValue.serverTimestamp()
        )).update(ref.collection("eventos").document(eventoId),
            mapOf(
                "minuto" to minuto,
                "equipoId" to equipoId,
                "jugadorId" to jugadorId,
                "jugadorNombre" to jugadorNombre,
                "observaciones" to observaciones,
                "editadoPor" to currentUid(),
                "editadoEn" to FieldValue.serverTimestamp()
            )
        ).commit().await()
    }

    fun observePlayers(
        matchId: String,
        onPlayersChanged: (List<PlayerModel>) -> Unit,
        onError: (String) -> Unit,
        academiaId: String = DEMO_ACADEMY_ID,
        equipoContextoId: String = DEMO_TEAM_ID
    ): () -> Unit {
        val listener = matchRef(matchId, academiaId, equipoContextoId).collection("jugadores")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    onError(error.message ?: "No se pudo consultar el plantel.")
                } else if (snapshot != null) {
                    try {
                        onPlayersChanged(snapshot.documents.map {
                            PlayerModel(
                                jugadorId = it.id,
                                nombre = it.getString("nombre").orEmpty(),
                                numero = (it.getLong("numero") ?: 0L).toInt(),
                                equipoId = it.getString("equipoId").orEmpty()
                            )
                        }.sortedBy { it.numero })
                    } catch (e: RuntimeException) {
                        onError("Plantel con formato invalido: ${e.message}")
                    }
                }
            }
        return { listener.remove() }
    }

    suspend fun cambiarFase(
        matchId: String,
        fase: String,
        periodo: Int,
        relojBaseMs: Long,
        tipoEvento: String,
        finalizarPartido: Boolean = false,
        academiaId: String = DEMO_ACADEMY_ID,
        equipoContextoId: String = DEMO_TEAM_ID,
        ejecucionId: String = "inicial"
    ): Result<Unit> = writeResult {
        requireMatchOperator(academiaId)
        require(tipoEvento in MATCH_EVENT_TYPES) { "Tipo de evento no valido." }
        val ref = matchRef(matchId, academiaId, equipoContextoId)
        val eventRef = ref.collection("eventos").document()
        val updates = mutableMapOf<String, Any?>(
            "fase" to fase,
            "periodo" to periodo,
            "relojBaseMs" to relojBaseMs,
            "revisionEventos" to FieldValue.increment(1),
            "relojInicioEn" to if (fase in listOf("PRIMER_TIEMPO", "SEGUNDO_TIEMPO")) {
                FieldValue.serverTimestamp()
            } else null,
            "editadoPor" to currentUid(),
            "editadoEn" to FieldValue.serverTimestamp()
        )
        if (finalizarPartido) {
            updates["estado"] = "FINALIZADO"
            updates["finalizadoEn"] = FieldValue.serverTimestamp()
        }
        database.runTransaction { transaction ->
            val current = transaction.get(ref)
            check(current.exists()) { "El partido no existe." }
            check((current.getString("ejecucionId") ?: "inicial") == ejecucionId) {
                "El partido fue reiniciado. Recarga antes de continuar."
            }
            val previousPhase = current.getString("fase")
            val allowed = when (fase) {
                "PRIMER_TIEMPO" -> previousPhase == "SIN_INICIAR"
                "ENTRETIEMPO" -> previousPhase == "PRIMER_TIEMPO"
                "SEGUNDO_TIEMPO" -> previousPhase == "ENTRETIEMPO"
                "FINALIZADO" -> previousPhase == "SEGUNDO_TIEMPO"
                else -> false
            }
            check(allowed && current.getString("estado") !in listOf("FINALIZADO", "CANCELADO")) {
                "El estado del partido cambio. Revisa el reloj antes de continuar."
            }
            if (current.getString("estado") != "EN_VIVO") updates["estado"] = fase
            transaction.update(ref, updates)
            transaction.set(
                eventRef,
                eventoMap(eventRef.id, tipoEvento, (relojBaseMs / 60_000).toInt(), periodo, "", ejecucionId = ejecucionId)
            )
        }.await()
    }

    suspend fun reiniciarPartido(
        matchId: String,
        ejecucionId: String,
        academiaId: String = DEMO_ACADEMY_ID,
        equipoContextoId: String = DEMO_TEAM_ID
    ): Result<Unit> = writeResult {
        requireMatchOperator(academiaId)
        val ref = matchRef(matchId, academiaId, equipoContextoId)
        val nextId = UUID.randomUUID().toString()
        val uid = currentUid()
        database.runTransaction { transaction ->
            val current = transaction.get(ref)
            check(current.exists()) { "El partido no existe." }
            check((current.getString("ejecucionId") ?: "inicial") == ejecucionId) {
                "Otro usuario ya reinicio el partido. Recarga la pantalla."
            }
            check(current.getString("estado") != "CANCELADO") { "Un partido cancelado no se puede reiniciar." }
            val archive = current.data.orEmpty().toMutableMap()
            archive["ejecucionId"] = ejecucionId
            archive["archivadoPor"] = uid
            archive["archivadoEn"] = FieldValue.serverTimestamp()
            archive["siguienteEjecucionId"] = nextId
            transaction.set(ref.collection("ejecuciones").document(ejecucionId), archive)
            transaction.update(ref, mapOf(
                "estado" to "PROGRAMADO", "fase" to "SIN_INICIAR", "periodo" to 1,
                "relojBaseMs" to 0L, "relojInicioEn" to null,
                "finalizadoEn" to FieldValue.delete(),
                "ejecucionId" to nextId,
                "revisionEventos" to 0L,
                "editadoPor" to uid, "editadoEn" to FieldValue.serverTimestamp()
            ))
        }.await()
    }

    fun observeMatch(
        matchId: String,
        onLiveChanged: (MatchLiveModel?) -> Unit,
        onEventsChanged: (List<MatchEventModel>) -> Unit,
        onError: (String) -> Unit,
        academiaId: String = DEMO_ACADEMY_ID,
        equipoContextoId: String = DEMO_TEAM_ID
    ): () -> Unit {
        val ref = matchRef(matchId, academiaId, equipoContextoId)
        val liveListener = ref.addSnapshotListener(MetadataChanges.INCLUDE) { snapshot, error ->
            if (error != null) {
                onError(error.message ?: "No se pudo consultar el partido.")
            } else if (snapshot != null) {
                try {
                    if (snapshot.exists()) {
                        onLiveChanged(MatchFirestoreMapper.live(snapshot.estimatedData()))
                    } else {
                        onLiveChanged(null)
                        if (!snapshot.metadata.isFromCache) {
                            onError("El partido no existe en el servidor. Selecciona otro en Partidos.")
                        }
                    }

                } catch (e: RuntimeException) {
                    onError("Partido con formato invalido: ${e.message}")
                }
            }
        }
        val eventsListener = ref.collection("eventos").addSnapshotListener { snapshot, error ->
            if (error != null) {
                onError(error.message ?: "No se pudo consultar la cronologia.")
            } else if (snapshot != null) {
                try {
                    onEventsChanged(snapshot.documents.map {
                        MatchFirestoreMapper.event(it.id, it.estimatedData())
                    }.sortedWith(MatchFirestoreMapper.eventOrder))
                } catch (e: RuntimeException) {
                    onError("Eventos con formato invalido: ${e.message}")
                }
            }
        }
        return {
            liveListener.remove()
            eventsListener.remove()
        }
    }

    fun observeExecutions(
        context: MatchContext,
        onChanged: (List<MatchLiveModel>) -> Unit,
        onError: (String) -> Unit
    ): () -> Unit {
        val listener = matchRef(context.partidoId, context.academiaId, context.equipoId)
            .collection("ejecuciones").addSnapshotListener { snapshot, error ->
                if (error != null) {
                    onError(error.message ?: "No se pudo consultar el historial.")
                } else if (snapshot != null) {
                    try {
                        onChanged(snapshot.documents.sortedByDescending {
                            it.getTimestamp("archivadoEn")?.toDate()?.time ?: 0L
                        }.map { MatchFirestoreMapper.live(it.estimatedData()) })
                    } catch (e: RuntimeException) {
                        onError("Historial con formato invalido: ${e.message}")
                    }
                }
            }
        return { listener.remove() }
    }

    private fun DocumentSnapshot.estimatedData(): Map<String, Any?> =
        getData(DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
            ?: throw IllegalStateException("El documento $id no contiene datos.")

    private suspend fun requireMatchOperator(academiaId: String) {
        val user = database.collection("usuarios").document(currentUid()).get().await()
        check(user.exists()) { "No se encontró tu perfil." }
        check(user.getString("rol") in setOf("Entrenador", "Administrador")) {
            "Solo Entrenador o Administrador puede operar el partido."
        }
        check(user.getString("academiaId").orEmpty() == academiaId) {
            "Tu usuario no pertenece a la academia de este partido."
        }
    }

    private suspend fun writeResult(block: suspend () -> Unit): Result<Unit> = try {
        block()
        Result.success(Unit)
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        Result.failure(e)
    }

    private val MATCH_EVENT_TYPES = setOf(
        "Gol", "Tarjeta", "Falta", "Penal", "Cambio",
        "Tiro de esquina", "Fuera de juego", "Saque lateral", "Saque de meta",
        "Inicio", "Inicio 2PT", "Fin 1PT", "Fin del partido"
    )
}
