package com.example.sportprog3.data.remote

import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.tasks.await

data class TrainingExercise(
    val id: String,
    val name: String,
    val description: String,
    val category: String,
    val durationMinutes: Int
)

data class TrainingSession(
    val id: String,
    val date: String,
    val time: String,
    val teamId: String,
    val teamName: String,
    val field: String,
    val durationMinutes: Int,
    val objective: String,
    val exercises: List<TrainingExercise>
)

data class TrainingTeam(val id: String, val name: String)

data class TrainingDirectoryState(
    val academyId: String = "",
    val academyName: String = "",
    val canManage: Boolean = false,
    val loading: Boolean = true,
    val teams: List<TrainingTeam> = emptyList(),
    val sessions: List<TrainingSession> = emptyList(),
    val library: List<TrainingExercise> = emptyList(),
    val error: String? = null
)

object TrainingRepository {
    private val auth by lazy { FirebaseAuth.getInstance() }
    private val firestore by lazy { FirebaseFirestore.getInstance() }

    fun observeDirectory(onChanged: (TrainingDirectoryState) -> Unit): () -> Unit {
        val uid = auth.currentUser?.uid
        if (uid == null) {
            onChanged(TrainingDirectoryState(loading = false, error = "Inicia sesión para consultar las sesiones."))
            return {}
        }

        var stopped = false
        var state = TrainingDirectoryState()
        var teamsListener: ListenerRegistration? = null
        var sessionsListener: ListenerRegistration? = null
        var libraryListener: ListenerRegistration? = null

        fun emit(error: String? = state.error, loading: Boolean = state.loading) {
            state = state.copy(error = error, loading = loading)
            if (!stopped) onChanged(state)
        }

        fun clearAcademyListeners() {
            teamsListener?.remove()
            sessionsListener?.remove()
            libraryListener?.remove()
            teamsListener = null
            sessionsListener = null
            libraryListener = null
        }

        val userListener = firestore.collection("usuarios").document(uid)
            .addSnapshotListener { user, userError ->
                if (stopped) return@addSnapshotListener
                if (userError != null) {
                    emit("No se pudo consultar tu perfil: ${userError.localizedMessage ?: "error de Firestore"}", false)
                    return@addSnapshotListener
                }
                if (user == null || !user.exists()) {
                    emit("No se encontró el perfil del usuario.", false)
                    return@addSnapshotListener
                }

                val academyId = user.getString("academiaId").orEmpty().trim()
                val role = user.getString("rol").orEmpty()
                if (academyId.isBlank()) {
                    clearAcademyListeners()
                    state = state.copy(
                        academyId = "",
                        academyName = "",
                        canManage = role in setOf("Entrenador", "Administrador"),
                        teams = emptyList(),
                        sessions = emptyList(),
                        library = emptyList()
                    )
                    emit("Tu usuario todavía no está asociado a una academia.", false)
                    return@addSnapshotListener
                }
                if (academyId == state.academyId && sessionsListener != null) {
                    state = state.copy(canManage = role in setOf("Entrenador", "Administrador"))
                    emit(error = null, loading = false)
                    return@addSnapshotListener
                }

                clearAcademyListeners()
                state = TrainingDirectoryState(
                    academyId = academyId,
                    academyName = "Academia $academyId",
                    canManage = role in setOf("Entrenador", "Administrador"),
                    loading = true
                )
                emit(error = null, loading = true)

                val academyRef = firestore.collection("academias").document(academyId)
                teamsListener = academyRef.collection("equipos")
                    .addSnapshotListener { snapshot, error ->
                        if (stopped) return@addSnapshotListener
                        if (error != null) {
                            emit("No se pudieron cargar las categorías: ${error.localizedMessage ?: "error de Firestore"}", false)
                        } else if (snapshot != null) {
                            state = state.copy(
                                teams = snapshot.documents.map { document ->
                                    TrainingTeam(
                                        id = document.id,
                                        name = document.getString("nombre")
                                            ?: document.getString("name")
                                            ?: document.id
                                    )
                                }.sortedBy { it.name.lowercase() }
                            )
                            emit(error = null, loading = false)
                        }
                    }

                sessionsListener = academyRef.collection("entrenamientos")
                    .addSnapshotListener { snapshot, error ->
                        if (stopped) return@addSnapshotListener
                        if (error != null) {
                            emit("No se pudieron cargar las sesiones: ${error.localizedMessage ?: "error de Firestore"}", false)
                        } else if (snapshot != null) {
                            state = state.copy(
                                sessions = snapshot.documents.map { document ->
                                    TrainingSession(
                                        id = document.id,
                                        date = document.getString("fecha").orEmpty(),
                                        time = document.getString("hora").orEmpty(),
                                        teamId = document.getString("equipoId").orEmpty(),
                                        teamName = document.getString("equipoNombre").orEmpty(),
                                        field = document.getString("cancha").orEmpty(),
                                        durationMinutes = (document.getLong("duracionMinutos") ?: 0L).toInt(),
                                        objective = document.getString("objetivo").orEmpty(),
                                        exercises = readExercises(document.get("ejercicios"))
                                    )
                                }.sortedWith(compareByDescending<TrainingSession> { it.date }.thenByDescending { it.time })
                            )
                            emit(error = null, loading = false)
                        }
                    }

                libraryListener = academyRef.collection("bibliotecaEjercicios")
                    .addSnapshotListener { snapshot, error ->
                        if (stopped) return@addSnapshotListener
                        if (error != null) {
                            emit("No se pudo cargar la biblioteca: ${error.localizedMessage ?: "error de Firestore"}", false)
                        } else if (snapshot != null) {
                            state = state.copy(
                                library = snapshot.documents.map { document ->
                                    TrainingExercise(
                                        id = document.id,
                                        name = document.getString("nombre").orEmpty(),
                                        description = document.getString("descripcion").orEmpty(),
                                        category = document.getString("categoria").orEmpty(),
                                        durationMinutes = (document.getLong("duracionMinutos") ?: 0L).toInt()
                                    )
                                }.sortedBy { it.name.lowercase() }
                            )
                            emit(error = null, loading = false)
                        }
                    }
            }

        return {
            stopped = true
            userListener.remove()
            clearAcademyListeners()
        }
    }

