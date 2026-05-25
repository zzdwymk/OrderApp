package com.zzdwymk.order.ui.page

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.kyant.backdrop.backdrops.rememberLayerBackdrop
import com.kyant.backdrop.backdrops.layerBackdrop
import com.kyant.backdrop.drawBackdrop
import com.kyant.backdrop.effects.blur
import com.kyant.backdrop.effects.vibrancy
import com.zzdwymk.order.data.CartViewModel
import com.zzdwymk.order.model.Food
import com.zzdwymk.order.model.Shop
import com.zzdwymk.order.ui.component.ShopDetailFluidBottomBar
import com.zzdwymk.order.ui.HapticUtils
import kotlinx.coroutines.launch
import kotlin.math.min
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.animation.ExperimentalSharedTransitionApi

@Preview(showSystemUi = true)
@Composable
fun FoodCardPreview() {
    val colorScheme = MaterialTheme.colorScheme
    val mockFood = Food(
        foodId = "1",
        foodName = "招牌北京烤鸭",
        taste = "皮脆肉嫩，香气四溢",
        saleNum = 999,
        price = 128.0,
        count = 0,
        foodPic = ""
    )

    MaterialTheme(colorScheme = colorScheme) {
        Column {
            FoodCard(
                food = mockFood,
                count = 0,
                onAdd = {},
                onRemove = {}
            )
            Spacer(modifier = Modifier.height(12.dp))
            FoodCard(
                food = mockFood,
                count = 3,
                onAdd = {},
                onRemove = {}
            )
        }
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun ShopDetailPage(
    shop: Shop,
    cartViewModel: CartViewModel,
    onBack: () -> Unit,
    onOrderClick: () -> Unit = {},
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope
) {
    val colorScheme = MaterialTheme.colorScheme
    val background = colorScheme.background
    val scrollState = rememberLazyListState()
    val screenWidth = LocalConfiguration.current.screenWidthDp.dp
    val hapticFeedback = LocalHapticFeedback.current

    LaunchedEffect(Unit) {
        cartViewModel.hideCartPopup()
    }

    val backdrop = rememberLayerBackdrop { drawRect(background); drawContent() }

    val scrollProgress by remember {
        derivedStateOf {
            if (scrollState.firstVisibleItemIndex > 0) 1f
            else min(1f, scrollState.firstVisibleItemScrollOffset.toFloat() / 300f)
        }
    }

    val rawProgress by animateFloatAsState(
        targetValue = scrollProgress,
        animationSpec = spring(dampingRatio = 0.6f, stiffness = 400f),
        label = "scrollProgress"
    )

    val titleSize = (22f - (4f * rawProgress)).sp

    // 【修改点 1】改用 derivedStateOf。当购物车从空到有时，能保证动态状态链被正确刷新，不锁死在空列表缓存中
    val shopItems by remember { derivedStateOf { cartViewModel.getShopItems(shop.id) } }
    val totalCount = cartViewModel.getTotalFoodCount(shop.id)
    val totalPrice = cartViewModel.getTotalPrice(shop.id)

    Box(modifier = Modifier.fillMaxSize().background(background)) {
        // 1. 列表层
        LazyColumn(
            state = scrollState,
            modifier = Modifier.fillMaxSize().layerBackdrop(backdrop),
            contentPadding = PaddingValues(top = 120.dp, bottom = 160.dp, start = 16.dp, end = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                ShopHeaderCard(
                    shop = shop,
                    colorScheme = colorScheme,
                    sharedTransitionScope = sharedTransitionScope,
                    animatedVisibilityScope = animatedVisibilityScope
                )
            }

            item {
                Text("公告", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = colorScheme.onSurface)
                Spacer(modifier = Modifier.height(6.dp))
                Text(shop.shopNotice, fontSize = 13.sp, color = colorScheme.onSurfaceVariant)
            }

            item {
                Text("全部菜品", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = colorScheme.onSurface)
            }

            items(shop.foodList) { food ->
                val count = cartViewModel.getFoodCount(shop.id, food.foodId)
                FoodCard(
                    food = food,
                    count = count,
                    onAdd = { cartViewModel.addFood(shop, food) },
                    onRemove = { cartViewModel.removeFood(shop, food) }
                )
            }
        }

        // 2. 顶部导航栏
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(colorScheme.surface.copy(0.7f).compositeOver(Color.White.copy(0.4f)), RoundedCornerShape(0.dp))
                .drawBackdrop(backdrop, { RoundedCornerShape(0.dp) }, { vibrancy(); blur(8.dp.toPx()) }, onDrawSurface = { drawRect(colorScheme.primary.copy(alpha = 0.08f)) })
                .statusBarsPadding()
                .align(Alignment.TopCenter)
        ) {
            with(sharedTransitionScope) {
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
                            ) {
                                HapticUtils.performLightImpact(hapticFeedback)
                                onBack()
                            }
                            .padding(8.dp),
                        tint = colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Box(
                        modifier = Modifier.fillMaxWidth().weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            shop.shopName,
                            fontSize = titleSize,
                            fontWeight = FontWeight.Bold,
                            color = colorScheme.onSurface,
                            modifier = Modifier
                                .graphicsLayer {
                                    val scale = 1f - rawProgress * 0.15f
                                    scaleX = scale
                                    scaleY = scale
                                    val targetCenterX = (screenWidth.toPx() / 2) - (size.width * scale / 2) - 48.dp.toPx()
                                    translationX = targetCenterX * rawProgress
                                }
                                .sharedElement(
                                    rememberSharedContentState(key = "title_${shop.id}"),
                                    animatedVisibilityScope = animatedVisibilityScope
                                )
                        )
                    }
                    Spacer(modifier = Modifier.width(44.dp))
                }
            }
        }

        // 3. 购物车弹窗
        // 【修改点 2】把弹窗移到最底层结算栏的上方声明。这样弹窗无论是在展开还是在退出隐藏的过程中，
        // 其透明动画图层绝不会盖在最关键的底栏按钮上方，从而消灭“点击穿透被吞”的死锁情况。
        AnimatedVisibility(
            visible = cartViewModel.showCartPopup && shopItems.isNotEmpty(),
            enter = fadeIn() + slideInVertically { it / 4 },
            exit = fadeOut() + slideOutVertically { it / 4 },
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            BackHandler { cartViewModel.hideCartPopup() }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp)
                    .padding(bottom = 75.dp)
                    .navigationBarsPadding()
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color.White.copy(alpha = 0.92f))
                    .drawBackdrop(backdrop, { RoundedCornerShape(24.dp) }, { vibrancy(); blur(16.dp.toPx()) }, onDrawSurface = { drawRect(colorScheme.surface.copy(alpha = 0.3f)) })
                    .border(1.dp, colorScheme.outline.copy(alpha = 0.1f), RoundedCornerShape(24.dp))
                    .padding(18.dp)
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(colorScheme.primary.copy(alpha = 0.12f)),
                            contentAlignment = Alignment.Center
                        ) {
                            AsyncImage(
                                model = shop.shopPic,
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.size(36.dp).clip(CircleShape)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                shop.shopName,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = colorScheme.onSurface
                            )
                            Text(
                                "${shopItems.size}种商品 · ${totalCount}件",
                                fontSize = 11.sp,
                                color = colorScheme.onSurfaceVariant
                            )
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                "清空",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = colorScheme.error,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .clickable(
                                        interactionSource = remember { MutableInteractionSource() },
                                        indication = null
                                    ) {
                                        HapticUtils.performMediumImpact(hapticFeedback)
                                        cartViewModel.clearAll()
                                    }
                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                Icons.Default.Close,
                                contentDescription = "关闭",
                                tint = colorScheme.onSurfaceVariant,
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(CircleShape)
                                    .clickable(
                                        interactionSource = remember { MutableInteractionSource() },
                                        indication = null
                                    ) {
                                        HapticUtils.performLightImpact(hapticFeedback)
                                        cartViewModel.hideCartPopup()
                                    }
                                    .padding(4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(colorScheme.onSurface.copy(alpha = 0.06f))
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    shopItems.forEach { entry ->
                        key(entry.food.foodId) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                AsyncImage(
                                    model = entry.food.foodPic,
                                    contentDescription = null,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        entry.food.foodName,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = colorScheme.onSurface,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            "¥${entry.food.price}",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = colorScheme.primary
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            "小计 ¥${String.format("%.1f", entry.count * entry.food.price)}",
                                            fontSize = 10.sp,
                                            color = colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                                        )
                                    }
                                }
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .background(
                                            colorScheme.surfaceContainerHighest.copy(alpha = 0.5f),
                                            CircleShape
                                        )
                                        .animateContentSize()
                                ) {
                                    Icon(
                                        Icons.Default.Remove, contentDescription = "减少",
                                        modifier = Modifier
                                            .size(28.dp)
                                            .clip(CircleShape)
                                            .clickable(
                                                interactionSource = remember { MutableInteractionSource() },
                                                indication = null
                                            ) {
                                                HapticUtils.performLightImpact(hapticFeedback)
                                                cartViewModel.removeFood(shop, entry.food)
                                                if (cartViewModel.getTotalFoodCount(shop.id) == 0) {
                                                    cartViewModel.hideCartPopup()
                                                }
                                            }
                                            .padding(5.dp),
                                        tint = colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        "${entry.count}",
                                        modifier = Modifier.padding(horizontal = 5.dp),
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = colorScheme.onSurface
                                    )
                                    Icon(
                                        Icons.Default.Add, contentDescription = "添加",
                                        modifier = Modifier
                                            .size(28.dp)
                                            .clip(CircleShape)
                                            .background(colorScheme.primary, CircleShape)
                                            .clickable(
                                                interactionSource = remember { MutableInteractionSource() },
                                                indication = null
                                            ) {
                                                HapticUtils.performLightImpact(hapticFeedback)
                                                cartViewModel.addFood(shop, entry.food)
                                            }
                                            .padding(5.dp),
                                        tint = colorScheme.onPrimary
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // 4. 底部工具下单栏放最后
        // 【修改点 3】增加 key(totalCount > 0)。当数量从 0 变为大数时，强行将组件彻底重组初始化。
        // 同时将其位置移到 Box 的最后一项声明，确保它永远待在整个屏幕的最顶层，拿到绝对最高点击权限。
        key(totalCount > 0) {
            ShopDetailFluidBottomBar(
                backdrop = backdrop,
                totalCount = totalCount,
                totalPrice = totalPrice,
                onCartClick = { cartViewModel.toggleCartPopup() },
                onOrderClick = {
                    if (totalCount > 0) {
                        cartViewModel.hideCartPopup() // 下单时顺手安全关闭弹窗
                        onOrderClick()
                    }
                },
                modifier = Modifier.align(Alignment.BottomCenter)
            )
        }
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun ShopHeaderCard(
    shop: Shop,
    colorScheme: androidx.compose.material3.ColorScheme,
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            with(sharedTransitionScope) {
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .sharedElement(
                            rememberSharedContentState(key = "icon_${shop.id}"),
                            animatedVisibilityScope = animatedVisibilityScope
                        )
                        .clip(RoundedCornerShape(22.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    AsyncImage(
                        model = shop.shopPic,
                        contentDescription = shop.shopName,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(shop.shopName, fontWeight = FontWeight.Bold, fontSize = 22.sp, color = colorScheme.onSurface)
                Spacer(modifier = Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("月售${shop.saleNum}", fontSize = 12.sp, color = colorScheme.onSurfaceVariant)
                    Text("起送¥${shop.offerPrice}", fontSize = 12.sp, color = colorScheme.onSurfaceVariant)
                    Text("配送¥${shop.distributionCost}", fontSize = 12.sp, color = colorScheme.primary)
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(shop.time, fontSize = 11.sp, color = colorScheme.onSurfaceVariant.copy(alpha = 0.6f))
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(colorScheme.error.copy(alpha = 0.08f), RoundedCornerShape(12.dp))
                .padding(12.dp)
        ) {
            Text(shop.welfare, fontSize = 13.sp, color = colorScheme.error, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
fun FoodCard(
    food: Food,
    count: Int,
    onAdd: () -> Unit,
    onRemove: () -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    val hapticFeedback = LocalHapticFeedback.current

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(colorScheme.surface)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = food.foodPic,
                contentDescription = food.foodName,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(14.dp))
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(food.foodName, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = colorScheme.onSurface)
                Spacer(modifier = Modifier.height(4.dp))
                Text(food.taste, fontSize = 12.sp, color = colorScheme.onSurfaceVariant, maxLines = 2)
                Spacer(modifier = Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("¥${food.price}", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = colorScheme.primary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("已售${food.saleNum}", fontSize = 11.sp, color = colorScheme.onSurfaceVariant.copy(alpha = 0.5f))
                }
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.animateContentSize()) {
                Icon(
                    Icons.Default.Add, contentDescription = "添加",
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(colorScheme.primary.copy(alpha = 0.12f))
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) {
                            HapticUtils.performLightImpact(hapticFeedback)
                            onAdd()
                        }
                        .padding(6.dp),
                    tint = colorScheme.primary
                )

                AnimatedVisibility(
                    visible = count > 0,
                    enter = scaleIn(animationSpec = spring(dampingRatio = 0.6f, stiffness = 400f)) + fadeIn(),
                    exit = scaleOut(animationSpec = spring(dampingRatio = 0.6f, stiffness = 400f)) + fadeOut()
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text("$count", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = colorScheme.onSurface)
                        Spacer(modifier = Modifier.height(6.dp))
                        Icon(
                            Icons.Default.Remove, contentDescription = "减少",
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(colorScheme.error.copy(alpha = 0.12f))
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null
                                ) {
                                    HapticUtils.performLightImpact(hapticFeedback)
                                    onRemove()
                                }
                                .padding(6.dp),
                            tint = colorScheme.error
                        )
                    }
                }
            }
        }
    }
}