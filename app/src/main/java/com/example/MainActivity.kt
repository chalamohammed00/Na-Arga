package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.shadow
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.model.*
import com.example.ui.components.GlassmorphicCard
import com.example.ui.theme.*
import com.example.viewmodel.NaArgaViewModel
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {
  private val viewModel: NaArgaViewModel by viewModels()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      MyApplicationTheme {
        NaArgaApp(viewModel)
      }
    }
  }
}

@Composable
fun NaArgaApp(viewModel: NaArgaViewModel) {
  val currentScreen by viewModel.currentScreen.collectAsStateWithLifecycle()
  val isAuthenticated by viewModel.isAuthenticated.collectAsStateWithLifecycle()
  
  // Showcase switcher state
  var isShowcaseHubOpen by remember { mutableStateOf(false) }

  Scaffold(
    modifier = Modifier.fillMaxSize(),
    containerColor = ObsidianBackground,
    contentWindowInsets = WindowInsets.safeDrawing,
    bottomBar = {
      // Bottom navigation is only visible if on main screens
      val mainScreens = listOf("home_feed", "explore", "create", "messaging", "profile")
      if (isAuthenticated && mainScreens.contains(currentScreen)) {
        NaArgaBottomNavBar(
          currentScreen = currentScreen,
          onNavigate = { route -> viewModel.setScreen(route) }
        )
      }
    }
  ) { innerPadding ->
    Box(
      modifier = Modifier
        .fillMaxSize()
        .padding(
          bottom = if (isAuthenticated && listOf("home_feed", "explore", "create", "messaging", "profile").contains(currentScreen)) {
            innerPadding.calculateBottomPadding()
          } else {
            0.dp
          }
        )
    ) {
      // Linear cyber ambient background glowing lines
      Canvas(modifier = Modifier.fillMaxSize()) {
        drawCircle(
          color = NeonPurple.copy(alpha = 0.08f),
          center = Offset(size.width * 0.8f, size.height * 0.2f),
          radius = size.width * 0.6f
        )
        drawCircle(
          color = NeonCyan.copy(alpha = 0.08f),
          center = Offset(size.width * 0.1f, size.height * 0.8f),
          radius = size.width * 0.5f
        )
      }

      // Root Screen selector
      Crossfade(
        targetState = currentScreen,
        animationSpec = spring(stiffness = Spring.StiffnessLow),
        label = "ScreenCrossfade"
      ) { screen ->
        when (screen) {
          "onboarding" -> OnboardingScreen(
            onNavigateToLogin = { viewModel.setScreen("login") }
          )
          "login" -> LoginScreen(
            onLoginSuccess = { viewModel.authenticate(true) }
          )
          "home_feed" -> HomeFeedScreen(viewModel = viewModel)
          "explore" -> ExploreScreen(viewModel = viewModel)
          "video_detail" -> VideoDetailScreen(viewModel = viewModel)
          "create" -> CreateScreen(viewModel = viewModel)
          "video_editor" -> VideoEditorScreen(viewModel = viewModel)
          "messaging" -> MessagingScreen(viewModel = viewModel)
          "notifications" -> NotificationsScreen(viewModel = viewModel)
          "profile" -> ProfileScreen(viewModel = viewModel)
          "creator_dashboard" -> CreatorDashboardScreen(viewModel = viewModel)
          "settings" -> SettingsScreen(viewModel = viewModel)
        }
      }

      // 12-Screen Presenter Overlay (Showcase Grid Hub)
      Box(
        modifier = Modifier
          .align(Alignment.BottomStart)
          .padding(start = 16.dp, bottom = if (isAuthenticated) 84.dp else 24.dp)
      ) {
        FloatingShowcaseTriggerButton(
          isOpen = isShowcaseHubOpen,
          onClick = { isShowcaseHubOpen = !isShowcaseHubOpen }
        )
      }

      if (isShowcaseHubOpen) {
        ShowcaseSelectorHub(
          onDismiss = { isShowcaseHubOpen = false },
          currentActiveScreen = currentScreen,
          onSelectScreen = { screenRoute ->
            if (listOf("home_feed", "explore", "create", "video_editor", "messaging", "notifications", "profile", "creator_dashboard", "settings").contains(screenRoute)) {
              viewModel.authenticate(true)
            } else {
              viewModel.authenticate(false)
            }
            viewModel.setScreen(screenRoute)
            isShowcaseHubOpen = false
          }
        )
      }
    }
  }
}

// ==========================================
// CUSTOM COMPOSABLES & UI PILLARS
// ==========================================

@Composable
fun GlowingButton(
  text: String,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
  glowingColor: Color = NeonCyan,
  icon: ImageVector? = null
) {
  val infiniteTransition = rememberInfiniteTransition(label = "ButtonGlow")
  val glowAlpha by infiniteTransition.animateFloat(
    initialValue = 0.4f,
    targetValue = 0.9f,
    animationSpec = infiniteRepeatable(
      animation = tween(1200, easing = EaseInOutSine),
      repeatMode = RepeatMode.Reverse
    ),
    label = "glowAlpha"
  )

  Button(
    onClick = onClick,
    modifier = modifier
      .shadow(
        elevation = 8.dp,
        shape = RoundedCornerShape(14.dp),
        clip = false,
        ambientColor = glowingColor.copy(alpha = glowAlpha),
        spotColor = glowingColor.copy(alpha = glowAlpha)
      )
      .border(
        width = 1.5.dp,
        brush = Brush.linearGradient(listOf(glowingColor, NeonPurple)),
        shape = RoundedCornerShape(14.dp)
      ),
    colors = ButtonDefaults.buttonColors(
      containerColor = Color(0xBF0F111E),
      contentColor = PureWhite
    ),
    shape = RoundedCornerShape(14.dp),
    contentPadding = PaddingValues(vertical = 14.dp, horizontal = 24.dp)
  ) {
    Row(verticalAlignment = Alignment.CenterVertically) {
      if (icon != null) {
        Icon(imageVector = icon, contentDescription = null, modifier = Modifier.size(18.dp))
        Spacer(modifier = Modifier.width(8.dp))
      }
      Text(
        text = text,
        fontSize = 15.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 0.5.sp,
        fontFamily = FontFamily.SansSerif
      )
    }
  }
}

@Composable
fun NaArgaBottomNavBar(
  currentScreen: String,
  onNavigate: (String) -> Unit
) {
  val tabs = listOf(
    Triple("home_feed", "Home", Icons.Default.Home),
    Triple("explore", "Explore", Icons.Default.Search),
    Triple("create", "Create", Icons.Default.AddCircle),
    Triple("messaging", "Direct", Icons.Default.Mail),
    Triple("profile", "Profile", Icons.Default.Person)
  )

  Surface(
    modifier = Modifier
      .fillMaxWidth()
      .windowInsetsPadding(WindowInsets.navigationBars),
    color = ObsidianBackground.copy(alpha = 0.9f),
    tonalElevation = 8.dp
  ) {
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .border(width = (0.5).dp, color = GlassOutline, shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
        .padding(vertical = 10.dp)
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = Alignment.CenterVertically
      ) {
        tabs.forEach { (route, label, icon) ->
          val isActive = currentScreen == route || (route == "create" && currentScreen == "video_editor")
          val activeColor = if (route == "create") NeonPink else NeonCyan
          val scale by animateFloatAsState(if (isActive) 1.15f else 1f, label = "ActiveTabScale")

          IconButton(
            onClick = { onNavigate(route) },
            modifier = Modifier
              .scale(scale)
              .testTag("nav_tab_$route")
          ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
              Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (isActive) activeColor else MutedGrey.copy(alpha = 0.7f),
                modifier = Modifier.size(24.dp)
              )
              Spacer(modifier = Modifier.height(2.dp))
              Text(
                text = label,
                fontSize = 10.sp,
                color = if (isActive) PureWhite else MutedGrey,
                fontWeight = if (isActive) FontWeight.SemiBold else FontWeight.Normal
              )
            }
          }
        }
      }
    }
  }
}

// ==========================================
// 1. ONBOARDING SCREEN
// ==========================================

@Composable
fun OnboardingScreen(onNavigateToLogin: () -> Unit) {
  var startAnim by remember { mutableStateOf(false) }
  LaunchedEffect(Unit) {
    delay(100)
    startAnim = true
  }

  val alphaValue by animateFloatAsState(
    targetValue = if (startAnim) 1f else 0f,
    animationSpec = tween(durationMillis = 1000, easing = EaseOutCubic),
    label = "OnboardAlpha"
  )

  Column(
    modifier = Modifier
      .fillMaxSize()
      .background(ObsidianBackground)
      .padding(horizontal = 24.dp)
      .statusBarsPadding()
      .navigationBarsPadding(),
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.SpaceBetween
  ) {
    // Header
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(top = 16.dp),
      horizontalArrangement = Arrangement.Center
    ) {
      Text(
        text = "NA ARGA",
        fontSize = 14.sp,
        fontWeight = FontWeight.W900,
        color = NeonCyan,
        letterSpacing = 6.sp,
        modifier = Modifier.testTag("onboard_brand_header")
      )
    }

    // Mid section Logo connection graphic
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .weight(1f),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.Center
    ) {
      Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
          .size(260.dp)
          .alpha(alphaValue)
      ) {
        val infiniteTransition = rememberInfiniteTransition(label = "LogoPulse")
        val pulseScale by infiniteTransition.animateFloat(
          initialValue = 0.95f,
          targetValue = 1.05f,
          animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
          ),
          label = "logoPulse"
        )
        val rotationAngle by infiniteTransition.animateFloat(
          initialValue = 0f,
          targetValue = 360f,
          animationSpec = infiniteRepeatable(
            animation = tween(12000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
          ),
          label = "logoRotation"
        )

        // Backdrop glowing circle
        Canvas(modifier = Modifier
          .fillMaxSize()
          .scale(pulseScale)) {
          drawCircle(
            brush = Brush.radialGradient(
              colors = listOf(NeonCyan.copy(alpha = 0.25f), NeonPurple.copy(alpha = 0.15f), Color.Transparent)
            ),
            radius = size.width / 2.1f
          )
        }

        // Futuristic connection wire logo drawn dynamically via Canvas
        Canvas(modifier = Modifier
          .size(200.dp)
          .rotate(rotationAngle)) {
          val stroke = 3f
          // Glowing orbital loop
          drawCircle(
            brush = Brush.linearGradient(listOf(NeonCyan, NeonPurple)),
            radius = size.width / 2.3f,
            style = Stroke(width = 6f, pathEffect = null)
          )

          // Inner cross nodes representing connection
          drawCircle(
            color = NeonPink,
            radius = 12f,
            center = Offset(size.width / 2 + 50, size.height / 2 - 50)
          )
          drawCircle(
            color = NeonCyan,
            radius = 12f,
            center = Offset(size.width / 2 - 60, size.height / 2 + 40)
          )
          drawCircle(
            color = NeonPurple,
            radius = 16f,
            center = Offset(size.width / 2, size.height / 2)
          )

          // Interconnecting cyberlines
          drawLine(
            brush = Brush.linearGradient(listOf(NeonCyan, NeonPurple)),
            start = Offset(size.width / 2 - 60, size.height / 2 + 40),
            end = Offset(size.width / 2, size.height / 2),
            strokeWidth = 4f
          )
          drawLine(
            brush = Brush.linearGradient(listOf(NeonPurple, NeonPink)),
            start = Offset(size.width / 2, size.height / 2),
            end = Offset(size.width / 2 + 50, size.height / 2 - 50),
            strokeWidth = 4f
          )
        }

        // Static Center Hologram Logo Emblem
        Box(
          modifier = Modifier
            .size(90.dp)
            .clip(CircleShape)
            .background(Color(0xCE080A15))
            .border(2.dp, NeonCyan, CircleShape),
          contentAlignment = Alignment.Center
        ) {
          Text(
            text = "NA",
            fontSize = 32.sp,
            color = PureWhite,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            letterSpacing = 1.sp
          )
        }
      }

      Spacer(modifier = Modifier.height(36.dp))

      Text(
        text = "Connect. Create. Discover.",
        fontSize = 24.sp,
        fontWeight = FontWeight.Bold,
        color = PureWhite,
        textAlign = TextAlign.Center,
        modifier = Modifier.alpha(alphaValue)
      )

      Spacer(modifier = Modifier.height(10.dp))

      Text(
        text = "Steer the matrix of premium short video curation. Explore 2026 sensory synthesis on Na Arga.",
        fontSize = 14.sp,
        color = MutedGrey,
        textAlign = TextAlign.Center,
        modifier = Modifier
          .padding(horizontal = 16.dp)
          .alpha(alphaValue)
      )
    }

    // Call Actions
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(bottom = 36.dp),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      GlowingButton(
        text = "Get Started",
        onClick = onNavigateToLogin,
        modifier = Modifier
          .fillMaxWidth()
          .testTag("onboard_get_started_btn"),
        glowingColor = NeonCyan
      )
      Spacer(modifier = Modifier.height(14.dp))
      Text(
        text = "Terms of Service & Neuro-Privacy compliant",
        color = MutedGrey.copy(alpha = 0.5f),
        fontSize = 11.sp
      )
    }
  }
}

