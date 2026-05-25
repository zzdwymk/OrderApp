package com.zzdwymk.order.ui.page

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.lerp
import androidx.compose.ui.window.Dialog
import com.zzdwymk.order.R
import com.zzdwymk.order.data.AddressViewModel
import com.zzdwymk.order.model.Address
import com.zzdwymk.order.ui.component.AddAddressDialog
import com.kyant.backdrop.backdrops.layerBackdrop
import com.kyant.backdrop.backdrops.rememberLayerBackdrop
import com.kyant.backdrop.drawBackdrop
import com.kyant.backdrop.effects.blur
import com.kyant.backdrop.effects.vibrancy
import kotlinx.coroutines.launch
import kotlin.math.min
import androidx.compose.ui.graphics.Color

@Preview
@Composable
fun ProfileHeaderCardPreview() {
    val colorScheme = MaterialTheme.colorScheme
    MaterialTheme(colorScheme = colorScheme) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .background(colorScheme.background)
        ) {
            ProfileHeaderCard(colorScheme)
        }
    }
}

@Preview
@Composable
fun AssetsRowPreview() {
    val colorScheme = MaterialTheme.colorScheme
    MaterialTheme(colorScheme = colorScheme) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .background(colorScheme.background)
        ) {
            AssetsRow(colorScheme)
        }
    }
}

@Preview
@Composable
fun ProfileMenuItemPreview() {
    val colorScheme = MaterialTheme.colorScheme
    MaterialTheme(colorScheme = colorScheme) {
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.padding(16.dp)
        ) {
            ProfileMenuItem("收货地址", Icons.Default.LocationOn, colorScheme.primary)
            ProfileMenuItem("评价中心", Icons.Default.Star, colorScheme.tertiary)
            ProfileMenuItem("我的收藏", Icons.Default.Favorite, colorScheme.error)
        }
    }
}

