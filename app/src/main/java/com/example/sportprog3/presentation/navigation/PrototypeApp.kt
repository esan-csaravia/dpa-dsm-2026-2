package com.example.sportprog3.presentation.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.sportprog3.presentation.auth.AuthScreens
import com.example.sportprog3.presentation.community.CommunityScreens
import com.example.sportprog3.presentation.dashboard.DashboardScreen
import com.example.sportprog3.presentation.inbox.InboxScreen
import com.example.sportprog3.presentation.match.MatchScreens
import com.example.sportprog3.presentation.teams.TeamScreens
import com.example.sportprog3.presentation.training.TrainingScreens
import com.example.sportprog3.ui.theme.Forest
import com.example.sportprog3.ui.theme.Paper

data class PrototypeRoute(val id: Int, val title: String, val group: String)

private val prototypeRoutes = listOf(
    PrototypeRoute(1, "Iniciar sesión", "Cuenta"),
    PrototypeRoute(2, "Crear cuenta y elegir rol", "Cuenta"),
    PrototypeRoute(3, "Inicio del entrenador", "Inicio"),
    PrototypeRoute(4, "Equipos y categorías", "Equipo"),
    PrototypeRoute(5, "Plantel de jugadores", "Equipo"),
    PrototypeRoute(6, "Perfil del jugador", "Perfil"),
    PrototypeRoute(7, "Mensualidades simuladas", "Administración"),
    PrototypeRoute(8, "Plan de entrenamiento", "Entrenamiento"),
    PrototypeRoute(9, "Asistencia", "Entrenamiento"),
    PrototypeRoute(10, "Convocatoria del partido", "Partido"),
    PrototypeRoute(11, "Alineación 4-3-3", "Partido"),
    PrototypeRoute(12, "Operador · partido en vivo", "Partido"),
    PrototypeRoute(13, "Registrar evento", "Partido"),
    PrototypeRoute(14, "Eventos registrados", "Partido"),
    PrototypeRoute(15, "Estadísticas", "Partido"),
    PrototypeRoute(16, "Crónica con IA", "Partido"),
    PrototypeRoute(17, "Comunidad", "Comunidad"),
    PrototypeRoute(18, "Pruebas abiertas", "Comunidad"),
    PrototypeRoute(19, "Moderación", "Administración"),
    PrototypeRoute(20, "Avisos y notificaciones", "Comunidad"),
    PrototypeRoute(21, "Catálogo de eventos", "Partido")
)

@Composable
fun PrototypeApp() {
    var selectedId by remember { mutableIntStateOf(1) }
    var showViews by remember { mutableStateOf(false) }
    val route = prototypeRoutes.first { it.id == selectedId }
    val isAuth = selectedId in 1..2

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = Paper,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Forest)
                    .windowInsetsPadding(WindowInsets.statusBars)
                    .padding(horizontal = 20.dp, vertical = 12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            "SPORTPRO G3",
                            color = MaterialTheme.colorScheme.tertiary,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Black
                        )
                        Text(
                            route.title,
                            color = Color.White,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Surface(
                        color = Color.White.copy(alpha = 0.12f),
                        shape = RoundedCornerShape(18.dp),
                        modifier = Modifier.clickable { showViews = true }
                    ) {
                        Text(
                            "US-${route.id.toString().padStart(3, '0')}  ▾",
                            color = Color.White,
                            style = MaterialTheme.typography.labelLarge,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp)
                        )
                    }
                }
            }
        },
        bottomBar = {
            if (!isAuth) {
                Surface(shadowElevation = 8.dp, color = Color.White) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .windowInsetsPadding(WindowInsets.navigationBars)
                            .padding(horizontal = 8.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        listOf(
                            "Inicio" to 3,
                            "Equipo" to 5,
                            "Partido" to 12,
                            "Comunidad" to 17,
                            "Perfil" to 6
                        ).forEach { (label, target) ->
                            val active = when (target) {
                                3 -> selectedId == 3
                                5 -> selectedId in 4..7
                                12 -> selectedId in 10..16 || selectedId == 21
                                17 -> selectedId in 17..20
                                else -> selectedId == 6
                            }
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier
                                    .clickable { selectedId = target }
                                    .padding(horizontal = 3.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = when (label) {
                                        "Inicio" -> "⌂"
                                        "Equipo" -> "◉"
                                        "Partido" -> "⚽"
                                        "Comunidad" -> "▤"
                                        else -> "○"
                                    },
                                    color = if (active) Forest else MaterialTheme.colorScheme.onSurfaceVariant,
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Text(
                                    label,
                                    color = if (active) Forest else MaterialTheme.colorScheme.onSurfaceVariant,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = if (active) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            when (selectedId) {
                1, 2 -> AuthScreens(selectedId, onNavigate = { selectedId = it })
                3 -> DashboardScreen(onNavigate = { selectedId = it })
                4, 5, 6, 7 -> TeamScreens(selectedId)
                8, 9 -> TrainingScreens(selectedId)
                10, 11, 12, 13, 14, 15, 16, 21 -> MatchScreens(selectedId, onNavigate = { selectedId = it })
                17, 18, 19 -> CommunityScreens(selectedId)
                20 -> InboxScreen()
            }
        }
    }

    if (showViews) {
        AlertDialog(
            onDismissRequest = { showViews = false },
            title = { Text("Explorar las 21 vistas") },
            text = {
                LazyColumn {
                    items(prototypeRoutes) { item ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    selectedId = item.id
                                    showViews = false
                                }
                                .padding(vertical = 11.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "US-${item.id.toString().padStart(3, '0')}",
                                color = Forest,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(end = 12.dp)
                            )
                            Column {
                                Text(item.title, style = MaterialTheme.typography.bodyMedium)
                                Text(item.group, style = MaterialTheme.typography.labelSmall)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showViews = false }) { Text("Cerrar") }
            }
        )
    }
}