// ==========================================
// 2. LOGIN SCREEN
// ==========================================

@Composable
fun LoginScreen(onLoginSuccess: () -> Unit) {
  var email by remember { mutableStateOf("") }
  var password by remember { mutableStateOf("") }
  val focusManager = LocalFocusManager.current

  Column(
    modifier = Modifier
      .fillMaxSize()
      .background(ObsidianBackground)
      .padding(horizontal = 24.dp)
      .statusBarsPadding()
      .navigationBarsPadding(),
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.Center
  ) {
    Text(
      text = "SECURE LOG IN",
      fontSize = 24.sp,
      fontWeight = FontWeight.Black,
      letterSpacing = 2.sp,
      color = PureWhite
    )
    Spacer(modifier = Modifier.height(4.dp))
    Text(
      text = "Join the 2026 decentralized creator collective",
      fontSize = 13.sp,
      color = MutedGrey
    )

    Spacer(modifier = Modifier.height(32.dp))

    GlassmorphicCard(
      modifier = Modifier
        .fillMaxWidth()
        .testTag("login_card_container"),
      cornerRadius = 24.dp,
      borderWidth = 1.5.dp,
      glowColor = NeonPurple.copy(alpha = 0.6f),
      glowRadius = 16.dp
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        Text(
          text = "ENTER CREDENTIALS",
          fontSize = 11.sp,
          color = NeonCyan,
          fontWeight = FontWeight.Bold,
          letterSpacing = 1.5.sp,
          modifier = Modifier.align(Alignment.Start)
        )
        Spacer(modifier = Modifier.height(14.dp))

        // Email field
        OutlinedTextField(
          value = email,
          onValueChange = { email = it },
          label = { Text("Quantum Mail") },
          colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = NeonCyan,
            unfocusedBorderColor = GlassOutline,
            focusedTextColor = PureWhite,
            unfocusedTextColor = PureWhite,
            focusedContainerColor = ObsidianBackground.copy(alpha = 0.5f)
          ),
          modifier = Modifier
            .fillMaxWidth()
            .testTag("login_email_input"),
          singleLine = true,
          shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Password field
        OutlinedTextField(
          value = password,
          onValueChange = { password = it },
          label = { Text("Cyber Key") },
          visualTransformation = PasswordVisualTransformation(),
          colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = NeonPurple,
            unfocusedBorderColor = GlassOutline,
            focusedTextColor = PureWhite,
            unfocusedTextColor = PureWhite,
            focusedContainerColor = ObsidianBackground.copy(alpha = 0.5f)
          ),
          modifier = Modifier
            .fillMaxWidth()
            .testTag("login_password_input"),
          singleLine = true,
          shape = RoundedCornerShape(12.dp),
          keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
          keyboardActions = KeyboardActions(onDone = {
            focusManager.clearFocus()
            onLoginSuccess()
          })
        )

        Spacer(modifier = Modifier.height(24.dp))

        GlowingButton(
          text = "Sync Uplink",
          onClick = onLoginSuccess,
          modifier = Modifier
            .fillMaxWidth()
            .testTag("login_submit_btn"),
          glowingColor = NeonPurple
        )
      }
    }

    Spacer(modifier = Modifier.height(28.dp))

    Text(
      text = "OR ALTERNATIVE CHANNELS",
      fontSize = 10.sp,
      fontWeight = FontWeight.Bold,
      color = MutedGrey,
      letterSpacing = 1.5.sp
    )

    Spacer(modifier = Modifier.height(16.dp))

    // Google & Apple login
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
      Button(
        onClick = onLoginSuccess,
        modifier = Modifier
          .weight(1f)
          .height(52.dp)
          .border(1.dp, GlassOutline, RoundedCornerShape(14.dp))
          .testTag("google_login_btn"),
        colors = ButtonDefaults.buttonColors(containerColor = GlassBg)
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(imageVector = Icons.Default.Cloud, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(18.dp))
          Spacer(modifier = Modifier.width(8.dp))
          Text("Google", color = PureWhite, fontSize = 14.sp, fontWeight = FontWeight.Bold)
        }
      }

      Button(
        onClick = onLoginSuccess,
        modifier = Modifier
          .weight(1f)
          .height(52.dp)
          .border(1.dp, GlassOutline, RoundedCornerShape(14.dp))
          .testTag("apple_login_btn"),
        colors = ButtonDefaults.buttonColors(containerColor = GlassBg)
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(imageVector = Icons.Default.Hub, contentDescription = null, tint = NeonPink, modifier = Modifier.size(18.dp))
          Spacer(modifier = Modifier.width(8.dp))
          Text("Apple ID", color = PureWhite, fontSize = 14.sp, fontWeight = FontWeight.Bold)
        }
      }
    }
  }
}

// ==========================================
// 3. HOME FEED
// ==========================================

