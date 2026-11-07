
package com.example.sportprog3.presentation.training

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

import com.example.sportprog3.presentation.components.Badge
import com.example.sportprog3.presentation.components.Body
import com.example.sportprog3.presentation.components.Eyebrow
import com.example.sportprog3.presentation.components.Metric
import com.example.sportprog3.presentation.components.MockCard
import com.example.sportprog3.presentation.components.Notice
import com.example.sportprog3.presentation.components.PlayerRow
import com.example.sportprog3.presentation.components.PrimaryAction
import com.example.sportprog3.presentation.components.ScreenFrame
import com.example.sportprog3.presentation.components.SectionTitle
import com.example.sportprog3.presentation.components.SoftDivider
import com.example.sportprog3.presentation.components.toneForState

import com.example.sportprog3.ui.theme.Forest
import com.example.sportprog3.ui.theme.Mint

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
fun TrainingScreens(screen: Int) {
    ScreenFrame {
        if (screen == 8) {
            TrainingPlan()
        } else {
            Attendance()
        }
    }
}

// =====================================================
// US-008: PLAN DE ENTRENAMIENTO
// SE CONSERVA LA PANTALLA DEL COMPAÑERO
// =====================================================

@Composable
private fun TrainingPlan() {
    Eyebrow("SÁBADO · 19 SEPTIEMBRE")

    SectionTitle("Sesión de entrenamiento", "Borrador")

    MockCard(containerColor = Forest) {
        Text(
            "Sub-15 A  ·  Cancha principal",
            color = Color.White,
            fontWeight = FontWeight.Bold
        )

        Body(
            "17:00–18:30  ·  Duración 90 min",
            Color.White.copy(alpha = .8f)
        )

        Badge(
            "RESISTENCIA + DEFINICIÓN",
            color = Mint
        )
    }

    SectionTitle("Objetivo de la sesión")

    MockCard {
        Text(
            "Mejorar transición rápida y definición en el último tercio.",
            style = MaterialTheme.typography.bodyLarge
        )
    }

    SectionTitle("Bloques de trabajo", "＋ Biblioteca")

    MockCard {
        PlayerRow(
            "15'",
            "Activación dinámica",
            "Movilidad y rondos de posesión",
            "Calentamiento"
        )

        SoftDivider()

        PlayerRow(
            "25'",
            "Rondo de transición",
            "6 vs 3 · presión tras pérdida",
            "Técnica"
        )

        SoftDivider()

        PlayerRow(
            "35'",
            "Ataque 4 vs 4 + porteros",
            "Finalización en zona ofensiva",
            "Táctica"
        )

        SoftDivider()

        PlayerRow(
            "15'",
            "Vuelta a la calma",
            "Estiramiento y feedback grupal",
            "Cierre"
        )
    }

    Row(
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        MockCard(modifier = Modifier.weight(1f)) {
            Metric("4", "ejercicios")
        }

        MockCard(modifier = Modifier.weight(1f)) {
            Metric("90'", "duración")
        }
    }

    PrimaryAction("Guardar sesión")
    Badge("TOMAR ASISTENCIA  →")
}

