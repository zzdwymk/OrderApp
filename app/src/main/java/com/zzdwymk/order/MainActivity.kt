package com.zzdwymk.order

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.animation.*
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerDefaults
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.kyant.backdrop.backdrops.layerBackdrop
import coil3.compose.AsyncImage
import com.kyant.backdrop.backdrops.rememberLayerBackdrop
import com.kyant.backdrop.drawBackdrop
import com.kyant.backdrop.effects.blur
import com.kyant.backdrop.effects.vibrancy
import com.zzdwymk.order.data.AddressViewModel
import com.zzdwymk.order.ui.page.OrderConfirmPage
import com.zzdwymk.order.ui.page.OrderDetailPage
import com.zzdwymk.order.ui.page.OrderPage
import com.zzdwymk.order.ui.page.ShopDetailPage
import com.zzdwymk.order.data.CartViewModel
import com.zzdwymk.order.data.OrderConfirmViewModel
import com.zzdwymk.order.data.OrderViewModel
import com.zzdwymk.order.data.ShopViewModel
import com.zzdwymk.order.model.Shop
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import com.zzdwymk.order.ui.page.MyProfilePage
import com.zzdwymk.order.ui.component.UltraFluidGlassBottomBar
import com.zzdwymk.order.ui.component.UltraFluidRefreshBox
import com.zzdwymk.order.ui.page.NotificationPage
import com.zzdwymk.order.ui.page.ShoppingCartPage
import com.zzdwymk.order.ui.HapticUtils
import com.zzdwymk.order.ui.page.DeliveryTrackingPage
import com.zzdwymk.order.ui.theme.OrderTheme