@Composable
fun HomeFeedScreen(viewModel: NaArgaViewModel) {
  val posts by viewModel.videoPosts.collectAsStateWithLifecycle()
  val activeIndex by viewModel.activeFeedIndex.collectAsStateWithLifecycle()

  var isCommentsOpen by remember { mutableStateOf(false) }

  if (posts.isEmpty()) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
      CircularProgressIndicator(color = NeonCyan)
    }
    return
  }

  val activePost = posts[activeIndex % posts.size]

  Box(
    modifier = Modifier
      .fillMaxSize()
      .background(ObsidianBackground)
      .testTag("home_feed_screen")
  ) {
    // Simulated Video background using a rich gradient with rotating particles
    Box(
      modifier = Modifier
        .fillMaxSize()
        .background(
          Brush.verticalGradient(
            colors = activePost.videoGradient
          )
        )
    ) {
      // Rotating Matrix Circle overlaying standard feeds
      val infiniteTransition = rememberInfiniteTransition(label = "BgRotate")
      val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
          animation = tween(18000, easing = LinearEasing),
          repeatMode = RepeatMode.Restart
        ),
        label = "bgRotation"
      )
      Canvas(modifier = Modifier
        .fillMaxSize()
        .rotate(rotation)
        .alpha(0.12f)) {
        drawCircle(
          brush = Brush.radialGradient(listOf(NeonCyan, Color.Transparent)),
          radius = size.width * 0.7f,
          center = Offset(size.width, size.height)
        )
      }

      // Vertical swipe detectors at the extreme top-mid segments
      Column(
        modifier = Modifier
          .fillMaxSize()
          .clickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = null
          ) {
            viewModel.nextFeedPost()
          }
      ) {}
    }

    // Gradient fade shadow overlays
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .height(180.dp)
        .background(Brush.verticalGradient(listOf(Color(0xE006070B), Color.Transparent)))
        .align(Alignment.TopCenter)
    )
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .height(240.dp)
        .background(Brush.verticalGradient(listOf(Color.Transparent, Color(0xF406070B))))
        .align(Alignment.BottomCenter)
    )

    // Top Header Switch ("Live Sync" / "For You")
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .statusBarsPadding()
        .padding(horizontal = 24.dp, vertical = 12.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      IconButton(onClick = { viewModel.setScreen("notifications") }) {
        Icon(imageVector = Icons.Default.Notifications, contentDescription = "Alerts", tint = PureWhite)
      }

      Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        Text("MATRIX", fontSize = 14.sp, color = MutedGrey, fontWeight = FontWeight.Bold)
        Text(
          "FOR YOU",
          fontSize = 14.sp,
          color = NeonCyan,
          fontWeight = FontWeight.Bold,
          modifier = Modifier.drawBehind {
            val strokeWidth = 1.5.dp.toPx()
            val w = this.size.width
            val h = this.size.height
            val y = h - strokeWidth / 2
            drawLine(
              color = NeonCyan,
              start = Offset(0f, y),
              end = Offset(w, y),
              strokeWidth = strokeWidth
            )
          }
        )
      }

      IconButton(onClick = { viewModel.setScreen("settings") }) {
        Icon(imageVector = Icons.Default.Settings, contentDescription = "Settings", tint = PureWhite)
      }
    }

    // Creator Overlay Information Info pane
    Column(
      modifier = Modifier
        .fillMaxWidth(0.82f)
        .align(Alignment.BottomStart)
        .padding(start = 16.dp, bottom = 24.dp)
    ) {
      Row(verticalAlignment = Alignment.CenterVertically) {
        // Avatar circle
        Box(
          modifier = Modifier
            .size(42.dp)
            .clip(CircleShape)
            .background(NeonPurple)
            .border(1.5.dp, NeonCyan, CircleShape),
          contentAlignment = Alignment.Center
        ) {
          Text(
            text = activePost.creatorHandle.getOrNull(1)?.uppercase() ?: "K",
            color = PureWhite,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp
          )
        }
        Spacer(modifier = Modifier.width(10.dp))
        Text(
          text = activePost.creatorHandle,
          color = PureWhite,
          fontWeight = FontWeight.ExtraBold,
          fontSize = 16.sp,
          modifier = Modifier.testTag("feed_creator_handle")
        )
        Spacer(modifier = Modifier.width(8.dp))

        // Follow check pill button
        Box(
          modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(if (activePost.initialIsFollowing) Color(0x3D00FF87) else NeonPink)
            .clickable { viewModel.toggleFollow(activePost.id) }
            .padding(horizontal = 8.dp, vertical = 3.dp)
        ) {
          Text(
            text = if (activePost.initialIsFollowing) "Following" else "+ Follow",
            color = PureWhite,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold
          )
        }
      }

      Spacer(modifier = Modifier.height(10.dp))

      Text(
        text = activePost.videoTitle,
        color = PureWhite,
        fontSize = 13.sp,
        maxLines = 3,
        overflow = TextOverflow.Ellipsis
      )

      val hashtagsStr = activePost.hashtags.joinToString(" ")
      Text(
        text = hashtagsStr,
        color = NeonCyan,
        fontSize = 13.sp,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier.padding(top = 4.dp)
      )

      Spacer(modifier = Modifier.height(12.dp))

      // Music Audio symbol
      Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(8.dp))
          .background(Color(0x1E000000))
          .padding(horizontal = 8.dp, vertical = 6.dp)
      ) {
        Icon(imageVector = Icons.Default.MusicNote, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(16.dp))
        Spacer(modifier = Modifier.width(6.dp))
        Text(
          text = activePost.songTitle,
          color = MutedGrey,
          fontSize = 11.sp,
          overflow = TextOverflow.Ellipsis,
          maxLines = 1
        )
      }
    }

    // Right Action Sidebar (Likes, comments, shares buttons list)
    Column(
      modifier = Modifier
        .align(Alignment.BottomEnd)
        .padding(end = 16.dp, bottom = 48.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
      // Like icon
      ActionIconButton(
        icon = Icons.Default.Favorite,
        isActive = activePost.initialIsLiked,
        activeColor = NeonPink,
        countText = activePost.likesCount.toString(),
        onClick = { viewModel.toggleLike(activePost.id) },
        testTag = "like_action_btn"
      )

      // Comment icon
      ActionIconButton(
        icon = Icons.Default.Comment,
        isActive = isCommentsOpen,
        activeColor = NeonCyan,
        countText = activePost.commentsCount.toString(),
        onClick = { isCommentsOpen = true },
        testTag = "comment_action_btn"
      )

      // Share icon
      ActionIconButton(
        icon = Icons.Default.Share,
        isActive = false,
        activeColor = NeonPurple,
        countText = activePost.shareCount.toString(),
        onClick = { viewModel.setScreen("video_detail") },
        testTag = "share_action_btn"
      )

      // Dynamic disc simulation rotating
      val infiniteTransition = rememberInfiniteTransition(label = "DiscRotation")
      val discRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
          animation = tween(4000, easing = LinearEasing),
          repeatMode = RepeatMode.Restart
        ),
        label = "disc"
      )

      Box(
        modifier = Modifier
          .size(46.dp)
          .clip(CircleShape)
          .background(Color(0xFF04060C))
          .border(2.dp, NeonPurple, CircleShape),
        contentAlignment = Alignment.Center
      ) {
        Box(
          modifier = Modifier
            .size(32.dp)
            .rotate(discRotation)
            .clip(CircleShape)
            .background(Brush.sweepGradient(listOf(NeonCyan, NeonPink, NeonPurple, NeonCyan)))
        )
      }
    }

    // Progress bar for custom videos
    LinearProgressIndicator(
      progress = { 0.45f },
      modifier = Modifier
        .fillMaxWidth()
        .height(3.dp)
        .align(Alignment.BottomCenter),
      color = NeonCyan,
      trackColor = GlassOutline.copy(alpha = 0.2f)
    )

    // Slides overlay Comments Overlay sheet
    if (isCommentsOpen) {
      CommentsBottomSheet(
        viewModel = viewModel,
        activePost = activePost,
        onDismiss = { isCommentsOpen = false }
      )
    }
  }
}

@Composable
fun ActionIconButton(
  icon: ImageVector,
  isActive: Boolean,
  activeColor: Color,
  countText: String,
  onClick: () -> Unit,
  testTag: String
) {
  val scale by animateFloatAsState(if (isActive) 1.25f else 1.0f, label = "ButtonScale")

  Column(
    horizontalAlignment = Alignment.CenterHorizontally,
    modifier = Modifier.scale(scale)
  ) {
    IconButton(
      onClick = onClick,
      modifier = Modifier
        .size(48.dp)
        .clip(CircleShape)
        .background(GlassBg)
        .border(1.dp, GlassOutline, CircleShape)
        .testTag(testTag)
    ) {
      Icon(
        imageVector = icon,
        contentDescription = null,
        tint = if (isActive) activeColor else PureWhite,
        modifier = Modifier.size(22.dp)
      )
    }
    Spacer(modifier = Modifier.height(3.dp))
    Text(
      text = countText,
      color = PureWhite,
      fontSize = 11.sp,
      fontWeight = FontWeight.Bold
    )
  }
}

// Bottom sheet overlay for comment additions
@Composable
fun CommentsBottomSheet(
  viewModel: NaArgaViewModel,
  activePost: VideoPost,
  onDismiss: () -> Unit
) {
  val commentsFlow by viewModel.videoComments.collectAsStateWithLifecycle()
  val postComments = commentsFlow[activePost.id] ?: emptyList()
  var inputCommentText by remember { mutableStateOf("") }

  Box(
    modifier = Modifier
      .fillMaxSize()
      .background(Color(0xBF050508))
      .clickable { onDismiss() }
  ) {
    Surface(
      modifier = Modifier
        .fillMaxWidth()
        .fillMaxHeight(0.6f)
        .align(Alignment.BottomCenter)
        .clickable(enabled = false, onClick = {}),
      shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
      color = CyberBackgroundCard,
      tonalElevation = 12.dp
    ) {
      Column(
        modifier = Modifier
          .fillMaxSize()
          .border(
            BorderStroke(1.5.dp, Brush.verticalGradient(listOf(NeonCyan, Color.Transparent))),
            RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
          )
          .padding(20.dp)
      ) {
        // Drag Handle
        Box(
          modifier = Modifier
            .size(45.dp, 4.dp)
            .clip(RoundedCornerShape(2.dp))
            .background(MutedGrey.copy(alpha = 0.5f))
            .align(Alignment.CenterHorizontally)
        )

        Spacer(modifier = Modifier.height(14.dp))

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = "COMMENTS",
            fontSize = 14.sp,
            color = NeonCyan,
            fontWeight = FontWeight.Bold,
            letterSpacing = 2.sp
          )
          IconButton(onClick = onDismiss) {
            Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = PureWhite)
          }
        }

        Divider(modifier = Modifier.padding(vertical = 10.dp), color = GlassOutline)

        // List
        LazyColumn(
          modifier = Modifier
            .weight(1f)
            .fillMaxWidth()
        ) {
          if (postComments.isEmpty()) {
            item {
              Box(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(vertical = 40.dp),
                contentAlignment = Alignment.Center
              ) {
                Text("Be the first to leave an offline telemetry note!", color = MutedGrey, fontSize = 13.sp)
              }
            }
          } else {
            items(postComments) { (author, txt) ->
              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(vertical = 10.dp)
              ) {
                Box(
                  modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(NeonPurple),
                  contentAlignment = Alignment.Center
                ) {
                  Text(author.take(1).uppercase(), color = PureWhite, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                  Text(author, fontSize = 12.sp, color = NeonCyan, fontWeight = FontWeight.SemiBold)
                  Spacer(modifier = Modifier.height(2.dp))
                  Text(txt, fontSize = 13.sp, color = PureWhite)
                }
              }
            }
          }
        }

        // Add commentary row
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(top = 10.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          OutlinedTextField(
            value = inputCommentText,
            onValueChange = { inputCommentText = it },
            placeholder = { Text("Add comment to feed...", color = MutedGrey) },
            colors = OutlinedTextFieldDefaults.colors(
              focusedTextColor = PureWhite,
              unfocusedTextColor = PureWhite,
              focusedBorderColor = NeonCyan,
              unfocusedBorderColor = GlassOutline
            ),
            modifier = Modifier
              .weight(1f)
              .testTag("feed_comment_input"),
            singleLine = true,
            shape = RoundedCornerShape(12.dp)
          )

          Spacer(modifier = Modifier.width(10.dp))

          IconButton(
            onClick = {
              viewModel.addComment(activePost.id, "Me", inputCommentText)
              inputCommentText = ""
            },
            modifier = Modifier
              .clip(CircleShape)
              .background(NeonCyan)
              .testTag("feed_comment_submit_btn")
          ) {
            Icon(imageVector = Icons.Default.Send, contentDescription = "Send", tint = ObsidianBackground)
          }
        }
      }
    }
  }
}

// ==========================================
// 4. EXPLORE SCREEN
// ==========================================

