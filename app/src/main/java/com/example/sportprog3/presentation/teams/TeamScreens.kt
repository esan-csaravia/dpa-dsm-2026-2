
package com.example.sportprog3.presentation.teams

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import com.example.sportprog3.presentation.components.Avatar
import com.example.sportprog3.presentation.components.Badge
import com.example.sportprog3.presentation.components.Body
import com.example.sportprog3.presentation.components.Eyebrow
import com.example.sportprog3.presentation.components.MockCard
import com.example.sportprog3.presentation.components.Metric
import com.example.sportprog3.presentation.components.Notice
import com.example.sportprog3.presentation.components.PlayerRow
import com.example.sportprog3.presentation.components.ScreenFrame
import com.example.sportprog3.presentation.components.SectionTitle
import com.example.sportprog3.presentation.components.SoftDivider
import com.example.sportprog3.presentation.components.toneForState
import com.example.sportprog3.ui.theme.Forest
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration

// =====================================================
// US-005: MODELOS DE EQUIPOS Y JUGADORES
// =====================================================

private data class EquipoPlantel(
    val id: String,
    val nombre: String,
    val categoria: String,
    val division: String
) {
    val etiqueta: String
        get() = "$nombre · $categoria · División $division"
}

private data class JugadorPlantel(
    val id: String,
    val dorsal: Int,
    val nombre: String,
    val posicion: String,
    val estado: String
)

private fun DocumentSnapshot.aEquipoActivo(): EquipoPlantel? {
    if (!exists() || getString("estado") != "ACTIVO") {
        return null
    }

    return EquipoPlantel(
        id = id,
        nombre = getString("nombre") ?: id,
        categoria = getString("categoria") ?: "Sin categoría",
        division = getString("division") ?: "Sin división"
    )
}

// =====================================================
// NAVEGACIÓN ORIGINAL DEL MÓDULO
// =====================================================

@Composable
fun TeamScreens(
    screen: Int,
    onNavigate: (Int) -> Unit = {}
) {
    ScreenFrame {
        when (screen) {
            4 -> TeamsView()
            5 -> RosterView(onNavigate)
            6 -> PlayerProfileView()
            7 -> DuesView()
        }
    }
}

// =====================================================
// US-004: EQUIPOS Y CATEGORÍAS
// PANTALLA DEL COMPAÑERO: SIN CAMBIOS FUNCIONALES
// =====================================================

@Composable
private fun TeamsView() {
    Eyebrow("ACADEMIA LOS CEDROS")
    SectionTitle("Equipos y categorías", "＋ Nuevo")
    Body("6 categorías activas · temporada 2026")

    listOf(
        "Sub-10" to "22",
        "Sub-13" to "20",
        "Sub-15 A" to "18",
        "Sub-15 B" to "16",
        "Sub-17" to "19",
        "Primera" to "24"
    ).forEach { (name, total) ->
        MockCard {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Body("$total jugadores · Los Cedros FC")
                }
                Badge("Ver plantel  →")
            }
        }
    }
}

// =====================================================
// US-005: CONSULTAR PLANTEL DE JUGADORES
// EQUIPOS AUTORIZADOS DESDE FIRESTORE
// =====================================================

