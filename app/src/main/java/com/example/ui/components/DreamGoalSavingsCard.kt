package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.Stars
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.DreamGoalProgress
import com.example.ui.SavingsSource
import com.example.util.FormatUtils
import java.util.Locale

@Composable
fun DreamGoalSavingsCard(
    progress: DreamGoalProgress,
    onEditGoalClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showDailyView by remember { mutableStateOf(false) }

    val animatedProgress by animateFloatAsState(
        targetValue = if (showDailyView) progress.todayDailyTargetPercent else progress.progressPercent,
        animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing),
        label = "dream_goal_progress_anim"
    )

    val isFull = if (showDailyView) progress.todayDailyTargetPercent >= 1f else progress.isAchieved
    val displayPercent = if (showDailyView) {
        (progress.todayDailyTargetPercent * 100f).coerceAtLeast(0f)
    } else {
        (progress.progressPercent * 100f).coerceAtLeast(0f)
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("dream_goal_savings_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(
            width = if (progress.isAchieved) 1.5.dp else 1.dp,
            color = if (progress.isAchieved) Color(0xFF16A34A) else MaterialTheme.colorScheme.outlineVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header Row: Title, Target Item & Edit Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(
                                if (progress.isAchieved) Color(0xFFDCFCE7) else Color(0xFFFEF3C7)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (progress.isAchieved) Icons.Default.EmojiEvents else Icons.Default.Savings,
                            contentDescription = null,
                            tint = if (progress.isAchieved) Color(0xFF16A34A) else Color(0xFFD97706),
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Tabungan Barang Impian",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            if (progress.isAchieved) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = Color(0xFFDCFCE7)
                                ) {
                                    Text(
                                        text = "Tercapai! 🎉",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF15803D),
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }

                        Text(
                            text = progress.config.itemName,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                IconButton(
                    onClick = onEditGoalClick,
                    modifier = Modifier.testTag("edit_dream_goal_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Ubah Target Impian",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // View Toggle Chips: Progres Total vs Target Harian
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    onClick = { showDailyView = false },
                    shape = RoundedCornerShape(20.dp),
                    color = if (!showDailyView) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                    border = BorderStroke(
                        1.dp,
                        if (!showDailyView) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
                    )
                ) {
                    Text(
                        text = "🎯 Progres Total Barang",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = if (!showDailyView) FontWeight.Bold else FontWeight.Medium,
                        color = if (!showDailyView) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }

                Surface(
                    onClick = { showDailyView = true },
                    shape = RoundedCornerShape(20.dp),
                    color = if (showDailyView) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                    border = BorderStroke(
                        1.dp,
                        if (showDailyView) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
                    )
                ) {
                    Text(
                        text = "⚡ Omset Harian Hari Ini",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = if (showDailyView) FontWeight.Bold else FontWeight.Medium,
                        color = if (showDailyView) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }
            }

            // Big Percentage & Goal Status Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = if (showDailyView) "Pencapaian Target Harian" else "Progres Terkumpul",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            text = String.format(Locale.getDefault(), "%.1f%%", displayPercent),
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Black,
                            color = when {
                                isFull -> Color(0xFF16A34A)
                                displayPercent >= 50f -> MaterialTheme.colorScheme.primary
                                else -> Color(0xFF0284C7)
                            }
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        Text(
                            text = if (showDailyView) {
                                "/ ${FormatUtils.formatRupiah(progress.config.dailyTargetAmount)}"
                            } else {
                                "/ ${FormatUtils.formatRupiah(progress.config.targetAmount)}"
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }

                // Motivation Badge Tag
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = when {
                        isFull -> Color(0xFFDCFCE7)
                        displayPercent >= 75f -> Color(0xFFFEF3C7)
                        else -> Color(0xFFE0F2FE)
                    }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = when {
                                isFull -> Icons.Default.CheckCircle
                                displayPercent >= 75f -> Icons.Default.TrendingUp
                                else -> Icons.Default.Stars
                            },
                            contentDescription = null,
                            tint = when {
                                isFull -> Color(0xFF15803D)
                                displayPercent >= 75f -> Color(0xFFD97706)
                                else -> Color(0xFF0284C7)
                            },
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = when {
                                isFull -> "Target Lunas"
                                displayPercent >= 75f -> "Sedikit Lagi!"
                                displayPercent >= 50f -> "Setengah Jalan"
                                else -> "Dalam Proses"
                            },
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = when {
                                isFull -> Color(0xFF15803D)
                                displayPercent >= 75f -> Color(0xFF92400E)
                                else -> Color(0xFF0369A1)
                            }
                        )
                    }
                }
            }

            // VISUAL PROGRESS BAR with Milestones
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(18.dp)
                        .clip(RoundedCornerShape(9.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    // Animated Fill
                    val fillBrush = if (isFull) {
                        Brush.horizontalGradient(
                            listOf(Color(0xFF22C55E), Color(0xFF16A34A), Color(0xFF15803D))
                        )
                    } else {
                        Brush.horizontalGradient(
                            listOf(Color(0xFF38BDF8), Color(0xFF0284C7), Color(0xFF0D9488))
                        )
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(fraction = animatedProgress.coerceIn(0f, 1f))
                            .clip(RoundedCornerShape(9.dp))
                            .background(fillBrush)
                    )

                    // Milestone Tick marks: 25%, 50%, 75%
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .fillMaxHeight()
                            .padding(horizontal = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Spacer(modifier = Modifier.weight(0.25f))
                        Box(
                            modifier = Modifier
                                .width(1.5.dp)
                                .height(8.dp)
                                .background(Color.White.copy(alpha = 0.5f))
                        )
                        Spacer(modifier = Modifier.weight(0.25f))
                        Box(
                            modifier = Modifier
                                .width(1.5.dp)
                                .height(8.dp)
                                .background(Color.White.copy(alpha = 0.5f))
                        )
                        Spacer(modifier = Modifier.weight(0.25f))
                        Box(
                            modifier = Modifier
                                .width(1.5.dp)
                                .height(8.dp)
                                .background(Color.White.copy(alpha = 0.5f))
                        )
                        Spacer(modifier = Modifier.weight(0.25f))
                    }
                }

                // Milestone Labels
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("0%", style = MaterialTheme.typography.labelSmall, fontSize = 10.sp, color = MaterialTheme.colorScheme.outline)
                    Text("25%", style = MaterialTheme.typography.labelSmall, fontSize = 10.sp, color = MaterialTheme.colorScheme.outline)
                    Text("50%", style = MaterialTheme.typography.labelSmall, fontSize = 10.sp, color = MaterialTheme.colorScheme.outline)
                    Text("75%", style = MaterialTheme.typography.labelSmall, fontSize = 10.sp, color = MaterialTheme.colorScheme.outline)
                    Text("100%", style = MaterialTheme.typography.labelSmall, fontSize = 10.sp, color = MaterialTheme.colorScheme.outline)
                }
            }

            // Key Metrics Grid
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Metric Row 1: Terkumpul & Dari Omset Hari Ini
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = if (showDailyView) "Omset Cuci Hari Ini" else "Total Dana Terkumpul",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = if (showDailyView) FormatUtils.formatRupiah(progress.todayRevenue) else FormatUtils.formatRupiah(progress.totalSaved),
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Hari Ini:",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = Color(0xFFDCFCE7)
                                ) {
                                    Text(
                                        text = "+${FormatUtils.formatRupiah(progress.todayRevenue)}",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF15803D),
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            Text(
                                text = "dari total omset cuci",
                                style = MaterialTheme.typography.bodySmall,
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                    )

                    // Metric Row 2: Sisa Dana & Estimasi Hari
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = if (showDailyView) "Sisa Target Harian" else "Kekurangan Dana",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            val remainingToShow = if (showDailyView) {
                                (progress.config.dailyTargetAmount - progress.todayRevenue).coerceAtLeast(0L)
                            } else {
                                progress.remainingAmount
                            }
                            Text(
                                text = if (remainingToShow == 0L) "Lunas! 🎉" else FormatUtils.formatRupiah(remainingToShow),
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (remainingToShow == 0L) Color(0xFF16A34A) else Color(0xFFD97706)
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "Estimasi Tercapai",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = if (progress.isAchieved) {
                                    "Sudah Tercapai! ✨"
                                } else if (progress.estimatedDaysRemaining > 0) {
                                    "~${progress.estimatedDaysRemaining} hari lagi"
                                } else {
                                    "Lanjutkan cuci"
                                },
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }

            // Info Note Footer: Basis Perhitungan
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "💡 Berdasarkan ${progress.config.savingSource.label} • Rata-rata ${FormatUtils.formatRupiah(progress.averageDailyRevenue)}/hari",
                    style = MaterialTheme.typography.bodySmall,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.outline
                )
            }
        }
    }
}