@Composable
fun ExploreScreen(viewModel: NaArgaViewModel) {
  val posts by viewModel.videoPosts.collectAsStateWithLifecycle()
  var searchText by remember { mutableStateOf("") }

  val categories = listOf("Decentralized", "Visual Synth", "#2026", "Holography", "#neural", "Quantum Core")
  val focusManager = LocalFocusManager.current

  Column(
    modifier = Modifier
      .fillMaxSize()
      .background(ObsidianBackground)
      .padding(horizontal = 16.dp)
      .statusBarsPadding()
  ) {
    Spacer(modifier = Modifier.height(14.dp))
    
    // Glassmorphic Interactive Search Bar
    GlassmorphicCard(
      modifier = Modifier.fillMaxWidth().height(54.dp),
      cornerRadius = 14.dp,
      borderWidth = 1.dp
    ) {
      Row(
        modifier = Modifier.fillMaxSize().padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        Icon(imageVector = Icons.Default.Search, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Box(modifier = Modifier.weight(1f)) {
          if (searchText.isEmpty()) {
            Text("Search tags, links, channels...", color = MutedGrey, fontSize = 14.sp)
          }
          BasicTextField(
            value = searchText,
            onValueChange = { newValue -> searchText = newValue },
            textStyle = androidx.compose.ui.text.TextStyle(color = PureWhite, fontSize = 14.sp),
            modifier = Modifier.fillMaxWidth().testTag("explore_search_bar"),
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(onSearch = { focusManager.clearFocus() })
          )
        }
        if (searchText.isNotEmpty()) {
          IconButton(onClick = { searchText = "" }) {
            Icon(imageVector = Icons.Default.Close, contentDescription = "clear", tint = PureWhite)
          }
        }
      }
    }

    Spacer(modifier = Modifier.height(14.dp))

    // Hashtags Categories scrollable chips
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .horizontalScroll(rememberScrollState()),
      horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      categories.forEach { tag ->
        Box(
          modifier = Modifier
            .clip(RoundedCornerShape(32.dp))
            .background(GlassBg)
            .border(0.5.dp, GlassOutline, RoundedCornerShape(32.dp))
            .clickable { searchText = tag }
            .padding(horizontal = 14.dp, vertical = 8.dp)
        ) {
          Text(text = tag, color = if (searchText == tag) NeonCyan else PureWhite, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
      }
    }

    Spacer(modifier = Modifier.height(20.dp))

    // Trending Title
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Text(
        "TRENDING DISCOVERY",
        fontSize = 12.sp,
        fontWeight = FontWeight.ExtraBold,
        letterSpacing = 1.5.sp,
        color = NeonPurple
      )
      Text("Holograms Live", color = NeonCyan, fontSize = 11.sp, fontWeight = FontWeight.Bold)
    }

    Spacer(modifier = Modifier.height(10.dp))

    // Premium Trending Creators horizontal list
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .horizontalScroll(rememberScrollState()),
      horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
      val mockCreators = listOf(
        "@Vesper_X" to "Nexus",
        "@Aria_Vibe" to "Runway",
        "@Chronos_Lab" to "Time",
        "@Neon_Siren" to "Cyber",
        "Your Space" to "+ Upload"
      )
      mockCreators.forEach { (handle, badge) ->
        Column(
          horizontalAlignment = Alignment.CenterHorizontally,
          modifier = Modifier.clickable {
            if (handle.startsWith("@")) {
              viewModel.selectChatCreator(handle)
              viewModel.setScreen("messaging")
            } else {
              viewModel.setScreen("create")
            }
          }
        ) {
          Box(
            modifier = Modifier
              .size(56.dp)
              .clip(CircleShape)
              .background(Brush.sweepGradient(listOf(NeonCyan, NeonPurple, NeonPink, NeonCyan)))
              .padding(2.5.dp)
              .clip(CircleShape)
              .background(ObsidianBackground),
            contentAlignment = Alignment.Center
          ) {
            Box(
              modifier = Modifier
                .size(46.dp)
                .clip(CircleShape)
                .background(Color(0xFF0C0E18)),
              contentAlignment = Alignment.Center
            ) {
              Text(
                text = handle.take(2).uppercase().replace("@", ""),
                color = PureWhite,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp
              )
            }
          }
          Spacer(modifier = Modifier.height(4.dp))
          Text(text = badge, fontSize = 10.sp, color = MutedGrey, fontWeight = FontWeight.Bold)
        }
      }
    }

    Spacer(modifier = Modifier.height(24.dp))

    Text(
      "CURATED MATRIX REEL",
      fontSize = 12.sp,
      fontWeight = FontWeight.ExtraBold,
      letterSpacing = 1.5.sp,
      color = NeonCyan,
      modifier = Modifier.padding(bottom = 10.dp)
    )

    // Visual grid representing videos
    LazyVerticalGrid(
      columns = GridCells.Fixed(2),
      horizontalArrangement = Arrangement.spacedBy(12.dp),
      verticalArrangement = Arrangement.spacedBy(12.dp),
      modifier = Modifier.weight(1f)
    ) {
      items(posts) { post ->
        GlassmorphicCard(
          modifier = Modifier
            .fillMaxWidth()
            .height(170.dp)
            .clickable {
              viewModel.setScreen("video_detail")
            },
          cornerRadius = 16.dp
        ) {
          // Inner grid gradient simulation
          Box(
            modifier = Modifier
              .fillMaxSize()
              .background(Brush.radialGradient(post.videoGradient))
          ) {
            // Glass card overlay containing info info
            Column(
              modifier = Modifier
                .fillMaxSize()
                .padding(10.dp),
              verticalArrangement = Arrangement.SpaceBetween
            ) {
              Box(
                modifier = Modifier
                  .clip(RoundedCornerShape(6.dp))
                  .background(Color(0x8D000000))
                  .padding(horizontal = 6.dp, vertical = 2.dp)
              ) {
                Text(text = post.viewCount, color = NeonCyan, fontSize = 9.sp, fontWeight = FontWeight.Bold)
              }

              Column {
                Text(
                  text = post.creatorHandle,
                  color = PureWhite,
                  fontSize = 11.sp,
                  fontWeight = FontWeight.Bold,
                  overflow = TextOverflow.Ellipsis,
                  maxLines = 1
                )
                Text(
                  text = post.videoTitle,
                  color = MutedGrey,
                  fontSize = 10.sp,
                  maxLines = 2,
                  overflow = TextOverflow.Ellipsis
                )
              }
            }
          }
        }
      }
    }
  }
}

// ==========================================
// 5. VIDEO DETAIL SCREEN
// ==========================================

@Composable
fun VideoDetailScreen(viewModel: NaArgaViewModel) {
  val posts by viewModel.videoPosts.collectAsStateWithLifecycle()
  val activeIndex by viewModel.activeFeedIndex.collectAsStateWithLifecycle()
  val activePost = posts[activeIndex % posts.size]

  var viewShowcasePng by remember { mutableStateOf(false) }

  Column(
    modifier = Modifier
      .fillMaxSize()
      .background(ObsidianBackground)
      .statusBarsPadding()
      .padding(horizontal = 16.dp)
  ) {
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = { viewModel.setScreen("home_feed") }) {
          Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back", tint = PureWhite)
        }
      Text("VIDEO TELEMETRY", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = NeonCyan, letterSpacing = 2.sp)
      IconButton(onClick = { viewModel.setScreen("creator_dashboard") }) {
        Icon(imageVector = Icons.Default.Analytics, contentDescription = "Dashboard", tint = NeonPurple)
      }
    }

    Spacer(modifier = Modifier.height(12.dp))

    // Grid details Layout
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .weight(1.1f)
        .clip(RoundedCornerShape(24.dp))
        .border(1.dp, NeonCyan, RoundedCornerShape(24.dp))
        .background(Brush.verticalGradient(activePost.videoGradient))
    ) {
      if (viewShowcasePng) {
        // Attempt to render the generated image presentation mockup directly on client view
        Image(
          painter = painterResource(id = R.drawable.img_ui_showcase_1780364711486),
          contentDescription = "UI Showcase Illustration",
          modifier = Modifier.fillMaxSize(),
          contentScale = ContentScale.Crop
        )

        // Title Tag
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xE0050512))
            .align(Alignment.BottomCenter)
            .padding(12.dp)
        ) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column {
              Text("PRESENTER ARTWORK", color = NeonCyan, fontSize = 11.sp, fontWeight = FontWeight.Bold)
              Text("High-fidelity 2026 Grid Showcase mockup render.", color = PureWhite, fontSize = 12.sp)
            }
            IconButton(onClick = { viewShowcasePng = false }) {
              Icon(imageVector = Icons.Default.Videocam, contentDescription = "Video", tint = NeonPink)
            }
          }
        }
      } else {
        // Full interactive video metrics overlay representation
        Column(
          modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
          verticalArrangement = Arrangement.SpaceBetween
        ) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Box(
              modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0x7D000000))
                .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
              Text(text = "UPLINK: ACTIVE", color = NeonCyan, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
            }

            IconButton(
              onClick = { viewShowcasePng = true },
              modifier = Modifier
                .clip(CircleShape)
                .background(Color(0x3B607D8B))
            ) {
              Icon(imageVector = Icons.Default.Preview, contentDescription = "Preview PNG", tint = PureWhite)
            }
          }

          Column(horizontalAlignment = Alignment.Start) {
            Text(activePost.creatorName, color = PureWhite, fontSize = 20.sp, fontWeight = FontWeight.Black)
            Text(activePost.creatorHandle, color = NeonCyan, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(10.dp))
            Text(
              activePost.videoTitle,
              color = PureWhite,
              fontSize = 14.sp,
              lineHeight = 20.sp
            )
          }
        }
      }
    }

    Spacer(modifier = Modifier.height(16.dp))

    // Share Options & Comments Detail pane bottom part
    Text(
      "DISSECT METRIC DETAILS & INHERITED REEL",
      fontSize = 11.sp,
      fontWeight = FontWeight.Bold,
      color = MutedGrey,
      letterSpacing = 1.sp
    )

    Spacer(modifier = Modifier.height(10.dp))

    LazyColumn(
      modifier = Modifier
        .weight(1f)
        .fillMaxWidth()
    ) {
      item {
        Card(
          colors = CardDefaults.cardColors(containerColor = CyberBackgroundCard),
          shape = RoundedCornerShape(16.dp),
          modifier = Modifier.padding(bottom = 12.dp)
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceAround
          ) {
            MetricStat("Likes", activePost.likesCount.toString(), NeonPink)
            MetricStat("Replies", activePost.commentsCount.toString(), NeonCyan)
            MetricStat("Shares", activePost.shareCount.toString(), NeonPurple)
          }
        }
      }

      item {
        Text("RELATED MATRIX REELS", fontSize = 13.sp, color = PureWhite, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 10.dp))
      }

      items(posts.filter { it.id != activePost.id }) { postItem ->
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(GlassBg)
            .clickable {
              // Swap active in viewmodel
            }
            .padding(8.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Box(
            modifier = Modifier
              .size(54.dp)
              .clip(RoundedCornerShape(8.dp))
              .background(Brush.horizontalGradient(postItem.videoGradient))
          )
          Spacer(modifier = Modifier.width(10.dp))
          Column(modifier = Modifier.weight(1f)) {
            Text(postItem.creatorHandle, color = NeonCyan, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Text(postItem.videoTitle, color = PureWhite, fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
          }
          Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null, tint = PureWhite)
        }
      }
    }
  }
}

@Composable
fun MetricStat(label: String, value: String, color: Color) {
  Column(horizontalAlignment = Alignment.CenterHorizontally) {
    Text(text = value, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = color)
    Text(text = label, fontSize = 11.sp, color = MutedGrey)
  }
}

// ==========================================
// 6. CREATE SCREEN
// ==========================================