@OptIn(
    ExperimentalLayoutApi::class,
    ExperimentalMaterial3Api::class
)
@Composable
private fun RosterView(
    onNavigate: (Int) -> Unit
) {
    val firestore = remember {
        FirebaseFirestore.getInstance()
    }

    val academiaId = "academia-demo"

    val uid = FirebaseAuth.getInstance()
        .currentUser?.uid

    // -------------------------------------------------
    // ESTADOS DE EQUIPOS
    // -------------------------------------------------

    var equipos by remember {
        mutableStateOf<List<EquipoPlantel>>(emptyList())
    }

    var equipoSeleccionadoId by remember {
        mutableStateOf<String?>(null)
    }

    var cargandoEquipos by remember {
        mutableStateOf(true)
    }

    var avisoEquipos by remember {
        mutableStateOf<String?>(null)
    }

    var selectorAbierto by remember {
        mutableStateOf(false)
    }

    // -------------------------------------------------
    // ESTADOS DEL PLANTEL
    // -------------------------------------------------

    var jugadores by remember {
        mutableStateOf<List<JugadorPlantel>>(emptyList())
    }

    var cargandoJugadores by remember {
        mutableStateOf(false)
    }

    var errorJugadores by remember {
        mutableStateOf<String?>(null)
    }

    var busqueda by remember {
        mutableStateOf("")
    }

    var filtro by remember {
        mutableStateOf("Todos")
    }

    var jugadorSeleccionado by remember {
        mutableStateOf<JugadorPlantel?>(null)
    }

    val equipoActual = equipos.firstOrNull {
        it.id == equipoSeleccionadoId
    }

    // =================================================
    // 1. CONSULTAR LOS EQUIPOS AUTORIZADOS
    // =================================================

    DisposableEffect(firestore, academiaId, uid) {
        var activo = true
        var version = 0

        var equiposListener: ListenerRegistration? = null

        val academiaRef = firestore
            .collection("academias")
            .document(academiaId)

        val equiposRef = academiaRef.collection("equipos")

        fun actualizarEquipos(
            nuevos: List<EquipoPlantel>,
            aviso: String?
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
            avisoEquipos = aviso
            cargandoEquipos = false

            // Ya no existe ningún equipo predeterminado.
            if (ordenados.none {
                    it.id == equipoSeleccionadoId
                }
            ) {
                equipoSeleccionadoId =
                    ordenados.firstOrNull()?.id
            }
        }

        if (uid == null) {
            actualizarEquipos(
                emptyList(),
                "Inicia sesión para consultar equipos."
            )

            onDispose {
                activo = false
            }
        } else {
            // Obtenemos la membresía del usuario autenticado.
            val miembroListener = academiaRef
                .collection("miembros")
                .document(uid)
                .addSnapshotListener miembro@{ miembro, error ->

                    if (!activo) return@miembro

                    version++
                    val solicitud = version

                    equiposListener?.remove()
                    equiposListener = null

                    cargandoEquipos = true
                    equipos = emptyList()
                    equipoSeleccionadoId = null
                    errorJugadores = null

                    if (
                        error != null ||
                        miembro == null ||
                        !miembro.exists()
                    ) {
                        actualizarEquipos(
                            emptyList(),
                            "No se pudo validar tu membresía en la academia."
                        )
                        return@miembro
                    }

                    val aprobado =
                        miembro.getBoolean("activo") == true &&
                                miembro.getString("estadoRol") == "APROBADO"

                    if (!aprobado) {
                        actualizarEquipos(
                            emptyList(),
                            "Tu membresía no está activa y aprobada."
                        )
                        return@miembro
                    }

                    val rol = miembro.getString("rol")

                    if (rol == "Administrador") {
                        // El administrador consulta los equipos activos.
                        // Firestore verifica sus permisos reales.
                        equiposListener = equiposRef
                            .whereEqualTo("estado", "ACTIVO")
                            .addSnapshotListener equipos@{
                                    snapshot, consultaError ->

                                if (
                                    !activo ||
                                    solicitud != version
                                ) {
                                    return@equipos
                                }

                                if (consultaError != null) {
                                    actualizarEquipos(
                                        emptyList(),
                                        "No tienes permiso para listar " +
                                                "los equipos de la academia."
                                    )
                                } else if (snapshot != null) {
                                    actualizarEquipos(
                                        snapshot.documents.mapNotNull {
                                            it.aEquipoActivo()
                                        },
                                        null
                                    )
                                }
                            }
                    } else {
                        // Entrenadores y otros miembros:
                        // solo IDs registrados en su membresía.
                        val ids = (
                                miembro.get("equipoIds") as? List<*>
                                )
                            ?.filterIsInstance<String>()
                            ?.filter { it.isNotBlank() }
                            ?.distinct()
                            .orEmpty()

                        if (ids.isEmpty()) {
                            actualizarEquipos(
                                emptyList(),
                                "No tienes equipos asignados."
                            )
                        } else {
                            val encontrados =
                                mutableListOf<EquipoPlantel>()

                            var pendientes = ids.size

                            ids.forEach { equipoId ->
                                equiposRef
                                    .document(equipoId)
                                    .get()
                                    .addOnCompleteListener consulta@{ tarea ->

                                        if (
                                            !activo ||
                                            solicitud != version
                                        ) {
                                            return@consulta
                                        }

                                        if (tarea.isSuccessful) {
                                            tarea.result
                                                ?.aEquipoActivo()
                                                ?.let {
                                                    encontrados.add(it)
                                                }
                                        }

                                        pendientes--

                                        if (pendientes == 0) {
                                            actualizarEquipos(
                                                encontrados,
                                                if (encontrados.isEmpty()) {
                                                    "No hay equipos activos " +
                                                            "disponibles con tus permisos."
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
                version++
                miembroListener.remove()
                equiposListener?.remove()
            }
        }
    }

    // =================================================
    // 2. CONSULTAR LOS JUGADORES DEL EQUIPO SELECCIONADO
    // =================================================

    DisposableEffect(
        firestore,
        academiaId,
        equipoSeleccionadoId
    ) {
        val id = equipoSeleccionadoId

        jugadores = emptyList()
        errorJugadores = null
        jugadorSeleccionado = null
        cargandoJugadores = id != null

        if (id == null) {
            onDispose {}
        } else {
            val registro = firestore
                .collection("academias")
                .document(academiaId)
                .collection("equipos")
                .document(id)
                .collection("membresiasEquipo")
                .addSnapshotListener { snapshot, error ->

                    if (error != null) {
                        errorJugadores =
                            error.localizedMessage
                                ?: "No se puede consultar este plantel."

                        jugadores = emptyList()
                        cargandoJugadores = false

                    } else if (snapshot != null) {
                        jugadores = snapshot.documents
                            .mapNotNull { documento ->

                                if (
                                    documento.getString("estado")
                                    != "ACTIVA"
                                ) {
                                    null
                                } else {
                                    JugadorPlantel(
                                        id = documento
                                            .getString("jugadorId")
                                            ?: documento.id,

                                        dorsal = documento
                                            .getLong("dorsal")
                                            ?.toInt() ?: 0,

                                        nombre = documento
                                            .getString("nombreDeportivo")
                                            ?: "Sin nombre deportivo",

                                        posicion = documento
                                            .getString("posicion")
                                            ?: "Sin posición",

                                        estado = "ACTIVA"
                                    )
                                }
                            }
                            .sortedWith(
                                compareBy(
                                    { it.dorsal },
                                    { it.nombre }
                                )
                            )

                        errorJugadores = null
                        cargandoJugadores = false
                    }
                }

            onDispose {
                registro.remove()
            }
        }
    }

    // =================================================
    // 3. BÚSQUEDA Y FILTROS
    // =================================================

    val posiciones = listOf(
        "Todos",
        "Portero",
        "Arquero",
        "Defensa",
        "Medio",
        "Mediocampista",
        "Delantero"
    )

    val termino = busqueda.trim()

    val jugadoresFiltrados = jugadores.filter { jugador ->

        val coincidePosicion =
            filtro == "Todos" ||
                    jugador.posicion.equals(
                        filtro,
                        ignoreCase = true
                    )

        val coincideBusqueda =
            jugador.nombre.contains(
                termino,
                ignoreCase = true
            ) ||
                    jugador.dorsal.toString().contains(termino)

        coincidePosicion && coincideBusqueda
    }

    // =================================================
    // 4. INTERFAZ US-005
    // =================================================

    Eyebrow("EQUIPO · TEMPORADA 2026")
    SectionTitle("Plantel de jugadores")

    TextButton(
        onClick = {
            onNavigate(4)
        }
    ) {
        Text("← Volver a equipos")
    }

    if (uid == null) {
        Notice(
            "Debes iniciar sesión para consultar el plantel.",
            warning = true
        )
    }

    // -------------------------------------------------
    // SELECTOR DE EQUIPOS
    // -------------------------------------------------

    if (cargandoEquipos) {
        Body("Cargando equipos autorizados...")
    } else if (equipos.isEmpty()) {
        Notice(
            avisoEquipos
                ?: "No tienes equipos activos disponibles.",
            warning = true
        )
    } else {
        ExposedDropdownMenuBox(
            expanded = selectorAbierto,
            onExpandedChange = {
                selectorAbierto = !selectorAbierto
            }
        ) {
            OutlinedTextField(
                value = equipoActual?.etiqueta
                    ?: "Selecciona un equipo",

                onValueChange = {},

                readOnly = true,

                label = {
                    Text("Equipo activo")
                },

                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(
                        expanded = selectorAbierto
                    )
                },

                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor()
            )

            ExposedDropdownMenu(
                expanded = selectorAbierto,
                onDismissRequest = {
                    selectorAbierto = false
                }
            ) {
                equipos.forEach { equipo ->
                    DropdownMenuItem(
                        text = {
                            Text(equipo.etiqueta)
                        },

                        onClick = {
                            equipoSeleccionadoId = equipo.id
                            selectorAbierto = false
                            busqueda = ""
                            filtro = "Todos"
                        }
                    )
                }
            }
        }

        avisoEquipos?.let { aviso ->
            Body(aviso)
        }

        if (equipoActual != null) {
            Body(
                "${jugadores.size} jugadores activos · " +
                        equipoActual.etiqueta
            )
        }
    }

    Notice(
        "Jugadores obtenidos desde Firebase. " +
                "El registro requiere autorización."
    )

    // -------------------------------------------------
    // BUSCADOR
    // -------------------------------------------------

    OutlinedTextField(
        value = busqueda,
        onValueChange = {
            busqueda = it
        },

        label = {
            Text("Buscar jugador")
        },

        placeholder = {
            Text("Nombre o dorsal")
        },

        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        shape = RoundedCornerShape(14.dp)
    )

    // -------------------------------------------------
    // FILTROS POR POSICIÓN
    // -------------------------------------------------

    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        posiciones.forEach { posicion ->
            FilterChip(
                selected = filtro == posicion,
                onClick = {
                    filtro = posicion
                },
                label = {
                    Text(posicion)
                }
            )
        }
    }

    // =================================================
    // 5. RESULTADOS DEL PLANTEL
    // =================================================

    when {
        cargandoEquipos || cargandoJugadores -> {
            Body("Cargando plantel desde Firebase...")
        }

        equipoSeleccionadoId == null -> {
            Body("Selecciona un equipo autorizado.")
        }

        errorJugadores != null -> {
            Notice(
                "No se pudo consultar el plantel: $errorJugadores",
                warning = true
            )
        }

        else -> {
            Body(
                "${jugadoresFiltrados.size} jugadores encontrados"
            )

            MockCard {
                if (jugadoresFiltrados.isEmpty()) {
                    Body("No hay jugadores para mostrar.")
                } else {
                    jugadoresFiltrados.forEachIndexed {
                            index, jugador ->

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    jugadorSeleccionado = jugador
                                }
                                .padding(vertical = 4.dp)
                        ) {
                            Column(
                                modifier = Modifier.weight(1f)
                            ) {
                                PlayerRow(
                                    number = jugador.dorsal.toString(),
                                    name = jugador.nombre,
                                    detail = jugador.posicion,
                                    state = "Ver"
                                )
                            }
                        }

                        if (
                            index < jugadoresFiltrados.lastIndex
                        ) {
                            SoftDivider()
                        }
                    }
                }
            }
        }
    }

    // =================================================
    // 6. INFORMACIÓN DEPORTIVA DEL JUGADOR
    // =================================================

    jugadorSeleccionado?.let { jugador ->
        AlertDialog(
            onDismissRequest = {
                jugadorSeleccionado = null
            },

            title = {
                Text("Información del jugador")
            },

            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        "Nombre deportivo: ${jugador.nombre}"
                    )
                    Text("Dorsal: ${jugador.dorsal}")
                    Text("Posición: ${jugador.posicion}")
                    Text(
                        "Categoría: " +
                                (equipoActual?.categoria ?: "Sin categoría")
                    )
                    Text("Estado: ${jugador.estado}")

                    Body(
                        "Perfil completo pendiente de " +
                                "integración con US-006."
                    )
                }
            },

            confirmButton = {
                TextButton(
                    onClick = {
                        jugadorSeleccionado = null
                    }
                ) {
                    Text("Cerrar")
                }
            }
        )
    }
}

