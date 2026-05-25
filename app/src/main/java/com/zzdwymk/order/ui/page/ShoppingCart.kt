package com.zzdwymk.order.ui.page

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
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
import com.zzdwymk.order.data.CartEntry
import com.zzdwymk.order.data.CartViewModel
import com.zzdwymk.order.ui.HapticUtils
import kotlin.math.min
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.tooling.preview.Preview
import com.zzdwymk.order.model.Food

@Preview
@Composable
fun CartItemCardPreview() {
    val colorScheme = MaterialTheme.colorScheme
    val mockFood = Food(
        foodId = "1",
        foodName = "招牌烤鸭",
        price = 68.0,
        taste = "香脆可口",
        saleNum = 999,
        count = 0,
        foodPic = ""
    )
    val mockEntry = CartEntry(
        shopId = 1,
        shopName = "北京烤鸭店",
        shopPic = "",
        food = mockFood,
        count = 2
    )

    MaterialTheme(colorScheme = colorScheme) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .background(colorScheme.background)
        ) {
            CartItemCard(
                entry = mockEntry,
                onAdd = {},
                onRemove = {}
            )
        }
    }
}

@Composable
fun ShoppingCartPage(cartViewModel: CartViewModel) {
    val colorScheme = MaterialTheme.colorScheme
    val background = colorScheme.background
    val scrollState = rememberLazyListState()
    val screenWidth = LocalConfiguration.current.screenWidthDp.dp
    val hapticFeedback = LocalHapticFeedback.current

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
    val titleEstimatedWidth = 60.dp
    val targetCenterX = (screenWidth / 2) - (titleEstimatedWidth / 2) - horizontalPadding
    val offsetX by animateDpAsState(targetValue = targetCenterX * animatedProgress, animationSpec = spring(dampingRatio = 0.6f, stiffness = 400f), label = "offsetX")

    val cartItems = cartViewModel.cartItems
    val groupedByShop = remember(cartItems) { cartItems.groupBy { it.shopId } }

    Box(modifier = Modifier.fillMaxSize().background(background)) {
        if (cartItems.isEmpty()) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    Icons.Default.Remove,
                    contentDescription = null,
                    tint = colorScheme.onSurfaceVariant.copy(alpha = 0.3f),
                    modifier = Modifier.size(64.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    "购物车是空的",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "去商家店铺添加美食吧",
                    fontSize = 13.sp,
                    color = colorScheme.onSurfaceVariant.copy(alpha = 0.35f)
                )
            }
        } else {
            LazyColumn(
                state = scrollState,
                modifier = Modifier.fillMaxSize().layerBackdrop(backdrop),
                contentPadding = PaddingValues(top = 100.dp, bottom = 120.dp, start = 16.dp, end = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                groupedByShop.forEach { (shopId, entries) ->
                    item(key = "shop_$shopId") {
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = colorScheme.surface.copy(alpha = 0.85f),
                            border = BorderStroke(1.dp, colorScheme.outline.copy(alpha = 0.1f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    AsyncImage(
                                        model = entries.firstOrNull()?.shopPic ?: "",
                                        contentDescription = null,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.size(28.dp).clip(CircleShape)
                                    )
                                    Text(
                                        entries.firstOrNull()?.shopName ?: "",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = colorScheme.onSurface
                                    )
                                    Spacer(modifier = Modifier.weight(1f))
                                    Text(
                                        "共${cartViewModel.getTotalFoodCount(shopId)}件 ¥${
                                            String.format("%.1f", cartViewModel.getTotalPrice(shopId))
                                        }",
                                        fontSize = 12.sp,
                                        color = colorScheme.onSurfaceVariant
                                    )
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                entries.forEachIndexed { index, entry ->
                                    CartItemCard(
                                        entry = entry,
                                        onAdd = { cartViewModel.addFood(
                                            com.zzdwymk.order.model.Shop(
                                                entry.shopId,
                                                entry.shopName,
                                                0, 0, 0, "", "", entry.shopPic, "",
                                                emptyList()
                                            ),
                                            entry.food
                                        ) },
                                        onRemove = { cartViewModel.removeFood(
                                            com.zzdwymk.order.model.Shop(
                                                entry.shopId,
                                                entry.shopName,
                                                0, 0, 0, "", "", entry.shopPic, "",
                                                emptyList()
                                            ),
                                            entry.food
                                        ) }
                                    )
                                    if (index < entries.size - 1) {
                                        Spacer(modifier = Modifier.height(10.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(colorScheme.surface.copy(0.3f).compositeOver(Color.White.copy(0.4f)), RoundedCornerShape(0.dp))
                .drawBackdrop(backdrop, { RoundedCornerShape(0.dp) }, { vibrancy(); blur(8.dp.toPx()) }, onDrawSurface = { drawRect(colorScheme.primaryContainer.copy(alpha = 0.3f)) })
                .statusBarsPadding()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .padding(horizontal = horizontalPadding),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "购物车",
                    fontSize = titleSize,
                    fontWeight = FontWeight.Black,
                    color = colorScheme.onSurface,
                    modifier = Modifier.offset(x = offsetX)
                )
                Spacer(modifier = Modifier.weight(1f))
                if (cartItems.isNotEmpty()) {
                    Text(
                        "清空",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = colorScheme.error,
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) {
                                HapticUtils.performMediumImpact(hapticFeedback)
                                cartViewModel.clearAll()
                            }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun CartItemCard(
    entry: CartEntry,
    onAdd: () -> Unit,
    onRemove: () -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    val hapticFeedback = LocalHapticFeedback.current

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(colorScheme.surface.copy(alpha = 0.72f))
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = entry.food.foodPic,
                contentDescription = entry.food.foodName,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(72.dp)
                    .clip(RoundedCornerShape(14.dp))
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    entry.food.foodName,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    entry.food.taste,
                    fontSize = 12.sp,
                    color = colorScheme.onSurfaceVariant,
                    maxLines = 1
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    "¥${entry.food.price}",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = colorScheme.primary
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .background(colorScheme.surfaceContainerHighest.copy(alpha = 0.5f), CircleShape)
            ) {
                Icon(
                    Icons.Default.Remove, contentDescription = "减少",
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) {
                            HapticUtils.performLightImpact(hapticFeedback)
                            onRemove()
                        }
                        .padding(6.dp),
                    tint = colorScheme.onSurfaceVariant
                )
                Text(
                    "${entry.count}",
                    modifier = Modifier.padding(horizontal = 6.dp),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = colorScheme.onSurface
                )
                Icon(
                    Icons.Default.Add, contentDescription = "添加",
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(colorScheme.primary, CircleShape)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) {
                            HapticUtils.performLightImpact(hapticFeedback)
                            onAdd()
                        }
                        .padding(6.dp),
                    tint = colorScheme.onPrimary
                )
            }
        }
    }
}