@Preview(showSystemUi = true)
@Composable
fun MyProfilePagePreview() {
    val colorScheme = MaterialTheme.colorScheme
    Box(modifier = Modifier.fillMaxSize().background(colorScheme.background)) {
        LazyColumn(contentPadding = PaddingValues(top = 100.dp, bottom = 120.dp, start = 20.dp, end = 20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            item { ProfileHeaderCard(colorScheme) }
            item { AssetsRow(colorScheme) }
            item { Text("常用功能", fontWeight = FontWeight.Bold, fontSize = 16.sp, modifier = Modifier.padding(top = 8.dp)) }
            item { ProfileMenuItem("收货地址", Icons.Default.LocationOn, colorScheme.primary) }
            item { ProfileMenuItem("评价中心", Icons.Default.Star, colorScheme.tertiary) }
            item { ProfileMenuItem("我的收藏", Icons.Default.Favorite, colorScheme.error) }
            item { ProfileMenuItem("客服与帮助", Icons.Default.SupportAgent, colorScheme.primary) }
            item { Text("设置", fontWeight = FontWeight.Bold, fontSize = 16.sp, modifier = Modifier.padding(top = 8.dp)) }
            item { ProfileMenuItem("账号安全", Icons.Default.Security, colorScheme.primary) }
            item { ProfileMenuItem("通知设置", Icons.Default.Notifications, colorScheme.tertiary) }
            item { ProfileMenuItem("隐私设置", Icons.Default.Lock, colorScheme.error) }
            item { ProfileMenuItem("资质规则", Icons.Default.Description, colorScheme.onSurfaceVariant) }
            item { ProfileMenuItem("关于我们", Icons.Default.Info, colorScheme.onSurfaceVariant) }
        }
        Box(Modifier.fillMaxWidth().height(48.dp).statusBarsPadding().background(colorScheme.surface.copy(alpha = 0.7f))) {
            Text("我的", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = colorScheme.onSurface, modifier = Modifier.padding(horizontal = 16.dp).align(Alignment.CenterStart))
        }
    }
}

@Composable
fun MyProfilePage(
    addressViewModel: AddressViewModel
) {
    val colorScheme = MaterialTheme.colorScheme
    val background = colorScheme.background
    val scrollState = rememberLazyListState()

    var showAddressDialog by remember { mutableStateOf(false) }
    var showAddAddressDialog by remember { mutableStateOf(false) }
    var selectedAddress by remember { mutableStateOf<Address?>(null) }
    var editingAddress by remember { mutableStateOf<Address?>(null) }

    val backdrop = rememberLayerBackdrop { drawRect(background); drawContent() }

    val scrollProgress by remember {
        derivedStateOf {
            if (scrollState.firstVisibleItemIndex > 0) 1f
            else min(1f, scrollState.firstVisibleItemScrollOffset.toFloat() / 300f)
        }
    }

    val animatedProgress by animateFloatAsState(targetValue = scrollProgress, animationSpec = spring(dampingRatio = 0.6f, stiffness = 400f), label = "scrollProgress")
    val titleSize = (18f + (24f - 18f) * animatedProgress).sp

    val density = LocalDensity.current
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp
    var titleWidthDp by remember { mutableFloatStateOf(0f) }
    val horizontalPadding = 16.dp

    val targetCenterX = remember(screenWidth, titleWidthDp) {
        if (titleWidthDp > 0f) {
            (screenWidth / 2) - (titleWidthDp.dp / 2) - horizontalPadding
        } else 0.dp
    }

    val offsetX by animateDpAsState(
        targetValue = targetCenterX * animatedProgress,
        animationSpec = spring(dampingRatio = 0.6f, stiffness = 400f),
        label = "offsetX"
    )

    Box(modifier = Modifier.fillMaxSize().background(background)) {
        LazyColumn(
            state = scrollState,
            modifier = Modifier.fillMaxSize().layerBackdrop(backdrop),
            contentPadding = PaddingValues(top = 100.dp, bottom = 120.dp, start = 20.dp, end = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { ProfileHeaderCard(colorScheme) }
            item { AssetsRow(colorScheme) }
            item { OrderStatusRow(colorScheme) }
            item { Text("常用功能", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = colorScheme.onSurface, modifier = Modifier.padding(top = 8.dp)) }
            item {
                ProfileMenuItem(
                    label = "收货地址",
                    icon = Icons.Default.LocationOn,
                    iconTint = colorScheme.primary,
                    onClick = { showAddressDialog = true }
                )
            }
            item { ProfileMenuItem("评价中心", Icons.Default.Star, colorScheme.tertiary) { } }
            item { ProfileMenuItem("我的收藏", Icons.Default.Favorite, colorScheme.error) { } }
            item { ProfileMenuItem("客服与帮助", Icons.Default.SupportAgent, colorScheme.primary) { } }
            item { Text("设置", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = colorScheme.onSurface, modifier = Modifier.padding(top = 8.dp)) }
            item { ProfileMenuItem("账号安全", Icons.Default.Security, colorScheme.primary) { } }
            item { ProfileMenuItem("通知设置", Icons.Default.Notifications, colorScheme.tertiary) { } }
            item { ProfileMenuItem("隐私设置", Icons.Default.Lock, colorScheme.error) { } }
            item { ProfileMenuItem("资质规则", Icons.Default.Description, colorScheme.onSurfaceVariant) { } }
            item { ProfileMenuItem("关于我们", Icons.Default.Info, colorScheme.onSurfaceVariant) { } }
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
                text = "我的",
                fontSize = titleSize,
                fontWeight = FontWeight.Black,
                color = colorScheme.onSurface,
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .padding(start = horizontalPadding)
                    .offset { IntOffset(offsetX.roundToPx(), 0) }
                    .onGloballyPositioned { titleWidthDp = with(density) { it.size.width.toDp().value } }
            )
        }
    }

    // Address management dialog
    if (showAddressDialog) {
        AddressManagementDialog(
            addresses = addressViewModel.addresses,
            onAddressClick = { address ->
                selectedAddress = address
                showAddressDialog = false
            },
            onAddNew = {
                showAddressDialog = false
                showAddAddressDialog = true
            },
            onDelete = { addressId ->
                addressViewModel.deleteAddress(addressId)
            },
            onSetDefault = { addressId ->
                addressViewModel.setDefaultAddress(addressId)
            },
            onEdit = { address ->
                editingAddress = address
                showAddressDialog = false
                showAddAddressDialog = true
            },
            onDismiss = { showAddressDialog = false },
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
                editingAddress = null
                showAddAddressDialog = false
            },
            colorScheme = colorScheme,
            editAddress = editingAddress
        )
    }
}

