package com.example.sportprog3.presentation.community

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.sportprog3.presentation.components.Avatar
import com.example.sportprog3.presentation.components.Badge
import com.example.sportprog3.presentation.components.Body
import com.example.sportprog3.presentation.components.Eyebrow
import com.example.sportprog3.presentation.components.MockCard
import com.example.sportprog3.presentation.components.MockField
import com.example.sportprog3.presentation.components.Notice
import com.example.sportprog3.presentation.components.PlayerRow
import com.example.sportprog3.presentation.components.PrimaryAction
import com.example.sportprog3.presentation.components.ScreenFrame
import com.example.sportprog3.presentation.components.SectionTitle
import com.example.sportprog3.presentation.components.SoftDivider
import com.example.sportprog3.ui.theme.Amber
import com.example.sportprog3.ui.theme.Forest
import com.example.sportprog3.ui.theme.Mint
import com.example.sportprog3.ui.theme.Rose

@Composable
fun CommunityScreens(screen: Int) {
    ScreenFrame {
        when (screen) {
            17 -> CommunityFeed()
            18 -> OpenTrials()
            19 -> ModerationQueue()
        }
    }
}

@Composable
private fun CommunityFeed() {
    Eyebrow("COMUNIDAD SPORTPRO")
    SectionTitle("Lo que pasa en la cancha", "＋ Publicar")
    MockCard {
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Avatar("CM")
            Column {
                Text("Carlos Medina", fontWeight = FontWeight.Bold)
                Body("Entrenador · Los Cedros FC · hace 2 h")
            }
        }
        Text("¡Gran esfuerzo del equipo en la fecha 5! Orgulloso de cómo lucharon hasta el final. 💚", style = MaterialTheme.typography.bodyLarge)
        Badge("LOS CEDROS FC")
        SoftDivider()
        Row(horizontalArrangement = Arrangement.spacedBy(22.dp)) {
            Body("♡  24")
            Body("◯  6 comentarios")
            Body("Reportar")
        }
    }
    MockCard {
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Avatar("AC")
            Column {
                Text("Academia Central", fontWeight = FontWeight.Bold)
                Body("Club verificado · hace 5 h")
            }
        }
        Text("¡Sub-15, nos vemos este sábado! Confirma tu disponibilidad desde la convocatoria.", style = MaterialTheme.typography.bodyLarge)
        SoftDivider()
        Row(horizontalArrangement = Arrangement.spacedBy(22.dp)) {
            Body("♡  18")
            Body("◯  3 comentarios")
            Body("Reportar")
        }
    }
    Notice("Las fotos de menores solo se publican con autorización de su apoderado.")
}

@Composable
private fun OpenTrials() {
    Eyebrow("OPORTUNIDADES VERIFICADAS")
    SectionTitle("Pruebas abiertas", "＋ Publicar")
    Body("Encuentra una oportunidad que se ajuste a ti.")
    MockField("Buscar por ubicación", "Lima, Perú")
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Badge("Categoría · Sub-15")
        Badge("Posición · Todas")
    }
    Badge("Fecha · Próximas")
    MockCard {
        Badge("CLUB VERIFICADO")
        Text("Convocatoria Sub-15 · Academia Central", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Body("Buscamos: defensas y mediocampistas")
        Body("Sáb. 26 sep · 09:00 · San Borja, Lima")
        SoftDivider()
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Body("Ver detalle  →")
            Body("Reportar")
        }
    }
    MockCard {
        Badge("CLUB VERIFICADO")
        Text("Pruebas formativas · Unión Miraflores", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Body("Buscamos: arqueros · Categoría Sub-13")
        Body("Dom. 27 sep · 10:30 · Miraflores, Lima")
        SoftDivider()
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Body("Ver detalle  →")
            Body("Reportar")
        }
    }
    Notice("SportPro nunca solicita pagos, depósitos ni datos bancarios para una prueba. Reporta cualquier aviso sospechoso.", warning = true)
}

@Composable
private fun ModerationQueue() {
    Eyebrow("PANEL DE ADMINISTRACIÓN")
    SectionTitle("Centro de moderación", "4 pendientes")
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Badge("Pendientes · 4")
        Badge("En revisión · 2")
        Badge("Resueltos")
    }
    MockCard {
        Badge("AVISO DE PRUEBA", color = Rose)
        Text("Academia Norte · «Pruebas Sub-15»", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Body("Motivo: solicitud de depósito · Reportado hace 12 min")
        SoftDivider()
        Notice("Contenido revisado: solicita un pago para reservar vacante.", warning = true)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Badge("Descartar")
            Badge("Eliminar aviso", color = Rose, textColor = Forest)
        }
        Badge("Bloquear cuenta")
    }
    MockCard {
        Badge("COMENTARIO")
        Text("Usuario · publicación de Los Cedros", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Body("Motivo: lenguaje inapropiado · Reportado hace 34 min")
        SoftDivider()
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Badge("Descartar")
            Badge("Eliminar comentario", color = Rose)
        }
    }
    SectionTitle("Registro reciente")
    MockCard {
        PlayerRow("✓", "Aviso retirado", "Decisión de C. Medina · hoy 09:14", "Resuelto")
        PlayerRow("✓", "Reporte descartado", "Decisión de A. Ruiz · ayer 16:48", "Resuelto")
    }
    Body("Cada decisión conserva moderador, fecha y motivo.")
}
