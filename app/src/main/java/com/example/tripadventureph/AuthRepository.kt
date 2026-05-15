package com.example.tripadventureph

import android.content.Context
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.time.Instant

class AuthRepository(private val context: Context) {

    private val client = OkHttpClient()
    private val jsonType = "application/json".toMediaType()

    private val supabaseUrl = BuildConfig.SUPABASE_URL
    private val anonKey = BuildConfig.SUPABASE_ANON_KEY

    fun signUp(
        fullName: String,
        email: String,
        password: String
    ): AuthResult {
        return try {
            val bodyJson = JSONObject().apply {
                put("email", email)
                put("password", password)
                put("data", JSONObject().apply {
                    put("full_name", fullName)
                })
            }

            val request = Request.Builder()
                .url("$supabaseUrl/auth/v1/signup")
                .addHeader("apikey", anonKey)
                .addHeader("Content-Type", "application/json")
                .post(bodyJson.toString().toRequestBody(jsonType))
                .build()

            client.newCall(request).execute().use { response ->
                val responseBody = response.body?.string().orEmpty()

                if (!response.isSuccessful) {
                    val msg = try {
                        JSONObject(responseBody).optString("msg", "Signup failed")
                    } catch (_: Exception) {
                        "Signup failed"
                    }
                    return AuthResult(false, msg)
                }

                val json = JSONObject(responseBody)
                val user = json.optJSONObject("user")

                if (user == null) {
                    return AuthResult(true, "Signup successful. Check email if confirmation is enabled.")
                }

                val userId = user.optString("id")
                insertProfileStub(userId, fullName)
                insertWalletStub(userId)

                AuthResult(true, "Signup successful.")
            }
        } catch (e: Exception) {
            AuthResult(false, e.message ?: "Signup error")
        }
    }

    fun login(email: String, password: String): AuthResult {
        return try {
            val bodyJson = JSONObject().apply {
                put("email", email)
                put("password", password)
            }

            val request = Request.Builder()
                .url("$supabaseUrl/auth/v1/token?grant_type=password")
                .addHeader("apikey", anonKey)
                .addHeader("Content-Type", "application/json")
                .post(bodyJson.toString().toRequestBody(jsonType))
                .build()

            client.newCall(request).execute().use { response ->
                val responseBody = response.body?.string().orEmpty()

                if (!response.isSuccessful) {
                    val msg = try {
                        JSONObject(responseBody).optString("msg", "Login failed")
                    } catch (_: Exception) {
                        "Login failed"
                    }
                    return AuthResult(false, msg)
                }

                val json = JSONObject(responseBody)
                val accessToken = json.optString("access_token")
                val user = json.optJSONObject("user")
                val userId = user?.optString("id").orEmpty()
                val userEmail = user?.optString("email").orEmpty()

                val profileComplete = fetchProfileComplete(accessToken, userId)

                AuthResult(
                    success = true,
                    message = "Login successful.",
                    accessToken = accessToken,
                    userId = userId,
                    email = userEmail,
                    profileComplete = profileComplete
                )
            }
        } catch (e: Exception) {
            AuthResult(false, e.message ?: "Login error")
        }
    }

