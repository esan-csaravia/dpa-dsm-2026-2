
package com.example.sportprog3.presentation.training

import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.sportprog3.data.remote.TrainingDirectoryState
import com.example.sportprog3.data.remote.TrainingExercise
import com.example.sportprog3.data.remote.TrainingRepository
import com.example.sportprog3.data.remote.TrainingSession
import com.example.sportprog3.presentation.components.Body
import com.example.sportprog3.presentation.components.Eyebrow
import com.example.sportprog3.presentation.components.Metric
import com.example.sportprog3.presentation.components.MockCard
import com.example.sportprog3.presentation.components.Notice
import com.example.sportprog3.presentation.components.PlayerRow
import com.example.sportprog3.presentation.components.PrimaryAction
import com.example.sportprog3.presentation.components.ScreenFrame
import com.example.sportprog3.presentation.components.SecondaryAction
import com.example.sportprog3.presentation.components.SectionTitle
import com.example.sportprog3.presentation.components.SoftDivider
import com.example.sportprog3.presentation.components.toneForState

import com.example.sportprog3.ui.theme.Forest
import com.example.sportprog3.ui.theme.Mint
import com.example.sportprog3.ui.theme.Rose
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.SetOptions
import com.google.firebase.Timestamp

import java.text.SimpleDateFormat
import java.util.Locale

// =====================================================
// MODELOS DE US-009
// =====================================================

private data class EquipoAsistencia(
    val id: String,
    val nombre: String,
    val categoria: String,
    val division: String
) {
    val etiqueta: String
        get() = "$nombre · $categoria · División $division"
}

private data class SesionAsistencia(
    val id: String,
    val fecha: Timestamp?,
    val estado: String,
    val objetivo: String,
    val ubicacion: String,
    val confirmada: Boolean,
    val revision: Long,
    val membresiaIds: List<String>
)

private data class JugadorAsistencia(
    val membresiaId: String,
    val jugadorId: String,
    val jugadorUid: String?,
    val dorsal: Int,
    val nombre: String,
    val posicion: String
)

private val ESTADOS_ASISTENCIA = listOf(
    "Presente",
    "Tarde",
    "Ausente"
)

private fun formatoFecha(fecha: Timestamp?): String {
    if (fecha == null) return "Fecha no registrada"

    return SimpleDateFormat(
        "dd/MM/yyyy · HH:mm",
        Locale("es", "PE")
    ).format(fecha.toDate())
}

private fun DocumentSnapshot.convertirEquipo(): EquipoAsistencia? {
    if (!exists() || getString("estado") != "ACTIVO") {
        return null
    }

    return EquipoAsistencia(
        id = id,
        nombre = getString("nombre") ?: id,
        categoria = getString("categoria") ?: "Sin categoría",
        division = getString("division") ?: "Sin división"
    )
}

private fun DocumentSnapshot.convertirSesion(): SesionAsistencia {
    val ids = (get("membresiaIdsSesion") as? List<*>)
        ?.filterIsInstance<String>()
        ?.distinct()
        .orEmpty()

    return SesionAsistencia(
        id = id,
        fecha = getTimestamp("fecha"),
        estado = getString("estado") ?: "SIN_ESTADO",
        objetivo = getString("objetivo") ?: "Sin objetivo registrado",
        ubicacion = getString("ubicacion") ?: "Sin ubicación",
        confirmada = getBoolean("asistenciaConfirmada") == true,
        revision = getLong("revision") ?: 0L,
        membresiaIds = ids
    )
}

// =====================================================
// NAVEGACIÓN ORIGINAL
// =====================================================

@Composable
fun TrainingScreens(screen: Int, onNavigate: (Int) -> Unit = {}) {
    var selectedSession by remember { mutableStateOf<TrainingSession?>(null) }
    ScreenFrame {
        if (screen == 8) {
            TrainingPlan(onNavigate = onNavigate, onSessionSelected = { selectedSession = it })
        } else {
            Attendance(session = selectedSession)
        }
    }
}

// =====================================================
// US-008: PLAN DE ENTRENAMIENTO
// SE CONSERVA LA PANTALLA DEL COMPAÑERO
// =====================================================