    suspend fun saveSession(session: TrainingSession): Result<String> = try {
        val uid = requireManager()
        val academyId = requireAcademyId(uid)
        val team = firestore.collection("academias").document(academyId)
            .collection("equipos").document(session.teamId).get().await()
        check(team.exists()) { "La categoría seleccionada ya no existe." }
        val teamName = team.getString("nombre") ?: team.getString("name") ?: session.teamName
        require(session.date.isNotBlank()) { "Ingresa la fecha de la sesión." }
        require(session.time.isNotBlank()) { "Ingresa la hora de la sesión." }
        require(session.field.isNotBlank()) { "Ingresa la cancha de entrenamiento." }
        require(session.durationMinutes > 0) { "La duración debe ser mayor que cero." }
        require(session.objective.isNotBlank()) { "Define el objetivo principal de la sesión." }
        require(session.exercises.isNotEmpty()) { "Agrega al menos un ejercicio desde la biblioteca." }
        require(session.exercises.all { it.durationMinutes > 0 }) { "Cada ejercicio debe tener una duración mayor que cero." }

        val ref = firestore.collection("academias").document(academyId)
            .collection("entrenamientos")
        val document = if (session.id.isBlank()) ref.document() else ref.document(session.id)
        document.set(
            mapOf(
                "fecha" to session.date.trim(),
                "hora" to session.time.trim(),
                "equipoId" to session.teamId,
                "equipoNombre" to teamName,
                "cancha" to session.field.trim(),
                "duracionMinutos" to session.durationMinutes,
                "objetivo" to session.objective.trim(),
                "ejercicios" to session.exercises.map { exercise ->
                    mapOf(
                        "ejercicioId" to exercise.id,
                        "nombre" to exercise.name,
                        "descripcion" to exercise.description,
                        "categoria" to exercise.category,
                        "duracionMinutos" to exercise.durationMinutes
                    )
                },
                "actualizadoPor" to uid,
                "actualizadoEn" to Timestamp.now()
            )
        ).await()
        Result.success(document.id)
    } catch (error: CancellationException) {
        throw error
    } catch (error: Exception) {
        Result.failure(error)
    }

    suspend fun createLibraryExercise(exercise: TrainingExercise): Result<Unit> = try {
        val uid = requireManager()
        val academyId = requireAcademyId(uid)
        require(exercise.name.isNotBlank()) { "Ingresa el nombre del ejercicio." }
        require(exercise.durationMinutes > 0) { "La duración debe ser mayor que cero." }
        firestore.collection("academias").document(academyId)
            .collection("bibliotecaEjercicios")
            .add(
                mapOf(
                    "nombre" to exercise.name.trim(),
                    "descripcion" to exercise.description.trim(),
                    "categoria" to exercise.category.trim(),
                    "duracionMinutos" to exercise.durationMinutes,
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

    private suspend fun requireManager(): String {
        val uid = auth.currentUser?.uid
            ?: throw IllegalStateException("Inicia sesión para gestionar sesiones.")
        val user = firestore.collection("usuarios").document(uid).get().await()
        check(user.exists()) { "No se encontró el perfil del usuario." }
        check(user.getString("rol") in setOf("Entrenador", "Administrador")) {
            "Tu rol no tiene permiso para gestionar sesiones."
        }
        return uid
    }

    private suspend fun requireAcademyId(uid: String): String {
        val academyId = firestore.collection("usuarios").document(uid).get().await()
            .getString("academiaId").orEmpty().trim()
        check(academyId.isNotBlank()) { "Tu usuario todavía no está asociado a una academia." }
        return academyId
    }

    private fun readExercises(value: Any?): List<TrainingExercise> {
        val items = value as? List<*> ?: return emptyList()
        return items.mapNotNull { item ->
            val data = item as? Map<*, *> ?: return@mapNotNull null
            TrainingExercise(
                id = data["ejercicioId"] as? String ?: "",
                name = data["nombre"] as? String ?: "",
                description = data["descripcion"] as? String ?: "",
                category = data["categoria"] as? String ?: "",
                durationMinutes = (data["duracionMinutos"] as? Number)?.toInt() ?: 0
            )
        }
    }
}
