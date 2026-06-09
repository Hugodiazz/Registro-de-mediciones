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
            text = "Guía Completa de Medición y Salud",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        Text(
            text = "Consulta las bases científicas, fórmulas matemáticas y protocolos clínicos que utiliza la aplicación para procesar tus métricas corporales en tiempo real.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        // CARD 1: IMC
        InfoCard(
            title = "Índice de Masa Corporal (IMC)",
            icon = Icons.Default.TrendingUp,
            iconTint = Color(0xFFE91E63)
        ) {
            Text(
                text = "Métrica estándar recomendada por la Organización Mundial de la Salud (OMS) para evaluar si un individuo tiene un peso saludable en relación con su estatura.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                lineHeight = 20.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            FormulaContainer("IMC = Peso (kg) / Altura² (m)")
            BulletRow(boldText = "< 18.5:", text = "Bajo peso (desnutrición o delgadez constitucional)")
            BulletRow(boldText = "18.5 a 24.9:", text = "Peso saludable (Óptimo)")
            BulletRow(boldText = "25.0 a 29.9:", text = "Sobrepeso")
            BulletRow(boldText = ">= 30.0:", text = "Obesidad (Riesgo cardiovascular aumentado)")
        }

        // CARD 2: COMPOSICION CORPORAL
        InfoCard(
            title = "Composición Corporal Avanzada",
            icon = Icons.Default.AccessibilityNew,
            iconTint = Color(0xFF4CAF50)
        ) {
            Text(
                text = "Más allá del peso bruto, dividimos tu organismo en tejido graso y masa muscular magra activa para un análisis antropométrico completo:",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                lineHeight = 20.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            FormulaContainer(
                "Masa Grasa (kg) = Peso (kg) × ( % Grasa / 100 )\n\n" +
                "Masa Magra (kg) = Peso (kg) − Masa Grasa (kg)\n\n" +
                "FFMI (Índice de Masa Libre de Grasa) = Masa Magra (kg) / Altura² (m)"
            )
            BulletRow(
                boldText = "FFMI (Fat-Free Mass Index):",
                text = "Indica tu densidad muscular por metro cuadrado. Un FFMI entre 18-20 es el promedio; de 21-22 es atlético y muscular destacado; de 23-25 se considera el límite máximo alcanzable de forma natural."
            )
        }

        // CARD 3: US NAVY METHOD
        InfoCard(
            title = "Fórmula de Grasa US Navy",
            icon = Icons.Default.Percent,
            iconTint = MaterialTheme.colorScheme.primary
        ) {
            Text(
                text = "Mapea tus circunferencias musculares frente a la altura. El método científico de la Marina de EE. UU. cuenta con una desviación de solo +/- 3.5% respecto a escaneos DEXA de laboratorio.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                lineHeight = 20.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            FormulaContainer(
                "Hombres:\n%Grasa = 495 / [1.0324 - 0.19077 × log10(Cintura - Cuello) + 0.15456 × log10(Altura)] - 450\n\n" +
                "Mujeres:\n%Grasa = 495 / [1.29579 - 0.35004 × log10(Cintura + Cadera - Cuello) + 0.22100 × log10(Altura)] - 450"
            )
            BulletRow(
                boldText = "Importante para mujeres:",
                text = "La cadera es la zona principal de acumulación de estrógeno y grasa ginoide, por lo que su perímetro es indispensable para el cálculo femenino."
            )
        }

        // CARD 4: SALUD Y DISTRIBUCION
        InfoCard(
            title = "Distribución y Riesgo Cardiovascular (ICC/ICA)",
            icon = Icons.Default.HealthAndSafety,
            iconTint = Color(0xFF009688)
        ) {
            Text(
                text = "Evaluamos cómo y dónde almacenas grasa, midiendo el riesgo coronario y vascular central:",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                lineHeight = 20.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            FormulaContainer(
                "Índice Cintura-Cadera (ICC) = Cintura (cm) / Cadera (cm)\n\n" +
                "Índice Cintura-Altura (ICA) = Cintura (cm) / Altura (cm)"
            )
            BulletRow(
                boldText = "ICC Sano:",
                text = "Hombres < 0.90, Mujeres < 0.85. Valores superiores indican obesidad androide visceral extrema."
            )
            BulletRow(
                boldText = "ICA Sano:",
                text = "Menor de 0.50 (la cintura debe medir menos de la mitad de tu altura). Excelente marcador para descartar aterosclerosis y resistencia a la insulina."
            )
        }

        // CARD 5: GRASA VISCERAL
        InfoCard(
            title = "Área de Grasa Visceral Estimada (VFA)",
            icon = Icons.Default.Warning,
            iconTint = Color(0xFFFF9800)
        ) {
            Text(
                text = "La grasa visceral recubre tus órganos vitales (hígado, páncreas) y secreta citocinas inflamatorias destructivas.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                lineHeight = 20.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            FormulaContainer(
                "Hombres: VFA (cm²) = 1.48 × Cintura (cm) + 0.35 × Edad (años) − 88.5\n\n" +
                "Mujeres: VFA (cm²) = 1.03 × Cintura (cm) + 0.56 × Edad (años) − 51.2"
            )
            BulletRow(boldText = "VFA < 100 cm²:", text = "Riesgo Bajo / Zona Óptima de inmunidad.")
            BulletRow(boldText = "100 - 140 cm²:", text = "Riesgo Moderado / Alerta de estilo de vida.")
            BulletRow(boldText = "VFA > 140 cm²:", text = "Riesgo Alto / Marcado incremento de diabetes tipo 2 y cardiopatías.")
        }

        // CARD 6: METABOLICO Y ENERGETICO
        InfoCard(
            title = "Tasa Metabólica y Energía (TMB/GETD)",
            icon = Icons.Default.Bolt,
            iconTint = Color(0xFFFBC02D)
        ) {
            Text(
                text = "Establece el balance calórico que requiere tu cuerpo para subsistir y entrenar, utilizando la fórmula de Katch-McArdle, al basarse directamente sobre tu masa magra muscular metabólicamente muy activa:",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                lineHeight = 20.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            FormulaContainer(
                "Tasa Metabólica Basal (TMB) = 370 + 21.6 × Masa Magra (kg)\n\n" +
                "Gasto Energético Total Diario (GETD) = TMB × Factor Actividad"
            )
            BulletRow(boldText = "Sedentario (x1.2):", text = "Trabajo de oficina y mínimo movimiento diario.")
            BulletRow(boldText = "Ligero (x1.375):", text = "Actividad suave o entrenamiento de 1 a 3 días por semana.")
            BulletRow(boldText = "Moderado (x1.55):", text = "Entrenamiento intenso de 3 a 5 días por semana.")
            BulletRow(boldText = "Intenso (x1.725):", text = "Práctica deportiva intensa diaria o trabajo físico demandante.")
        }

        // CARD 7: PROPORCIONES CLASICAS (REEVES)
        InfoCard(
            title = "Simetría de Proporciones Clásicas (Steve Reeves)",
            icon = Icons.Default.Star,
            iconTint = Color(0xFF9C27B0)
        ) {
            Text(
                text = "En base al canon estético clásico de la era de oro del culturismo de Steve Reeves, se utiliza como ancla biométrica el tamaño óseo del Pecho ideal (calculado idealmente como el 58% de tu estatura). A partir de allí, se calculan las proporciones perfectas para esculpir simetría:",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                lineHeight = 20.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            FormulaContainer(
                "Pecho Ideal = Altura (cm) × 0.58\n\n" +
                "Cintura Ideal = Pecho × 0.70 (Silueta en V / V-Taper)\n" +
                "Cadera Ideal = Pecho × 0.85\n" +
                "Cuello / Bíceps / Gemelos Ideales = Pecho × 0.38\n" +
                "Antebrazo Ideal = Pecho × 0.30\n" +
                "Muslo Ideal = Pecho × 0.60"
            )
            BulletRow(
                boldText = "Silueta V-Taper:",
                text = "Dividir el contorno del pecho sobre el contorno de la cintura nos da la relación V-Taper. Una relación por encima de 1.4 es sinónimo de una cintura estrecha y dorsal prominente estética."
            )
        }

        // CARD 8: PROTOCOLO CLINICO
        InfoCard(
            title = "Protocolo para Toma de Medidas Perfecta",
            icon = Icons.Default.AssignmentTurnedIn,
            iconTint = Color(0xFF3F51B5)
        ) {
            StepRow(stepNum = "1", text = "Momento óptimo: Mídete siempre por la mañana, en ayunas, recién levantado y después de vaciar la vejiga.")
            StepRow(stepNum = "2", text = "Cinta métrica: Utiliza cintas de sastre flexibles no elásticas. Asegúrate de que la cinta rodee el plano horizontal exacto sin presionar la piel.")
            StepRow(stepNum = "3", text = "Cuello: Rodea la zona justo por debajo de la laringe (nuez de Adán).")
            StepRow(stepNum = "4", text = "Cintura: Hombres miren sobre el ombligo. Mujeres miren horizontalmente en el punto más estrecho del torso.")
            StepRow(stepNum = "5", text = "Cadera: Mujeres rodeen la parte con máxima circunferencia de los glúteos.")
            StepRow(stepNum = "6", text = "Antebrazo: El antebrazo y los flexores del brazo deben medirse en el volumen del contorno máximo del brazo completamente relajado.")
        }

        Spacer(modifier = Modifier.height(70.dp))
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
fun FormulaContainer(
    formula: String
) {
    Surface(
        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f),
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Text(
            text = formula,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(14.dp),
            textAlign = TextAlign.Center,
            lineHeight = 20.sp
        )
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
