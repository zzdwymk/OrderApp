package com.zzdwymk.order.ui.page

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.lerp
import com.zzdwymk.order.data.Order
import com.zzdwymk.order.data.OrderStatus
import com.kyant.backdrop.backdrops.layerBackdrop
import com.kyant.backdrop.backdrops.rememberLayerBackdrop
import com.kyant.backdrop.drawBackdrop
import com.kyant.backdrop.effects.blur
import com.kyant.backdrop.effects.vibrancy
import com.zzdwymk.order.ui.HapticUtils
import kotlinx.coroutines.launch

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun DeliveryTrackingPage(
    order: Order,
    onBack: () -> Unit,
    onRiderCall: () -> Unit = {},
    onContactShop: () -> Unit = {}
) {
    val colorScheme = MaterialTheme.colorScheme
    val background = colorScheme.background
    val backdrop = rememberLayerBackdrop { drawRect(background); drawContent() }
    val scope = rememberCoroutineScope()
    val haptic = LocalHapticFeedback.current

    // Simulated rider data
    val riderName = "张师傅"
    val riderPhone = "138****1234"

    // Get delivery status based on order status
    val deliveryStatus = when (order.status) {
        OrderStatus.PENDING_PAYMENT -> "等待支付"
        OrderStatus.DELIVERING -> "商品配送中"
        OrderStatus.COMPLETED -> "配送已完成"
        OrderStatus.PENDING_REVIEW -> "待评价"
    }

    val eta = when (order.status) {
        OrderStatus.PENDING_PAYMENT -> "支付后开始配送"
        OrderStatus.DELIVERING -> "约25分钟后送达"
        OrderStatus.COMPLETED -> "已送达"
        OrderStatus.PENDING_REVIEW -> "感谢您的选择"
    }

    // Animation for rider pulsing
    val infiniteTransition = rememberInfiniteTransition(label = "rider")
    val riderPulse by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (order.status == OrderStatus.DELIVERING) 1.1f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    // Button press animation
    val contactBtnPress = remember { Animatable(0f) }
    val bouncySpec = spring<Float>(dampingRatio = 0.2f, stiffness = Spring.StiffnessMediumLow)
    val downSpec = spring<Float>(dampingRatio = 0.7f, stiffness = 1500f)

    // Delivery progress steps based on order status
    val deliverySteps = when (order.status) {
        OrderStatus.PENDING_PAYMENT -> listOf(
            DeliveryStep("已下单", "等待支付", true),
            DeliveryStep("骑手接单", "待接单", false),
            DeliveryStep("骑手到店", "待到店", false),
            DeliveryStep("商品配送中", "待配送", false),
            DeliveryStep(eta, "预计送达", false)
        )
        OrderStatus.DELIVERING -> listOf(
            DeliveryStep("已下单", "已完成", true),
            DeliveryStep("骑手接单", "已完成", true),
            DeliveryStep("骑手到店", "已完成", true),
            DeliveryStep("商品配送中", "进行中", true),
            DeliveryStep(eta, "预计送达", false)
        )
        OrderStatus.COMPLETED -> listOf(
            DeliveryStep("已下单", "已完成", true),
            DeliveryStep("骑手接单", "已完成", true),
            DeliveryStep("骑手到店", "已完成", true),
            DeliveryStep("商品配送中", "已完成", true),
            DeliveryStep("已送达", "已完成", true)
        )
        OrderStatus.PENDING_REVIEW -> listOf(
            DeliveryStep("已下单", "已完成", true),
            DeliveryStep("骑手接单", "已完成", true),
            DeliveryStep("骑手到店", "已完成", true),
            DeliveryStep("商品配送中", "已完成", true),
            DeliveryStep("已送达", "已完成", true)
        )
    }

    Box(modifier = Modifier.fillMaxSize().background(background)) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .layerBackdrop(backdrop)
                .verticalScroll(rememberScrollState())
                .padding(top = 105.dp, bottom = 100.dp)
        ) {
            // Simulated map area
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .padding(horizontal = 16.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = colorScheme.surfaceContainerHighest.copy(alpha = 0.5f),
                    border = BorderStroke(1.dp, colorScheme.outline.copy(alpha = 0.2f)),
                    modifier = Modifier.fillMaxSize()
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                when (order.status) {
                                    OrderStatus.PENDING_PAYMENT -> Icons.Default.Schedule
                                    OrderStatus.DELIVERING -> Icons.Default.DeliveryDining
                                    else -> Icons.Default.CheckCircle
                                },
                                contentDescription = null,
                                tint = when (order.status) {
                                    OrderStatus.PENDING_PAYMENT -> colorScheme.onSurfaceVariant
                                    OrderStatus.DELIVERING -> colorScheme.primary
                                    else -> colorScheme.tertiary
                                },
                                modifier = Modifier
                                    .size(48.dp)
                                    .graphicsLayer {
                                        scaleX = riderPulse
                                        scaleY = riderPulse
                                    }
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = deliveryStatus,
                                fontSize = 14.sp,
                                color = colorScheme.onSurface,
                                fontWeight = FontWeight.Medium
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = eta,
                                fontSize = 20.sp,
                                color = when (order.status) {
                                    OrderStatus.PENDING_PAYMENT -> colorScheme.onSurfaceVariant
                                    OrderStatus.DELIVERING -> colorScheme.primary
                                    else -> colorScheme.tertiary
                                },
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Rider info card
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = colorScheme.primaryContainer.copy(alpha = 0.2f),
                border = BorderStroke(1.dp, colorScheme.outline.copy(alpha = 0.1f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Rider avatar
                    Surface(
                        shape = CircleShape,
                        color = colorScheme.primaryContainer,
                        modifier = Modifier.size(56.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                Icons.Default.Person,
                                contentDescription = null,
                                tint = colorScheme.primary,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "配送骑手",
                            fontSize = 12.sp,
                            color = colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = riderName,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = colorScheme.onSurface
                        )
                        Text(
                            text = riderPhone,
                            fontSize = 13.sp,
                            color = colorScheme.onSurfaceVariant
                        )
                    }

                    // Call rider button
                    FilledIconButton(
                        onClick = onRiderCall,
                        colors = IconButtonDefaults.filledIconButtonColors(
                            containerColor = colorScheme.primaryContainer
                        ),
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(
                            Icons.Default.Phone,
                            contentDescription = "联系骑手",
                            tint = colorScheme.primary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Delivery progress
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = colorScheme.surface.copy(alpha = 0.85f),
                border = BorderStroke(1.dp, colorScheme.outline.copy(alpha = 0.1f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "配送进度",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    deliverySteps.forEachIndexed { index, step ->
                        DeliveryStepItem(
                            step = step,
                            isLast = index == deliverySteps.lastIndex,
                            colorScheme = colorScheme
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Order info
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = colorScheme.surface.copy(alpha = 0.85f),
                border = BorderStroke(1.dp, colorScheme.outline.copy(alpha = 0.1f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "订单信息",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    InfoRow("商家", order.shopName, colorScheme)
                    Spacer(modifier = Modifier.height(8.dp))
                    InfoRow("订单号", order.id.takeLast(8), colorScheme)
                    Spacer(modifier = Modifier.height(8.dp))
                    InfoRow("配送地址", order.address?.detailAddress ?: "未知地址", colorScheme)
                    Spacer(modifier = Modifier.height(8.dp))
                    InfoRow("订单状态", order.status.displayName, colorScheme)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }

        // Bottom button with liquid glass
        Box(
            modifier = Modifier.height(90.dp)
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(horizontal = 16.dp, vertical = 22.dp)
                .graphicsLayer {
                    val p = contactBtnPress.value
                    scaleX = lerp(1f, 0.96f, p)
                    scaleY = lerp(1f, 0.96f, p)
                }
                .drawBackdrop(backdrop, { RoundedCornerShape(200.dp) }, { vibrancy(); blur(8.dp.toPx()) }, onDrawSurface = { drawRect(colorScheme.primary.copy(alpha = 0.12f)) })
                .background(
                    colorScheme.primaryContainer.copy(alpha = 0.4f).compositeOver(Color.White.copy(0.5f)),
                    RoundedCornerShape(20.dp)
                )
                .border(1.dp, colorScheme.primary.copy(alpha = 0.3f), RoundedCornerShape(20.dp))
                .pointerInput(Unit) {
                    awaitEachGesture {
                        awaitFirstDown()
                        scope.launch { contactBtnPress.animateTo(1f, downSpec) }
                        val up = waitForUpOrCancellation()
                        if (up != null) {
                            HapticUtils.performMediumImpact(haptic)
                            onContactShop()
                        }
                        scope.launch { contactBtnPress.animateTo(0f, bouncySpec) }
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    Icons.Default.Store,
                    contentDescription = null,
                    tint = colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "联系商家",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = colorScheme.primary
                )
            }
        }

        // Top bar with liquid glass effect (same style as other pages)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(colorScheme.surface.copy(0.7f).compositeOver(Color.White.copy(0.4f)), RoundedCornerShape(0.dp))
                .drawBackdrop(backdrop, { RoundedCornerShape(0.dp) }, { vibrancy(); blur(8.dp.toPx()) }, onDrawSurface = { drawRect(colorScheme.primary.copy(alpha = 0.08f)) })
                .statusBarsPadding()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "返回",
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { onBack() }
                        .padding(8.dp),
                    tint = colorScheme.onSurface
                )
                Spacer(modifier = Modifier.width(4.dp))
                Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                    Text("查看配送", fontSize = 17.sp, fontWeight = FontWeight.Bold, color = colorScheme.onSurface)
                }
                Spacer(modifier = Modifier.width(44.dp))
            }
        }
    }
}

data class DeliveryStep(
    val title: String,
    val status: String,
    val isCompleted: Boolean
)

@Composable
private fun DeliveryStepItem(
    step: DeliveryStep,
    isLast: Boolean,
    colorScheme: ColorScheme
) {
    Row(modifier = Modifier.fillMaxWidth()) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.width(60.dp)
        ) {
            // Timeline dot
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .clip(CircleShape)
                    .background(
                        if (step.isCompleted) colorScheme.primary
                        else colorScheme.surfaceVariant
                    )
                    .border(
                        2.dp,
                        if (step.isCompleted) colorScheme.primary
                        else colorScheme.outline.copy(alpha = 0.3f),
                        CircleShape
                    )
            )
            // Timeline line
            if (!isLast) {
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .height(40.dp)
                        .background(
                            if (step.isCompleted) colorScheme.primary.copy(alpha = 0.3f)
                            else colorScheme.surfaceVariant
                        )
                )
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f).padding(bottom = if (isLast) 0.dp else 16.dp)) {
            Text(
                text = step.title,
                fontSize = 14.sp,
                fontWeight = if (step.isCompleted) FontWeight.Medium else FontWeight.Normal,
                color = if (step.isCompleted) colorScheme.onSurface else colorScheme.onSurfaceVariant
            )
            Text(
                text = step.status,
                fontSize = 12.sp,
                color = if (step.isCompleted) colorScheme.primary else colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun InfoRow(
    label: String,
    value: String,
    colorScheme: ColorScheme
) {
    Row(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = label,
            fontSize = 13.sp,
            color = colorScheme.onSurfaceVariant,
            modifier = Modifier.width(70.dp)
        )
        Text(
            text = value,
            fontSize = 13.sp,
            color = colorScheme.onSurface
        )
    }
}
