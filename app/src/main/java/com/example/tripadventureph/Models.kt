package com.example.tripadventureph

data class AuthResult(
    val success: Boolean,
    val message: String,
    val accessToken: String? = null,
    val userId: String? = null,
    val email: String? = null,
    val profileComplete: Boolean = false
)

data class Destination(
    val id: String,
    val name: String,
    val category: String,
    val location: String,
    val difficulty: String,
    val rewardPoints: Int,
    val description: String,
    val imageUrl: String?
)

data class CheckInResult(
    val success: Boolean,
    val message: String
)

data class FeedPost(
    val id: String,
    val userId: String,
    val authorName: String,
    val destination: String,
    val caption: String,
    val imageUrl: String?,
    val likesCount: Int,
    val commentsCount: Int,
    val postType: String,
    val createdAt: String
)

data class FeedActionResult(
    val success: Boolean,
    val message: String
)

data class WalletSummary(
    val availableBalance: Double = 0.0,
    val lockedBalance: Double = 0.0,
    val pendingBalance: Double = 0.0
)

data class UserProfile(
    val id: String,
    val fullName: String,
    val displayName: String,
    val bio: String,
    val walletAddress: String,
    val phoneLocalNumber: String,
    val country: String,
    val region: String,
    val municipality: String,
    val barangay: String,
    val zipCode: String,
    val avatarUrl: String?,
    val isProfileComplete: Boolean
)