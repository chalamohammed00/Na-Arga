package com.example.model

import androidx.compose.ui.graphics.Color

data class VideoPost(
  val id: String,
  val creatorName: String,
  val creatorHandle: String,
  val creatorAvatar: Int, // drawable resource ID (or abstract code)
  val videoTitle: String,
  val hashtags: List<String>,
  val songTitle: String,
  val likesCount: Int,
  val commentsCount: Int,
  val shareCount: Int,
  val initialIsLiked: Boolean = false,
  val initialIsFollowing: Boolean = false,
  val viewCount: String = "1.2M",
  val videoGradient: List<Color> // Abstract background representation since there is no real video files
)

data class ChatMessage(
  val id: String,
  val senderId: String, // "me" or creatorHandle
  val text: String,
  val timestamp: String,
  val isVoiceMessage: Boolean = false,
  val voiceDuration: String = "0:00"
)

enum class NotificationType {
  LIKE, COMMENT, FOLLOW, MENTION
}

data class NotificationItem(
  val id: String,
  val type: NotificationType,
  val userHandle: String,
  val userAvatarLetter: String,
  val avatarColor: Color,
  val actionText: String,
  val timeAgo: String,
  val isRead: Boolean = false,
  val commentText: String? = null
)

data class DashboardMetric(
  val title: String,
  val value: String,
  val changePercent: String,
  val isPositive: Boolean = true,
  val accentColor: Color,
  val graphPoints: List<Float>
)
