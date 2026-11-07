package com.example.sportprog3.data.remote

import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.tasks.await

data class AcademyTeam(
    val academyId: String,
    val id: String,
    val name: String,
    val season: String,
    val playerCount: Int = 0
)

data class TeamDirectoryState(
    val academyId: String = "",
    val academyName: String = "",
    val canCreate: Boolean = false,
    val loading: Boolean = true,
    val teams: List<AcademyTeam> = emptyList(),
    val error: String? = null
)

data class TeamPlayer(
    val id: String,
    val name: String,
    val number: String,
    val detail: String
)

object TeamRepository {
    private val auth by lazy { FirebaseAuth.getInstance() }
    private val firestore by lazy { FirebaseFirestore.getInstance() }

    fun observeTeams(onChanged: (TeamDirectoryState) -> Unit): () -> Unit {
        val uid = auth.currentUser?.uid
        if (uid == null) {
            onChanged(TeamDirectoryState(loading = false, error = "Inicia sesión para consultar los equipos de tu academia."))
            return {}
        }

        var stopped = false
        var academyId = ""
        var academyName = ""
        var canCreate = false
        var teams = emptyList<AcademyTeam>()
        var academyListener: ListenerRegistration? = null
        var teamsListener: ListenerRegistration? = null
        val playerListeners = mutableListOf<ListenerRegistration>()

        fun emit(loading: Boolean = false, error: String? = null) {
            if (!stopped) {
                onChanged(
                    TeamDirectoryState(
                        academyId = academyId,
                        academyName = academyName,
                        canCreate = canCreate,
                        loading = loading,
                        teams = teams,
                        error = error
                    )
                )
            }
        }

        fun removeTeamListeners() {
            teamsListener?.remove()
            teamsListener = null
            playerListeners.forEach(ListenerRegistration::remove)
            playerListeners.clear()
        }

        val userListener = firestore.collection("usuarios").document(uid)
            .addSnapshotListener { user, userError ->
                if (stopped) return@addSnapshotListener
                if (userError != null) {
                    emit(error = "No se pudo consultar tu academia: ${userError.localizedMessage ?: "error de Firestore"}")
                    return@addSnapshotListener
                }
                if (user == null || !user.exists()) {
                    emit(loading = false, error = "No se encontró el perfil del usuario.")
                    return@addSnapshotListener
                }

                val resolvedAcademyId = user.getString("academiaId").orEmpty().trim()
                canCreate = user.getString("rol") in setOf("Entrenador", "Administrador")
                if (resolvedAcademyId.isBlank()) {
                    removeTeamListeners()
                    academyListener?.remove()
                    academyListener = null
                    academyId = ""
                    academyName = ""
                    teams = emptyList()
                    emit(loading = false, error = "Tu usuario todavía no está asociado a una academia.")
                    return@addSnapshotListener
                }
                if (resolvedAcademyId == academyId && teamsListener != null) {
                    emit()
                    return@addSnapshotListener
                }

                removeTeamListeners()
                academyListener?.remove()
                academyId = resolvedAcademyId
                academyName = "Academia $resolvedAcademyId"
                teams = emptyList()
                emit(loading = true)

                academyListener = firestore.collection("academias").document(academyId)
                    .addSnapshotListener { academy, academyError ->
                        if (stopped) return@addSnapshotListener
                        if (academyError != null) {
                            emit(error = "No se pudo consultar la academia: ${academyError.localizedMessage ?: "error de Firestore"}")
                        } else if (academy != null && academy.exists()) {
                            academyName = academy.getString("nombre")
                                ?: academy.getString("name")
                                ?: "Academia $academyId"
                            emit()
                        }
                    }

                teamsListener = firestore.collection("academias").document(academyId)
                    .collection("equipos")
                    .addSnapshotListener { snapshot, teamsError ->
                        if (stopped) return@addSnapshotListener
                        if (teamsError != null) {
                            emit(loading = false, error = "No se pudieron cargar los equipos: ${teamsError.localizedMessage ?: "error de Firestore"}")
                            return@addSnapshotListener
                        }
                        if (snapshot == null) {
                            emit(loading = false, error = "Firestore no devolvió la lista de equipos.")
                            return@addSnapshotListener
                        }

                        playerListeners.forEach(ListenerRegistration::remove)
                        playerListeners.clear()
                        val playerCounts = mutableMapOf<String, Int>()
                        teams = snapshot.documents.map { document ->
                            AcademyTeam(
                                academyId = academyId,
                                id = document.id,
                                name = document.getString("nombre")
                                    ?: document.getString("name")
                                    ?: document.id,
                                season = document.getString("temporada")
                                    ?: document.getString("season")
                                    ?: ""
                            )
                        }.sortedBy { it.name.lowercase() }
                        emit()

                        snapshot.documents.forEach { document ->
                            val teamId = document.id
                            val countListener = firestore.collection("academias").document(academyId)
                                .collection("equipos").document(teamId)
                                .collection("jugadores")
                                .addSnapshotListener { players, countError ->
                                    if (stopped) return@addSnapshotListener
                                    if (countError != null) {
                                        emit(error = "No se pudo contar el plantel de ${teams.firstOrNull { it.id == teamId }?.name ?: teamId}: ${countError.localizedMessage ?: "error de Firestore"}")
                                        return@addSnapshotListener
                                    }
                                    playerCounts[teamId] = players?.size() ?: 0
                                    teams = teams.map { team ->
                                        if (team.id == teamId) team.copy(playerCount = playerCounts.getValue(teamId))
                                        else team
                                    }
                                    emit()
                                }
                            playerListeners += countListener
                        }
                    }
            }

        return {
            stopped = true
            userListener.remove()
            academyListener?.remove()
            removeTeamListeners()
        }
    }