@Composable
fun CreateScreen(viewModel: NaArgaViewModel) {
  val isRecording by viewModel.isRecording.collectAsStateWithLifecycle()
  val recDuration by viewModel.recordingDurationSeconds.collectAsStateWithLifecycle()

  var isTimerRunning by remember { mutableStateOf(false) }

  // Simulate record progress loop
  LaunchedEffect(isRecording) {
    if (isRecording) {
      isTimerRunning = true
      while (isTimerRunning) {
        delay(1000)
        // trigger counter tick in viewmodel or state
      }
    } else {
      isTimerRunning = false
    }
  }

  Box(
    modifier = Modifier
      .fillMaxSize()
      .background(ObsidianBackground)
      .testTag("create_screen")
  ) {
    // Camera Simulator Background
    Box(
      modifier = Modifier
        .fillMaxSize()
        .background(Color(0xFF030508))
    ) {
      // Focus brackets drawing
      Canvas(modifier = Modifier.fillMaxSize()) {
        val stroke = 3f
        val bracketLen = 40f
        val gap = 120f
        
        // Center Brackets representation
        // Top Left
        drawPath(
          color = NeonCyan.copy(alpha = 0.5f),
          path = androidx.compose.ui.graphics.Path().apply {
            moveTo(size.width / 2 - gap, size.height / 2 - gap + bracketLen)
            lineTo(size.width / 2 - gap, size.height / 2 - gap)
            lineTo(size.width / 2 - gap + bracketLen, size.height / 2 - gap)
          },
          style = Stroke(width = stroke)
        )
        // Top Right
        drawPath(
          color = NeonCyan.copy(alpha = 0.5f),
          path = androidx.compose.ui.graphics.Path().apply {
            moveTo(size.width / 2 + gap, size.height / 2 - gap + bracketLen)
            lineTo(size.width / 2 + gap, size.height / 2 - gap)
            lineTo(size.width / 2 + gap - bracketLen, size.height / 2 - gap)
          },
          style = Stroke(width = stroke)
        )
        // Bottom Left
        drawPath(
          color = NeonCyan.copy(alpha = 0.5f),
          path = androidx.compose.ui.graphics.Path().apply {
            moveTo(size.width / 2 - gap, size.height / 2 + gap - bracketLen)
            lineTo(size.width / 2 - gap, size.height / 2 + gap)
            lineTo(size.width / 2 - gap + bracketLen, size.height / 2 + gap)
          },
          style = Stroke(width = stroke)
        )
        // Bottom Right
        drawPath(
          color = NeonCyan.copy(alpha = 0.5f),
          path = androidx.compose.ui.graphics.Path().apply {
            moveTo(size.width / 2 + gap, size.height / 2 + gap - bracketLen)
            lineTo(size.width / 2 + gap, size.height / 2 + gap)
            lineTo(size.width / 2 + gap - bracketLen, size.height / 2 + gap)
          },
          style = Stroke(width = stroke)
        )
      }
    }

    // Top Settings Toolbar
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .statusBarsPadding()
        .padding(horizontal = 24.dp, vertical = 12.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      IconButton(onClick = { viewModel.setScreen("home_feed") }) {
        Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = PureWhite)
      }

      // Add Music Pill
      Box(
        modifier = Modifier
          .clip(RoundedCornerShape(24.dp))
          .background(Color(0xC20F111E))
          .border(1.dp, NeonCyan, RoundedCornerShape(24.dp))
          .clickable { /* Select audio */ }
          .padding(horizontal = 14.dp, vertical = 6.dp)
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(imageVector = Icons.Default.MusicNote, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(14.dp))
          Spacer(modifier = Modifier.width(6.dp))
          Text(text = viewModel.selectedMusic.value, color = PureWhite, fontSize = 11.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
      }

      IconButton(onClick = { /* Flash control */ }) {
        Icon(imageVector = Icons.Outlined.FlashOn, contentDescription = "Flash", tint = PureWhite)
      }
    }

    // Right Floating Side Actions toolbar list
    Column(
      modifier = Modifier
        .align(Alignment.CenterEnd)
        .padding(end = 16.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
      CameraActionPill(icon = Icons.Outlined.FlipCameraAndroid, text = "Flip")
      CameraActionPill(icon = Icons.Outlined.Speed, text = "1.0x")
      CameraActionPill(icon = Icons.Outlined.AutoFixHigh, text = "Effects")
      CameraActionPill(icon = Icons.Outlined.Timer, text = "Timer")
      CameraActionPill(icon = Icons.Outlined.FilterBAndW, text = "Filters")
    }

    // Center bottom Recording indicator
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .align(Alignment.BottomCenter)
        .padding(bottom = 36.dp),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      if (isRecording) {
        Text(
          text = "REC 0:08",
          color = NeonPink,
          fontWeight = FontWeight.ExtraBold,
          fontSize = 14.sp,
          letterSpacing = 1.sp,
          modifier = Modifier.padding(bottom = 12.dp)
        )
      }

      Row(
        modifier = Modifier.fillMaxWidth(0.85f),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
      ) {
        // Upload photo card thumbnail
        Column(
          horizontalAlignment = Alignment.CenterHorizontally,
          modifier = Modifier.clickable { /* Photo pick */ }
        ) {
          Box(
            modifier = Modifier
              .size(46.dp)
              .clip(RoundedCornerShape(8.dp))
              .background(GlassBg)
              .border(1.dp, GlassOutline, RoundedCornerShape(8.dp))
          )
          Spacer(modifier = Modifier.height(3.dp))
          Text("Upload", color = PureWhite, fontSize = 11.sp)
        }

        // Recording circle triggers action
        Box(
          modifier = Modifier
            .size(80.dp)
            .clip(CircleShape)
            .background(Color.Transparent)
            .border(4.dp, PureWhite, CircleShape)
            .padding(6.dp)
            .clickable {
              if (isRecording) {
                viewModel.stopRecording()
                viewModel.setScreen("video_editor")
              } else {
                viewModel.startRecording()
              }
            },
          contentAlignment = Alignment.Center
        ) {
          val recordScale by animateFloatAsState(if (isRecording) 0.85f else 1f, label = "RecordScale")
          Box(
            modifier = Modifier
              .fillMaxSize()
              .scale(recordScale)
              .clip(CircleShape)
              .background(if (isRecording) NeonPink else NeonPurple)
          )
        }

        // Advanced editing editor bypass
        Column(
          horizontalAlignment = Alignment.CenterHorizontally,
          modifier = Modifier.clickable {
            viewModel.setScreen("video_editor")
          }
        ) {
          IconButton(
            onClick = { viewModel.setScreen("video_editor") },
            modifier = Modifier
              .size(46.dp)
              .clip(CircleShape)
              .background(GlassBg)
              .border(1.dp, NeonCyan, CircleShape)
          ) {
            Icon(imageVector = Icons.Default.Hardware, contentDescription = "Editor Matrix", tint = NeonCyan, modifier = Modifier.size(18.dp))
          }
          Spacer(modifier = Modifier.height(3.dp))
          Text("Timeline", color = NeonCyan, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        }
      }
    }
  }
}

@Composable
fun CameraActionPill(icon: ImageVector, text: String) {
  Column(horizontalAlignment = Alignment.CenterHorizontally) {
    IconButton(
      onClick = { /* Action */ },
      modifier = Modifier
        .size(40.dp)
        .clip(CircleShape)
        .background(Color(0xA006070B))
        .border(0.5.dp, GlassOutline, CircleShape)
    ) {
      Icon(imageVector = icon, contentDescription = text, tint = PureWhite, modifier = Modifier.size(18.dp))
    }
    Spacer(modifier = Modifier.height(2.dp))
    Text(text = text, color = PureWhite, fontSize = 9.sp, fontWeight = FontWeight.Bold)
  }
}

// ==========================================
// 7. VIDEO EDITOR SCREEN
// ==========================================

@Composable
fun VideoEditorScreen(viewModel: NaArgaViewModel) {
  var videoTitleText by remember { mutableStateOf("") }
  var hashtagsText by remember { mutableStateOf("#cyber #25th") }
  val focusManager = LocalFocusManager.current

  Column(
    modifier = Modifier
      .fillMaxSize()
      .background(ObsidianBackground)
      .statusBarsPadding()
      .padding(horizontal = 16.dp),
    verticalArrangement = Arrangement.SpaceBetween
  ) {
    // Header Part
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      IconButton(onClick = { viewModel.setScreen("create") }) {
        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back", tint = PureWhite)
      }
      Text("CYBER TIMELINE STUDIO", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = NeonPurple, letterSpacing = 2.sp)
      GlowingButton(
        text = "Publish",
        onClick = {
          viewModel.simulateUploadVideo(videoTitleText, hashtagsText)
          viewModel.setScreen("profile")
        },
        modifier = Modifier
          .height(38.dp)
          .testTag("publish_stream_btn"),
        glowingColor = NeonPink
      )
    }

    Spacer(modifier = Modifier.height(10.dp))

    // Preview Window Box simulation
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .height(180.dp)
        .clip(RoundedCornerShape(16.dp))
        .background(Brush.linearGradient(listOf(NeonPurple, NeonPink, ObsidianBackground))),
      contentAlignment = Alignment.Center
    ) {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null, tint = PureWhite, modifier = Modifier.size(36.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Text("Active Rendering Pipeline (0:08s)", color = PureWhite, fontSize = 14.sp, fontWeight = FontWeight.Bold)
      }
    }

    Spacer(modifier = Modifier.height(14.dp))

    // Interactive Timeline Tracks Component
    Text(
      "MULTI-TRACK SYNC TIMELINE",
      fontSize = 11.sp,
      fontWeight = FontWeight.Bold,
      color = MutedGrey,
      letterSpacing = 1.sp
    )

    Spacer(modifier = Modifier.height(4.dp))

    Column(
      modifier = Modifier
        .fillMaxWidth()
        .clip(RoundedCornerShape(16.dp))
        .background(CyberBackgroundCard)
        .border(1.dp, GlassOutline, RoundedCornerShape(16.dp))
        .padding(12.dp)
    ) {
      // Stream Track 1 Video
      TimelineTrackItem(title = "Video Clip Track", accentColor = NeonCyan, progressVal = 0.62f)
      Divider(color = GlassOutline, modifier = Modifier.padding(vertical = 8.dp))
      // Stream Track 2 Audio Waveform
      TimelineTrackItem(title = "Synthesizer Loop", accentColor = NeonPurple, progressVal = 0.85f, isWaveform = true)
      Divider(color = GlassOutline, modifier = Modifier.padding(vertical = 8.dp))
      // Stream Track 3 Subtitles
      TimelineTrackItem(title = "Ai Caption Stream", accentColor = NeonPink, progressVal = 0.45f)
    }

    Spacer(modifier = Modifier.height(14.dp))

    // Publishing Settings Box Forms
    GlassmorphicCard(
      modifier = Modifier.fillMaxWidth(),
      cornerRadius = 16.dp
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(16.dp)
      ) {
        Text("METADATA UPLINK", color = NeonCyan, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
        Spacer(modifier = Modifier.height(10.dp))

        OutlinedTextField(
          value = videoTitleText,
          onValueChange = { videoTitleText = it },
          label = { Text("Uplink Synopsis Details") },
          colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = NeonCyan,
            unfocusedBorderColor = GlassOutline,
            focusedTextColor = PureWhite,
            unfocusedTextColor = PureWhite
          ),
          modifier = Modifier
            .fillMaxWidth()
            .testTag("editor_title_input"),
          singleLine = true
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
          value = hashtagsText,
          onValueChange = { hashtagsText = it },
          label = { Text("Cyber Link Hashtags") },
          colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = NeonPurple,
            unfocusedBorderColor = GlassOutline,
            focusedTextColor = PureWhite,
            unfocusedTextColor = PureWhite
          ),
          modifier = Modifier
            .fillMaxWidth()
            .testTag("editor_hashtags_input"),
          singleLine = true,
          keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
          keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() })
        )
      }
    }
  }
}

