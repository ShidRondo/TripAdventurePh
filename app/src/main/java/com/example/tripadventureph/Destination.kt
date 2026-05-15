package com.example.tripadventureph

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