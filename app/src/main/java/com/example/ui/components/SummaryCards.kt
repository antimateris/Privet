package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.EaseOutCubic
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.DirectionsBike
import androidx.compose.material.icons.filled.Handshake
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.WashFinancialSummary
import com.example.util.FormatUtils
import com.example.util.coloredShadow
import kotlin.math.roundToLong

@Composable
fun SummaryCardsSection(
    summary: WashFinancialSummary,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Row 1: Total Motors & Gross Income
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            SummaryKpiCard(
                title = "Total Motor",
                value = "${summary.totalMotors}",
                rawValue = summary.totalMotors.toLong(),
                isCurrency = false,
                unit = "Motor Dicuci",
                icon = Icons.Default.DirectionsBike,
                iconTint = Color(0xFF0284C7),
                bgTint = Color(0xFFE0F2FE),
                modifier = Modifier.weight(1f)
            )

            SummaryKpiCard(
                title = "Total Omset",
                value = FormatUtils.formatRupiah(summary.totalGrossRevenue),
                rawValue = summary.totalGrossRevenue,
                isCurrency = true,
                unit = "Pendapatan Kotor",
                icon = Icons.Default.Payments,
                iconTint = Color(0xFF16A34A),
                bgTint = Color(0xFFDCFCE7),
                modifier = Modifier.weight(1f)
            )
        }

        // Row 2: Washer Share & Owner Net
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            SummaryKpiCard(
                title = "Bagi Hasil Petugas",
                value = FormatUtils.formatRupiah(summary.totalWasherShare),
                rawValue = summary.totalWasherShare,
                isCurrency = true,
                unit = "Komisi Pekerja",
                icon = Icons.Default.Handshake,
                iconTint = Color(0xFFD97706),
                bgTint = Color(0xFFFEF3C7),
                modifier = Modifier.weight(1f)
            )

            SummaryKpiCard(
                title = "Kas Bersih Pemilik",
                value = FormatUtils.formatRupiah(summary.totalOwnerShare),
                rawValue = summary.totalOwnerShare,
                isCurrency = true,
                unit = "Laba Usaha Steam",
                icon = Icons.Default.AccountBalance,
                iconTint = Color(0xFF0D9488),
                bgTint = Color(0xFFCCFBF1),
                modifier = Modifier.weight(1f)
            )
        }
    }
}

/**
 * KPI card with lightweight, one-shot animations:
 *  - the numeric value counts up smoothly from its previous displayed value
 *  - the card does a tiny GPU-composited scale pulse (1f -> 1.03f -> 1f) right after value changes
 */
@Composable
private fun SummaryKpiCard(
    title: String,
    value: String,
    unit: String,
    icon: ImageVector,
    iconTint: Color,
    bgTint: Color,
    modifier: Modifier = Modifier,
    rawValue: Long? = null,
    isCurrency: Boolean = true
) {
    val pulseScale = remember { Animatable(1f) }
    val animatedRaw = remember { Animatable(rawValue?.toFloat() ?: 0f) }

    LaunchedEffect(rawValue, value) {
        if (rawValue != null) {
            animatedRaw.animateTo(
                targetValue = rawValue.toFloat(),
                animationSpec = tween(durationMillis = 280, easing = EaseOutCubic)
            )
        }
        pulseScale.snapTo(1f)
        pulseScale.animateTo(1.03f, tween(100, easing = EaseOutCubic))
        pulseScale.animateTo(1f, tween(100, easing = EaseOutCubic))
    }

    val displayValue = if (rawValue != null) {
        if (isCurrency) {
            FormatUtils.formatRupiah(animatedRaw.value.roundToLong())
        } else {
            "${animatedRaw.value.roundToLong()}"
        }
    } else {
        value
    }

    Card(
        modifier = modifier
            .coloredShadow(
                color = iconTint,
                alpha = 0.42f,
                borderRadius = 16.dp,
                shadowRadius = 8.dp,
                offsetY = 4.dp,
                elevation = 6.dp
            )
            .scale(pulseScale.value),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(1.dp, iconTint.copy(alpha = 0.25f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(bgTint),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconTint,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = displayValue,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Text(
                text = unit,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.outline,
                fontSize = 11.sp
            )
        }
    }
}
