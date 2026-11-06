
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
import com.google.firebase.firestore.FirebaseFirestore

// =====================================================
// US-005: MODELO DEL PLANTEL
// =====================================================

private data class JugadorPlantel(
    val id: String,
    val dorsal: Int,
    val nombre: String,
    val posicion: String,
    val estado: String
)

// =====================================================
// NAVEGACION ORIGINAL
// =====================================================

@Composable
fun TeamScreens(screen: Int) {
    ScreenFrame {
        when (screen) {
            4 -> TeamsView()
            5 -> RosterView()
            6 -> PlayerProfileView()
            7 -> DuesView()
        }
    }
}

// =====================================================
// US-004: EQUIPOS Y CATEGORIAS
// CODIGO ORIGINAL
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
// RESPONSABLE: ARMANDO AGUIRRE
// FIREBASE CLOUD FIRESTORE
// =====================================================

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun RosterView() {

    val firestore = remember {
        FirebaseFirestore.getInstance()
    }

    // Equipo utilizado para la primera integración.
    val academiaId = "academia-demo"
    val equipoId = "los-cedros"

    var jugadores by remember {
        mutableStateOf<List<JugadorPlantel>>(emptyList())
    }

    var nombreEquipo by remember {
        mutableStateOf("Los Cedros")
    }

    var categoria by remember {
        mutableStateOf("Sub-16")
    }

    var cargando by remember {
        mutableStateOf(true)
    }

    var error by remember {
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

    // -------------------------------------------------
    // CONSULTAR JUGADORES EN FIRESTORE
    // -------------------------------------------------

    DisposableEffect(firestore, academiaId, equipoId) {

        val equipoRef = firestore
            .collection("academias")
            .document(academiaId)
            .collection("equipos")
            .document(equipoId)

        val equipoListener = equipoRef.addSnapshotListener {
                documento, excepcion ->

            if (excepcion == null && documento != null && documento.exists()) {

                nombreEquipo =
                    documento.getString("nombre") ?: "Los Cedros"

                categoria =
                    documento.getString("categoria") ?: "Sub-16"
            }
        }

        val jugadoresListener = equipoRef
            .collection("membresiasEquipo")
            .addSnapshotListener { snapshot, excepcion ->

                if (excepcion != null) {

                    error = "No se pudo consultar el plantel: " +
                            (excepcion.localizedMessage ?: "Error desconocido")

                    cargando = false

                } else if (snapshot != null) {

                    jugadores = snapshot.documents.mapNotNull { documento ->

                        val estado =
                            documento.getString("estado") ?: ""

                        if (estado != "ACTIVA") {
                            return@mapNotNull null
                        }

                        JugadorPlantel(
                            id = documento.getString("jugadorId")
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

                            estado = estado
                        )
                    }.sortedWith(
                        compareBy(
                            { it.dorsal },
                            { it.nombre }
                        )
                    )

                    error = null
                    cargando = false
                }
            }

        onDispose {
            equipoListener.remove()
            jugadoresListener.remove()
        }
    }

    // -------------------------------------------------
    // BUSQUEDA Y FILTROS
    // -------------------------------------------------

    val posiciones = listOf(
        "Todos",
        "Arquero",
        "Defensa",
        "Mediocampista",
        "Delantero"
    )

    val jugadoresFiltrados = jugadores.filter { jugador ->

        val coincidePosicion =
            filtro == "Todos" ||
                    jugador.posicion.equals(
                        filtro,
                        ignoreCase = true
                    )

        val coincideBusqueda =
            jugador.nombre.contains(
                busqueda.trim(),
                ignoreCase = true
            ) ||
                    jugador.dorsal.toString().contains(
                        busqueda.trim()
                    )

        coincidePosicion && coincideBusqueda
    }

    // -------------------------------------------------
    // INTERFAZ
    // -------------------------------------------------

    Eyebrow("EQUIPO · TEMPORADA 2026")

    SectionTitle("Plantel de jugadores")

    Body("$nombreEquipo · $categoria")

    Body("${jugadores.size} jugadores activos")

    Notice(
        "Los jugadores se consultan desde Firebase. " +
                "El registro de nuevos jugadores requiere autorización."
    )

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

    // -------------------------------------------------
    // RESULTADOS
    // -------------------------------------------------

    if (FirebaseAuth.getInstance().currentUser == null) {

        Notice(
            "Debes iniciar sesión para consultar el plantel.",
            warning = true
        )
    }

    if (cargando) {

        Body("Cargando jugadores desde Firebase...")

    } else if (error != null) {

        Notice(
            text = error ?: "Error desconocido",
            warning = true
        )

    } else {

        Body("${jugadoresFiltrados.size} jugadores encontrados")

        MockCard {

            if (jugadoresFiltrados.isEmpty()) {

                Body("No hay jugadores para mostrar.")

            } else {

                jugadoresFiltrados.forEachIndexed { index, jugador ->

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

                    if (index < jugadoresFiltrados.lastIndex) {
                        SoftDivider()
                    }
                }
            }
        }
    }

    // -------------------------------------------------
    // DETALLE DEPORTIVO
    // -------------------------------------------------

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
                    Text("Nombre deportivo: ${jugador.nombre}")
                    Text("Dorsal: ${jugador.dorsal}")
                    Text("Posición: ${jugador.posicion}")
                    Text("Categoría: $categoria")
                    Text("Estado: ${jugador.estado}")

                    Body(
                        "Perfil completo pendiente de integración con US-006."
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
// CODIGO ORIGINAL
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
        "La información sensible solo es visible para el entrenador, el jugador y su apoderado."
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
// CODIGO ORIGINAL
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
            Triple("4", "Diego Torres", "Al día · 05 sep"),
            Triple("8", "Andrés Flores", "Pendiente · vence 20 sep"),
            Triple("9", "Thiago Ramos", "Vencida · 10 sep"),
            Triple("10", "Nicolás Paredes", "Al día · 03 sep"),
            Triple("11", "Gabriel Soto", "Pendiente · vence 20 sep"),
            Triple("15", "Bruno León", "Vencida · 10 sep")
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