@Composable
private fun ProfileHeaderCard(colorScheme: androidx.compose.material3.ColorScheme) {
    val scope = rememberCoroutineScope()
    val press = remember { Animatable(0f) }
    val downSpec = spring<Float>(dampingRatio = 0.7f, stiffness = 1500f)
    val bouncySpec = spring<Float>(dampingRatio = 0.35f, stiffness = Spring.StiffnessLow)
    Box(
        modifier = Modifier.fillMaxWidth()
            .graphicsLayer { scaleX = lerp(1f, 0.97f, press.value); scaleY = lerp(1f, 0.97f, press.value); translationY = lerp(0f, 3.dp.toPx(), press.value) }
            .background(colorScheme.primaryContainer.copy(alpha = 0.3f), RoundedCornerShape(24.dp))
            .border(1.dp, colorScheme.outlineVariant.copy(alpha = 0.3f), RoundedCornerShape(24.dp))
            .pointerInput(Unit) { awaitEachGesture { awaitFirstDown(requireUnconsumed = false); scope.launch { press.animateTo(1f, downSpec) }; waitForUpOrCancellation(); scope.launch { press.animateTo(0f, bouncySpec) } } }
            .padding(20.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(64.dp).clip(CircleShape).background(colorScheme.surface.copy(alpha = 0.5f), CircleShape).border(2.dp, colorScheme.primary.copy(alpha = 0.3f), CircleShape), contentAlignment = Alignment.Center) {
                Icon(painterResource(R.drawable.icon), null, Modifier.size(52.dp).clip(CircleShape), tint = Color.Unspecified)
            }
            Spacer(Modifier.width(16.dp))
            Column(Modifier.weight(1f)) {
                Text("吃货小助手", fontWeight = FontWeight.Black, fontSize = 22.sp, color = colorScheme.onSurface)
                Spacer(Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(18.dp).background(colorScheme.tertiary.copy(alpha = 0.2f), CircleShape), contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.AutoAwesome, null, Modifier.size(12.dp), tint = colorScheme.tertiary)
                    }
                    Spacer(Modifier.width(6.dp))
                    Text("超级尊享会员", fontSize = 13.sp, color = colorScheme.tertiary, fontWeight = FontWeight.Bold)
                }
            }
            Icon(Icons.Default.ChevronRight, null, tint = colorScheme.onSurfaceVariant.copy(alpha = 0.4f), modifier = Modifier.size(20.dp))
        }
    }
}

@Composable
private fun OrderStatusRow(colorScheme: androidx.compose.material3.ColorScheme) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        OrderStatusItem(Modifier.weight(1f), "待付款", Icons.Default.Payment, colorScheme.error, colorScheme)
        OrderStatusItem(Modifier.weight(1f), "待发货", Icons.Default.LocalShipping, colorScheme.primary, colorScheme)
        OrderStatusItem(Modifier.weight(1f), "待收货", Icons.Default.Inventory2, colorScheme.tertiary, colorScheme)
        OrderStatusItem(Modifier.weight(1f), "待评价", Icons.Default.RateReview, colorScheme.secondary, colorScheme)
    }
}

@Composable
private fun OrderStatusItem(
    modifier: Modifier,
    label: String,
    icon: ImageVector,
    accentColor: Color,
    colorScheme: androidx.compose.material3.ColorScheme
) {
    val scope = rememberCoroutineScope()
    val press = remember { Animatable(0f) }
    val downSpec = spring<Float>(dampingRatio = 0.7f, stiffness = 1500f)
    val bouncySpec = spring<Float>(dampingRatio = 0.35f, stiffness = Spring.StiffnessLow)
    Box(
        modifier = modifier.height(80.dp)
            .graphicsLayer { scaleX = lerp(1f, 0.93f, press.value); scaleY = lerp(1f, 0.93f, press.value); translationY = lerp(0f, 4.dp.toPx(), press.value) }
            .background(accentColor.copy(alpha = 0.08f), RoundedCornerShape(20.dp))
            .border(1.dp, accentColor.copy(alpha = 0.15f), RoundedCornerShape(20.dp))
            .pointerInput(Unit) { awaitEachGesture { awaitFirstDown(requireUnconsumed = false); scope.launch { press.animateTo(1f, downSpec) }; waitForUpOrCancellation(); scope.launch { press.animateTo(0f, bouncySpec) } } }
            .padding(8.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            Box(Modifier.size(28.dp).background(accentColor.copy(alpha = 0.12f), CircleShape), contentAlignment = Alignment.Center) {
                Icon(icon, null, tint = accentColor, modifier = Modifier.size(16.dp))
            }
            Spacer(Modifier.height(6.dp))
            Text(label, fontSize = 11.sp, fontWeight = FontWeight.Medium, color = colorScheme.onSurface)
        }
    }
}

@Composable
private fun AssetsRow(colorScheme: androidx.compose.material3.ColorScheme) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        AssetCard(Modifier.weight(1f), "12", "红包卡券", colorScheme.error, colorScheme.errorContainer, colorScheme)
        AssetCard(Modifier.weight(1f), "280", "能量积分", colorScheme.tertiary, colorScheme.tertiaryContainer, colorScheme)
    }
}