// =====================================================
// US-009: REGISTRAR ASISTENCIA AL ENTRENAMIENTO
// =====================================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun Attendance() {

    val firestore = remember {
        FirebaseFirestore.getInstance()
    }

    val academiaId = "academia-demo"

    val uid = FirebaseAuth.getInstance().currentUser?.uid

    // -------------------------------------------------
    // ESTADO DE EQUIPOS
    // -------------------------------------------------

    var equipos by remember {
        mutableStateOf<List<EquipoAsistencia>>(emptyList())
    }

    var equipoSeleccionadoId by remember {
        mutableStateOf<String?>(null)
    }

    var cargandoEquipos by remember {
        mutableStateOf(true)
    }

    var errorEquipos by remember {
        mutableStateOf<String?>(null)
    }

    var menuEquiposAbierto by remember {
        mutableStateOf(false)
    }

    // -------------------------------------------------
    // ESTADO DE SESIONES
    // -------------------------------------------------

    var sesiones by remember {
        mutableStateOf<List<SesionAsistencia>>(emptyList())
    }

    var sesionSeleccionadaId by remember {
        mutableStateOf<String?>(null)
    }

    var cargandoSesiones by remember {
        mutableStateOf(false)
    }

    var errorSesiones by remember {
        mutableStateOf<String?>(null)
    }

    var menuSesionesAbierto by remember {
        mutableStateOf(false)
    }

    // -------------------------------------------------
    // ESTADO DE JUGADORES Y ASISTENCIAS
    // -------------------------------------------------

    var jugadores by remember {
        mutableStateOf<List<JugadorAsistencia>>(emptyList())
    }

    var cargandoJugadores by remember {
        mutableStateOf(false)
    }

    var errorJugadores by remember {
        mutableStateOf<String?>(null)
    }

    var asistenciasGuardadas by remember {
        mutableStateOf<Map<String, String>>(emptyMap())
    }

    var asistenciasEditadas by remember {
        mutableStateOf<Map<String, String>>(emptyMap())
    }

    var cargandoAsistencias by remember {
        mutableStateOf(false)
    }

    var errorAsistencias by remember {
        mutableStateOf<String?>(null)
    }

    var guardando by remember {
        mutableStateOf(false)
    }

    var mensajeGuardado by remember {
        mutableStateOf<String?>(null)
    }

    val equipoActual = equipos.firstOrNull {
        it.id == equipoSeleccionadoId
    }

    val sesionActual = sesiones.firstOrNull {
        it.id == sesionSeleccionadaId
    }

    // =================================================
    // 1. CONSULTAR EQUIPOS ASIGNADOS
    // =================================================

    DisposableEffect(firestore, academiaId, uid) {

        var activo = true
        var revisionConsulta = 0

        var equiposRegistro: ListenerRegistration? = null

        val academiaRef = firestore
            .collection("academias")
            .document(academiaId)

        val equiposRef = academiaRef.collection("equipos")

        fun actualizarEquipos(
            nuevos: List<EquipoAsistencia>,
            error: String?
        ) {
            if (!activo) return

            val ordenados = nuevos
                .distinctBy { it.id }
                .sortedWith(
                    compareBy(
                        { it.nombre },
                        { it.categoria },
                        { it.division }
                    )
                )

            equipos = ordenados
            errorEquipos = error
            cargandoEquipos = false

            if (ordenados.none { it.id == equipoSeleccionadoId }) {
                equipoSeleccionadoId = ordenados.firstOrNull()?.id
            }
        }

        if (uid == null) {

            actualizarEquipos(
                emptyList(),
                "Debes iniciar sesión."
            )

            onDispose {
                activo = false
            }

        } else {

            val miembroRegistro = academiaRef
                .collection("miembros")
                .document(uid)
                .addSnapshotListener miembro@{ miembro, error ->

                    if (!activo) return@miembro

                    revisionConsulta++
                    val version = revisionConsulta

                    equiposRegistro?.remove()
                    equiposRegistro = null

                    cargandoEquipos = true
                    errorEquipos = null
                    equipos = emptyList()
                    equipoSeleccionadoId = null

                    if (
                        error != null ||
                        miembro == null ||
                        !miembro.exists()
                    ) {
                        actualizarEquipos(
                            emptyList(),
                            "No se pudo validar tu membresía."
                        )
                        return@miembro
                    }

                    if (
                        miembro.getBoolean("activo") != true ||
                        miembro.getString("estadoRol") != "APROBADO"
                    ) {
                        actualizarEquipos(
                            emptyList(),
                            "Tu membresía no está activa y aprobada."
                        )
                        return@miembro
                    }

                    if (miembro.getString("rol") == "Administrador") {

                        equiposRegistro = equiposRef
                            .whereEqualTo("estado", "ACTIVO")
                            .addSnapshotListener consulta@{
                                    snapshot, fallo ->

                                if (!activo || version != revisionConsulta) {
                                    return@consulta
                                }

                                if (fallo != null) {
                                    actualizarEquipos(
                                        emptyList(),
                                        "Sin autorización para consultar equipos."
                                    )
                                } else if (snapshot != null) {
                                    actualizarEquipos(
                                        snapshot.documents.mapNotNull {
                                            it.convertirEquipo()
                                        },
                                        null
                                    )
                                }
                            }

                    } else {

                        val ids = (miembro.get("equipoIds") as? List<*>)
                            ?.filterIsInstance<String>()
                            ?.distinct()
                            .orEmpty()

                        if (ids.isEmpty()) {

                            actualizarEquipos(
                                emptyList(),
                                "No tienes equipos asignados."
                            )

                        } else {

                            val encontrados =
                                mutableListOf<EquipoAsistencia>()

                            var pendientes = ids.size

                            ids.forEach { equipoId ->

                                equiposRef.document(equipoId)
                                    .get()
                                    .addOnCompleteListener consulta@{ tarea ->

                                        if (
                                            !activo ||
                                            version != revisionConsulta
                                        ) {
                                            return@consulta
                                        }

                                        if (tarea.isSuccessful) {
                                            tarea.result
                                                ?.convertirEquipo()
                                                ?.let {
                                                    encontrados.add(it)
                                                }
                                        }

                                        pendientes--

                                        if (pendientes == 0) {
                                            actualizarEquipos(
                                                encontrados,
                                                if (encontrados.isEmpty()) {
                                                    "No hay equipos autorizados."
                                                } else {
                                                    null
                                                }
                                            )
                                        }
                                    }
                            }
                        }
                    }
                }

            onDispose {
                activo = false
                revisionConsulta++
                miembroRegistro.remove()
                equiposRegistro?.remove()
            }
        }
    }

    // =================================================
    // 2. CONSULTAR ENTRENAMIENTOS DEL EQUIPO
    // =================================================

    DisposableEffect(
        firestore,
        academiaId,
        equipoSeleccionadoId
    ) {

        sesiones = emptyList()
        sesionSeleccionadaId = null
        errorSesiones = null

        val equipoId = equipoSeleccionadoId

        if (equipoId == null) {

            cargandoSesiones = false
            onDispose {}

        } else {

            cargandoSesiones = true

            val registro = firestore
                .collection("academias")
                .document(academiaId)
                .collection("equipos")
                .document(equipoId)
                .collection("entrenamientos")
                .addSnapshotListener { snapshot, error ->

                    if (error != null) {

                        errorSesiones =
                            "No se pudieron consultar los entrenamientos: " +
                                    (error.localizedMessage ?: "Sin permisos")

                        sesiones = emptyList()
                        cargandoSesiones = false

                    } else if (snapshot != null) {

                        val nuevas = snapshot.documents
                            .map { it.convertirSesion() }
                            .sortedByDescending {
                                it.fecha?.seconds ?: 0L
                            }

                        sesiones = nuevas
                        errorSesiones = null
                        cargandoSesiones = false

                        if (
                            nuevas.none { it.id == sesionSeleccionadaId }
                        ) {
                            sesionSeleccionadaId =
                                nuevas.firstOrNull()?.id
                        }
                    }
                }

            onDispose {
                registro.remove()
            }
        }
    }

    // =================================================
    // 3. CONSULTAR JUGADORES DE LA SESIÓN
    // =================================================

    val idsSesion = sesionActual?.membresiaIds.orEmpty()

    DisposableEffect(
        firestore,
        academiaId,
        equipoSeleccionadoId,
        sesionSeleccionadaId,
        idsSesion
    ) {

        jugadores = emptyList()
        errorJugadores = null

        val equipoId = equipoSeleccionadoId

        if (
            equipoId == null ||
            sesionSeleccionadaId == null
        ) {

            cargandoJugadores = false
            onDispose {}

        } else if (idsSesion.isEmpty()) {

            cargandoJugadores = false
            errorJugadores =
                "Esta sesión no tiene membresías de jugadores registradas."

            onDispose {}

        } else {

            var activo = true
            cargandoJugadores = true

            val encontrados = mutableListOf<JugadorAsistencia>()
            var pendientes = idsSesion.size
            var fallidos = 0

            val membresiasRef = firestore
                .collection("academias")
                .document(academiaId)
                .collection("equipos")
                .document(equipoId)
                .collection("membresiasEquipo")

            idsSesion.forEach { membresiaId ->

                membresiasRef.document(membresiaId)
                    .get()
                    .addOnCompleteListener { tarea ->

                        if (!activo) {
                            return@addOnCompleteListener
                        }

                        val documento = if (tarea.isSuccessful) {
                            tarea.result
                        } else {
                            null
                        }

                        if (
                            documento != null &&
                            documento.exists()
                        ) {

                            encontrados.add(
                                JugadorAsistencia(
                                    membresiaId = membresiaId,

                                    jugadorId = documento
                                        .getString("jugadorId")
                                        ?: membresiaId,

                                    jugadorUid = documento
                                        .getString("jugadorUid"),

                                    dorsal = documento
                                        .getLong("dorsal")
                                        ?.toInt() ?: 0,

                                    nombre = documento
                                        .getString("nombreDeportivo")
                                        ?: "Jugador sin nombre",

                                    posicion = documento
                                        .getString("posicion")
                                        ?: "Sin posición"
                                )
                            )

                        } else {
                            fallidos++
                        }

                        pendientes--

                        if (pendientes == 0) {

                            jugadores = encontrados.sortedWith(
                                compareBy(
                                    { it.dorsal },
                                    { it.nombre }
                                )
                            )

                            if (fallidos > 0) {
                                errorJugadores =
                                    "No se pudieron consultar todas " +
                                            "las membresías de esta sesión."
                            }

                            cargandoJugadores = false
                        }
                    }
            }

            onDispose {
                activo = false
            }
        }
    }

    // =================================================
    // 4. LEER ASISTENCIAS EXISTENTES
    // =================================================

    DisposableEffect(
        firestore,
        academiaId,
        equipoSeleccionadoId,
        sesionSeleccionadaId
    ) {

        asistenciasGuardadas = emptyMap()
        asistenciasEditadas = emptyMap()
        errorAsistencias = null
        mensajeGuardado = null

        val equipoId = equipoSeleccionadoId
        val sesionId = sesionSeleccionadaId

        if (equipoId == null || sesionId == null) {

            cargandoAsistencias = false
            onDispose {}

        } else {

            cargandoAsistencias = true

            val registro = firestore
                .collection("academias")
                .document(academiaId)
                .collection("equipos")
                .document(equipoId)
                .collection("entrenamientos")
                .document(sesionId)
                .collection("asistencias")
                .addSnapshotListener { snapshot, error ->

                    if (error != null) {

                        errorAsistencias =
                            "No se pudieron leer las asistencias: " +
                                    (error.localizedMessage ?: "Sin permisos")

                        cargandoAsistencias = false

                    } else if (snapshot != null) {

                        val guardadas = snapshot.documents
                            .mapNotNull { documento ->

                                val estado = documento.getString("estado")

                                if (estado in ESTADOS_ASISTENCIA) {
                                    documento.id to estado!!
                                } else {
                                    null
                                }
                            }
                            .toMap()

                        asistenciasGuardadas = guardadas

                        // No se reemplazan cambios pendientes
                        // si el usuario ya está editando.
                        if (asistenciasEditadas.isEmpty()) {
                            asistenciasEditadas = guardadas
                        }

                        errorAsistencias = null
                        cargandoAsistencias = false
                    }
                }

            onDispose {
                registro.remove()
            }
        }
    }

    // =================================================
    // 5. CÁLCULOS Y VALIDACIONES
    // =================================================

    val totalPresentes = jugadores.count {
        asistenciasEditadas[it.membresiaId] == "Presente"
    }

    val totalTardes = jugadores.count {
        asistenciasEditadas[it.membresiaId] == "Tarde"
    }

    val totalAusentes = jugadores.count {
        asistenciasEditadas[it.membresiaId] == "Ausente"
    }

    val totalMarcados =
        totalPresentes + totalTardes + totalAusentes

    val todosMarcados =
        jugadores.isNotEmpty() &&
                totalMarcados == jugadores.size

    val hayCambios =
        asistenciasEditadas != asistenciasGuardadas

    val sesionEditable =
        sesionActual != null &&
                sesionActual.estado == "REALIZADA" &&
                !sesionActual.confirmada

    val datosCompletos =
        !cargandoEquipos &&
                !cargandoSesiones &&
                !cargandoJugadores &&
                !cargandoAsistencias &&
                errorEquipos == null &&
                errorSesiones == null &&
                errorJugadores == null &&
                errorAsistencias == null

    // =================================================
    // 6. GUARDAR ASISTENCIA REAL
    // =================================================

    fun guardarAsistencia() {

        val equipo = equipoActual
        val sesion = sesionActual
        val entrenadorUid = uid

        if (
            equipo == null ||
            sesion == null ||
            entrenadorUid == null ||
            !datosCompletos ||
            !sesionEditable ||
            !todosMarcados ||
            guardando
        ) {
            mensajeGuardado =
                "No se puede guardar. Comprueba la sesión, " +
                        "los permisos y todos los estados."
            return
        }

        // VALIDACIÓN 1: membresías duplicadas.

        val idsJugadores = jugadores.map {
            it.membresiaId
        }

        if (
            idsJugadores.isEmpty() ||
            idsJugadores.size != idsJugadores.toSet().size
        ) {
            mensajeGuardado =
                "Hay jugadores duplicados o no hay jugadores en la sesión."
            return
        }

        // VALIDACIÓN 2: estados permitidos.

        val estadosAEnviar = jugadores.associate { jugador ->
            jugador.membresiaId to
                    (asistenciasEditadas[jugador.membresiaId] ?: "")
        }

        if (
            estadosAEnviar.size != jugadores.size ||
            estadosAEnviar.values.any {
                it !in ESTADOS_ASISTENCIA
            }
        ) {
            mensajeGuardado =
                "Hay estados de asistencia inválidos."
            return
        }

        val entrenamientoRef = firestore
            .collection("academias")
            .document(academiaId)
            .collection("equipos")
            .document(equipo.id)
            .collection("entrenamientos")
            .document(sesion.id)

        guardando = true
        mensajeGuardado = null

        // Transacción: la sesión y sus asistencias
        // deben quedar consistentes o no cambiar nada.

        firestore.runTransaction { transaccion ->

            val sesionServidor = transaccion.get(entrenamientoRef)

            if (!sesionServidor.exists()) {
                throw IllegalStateException(
                    "El entrenamiento ya no existe."
                )
            }

            val estadoServidor =
                sesionServidor.getString("estado")

            val confirmadaServidor =
                sesionServidor.getBoolean("asistenciaConfirmada") == true

            val revisionServidor =
                sesionServidor.getLong("revision") ?: 0L

            if (
                estadoServidor != "REALIZADA" ||
                confirmadaServidor
            ) {
                throw IllegalStateException(
                    "La sesión está confirmada o no permite registro."
                )
            }

            if (revisionServidor != sesion.revision) {
                throw IllegalStateException(
                    "La sesión fue modificada. Actualiza los datos."
                )
            }

            val idsServidor = (
                    sesionServidor.get("membresiaIdsSesion") as? List<*>
                    )
                ?.filterIsInstance<String>()
                ?.toSet()
                .orEmpty()

            if (
                idsServidor != estadosAEnviar.keys ||
                estadosAEnviar.values.any {
                    it !in ESTADOS_ASISTENCIA
                }
            ) {
                throw IllegalStateException(
                    "El listado de asistencia no coincide con la sesión."
                )
            }

            val referencias = jugadores.associate { jugador ->
                jugador.membresiaId to entrenamientoRef
                    .collection("asistencias")
                    .document(jugador.membresiaId)
            }

            // Todas las lecturas se realizan antes
            // de la primera escritura de la transacción.
            val documentosActuales = referencias.mapValues {
                    (_, referencia) ->
                transaccion.get(referencia)
            }

            val nuevaRevision = revisionServidor + 1L

            jugadores.forEach { jugador ->

                val referencia = referencias.getValue(
                    jugador.membresiaId
                )

                val documentoActual = documentosActuales.getValue(
                    jugador.membresiaId
                )

                val datos = mutableMapOf<String, Any?>(
                    "academiaId" to academiaId,
                    "equipoId" to equipo.id,
                    "entrenamientoId" to sesion.id,
                    "membresiaId" to jugador.membresiaId,
                    "jugadorId" to jugador.jugadorId,
                    "jugadorUid" to jugador.jugadorUid,
                    "estado" to estadosAEnviar.getValue(
                        jugador.membresiaId
                    ),
                    "revision" to nuevaRevision,
                    "editadoPor" to entrenadorUid,
                    "editadoEn" to FieldValue.serverTimestamp()
                )

                if (!documentoActual.exists()) {
                    datos["creadoPor"] = entrenadorUid
                    datos["creadoEn"] =
                        FieldValue.serverTimestamp()

                    transaccion.set(referencia, datos)

                } else {
                    // Conservar los campos de auditoría
                    // originales del registro existente.
                    transaccion.set(
                        referencia,
                        datos,
                        SetOptions.merge()
                    )
                }
            }

            // RESUMEN: contar los mismos estados que se guardan.

            val resumen = mapOf(
                "Presente" to estadosAEnviar.values.count {
                    it == "Presente"
                },
                "Tarde" to estadosAEnviar.values.count {
                    it == "Tarde"
                },
                "Ausente" to estadosAEnviar.values.count {
                    it == "Ausente"
                }
            )

            transaccion.update(
                entrenamientoRef,
                mapOf(
                    "resumenAsistencia" to resumen,
                    "asistenciaConfirmada" to true,
                    "revision" to nuevaRevision,
                    "editadoPor" to entrenadorUid,
                    "editadoEn" to FieldValue.serverTimestamp()
                )
            )

            true

        }.addOnSuccessListener {

            guardando = false

            mensajeGuardado =
                "Asistencia guardada y confirmada correctamente."

        }.addOnFailureListener { error ->

            guardando = false

            mensajeGuardado =
                "No se pudo guardar en Firebase: " +
                        (error.localizedMessage ?: "Error desconocido")
        }
    }

    // =================================================
    // 7. INTERFAZ DE US-009
    // =================================================

    Eyebrow("ENTRENAMIENTO · CONTROL DE ASISTENCIA")

    SectionTitle("Control de asistencia")

    Body(
        "Selecciona una sesión y registra la participación " +
                "de sus jugadores."
    )

    // -------------------------------------------------
    // SELECTOR DE EQUIPO
    // -------------------------------------------------

    if (cargandoEquipos) {

        Body("Cargando equipos autorizados...")

    } else if (errorEquipos != null) {

        Notice(errorEquipos!!, warning = true)

    } else if (equipos.isEmpty()) {

        Notice(
            "No tienes equipos activos asignados.",
            warning = true
        )

    } else {

        ExposedDropdownMenuBox(
            expanded = menuEquiposAbierto,
            onExpandedChange = {
                menuEquiposAbierto = !menuEquiposAbierto
            }
        ) {

            OutlinedTextField(
                value = equipoActual?.etiqueta
                    ?: "Selecciona un equipo",
                onValueChange = {},
                readOnly = true,
                label = {
                    Text("Equipo")
                },
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(
                        expanded = menuEquiposAbierto
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor()
            )

            ExposedDropdownMenu(
                expanded = menuEquiposAbierto,
                onDismissRequest = {
                    menuEquiposAbierto = false
                }
            ) {

                equipos.forEach { equipo ->

                    DropdownMenuItem(
                        text = {
                            Text(equipo.etiqueta)
                        },
                        onClick = {
                            equipoSeleccionadoId = equipo.id
                            menuEquiposAbierto = false
                            mensajeGuardado = null
                        }
                    )
                }
            }
        }
    }

    // -------------------------------------------------
    // SELECTOR DE ENTRENAMIENTO
    // -------------------------------------------------

    if (cargandoSesiones) {

        Body("Cargando sesiones del equipo...")

    } else if (errorSesiones != null) {

        Notice(
            errorSesiones!!,
            warning = true
        )

    } else if (
        equipoSeleccionadoId != null &&
        sesiones.isEmpty()
    ) {

        Notice("Este equipo no tiene sesiones registradas.")

    } else if (sesiones.isNotEmpty()) {

        ExposedDropdownMenuBox(
            expanded = menuSesionesAbierto,
            onExpandedChange = {
                menuSesionesAbierto = !menuSesionesAbierto
            }
        ) {

            OutlinedTextField(
                value = sesionActual?.let {
                    "${formatoFecha(it.fecha)} · ${it.estado}"
                } ?: "Selecciona una sesión",

                onValueChange = {},
                readOnly = true,
                label = {
                    Text("Sesión de entrenamiento")
                },
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(
                        expanded = menuSesionesAbierto
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor()
            )

            ExposedDropdownMenu(
                expanded = menuSesionesAbierto,
                onDismissRequest = {
                    menuSesionesAbierto = false
                }
            ) {

                sesiones.forEach { sesion ->

                    DropdownMenuItem(
                        text = {
                            Text(
                                "${formatoFecha(sesion.fecha)} · " +
                                        sesion.estado
                            )
                        },
                        onClick = {
                            sesionSeleccionadaId = sesion.id
                            menuSesionesAbierto = false
                            mensajeGuardado = null
                        }
                    )
                }
            }
        }
    }

    // -------------------------------------------------
    // DATOS REALES DE LA SESIÓN
    // -------------------------------------------------

    if (sesionActual != null && equipoActual != null) {

        MockCard(containerColor = Forest) {

            Text(
                equipoActual.etiqueta,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )

            Body(
                formatoFecha(sesionActual.fecha),
                Color.White.copy(alpha = .85f)
            )

            Body(
                "Lugar: ${sesionActual.ubicacion}",
                Color.White.copy(alpha = .85f)
            )

            Body(
                "Objetivo: ${sesionActual.objetivo}",
                Color.White.copy(alpha = .85f)
            )

            Badge(
                if (sesionActual.confirmada) {
                    "ASISTENCIA CONFIRMADA"
                } else {
                    "PENDIENTE DE CONFIRMACIÓN"
                },
                color = Mint
            )
        }

        if (sesionActual.confirmada) {

            Notice(
                "Esta asistencia ya fue confirmada. " +
                        "Se muestra en modo de consulta."
            )

        } else if (sesionActual.estado != "REALIZADA") {

            Notice(
                "El registro se habilita para sesiones realizadas.",
                warning = true
            )

        } else {

            Notice(
                "Marca Presente, Tarde o Ausente. " +
                        "Puedes corregir antes de guardar."
            )
        }
    }

    // -------------------------------------------------
    // CONTADORES AUTOMÁTICOS
    // -------------------------------------------------

    if (sesionActual != null) {

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {

            MockCard(modifier = Modifier.weight(1f)) {
                Metric(
                    totalPresentes.toString(),
                    "presentes"
                )
            }

            MockCard(modifier = Modifier.weight(1f)) {
                Metric(
                    totalTardes.toString(),
                    "tarde"
                )
            }

            MockCard(modifier = Modifier.weight(1f)) {
                Metric(
                    totalAusentes.toString(),
                    "ausentes"
                )
            }
        }

        Body(
            "$totalMarcados de ${jugadores.size} jugadores marcados"
        )
    }

    // -------------------------------------------------
    // LISTADO Y EDICIÓN
    // -------------------------------------------------

    if (cargandoJugadores || cargandoAsistencias) {

        Body("Cargando jugadores y asistencias...")

    } else if (errorJugadores != null) {

        Notice(
            errorJugadores!!,
            warning = true
        )

    } else if (errorAsistencias != null) {

        Notice(
            errorAsistencias!!,
            warning = true
        )

    } else if (
        sesionActual != null &&
        jugadores.isEmpty()
    ) {

        Notice(
            "La sesión no tiene jugadores disponibles.",
            warning = true
        )

    } else if (
        sesionActual != null &&
        jugadores.isNotEmpty()
    ) {

        SectionTitle("Jugadores de la sesión")

        jugadores.forEach { jugador ->

            val estado = asistenciasEditadas[
                jugador.membresiaId
            ]

            MockCard {

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {

                    Column(modifier = Modifier.weight(1f)) {

                        Text(
                            "${jugador.dorsal}. ${jugador.nombre}",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )

                        Body(jugador.posicion)
                    }

                    Badge(
                        estado ?: "Sin marcar",
                        color = toneForState(estado ?: "")
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {

                    ESTADOS_ASISTENCIA.forEach { opcion ->

                        FilterChip(
                            modifier = Modifier.weight(1f),
                            selected = estado == opcion,
                            enabled = sesionEditable && !guardando,
                            onClick = {
                                asistenciasEditadas =
                                    asistenciasEditadas.toMutableMap()
                                        .apply {
                                            put(
                                                jugador.membresiaId,
                                                opcion
                                            )
                                        }

                                mensajeGuardado = null
                            },
                            label = {
                                Text(
                                    opcion,
                                    style = MaterialTheme.typography.labelSmall
                                )
                            }
                        )
                    }
                }
            }
        }

        if (sesionEditable) {

            Body(
                if (hayCambios) {
                    "Hay cambios pendientes de guardar."
                } else {
                    "No hay cambios pendientes."
                }
            )

            Button(
                onClick = {
                    guardarAsistencia()
                },
                enabled =
                    datosCompletos &&
                            todosMarcados &&
                            !guardando,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    if (guardando) {
                        "Guardando asistencia..."
                    } else {
                        "Guardar asistencia"
                    }
                )
            }
        }
    }

    mensajeGuardado?.let { mensaje ->
        Notice(
            mensaje,
            warning = !mensaje.startsWith(
                "Asistencia guardada"
            )
        )
    }
}