@Composable
fun TimelineTrackItem(
  title: String,
  accentColor: Color,
  progressVal: Float,
  isWaveform: Boolean = false
) {
  Row(
    modifier = Modifier.fillMaxWidth(),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.SpaceBetween
  ) {
    Column(modifier = Modifier.weight(0.35f)) {
      Text(text = title, fontSize = 11.sp, color = PureWhite, fontWeight = FontWeight.Bold)
      Text(text = "Track Output", fontSize = 9.sp, color = MutedGrey)
    }

    Box(
      modifier = Modifier
        .weight(0.65f)
        .height(34.dp)
        .clip(RoundedCornerShape(8.dp))
        .background(ObsidianBackground)
        .padding(horizontal = 10.dp, vertical = 4.dp),
      contentAlignment = Alignment.CenterStart
    ) {
      if (isWaveform) {
        // Draw cute synthetic pulse shapes
        Row(
          modifier = Modifier.fillMaxSize(),
          horizontalArrangement = Arrangement.spacedBy(3.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          val randomHeights = listOf(14, 25, 12, 10, 22, 16, 28, 12, 8, 20, 26, 14, 11, 24, 18)
          randomHeights.forEachIndexed { idx, ht ->
            val color = if (idx < (randomHeights.size * progressVal)) accentColor else MutedGrey.copy(alpha = 0.3f)
            Box(
              modifier = Modifier
                .width(4.dp)
                .height(ht.dp)
                .clip(CircleShape)
                .background(color)
            )
          }
        }
      } else {
        // Simple timeline progress bar filler block
        LinearProgressIndicator(
          progress = { progressVal },
          modifier = Modifier
            .fillMaxWidth()
            .height(8.dp)
            .clip(CircleShape),
          color = accentColor,
          trackColor = GlassOutline.copy(alpha = 0.2f)
        )
      }
    }
  }
}

// ==========================================
// 8. MESSAGING SCREEN
// ==========================================

@Composable
fun MessagingScreen(viewModel: NaArgaViewModel) {
  val activeContact by viewModel.selectedCreatorForChat.collectAsStateWithLifecycle()
  val chatsHistory by viewModel.chatHistories.collectAsStateWithLifecycle()
  val activeChat = chatsHistory[activeContact] ?: emptyList()

  var inputTextMsg by remember { mutableStateOf("") }
  val focusManager = LocalFocusManager.current

  Column(
    modifier = Modifier
      .fillMaxSize()
      .background(ObsidianBackground)
      .statusBarsPadding()
      .padding(horizontal = 16.dp)
  ) {
    // Header contact switcher row
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(vertical = 12.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
          modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(NeonPurple),
          contentAlignment = Alignment.Center
        ) {
          Text(activeContact.take(2).replace("@", "").uppercase(), fontWeight = FontWeight.Bold, color = PureWhite)
        }
        Spacer(modifier = Modifier.width(10.dp))
        Column {
          Text(text = activeContact, color = PureWhite, fontWeight = FontWeight.Bold, fontSize = 14.sp)
          Text(text = "Active encrypted node link", color = CustomGradientLines, fontSize = 10.sp)
        }
      }

      // Quick trigger to notifications
      IconButton(onClick = { viewModel.setScreen("notifications") }) {
        Icon(imageVector = Icons.Default.CircleNotifications, contentDescription = "Alerts Hub", tint = NeonCyan)
      }
    }

    // Horizontally scrollable other creators online indicator row
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .horizontalScroll(rememberScrollState())
        .padding(vertical = 8.dp),
      horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
      listOf("@Vesper_X", "@Aria_Vibe", "@Chronos_Lab").forEach { tag ->
        val isActive = tag == activeContact
        Box(
          modifier = Modifier
            .clip(RoundedCornerShape(24.dp))
            .background(if (isActive) NeonPurple.copy(alpha = 0.3f) else GlassBg)
            .border(1.dp, if (isActive) NeonPurple else GlassOutline, RoundedCornerShape(24.dp))
            .clickable { viewModel.selectChatCreator(tag) }
            .padding(horizontal = 12.dp, vertical = 6.dp)
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(if (isActive) NeonCyan else MutedGrey))
            Spacer(modifier = Modifier.width(6.dp))
            Text(text = tag, color = PureWhite, fontSize = 11.sp, fontWeight = FontWeight.Bold)
          }
        }
      }
    }

    Divider(color = GlassOutline, modifier = Modifier.padding(vertical = 4.dp))

    // Messages history feed lists
    LazyColumn(
      modifier = Modifier
        .weight(1f)
        .fillMaxWidth()
        .padding(vertical = 8.dp),
      verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
      items(activeChat) { msg ->
        val isMe = msg.senderId == "me"
        Column(
          modifier = Modifier.fillMaxWidth(),
          horizontalAlignment = if (isMe) Alignment.End else Alignment.Start
        ) {
          Row(
            modifier = Modifier.fillMaxWidth(0.85f),
            horizontalArrangement = if (isMe) Arrangement.End else Arrangement.Start,
            verticalAlignment = Alignment.Bottom
          ) {
            if (!isMe) {
              Box(
                modifier = Modifier
                  .padding(end = 8.dp)
                  .size(24.dp)
                  .clip(CircleShape)
                  .background(GlassOutline),
                contentAlignment = Alignment.Center
              ) {
                Text(activeContact.take(2).replace("@", "").uppercase(), color = PureWhite, fontSize = 8.sp, fontWeight = FontWeight.Bold)
              }
            }

            // Message Bubble Card
            Box(
              modifier = Modifier
                .clip(
                  RoundedCornerShape(
                    topStart = 16.dp,
                    topEnd = 16.dp,
                    bottomStart = if (isMe) 16.dp else 4.dp,
                    bottomEnd = if (isMe) 4.dp else 16.dp
                  )
                )
                .background(if (isMe) NeonPurple.copy(alpha = 0.85f) else CyberBackgroundCard)
                .border(
                  0.5.dp,
                  if (isMe) NeonPurple else GlassOutline,
                  RoundedCornerShape(
                    topStart = 16.dp,
                    topEnd = 16.dp,
                    bottomStart = if (isMe) 16.dp else 4.dp,
                    bottomEnd = if (isMe) 4.dp else 16.dp
                  )
                )
                .padding(12.dp)
            ) {
              Column {
                if (msg.isVoiceMessage) {
                  // Custom voice message waveform layout representation
                  Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Mic, contentDescription = "Voice", tint = NeonCyan, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(imageVector = Icons.Default.GraphicEq, contentDescription = null, tint = PureWhite, modifier = Modifier.size(48.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = msg.voiceDuration, color = PureWhite, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                  }
                } else {
                  Text(text = msg.text, color = PureWhite, fontSize = 13.sp)
                }
              }
            }
          }
          Spacer(modifier = Modifier.height(2.dp))
          Text(
            text = msg.timestamp,
            fontSize = 9.sp,
            color = MutedGrey,
            modifier = Modifier.padding(horizontal = if (isMe) 4.dp else 36.dp)
          )
        }
      }
    }

    // Send chat box input Row
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .navigationBarsPadding()
        .padding(vertical = 10.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      OutlinedTextField(
        value = inputTextMsg,
        onValueChange = { inputTextMsg = it },
        placeholder = { Text("Encrypted transmission...", color = MutedGrey) },
        colors = OutlinedTextFieldDefaults.colors(
          focusedTextColor = PureWhite,
          unfocusedTextColor = PureWhite,
          focusedBorderColor = NeonCyan,
          unfocusedBorderColor = GlassOutline
        ),
        modifier = Modifier
          .weight(1f)
          .testTag("chat_input_text"),
        singleLine = true,
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
        keyboardActions = KeyboardActions(onSend = {
          viewModel.sendChatMessage(inputTextMsg)
          inputTextMsg = ""
          focusManager.clearFocus()
        })
      )

      Spacer(modifier = Modifier.width(10.dp))

      IconButton(
        onClick = {
          viewModel.sendChatMessage(inputTextMsg)
          inputTextMsg = ""
          focusManager.clearFocus()
        },
        modifier = Modifier
          .clip(CircleShape)
          .background(NeonCyan)
          .testTag("chat_send_btn")
      ) {
        Icon(imageVector = Icons.Default.Send, contentDescription = "Send Message", tint = ObsidianBackground)
      }
    }
  }
}

// ==========================================
// 9. NOTIFICATIONS SCREEN
// ==========================================

