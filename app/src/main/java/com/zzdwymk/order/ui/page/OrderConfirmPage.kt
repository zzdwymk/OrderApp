package com.zzdwymk.order.ui.page

import androidx.activity.BackEventCompat
import androidx.activity.compose.PredictiveBackHandler
import androidx.compose.animation.*
import kotlinx.coroutines.CancellationException
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import com.kyant.backdrop.backdrops.rememberLayerBackdrop
import com.kyant.backdrop.backdrops.layerBackdrop
import com.kyant.backdrop.drawBackdrop
import com.kyant.backdrop.effects.blur
import com.kyant.backdrop.effects.vibrancy
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalLocale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.lerp
import androidx.compose.ui.window.Dialog
import coil3.compose.AsyncImage
import com.zzdwymk.order.data.AddressViewModel
import com.zzdwymk.order.data.OrderConfirmViewModel
import com.zzdwymk.order.model.Address
import com.zzdwymk.order.model.Coupon
import com.zzdwymk.order.ui.HapticUtils
import com.zzdwymk.order.ui.component.AddAddressDialog
import kotlinx.coroutines.launch

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun OrderConfirmPage(
    viewModel: OrderConfirmViewModel,
    addressViewModel: AddressViewModel,
    onBack: () -> Unit,
    onSubmitSuccess: () -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    val background = colorScheme.background
    val scope = rememberCoroutineScope()
    val hapticFeedback = LocalHapticFeedback.current

    var showAddAddressDialog by remember { mutableStateOf(false) }
    var editingAddress by remember { mutableStateOf<Address?>(null) }

    val orderData = viewModel.orderData

    val backdrop = rememberLayerBackdrop { drawRect(background); drawContent() }

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
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                AddressCard(
                    address = orderData.address,
                    colorScheme = colorScheme,
                    onClick = {
                        HapticUtils.performLightImpact(hapticFeedback)
                        if (addressViewModel.addresses.isEmpty()) {
                            showAddAddressDialog = true
                        } else {
                            viewModel.toggleAddressPicker()
                        }
                    }
                )
            }

            item {
                ShopInfoCard(
                    shopName = orderData.shopName,
                    shopPic = orderData.shopPic,
                    colorScheme = colorScheme
                )
            }

            item {
                Text("商品清单", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = colorScheme.onSurface)
            }

            items(orderData.items) { item ->
                OrderItemCard(item = item, colorScheme = colorScheme)
            }

            if (orderData.giftItems.isNotEmpty()) {
                item {
                    GiftSection(giftItems = orderData.giftItems, colorScheme = colorScheme)
                }
            }

            item {
                CouponSection(
                    selectedCoupon = orderData.selectedCoupon,
                    availableCoupons = orderData.availableCoupons,
                    subtotal = orderData.subtotal,
                    colorScheme = colorScheme,
                    onClick = {
                        HapticUtils.performLightImpact(hapticFeedback)
                        viewModel.toggleCouponPicker()
                    }
                )
            }

            item {
                DeliveryInfoSection(viewModel = viewModel, colorScheme = colorScheme)
            }

            item {
                RemarkSection(
                    remark = orderData.remark,
                    colorScheme = colorScheme,
                    onClick = {
                        HapticUtils.performLightImpact(hapticFeedback)
                        viewModel.toggleRemarkDialog()
                    }
                )
            }

            item {
                CostDetailSection(orderData = orderData, colorScheme = colorScheme)
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

        BottomBar(
            orderData = orderData,
            isSubmitting = viewModel.isSubmitting,
            colorScheme = colorScheme,
            backdrop = backdrop,
            onSubmit = {
                HapticUtils.performMediumImpact(hapticFeedback)
                scope.launch {
                    val success = viewModel.submitOrder()
                    if (success) onSubmitSuccess()
                }
            },
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }

    if (viewModel.showAddressPicker) {
        AddressPickerDialog(
            addresses = addressViewModel.addresses,
            currentAddress = orderData.address,
            onSelect = { address -> viewModel.updateAddress(address) },
            onAddNew = {
                viewModel.toggleAddressPicker()
                showAddAddressDialog = true
            },
            onEdit = { address ->
                editingAddress = address
                viewModel.toggleAddressPicker()
                showAddAddressDialog = true
            },
            onDismiss = { viewModel.toggleAddressPicker() },
            colorScheme = colorScheme
        )
    }

    if (showAddAddressDialog) {
        AddAddressDialog(
            onDismiss = {
                showAddAddressDialog = false
                editingAddress = null
            },
            onSave = { address ->
                if (editingAddress != null) {
                    addressViewModel.updateAddress(address)
                } else {
                    addressViewModel.addAddress(address)
                }
                viewModel.updateAddress(address)
                editingAddress = null
                showAddAddressDialog = false
            },
            colorScheme = colorScheme,
            editAddress = editingAddress
        )
    }

    if (viewModel.showCouponPicker) {
        CouponPickerDialog(
            coupons = orderData.availableCoupons,
            selectedCoupon = orderData.selectedCoupon,
            subtotal = orderData.subtotal,
            onSelect = { coupon -> viewModel.selectCoupon(coupon) },
            onDismiss = { viewModel.toggleCouponPicker() },
            colorScheme = colorScheme
        )
    }

    if (viewModel.showRemarkDialog) {
        RemarkDialog(
            currentRemark = orderData.remark,
            onSave = { remark -> viewModel.updateRemark(remark) },
            onDismiss = { viewModel.toggleRemarkDialog() },
            colorScheme = colorScheme
        )
    }

    if (viewModel.showDeliveryTimePicker) {
        DeliveryTimePickerDialog(
            selectedTime = viewModel.scheduledDeliveryTime,
            onSelect = { time ->
                viewModel.updateDeliveryTime(time)
                viewModel.toggleDeliveryTimePicker()
            },
            onDismiss = { viewModel.toggleDeliveryTimePicker() },
            colorScheme = colorScheme
        )
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
                Text("确认订单", fontSize = 17.sp, fontWeight = FontWeight.Bold, color = colorScheme.onSurface)
            }
            Spacer(modifier = Modifier.width(44.dp))
        }
    }
}

