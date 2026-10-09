package com.example.sportprog3.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sportprog3.ui.theme.Amber
import com.example.sportprog3.ui.theme.Forest
import com.example.sportprog3.ui.theme.Ink
import com.example.sportprog3.ui.theme.Line
import com.example.sportprog3.ui.theme.Mint
import com.example.sportprog3.ui.theme.Muted
import com.example.sportprog3.ui.theme.Paper
import com.example.sportprog3.ui.theme.Pitch
import com.example.sportprog3.ui.theme.Rose

@Composable
fun ScreenFrame(content: @Composable () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Paper)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 18.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) { content() }
}

@Composable
fun SectionTitle(title: String, trailing: String? = null) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Ink)
        trailing?.let {
            Text(it, style = MaterialTheme.typography.labelLarge, color = Pitch, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
fun MockCard(
    modifier: Modifier = Modifier,
    containerColor: Color = Color.White,
    content: @Composable () -> Unit
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) { content() }
    }
}

@Composable
fun Eyebrow(text: String, color: Color = Muted) {
    Text(
        text.uppercase(),
        color = color,
        style = MaterialTheme.typography.labelSmall,
        fontWeight = FontWeight.Bold,
        letterSpacing = 1.1.sp
    )
}

@Composable
fun Body(text: String, color: Color = Muted) {
    Text(text, style = MaterialTheme.typography.bodyMedium, color = color)
}

@Composable
fun Badge(text: String, color: Color = Mint, textColor: Color = Forest) {
    Surface(color = color, shape = RoundedCornerShape(50)) {
        Text(
            text,
            color = textColor,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
        )
    }
}

@Composable
fun PrimaryAction(
    text: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    onClick: () -> Unit = {}
) {
    Button(
        onClick = onClick,
        modifier = modifier.fillMaxWidth().height(52.dp),
        enabled = enabled,
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(containerColor = Forest)
    ) {
        Text(text, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
    }
}

@Composable
fun SecondaryAction(text: String, modifier: Modifier = Modifier, onClick: () -> Unit = {}) {
    TextButton(onClick = onClick, modifier = modifier) {
        Text(text, color = Forest, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun MockField(label: String, value: String, modifier: Modifier = Modifier) {
    OutlinedTextField(
        value = value,
        onValueChange = {},
        label = { Text(label) },
        modifier = modifier.fillMaxWidth(),
        singleLine = true,
        readOnly = true,
        shape = RoundedCornerShape(14.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Pitch,
            unfocusedBorderColor = Line,
            focusedLabelColor = Pitch,
            unfocusedLabelColor = Muted
        )
    )
}

@Composable
fun Avatar(initials: String, modifier: Modifier = Modifier, background: Color = Mint, foreground: Color = Forest) {
    Box(
        modifier = modifier.size(46.dp).background(background, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Text(initials, color = foreground, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun PlayerRow(
    number: String,
    name: String,
    detail: String,
    state: String? = null,
    stateColor: Color = Mint
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(11.dp)
    ) {
        Surface(color = Mint, shape = RoundedCornerShape(12.dp)) {
            Text(
                number,
                modifier = Modifier.padding(horizontal = 11.dp, vertical = 9.dp),
                color = Forest,
                fontWeight = FontWeight.Bold
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(name, color = Ink, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyMedium)
            Text(detail, color = Muted, style = MaterialTheme.typography.bodySmall)
        }
        state?.let { Badge(it, color = stateColor) }
    }
}

@Composable
fun Metric(
    value: String,
    label: String,
    modifier: Modifier = Modifier,
    valueColor: Color = Forest,
    labelColor: Color = Muted
) {
    Column(modifier = modifier, horizontalAlignment = Alignment.Start) {
        Text(value, color = valueColor, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Black)
        Text(label, color = labelColor, style = MaterialTheme.typography.labelSmall)
    }
}

@Composable
fun SoftDivider() {
    Spacer(modifier = Modifier.fillMaxWidth().height(1.dp).background(Line))
}

@Composable
fun Notice(text: String, warning: Boolean = false) {
    Surface(color = if (warning) Amber else Mint, shape = RoundedCornerShape(14.dp)) {
        Text(text, modifier = Modifier.padding(12.dp), color = Ink, style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
fun EventLine(time: String, event: String, detail: String, accent: Color = Pitch) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.Top
    ) {
        Text(time, color = accent, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(event, color = Ink, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyMedium)
            Text(detail, color = Muted, style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
fun toneForState(state: String): Color = when (state.lowercase()) {
    "vencida", "ausente", "no disponible", "bloqueado" -> Rose
    "pendiente", "tarde" -> Amber
    else -> Mint
}

@Composable
fun WhiteSpace(height: Int = 6) {
    Spacer(Modifier.height(height.dp))
}
