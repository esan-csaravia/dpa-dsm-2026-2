package com.example.sportprog3.presentation.inbox

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import com.example.sportprog3.presentation.components.Body
import com.example.sportprog3.presentation.components.Eyebrow
import com.example.sportprog3.presentation.components.MockCard
import com.example.sportprog3.presentation.components.Notice
import com.example.sportprog3.presentation.components.ScreenFrame
import com.example.sportprog3.presentation.components.SectionTitle
import com.example.sportprog3.presentation.components.SoftDivider
import com.example.sportprog3.ui.theme.Forest

@Composable
fun InboxScreen() {
    ScreenFrame {
        Eyebrow("LOS CEDROS FC")
        SectionTitle("Avisos para ti", "Marcar todos leídos")
        NotificationItem("Hoy · 09:15", "Convocatoria publicada", "Confirma tu disponibilidad para el partido del sábado.")
        NotificationItem("Hoy · 08:30", "Cambio de horario", "El entrenamiento del martes pasa a las 18:00.")
        NotificationItem("Ayer · 19:05", "Resumen del partido", "La crónica de la fecha 5 ya está aprobada.")
        NotificationItem("Lun · 16:40", "Mensualidad pendiente", "Septiembre figura como pendiente (simulado).")
        NotificationItem("Sáb · 18:22", "Asistencia registrada", "Fuiste marcado como presente el 12/09.")
        Notice("Los avisos solo llegan a personas vinculadas con el equipo. Notificaciones push contempladas con Firebase Cloud Messaging.")
    }
}

@Composable
private fun NotificationItem(time: String, title: String, message: String) {
    MockCard {
        Eyebrow(time, Forest)
        Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Body(message)
        SoftDivider()
        Text("Abrir detalle  →", color = Forest, style = MaterialTheme.typography.labelLarge)
    }
}