@Composable
fun NotificationsScreen(viewModel: NaArgaViewModel) {
  val listAlerts by viewModel.notifications.collectAsStateWithLifecycle()
  val activeFilter by viewModel.notificationFilter.collectAsStateWithLifecycle()

  val filterList = listOf("All", "Likes", "Comments", "Follows")

  Column(
    modifier = Modifier
      .fillMaxSize()
      .background(ObsidianBackground)
      .statusBarsPadding()
      .padding(horizontal = 16.dp)
  ) {
    // Header
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(vertical = 12.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Row(verticalAlignment = Alignment.CenterVertically) {
        IconButton(onClick = { viewModel.setScreen("home_feed") }) {
          Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back", tint = PureWhite)
        }
        Text("UPLINK NOTIFICATIONS", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = NeonPink, letterSpacing = 2.sp)
      }

      Box(
        modifier = Modifier
          .clip(RoundedCornerShape(4.dp))
          .background(NeonPink.copy(alpha = 0.2f))
          .padding(horizontal = 6.dp, vertical = 2.dp)
      ) {
        Text("Real-time", color = NeonPink, fontSize = 9.sp, fontWeight = FontWeight.SemiBold)
      }
    }

    // Filter Chips Row
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(vertical = 8.dp),
      horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
      filterList.forEach { flt ->
        val isSelected = flt == activeFilter
        Box(
          modifier = Modifier
            .weight(1f)
            .clip(RoundedCornerShape(12.dp))
            .background(if (isSelected) NeonPink.copy(alpha = 0.3f) else GlassBg)
            .border(1.dp, if (isSelected) NeonPink else GlassOutline, RoundedCornerShape(12.dp))
            .clickable { viewModel.setNotificationFilter(flt) }
            .padding(vertical = 10.dp),
          contentAlignment = Alignment.Center
        ) {
          Text(text = flt, color = PureWhite, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        }
      }
    }

    Spacer(modifier = Modifier.height(10.dp))

    // Scrollable feed alerts
    val displayAlerts = if (activeFilter == "All") {
      listAlerts
    } else {
      listAlerts.filter {
        when (activeFilter) {
          "Likes" -> it.type == NotificationType.LIKE
          "Comments" -> it.type == NotificationType.COMMENT
          "Follows" -> it.type == NotificationType.FOLLOW
          else -> true
        }
      }
    }

    LazyColumn(
      modifier = Modifier.weight(1f).fillMaxWidth(),
      verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      if (displayAlerts.isEmpty()) {
        item {
          Box(modifier = Modifier.fillMaxWidth().padding(40.dp), contentAlignment = Alignment.Center) {
            Text("No alerts matches this filter criteria.", color = MutedGrey, fontSize = 13.sp)
          }
        }
      } else {
        items(displayAlerts) { alert ->
          val indicatorColor = when (alert.type) {
            NotificationType.LIKE -> NeonPink
            NotificationType.COMMENT -> NeonCyan
            NotificationType.FOLLOW -> CyberGreen
            NotificationType.MENTION -> NeonPurple
          }

          GlassmorphicCard(
            modifier = Modifier.fillMaxWidth(),
            cornerRadius = 14.dp
          ) {
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              // Avatar Node representation
              Box(
                modifier = Modifier
                  .size(42.dp)
                  .clip(CircleShape)
                  .background(Brush.linearGradient(listOf(indicatorColor, ObsidianBackground)))
                  .padding(1.5.dp)
                  .clip(CircleShape)
                  .background(CyberBackgroundCard),
                contentAlignment = Alignment.Center
              ) {
                Text(text = alert.userAvatarLetter, color = PureWhite, fontWeight = FontWeight.Black)
              }

              Spacer(modifier = Modifier.width(12.dp))

              Column(modifier = Modifier.weight(1f)) {
                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.SpaceBetween,
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Text(text = alert.userHandle, fontSize = 12.sp, color = NeonCyan, fontWeight = FontWeight.Bold)
                  Text(text = alert.timeAgo, fontSize = 9.sp, color = MutedGrey)
                }

                Spacer(modifier = Modifier.height(2.dp))

                Text(text = alert.actionText, fontSize = 12.sp, color = PureWhite)

                if (alert.commentText != null) {
                  Box(
                    modifier = Modifier
                      .padding(top = 6.dp)
                      .fillMaxWidth()
                      .clip(RoundedCornerShape(6.dp))
                      .background(ObsidianBackground)
                      .padding(8.dp)
                  ) {
                    Text(text = alert.commentText, color = MutedGrey, fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                  }
                }
              }

              Spacer(modifier = Modifier.width(10.dp))

              // Read indicator dot
              if (!alert.isRead) {
                Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(indicatorColor))
              }
            }
          }
        }
      }
    }
  }
}

// ==========================================
// 10. PROFILE SCREEN
// ==========================================

@Composable
fun ProfileScreen(viewModel: NaArgaViewModel) {
  val posts by viewModel.profileVideos.collectAsStateWithLifecycle()
  val originalSize = posts.size

  Column(
    modifier = Modifier
      .fillMaxSize()
      .background(ObsidianBackground)
      .statusBarsPadding()
      .padding(horizontal = 16.dp)
  ) {
    // Header Profile Switchers
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(vertical = 12.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Text("CREATOR PROFILE", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = PureWhite)
        Spacer(modifier = Modifier.width(6.dp))
        Box(
          modifier = Modifier
            .clip(CircleShape)
            .background(NeonCyan)
            .size(8.dp)
        )
      }

      Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        IconButton(
          onClick = { viewModel.setScreen("creator_dashboard") },
          modifier = Modifier
            .size(36.dp)
            .clip(CircleShape)
            .background(GlassBg)
            .border(0.5.dp, GlassOutline, CircleShape)
            .testTag("dashboard_btn")
        ) {
          Icon(imageVector = Icons.Default.BarChart, contentDescription = "Dashboard metrics", tint = NeonCyan)
        }

        IconButton(
          onClick = { viewModel.setScreen("settings") },
          modifier = Modifier
            .size(36.dp)
            .clip(CircleShape)
            .background(GlassBg)
            .border(0.5.dp, GlassOutline, CircleShape)
            .testTag("settings_btn")
        ) {
          Icon(imageVector = Icons.Default.Settings, contentDescription = "Settings", tint = PureWhite)
        }
      }
    }

    Spacer(modifier = Modifier.height(10.dp))

    // Mini Premium Hero Bio Box
    GlassmorphicCard(
      modifier = Modifier.fillMaxWidth(),
      cornerRadius = 24.dp,
      borderWidth = 1.dp,
      glowColor = NeonPurple.copy(alpha = 0.4f),
      glowRadius = 12.dp
    ) {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(18.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        // Hologram user avatar
        Box(
          modifier = Modifier
            .size(72.dp)
            .clip(CircleShape)
            .background(Brush.sweepGradient(listOf(NeonCyan, NeonPurple, NeonPink, NeonCyan)))
            .padding(3.dp)
            .clip(CircleShape)
            .background(ObsidianBackground),
          contentAlignment = Alignment.Center
        ) {
          Box(
            modifier = Modifier
              .size(58.dp)
              .clip(CircleShape)
              .background(Color(0xFF0F121F)),
            contentAlignment = Alignment.Center
          ) {
            Text("CP", color = PureWhite, fontWeight = FontWeight.Bold, fontSize = 20.sp)
          }
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column {
          Text("Cyber Pioneer", fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = PureWhite)
          Text("@cyber_pioneer", fontSize = 13.sp, color = NeonCyan)
          Spacer(modifier = Modifier.height(6.dp))
          Text(
            "Visual synthesizer. Engineering the 2026 Android matrix showcase with pure high-fidelity Compose.",
            fontSize = 11.sp,
            color = MutedGrey,
            lineHeight = 15.sp
          )
        }
      }
    }

    Spacer(modifier = Modifier.height(16.dp))

    // Profile Numerical Indicators (Following, Followers, Likes)
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
      ProfileMetricBox(modifier = Modifier.weight(1f), label = "Subscribes", count = "14.2K", color = NeonCyan)
      ProfileMetricBox(modifier = Modifier.weight(1f), label = "Sectors", count = "340", color = NeonPurple)
      ProfileMetricBox(modifier = Modifier.weight(1f), label = "Uplink Likes", count = "128.9K", color = NeonPink)
    }

    Spacer(modifier = Modifier.height(20.dp))

    // Profile Video Grid header
    Row(
      modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(imageVector = Icons.Default.GridOn, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(16.dp))
        Spacer(modifier = Modifier.width(6.dp))
        Text("PUBLISHED MATRIX STREAMS", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = PureWhite, letterSpacing = 1.sp)
      }

      Text("$originalSize items", color = MutedGrey, fontSize = 11.sp)
    }

    // Grid Column scrollable view
    LazyVerticalGrid(
      columns = GridCells.Fixed(3),
      horizontalArrangement = Arrangement.spacedBy(8.dp),
      verticalArrangement = Arrangement.spacedBy(8.dp),
      modifier = Modifier.weight(1f)
    ) {
      items(posts) { post ->
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .height(130.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Brush.radialGradient(post.videoGradient))
            .clickable {
              // open detail view
              viewModel.setScreen("video_detail")
            }
        ) {
          Box(
            modifier = Modifier
              .fillMaxSize()
              .background(Color(0x3B000000))
          )

          Column(
            modifier = Modifier
              .fillMaxSize()
              .padding(8.dp),
            verticalArrangement = Arrangement.SpaceBetween
          ) {
            Box(
              modifier = Modifier
                .clip(RoundedCornerShape(4.dp))
                .background(Color(0x61000000))
                .padding(horizontal = 4.dp, vertical = 2.dp)
            ) {
              Text(text = post.viewCount, color = PureWhite, fontSize = 8.sp, fontWeight = FontWeight.Bold)
            }

            Text(
              text = post.videoTitle,
              color = PureWhite,
              fontSize = 9.sp,
              maxLines = 2,
              overflow = TextOverflow.Ellipsis
            )
          }
        }
      }
    }
  }
}

@Composable
fun ProfileMetricBox(
  modifier: Modifier = Modifier,
  label: String,
  count: String,
  color: Color
) {
  GlassmorphicCard(
    modifier = modifier.height(68.dp),
    cornerRadius = 14.dp
  ) {
    Column(
      modifier = Modifier.fillMaxSize(),
      verticalArrangement = Arrangement.Center,
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      Text(text = count, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = color)
      Text(text = label, fontSize = 10.sp, color = MutedGrey)
    }
  }
}

// ==========================================
// 11. CREATOR DASHBOARD
// ==========================================

@Composable
fun CreatorDashboardScreen(viewModel: NaArgaViewModel) {
  val stats by viewModel.dashboardMetrics.collectAsStateWithLifecycle()

  Column(
    modifier = Modifier
      .fillMaxSize()
      .background(ObsidianBackground)
      .statusBarsPadding()
      .padding(horizontal = 16.dp)
  ) {
    // Header
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(vertical = 12.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Row(verticalAlignment = Alignment.CenterVertically) {
        IconButton(onClick = { viewModel.setScreen("profile") }) {
          Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back", tint = PureWhite)
        }
        Text("ANALYTICS REALM", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = NeonPurple, letterSpacing = 2.sp)
      }

      Box(
        modifier = Modifier
          .clip(RoundedCornerShape(6.dp))
          .background(NeonPurple.copy(alpha = 0.2f))
          .padding(horizontal = 8.dp, vertical = 3.dp)
      ) {
        Text("SYSTEM INHERITANCE", color = NeonCyan, fontSize = 9.sp, fontWeight = FontWeight.Bold)
      }
    }

    Spacer(modifier = Modifier.height(10.dp))

    // Scrollable layout containing charts
    LazyColumn(
      modifier = Modifier.weight(1f).fillMaxWidth(),
      verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
      item {
        Text(
          "CREATOR GROWTH INDICES",
          fontSize = 11.sp,
          fontWeight = FontWeight.Bold,
          color = MutedGrey,
          letterSpacing = 1.5.sp
        )
      }

      // Display key stats horizontal modules
      items(stats) { metric ->
        GlassmorphicCard(
          modifier = Modifier.fillMaxWidth(),
          cornerRadius = 18.dp
        ) {
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .padding(16.dp)
          ) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Column {
                Text(text = metric.title, color = MutedGrey, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(2.dp))
                Text(text = metric.value, color = PureWhite, fontSize = 24.sp, fontWeight = FontWeight.Black)
              }

              Box(
                modifier = Modifier
                  .clip(RoundedCornerShape(6.dp))
                  .background(metric.accentColor.copy(alpha = 0.15f))
                  .padding(horizontal = 8.dp, vertical = 4.dp)
              ) {
                Text(text = metric.changePercent, color = metric.accentColor, fontSize = 11.sp, fontWeight = FontWeight.Bold)
              }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Dynamic mini linear chart graph representation via Canvas
            Canvas(
              modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
            ) {
              val points = metric.graphPoints
              if (points.isNotEmpty()) {
                val stepX = size.width / (points.size - 1)
                val maxVal = points.maxOrNull() ?: 1f
                val minVal = points.minOrNull() ?: 0f
                val dY = if (maxVal == minVal) 1f else maxVal - minVal

                val path = androidx.compose.ui.graphics.Path().apply {
                  points.forEachIndexed { index, value ->
                    val x = index * stepX
                    val y = size.height - ((value - minVal) / dY * size.height)
                    if (index == 0) moveTo(x, y) else lineTo(x, y)
                  }
                }

                drawPath(
                  path = path,
                  color = metric.accentColor,
                  style = Stroke(width = 4f, cap = StrokeCap.Round)
                )

                // Fill shade gradient
                val fillPath = androidx.compose.ui.graphics.Path().apply {
                  addPath(path)
                  lineTo(size.width, size.height)
                  lineTo(0f, size.height)
                  close()
                }
                drawPath(
                  path = fillPath,
                  brush = Brush.verticalGradient(
                    colors = listOf(metric.accentColor.copy(alpha = 0.35f), Color.Transparent)
                  )
                )
              }
            }
          }
        }
      }
    }
  }
}