@Composable
private fun AddressCard(
    address: Address?,
    colorScheme: ColorScheme,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = colorScheme.surfaceContainerLow.copy(alpha = 0.6f),
        border = BorderStroke(1.dp, colorScheme.outline.copy(alpha = 0.08f)),
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (address != null) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = colorScheme.primary,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "${address.name}  ${address.phone}",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = colorScheme.onSurface
                        )
                        if (address.isDefault) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = colorScheme.primary.copy(alpha = 0.1f)
                            ) {
                                Text(
                                    "默认",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = colorScheme.primary,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = address.detailAddress,
                        fontSize = 13.sp,
                        color = colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Icon(
                    Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = null,
                    tint = colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(24.dp).padding(start = 8.dp)
                )
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(Icons.Default.AddLocation, contentDescription = null, tint = colorScheme.primary, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("添加收货地址", fontSize = 15.sp, fontWeight = FontWeight.Medium, color = colorScheme.primary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = colorScheme.primary, modifier = Modifier.size(24.dp))
                }
            }
        }
    }
}

@Composable
private fun ShopInfoCard(
    shopName: String,
    shopPic: String,
    colorScheme: ColorScheme
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = colorScheme.surface.copy(alpha = 0.5f),
        border = BorderStroke(1.dp, colorScheme.outline.copy(alpha = 0.08f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = shopPic,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(12.dp))
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(shopName, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = colorScheme.onSurface)
                Spacer(modifier = Modifier.height(4.dp))
                Text("预计30-45分钟送达", fontSize = 12.sp, color = colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun OrderItemCard(
    item: com.zzdwymk.order.model.CartItem,
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
                Text("¥${String.format("%.2f", item.price)}", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = colorScheme.primary)
            }
            Text("x${item.count}", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun GiftSection(
    giftItems: List<com.zzdwymk.order.model.GiftItem>,
    colorScheme: ColorScheme
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = colorScheme.error.copy(alpha = 0.06f),
        border = BorderStroke(1.dp, colorScheme.error.copy(alpha = 0.15f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.CardGiftcard, contentDescription = null, tint = colorScheme.error, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("店铺赠送", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = colorScheme.error)
                Spacer(modifier = Modifier.width(6.dp))
                Text("(价值¥${giftItems.sumOf { it.originalPrice * it.count }})", fontSize = 11.sp, color = colorScheme.error.copy(alpha = 0.7f))
            }
            Spacer(modifier = Modifier.height(10.dp))
            giftItems.forEach { gift ->
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(gift.foodName, fontSize = 13.sp, color = colorScheme.onSurface)
                    Spacer(modifier = Modifier.weight(1f))
                    Text("x${gift.count}", fontSize = 13.sp, fontWeight = FontWeight.Medium, color = colorScheme.error)
                }
            }
        }
    }
}

@Composable
private fun CouponSection(
    selectedCoupon: Coupon?,
    availableCoupons: List<Coupon>,
    subtotal: Double,
    colorScheme: ColorScheme,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = colorScheme.surface.copy(alpha = 0.5f),
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.ConfirmationNumber, contentDescription = null, tint = colorScheme.primary, modifier = Modifier.size(22.dp))
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                if (selectedCoupon != null) {
                    Text(selectedCoupon.name, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = colorScheme.primary)
                    Spacer(modifier = Modifier.height(2.dp))
                    Text("-¥${String.format("%.2f", selectedCoupon.discountAmount)}", fontSize = 12.sp, color = colorScheme.error)
                } else {
                    Text(if (availableCoupons.isNotEmpty()) "${availableCoupons.size}张可用" else "暂无优惠券", fontSize = 14.sp, color = colorScheme.onSurfaceVariant)
                }
            }
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = colorScheme.onSurfaceVariant, modifier = Modifier.size(24.dp))
        }
    }
}

