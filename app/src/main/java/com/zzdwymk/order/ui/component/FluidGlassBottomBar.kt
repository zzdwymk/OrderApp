package com.zzdwymk.order.ui.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.lerp as dpLerp
import androidx.compose.ui.util.lerp
import com.kyant.backdrop.drawBackdrop
import com.kyant.backdrop.effects.blur
import com.kyant.backdrop.effects.vibrancy
import com.zzdwymk.order.NavItem
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@Preview
@Composable
fun UltraFluidGlassBottomBarPreview() {
    val colorScheme = MaterialTheme.colorScheme
    val backdrop = com.kyant.backdrop.backdrops.rememberLayerBackdrop { }

    val previewItems = listOf(
        NavItem("购物车", Icons.Default.ShoppingCart, "shoppingcart"),
        NavItem("订单", Icons.Default.Receipt, "order"),
        NavItem("我的", Icons.Default.Person, "profile"),
        NavItem("消息", Icons.Default.ChatBubble, "notifications"),
        NavItem("主页", Icons.Default.Home, "home")
    )

    MaterialTheme(colorScheme = colorScheme) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(colorScheme.background)
                .padding(vertical = 16.dp)
        ) {
            UltraFluidGlassBottomBar(
                backdrop = backdrop,
                items = previewItems,
                currentRoute = "home",
                pagerPage = 4,
                pagerOffset = 0f,
                onItemClick = {}
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun NavigationIconContentPreview() {
    val colorScheme = MaterialTheme.colorScheme

    MaterialTheme(colorScheme = colorScheme) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(20.dp),
            modifier = Modifier.padding(20.dp)
        ) {
            NavigationIconContent(
                item = NavItem("首页", Icons.Default.Home, "home"),
                isSelected = false,
                colorScheme = colorScheme
            )
            NavigationIconContent(
                item = NavItem("订单", Icons.Default.Receipt, "order"),
                isSelected = true,
                colorScheme = colorScheme
            )
        }
    }
}

/**
 * 液态玻璃底部导航栏（完全修复制导定位版）
 */
