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

data class WalletTransaction(
    val id: String,
    val userId: String,
    val txType: String,
    val amount: Double,
    val direction: String,
    val title: String,
    val description: String,
    val referenceId: String?,
    val createdAt: String
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

data class Trail(
    val id: String,
    val code: String,
    val name: String,
    val area: String,
    val nextTrailId: String?
)

data class Trailhead(
    val id: String,
    val trailId: String,
    val name: String,
    val location: String,
    val lat: Double?,
    val lng: Double?
)

data class HikeSessionResult(
    val success: Boolean,
    val message: String
)

data class EventModel(
    val id: String,
    val createdBy: String,
    val title: String,
    val category: String,
    val difficulty: String,
    val description: String,
    val eventImageUrl: String?,
    val startDate: String,
    val expirationDate: String,
    val startTime: String,
    val endTime: String,
    val capacity: Int,
    val stakeAmount: Double,
    val routeStartName: String,
    val routeDestinationName: String,
    val routeStartLatitude: Double?,
    val routeStartLongitude: Double?,
    val routeDestinationLatitude: Double?,
    val routeDestinationLongitude: Double?,
    val rewardPool: Double,
    val burnAmount: Double,
    val remainingRewardPool: Double,
    val status: String
)

data class EventActionResult(
    val success: Boolean,
    val message: String
)

data class EventParticipant(
    val id: String,
    val eventId: String,
    val userId: String,
    val joined: Boolean,
    val verifiedStart: Boolean,
    val completed: Boolean,
    val failed: Boolean,
    val rewardClaimed: Boolean
)