@Composable
private fun DeliveryInfoSection(viewModel: OrderConfirmViewModel, colorScheme: ColorScheme) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = colorScheme.surface.copy(alpha = 0.5f),
        onClick = { viewModel.toggleDeliveryTimePicker() },
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(start = 16.dp, top = 4.dp, bottom = 4.dp, end = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.Schedule, contentDescription = null, tint = colorScheme.primary, modifier = Modifier.size(22.dp))
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text("配送时间", fontSize = 14.sp, color = colorScheme.onSurface)
                Spacer(modifier = Modifier.height(2.dp))
                Text(viewModel.scheduledDeliveryTime, fontSize = 12.sp, color = colorScheme.onSurfaceVariant)
            }
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = colorScheme.onSurfaceVariant, modifier = Modifier.size(24.dp))
        }
    }

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = colorScheme.surface.copy(alpha = 0.5f),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.LocalShipping, contentDescription = null, tint = colorScheme.primary, modifier = Modifier.size(22.dp))
            Spacer(modifier = Modifier.width(12.dp))
            Text("配送方式", fontSize = 14.sp, color = colorScheme.onSurface, modifier = Modifier.weight(1f))
            Text("专人配送", fontSize = 13.sp, fontWeight = FontWeight.Medium, color = colorScheme.onSurface)
        }
    }
}

@Composable
private fun RemarkSection(
    remark: String,
    colorScheme: ColorScheme,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = colorScheme.surface.copy(alpha = 0.5f),
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.EditNote, contentDescription = null, tint = colorScheme.onSurfaceVariant, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(12.dp))
            if (remark.isNotEmpty()) {
                Text(remark, fontSize = 14.sp, color = colorScheme.onSurface, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
            } else {
                Text("口味、偏好等要求（选填）", fontSize = 14.sp, color = colorScheme.onSurfaceVariant, modifier = Modifier.weight(1f))
            }
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = colorScheme.onSurfaceVariant, modifier = Modifier.size(24.dp))
        }
    }
}