@Composable
fun UltraFluidGlassBottomBar(
    backdrop: com.kyant.backdrop.backdrops.LayerBackdrop,
    items: List<NavItem>,
    currentRoute: String,
    pagerPage: Int,
    pagerOffset: Float,
    onItemClick: (NavItem) -> Unit,
    modifier: Modifier = Modifier // 🚀 1. 补上系统标准的修饰符入参，让外界能决定它的位置
) {
    val colorScheme = MaterialTheme.colorScheme
    val density = LocalDensity.current
    val configuration = LocalConfiguration.current
    val animationScope = rememberCoroutineScope()

    val mainNavItems = items.take(4)
    val actionItem = items.lastOrNull()
    val selectedIndex = items.indexOfFirst { it.route == currentRoute }

    val barHeight = 64.dp
    val sepSpacing = 12.dp
    val actionSize = 64.dp
    val horizontalPadding = 20.dp
    val internalPadding = 4.dp
    val totalScreenWidth = configuration.screenWidthDp.dp
    val mainBarWidth = totalScreenWidth - (horizontalPadding * 2) - sepSpacing - actionSize
    val tabWidth = (mainBarWidth - (internalPadding * 2)) / 4

    val rawFloatIndex = (pagerPage.toFloat() + pagerOffset).coerceIn(0f, 4f)
    val floatIndex by animateFloatAsState(
        targetValue = rawFloatIndex,
        animationSpec = spring(dampingRatio = 0.35f, stiffness = 500f),
        label = "sliderFloatIndex"
    )
    val visualSelectedIndex = floatIndex.roundToInt().coerceIn(0, items.size - 1)

    val mainBarPress = remember { Animatable(0f) }
    val actionBtnPress = remember { Animatable(0f) }

    val bouncySpec = spring<Float>(dampingRatio = 0.2f, stiffness = Spring.StiffnessMediumLow)
    val downSpec = spring<Float>(dampingRatio = 0.7f, stiffness = 1500f)

    val actionItemWidth = actionSize - internalPadding * 2
    val sliderBaseX = horizontalPadding + internalPadding
    val sliderTargetX = if (floatIndex <= 3f) {
        sliderBaseX + tabWidth * floatIndex
    } else {
        val t = floatIndex - 3f
        val tab3X = (horizontalPadding + internalPadding) + tabWidth * 3f // 基于原始基准计算
        // 目标: 滑块在action button处居中
        // actionButtonCenter = totalScreenWidth - horizontalPadding - actionSize / 2
        val actionXCentered = totalScreenWidth - horizontalPadding - actionSize / 2 - tabWidth / 2
        dpLerp(tab3X, actionXCentered, t)
    }
    val sliderTargetW = if (floatIndex <= 3f) {
        tabWidth
    } else {
        val t = floatIndex - 3f
        dpLerp(tabWidth, actionItemWidth, t)
    }
    val sliderPxOffset = with(density) { sliderTargetX.toPx() - sliderBaseX.toPx() }
    val sliderScaleX = sliderTargetW / tabWidth

    // 🚀 2. 融合 modifier。把外界传进来的对齐属性（如 .align(Alignment.BottomCenter)）加在顶层
    Box(
        modifier = modifier
            .navigationBarsPadding() // 内部安全适配系统全面屏手势小白条
            .padding(bottom = 24.dp)
            .fillMaxWidth()
            .height(barHeight)
    ) {

        // --- 1. 背景层：左侧长条 ---
        Box(
            modifier = Modifier
                .padding(start = horizontalPadding)
                .width(mainBarWidth)
                .fillMaxHeight()
                .graphicsLayer {
                    val p = mainBarPress.value
                    scaleX = lerp(1f, 0.95f, p)
                    scaleY = lerp(1f, 0.95f, p)
                    translationY = lerp(0f, 4.dp.toPx(), p)
                }
                .background(colorScheme.surface.copy(0.3f).compositeOver(Color.White.copy(0.4f)), CircleShape)
                .drawBackdrop(backdrop, { CircleShape }, { vibrancy(); blur(8.dp.toPx()) })
        )

        // --- 2. 背景层：右侧圆圈 ---
        Box(
            modifier = Modifier
                .padding(end = horizontalPadding)
                .align(Alignment.CenterEnd)
                .size(actionSize)
                .graphicsLayer {
                    val p = actionBtnPress.value
                    scaleX = lerp(1f, 0.9f, p)
                    scaleY = lerp(1f, 0.9f, p)
                    translationY = lerp(0f, 5.dp.toPx(), p)
                }
                .background(colorScheme.surface.copy(0.3f).compositeOver(Color.White.copy(0.4f)), CircleShape)
                .drawBackdrop(
                    backdrop,
                    { CircleShape },
                    { vibrancy(); blur(8.dp.toPx()) },
                )
        )

        // --- 3. 全局跨组件滑块 ---
        Box(
            modifier = Modifier
                .offset(x = sliderBaseX, y = internalPadding)
                .width(tabWidth)
                .height(barHeight - (internalPadding * 2))
                .graphicsLayer {
                    val belongsToRight = visualSelectedIndex >= 4
                    val p = if (belongsToRight) actionBtnPress.value else mainBarPress.value
                    val pressScale = if (belongsToRight) lerp(1f, 0.9f, p) else lerp(1f, 0.95f, p)

                    translationX = sliderPxOffset
                    scaleX = sliderScaleX * pressScale
                    scaleY = pressScale
                    translationY = if (belongsToRight) lerp(0f, 5.dp.toPx(), p)
                    else lerp(0f, 4.dp.toPx(), p)
                }
                .drawBackdrop(
                    backdrop,
                    { CircleShape },
                    { vibrancy(); blur(1.5.dp.toPx()) },
                    onDrawSurface = { drawRect(colorScheme.primaryContainer.copy(alpha = 0.85f)) }
                )
                .border(1.dp, Color.White.copy(alpha = 0.2f), CircleShape)
        )

        // --- 4. 交互层 (图标与手势) ---
        Row(
            modifier = Modifier.padding(horizontal = horizontalPadding).fillMaxSize(),
            horizontalArrangement = Arrangement.spacedBy(sepSpacing),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 左侧图标区域
            Row(
                modifier = Modifier.weight(1f).fillMaxHeight()
                    .graphicsLayer {
                        val p = mainBarPress.value
                        scaleX = lerp(1f, 0.95f, p)
                        scaleY = lerp(1f, 0.95f, p)
                        translationY = lerp(0f, 4.dp.toPx(), p)
                    }
                    .padding(horizontal = internalPadding)
            ) {
                mainNavItems.forEach { item ->
                    Box(modifier = Modifier.weight(1f).fillMaxHeight().clip(CircleShape)
                        .pointerInput(item.route) {
                            awaitEachGesture {
                                awaitFirstDown()
                                animationScope.launch { mainBarPress.animateTo(1f, downSpec) }
                                if (waitForUpOrCancellation() != null) onItemClick(item)
                                animationScope.launch { mainBarPress.animateTo(0f, bouncySpec) }
                            }
                        }, contentAlignment = Alignment.Center
                    ) {
                        NavigationIconContent(item, items.indexOf(item) == visualSelectedIndex, colorScheme)
                    }
                }
            }

            // 右侧按钮区域
            Box(
                modifier = Modifier.size(actionSize).clip(CircleShape)
                    .graphicsLayer {
                        val p = actionBtnPress.value
                        scaleX = lerp(1f, 0.9f, p)
                        scaleY = lerp(1f, 0.9f, p)
                        translationY = lerp(0f, 5.dp.toPx(), p)
                    }
                    .pointerInput(Unit) {
                        awaitEachGesture {
                            awaitFirstDown()
                            animationScope.launch { actionBtnPress.animateTo(1f, downSpec) }
                            if (waitForUpOrCancellation() != null) actionItem?.let { onItemClick(it) }
                            animationScope.launch { actionBtnPress.animateTo(0f, bouncySpec) }
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                actionItem?.let {
                    Icon(it.icon, null, tint = if (items.indexOf(it) == visualSelectedIndex) colorScheme.primary else colorScheme.onSurfaceVariant.copy(0.7f), modifier = Modifier.size(26.dp))
                }
            }
        }
    }
}


@Composable
fun NavigationIconContent(
    item: NavItem,
    isSelected: Boolean,
    colorScheme: ColorScheme
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        val iconScale by animateFloatAsState(if (isSelected) 1.2f else 1f, spring(0.4f, 400f))
        Icon(
            imageVector = item.icon,
            contentDescription = null,
            tint = if (isSelected) colorScheme.primary else colorScheme.onSurfaceVariant.copy(0.7f),
            modifier = Modifier.size(24.dp).graphicsLayer {
                scaleX = iconScale
                scaleY = iconScale
                translationY = if (isSelected) (3).dp.toPx() else 0f
            }
        )
        AnimatedVisibility(
            visible = isSelected,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            Text(
                text = item.label,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = colorScheme.primary,
                modifier = Modifier.padding(top = 2.dp)
            )
        }
    }
}