    fun observePlayers(
        team: AcademyTeam,
        onChanged: (List<TeamPlayer>) -> Unit,
        onError: (String) -> Unit
    ): () -> Unit {
        val listener = firestore.collection("academias").document(team.academyId)
            .collection("equipos").document(team.id)
            .collection("jugadores")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    onError("No se pudo cargar el plantel: ${error.localizedMessage ?: "error de Firestore"}")
                } else if (snapshot != null) {
                    onChanged(snapshot.documents.map { player ->
                        TeamPlayer(
                            id = player.id,
                            name = player.getString("nombre")
                                ?: player.getString("name")
                                ?: "Jugador",
                            number = (player.getLong("numero") ?: player.getLong("dorsal"))
                                ?.toString()
                                ?: "—",
                            detail = listOfNotNull(
                                player.getString("posicion"),
                                player.getString("estado")
                            ).joinToString(" · ").ifBlank { "Integrante del equipo" }
                        )
                    }.sortedBy { it.number.toIntOrNull() ?: Int.MAX_VALUE })
                }
            }
        return { listener.remove() }
    }

    suspend fun createTeam(name: String, season: String): Result<Unit> = try {
        val uid = auth.currentUser?.uid
            ?: throw IllegalStateException("Inicia sesión para registrar un equipo.")
        val user = firestore.collection("usuarios").document(uid).get().await()
        check(user.exists()) { "No se encontró el perfil del usuario." }
        check(user.getString("rol") in setOf("Entrenador", "Administrador")) {
            "Tu rol no tiene permiso para registrar equipos."
        }
        val academyId = user.getString("academiaId").orEmpty().trim()
        check(academyId.isNotBlank()) { "Tu usuario todavía no está asociado a una academia." }
        val normalizedName = name.trim()
        require(normalizedName.isNotBlank()) { "Ingresa el nombre de la categoría." }

        firestore.collection("academias").document(academyId)
            .collection("equipos").add(
                mapOf(
                    "nombre" to normalizedName,
                    "temporada" to season.trim(),
                    "creadoPor" to uid,
                    "creadoEn" to Timestamp.now()
                )
            ).await()
        Result.success(Unit)
    } catch (error: CancellationException) {
        throw error
    } catch (error: Exception) {
        Result.failure(error)
    }
}