@Composable
private fun CostDetailSection(
    orderData: com.zzdwymk.order.model.OrderConfirmData,
    colorScheme: ColorScheme
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = colorScheme.surfaceContainerLowest.copy(alpha = 0.5f),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text("费用明细", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = colorScheme.onSurface)
            Spacer(modifier = Modifier.height(4.dp))

            CostRow(label = "商品小计", value = "¥${String.format("%.2f", orderData.subtotal)}", colorScheme = colorScheme)
            if (orderData.couponDiscount > 0) {
                CostRow(label = "优惠券", value = "-¥${String.format("%.2f", orderData.couponDiscount)}", isHighlight = true, colorScheme = colorScheme)
            }
            CostRow(label = "配送费", value = "¥${String.format("%.2f", orderData.deliveryFee)}", colorScheme = colorScheme)
            CostRow(label = "打包费", value = "¥${String.format("%.2f", orderData.packagingFee)}", colorScheme = colorScheme)

            if (orderData.giftValue > 0) {
                CostRow(label = "赠品价值", value = "¥${String.format("%.2f", orderData.giftValue)}", isGift = true, colorScheme = colorScheme)
            }

            HorizontalDivider(color = colorScheme.outline.copy(alpha = 0.15f), modifier = Modifier.padding(vertical = 4.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("实付金额", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = colorScheme.onSurface)
                Text(
                    "¥${String.format("%.2f", orderData.totalAmount)}",
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
    isHighlight: Boolean = false,
    isGift: Boolean = false,
    colorScheme: ColorScheme
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, fontSize = 13.sp, color = colorScheme.onSurfaceVariant)
        Text(
            value,
            fontSize = 13.sp,
            fontWeight = if (isHighlight || isGift) FontWeight.Bold else FontWeight.Normal,
            color = when {
                isHighlight -> colorScheme.error
                isGift -> colorScheme.error.copy(alpha = 0.7f)
                else -> colorScheme.onSurface
            }
        )
    }
}

@Composable
private fun BottomBar(
    orderData: com.zzdwymk.order.model.OrderConfirmData,
    isSubmitting: Boolean,
    colorScheme: ColorScheme,
    backdrop: com.kyant.backdrop.backdrops.LayerBackdrop,
    onSubmit: () -> Unit,
    modifier: Modifier = Modifier
) {
    val animationScope = rememberCoroutineScope()
    val hapticFeedback = LocalHapticFeedback.current

    val leftBtnPress = remember { Animatable(0f) }
    val rightBtnPress = remember { Animatable(0f) }
    val bouncySpec = spring<Float>(dampingRatio = 0.2f, stiffness = Spring.StiffnessMediumLow)
    val downSpec = spring<Float>(dampingRatio = 0.7f, stiffness = 1500f)

    val totalCount = orderData.items.sumOf { it.count }

    Box(
        modifier = modifier
            .navigationBarsPadding()
            .fillMaxWidth()
            .height(84.dp)
            .padding(horizontal = 12.dp, vertical = 14.dp)
    ) {
        Box(
            modifier = Modifier
                .widthIn(min = 180.dp)
                .fillMaxHeight()
                .align(Alignment.CenterStart)
                .graphicsLayer {
                    val p = leftBtnPress.value
                    scaleX = lerp(1f, 0.95f, p)
                    scaleY = lerp(1f, 0.95f, p)
                }
                .drawBackdrop(backdrop, { CircleShape }, {
                    vibrancy()
                    blur(8.dp.toPx())
                })
                .background(
                    colorScheme.surface.copy(0.3f).compositeOver(Color.White.copy(0.4f)),
                    CircleShape
                )
                .border(1.dp, Color.White.copy(alpha = 0.2f), CircleShape)
        )

        Box(
            modifier = Modifier
                .widthIn(min = 180.dp)
                .fillMaxHeight()
                .align(Alignment.CenterStart)
                .clip(CircleShape)
                .graphicsLayer {
                    val p = leftBtnPress.value
                    scaleX = lerp(1f, 0.95f, p)
                    scaleY = lerp(1f, 0.95f, p)
                }
                .pointerInput(Unit) {
                    awaitEachGesture {
                        awaitFirstDown()
                        animationScope.launch { leftBtnPress.animateTo(1f, downSpec) }
                        waitForUpOrCancellation()
                        animationScope.launch { leftBtnPress.animateTo(0f, bouncySpec) }
                    }
                },
            contentAlignment = Alignment.CenterStart
        ) {
            Row(
                modifier = Modifier.padding(start = 8.dp, end = 20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(modifier = Modifier.size(44.dp)) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape)
                            .background(colorScheme.primary.copy(alpha = 0.12f))
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.ReceiptLong, null,
                            tint = colorScheme.primary,
                            modifier = Modifier.size(20.dp).align(Alignment.Center)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column {
                    Text(
                        "$totalCount 件商品",
                        fontSize = 9.sp,
                        color = colorScheme.onSurfaceVariant
                    )
                    Text(
                        "¥${String.format(LocalLocale.current.platformLocale, "%.1f", orderData.totalAmount)}",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }

        Box(
            modifier = Modifier
                .width(100.dp)
                .fillMaxHeight()
                .align(Alignment.CenterEnd)
                .graphicsLayer {
                    val p = rightBtnPress.value
                    scaleX = lerp(1f, 0.9f, p)
                    scaleY = lerp(1f, 0.9f, p)
                }
                .drawBackdrop(backdrop, { RoundedCornerShape(28.dp) }, {
                    vibrancy()
                    blur(8.dp.toPx())
                })
                .background(
                    colorScheme.surface.copy(0.3f).compositeOver(Color.White.copy(0.4f)),
                    RoundedCornerShape(28.dp)
                )
                .border(1.dp, Color.White.copy(alpha = 0.2f), RoundedCornerShape(28.dp))
        )

        Box(
            modifier = Modifier
                .width(100.dp)
                .fillMaxHeight()
                .align(Alignment.CenterEnd)
                .clip(RoundedCornerShape(28.dp))
                .graphicsLayer {
                    val p = rightBtnPress.value
                    scaleX = lerp(1f, 0.9f, p)
                    scaleY = lerp(1f, 0.9f, p)
                    alpha = if (isSubmitting) 0.6f else 1f
                }
                .pointerInput(Unit) {
                    awaitEachGesture {
                        awaitFirstDown()
                        animationScope.launch { rightBtnPress.animateTo(1f, downSpec) }
                        val up = waitForUpOrCancellation()
                        if (up != null && !isSubmitting) {
                            HapticUtils.performMediumImpact(hapticFeedback)
                            onSubmit()
                        }
                        animationScope.launch { rightBtnPress.animateTo(0f, bouncySpec) }
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            if (isSubmitting) {
                CircularProgressIndicator(
                    modifier = Modifier.size(22.dp),
                    color = colorScheme.primary,
                    strokeWidth = 2.dp
                )
            } else {
                Text(
                    "提交订单",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = colorScheme.primary
                )
            }
        }
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
private fun AddressPickerDialog(
    addresses: List<Address>,
    currentAddress: Address?,
    onSelect: (Address) -> Unit,
    onAddNew: () -> Unit,
    onEdit: (Address) -> Unit,
    onDismiss: () -> Unit,
    colorScheme: ColorScheme
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(28.dp),
            color = colorScheme.surface,
            tonalElevation = 6.dp,
            modifier = Modifier.heightIn(max = 500.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp).verticalScroll(rememberScrollState())
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("选择收货地址", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = colorScheme.onSurface)
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "关闭", tint = colorScheme.onSurfaceVariant)
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                addresses.forEach { address ->
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = if (currentAddress?.id == address.id) colorScheme.primaryContainer.copy(alpha = 0.25f)
                                 else colorScheme.surfaceContainerHighest.copy(alpha = 0.35f),
                        border = if (currentAddress?.id == address.id) BorderStroke(2.dp, colorScheme.primary.copy(alpha = 0.5f))
                                   else BorderStroke(1.dp, colorScheme.outline.copy(alpha = 0.1f)),
                        onClick = { onSelect(address) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(address.name, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = colorScheme.onSurface)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(address.phone, fontSize = 14.sp, color = colorScheme.onSurfaceVariant)
                                    if (address.isDefault) {
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Surface(shape = RoundedCornerShape(4.dp), color = colorScheme.primary.copy(alpha = 0.1f)) {
                                            Text("默认", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = colorScheme.primary, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                                        }
                                    }
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(address.detailAddress, fontSize = 13.sp, color = colorScheme.onSurfaceVariant, maxLines = 2, overflow = TextOverflow.Ellipsis)
                                Spacer(modifier = Modifier.height(6.dp))
                                Row(
                                    horizontalArrangement = Arrangement.End,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    TextButton(onClick = { onEdit(address) }) {
                                        Text("编辑", fontSize = 12.sp, color = colorScheme.primary)
                                    }
                                }
                            }
                            if (currentAddress?.id == address.id) {
                                Icon(Icons.Default.CheckCircle, contentDescription = "已选", tint = colorScheme.primary, modifier = Modifier.size(24.dp).padding(start = 8.dp))
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedButton(
                    onClick = onAddNew,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = colorScheme.primary)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("新增地址", fontSize = 14.sp)
                }
            }
        }
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
private fun CouponPickerDialog(
    coupons: List<Coupon>,
    selectedCoupon: Coupon?,
    subtotal: Double,
    onSelect: (Coupon?) -> Unit,
    onDismiss: () -> Unit,
    colorScheme: ColorScheme
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(28.dp),
            color = colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .heightIn(max = 500.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("选择优惠券", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = colorScheme.onSurface)
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "关闭", tint = colorScheme.onSurfaceVariant)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                coupons.forEachIndexed { index, coupon ->
                    val canUse = subtotal >= coupon.minSpend

                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = if (!canUse) colorScheme.surfaceContainerHighest.copy(alpha = 0.3f) else Color.Transparent,
                        border = if (selectedCoupon?.id == coupon.id) BorderStroke(2.dp, colorScheme.primary.copy(alpha = 0.5f))
                                   else BorderStroke(1.dp, colorScheme.outline.copy(alpha = 0.1f)),
                        onClick = { if (canUse) { if (selectedCoupon?.id == coupon.id) onSelect(null) else onSelect(coupon) } },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.Bottom) {
                                    Text("¥", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = if (canUse) colorScheme.error else colorScheme.outline)
                                    Text(String.format("%.0f", coupon.discountAmount), fontSize = 28.sp, fontWeight = FontWeight.Black, color = if (canUse) colorScheme.error else colorScheme.outline)
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    if (coupon.minSpend > 0) "满${String.format("%.0f", coupon.minSpend)}元可用" else "无门槛",
                                    fontSize = 11.sp,
                                    color = if (canUse) colorScheme.onSurfaceVariant else colorScheme.outline
                                )
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(coupon.name, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = if (canUse) colorScheme.onSurface else colorScheme.outline)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("有效期至 ${coupon.expireDate}", fontSize = 11.sp, color = if (canUse) colorScheme.onSurfaceVariant.copy(alpha = 0.6f) else colorScheme.outline)
                                if (selectedCoupon?.id == coupon.id) {
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Surface(shape = CircleShape, color = colorScheme.primary) {
                                        Icon(Icons.Default.Check, contentDescription = "已选", tint = colorScheme.onPrimary, modifier = Modifier.size(20.dp).padding(2.dp))
                                    }
                                }
                            }
                        }
                    }
                    if (index < coupons.lastIndex) Spacer(modifier = Modifier.height(10.dp))
                }

                Spacer(modifier = Modifier.height(12.dp))

                if (coupons.isNotEmpty() && selectedCoupon != null) {
                    OutlinedButton(
                        onClick = { onSelect(null) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = colorScheme.error)
                    ) {
                        Text("不使用优惠券", fontSize = 14.sp, fontWeight = FontWeight.Medium)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
private fun RemarkDialog(
    currentRemark: String,
    onSave: (String) -> Unit,
    onDismiss: () -> Unit,
    colorScheme: ColorScheme
) {
    var text by remember(currentRemark) { mutableStateOf(currentRemark) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(28.dp),
            color = colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier.padding(24.dp)
            ) {
                Text("订单备注", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = colorScheme.onSurface)

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it },
                    placeholder = { Text("请输入您的口味、偏好等特殊要求...", fontSize = 14.sp, color = colorScheme.onSurfaceVariant) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 120.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = colorScheme.primary,
                        unfocusedBorderColor = colorScheme.outline.copy(alpha = 0.3f)
                    ),
                    maxLines = 5
                )

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text("取消", fontSize = 14.sp)
                    }
                    Button(
                        onClick = { onSave(text) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text("保存", fontSize = 14.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun DeliveryTimePickerDialog(
    selectedTime: String,
    onSelect: (String) -> Unit,
    onDismiss: () -> Unit,
    colorScheme: ColorScheme
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(28.dp),
            color = colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier.padding(24.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("选择配送时间", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = colorScheme.onSurface)
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "关闭", tint = colorScheme.onSurfaceVariant)
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                val timeSlots = listOf(
                    "尽快送达" to "尽快送达",
                    "10:00" to "10:00-10:30",
                    "10:30" to "10:30-11:00",
                    "11:00" to "11:00-11:30",
                    "11:30" to "11:30-12:00",
                    "12:00" to "12:00-12:30",
                    "12:30" to "12:30-13:00",
                    "13:00" to "13:00-13:30",
                    "17:00" to "17:00-17:30",
                    "17:30" to "17:30-18:00",
                    "18:00" to "18:00-18:30",
                    "18:30" to "18:30-19:00",
                    "19:00" to "19:00-19:30"
                )

                timeSlots.chunked(3).forEachIndexed { index, rowSlots ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        rowSlots.forEach { (label, value) ->
                            val isSelected = value == selectedTime
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (isSelected) colorScheme.primary.copy(alpha = 0.12f) else colorScheme.surfaceContainerHighest.copy(alpha = 0.4f),
                                border = if (isSelected) BorderStroke(1.5.dp, colorScheme.primary) else BorderStroke(1.dp, colorScheme.outline.copy(alpha = 0.15f)),
                                onClick = { onSelect(value) },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    label,
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) colorScheme.primary else colorScheme.onSurface,
                                    modifier = Modifier.padding(vertical = 10.dp),
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                        if (rowSlots.size < 3) {
                            repeat(3 - rowSlots.size) { Spacer(modifier = Modifier.weight(1f)) }
                        }
                    }
                    if (index < timeSlots.chunked(3).lastIndex) {
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
        }
    }
}