@Composable
private fun AssetCard(modifier: Modifier, value: String, label: String, accentColor: Color, containerColor: Color, colorScheme: androidx.compose.material3.ColorScheme) {
    val press = remember { Animatable(0f) }
    val scope = rememberCoroutineScope()
    val downSpec = spring<Float>(dampingRatio = 0.7f, stiffness = 1500f)
    val bouncySpec = spring<Float>(dampingRatio = 0.35f, stiffness = Spring.StiffnessLow)
    Box(
        modifier = modifier.height(80.dp)
            .graphicsLayer { scaleX = lerp(1f, 0.95f, press.value); scaleY = lerp(1f, 0.95f, press.value) }
            .background(containerColor.copy(alpha = 0.3f), RoundedCornerShape(20.dp))
            .border(1.dp, colorScheme.outlineVariant.copy(alpha = 0.3f), RoundedCornerShape(20.dp))
            .pointerInput(Unit) { awaitEachGesture { awaitFirstDown(requireUnconsumed = false); scope.launch { press.animateTo(1f, downSpec) }; waitForUpOrCancellation(); scope.launch { press.animateTo(0f, bouncySpec) } } }
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) { Column(horizontalAlignment = Alignment.CenterHorizontally) { Text(value, fontSize = 22.sp, fontWeight = FontWeight.Black, color = accentColor); Spacer(Modifier.height(4.dp)); Text(label, fontSize = 12.sp, color = colorScheme.onSurfaceVariant) } }
}

@Composable
private fun ProfileMenuItem(
    label: String,
    icon: ImageVector,
    iconTint: Color,
    onClick: () -> Unit = {}
) {
    val colorScheme = MaterialTheme.colorScheme
    val scope = rememberCoroutineScope()
    val press = remember { Animatable(0f) }
    val downSpec = spring<Float>(dampingRatio = 0.7f, stiffness = 1500f)
    val bouncySpec = spring<Float>(dampingRatio = 0.35f, stiffness = Spring.StiffnessLow)
    Box(
        modifier = Modifier.fillMaxWidth().height(64.dp)
            .graphicsLayer { val p = press.value; scaleX = lerp(1f, 0.97f, p); scaleY = lerp(1f, 0.97f, p); translationY = lerp(0f, 3.dp.toPx(), p) }
            .background(colorScheme.primaryContainer.copy(alpha = 0.2f), RoundedCornerShape(20.dp))
            .border(1.dp, colorScheme.outlineVariant.copy(alpha = 0.3f), RoundedCornerShape(20.dp))
            .pointerInput(Unit) { awaitEachGesture { awaitFirstDown(requireUnconsumed = false); scope.launch { press.animateTo(1f, downSpec) }; val up = waitForUpOrCancellation(); if (up != null) { onClick(); scope.launch { press.animateTo(0f, bouncySpec) } } else { scope.launch { press.animateTo(0f, bouncySpec) } } } }
            .padding(horizontal = 20.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(36.dp).background(colorScheme.primaryContainer.copy(alpha = 0.5f), CircleShape), contentAlignment = Alignment.Center) { Icon(icon, null, tint = iconTint, modifier = Modifier.size(20.dp)) }
            Spacer(Modifier.width(14.dp))
            Text(label, fontWeight = FontWeight.Medium, fontSize = 15.sp, color = colorScheme.onSurface)
            Spacer(Modifier.weight(1f))
            Icon(Icons.Default.ChevronRight, null, tint = colorScheme.onSurfaceVariant.copy(alpha = 0.4f))
        }
    }
}

@Composable
private fun AddressManagementDialog(
    addresses: List<Address>,
    onAddressClick: (Address) -> Unit,
    onAddNew: () -> Unit,
    onDelete: (String) -> Unit,
    onSetDefault: (String) -> Unit,
    onEdit: (Address) -> Unit,
    onDismiss: () -> Unit,
    colorScheme: androidx.compose.material3.ColorScheme
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(28.dp),
            color = colorScheme.surface,
            tonalElevation = 6.dp,
            modifier = Modifier.fillMaxWidth().heightIn(max = 500.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp).verticalScroll(rememberScrollState())
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("管理收货地址", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = colorScheme.onSurface)
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "关闭", tint = colorScheme.onSurfaceVariant)
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                if (addresses.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxWidth().height(100.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("暂无收货地址", fontSize = 14.sp, color = colorScheme.onSurfaceVariant)
                    }
                } else {
                    addresses.forEach { address ->
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = colorScheme.surfaceContainerHighest.copy(alpha = 0.35f),
                            border = BorderStroke(1.dp, colorScheme.outline.copy(alpha = 0.1f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
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
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(address.detailAddress, fontSize = 13.sp, color = colorScheme.onSurfaceVariant)
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.End
                                ) {
                                    TextButton(onClick = { onEdit(address) }) {
                                        Text("编辑", fontSize = 12.sp, color = colorScheme.primary)
                                    }
                                    if (!address.isDefault) {
                                        TextButton(onClick = { onSetDefault(address.id) }) {
                                            Text("设为默认", fontSize = 12.sp, color = colorScheme.primary)
                                        }
                                    }
                                    TextButton(onClick = { onDelete(address.id) }) {
                                        Text("删除", fontSize = 12.sp, color = colorScheme.error)
                                    }
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                    }
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