class MainActivity : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        enableEdgeToEdge()
        setContent {
            OrderTheme {
                MainAppScaffold()
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalAnimationApi::class)
@Composable
private fun MainAppScaffold() {
    val navController = rememberNavController()
    val shopViewModel: ShopViewModel = viewModel()
    val cartViewModel: CartViewModel = viewModel()
    val orderConfirmViewModel: OrderConfirmViewModel = viewModel()
    val orderViewModel: OrderViewModel = viewModel()
    val addressViewModel: AddressViewModel = viewModel()

    // 🚀 核心架构升级：将“主页面”与“详情页”交给 NavHost 管理。
    // 这可以让详情页打开时，主页面组件树在底层完全静止，不参与任何无效重组，性能直接拉满！
    SharedTransitionLayout(modifier = Modifier.fillMaxSize()) {
        NavHost(
            navController = navController,
            startDestination = "main_home",
            modifier = Modifier.fillMaxSize()
        ) {
            // 目的地 1：完整的主页面（包含你所有的 Pager 和超流体玻璃底部栏布局）
            composable("main_home",
                enterTransition = { fadeIn(animationSpec = spring(stiffness = Spring.StiffnessMediumLow)) },
                exitTransition = { fadeOut(animationSpec = spring(stiffness = Spring.StiffnessMedium)) },
                popEnterTransition = { fadeIn(animationSpec = spring(stiffness = Spring.StiffnessMediumLow)) },
                popExitTransition = { fadeOut(animationSpec = spring(stiffness = Spring.StiffnessMedium)) }) {
                MainPage(
                    navController = navController,
                    shopViewModel = shopViewModel,
                    cartViewModel = cartViewModel,
                    orderViewModel = orderViewModel,
                    addressViewModel = addressViewModel,
                    sharedTransitionScope = this@SharedTransitionLayout,
                    animatedVisibilityScope = this@composable
                )
            }

            // 目的地 2：独立的店铺详情页
            composable(
                route = "shop_detail/{shopId}",
                arguments = listOf(navArgument("shopId") { type = NavType.IntType }),
                enterTransition = {
                    slideIntoContainer(
                        towards = AnimatedContentTransitionScope.SlideDirection.Left,
                        animationSpec = spring(dampingRatio = 0.8f, stiffness = Spring.StiffnessMediumLow)
                    ) + fadeIn(animationSpec = spring(stiffness = Spring.StiffnessMediumLow))
                },
                popEnterTransition = { fadeIn(animationSpec = spring(stiffness = Spring.StiffnessMediumLow)) },
                exitTransition = { fadeOut(animationSpec = spring(stiffness = Spring.StiffnessMedium)) },
                popExitTransition = {
                    slideOutOfContainer(
                        towards = AnimatedContentTransitionScope.SlideDirection.Right,
                        animationSpec = spring(dampingRatio = 0.8f, stiffness = Spring.StiffnessMediumLow)
                    ) + fadeOut(animationSpec = spring(stiffness = Spring.StiffnessMedium))
                }
            ) { backStackEntry ->
                val shopId = backStackEntry.arguments?.getInt("shopId") ?: 0
                val shop = shopViewModel.shops.find { it.id == shopId }

                if (shop != null) {
                    ShopDetailPage(
                        shop = shop,
                        cartViewModel = cartViewModel,
                        onBack = { navController.popBackStack() },
                        onOrderClick = { 
                            try {
                                val items = cartViewModel.getShopItems(shop.id)
                                if (items.isNotEmpty()) {
                                    cartViewModel.hideCartPopup()
                                    orderConfirmViewModel.initOrder(
                                        shopId = shop.id,
                                        shopName = shop.shopName,
                                        shopPic = shop.shopPic,
                                        cartItems = items,
                                        deliveryFee = shop.distributionCost.toDouble(),
                                        defaultAddress = addressViewModel.addresses.firstOrNull()
                                    )
                                    navController.navigate("order_confirm")
                                }
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        },
                        sharedTransitionScope = this@SharedTransitionLayout,
                        animatedVisibilityScope = this@composable
                    )
                }
            }

            // 目的地 3：确认订单页面
            composable(
                route = "order_confirm",
                enterTransition = {
                    slideIntoContainer(
                        towards = AnimatedContentTransitionScope.SlideDirection.Left,
                        animationSpec = spring(dampingRatio = 0.8f, stiffness = Spring.StiffnessMediumLow)
                    ) + fadeIn(animationSpec = spring(stiffness = Spring.StiffnessMediumLow))
                },
                popEnterTransition = { fadeIn(animationSpec = spring(stiffness = Spring.StiffnessMediumLow)) },
                exitTransition = { fadeOut(animationSpec = spring(stiffness = Spring.StiffnessMedium)) },
                popExitTransition = {
                    slideOutOfContainer(
                        towards = AnimatedContentTransitionScope.SlideDirection.Right,
                        animationSpec = spring(dampingRatio = 0.8f, stiffness = Spring.StiffnessMediumLow)
                    ) + fadeOut(animationSpec = spring(stiffness = Spring.StiffnessMedium))
                }
            ) {
                OrderConfirmPage(
                    viewModel = orderConfirmViewModel,
                    addressViewModel = addressViewModel,
                    onBack = {
                        val shopId = orderConfirmViewModel.orderData.shopId
                        if (shopId != 0) {
                            if (!navController.popBackStack("shop_detail/$shopId", false)) {
                                navController.navigate("shop_detail/$shopId") {
                                    launchSingleTop = true
                                }
                            }
                        }
                    },
                    onSubmitSuccess = {
                        cartViewModel.clearAll()
                        orderViewModel.addOrder(orderConfirmViewModel.orderData, orderConfirmViewModel.scheduledDeliveryTime)
                        navController.navigate("order_detail/${orderViewModel.currentOrderId}") {
                            popUpTo("main_home") { inclusive = false }
                        }
                    }
                )
            }

            // 目的地 4：订单详情页面（提交成功后）
            composable(
                route = "order_detail/{orderId}",
                arguments = listOf(navArgument("orderId") { type = NavType.StringType }),
                enterTransition = {
                    slideIntoContainer(
                        towards = AnimatedContentTransitionScope.SlideDirection.Left,
                        animationSpec = spring(dampingRatio = 0.8f, stiffness = Spring.StiffnessMediumLow)
                    ) + fadeIn(animationSpec = spring(stiffness = Spring.StiffnessMediumLow))
                },
                exitTransition = {
                    fadeOut(animationSpec = spring(stiffness = Spring.StiffnessMedium))
                },
                popEnterTransition = { fadeIn(animationSpec = spring(stiffness = Spring.StiffnessMediumLow)) },
                popExitTransition = {
                    slideOutOfContainer(
                        towards = AnimatedContentTransitionScope.SlideDirection.Right,
                        animationSpec = spring(dampingRatio = 0.8f, stiffness = Spring.StiffnessMediumLow)
                    ) + fadeOut(animationSpec = spring(stiffness = Spring.StiffnessMedium))
                }
            ) { backStackEntry ->
                val orderId = backStackEntry.arguments?.getString("orderId") ?: ""
                val order = orderViewModel.getOrderById(orderId)
                if (order != null) {
                    OrderDetailPage(
                        order = order,
                        orderViewModel = orderViewModel,
                        onBack = {
                            navController.navigate("shop_detail/${order.shopId}") {
                                popUpTo("main_home") { inclusive = false }
                            }
                        },
                        onHome = {
                            navController.navigate("main_home") {
                                popUpTo(0) { inclusive = true }
                            }
                        },
                        onViewOrders = {
                            navController.navigate("main_home") {
                                popUpTo(0) { inclusive = true }
                            }
                        },
                        onDeliveryTrack = {
                            navController.navigate("delivery_tracking/${order.id}")
                        }
                    )
                }
            }

            // 配送追踪页面
            composable(
                route = "delivery_tracking/{orderId}",
                arguments = listOf(navArgument("orderId") { type = NavType.StringType }),
                enterTransition = {
                    slideIntoContainer(
                        towards = AnimatedContentTransitionScope.SlideDirection.Left,
                        animationSpec = spring(dampingRatio = 0.8f, stiffness = Spring.StiffnessMediumLow)
                    ) + fadeIn(animationSpec = spring(stiffness = Spring.StiffnessMediumLow))
                },
                exitTransition = {
                    fadeOut(animationSpec = spring(stiffness = Spring.StiffnessMedium))
                },
                popEnterTransition = { fadeIn(animationSpec = spring(stiffness = Spring.StiffnessMediumLow)) },
                popExitTransition = {
                    slideOutOfContainer(
                        towards = AnimatedContentTransitionScope.SlideDirection.Right,
                        animationSpec = spring(dampingRatio = 0.8f, stiffness = Spring.StiffnessMediumLow)
                    ) + fadeOut(animationSpec = spring(stiffness = Spring.StiffnessMedium))
                }
            ) { backStackEntry ->
                val orderId = backStackEntry.arguments?.getString("orderId") ?: ""
                val order = orderViewModel.getOrderById(orderId)
                if (order != null) {
                    DeliveryTrackingPage(
                        order = order,
                        onBack = { navController.popBackStack() },
                        onRiderCall = { /* TODO: 调用骑手 */ },
                        onContactShop = { navController.navigate("shop_detail/${order.shopId}") }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
private fun MainPage(
    navController: NavController,
    shopViewModel: ShopViewModel,
    cartViewModel: CartViewModel,
    orderViewModel: OrderViewModel,
    addressViewModel: AddressViewModel,
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope
) {
    val activity = LocalActivity.current
    val colorScheme = MaterialTheme.colorScheme

    LaunchedEffect(Unit) {
        (activity as? ComponentActivity)?.enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.light(
                android.graphics.Color.TRANSPARENT,
                android.graphics.Color.TRANSPARENT
            )
        )
    }

    val navItems = listOf(
        NavItem("购物车", Icons.Default.ShoppingCart, "shoppingcart"),
        NavItem("订单", Icons.Default.Receipt, "order"),
        NavItem("我的", Icons.Default.Person, "profile"),
        NavItem("消息", Icons.Default.ChatBubble, "notifications"),
        NavItem("主页", Icons.Default.Home, "home")
    )
    var currentRoute by remember { mutableStateOf("home") }
    var isSearchActive by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()
    val haptic = LocalHapticFeedback.current

    val backgroundColor = colorScheme.background

    val backdrop = rememberLayerBackdrop {
        drawRect(backgroundColor)
        drawContent()
    }

    val pagerState = rememberPagerState(
        initialPage = navItems.indexOfFirst { it.route == currentRoute },
        pageCount = { navItems.size }
    )

    val pagerPage = pagerState.currentPage
    val pagerOffset = pagerState.currentPageOffsetFraction

    LaunchedEffect(pagerPage) {
        val route = navItems[pagerPage].route
        if (route != currentRoute) {
            currentRoute = route
        }
        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
    }

    // 💯 你的原有视效布局完全保留，一个组件都没挪动
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
    ) {
        val pagerFlingBehavior = PagerDefaults.flingBehavior(
            state = pagerState,
            snapAnimationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = 100f)
        )

        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxSize()
                .layerBackdrop(backdrop),
            flingBehavior = pagerFlingBehavior,
            key = { navItems[it].route }
        ) { page ->
            when (navItems[page].route) {
                "profile" -> MyProfilePage(addressViewModel = addressViewModel)
                "home" -> MainNavHost(
                    isSearchActive = isSearchActive,
                    onActiveChange = { isSearchActive = it },
                    searchQuery = searchQuery,
                    onSearchQueryChange = { searchQuery = it },
                    onShopClick = { shop -> navController.navigate("shop_detail/${shop.id}") },
                    shopViewModel = shopViewModel,
                    cartViewModel = cartViewModel,
                    sharedTransitionScope = sharedTransitionScope,
                    animatedVisibilityScope = animatedVisibilityScope
                )
                "shoppingcart" -> ShoppingCartPage(cartViewModel = cartViewModel)
                "order" -> OrderPage(
                    viewModel = orderViewModel,
                    onNavigateToDeliveryTrack = { orderId ->
                        navController.navigate("delivery_tracking/$orderId")
                    }
                )
                "notifications" -> NotificationPage()
                else -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("当前页面: ${navItems[page].route}")
                }
            }
        }

        Box(modifier = Modifier.align(Alignment.BottomCenter)) {
            UltraFluidGlassBottomBar(
                backdrop = backdrop,
                items = navItems,
                currentRoute = currentRoute,
                pagerPage = pagerPage,
                pagerOffset = pagerOffset,
                onItemClick = { item ->
                    val idx = navItems.indexOf(item)
                    scope.launch {
                        pagerState.animateScrollToPage(
                            idx,
                            animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessMediumLow)
                        )
                    }
                }
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class, ExperimentalAnimationApi::class)
@Composable
private fun MainNavHost(
    isSearchActive: Boolean,
    onActiveChange: (Boolean) -> Unit,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onShopClick: (Shop) -> Unit,
    shopViewModel: ShopViewModel,
    cartViewModel: CartViewModel,
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope
) {
    val colorScheme = MaterialTheme.colorScheme
    val springSpec = spring<Float>(dampingRatio = 0.45f, stiffness = 500f)
    val animProgress by animateFloatAsState(
        targetValue = if (isSearchActive) 0f else 1f,
        animationSpec = springSpec,
        label = "topBarAnim"
    )

    val backdrop = rememberLayerBackdrop {
        drawRect(colorScheme.background)
        drawContent()
    }

    val shops = shopViewModel.shops
    val filteredShops by remember(shops, searchQuery) {
        derivedStateOf {
            if (searchQuery.isBlank()) shops
            else shops.filter { it.shopName.contains(searchQuery, ignoreCase = true) }
        }
    }
    val shopNames = remember(shops) { shops.map { it.shopName } }
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    var isRefreshing by remember { mutableStateOf(false) }

    LaunchedEffect(shopViewModel.isLoading) {
        if (isRefreshing && !shopViewModel.isLoading) {
            isRefreshing = false
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = {
                isRefreshing = true
                shopViewModel.refreshFromNetwork()
            },
            modifier = Modifier.fillMaxSize()
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .fillMaxSize()
                        .layerBackdrop(backdrop)
                        .drawBehind {
                            drawCircle(
                                brush = Brush.radialGradient(
                                    colors = listOf(colorScheme.primaryContainer.copy(alpha = 0.18f), Color.Transparent),
                                    center = androidx.compose.ui.geometry.Offset(size.width * 0.2f, size.height * 0.3f),
                                    radius = size.width * 0.8f
                                )
                            )
                            drawCircle(
                                brush = Brush.radialGradient(
                                    colors = listOf(colorScheme.tertiaryContainer.copy(alpha = 0.15f), Color.Transparent),
                                    center = androidx.compose.ui.geometry.Offset(size.width * 0.8f, size.height * 0.7f),
                                    radius = size.width * 0.7f
                                )
                            )
                        },
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    contentPadding = PaddingValues(top = 110.dp, bottom = 130.dp, start = 16.dp, end = 16.dp)
                ) {
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(130.dp),
                            shape = RoundedCornerShape(24.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = colorScheme.primaryContainer.copy(alpha = 0.35f)
                            ),
                            border = BorderStroke(1.dp, colorScheme.primary.copy(alpha = 0.15f))
                        ) {
                            Row(
                                modifier = Modifier.fillMaxSize().padding(20.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Box(
                                        modifier = Modifier
                                            .background(colorScheme.primary.copy(alpha = 0.12f), CircleShape)
                                            .padding(horizontal = 10.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            "✨ TODAY'S PICK",
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Black,
                                            color = colorScheme.primary,
                                            letterSpacing = 1.2.sp
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        "宝藏店铺 · 灵感推荐",
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Black,
                                        color = colorScheme.onSurface
                                    )
                                    Text(
                                        "探索你身边的精致美味",
                                        fontSize = 11.sp,
                                        color = colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                    )
                                }

                                Box(
                                    modifier = Modifier
                                        .size(52.dp)
                                        .background(Color.White.copy(alpha = 0.4f), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Search,
                                        contentDescription = null,
                                        modifier = Modifier.size(22.dp),
                                        tint = colorScheme.primary
                                    )
                                }
                            }
                        }
                    }

                    items(filteredShops.size, key = { filteredShops[it].id }) { index ->
                        val shop = filteredShops[index]
                        BeautifulPremiumShopCard(
                            shop = shop,
                            index = index,
                            onClick = { onShopClick(shop) },
                            sharedTransitionScope = sharedTransitionScope,
                            animatedVisibilityScope = animatedVisibilityScope
                        )
                    }
                }
            }
        }

        // TopBar — fixed at top with liquid glass effect
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter)
                .background(colorScheme.primaryContainer.copy(0.8f).compositeOver(Color.White.copy(0.4f)), RoundedCornerShape(0.dp))
                .drawBackdrop(backdrop, { RoundedCornerShape(0.dp) }, { vibrancy(); blur(12.dp.toPx()) }, onDrawSurface = { drawRect(colorScheme.primary.copy(alpha = 0.08f)) })
                .statusBarsPadding()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .width(40.dp * animProgress)
                        .graphicsLayer {
                            alpha = animProgress
                            scaleX = animProgress
                            scaleY = animProgress
                        },
                    contentAlignment = Alignment.CenterStart
                ) {
                    Icon(
                        painter = painterResource(R.drawable.icon),
                        contentDescription = null,
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape),
                        tint = Color.Unspecified
                    )
                }

                Spacer(modifier = Modifier.width(12.dp * animProgress))

                Box(
                    modifier = Modifier.weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    CompactDockedSearchBar(
                        isActive = isSearchActive,
                        onActiveChange = onActiveChange,
                        query = searchQuery,
                        onQueryChange = onSearchQueryChange,
                        suggestions = shopNames
                    )
                }

                Spacer(modifier = Modifier.width(12.dp * animProgress))

                Box(
                    modifier = Modifier
                        .width(33.dp * animProgress)
                        .graphicsLayer {
                            alpha = animProgress
                            scaleX = animProgress
                            scaleY = animProgress
                        },
                    contentAlignment = Alignment.CenterEnd
                ) {
                    Icon(
                        painter = painterResource(R.drawable.message),
                        contentDescription = null,
                        modifier = Modifier.size(33.dp),
                        tint = Color.Unspecified
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class, ExperimentalAnimationApi::class)
@Composable
private fun BeautifulPremiumShopCard(
    shop: Shop,
    index: Int,
    onClick: () -> Unit,
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope
) {
    val colorScheme = MaterialTheme.colorScheme
    val hapticFeedback = LocalHapticFeedback.current

    with(sharedTransitionScope) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .sharedBounds(
                    rememberSharedContentState(key = "card_${shop.id}"),
                    animatedVisibilityScope = animatedVisibilityScope
                )
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) {
                    HapticUtils.performLightImpact(hapticFeedback)
                    onClick()
                },
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = colorScheme.surface.copy(alpha = 0.72f)
            ),
            border = BorderStroke(1.dp, colorScheme.onSurface.copy(alpha = 0.06f))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(88.dp)
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

                Spacer(modifier = Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = shop.shopName,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.sharedElement(
                            rememberSharedContentState(key = "title_${shop.id}"),
                            animatedVisibilityScope = animatedVisibilityScope
                        )
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = Color(0xFFFFC107),
                            modifier = Modifier.size(14.dp)
                        )
                        Text("4.9", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = colorScheme.onSurface)
                        Text("·", fontSize = 12.sp, color = colorScheme.onSurfaceVariant)
                        Text("月售${shop.saleNum}+", fontSize = 12.sp, color = colorScheme.onSurfaceVariant)
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = colorScheme.surfaceContainerHighest.copy(alpha = 0.5f)
                        ) {
                            Text(
                                "起送¥${shop.offerPrice}",
                                fontSize = 11.sp,
                                color = colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = colorScheme.primary.copy(alpha = 0.08f)
                        ) {
                            Text(
                                "配送¥${shop.distributionCost}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = colorScheme.primary,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        shop.time,
                        fontSize = 11.sp,
                        color = colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    )

                    if (shop.welfare.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    colorScheme.error.copy(alpha = 0.06f),
                                    RoundedCornerShape(6.dp)
                                )
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "🎁 ${shop.welfare}",
                                fontSize = 11.sp,
                                color = colorScheme.error,
                                fontWeight = FontWeight.Medium,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CompactDockedSearchBar(
    isActive: Boolean,
    onActiveChange: (Boolean) -> Unit,
    query: String,
    onQueryChange: (String) -> Unit,
    suggestions: List<String> = emptyList(),
    onSuggestionClick: (String) -> Unit = {}
) {
    val focusRequester = remember { androidx.compose.ui.focus.FocusRequester() }
    val colorScheme = MaterialTheme.colorScheme
    val hapticFeedback = LocalHapticFeedback.current
    Column(modifier = Modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(44.dp)
                .clip(CircleShape)
                .background(colorScheme.surfaceVariant)
                .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) {
                    if (!isActive) {
                        HapticUtils.performLightImpact(hapticFeedback)
                        onActiveChange(true)
                    }
                }
                .padding(horizontal = 12.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Search, null, modifier = Modifier.size(20.dp), tint = if (isActive) colorScheme.primary else Color.Gray)
                Spacer(modifier = Modifier.width(8.dp))
                Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.CenterStart) {
                    if (query.isEmpty()) { Text("搜索店铺...", fontSize = 14.sp, color = Color.Gray) }
                    BasicTextField(
                        value = query, onValueChange = onQueryChange, modifier = Modifier.fillMaxWidth().focusRequester(focusRequester),
                        singleLine = true, enabled = isActive, textStyle = androidx.compose.ui.text.TextStyle(fontSize = 14.sp, color = colorScheme.onSurface)
                    )
                }
                if (isActive) {
                    IconButton(onClick = { onActiveChange(false); onQueryChange("") }, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Default.Close, null, modifier = Modifier.size(18.dp))
                    }
                }
            }
        }
    }
    LaunchedEffect(isActive) { if (isActive) { delay(150); focusRequester.requestFocus() } }
}

data class NavItem(val label: String, val icon: ImageVector, val route: String)

@RequiresApi(Build.VERSION_CODES.O)
@Preview(showSystemUi = true)
@Composable
private fun MainPagePreview() { OrderTheme { MainAppScaffold() } }