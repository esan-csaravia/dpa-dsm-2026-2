package com.example.sportprog3.data.remote

import com.example.sportprog3.data.model.MatchEventModel
import com.example.sportprog3.data.model.MatchLiveModel
import com.example.sportprog3.data.model.PlayerModel
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ServerValue
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.tasks.await

object FirebaseMatchManager {

    const val DATABASE_URL = "https://sportpro-g3-default-rtdb.firebaseio.com/"
    const val DEMO_MATCH_ID = "partido-demo-001"

    private val database by lazy { FirebaseDatabase.getInstance(DATABASE_URL) }

    const val MINUTOS_POR_TIEMPO = 45

    @Volatile
    private var serverOffsetMs = 0L

    fun serverNow(): Long = System.currentTimeMillis() + serverOffsetMs

    private fun matchRef(matchId: String) = database.getReference("partidos").child(matchId)

    fun enableOfflinePersistence() = database.setPersistenceEnabled(true)

    private fun eventoMap(
        tipo: String,
        minuto: Int,
        periodo: Int,
        equipoId: String,
        jugadorId: String = "",
        jugadorNombre: String = "",
        observaciones: String = ""
    ) = mapOf(
        "tipo" to tipo,
        "minuto" to minuto,
        "periodo" to periodo,
        "equipoId" to equipoId,
        "jugadorId" to jugadorId,
        "jugadorNombre" to jugadorNombre,
        "observaciones" to observaciones,
        "creadoPor" to "operador-demo",
        "creadoEn" to ServerValue.TIMESTAMP,
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
        observaciones: String = ""
    ): Result<Unit> = try {
        matchRef(matchId).child("eventos").push().setValue(
            eventoMap(tipo, minuto, periodo, equipoId, jugadorId, jugadorNombre, observaciones)
        ).await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun actualizarEvento(
        matchId: String,
        eventoId: String,
        minuto: Int,
        equipoId: String,
        jugadorId: String,
        jugadorNombre: String,
        observaciones: String
    ): Result<Unit> = try {
        matchRef(matchId).child("eventos").child(eventoId).updateChildren(
            mapOf(
                "minuto" to minuto,
                "equipoId" to equipoId,
                "jugadorId" to jugadorId,
                "jugadorNombre" to jugadorNombre,
                "observaciones" to observaciones,
                "editadoPor" to "operador-demo",
                "editadoEn" to ServerValue.TIMESTAMP
            )
        ).await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    fun observePlayers(
        matchId: String,
        onPlayersChanged: (List<PlayerModel>) -> Unit,
        onError: (String) -> Unit
    ): () -> Unit {
        val playersRef = matchRef(matchId).child("jugadores")
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                onPlayersChanged(
                    snapshot.children
                        .mapNotNull { it.getValue(PlayerModel::class.java)?.copy(jugadorId = it.key.orEmpty()) }
                        .sortedBy { it.numero }
                )
            }

            override fun onCancelled(error: DatabaseError) = onError(error.message)
        }
        playersRef.addValueEventListener(listener)
        return { playersRef.removeEventListener(listener) }
    }

    // Cambia la fase del reloj y registra su evento en una sola escritura atómica.
    suspend fun cambiarFase(
        matchId: String,
        fase: String,
        periodo: Int,
        relojBaseMs: Long,
        tipoEvento: String,
        finalizarPartido: Boolean = false
    ): Result<Unit> = try {
        val ref = matchRef(matchId)
        val eventKey = ref.child("eventos").push().key ?: throw IllegalStateException("Sin clave de evento")
        val updates = mutableMapOf<String, Any?>(
            "en_vivo/fase" to fase,
            "en_vivo/periodo" to periodo,
            "en_vivo/relojBaseMs" to relojBaseMs,
            "en_vivo/relojInicioEn" to ServerValue.TIMESTAMP,
            "eventos/$eventKey" to eventoMap(tipoEvento, (relojBaseMs / 60_000).toInt(), periodo, "")
        )
        if (finalizarPartido) {
            updates["en_vivo/estado"] = "FINALIZADO"
            updates["en_vivo/finalizadoEn"] = ServerValue.TIMESTAMP
        }
        ref.updateChildren(updates).await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    fun observeMatch(
        matchId: String,
        onLiveChanged: (MatchLiveModel?) -> Unit,
        onEventsChanged: (List<MatchEventModel>) -> Unit,
        onError: (String) -> Unit
    ): () -> Unit {
        val liveRef = matchRef(matchId).child("en_vivo")
        val eventsRef = matchRef(matchId).child("eventos")

        val liveListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                onLiveChanged(snapshot.getValue(MatchLiveModel::class.java))
            }

            override fun onCancelled(error: DatabaseError) = onError(error.message)
        }
        val eventsListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                onEventsChanged(
                    snapshot.children
                        .mapNotNull { it.getValue(MatchEventModel::class.java)?.copy(eventoId = it.key.orEmpty()) }
                        .sortedWith(compareBy({ it.minuto }, { it.creadoEn }))
                )
            }

            override fun onCancelled(error: DatabaseError) = onError(error.message)
        }

        val offsetRef = database.getReference(".info/serverTimeOffset")
        val offsetListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                serverOffsetMs = snapshot.getValue(Long::class.java) ?: 0L
            }

            override fun onCancelled(error: DatabaseError) = Unit
        }

        offsetRef.addValueEventListener(offsetListener)
        liveRef.addValueEventListener(liveListener)
        eventsRef.addValueEventListener(eventsListener)
        return {
            offsetRef.removeEventListener(offsetListener)
            liveRef.removeEventListener(liveListener)
            eventsRef.removeEventListener(eventsListener)
        }
    }
}