@Composable
private fun TrainingPlan(
    onNavigate: (Int) -> Unit,
    onSessionSelected: (TrainingSession) -> Unit
) {
    var directory by remember { mutableStateOf(TrainingDirectoryState()) }
    var selectedSessionId by remember { mutableStateOf("") }
    var formKey by remember { mutableIntStateOf(0) }
    var selectedTeamId by remember { mutableStateOf("") }
    var showSessionPicker by remember { mutableStateOf(false) }
    var showTeamPicker by remember { mutableStateOf(false) }
    var showLibrary by remember { mutableStateOf(false) }
    var showNewExercise by remember { mutableStateOf(false) }
    var date by remember(formKey) { mutableStateOf(today()) }
    var time by remember(formKey) { mutableStateOf("17:00") }
    var field by remember(formKey) { mutableStateOf("") }
    var duration by remember(formKey) { mutableStateOf("90") }
    var objective by remember(formKey) { mutableStateOf("") }
    val exercises = remember(formKey) { mutableStateListOf<TrainingExercise>() }
    var saving by remember { mutableStateOf(false) }
    var saveMessage by remember { mutableStateOf<String?>(null) }
    var savingExercise by remember { mutableStateOf(false) }
    var newExerciseName by remember { mutableStateOf("") }
    var newExerciseDescription by remember { mutableStateOf("") }
    var newExerciseCategory by remember { mutableStateOf("") }
    var newExerciseDuration by remember { mutableStateOf("15") }
    var libraryError by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    DisposableEffect(Unit) {
        val stopObserving = TrainingRepository.observeDirectory { directory = it }
        onDispose { stopObserving() }
    }

    val selectedSession = directory.sessions.firstOrNull { it.id == selectedSessionId }
    val selectedTeam = directory.teams.firstOrNull { it.id == selectedTeamId }

    LaunchedEffect(directory.sessions, formKey) {
        if (selectedSessionId.isBlank() && directory.sessions.isNotEmpty() && formKey == 0) {
            val session = directory.sessions.first()
            selectedSessionId = session.id
            selectedTeamId = session.teamId
            date = session.date
            time = session.time
            field = session.field
            duration = session.durationMinutes.toString()
            objective = session.objective
            exercises.clear()
            exercises.addAll(session.exercises)
            onSessionSelected(session)
        }
        if (selectedTeamId.isBlank() && directory.teams.isNotEmpty()) {
            selectedTeamId = directory.teams.first().id
        }
    }

    Eyebrow(directory.academyName.ifBlank { "ACADEMIA" }.uppercase())
    SectionTitle("Sesión de entrenamiento")
    if (directory.sessions.isNotEmpty()) {
        SecondaryAction(
            "Sesión: ${selectedSession?.date ?: "Seleccionar guardada"} · ${selectedSession?.teamName ?: "Elegir"}",
            onClick = { showSessionPicker = true }
        )
    }
    if (directory.canManage && directory.academyId.isNotBlank()) {
        SecondaryAction("＋ Crear una nueva sesión", onClick = {
            selectedSessionId = ""
            selectedTeamId = directory.teams.firstOrNull()?.id.orEmpty()
            formKey++
            saveMessage = null
            date = today()
            time = "17:00"
            field = ""
            duration = "90"
            objective = ""
            exercises.clear()
        })
    }

    directory.error?.let { Notice(it, warning = true) }
    if (directory.loading) Notice("Cargando categorías, sesiones y biblioteca…")
    if (directory.academyId.isBlank() && !directory.loading) {
        Notice("Asocia tu usuario a una academia para crear y consultar sesiones.", warning = true)
    }
    if (!directory.canManage && directory.academyId.isNotBlank()) {
        Notice("Tu rol puede consultar, pero solo Entrenador o Administrador puede guardar sesiones.")
    }

    SectionTitle("Datos de la sesión")
    MockCard {
        OutlinedTextField(
            value = date,
            onValueChange = { date = it },
            label = { Text("Fecha (AAAA-MM-DD)") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = time,
            onValueChange = { time = it },
            label = { Text("Hora (HH:MM)") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        TextButton(
            onClick = { showTeamPicker = true },
            enabled = directory.teams.isNotEmpty()
        ) {
            Text("Categoría: ${selectedTeam?.name ?: "Seleccionar categoría"}")
        }
        OutlinedTextField(
            value = field,
            onValueChange = { field = it },
            label = { Text("Cancha") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = duration,
            onValueChange = { duration = it.filter(Char::isDigit).take(3) },
            label = { Text("Duración total (minutos)") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )
    }

    SectionTitle("Objetivo principal")
    OutlinedTextField(
        value = objective,
        onValueChange = { objective = it },
        label = { Text("¿Qué se busca mejorar en esta sesión?") },
        modifier = Modifier.fillMaxWidth(),
        minLines = 2
    )

    SectionTitle("Ejercicios", "＋ Biblioteca")
    SecondaryAction("Agregar ejercicio desde la biblioteca", onClick = {
        libraryError = null
        showLibrary = true
    })
    if (exercises.isEmpty()) {
        Notice("Agrega ejercicios desde la biblioteca para completar el plan.")
    } else {
        MockCard {
            exercises.forEachIndexed { index, exercise ->
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(exercise.name, fontWeight = FontWeight.SemiBold)
                            if (exercise.description.isNotBlank()) Body(exercise.description)
                            if (exercise.category.isNotBlank()) Body(exercise.category)
                        }
                        TextButton(onClick = { exercises.removeAt(index) }) { Text("Quitar") }
                    }
                    OutlinedTextField(
                        value = exercise.durationMinutes.toString(),
                        onValueChange = { value ->
                            val minutes = value.filter(Char::isDigit).take(3).toIntOrNull() ?: 0
                            exercises[index] = exercise.copy(durationMinutes = minutes)
                        },
                        label = { Text("Duración (min)") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                if (index < exercises.lastIndex) SoftDivider()
            }
        }
    }
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        MockCard(modifier = Modifier.weight(1f)) {
            Metric(exercises.size.toString(), "ejercicios")
        }
        MockCard(modifier = Modifier.weight(1f)) {
            Metric("${exercises.sumOf { it.durationMinutes }}′", "planificados")
        }
    }

    saveMessage?.let { Body(it, if (it.startsWith("No se pudo")) Rose else Forest) }
    PrimaryAction(
        text = if (saving) "Guardando sesión…" else "Guardar sesión",
        enabled = directory.canManage && directory.academyId.isNotBlank() &&
            selectedTeam != null && !saving,
        onClick = {
            val totalMinutes = duration.toIntOrNull()
            if (totalMinutes == null || totalMinutes <= 0) {
                saveMessage = "No se pudo guardar: ingresa una duración total válida."
            } else {
                saving = true
                saveMessage = "Guardando cambios…"
                val draft = TrainingSession(
                    id = selectedSessionId,
                    date = date,
                    time = time,
                    teamId = selectedTeam?.id.orEmpty(),
                    teamName = selectedTeam?.name.orEmpty(),
                    field = field,
                    durationMinutes = totalMinutes,
                    objective = objective,
                    exercises = exercises.toList()
                )
                scope.launch {
                    val result = TrainingRepository.saveSession(draft)
                    saving = false
                    if (result.isSuccess) {
                        val saved = draft.copy(id = result.getOrThrow())
                        selectedSessionId = saved.id
                        saveMessage = "Sesión guardada correctamente."
                        onSessionSelected(saved)
                    } else {
                        saveMessage = "No se pudo guardar: ${
                            result.exceptionOrNull()?.localizedMessage ?: "error inesperado"
                        }"
                    }
                }
            }
        }
    )
    PrimaryAction(
        text = "Registrar asistencia",
        enabled = selectedSession != null || selectedSessionId.isBlank() && date.isNotBlank() && selectedTeam != null,
        onClick = {
            val session = selectedSession ?: TrainingSession(
                id = selectedSessionId,
                date = date,
                time = time,
                teamId = selectedTeam?.id.orEmpty(),
                teamName = selectedTeam?.name.orEmpty(),
                field = field,
                durationMinutes = duration.toIntOrNull() ?: 0,
                objective = objective,
                exercises = exercises.toList()
            )
            onSessionSelected(session)
            onNavigate(9)
        }
    )

    if (showSessionPicker) {
        AlertDialog(
            onDismissRequest = { showSessionPicker = false },
            title = { Text("Sesiones guardadas") },
            text = {
                Column {
                    directory.sessions.forEach { session ->
                        TextButton(
                            onClick = {
                                selectedSessionId = session.id
                                selectedTeamId = session.teamId
                                date = session.date
                                time = session.time
                                field = session.field
                                duration = session.durationMinutes.toString()
                                objective = session.objective
                                exercises.clear()
                                exercises.addAll(session.exercises)
                                onSessionSelected(session)
                                showSessionPicker = false
                            }
                        ) {
                            Text("${session.date} · ${session.teamName} · ${session.objective}")
                        }
                    }
                }
            },
            confirmButton = { TextButton(onClick = { showSessionPicker = false }) { Text("Cerrar") }
            }
        )
    }

    if (showTeamPicker) {
        ChoiceDialog(
            title = "Seleccionar categoría",
            options = directory.teams.map { it.id to it.name },
            onSelect = { selectedTeamId = it; showTeamPicker = false },
            onDismiss = { showTeamPicker = false }
        )
    }

    if (showLibrary) {
        AlertDialog(
            onDismissRequest = { showLibrary = false },
            title = { Text("Biblioteca de ejercicios") },
            text = {
                Column {
                    libraryError?.let { Body(it, Rose) }
                    if (directory.library.isEmpty()) {
                        Body("La biblioteca de esta academia aún está vacía.")
                    }
                    directory.library.forEach { exercise ->
                        TextButton(
                            onClick = {
                                exercises.add(exercise)
                                showLibrary = false
                            }
                        ) {
                            Text(
                                listOf(
                                    exercise.name,
                                    exercise.durationMinutes.takeIf { it > 0 }?.let { "$it min" },
                                    exercise.category.takeIf(String::isNotBlank)
                                ).filterNotNull().joinToString(" · ")
                            )
                        }
                    }
                    TextButton(onClick = { showNewExercise = true }) {
                        Text("＋ Crear ejercicio reutilizable")
                    }
                }
            },
            confirmButton = { TextButton(onClick = { showLibrary = false }) { Text("Cerrar") } }
        )
    }

    if (showNewExercise) {
        AlertDialog(
            onDismissRequest = { if (!savingExercise) showNewExercise = false },
            title = { Text("Nuevo ejercicio de biblioteca") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = newExerciseName,
                        onValueChange = { newExerciseName = it },
                        label = { Text("Nombre") },
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = newExerciseDescription,
                        onValueChange = { newExerciseDescription = it },
                        label = { Text("Descripción") },
                        minLines = 2
                    )
                    OutlinedTextField(
                        value = newExerciseCategory,
                        onValueChange = { newExerciseCategory = it },
                        label = { Text("Tipo o categoría") },
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = newExerciseDuration,
                        onValueChange = { newExerciseDuration = it.filter(Char::isDigit).take(3) },
                        label = { Text("Duración (minutos)") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                }
            },
            confirmButton = {
                Button(
                    enabled = !savingExercise && directory.canManage &&
                        newExerciseName.isNotBlank() && (newExerciseDuration.toIntOrNull() ?: 0) > 0,
                    onClick = {
                        savingExercise = true
                        scope.launch {
                            val result = TrainingRepository.createLibraryExercise(
                                TrainingExercise(
                                    id = "",
                                    name = newExerciseName,
                                    description = newExerciseDescription,
                                    category = newExerciseCategory,
                                    durationMinutes = newExerciseDuration.toIntOrNull() ?: 0
                                )
                            )
                            savingExercise = false
                            if (result.isSuccess) {
                                newExerciseName = ""
                                newExerciseDescription = ""
                                newExerciseCategory = ""
                                newExerciseDuration = "15"
                                showNewExercise = false
                            } else {
                                libraryError = result.exceptionOrNull()?.localizedMessage
                                    ?: "No se pudo guardar el ejercicio."
                            }
                        }
                    }
                ) { Text(if (savingExercise) "Guardando…" else "Guardar en biblioteca") }
            },
            dismissButton = {
                TextButton(enabled = !savingExercise, onClick = { showNewExercise = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

// =====================================================
// US-009: REGISTRAR ASISTENCIA AL ENTRENAMIENTO
// =====================================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ChoiceDialog(
    title: String,
    options: List<Pair<String, String>>,
    onSelect: (String) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column {
                options.forEach { (id, name) ->
                    TextButton(onClick = { onSelect(id) }) { Text(name) }
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Cerrar") } }
    )
}

@Composable
private fun Attendance(session: TrainingSession?) {
    Eyebrow(
        session?.let { "${it.date} · ${it.time} · ${it.teamName}" }
            ?: "CONTROL DE ASISTENCIA"
    )
    SectionTitle("Control de asistencia", "Editar")
    session?.let {
        Body("${it.field} · Sesión de ${it.durationMinutes} min")
    } ?: Notice("Abre el registro desde una sesión de entrenamiento para asociar la asistencia.", warning = true)
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        MockCard(modifier = Modifier.weight(1f)) { Metric("14", "presentes") }
        MockCard(modifier = Modifier.weight(1f)) { Metric("2", "tarde") }
        MockCard(modifier = Modifier.weight(1f)) { Metric("2", "ausentes") }
    }
    Notice("Marca un estado por jugador. Puedes corregirlo antes de guardar.")
    MockCard {
        listOf(
            Triple("1", "Mateo Rivas", "Presente"),
            Triple("4", "Diego Torres", "Presente"),
            Triple("8", "Andrés Flores", "Tarde"),
            Triple("9", "Thiago Ramos", "Ausente"),
            Triple("10", "Nicolás Paredes", "Presente"),
            Triple("11", "Gabriel Soto", "Tarde"),
            Triple("15", "Bruno León", "Ausente")
        ).forEach { (number, name, state) ->
            PlayerRow(number, name, session?.teamName ?: "Equipo", state, toneForState(state))
        }
    }
    Body("El registro de asistencia se gestiona desde esta sesión.")
    PrimaryAction("Guardar asistencia", enabled = session != null)
}

private fun today(): String = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
