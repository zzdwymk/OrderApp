package com.zzdwymk.order.ui.component

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp

private const val THRESHOLD_DP = 64f
private const val MAX_DRAG_DP = 120f

@Composable
fun UltraFluidRefreshBox(
    isRefreshing: Boolean,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    val density = LocalDensity.current

    val thresholdPx = THRESHOLD_DP * density.density

    var dragOffset by remember { mutableFloatStateOf(0f) }

    val animOffset by animateFloatAsState(
        targetValue = if (isRefreshing) thresholdPx else dragOffset,
        animationSpec = spring(dampingRatio = 0.65f, stiffness = 350f),
        label = "animOffset"
    )

    // Arc rotation
    val arcRotation by animateFloatAsState(
        targetValue = if (isRefreshing) 360f else 0f,
        animationSpec = if (isRefreshing) {
            infiniteRepeatable(
                animation = tween(900, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            )
        } else {
            spring(dampingRatio = 0.8f, stiffness = 300f)
        },
        label = "arcRotation"
    )

    val arcSweep by animateFloatAsState(
        targetValue = if (isRefreshing) 280f else {
            val progress = (dragOffset / thresholdPx).coerceIn(0f, 1f)
            45f + progress * 235f
        },
        animationSpec = spring(dampingRatio = 0.7f, stiffness = 350f),
        label = "arcSweep"
    )

    val pullProgress = (dragOffset / thresholdPx).coerceIn(0f, 1f)
    val isPastThreshold = dragOffset >= thresholdPx

    var refreshTriggered by remember { mutableStateOf(false) }

    LaunchedEffect(isRefreshing) {
        if (!isRefreshing) {
            refreshTriggered = false
            dragOffset = 0f
        }
    }

    val nestedScrollConnection = remember {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                if (isRefreshing) return Offset.Zero
                if (available.y < 0 && !refreshTriggered && dragOffset > 0f) {
                    dragOffset = 0f
                    return Offset(0f, -available.y)
                }
                return Offset.Zero
            }

            override fun onPostScroll(
                consumed: Offset,
                available: Offset,
                source: NestedScrollSource
            ): Offset {
                if (isRefreshing) return Offset.Zero
                if (available.y > 0) {
                    val maxDragPx = MAX_DRAG_DP * density.density
                    val newOffset = (dragOffset + available.y).coerceIn(0f, maxDragPx)
                    val consumed = newOffset - dragOffset
                    dragOffset = newOffset
                    return Offset(0f, consumed)
                }
                return Offset.Zero
            }
        }
    }

    LaunchedEffect(dragOffset, isRefreshing) {
        if (dragOffset >= thresholdPx && !isRefreshing && !refreshTriggered) {
            refreshTriggered = true
            onRefresh()
        }
    }

    Box(modifier = modifier.nestedScroll(nestedScrollConnection)) {
        // Subtle top glow
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            colorScheme.primary.copy(alpha = pullProgress * 0.15f),
                            Color.Transparent
                        )
                    )
                )
        )

        Box(modifier = Modifier.fillMaxSize()) {
            content()
        }

        // Indicator
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = with(density) { (animOffset - 52.dp.toPx()).coerceAtLeast(0f).toDp() })
                .graphicsLayer {
                    alpha = if (isRefreshing) 1f else (animOffset / 18f).coerceIn(0f, 1f)
                },
            contentAlignment = Alignment.Center
        ) {
            val radius = with(density) { 18.dp.toPx() }

            Canvas(modifier = Modifier.size(with(density) { 46.dp.toPx() }.dp)) {
                val center = Offset(size.width / 2, size.height / 2)
                val r = radius * (0.85f + pullProgress * 0.15f)

                // Glow
                if (pullProgress > 0.05f || isRefreshing) {
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                colorScheme.primary.copy(alpha = if (isRefreshing) 0.15f else pullProgress * 0.1f),
                                Color.Transparent
                            ),
                            center = center,
                            radius = r * 1.9f
                        ),
                        radius = r * 1.9f,
                        center = center
                    )
                }

                // Background
                drawCircle(
                    color = colorScheme.surface.copy(alpha = 0.92f),
                    radius = r,
                    center = center
                )

                // Arc
                val arcR = r - with(density) { 3.5.dp.toPx() }
                drawArc(
                    brush = Brush.sweepGradient(
                        0f to colorScheme.primary,
                        0.4f to colorScheme.tertiary,
                        0.75f to colorScheme.tertiaryContainer,
                        1f to colorScheme.primary,
                        center = center
                    ),
                    startAngle = arcRotation,
                    sweepAngle = arcSweep,
                    useCenter = false,
                    style = Stroke(width = with(density) { 2.8.dp.toPx() }, cap = StrokeCap.Round),
                    topLeft = Offset(center.x - arcR, center.y - arcR),
                    size = androidx.compose.ui.geometry.Size(arcR * 2, arcR * 2)
                )

                // Arrow — rotates 180° when past threshold
                if (!isRefreshing && pullProgress > 0.15f) {
                    val arrowColor = if (isPastThreshold) colorScheme.primary else colorScheme.onSurface.copy(alpha = 0.5f)
                    val arrowLen = arcR * 0.25f
                    val dir = if (isPastThreshold) -1f else 1f

                    drawLine(
                        color = arrowColor,
                        start = Offset(center.x, center.y + arrowLen * dir * 0.5f),
                        end = Offset(center.x, center.y - arrowLen * dir * 0.5f),
                        strokeWidth = with(density) { 2.dp.toPx() },
                        cap = StrokeCap.Round
                    )
                    drawLine(
                        color = arrowColor,
                        start = Offset(center.x, center.y - arrowLen * dir * 0.5f),
                        end = Offset(center.x - arrowLen * 0.5f, center.y - arrowLen * dir * 1.25f),
                        strokeWidth = with(density) { 2.dp.toPx() },
                        cap = StrokeCap.Round
                    )
                    drawLine(
                        color = arrowColor,
                        start = Offset(center.x, center.y - arrowLen * dir * 0.5f),
                        end = Offset(center.x + arrowLen * 0.5f, center.y - arrowLen * dir * 1.25f),
                        strokeWidth = with(density) { 2.dp.toPx() },
                        cap = StrokeCap.Round
                    )
                }
            }
        }
    }
}