// =====================================================
// US-006: PERFIL DEL JUGADOR
// PANTALLA ORIGINAL DEL COMPAÑERO
// =====================================================

@Composable
private fun PlayerProfileView() {

    MockCard(containerColor = Forest) {

        Row(
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {

            Avatar(
                "DT",
                background = Color.White
            )

            Column {
                Eyebrow(
                    "SUB-15 A · LOS CEDROS",
                    Color.White.copy(alpha = .75f)
                )

                Text(
                    "Diego Torres",
                    color = Color.White,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                Body(
                    "Defensa central  ·  #4",
                    Color.White.copy(alpha = .8f)
                )
            }
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(26.dp)
        ) {
            Metric("92%", "asistencia")
            Metric("5", "partidos")
            Metric("2", "goles")
        }
    }

    SectionTitle("Ficha deportiva", "Editar")

    MockCard {
        PlayerRow(
            "↗",
            "Datos físicos",
            "1.68 m · 58 kg · Pie derecho"
        )

        SoftDivider()

        PlayerRow(
            "◎",
            "Fecha de nacimiento",
            "12 de febrero de 2011"
        )

        SoftDivider()

        PlayerRow(
            "☎",
            "Contacto",
            "Visible solo para el equipo autorizado"
        )

        SoftDivider()

        PlayerRow(
            "SOS",
            "Emergencia",
            "María Torres · Madre · ••• ••• 482"
        )
    }

    Notice(
        "La información sensible solo es visible para " +
                "el entrenador, el jugador y su apoderado."
    )

    SectionTitle("Participación reciente")

    MockCard {
        PlayerRow(
            "12",
            "Entrenamiento",
            "Presente · 15 sep",
            "Presente"
        )

        PlayerRow(
            "09",
            "Entrenamiento",
            "Presente · 12 sep",
            "Presente"
        )

        PlayerRow(
            "05",
            "Fecha 5 vs. San Borja",
            "1 gol · 1 asistencia"
        )
    }
}

// =====================================================
// US-007: MENSUALIDADES SIMULADAS
// PANTALLA ORIGINAL DEL COMPAÑERO
// =====================================================

@Composable
private fun DuesView() {

    Eyebrow("ADMINISTRACIÓN · SEPTIEMBRE 2026")
    SectionTitle("Mensualidades", "Sub-15 A")

    Notice(
        "Vista demostrativa: no procesa pagos ni guarda datos de tarjetas.",
        warning = true
    )

    Row(
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        MockCard(modifier = Modifier.weight(1f)) {
            Metric("12", "al día")
        }

        MockCard(modifier = Modifier.weight(1f)) {
            Metric("3", "pendientes")
        }

        MockCard(modifier = Modifier.weight(1f)) {
            Metric("3", "vencidas")
        }
    }

    MockCard {
        listOf(
            Triple(
                "4", "Diego Torres", "Al día · 05 sep"
            ),
            Triple(
                "8", "Andrés Flores", "Pendiente · vence 20 sep"
            ),
            Triple(
                "9", "Thiago Ramos", "Vencida · 10 sep"
            ),
            Triple(
                "10", "Nicolás Paredes", "Al día · 03 sep"
            ),
            Triple(
                "11", "Gabriel Soto", "Pendiente · vence 20 sep"
            ),
            Triple(
                "15", "Bruno León", "Vencida · 10 sep"
            )
        ).forEach { (num, name, detail) ->
            val status = detail.substringBefore(" ·")

            PlayerRow(
                num,
                name,
                detail,
                status,
                toneForState(status)
            )
        }
    }

    Body("Los estados son registros administrativos simulados.")
}
