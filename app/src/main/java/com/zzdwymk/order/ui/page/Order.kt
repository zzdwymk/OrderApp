package com.zzdwymk.order.ui.page

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.kyant.backdrop.backdrops.layerBackdrop
import com.kyant.backdrop.backdrops.rememberLayerBackdrop
import com.kyant.backdrop.drawBackdrop
import com.kyant.backdrop.effects.blur
import com.kyant.backdrop.effects.vibrancy
import com.zzdwymk.order.data.Order
import com.zzdwymk.order.data.OrderStatus
import com.zzdwymk.order.data.OrderViewModel
import com.zzdwymk.order.ui.HapticUtils
import kotlin.math.min

@Composable
fun OrderPage(
    viewModel: OrderViewModel,
    onNavigateToDeliveryTrack: (String) -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    val background = colorScheme.background
    val scrollState = rememberLazyListState()
    val screenWidth = LocalConfiguration.current.screenWidthDp.dp
    val haptic = LocalHapticFeedback.current

    val backdrop = rememberLayerBackdrop { drawRect(background); drawContent() }

    val scrollProgress by remember {
        derivedStateOf {
            if (scrollState.firstVisibleItemIndex > 0) 1f
            else min(1f, scrollState.firstVisibleItemScrollOffset.toFloat() / 300f)
        }
    }

    val animatedProgress by animateFloatAsState(targetValue = scrollProgress, animationSpec = spring(dampingRatio = 0.6f, stiffness = 400f), label = "scrollProgress")
    val titleSize = (18f + (24f - 18f) * animatedProgress).sp
    val horizontalPadding = 16.dp
    val titleEstimatedWidth = 80.dp
    val targetCenterX = (screenWidth / 2) - (titleEstimatedWidth / 2) - horizontalPadding
    val offsetX by animateDpAsState(targetValue = targetCenterX * animatedProgress, animationSpec = spring(dampingRatio = 0.6f, stiffness = 400f), label = "offsetX")

    Box(modifier = Modifier.fillMaxSize().background(background)) {
        LazyColumn(
            state = scrollState,
            modifier = Modifier.fillMaxSize().layerBackdrop(backdrop),
            contentPadding = PaddingValues(top = 100.dp, bottom = 120.dp, start = 16.dp, end = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (viewModel.orders.isEmpty()) {
                item {
                    EmptyOrderState(colorScheme = colorScheme)
                }
            } else {
                items(viewModel.orders, key = { it.id }) { order ->
                    OrderCard(
                        order = order,
                        colorScheme = colorScheme,
                        onPay = {
                            HapticUtils.performLightImpact(haptic)
                            viewModel.payOrder(order.id)
                        },
                        onCancel = {
                            HapticUtils.performLightImpact(haptic)
                            viewModel.cancelOrder(order.id)
                        },
                        onDeliveryTrack = {
                            HapticUtils.performLightImpact(haptic)
                            onNavigateToDeliveryTrack(order.id)
                        }
                    )
                }
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(colorScheme.surface.copy(0.3f).compositeOver(Color.White.copy(0.4f)), RoundedCornerShape(0.dp))
                .drawBackdrop(backdrop, { RoundedCornerShape(0.dp) }, { vibrancy(); blur(8.dp.toPx()) }, onDrawSurface = { drawRect(colorScheme.primaryContainer.copy(alpha = 0.3f)) })
                .statusBarsPadding()
                .height(48.dp)
        ) {
            Text(
                text = "我的订单",
                fontSize = titleSize,
                fontWeight = FontWeight.Black,
                color = colorScheme.onSurface,
                modifier = Modifier.padding(horizontal = horizontalPadding).align(Alignment.CenterStart).offset(x = offsetX)
            )
        }
    }
}

@Composable
private fun EmptyOrderState(colorScheme: ColorScheme) {
    Surface(
        shape = RoundedCornerShape(24.dp),
        color = colorScheme.surface.copy(alpha = 0.7f),
        border = BorderStroke(1.dp, colorScheme.outline.copy(alpha = 0.1f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(48.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                Icons.Default.Receipt,
                contentDescription = null,
                tint = colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                modifier = Modifier.size(72.dp)
            )
            Spacer(modifier = Modifier.height(20.dp))
            Text(
                text = "暂无订单",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "快去选购美食吧",
                fontSize = 14.sp,
                color = colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun OrderCard(
    order: Order,
    colorScheme: ColorScheme,
    onPay: () -> Unit,
    onCancel: () -> Unit,
    onDeliveryTrack: () -> Unit
) {
    val statusColor = when (order.status) {
        OrderStatus.PENDING_PAYMENT -> colorScheme.error
        OrderStatus.DELIVERING -> colorScheme.primary
        OrderStatus.COMPLETED -> colorScheme.tertiary
        OrderStatus.PENDING_REVIEW -> colorScheme.onSurfaceVariant
    }

    val statusIcon = when (order.status) {
        OrderStatus.PENDING_PAYMENT -> Icons.Default.Schedule
        OrderStatus.DELIVERING -> Icons.Default.DeliveryDining
        OrderStatus.COMPLETED, OrderStatus.PENDING_REVIEW -> Icons.Default.CheckCircle
    }

    Surface(
        shape = RoundedCornerShape(20.dp),
        color = colorScheme.surface.copy(alpha = 0.85f),
        border = BorderStroke(1.dp, colorScheme.outline.copy(alpha = 0.08f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header: Shop name and status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    AsyncImage(
                        model = order.shopPic,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(10.dp))
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = order.shopName,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = colorScheme.onSurface
                    )
                }
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = statusColor.copy(alpha = 0.12f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = statusIcon,
                            contentDescription = null,
                            tint = statusColor,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = order.status.displayName,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = statusColor
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Items preview
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (order.items.isNotEmpty()) {
                    AsyncImage(
                        model = order.items.first().foodPic,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(56.dp)
                            .clip(RoundedCornerShape(12.dp))
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(colorScheme.secondaryContainer)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    val itemNames = order.items.take(2).joinToString(", ") { it.foodName }
                    val remaining = if (order.items.size > 2) " 等${order.items.size}件" else ""
                    Text(
                        text = itemNames + remaining,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "共${order.items.sumOf { it.count }}件商品",
                        fontSize = 11.sp,
                        color = colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
            HorizontalDivider(color = colorScheme.outline.copy(alpha = 0.1f))
            Spacer(modifier = Modifier.height(14.dp))

            // Footer: Price and actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "实付",
                        fontSize = 11.sp,
                        color = colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "¥${String.format("%.2f", order.totalAmount)}",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black,
                        color = statusColor
                    )
                }

                when (order.status) {
                    OrderStatus.PENDING_PAYMENT -> {
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            OutlinedButton(
                                onClick = onCancel,
                                shape = RoundedCornerShape(20.dp),
                                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = colorScheme.onSurfaceVariant)
                            ) {
                                Text("取消", fontSize = 13.sp, fontWeight = FontWeight.Medium)
                            }
                            Button(
                                onClick = onPay,
                                shape = RoundedCornerShape(20.dp),
                                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = colorScheme.error)
                            ) {
                                Text("立即支付", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                    OrderStatus.DELIVERING -> {
                        Button(
                            onClick = onDeliveryTrack,
                            shape = RoundedCornerShape(20.dp),
                            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = statusColor)
                        ) {
                            Icon(Icons.Default.LocalShipping, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("查看配送", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                    OrderStatus.COMPLETED -> {
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            OutlinedButton(
                                onClick = { },
                                shape = RoundedCornerShape(20.dp),
                                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                            ) {
                                Text("评价", fontSize = 13.sp, fontWeight = FontWeight.Medium)
                            }
                            Button(
                                onClick = { },
                                shape = RoundedCornerShape(20.dp),
                                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = statusColor)
                            ) {
                                Text("再来一单", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                    OrderStatus.PENDING_REVIEW -> {
                        OutlinedButton(
                            onClick = { },
                            shape = RoundedCornerShape(20.dp),
                            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp)
                        ) {
                            Text("查看评价", fontSize = 13.sp, fontWeight = FontWeight.Medium)
                        }
                    }
                }
            }
        }
    }
}
