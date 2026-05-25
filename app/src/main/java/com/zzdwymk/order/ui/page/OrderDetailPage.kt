package com.zzdwymk.order.ui.page

import android.os.Build
import androidx.activity.BackEventCompat
import androidx.activity.compose.PredictiveBackHandler
import androidx.annotation.RequiresApi
import androidx.compose.animation.*
import kotlinx.coroutines.CancellationException
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalContext
import android.content.ClipData
import android.content.Context
import android.widget.Toast
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.lerp
import coil3.compose.AsyncImage
import com.kyant.backdrop.backdrops.layerBackdrop
import com.kyant.backdrop.backdrops.rememberLayerBackdrop
import com.kyant.backdrop.drawBackdrop
import com.kyant.backdrop.effects.blur
import com.kyant.backdrop.effects.vibrancy
import com.zzdwymk.order.data.Order
import com.zzdwymk.order.data.OrderStatus
import com.zzdwymk.order.data.OrderViewModel
import com.zzdwymk.order.model.Address
import com.zzdwymk.order.model.CartItem
import com.zzdwymk.order.ui.HapticUtils
import kotlinx.coroutines.launch

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalAnimationApi::class)
@Composable
fun OrderDetailPage(
    order: Order,
    orderViewModel: OrderViewModel,
    onBack: () -> Unit,
    onHome: () -> Unit,
    onViewOrders: () -> Unit,
    onDeliveryTrack: () -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    val background = colorScheme.background
    val scope = rememberCoroutineScope()
    val hapticFeedback = LocalHapticFeedback.current
    val context = LocalContext.current

    val backdrop = rememberLayerBackdrop { drawRect(background); drawContent() }

    fun copyOrderIdToClipboard() {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
        val clip = ClipData.newPlainText("order_id", order.id)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(context, "订单号已复制：${order.id}", Toast.LENGTH_SHORT).show()
    }

    val successAnimation = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        successAnimation.animateTo(
            targetValue = 1f,
            animationSpec = spring(dampingRatio = 0.5f, stiffness = Spring.StiffnessMediumLow)
        )
    }

    PredictiveBackHandler(onBack = { backEvents ->
        try {
            backEvents.collect { event ->
                if (event.progress == 0f) {
                    HapticUtils.performLightImpact(hapticFeedback)
                }
            }
            onBack()
        } catch (e: CancellationException) {
            throw e
        } catch (_: Exception) {
        }
    })

    Box(modifier = Modifier.fillMaxSize().background(background)) {

        LazyColumn(
            modifier = Modifier.fillMaxSize().layerBackdrop(backdrop),
            contentPadding = PaddingValues(top = 100.dp, bottom = 180.dp, start = 16.dp, end = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            item {
                SuccessStatusCard(
                    order = order,
                    colorScheme = colorScheme,
                    animationProgress = successAnimation.value
                )
            }

            item {
                ShopInfoDetailCard(
                    shopName = order.shopName,
                    shopPic = order.shopPic,
                    orderId = order.id,
                    colorScheme = colorScheme,
                    onCopyOrderId = { copyOrderIdToClipboard() }
                )
            }

            item {
                AddressDetailCard(
                    address = order.address,
                    colorScheme = colorScheme
                )
            }

            item {
                Text("商品清单", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = colorScheme.onSurface)
            }

            items(order.items) { item ->
                OrderItemDetailCard(item = item, colorScheme = colorScheme)
            }

            item {
                CostSummaryCard(
                    order = order,
                    colorScheme = colorScheme
                )
            }

            item {
                DeliveryTimeline(orderStatus = order.status, colorScheme = colorScheme)
            }
        }

        TopBar(
            colorScheme = colorScheme,
            backdrop = backdrop,
            onBack = {
                HapticUtils.performLightImpact(hapticFeedback)
                onBack()
            }
        )

        BottomActionBar(
            order = order,
            orderViewModel = orderViewModel,
            colorScheme = colorScheme,
            backdrop = backdrop,
            onHome = {
                HapticUtils.performMediumImpact(hapticFeedback)
                onHome()
            },
            onViewOrders = {
                HapticUtils.performMediumImpact(hapticFeedback)
                onViewOrders()
            },
            onDeliveryTrack = {
                HapticUtils.performMediumImpact(hapticFeedback)
                onDeliveryTrack()
            },
            onBack = onBack,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
private fun SuccessStatusCard(
    order: Order,
    colorScheme: ColorScheme,
    animationProgress: Float
) {
    val statusIcon = when (order.status) {
        OrderStatus.PENDING_PAYMENT -> Icons.Default.Schedule
        OrderStatus.DELIVERING -> Icons.Default.DeliveryDining
        OrderStatus.COMPLETED, OrderStatus.PENDING_REVIEW -> Icons.Default.CheckCircle
    }

    val statusText = when (order.status) {
        OrderStatus.PENDING_PAYMENT -> "等待支付"
        OrderStatus.DELIVERING -> "配送中"
        OrderStatus.COMPLETED -> "订单完成"
        OrderStatus.PENDING_REVIEW -> "待评价"
    }

    val statusColor = when (order.status) {
        OrderStatus.PENDING_PAYMENT -> colorScheme.error
        OrderStatus.DELIVERING -> colorScheme.primary
        OrderStatus.COMPLETED -> colorScheme.tertiary
        OrderStatus.PENDING_REVIEW -> colorScheme.onSurfaceVariant
    }

    Surface(
        shape = RoundedCornerShape(24.dp),
        color = colorScheme.primaryContainer.copy(alpha = 0.2f),
        border = BorderStroke(1.dp, colorScheme.primary.copy(alpha = 0.15f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .graphicsLayer {
                        scaleX = animationProgress
                        scaleY = animationProgress
                        alpha = animationProgress
                    }
                    .background(statusColor.copy(alpha = 0.15f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    statusIcon,
                    contentDescription = null,
                    tint = statusColor,
                    modifier = Modifier.size(48.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = when (order.status) {
                    OrderStatus.PENDING_PAYMENT -> "订单待支付"
                    OrderStatus.DELIVERING -> "订单已支付"
                    OrderStatus.COMPLETED -> "订单已完成"
                    OrderStatus.PENDING_REVIEW -> "等待评价"
                },
                fontSize = 22.sp,
                fontWeight = FontWeight.Black,
                color = colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(8.dp))

            val deliveryText = if (order.remark.isNotEmpty() && order.remark != "尽快送达") {
                "预计 ${order.remark} 送达"
            } else {
                "预计约30-35分钟后送达"
            }
            Text(
                text = when (order.status) {
                    OrderStatus.PENDING_PAYMENT -> if (order.remark.isNotEmpty() && order.remark != "尽快送达") "请在30分钟内完成支付，${order.remark}送达" else "请在30分钟内完成支付"
                    OrderStatus.DELIVERING -> "$deliveryText"
                    else -> "感谢您的下单"
                },
                fontSize = 13.sp,
                color = colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(12.dp))

            Surface(
                shape = RoundedCornerShape(12.dp),
                color = statusColor.copy(alpha = 0.08f)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        statusIcon,
                        contentDescription = null,
                        tint = statusColor,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        statusText,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = statusColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("实付金额", fontSize = 12.sp, color = colorScheme.onSurfaceVariant)
                    Text(
                        "¥${String.format("%.2f", order.totalAmount)}",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Black,
                        color = statusColor
                    )
                }

                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = statusColor.copy(alpha = 0.08f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.LocalShipping,
                            contentDescription = null,
                            tint = statusColor,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            statusText,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = statusColor
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ShopInfoDetailCard(
    shopName: String,
    shopPic: String,
    orderId: String,
    colorScheme: ColorScheme,
    onCopyOrderId: () -> Unit
) {
    val hapticFeedback = LocalHapticFeedback.current

    Surface(
        shape = RoundedCornerShape(20.dp),
        color = colorScheme.surfaceContainerLowest.copy(alpha = 0.5f)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AsyncImage(
                    model = shopPic,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.size(50.dp).clip(RoundedCornerShape(14.dp))
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(shopName, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = colorScheme.onSurface)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("订单号：$orderId", fontSize = 11.sp, color = colorScheme.onSurfaceVariant, fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace)
                }
                Icon(
                    Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = "复制订单号",
                    tint = colorScheme.primary.copy(alpha = 0.7f),
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) {
                            HapticUtils.performLightImpact(hapticFeedback)
                            onCopyOrderId()
                        }
                        .padding(4.dp)
                )
            }
        }
    }
}

@Composable
private fun AddressDetailCard(
    address: Address?,
    colorScheme: ColorScheme
) {
    if (address == null) return

    Surface(
        shape = RoundedCornerShape(20.dp),
        color = colorScheme.surfaceContainerLow.copy(alpha = 0.6f),
        border = BorderStroke(1.dp, colorScheme.outline.copy(alpha = 0.08f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.LocationOn,
                contentDescription = null,
                tint = colorScheme.primary,
                modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("${address.name}  ${address.phone}", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = colorScheme.onSurface)
                    if (address.isDefault) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(shape = RoundedCornerShape(4.dp), color = colorScheme.primary.copy(alpha = 0.1f)) {
                            Text("默认", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = colorScheme.primary, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                        }
                    }
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(address.detailAddress, fontSize = 12.sp, color = colorScheme.onSurfaceVariant, maxLines = 2, overflow = TextOverflow.Ellipsis)
            }
        }
    }
}

@Composable
private fun OrderItemDetailCard(
    item: CartItem,
    colorScheme: ColorScheme
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = colorScheme.surface.copy(alpha = 0.5f)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = item.foodPic,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.size(64.dp).clip(RoundedCornerShape(12.dp))
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(item.foodName, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = colorScheme.onSurface, maxLines = 1, overflow = TextOverflow.Ellipsis)
                if (item.taste.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(item.taste, fontSize = 11.sp, color = colorScheme.onSurfaceVariant, maxLines = 1)
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text("¥${String.format("%.2f", item.price)} × ${item.count}", fontSize = 13.sp, fontWeight = FontWeight.Medium, color = colorScheme.onSurfaceVariant)
            }
            Text(
                "¥${String.format("%.2f", item.price * item.count)}",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun CostSummaryCard(
    order: Order,
    colorScheme: ColorScheme
) {
    val subtotal = order.items.sumOf { it.price * it.count }

    Surface(
        shape = RoundedCornerShape(20.dp),
        color = colorScheme.surfaceContainerLowest.copy(alpha = 0.5f),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text("费用明细", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = colorScheme.onSurface)

            CostRow(label = "商品小计", value = "¥${String.format("%.2f", subtotal)}", colorScheme = colorScheme)
            CostRow(label = "配送费", value = "¥5.00", colorScheme = colorScheme)
            CostRow(label = "打包费", value = "¥2.00", colorScheme = colorScheme)

            HorizontalDivider(color = colorScheme.outline.copy(alpha = 0.15f), modifier = Modifier.padding(vertical = 4.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("实付金额", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = colorScheme.onSurface)
                Text(
                    "¥${String.format("%.2f", order.totalAmount)}",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black,
                    color = colorScheme.primary
                )
            }
        }
    }
}

@Composable
private fun CostRow(
    label: String,
    value: String,
    colorScheme: ColorScheme
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, fontSize = 13.sp, color = colorScheme.onSurfaceVariant)
        Text(value, fontSize = 13.sp, color = colorScheme.onSurface)
    }
}

@Composable
private fun DeliveryTimeline(orderStatus: OrderStatus, colorScheme: ColorScheme) {
    val steps = listOf(
        Triple("已下单", "订单已确认", orderStatus != OrderStatus.PENDING_PAYMENT),
        Triple("支付成功", "等待商家接单", orderStatus != OrderStatus.PENDING_PAYMENT),
        Triple("配送中", "骑手正在配送", orderStatus == OrderStatus.DELIVERING || orderStatus == OrderStatus.COMPLETED || orderStatus == OrderStatus.PENDING_REVIEW),
        Triple("已完成", "订单已完成", orderStatus == OrderStatus.COMPLETED || orderStatus == OrderStatus.PENDING_REVIEW)
    )

    Surface(
        shape = RoundedCornerShape(20.dp),
        color = colorScheme.surfaceContainerLowest.copy(alpha = 0.5f),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Text("订单进度", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = colorScheme.onSurface)
            Spacer(modifier = Modifier.height(16.dp))

            steps.forEachIndexed { index, (title, desc, completed) ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Top
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            modifier = Modifier
                                .size(14.dp)
                                .background(
                                    if (completed) colorScheme.primary else colorScheme.surfaceContainerHighest,
                                    CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            if (completed && index < steps.lastIndex) {
                                Icon(
                                    Icons.Default.Check,
                                    contentDescription = null,
                                    tint = colorScheme.onPrimary,
                                    modifier = Modifier.size(9.dp)
                                )
                            }
                        }
                        if (index < steps.lastIndex) {
                            Box(
                                modifier = Modifier
                                    .width(2.dp)
                                    .height(32.dp)
                                    .background(
                                        if (completed) colorScheme.primary else colorScheme.surfaceContainerHighest
                                    )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            title,
                            fontSize = 14.sp,
                            fontWeight = if (completed) FontWeight.Bold else FontWeight.Normal,
                            color = if (completed) colorScheme.onSurface else colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(desc, fontSize = 12.sp, color = colorScheme.onSurfaceVariant.copy(alpha = 0.7f))
                    }
                }

                if (index < steps.lastIndex) {
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }
        }
    }
}

@Composable
private fun TopBar(
    colorScheme: ColorScheme,
    backdrop: com.kyant.backdrop.backdrops.LayerBackdrop,
    onBack: () -> Unit
) {
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
                Text("订单详情", fontSize = 17.sp, fontWeight = FontWeight.Bold, color = colorScheme.onSurface)
            }
            Spacer(modifier = Modifier.width(44.dp))
        }
    }
}

@Composable
private fun BottomActionBar(
    order: Order,
    orderViewModel: OrderViewModel,
    colorScheme: ColorScheme,
    backdrop: com.kyant.backdrop.backdrops.LayerBackdrop,
    onHome: () -> Unit,
    onViewOrders: () -> Unit,
    onDeliveryTrack: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val homeBtnPress = remember { Animatable(0f) }
    val actionBtnPress = remember { Animatable(0f) }
    val animationScope = rememberCoroutineScope()
    val haptic = LocalHapticFeedback.current
    val context = LocalContext.current

    val bouncySpec = spring<Float>(dampingRatio = 0.2f, stiffness = Spring.StiffnessMediumLow)
    val downSpec = spring<Float>(dampingRatio = 0.7f, stiffness = 1500f)

    val showPayNow = order.status == OrderStatus.PENDING_PAYMENT
    val barHeight = 64.dp
    val horizontalPadding = 20.dp
    val internalPadding = 4.dp
    val buttonWidth = 120.dp
    val buttonHeight = barHeight - internalPadding * 2

    // 🔥 强制把按钮文本抽成独立变量，彻底解决缓存BUG
    val rightButtonText = remember(showPayNow) {
        if (showPayNow) "立即支付" else "查看配送"
    }

    Box(
        modifier = modifier
            .navigationBarsPadding()
            .padding(bottom = 24.dp)
            .fillMaxWidth()
            .height(barHeight)
    ) {
        // 左侧背景
        Box(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(start = horizontalPadding)
                .width(buttonWidth)
                .height(buttonHeight)
                .graphicsLayer {
                    val p = homeBtnPress.value
                    scaleX = lerp(1f, 0.95f, p)
                    scaleY = lerp(1f, 0.95f, p)
                }
                .background(colorScheme.surface.copy(0.3f).compositeOver(Color.White.copy(0.4f)), CircleShape)
                .drawBackdrop(backdrop, { CircleShape }, { vibrancy(); blur(8.dp.toPx()) })
        )

        // 右侧背景
        Box(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = horizontalPadding)
                .width(buttonWidth)
                .height(buttonHeight)
                .graphicsLayer {
                    val p = actionBtnPress.value
                    scaleX = lerp(1f, 0.95f, p)
                    scaleY = lerp(1f, 0.95f, p)
                }
                .background(colorScheme.surface.copy(0.3f).compositeOver(Color.White.copy(0.4f)), CircleShape)
                .drawBackdrop(backdrop, { CircleShape }, { vibrancy(); blur(8.dp.toPx()) })
        )

        // 左侧按钮
        Box(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(start = horizontalPadding)
                .width(buttonWidth)
                .height(buttonHeight)
                .clip(CircleShape)
                .graphicsLayer {
                    val p = homeBtnPress.value
                    scaleX = lerp(1f, 0.95f, p)
                    scaleY = lerp(1f, 0.95f, p)
                }
                .pointerInput(Unit) {
                    awaitEachGesture {
                        awaitFirstDown()
                        animationScope.launch { homeBtnPress.animateTo(1f, downSpec) }
                        val up = waitForUpOrCancellation()
                        if (up != null) {
                            HapticUtils.performMediumImpact(haptic)
                            if (showPayNow) {
                                orderViewModel.cancelOrder(order.id)
                                Toast.makeText(context, "订单已取消", Toast.LENGTH_SHORT).show()
                                onBack()
                            } else {
                                onHome()
                            }
                        }
                        animationScope.launch { homeBtnPress.animateTo(0f, bouncySpec) }
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    if (showPayNow) Icons.Default.Close else Icons.Default.Home,
                    contentDescription = null,
                    tint = if (showPayNow) colorScheme.error else colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    if (showPayNow) "取消订单" else "回到首页",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    color = if (showPayNow) colorScheme.error else colorScheme.onSurfaceVariant
                )
            }
        }

        // ✅✅✅ 右侧按钮 —— 绝杀修复，100%必跳
        Box(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = horizontalPadding)
                .width(buttonWidth)
                .height(buttonHeight)
                .clip(CircleShape)
                .graphicsLayer {
                    val p = actionBtnPress.value
                    scaleX = lerp(1f, 0.95f, p)
                    scaleY = lerp(1f, 0.95f, p)
                }
                // 强制让点击逻辑跟着按钮文本刷新
                .pointerInput(rightButtonText) {
                    awaitEachGesture {
                        awaitFirstDown()
                        animationScope.launch { actionBtnPress.animateTo(1f, downSpec) }
                        val up = waitForUpOrCancellation()
                        if (up != null) {
                            HapticUtils.performMediumImpact(haptic)

                            // ==========================================
                            // 【绝对正确】文本是查看配送 → 直接跳转
                            // ==========================================
                            if (rightButtonText == "查看配送") {
                                onDeliveryTrack() // 👉 直接跳！
                            } else {
                                orderViewModel.payOrder(order.id)
                                Toast.makeText(context, "支付成功！", Toast.LENGTH_SHORT).show()
                            }
                        }
                        animationScope.launch { actionBtnPress.animateTo(0f, bouncySpec) }
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    if (showPayNow) Icons.Default.Payment else Icons.Default.LocalShipping,
                    contentDescription = null,
                    tint = if (showPayNow) colorScheme.error else colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    rightButtonText,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    color = if (showPayNow) colorScheme.error else colorScheme.primary
                )
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
private fun getEstimatedDeliveryTime(): String {
    val currentTime = java.time.LocalTime.now()
    val estimatedTime = currentTime.plusMinutes(35)
    return "${estimatedTime.hour}:${String.format("%02d", estimatedTime.minute)}"
}
