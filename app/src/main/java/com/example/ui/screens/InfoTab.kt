package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun InfoTab(
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Guía de Medición y Salud",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )

        // Card: Navy body fat method
        InfoCard(
            title = "Método de la Marina de EE. UU.",
            icon = Icons.Default.HelpOutline,
            iconTint = MaterialTheme.colorScheme.primary
        ) {
            Text(
                text = "Este método estima el porcentaje de grasa metabólica utilizando únicamente una cinta métrica. Los estudios de la Marina de EE. UU. (US Navy) demostraron que correlacionar circunferencias musculares con la altura ofrece una exactitud notable, desviándose de promedio solo un 3% a 4% respecto al pesaje hidrostático profesional.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                lineHeight = 20.sp
            )

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Variables requeridas según tu sexo:",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            BulletRow(boldText = "Hombres:", text = "Requieren Altura, Cuello y Cintura.")
            BulletRow(boldText = "Mujeres:", text = "Requieren Altura, Cuello, Cintura y Cadera.")
        }

        // Card: Accurate measurement tips
        InfoCard(
            title = "Protocolo de Medición Perfecta",
            icon = Icons.Default.AssignmentTurnedIn,
            iconTint = Color(0xFF009688)
        ) {
            Text(
                text = "Para obtener tendencias de progreso verídicas y constantes en tus gráficos, te sugerimos seguir estas pautas clínicas de medición:",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                lineHeight = 20.sp
            )

            Spacer(modifier = Modifier.height(10.dp))

            StepRow(stepNum = "1", text = "Mídete preferiblemente por la mañana en ayunas, después de ir al baño, y antes de consumir cualquier alimento o realizar deporte.")
            StepRow(stepNum = "2", text = "Usa una cinta métrica flexible pero inextensible (metálica o de sastre). Mantén la cinta paralela al suelo y firme, pero sin oprimir o tensar de más la piel.")
            StepRow(stepNum = "3", text = "Cuello: Mide la circunferencia justo debajo de la laringe (nuez de Adán).")
            StepRow(stepNum = "4", text = "Cintura: Hombres miren horizontalmente sobre el ombligo. Mujeres miren horizontalmente a nivel del diámetro mínimo (cintura natural).")
            StepRow(stepNum = "5", text = "Cadera (Solo mujeres): Mide horizontalmente sobre la circunferencia máxima de los glúteos.")
        }

        // Card: ACE body fat ranges
        InfoCard(
            title = "Rangos de Grasa Corporal (ACE)",
            icon = Icons.Default.Info,
            iconTint = Color(0xFF3B82F6)
        ) {
            Text(
                text = "Niveles de grasa recomendados por el American Council on Exercise (ACE):",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                lineHeight = 20.sp
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = "Masculino", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, fontSize = 13.sp)
                    BulletRow(boldText = "Grasa Esencial:", text = "2% a 5%")
                    BulletRow(boldText = "Atletas:", text = "6% a 13%")
                    BulletRow(boldText = "Fitness:", text = "14% a 17%")
                    BulletRow(boldText = "Aceptable:", text = "18% a 24%")
                    BulletRow(boldText = "Obesidad:", text = ">= 25%")
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(text = "Femenino", fontWeight = FontWeight.Bold, color = Color(0xFFE91E63), fontSize = 13.sp)
                    BulletRow(boldText = "Grasa Esencial:", text = "10% a 13%")
                    BulletRow(boldText = "Atletas:", text = "14% a 20%")
                    BulletRow(boldText = "Fitness:", text = "21% a 24%")
                    BulletRow(boldText = "Aceptable:", text = "25% a 31%")
                    BulletRow(boldText = "Obesidad:", text = ">= 32%")
                }
            }
        }

        Spacer(modifier = Modifier.height(70.dp)) // Safe scrolling margin for navigation bar
    }
}

@Composable
fun InfoCard(
    title: String,
    icon: ImageVector,
    iconTint: Color,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(color = iconTint.copy(alpha = 0.1f), shape = CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(imageVector = icon, contentDescription = title, tint = iconTint, modifier = Modifier.size(18.dp))
                }
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            content()
        }
    }
}

@Composable
fun BulletRow(
    boldText: String,
    text: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        verticalAlignment = Alignment.Top
    ) {
        Text(text = "• ", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, fontSize = 13.sp)
        Text(text = boldText, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface)
        Spacer(modifier = Modifier.width(4.dp))
        Text(text = text, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f))
    }
}

@Composable
fun StepRow(
    stepNum: String,
    text: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .size(18.dp)
                .background(color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f), shape = CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(text = stepNum, fontSize = 11.sp, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary)
        }
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = text,
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
            lineHeight = 18.sp
        )
    }
}