    fun completeProfile(
        accessToken: String,
        userId: String,
        fullName: String,
        bio: String,
        contactNumber: String,
        country: String,
        region: String,
        municipality: String,
        barangay: String,
        zipCode: String,
        walletAddress: String
    ): AuthResult {
        return try {
            val bodyJson = JSONObject().apply {
                put("full_name", fullName)
                put("display_name", fullName)
                put("bio", bio)
                put("phone_local_number", contactNumber)
                put("country", country)
                put("region", region)
                put("municipality", municipality)
                put("barangay", barangay)
                put("zip_code", zipCode)
                put("wallet_address", walletAddress)
                put("is_profile_complete", true)
                put("updated_at", Instant.now().toString())
            }

            val request = Request.Builder()
                .url("$supabaseUrl/rest/v1/profiles?id=eq.$userId")
                .addHeader("apikey", anonKey)
                .addHeader("Authorization", "Bearer $accessToken")
                .addHeader("Content-Type", "application/json")
                .addHeader("Prefer", "return=minimal")
                .patch(bodyJson.toString().toRequestBody(jsonType))
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    val body = response.body?.string().orEmpty()
                    return AuthResult(false, if (body.isNotBlank()) body else "Failed to complete profile")
                }
                AuthResult(true, "Profile completed successfully.")
            }
        } catch (e: Exception) {
            AuthResult(false, e.message ?: "Profile update error")
        }
    }

    fun fetchDestinations(accessToken: String): List<Destination> {
        return try {
            val request = Request.Builder()
                .url("$supabaseUrl/rest/v1/destinations?select=id,name,category,location,difficulty,reward_points,description,image_url&order=created_at.desc")
                .addHeader("apikey", anonKey)
                .addHeader("Authorization", "Bearer $accessToken")
                .get()
                .build()

            client.newCall(request).execute().use { response ->
                val body = response.body?.string().orEmpty()
                if (!response.isSuccessful) return emptyList()

                val array = JSONArray(body)
                buildList {
                    for (i in 0 until array.length()) {
                        val obj = array.getJSONObject(i)
                        add(
                            Destination(
                                id = obj.optString("id"),
                                name = obj.optString("name"),
                                category = obj.optString("category"),
                                location = obj.optString("location"),
                                difficulty = obj.optString("difficulty"),
                                rewardPoints = obj.optInt("reward_points", 0),
                                description = obj.optString("description"),
                                imageUrl = obj.optString("image_url").ifBlank { null }
                            )
                        )
                    }
                }
            }
        } catch (_: Exception) {
            emptyList()
        }
    }

    fun submitBasicCheckIn(
        accessToken: String,
        userId: String,
        destinationId: String
    ): CheckInResult {
        return try {
            val bodyJson = JSONObject().apply {
                put("user_id", userId)
                put("destination_id", destinationId)
                put("gps_verified", false)
                put("photo_verified", false)
                put("verified", false)
                put("status", "pending")
                put("reward_amount", 0)
                put("rewarded", false)
            }

            val request = Request.Builder()
                .url("$supabaseUrl/rest/v1/checkins")
                .addHeader("apikey", anonKey)
                .addHeader("Authorization", "Bearer $accessToken")
                .addHeader("Content-Type", "application/json")
                .addHeader("Prefer", "return=minimal")
                .post(bodyJson.toString().toRequestBody(jsonType))
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    val body = response.body?.string().orEmpty()
                    return CheckInResult(
                        false,
                        if (body.isNotBlank()) body else "Failed to submit check-in"
                    )
                }
                CheckInResult(true, "Check-in submitted successfully.")
            }
        } catch (e: Exception) {
            CheckInResult(false, e.message ?: "Check-in error")
        }
    }

    fun fetchPosts(accessToken: String): List<FeedPost> {
        return try {
            val request = Request.Builder()
                .url("$supabaseUrl/rest/v1/posts?select=id,user_id,author_name,destination,caption,image_url,likes_count,comments_count,post_type,created_at&order=created_at.desc")
                .addHeader("apikey", anonKey)
                .addHeader("Authorization", "Bearer $accessToken")
                .get()
                .build()

            client.newCall(request).execute().use { response ->
                val body = response.body?.string().orEmpty()
                if (!response.isSuccessful) return emptyList()

                val array = JSONArray(body)
                buildList {
                    for (i in 0 until array.length()) {
                        val obj = array.getJSONObject(i)
                        add(
                            FeedPost(
                                id = obj.optString("id"),
                                userId = obj.optString("user_id"),
                                authorName = obj.optString("author_name"),
                                destination = obj.optString("destination"),
                                caption = obj.optString("caption"),
                                imageUrl = obj.optString("image_url").ifBlank { null },
                                likesCount = obj.optInt("likes_count", 0),
                                commentsCount = obj.optInt("comments_count", 0),
                                postType = obj.optString("post_type", "standard"),
                                createdAt = obj.optString("created_at")
                            )
                        )
                    }
                }
            }
        } catch (_: Exception) {
            emptyList()
        }
    }

    fun createBasicPost(
        accessToken: String,
        userId: String,
        authorName: String,
        destination: String,
        caption: String
    ): FeedActionResult {
        return try {
            val bodyJson = JSONObject().apply {
                put("user_id", userId)
                put("post_type", "standard")
                put("author_name", authorName)
                put("destination", destination)
                put("caption", caption)
                put("likes_count", 0)
                put("comments_count", 0)
            }

            val request = Request.Builder()
                .url("$supabaseUrl/rest/v1/posts")
                .addHeader("apikey", anonKey)
                .addHeader("Authorization", "Bearer $accessToken")
                .addHeader("Content-Type", "application/json")
                .addHeader("Prefer", "return=minimal")
                .post(bodyJson.toString().toRequestBody(jsonType))
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    val body = response.body?.string().orEmpty()
                    return FeedActionResult(
                        false,
                        if (body.isNotBlank()) body else "Failed to create post"
                    )
                }
                FeedActionResult(true, "Post created successfully.")
            }
        } catch (e: Exception) {
            FeedActionResult(false, e.message ?: "Post creation error")
        }
    }

    fun deleteOwnPost(
        accessToken: String,
        postId: String,
        userId: String
    ): FeedActionResult {
        return try {
            val request = Request.Builder()
                .url("$supabaseUrl/rest/v1/posts?id=eq.$postId&user_id=eq.$userId")
                .addHeader("apikey", anonKey)
                .addHeader("Authorization", "Bearer $accessToken")
                .addHeader("Prefer", "return=minimal")
                .delete()
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    val body = response.body?.string().orEmpty()
                    return FeedActionResult(
                        false,
                        if (body.isNotBlank()) body else "Failed to delete post"
                    )
                }
                FeedActionResult(true, "Post deleted successfully.")
            }
        } catch (e: Exception) {
            FeedActionResult(false, e.message ?: "Delete post error")
        }
    }

    fun fetchWalletSummary(accessToken: String, userId: String): WalletSummary {
        return try {
            val request = Request.Builder()
                .url("$supabaseUrl/rest/v1/wallets?user_id=eq.$userId&select=available_balance,locked_balance,pending_balance")
                .addHeader("apikey", anonKey)
                .addHeader("Authorization", "Bearer $accessToken")
                .get()
                .build()

            client.newCall(request).execute().use { response ->
                val body = response.body?.string().orEmpty()
                if (!response.isSuccessful) return WalletSummary()

                val array = JSONArray(body)
                if (array.length() == 0) return WalletSummary()

                val obj = array.getJSONObject(0)
                WalletSummary(
                    availableBalance = obj.optDouble("available_balance", 0.0),
                    lockedBalance = obj.optDouble("locked_balance", 0.0),
                    pendingBalance = obj.optDouble("pending_balance", 0.0)
                )
            }
        } catch (_: Exception) {
            WalletSummary()
        }
    }

    fun fetchProfile(accessToken: String, userId: String): UserProfile? {
        return try {
            val request = Request.Builder()
                .url(
                    "$supabaseUrl/rest/v1/profiles" +
                            "?id=eq.$userId" +
                            "&select=id,full_name,display_name,bio,wallet_address,phone_local_number,country,region,municipality,barangay,zip_code,avatar_url,is_profile_complete"
                )
                .addHeader("apikey", anonKey)
                .addHeader("Authorization", "Bearer $accessToken")
                .get()
                .build()

            client.newCall(request).execute().use { response ->
                val body = response.body?.string().orEmpty()
                if (!response.isSuccessful) return null

                val array = JSONArray(body)
                if (array.length() == 0) return null

                val obj = array.getJSONObject(0)
                UserProfile(
                    id = obj.optString("id"),
                    fullName = obj.optString("full_name"),
                    displayName = obj.optString("display_name"),
                    bio = obj.optString("bio"),
                    walletAddress = obj.optString("wallet_address"),
                    phoneLocalNumber = obj.optString("phone_local_number"),
                    country = obj.optString("country"),
                    region = obj.optString("region"),
                    municipality = obj.optString("municipality"),
                    barangay = obj.optString("barangay"),
                    zipCode = obj.optString("zip_code"),
                    avatarUrl = obj.optString("avatar_url").ifBlank { null },
                    isProfileComplete = obj.optBoolean("is_profile_complete", false)
                )
            }
        } catch (_: Exception) {
            null
        }
    }

    fun updateProfile(
        accessToken: String,
        userId: String,
        fullName: String,
        displayName: String,
        bio: String,
        phoneLocalNumber: String,
        country: String,
        region: String,
        municipality: String,
        barangay: String,
        zipCode: String,
        walletAddress: String
    ): FeedActionResult {
        return try {
            val bodyJson = JSONObject().apply {
                put("full_name", fullName)
                put("display_name", displayName)
                put("bio", bio)
                put("phone_local_number", phoneLocalNumber)
                put("country", country)
                put("region", region)
                put("municipality", municipality)
                put("barangay", barangay)
                put("zip_code", zipCode)
                put("wallet_address", walletAddress)
                put("updated_at", Instant.now().toString())
            }

            val request = Request.Builder()
                .url("$supabaseUrl/rest/v1/profiles?id=eq.$userId")
                .addHeader("apikey", anonKey)
                .addHeader("Authorization", "Bearer $accessToken")
                .addHeader("Content-Type", "application/json")
                .addHeader("Prefer", "return=minimal")
                .patch(bodyJson.toString().toRequestBody(jsonType))
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    val body = response.body?.string().orEmpty()
                    return FeedActionResult(
                        false,
                        if (body.isNotBlank()) body else "Failed to update profile"
                    )
                }
                FeedActionResult(true, "Profile updated successfully.")
            }
        } catch (e: Exception) {
            FeedActionResult(false, e.message ?: "Profile update error")
        }
    }

    private fun fetchProfileComplete(accessToken: String, userId: String): Boolean {
        return try {
            val request = Request.Builder()
                .url("$supabaseUrl/rest/v1/profiles?id=eq.$userId&select=is_profile_complete")
                .addHeader("apikey", anonKey)
                .addHeader("Authorization", "Bearer $accessToken")
                .get()
                .build()

            client.newCall(request).execute().use { response ->
                val body = response.body?.string().orEmpty()
                if (!response.isSuccessful) return false

                val array = JSONArray(body)
                if (array.length() == 0) return false

                array.getJSONObject(0).optBoolean("is_profile_complete", false)
            }
        } catch (_: Exception) {
            false
        }
    }

    private fun insertProfileStub(userId: String, fullName: String) {
        try {
            val bodyJson = JSONObject().apply {
                put("id", userId)
                put("full_name", fullName)
                put("display_name", fullName)
                put("bio", "")
                put("is_profile_complete", false)
            }

            val request = Request.Builder()
                .url("$supabaseUrl/rest/v1/profiles")
                .addHeader("apikey", anonKey)
                .addHeader("Authorization", "Bearer $anonKey")
                .addHeader("Content-Type", "application/json")
                .addHeader("Prefer", "return=minimal")
                .post(bodyJson.toString().toRequestBody(jsonType))
                .build()

            client.newCall(request).execute().close()
        } catch (_: Exception) {
        }
    }

    private fun insertWalletStub(userId: String) {
        try {
            val bodyJson = JSONObject().apply {
                put("user_id", userId)
                put("available_balance", 1200)
                put("locked_balance", 0)
                put("pending_balance", 0)
            }

            val request = Request.Builder()
                .url("$supabaseUrl/rest/v1/wallets")
                .addHeader("apikey", anonKey)
                .addHeader("Authorization", "Bearer $anonKey")
                .addHeader("Content-Type", "application/json")
                .addHeader("Prefer", "return=minimal")
                .post(bodyJson.toString().toRequestBody(jsonType))
                .build()

            client.newCall(request).execute().close()
        } catch (_: Exception) {
        }
    }
}