// ==========================================
// 12. SETTINGS SCREEN
// ==========================================

@Composable
fun SettingsScreen(viewModel: NaArgaViewModel) {
  val isGlowEnabled by viewModel.isUiNeonGlowEnabled.collectAsStateWithLifecycle()
  val isPerformanceMode by viewModel.performanceMode.collectAsStateWithLifecycle()

  Column(
    modifier = Modifier
      .fillMaxSize()
      .background(ObsidianBackground)
      .statusBarsPadding()
      .padding(horizontal = 16.dp)
  ) {
    // Header Setting Panel
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(vertical = 12.dp),
      horizontalArrangement = Arrangement.Start,
      verticalAlignment = Alignment.CenterVertically
    ) {
      IconButton(onClick = { viewModel.setScreen("profile") }) {
        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back", tint = PureWhite)
      }
      Text("CORE MATRIX CONTROLS", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = PureWhite, letterSpacing = 2.sp)
    }

    Spacer(modifier = Modifier.height(14.dp))

    Text(
      "INTERFACE SHADER CONTROL",
      fontSize = 11.sp,
      fontWeight = FontWeight.Bold,
      color = MutedGrey,
      letterSpacing = 1.5.sp
    )

    Spacer(modifier = Modifier.height(10.dp))

    // Advanced Option items
    GlassmorphicCard(
      modifier = Modifier.fillMaxWidth(),
      cornerRadius = 18.dp
    ) {
      Column(modifier = Modifier.padding(16.dp)) {
        Row(
          modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Column {
            Text("Cyber Neon Glow", color = PureWhite, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            Text("Enables soft drop-shadow glowing pipelines in UI", tint = MutedGrey, fontSize = 11.sp)
          }
          Switch(
            checked = isGlowEnabled,
            onCheckedChange = { viewModel.isUiNeonGlowEnabled.value = it },
            colors = SwitchDefaults.colors(checkedThumbColor = NeonCyan, checkedTrackColor = NeonPurple.copy(alpha = 0.5f))
          )
        }

        Divider(color = GlassOutline)

        Row(
          modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Column {
            Text("Telemetry Overclocking", color = PureWhite, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            Text("Bypasses rendering limits, saving client power", tint = MutedGrey, fontSize = 11.sp)
          }
          Switch(
            checked = isPerformanceMode,
            onCheckedChange = { viewModel.performanceMode.value = it },
            colors = SwitchDefaults.colors(checkedThumbColor = NeonCyan, checkedTrackColor = NeonPurple.copy(alpha = 0.5f))
          )
        }
      }
    }

    Spacer(modifier = Modifier.height(24.dp))

    Text(
      "ACCOUNT SYNAPSE PROFILE",
      fontSize = 11.sp,
      fontWeight = FontWeight.Bold,
      color = MutedGrey,
      letterSpacing = 1.5.sp
    )

    Spacer(modifier = Modifier.height(10.dp))

    // Basic controls list
    GlassmorphicCard(
      modifier = Modifier.fillMaxWidth(),
      cornerRadius = 18.dp
    ) {
      Column(modifier = Modifier.padding(16.dp)) {
        SettingsRowItem(label = "Holographic Languages", valStr = "English (Holo)")
        Divider(color = GlassOutline)
        SettingsRowItem(label = "Quantum Ledger Auth", valStr = "com.naarga.identity")
        Divider(color = GlassOutline)
        SettingsRowItem(label = "Neuro Encryption", valStr = "AES-512 ACTIVE")
      }
    }

    Spacer(modifier = Modifier.weight(1f))

    // App Credits card
    Card(
      colors = CardDefaults.cardColors(containerColor = CyberBackgroundCard),
      shape = RoundedCornerShape(16.dp),
      modifier = Modifier
        .fillMaxWidth()
        .navigationBarsPadding()
        .padding(bottom = 24.dp)
    ) {
      Column(
        modifier = Modifier.padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        Text("Na Arga Social Platform", color = PureWhite, fontWeight = FontWeight.Bold, fontSize = 13.sp)
        Text("System Version 2026.11.4 - Secure Decentralized Build", color = NeonCyan, fontSize = 11.sp)
        Spacer(modifier = Modifier.height(4.dp))
        Text("Designed for Ultra Premium UI/UX Presentation Experience.", color = MutedGrey, fontSize = 10.sp, textAlign = TextAlign.Center)
      }
    }
  }
}

@Composable
fun SettingsRowItem(label: String, valStr: String) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .padding(vertical = 12.dp),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically
  ) {
    Text(text = label, color = PureWhite, fontSize = 13.sp, fontWeight = FontWeight.Bold)
    Text(text = valStr, color = NeonCyan, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
  }
}

// Wrapper to fix compile warning on custom text color modifier overrides
@Composable
private fun Text(text: String, color: Color, fontSize: androidx.compose.ui.unit.TextUnit, fontWeight: FontWeight? = null, letterSpacing: androidx.compose.ui.unit.TextUnit = androidx.compose.ui.unit.TextUnit.Unspecified, textAlign: TextAlign? = null, modifier: Modifier = Modifier, maxLines: Int = Int.MAX_VALUE, overflow: TextOverflow = TextOverflow.Clip, lineHeight: androidx.compose.ui.unit.TextUnit = androidx.compose.ui.unit.TextUnit.Unspecified) {
  androidx.compose.material3.Text(text = text, color = color, fontSize = fontSize, fontWeight = fontWeight, letterSpacing = letterSpacing, textAlign = textAlign, modifier = modifier, maxLines = maxLines, overflow = overflow, lineHeight = lineHeight, fontFamily = FontFamily.SansSerif)
}

// Helper to wrap typography tinting
@Composable
private fun Text(text: String, tint: Color, fontSize: androidx.compose.ui.unit.TextUnit) {
  androidx.compose.material3.Text(text = text, color = tint, fontSize = fontSize, fontFamily = FontFamily.SansSerif)
}

// ==========================================
// 12-SCREEN SHOWCASE NAVIGATOR COMPOSABLES
// ==========================================

@Composable
fun FloatingShowcaseTriggerButton(
  isOpen: Boolean,
  onClick: () -> Unit
) {
  IconButton(
    onClick = onClick,
    modifier = Modifier
      .size(52.dp)
      .clip(CircleShape)
      .background(Brush.linearGradient(listOf(NeonCyan, NeonPink)))
      .border(1.5.dp, PureWhite, CircleShape)
      .testTag("showcase_dial_trigger")
  ) {
    Icon(
      imageVector = if (isOpen) Icons.Default.Close else Icons.Default.DashboardCustomize,
      contentDescription = "Showcase selector dials",
      tint = ObsidianBackground,
      modifier = Modifier.size(24.dp)
    )
  }
}

@Composable
fun ShowcaseSelectorHub(
  onDismiss: () -> Unit,
  currentActiveScreen: String,
  onSelectScreen: (String) -> Unit
) {
  val listScreens = listOf(
    "onboarding" to "1. Onboarding Screen",
    "login" to "2. Login Screen",
    "home_feed" to "3. Home Feed",
    "explore" to "4. Explore Screen",
    "video_detail" to "5. Video Detail",
    "create" to "6. Create Screen",
    "video_editor" to "7. Video Editor",
    "messaging" to "8. Messaging Chat",
    "notifications" to "9. Notifications Feed",
    "profile" to "10. Profile Screen",
    "creator_dashboard" to "11. Creator Dashboard",
    "settings" to "12. Settings Panel"
  )

  Box(
    modifier = Modifier
      .fillMaxSize()
      .background(Color(0xE0040509))
      .clickable { onDismiss() }
  ) {
    Surface(
      modifier = Modifier
        .fillMaxWidth()
        .fillMaxHeight(0.72f)
        .align(Alignment.BottomCenter)
        .clickable(enabled = false, onClick = {}),
      shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
      color = Color(0xFF0F1221),
      tonalElevation = 16.dp
    ) {
      Column(
        modifier = Modifier
          .fillMaxSize()
          .border(
            BorderStroke(1.5.dp, Brush.horizontalGradient(listOf(NeonCyan, NeonPurple, NeonPink))),
            RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
          )
          .padding(24.dp)
      ) {
        Box(
          modifier = Modifier
            .size(45.dp, 4.dp)
            .clip(RoundedCornerShape(2.dp))
            .background(MutedGrey.copy(alpha = 0.5f))
            .align(Alignment.CenterHorizontally)
        )

        Spacer(modifier = Modifier.height(14.dp))

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Column {
            Text("NA ARGA DESIGN MATRIX", fontSize = 16.sp, color = NeonCyan, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
            Text("Select any of the 12 requested screens to view its UI mockup instantly.", color = MutedGrey, fontSize = 11.sp)
          }
          IconButton(onClick = onDismiss) {
            Icon(imageVector = Icons.Default.Close, contentDescription = null, tint = PureWhite)
          }
        }

        Spacer(modifier = Modifier.height(14.dp))

        LazyColumn(
          modifier = Modifier.weight(1f).fillMaxWidth(),
          verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          items(listScreens) { (route, label) ->
            val isActive = currentActiveScreen == route
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(if (isActive) GlassBg else ObsidianBackground)
                .border(1.dp, if (isActive) NeonCyan else GlassOutline, RoundedCornerShape(12.dp))
                .clickable { onSelectScreen(route) }
                .padding(14.dp),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text(
                text = label,
                fontSize = 13.sp,
                color = if (isActive) NeonCyan else PureWhite,
                fontWeight = FontWeight.Bold
              )
              if (isActive) {
                Box(
                  modifier = Modifier
                    .clip(CircleShape)
                    .background(NeonCyan)
                    .size(8.dp)
                )
              }
            }
          }
        }
      }
    }
  }
}

// Styling Constants helper declarations
val CustomGradientLines = Color(0xFF00FFE0)
