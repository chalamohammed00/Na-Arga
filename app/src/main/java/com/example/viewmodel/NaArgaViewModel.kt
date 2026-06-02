package com.example.viewmodel

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import com.example.model.*
import com.example.ui.theme.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class NaArgaViewModel : ViewModel() {

  // Current selected screen route for deep presentation switching or top hub selector
  private val _currentScreen = MutableStateFlow("onboarding")
  val currentScreen: StateFlow<String> = _currentScreen.asStateFlow()

  // Track if user is authenticated
  private val _isAuthenticated = MutableStateFlow(false)
  val isAuthenticated: StateFlow<Boolean> = _isAuthenticated.asStateFlow()

  // Video feed posts list
  private val _videoPosts = MutableStateFlow<List<VideoPost>>(emptyList())
  val videoPosts: StateFlow<List<VideoPost>> = _videoPosts.asStateFlow()

  // Current active index in Home Feed
  private val _activeFeedIndex = MutableStateFlow(0)
  val activeFeedIndex: StateFlow<Int> = _activeFeedIndex.asStateFlow()

  // Direct chat history mapped by contact handle
  private val _selectedCreatorForChat = MutableStateFlow("@Vesper_X")
  val selectedCreatorForChat: StateFlow<String> = _selectedCreatorForChat.asStateFlow()

  private val _chatHistories = MutableStateFlow<Map<String, List<ChatMessage>>>(emptyMap())
  val chatHistories: StateFlow<Map<String, List<ChatMessage>>> = _chatHistories.asStateFlow()

  // Custom comments stored per Video ID
  private val _videoComments = MutableStateFlow<Map<String, List<Pair<String, String>>>>(emptyMap())
  val videoComments: StateFlow<Map<String, List<Pair<String, String>>>> = _videoComments.asStateFlow()

  // Notification Feed list
  private val _notifications = MutableStateFlow<List<NotificationItem>>(emptyList())
  val notifications: StateFlow<List<NotificationItem>> = _notifications.asStateFlow()

  // Selected filter on notifications
  private val _notificationFilter = MutableStateFlow("All")
  val notificationFilter: StateFlow<String> = _notificationFilter.asStateFlow()

  // Dashboard Stats & charts elements
  private val _dashboardMetrics = MutableStateFlow<List<DashboardMetric>>(emptyList())
  val dashboardMetrics: StateFlow<List<DashboardMetric>> = _dashboardMetrics.asStateFlow()

  // Profile data
  private val _profileVideos = MutableStateFlow<List<VideoPost>>(emptyList())
  val profileVideos: StateFlow<List<VideoPost>> = _profileVideos.asStateFlow()

  // Simulated recording/created video info
  private val _recordingDurationSeconds = MutableStateFlow(0)
  val recordingDurationSeconds: StateFlow<Int> = _recordingDurationSeconds.asStateFlow()
  
  private val _isRecording = MutableStateFlow(false)
  val isRecording: StateFlow<Boolean> = _isRecording.asStateFlow()

  // Audio/Effects status for creator studio
  val selectedMusic = MutableStateFlow("Cyber Vibe (Original Sound)")
  val selectedEffect = MutableStateFlow("Neon Glow FX")

  // Dark Node UI Settings state
  val isUiNeonGlowEnabled = MutableStateFlow(true)
  val performanceMode = MutableStateFlow(false)
  val savedCount = MutableStateFlow(24)

  init {
    loadMockData()
  }

  fun setScreen(route: String) {
    _currentScreen.value = route
  }

  fun authenticate(success: Boolean) {
    _isAuthenticated.value = success
    if (success) {
      _currentScreen.value = "home_feed"
    }
  }

  fun toggleLike(postId: String) {
    _videoPosts.update { posts ->
      posts.map { post ->
        if (post.id == postId) {
          val isLikedNow = !post.initialIsLiked
          val change = if (isLikedNow) 1 else -1
          post.copy(
            initialIsLiked = isLikedNow,
            likesCount = post.likesCount + change
          )
        } else {
          post
        }
      }
    }
    
    // Also update detail in profile if matches
    _profileVideos.update { pVideos ->
      pVideos.map { post ->
        if (post.id == postId) {
          val isLikedNow = !post.initialIsLiked
          val change = if (isLikedNow) 1 else -1
          post.copy(
            initialIsLiked = isLikedNow,
            likesCount = post.likesCount + change
          )
        } else {
          post
        }
      }
    }
  }

  fun toggleFollow(postId: String) {
    _videoPosts.update { posts ->
      posts.map { post ->
        if (post.id == postId) {
          post.copy(initialIsFollowing = !post.initialIsFollowing)
        } else {
          post
        }
      }
    }
  }

  fun nextFeedPost() {
    if (_videoPosts.value.isNotEmpty()) {
      _activeFeedIndex.update { (it + 1) % _videoPosts.value.size }
    }
  }

  fun prevFeedPost() {
    if (_videoPosts.value.isNotEmpty()) {
      _activeFeedIndex.update { (it - 1 + _videoPosts.value.size) % _videoPosts.value.size }
    }
  }

  fun addComment(postId: String, userName: String, commentText: String) {
    if (commentText.isBlank()) return
    
    _videoComments.update { currentComments ->
      val existing = currentComments[postId] ?: emptyList()
      val updated = existing + (userName to commentText)
      currentComments + (postId to updated)
    }

    // Increment comments count on corresponding VideoPost
    _videoPosts.update { posts ->
      posts.map { post ->
        if (post.id == postId) {
          post.copy(commentsCount = post.commentsCount + 1)
        } else {
          post
        }
      }
    }
  }

  fun selectChatCreator(handle: String) {
    _selectedCreatorForChat.value = handle
  }

  fun sendChatMessage(text: String) {
    if (text.isBlank()) return
    val activeHandle = _selectedCreatorForChat.value
    val newMessage = ChatMessage(
      id = "m_${System.currentTimeMillis()}",
      senderId = "me",
      text = text,
      timestamp = "Just Now"
    )

    _chatHistories.update { currentHistory ->
      val existing = currentHistory[activeHandle] ?: emptyList()
      val updated = existing + newMessage
      currentHistory + (activeHandle to updated)
    }
  }

  fun setNotificationFilter(filter: String) {
    _notificationFilter.value = filter
  }

  fun startRecording() {
    _isRecording.value = true
    _recordingDurationSeconds.value = 0
  }

  fun stopRecording() {
    _isRecording.value = false
  }

  fun simulateUploadVideo(title: String, hashTags: String) {
    val hTags = hashTags.split(" ").filter { it.startsWith("#") || it.isNotBlank() }.map { 
      if (it.startsWith("#")) it else "#$it"
    }
    
    val newPost = VideoPost(
      id = "item_${System.currentTimeMillis()}",
      creatorName = "You (Creator)",
      creatorHandle = "@cyber_pioneer",
      creatorAvatar = 0,
      videoTitle = if (title.isBlank()) "Simulated Uplink Stream" else title,
      hashtags = if (hTags.isEmpty()) listOf("#naarga", "#discover", "#space") else hTags,
      songTitle = selectedMusic.value,
      likesCount = 0,
      commentsCount = 0,
      shareCount = 0,
      initialIsLiked = false,
      initialIsFollowing = false,
      viewCount = "1",
      videoGradient = listOf(NeonPink, NeonPurple, ObsidianBackground)
    )

    // Append to my profile videos
    _profileVideos.update { listOf(newPost) + it }
    // Insert into home feed too!
    _videoPosts.update { listOf(newPost) + it }
    _activeFeedIndex.value = 0
  }

  private fun loadMockData() {
    // Elegant neon color gradients representing cinematic videos
    val grad1 = listOf(Color(0xFF04132B), Color(0xFF052D5E), NeonCyan)
    val grad2 = listOf(Color(0xFF1B033F), Color(0xFF4C0896), NeonPurple)
    val grad3 = listOf(Color(0xFF2C021E), Color(0xFF8B125B), NeonPink)
    val grad4 = listOf(Color(0xFF021B1A), Color(0xFF0E4A42), Color(0xFF00FFC2))
    val grad5 = listOf(Color(0xFF1E1501), Color(0xFF4C3005), CyberOrange)

    val posts = listOf(
      VideoPost(
        id = "feed_1",
        creatorName = "Vesper Nexus",
        creatorHandle = "@Vesper_X",
        creatorAvatar = 1,
        videoTitle = "Building the decentralized 2026 holographic interface. Glassmorphism overlays are peak visual aesthetics! 🌐🔌",
        hashtags = listOf("#cyber", "#hologram", "#design2026", "#naarga"),
        songTitle = "Holo Sync - Synthwave Remix (Audio)",
        likesCount = 82400,
        commentsCount = 1420,
        shareCount = 9422,
        initialIsLiked = true,
        initialIsFollowing = false,
        viewCount = "4.2M",
        videoGradient = grad1
      ),
      VideoPost(
        id = "feed_2",
        creatorName = "Aria Vibe Node",
        creatorHandle = "@Aria_Vibe",
        creatorAvatar = 2,
        videoTitle = "Futuristic organic dance motion synchronized in virtual engine. Cyberpunk avatar fashion runway compilation. 🩰🕯️💫",
        hashtags = listOf("#dance", "#motionDesign", "#avatar", "#cyberStyle"),
        songTitle = "Aria Vibe - Digital Resonance (Live Sound)",
        likesCount = 124100,
        commentsCount = 3840,
        shareCount = 28100,
        initialIsLiked = false,
        initialIsFollowing = true,
        viewCount = "8.9M",
        videoGradient = grad2
      ),
      VideoPost(
        id = "feed_3",
        creatorName = "Chronos Tech Lab",
        creatorHandle = "@Chronos_Lab",
        creatorAvatar = 3,
        videoTitle = "Quantum quantum core rendering preview. Overclocking neon cooling tubes to absolute zero! 🤖💀🔬",
        hashtags = listOf("#tech", "#quantum", "#cyberNode", "#hardware"),
        songTitle = "Chronos Lab - Sub-zero humming ambient",
        likesCount = 5900,
        commentsCount = 312,
        shareCount = 899,
        initialIsLiked = false,
        initialIsFollowing = false,
        viewCount = "120K",
        videoGradient = grad4
      ),
      VideoPost(
        id = "feed_4",
        creatorName = "Neon Siren",
        creatorHandle = "@Neon_Siren",
        creatorAvatar = 4,
        videoTitle = "Live coding in neo-Tokyo capsule hotel. Writing UI shaders on transparent OLED layout panels. 👩‍💻🏙️🗼",
        hashtags = listOf("#developer", "#tokyo", "#futureUI", "#glitch"),
        songTitle = "Neon Siren - Tokyo Glitch Loops",
        likesCount = 47200,
        commentsCount = 890,
        shareCount = 1530,
        initialIsLiked = true,
        initialIsFollowing = true,
        viewCount = "1.8M",
        videoGradient = grad3
      ),
      VideoPost(
        id = "feed_5",
        creatorName = "Aether Horizon",
        creatorHandle = "@Aether_HZ",
        creatorAvatar = 5,
        videoTitle = "Breathtaking volumetric sunset flight simulation over floating neo-metropolis. Unreal Engine 6 tech showcase, absolute realism. 🌅🚀☁️",
        hashtags = listOf("#ue6", "#scifi", "#hovercraft", "#renders"),
        songTitle = "Aether HZ - Stratosphere Horizon Sound",
        likesCount = 92000,
        commentsCount = 1750,
        shareCount = 7410,
        initialIsLiked = false,
        initialIsFollowing = false,
        viewCount = "3.1M",
        videoGradient = grad5
      )
    )
    _videoPosts.value = posts

    // Build chat history map
    val chats = mapOf(
      "@Vesper_X" to listOf(
        ChatMessage("m1", "@Vesper_X", "Hey pioneering cohort, saw your response in the last uplink thread!", "10:32 AM"),
        ChatMessage("m2", "me", "Absolutely, that glowing holographic glass overlay you designed is incredible!", "10:34 AM"),
        ChatMessage("m3", "@Vesper_X", "Appreciate that deep render! Doing another stream tonight. Want to collaborate on a design preview?", "10:35 AM"),
        ChatMessage("m4", "me", "Stunning. Sign me up! Let's build the interactive wireframes live.", "10:37 AM"),
        ChatMessage("m5", "@Vesper_X", "Listen to this audio cue for the entry transition, thoughts?", "10:38 AM", isVoiceMessage = true, voiceDuration = "0:12")
      ),
      "@Aria_Vibe" to listOf(
        ChatMessage("cm1", "@Aria_Vibe", "Thanks for the support on my dynamic motion runway set! 💃", "Yesterday"),
        ChatMessage("cm2", "me", "Loved the cyber-sari design, the physics rendering is smooth!", "Yesterday"),
        ChatMessage("cm3", "@Aria_Vibe", "Yes, custom clothing colliders took 12 hours of offline processing, but so worth it!", "Yesterday")
      ),
      "@Chronos_Lab" to listOf(
        ChatMessage("cl1", "@Chronos_Lab", "Uplink warning: Liquid nitrogen pressure normal. Core operating at 5.4GHz. ❄️", "2 days ago")
      )
    )
    _chatHistories.value = chats

    // Comments map
    val comments = mapOf(
      "feed_1" to listOf(
        "cyber_nomad" to "This is absolutely unreal. Love the transparent menu cards! 🔥",
        "hologram_queen" to "How do you achieve that perfect glass reflection vector?",
        "shibuya_runner" to "Na Arga feeds are loading so fast. Beautiful premium UI vibe.",
        "dexter_ai" to "2026 style is definitely glassmorphism and neon gradients!"
      ),
      "feed_2" to listOf(
        "future_mover" to "The motion timing here is incredible. Truly elegant avatar art.",
        "synth_dreamer" to "The custom lighting feels incredibly cinematic."
      )
    )
    _videoComments.value = comments

    // Notifications listing
    val alerts = listOf(
      NotificationItem("n1", NotificationType.COMMENT, "@Aria_Vibe", "A", NeonPurple, "commented on your timeline clip", "4m", isRead = false, commentText = "Incredible frame rate choice here, let's collab!"),
      NotificationItem("n2", NotificationType.LIKE, "@Vesper_X", "V", NeonCyan, "liked your newly uploaded audio mix", "28m", isRead = false),
      NotificationItem("n3", NotificationType.FOLLOW, "@CyberKev", "C", GlassOutline, "started following you. Explore their space.", "1h", isRead = true),
      NotificationItem("n4", NotificationType.MENTION, "@Chronos_Lab", "C", NeonPink, "mentioned you in a creator post", "3h", isRead = true, commentText = "Check the specs on core system with @cyber_pioneer !"),
      NotificationItem("n5", NotificationType.LIKE, "@Neon_Siren", "N", NeonCyan, "liked your volumetric render card", "1d", isRead = true),
      NotificationItem("n6", NotificationType.COMMENT, "@Atlas_Core", "A", GlassOutline, "commented on your video editor preview", "2d", isRead = true, commentText = "Subtitles render is extremely clean.")
    )
    _notifications.value = alerts

    // Creator Dashboard Metrics
    val stats = listOf(
      DashboardMetric("Video Views", "384.2K", "+18.4%", true, NeonCyan, listOf(30f, 45f, 35f, 60f, 50f, 85f, 95f)),
      DashboardMetric("Viewer Engagement", "14.2%", "+4.1%", true, NeonPurple, listOf(12f, 15f, 13f, 18f, 14f, 17f, 19f)),
      DashboardMetric("Follower Growth", "+8,400", "+22.8%", true, NeonPink, listOf(200f, 500f, 400f, 1200f, 900f, 2100f, 2900f)),
      DashboardMetric("Active Creator Revenue", "$1,480.50", "+11.2%", true, CyberGreen, listOf(150f, 300f, 240f, 570f, 890f, 1120f, 1480f))
    )
    _dashboardMetrics.value = stats

    // Profile Videos preview grid
    val myVideos = listOf(
      VideoPost(
        id = "my_1",
        creatorName = "You (Creator)",
        creatorHandle = "@cyber_pioneer",
        creatorAvatar = 0,
        videoTitle = "My first volumetric test using generative cyber particles. Glow feedback feels alive! ✨🧪💻",
        hashtags = listOf("#hologram", "#composelight", "#particles", "#dev"),
        songTitle = "Cyber Pioneer - Particles Remix",
        likesCount = 1420,
        commentsCount = 28,
        shareCount = 55,
        viewCount = "24K",
        videoGradient = listOf(Color(0xFF031A15), Color(0xFF074D3F), NeonCyan)
      ),
      VideoPost(
        id = "my_2",
        creatorName = "You (Creator)",
        creatorHandle = "@cyber_pioneer",
        creatorAvatar = 0,
        videoTitle = "Sublime dark glass synthesizer mockup with live frequency visualizers in Android Compose. 🎹🕯️🎧",
        hashtags = listOf("#synthesizer", "#compose", "#neon", "#androiddev"),
        songTitle = "Cyber Pioneer - Ambient Oscillation (Original)",
        likesCount = 820,
        commentsCount = 14,
        shareCount = 19,
        viewCount = "12K",
        videoGradient = listOf(Color(0xFF20031F), Color(0xFF5D0A5A), NeonPink)
      ),
      VideoPost(
        id = "my_3",
        creatorName = "You (Creator)",
        creatorHandle = "@cyber_pioneer",
        creatorAvatar = 0,
        videoTitle = "Floating widget concept styled using glassmorphism borders and offset radial grids. 🎛️⚡️💾",
        hashtags = listOf("#uxshowcase", "#cyberpunk", "#dribbble", "#behance"),
        songTitle = "Cyber Pioneer - Widget Sound Byte",
        likesCount = 4220,
        commentsCount = 112,
        shareCount = 108,
        viewCount = "89K",
        videoGradient = listOf(Color(0xFF011425), Color(0xFF05345D), NeonPurple)
      )
    )
    _profileVideos.value = myVideos
  }
}
