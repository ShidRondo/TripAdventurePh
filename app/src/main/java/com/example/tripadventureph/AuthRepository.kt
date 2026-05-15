package com.example.tripadventureph

import android.content.Context
import android.net.Uri
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
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
                    return AuthResult(
                        true,
                        "Signup successful. Check email if confirmation is enabled."
                    )
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
                    return AuthResult(
                        false,
                        if (body.isNotBlank()) body else "Failed to complete profile"
                    )
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

    fun uploadProofImage(
        accessToken: String,
        userId: String,
        imageUri: Uri
    ): String? {
        return try {
            val inputStream = context.contentResolver.openInputStream(imageUri) ?: return null
            val tempFile = File.createTempFile(
                "proof_${System.currentTimeMillis()}",
                ".jpg",
                context.cacheDir
            )

            tempFile.outputStream().use { output ->
                inputStream.copyTo(output)
            }

            val filePath = "$userId/${tempFile.name}"
            val mediaType = "image/jpeg".toMediaType()
            val requestBody = tempFile.asRequestBody(mediaType)

            val request = Request.Builder()
                .url("$supabaseUrl/storage/v1/object/checkin-proofs/$filePath")
                .addHeader("apikey", anonKey)
                .addHeader("Authorization", "Bearer $accessToken")
                .addHeader("x-upsert", "true")
                .post(requestBody)
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return null
            }

            "$supabaseUrl/storage/v1/object/public/checkin-proofs/$filePath"
        } catch (_: Exception) {
            null
        }
    }

    fun submitAdvancedCheckIn(
        accessToken: String,
        userId: String,
        destinationName: String,
        currentLat: Double,
        currentLng: Double,
        routePointCount: Int,
        proofImageUrl: String?,
        proofMetadata: String,
        startVerified: Boolean,
        destinationReached: Boolean
    ): CheckInResult {
        return try {
            val bodyJson = JSONObject().apply {
                put("user_id", userId)
                put("destination_name", destinationName)
                put("gps_verified", destinationReached)
                put("photo_verified", proofImageUrl != null)
                put("verified", startVerified && destinationReached && proofImageUrl != null)
                put(
                    "status",
                    if (startVerified && destinationReached && proofImageUrl != null) {
                        "verified"
                    } else {
                        "pending"
                    }
                )
                put("reward_amount", 0)
                put("rewarded", false)
                put("proof_image_url", proofImageUrl)
                put("proof_metadata", proofMetadata)
                put("current_latitude", currentLat)
                put("current_longitude", currentLng)
                put("route_point_count", routePointCount)
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
                        if (body.isNotBlank()) body else "Failed to submit advanced check-in"
                    )
                }
                CheckInResult(true, "Advanced check-in submitted successfully.")
            }
        } catch (e: Exception) {
            CheckInResult(false, e.message ?: "Advanced check-in error")
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

    fun fetchWalletTransactions(
        accessToken: String,
        userId: String
    ): List<WalletTransaction> {
        return try {
            val request = Request.Builder()
                .url(
                    "$supabaseUrl/rest/v1/wallet_transactions?" +
                            "user_id=eq.$userId&select=id,user_id,tx_type,amount,direction,title,description,reference_id,created_at" +
                            "&order=created_at.desc"
                )
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
                            WalletTransaction(
                                id = obj.optString("id"),
                                userId = obj.optString("user_id"),
                                txType = obj.optString("tx_type"),
                                amount = obj.optDouble("amount", 0.0),
                                direction = obj.optString("direction"),
                                title = obj.optString("title"),
                                description = obj.optString("description"),
                                referenceId = obj.optString("reference_id").ifBlank { null },
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

    fun insertWalletTransaction(
        accessToken: String,
        userId: String,
        txType: String,
        amount: Double,
        direction: String,
        title: String,
        description: String,
        referenceId: String?
    ): EventActionResult {
        return try {
            val bodyJson = JSONObject().apply {
                put("user_id", userId)
                put("tx_type", txType)
                put("amount", amount)
                put("direction", direction)
                put("title", title)
                put("description", description)
                put("reference_id", referenceId ?: JSONObject.NULL)
            }

            val request = Request.Builder()
                .url("$supabaseUrl/rest/v1/wallet_transactions")
                .addHeader("apikey", anonKey)
                .addHeader("Authorization", "Bearer $accessToken")
                .addHeader("Content-Type", "application/json")
                .addHeader("Prefer", "return=minimal")
                .post(bodyJson.toString().toRequestBody(jsonType))
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    val body = response.body?.string().orEmpty()
                    return EventActionResult(
                        false,
                        if (body.isNotBlank()) body else "Failed to insert wallet transaction"
                    )
                }
                EventActionResult(true, "Wallet transaction inserted.")
            }
        } catch (e: Exception) {
            EventActionResult(false, e.message ?: "Wallet transaction error")
        }
    }

    fun addToWalletBalance(
        accessToken: String,
        userId: String,
        amount: Double
    ): EventActionResult {
        return try {
            val currentWallet = fetchWalletSummary(accessToken, userId)
            val newAvailable = currentWallet.availableBalance + amount

            val bodyJson = JSONObject().apply {
                put("available_balance", newAvailable)
                put("updated_at", Instant.now().toString())
            }

            val request = Request.Builder()
                .url("$supabaseUrl/rest/v1/wallets?user_id=eq.$userId")
                .addHeader("apikey", anonKey)
                .addHeader("Authorization", "Bearer $accessToken")
                .addHeader("Content-Type", "application/json")
                .addHeader("Prefer", "return=minimal")
                .patch(bodyJson.toString().toRequestBody(jsonType))
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    val body = response.body?.string().orEmpty()
                    return EventActionResult(
                        false,
                        if (body.isNotBlank()) body else "Failed to update wallet"
                    )
                }
                EventActionResult(true, "Wallet updated successfully.")
            }
        } catch (e: Exception) {
            EventActionResult(false, e.message ?: "Wallet update error")
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

    fun fetchTrails(accessToken: String): List<Trail> {
        return try {
            val request = Request.Builder()
                .url("$supabaseUrl/rest/v1/trails?select=id,code,name,area,next_trail_id&order=created_at.desc")
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
                            Trail(
                                id = obj.optString("id"),
                                code = obj.optString("code"),
                                name = obj.optString("name"),
                                area = obj.optString("area"),
                                nextTrailId = obj.optString("next_trail_id").ifBlank { null }
                            )
                        )
                    }
                }
            }
        } catch (_: Exception) {
            emptyList()
        }
    }

    fun fetchTrailheads(
        accessToken: String,
        trailId: String
    ): List<Trailhead> {
        return try {
            val request = Request.Builder()
                .url("$supabaseUrl/rest/v1/trailheads?trail_id=eq.$trailId&select=id,trail_id,name,location,lat,lng&order=created_at.asc")
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
                            Trailhead(
                                id = obj.optString("id"),
                                trailId = obj.optString("trail_id"),
                                name = obj.optString("name"),
                                location = obj.optString("location"),
                                lat = if (obj.isNull("lat")) null else obj.optDouble("lat"),
                                lng = if (obj.isNull("lng")) null else obj.optDouble("lng")
                            )
                        )
                    }
                }
            }
        } catch (_: Exception) {
            emptyList()
        }
    }

    fun createHikeSession(
        accessToken: String,
        userId: String,
        trailId: String
    ): HikeSessionResult {
        return try {
            val bodyJson = JSONObject().apply {
                put("user_id", userId)
                put("trail_id", trailId)
                put("active", false)
                put("trailhead_verified", false)
                put("current_trailhead_matched", false)
                put("target_reached", false)
                put("status", "Not Started")
                put("total_earned", 0)
                put("multi_destination_bonus_awarded", false)
                put("next_trail_ready", false)
            }

            val request = Request.Builder()
                .url("$supabaseUrl/rest/v1/hike_sessions")
                .addHeader("apikey", anonKey)
                .addHeader("Authorization", "Bearer $accessToken")
                .addHeader("Content-Type", "application/json")
                .addHeader("Prefer", "return=minimal")
                .post(bodyJson.toString().toRequestBody(jsonType))
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    val body = response.body?.string().orEmpty()
                    return HikeSessionResult(
                        false,
                        if (body.isNotBlank()) body else "Failed to create hike session"
                    )
                }
                HikeSessionResult(true, "Hike session created.")
            }
        } catch (e: Exception) {
            HikeSessionResult(false, e.message ?: "Create hike session error")
        }
    }

    fun startHikeSession(
        accessToken: String,
        userId: String,
        trailId: String
    ): HikeSessionResult {
        return try {
            val bodyJson = JSONObject().apply {
                put("active", true)
                put("status", "Active")
                put("started_at", Instant.now().toString())
            }

            val request = Request.Builder()
                .url("$supabaseUrl/rest/v1/hike_sessions?user_id=eq.$userId&trail_id=eq.$trailId")
                .addHeader("apikey", anonKey)
                .addHeader("Authorization", "Bearer $accessToken")
                .addHeader("Content-Type", "application/json")
                .addHeader("Prefer", "return=minimal")
                .patch(bodyJson.toString().toRequestBody(jsonType))
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    val body = response.body?.string().orEmpty()
                    return HikeSessionResult(
                        false,
                        if (body.isNotBlank()) body else "Failed to start hike session"
                    )
                }
                HikeSessionResult(true, "Hiking session started.")
            }
        } catch (e: Exception) {
            HikeSessionResult(false, e.message ?: "Start hike session error")
        }
    }

    fun verifyTrailheadMatch(
        accessToken: String,
        userId: String,
        trailId: String
    ): HikeSessionResult {
        return try {
            val bodyJson = JSONObject().apply {
                put("trailhead_verified", true)
                put("current_trailhead_matched", true)
            }

            val request = Request.Builder()
                .url("$supabaseUrl/rest/v1/hike_sessions?user_id=eq.$userId&trail_id=eq.$trailId")
                .addHeader("apikey", anonKey)
                .addHeader("Authorization", "Bearer $accessToken")
                .addHeader("Content-Type", "application/json")
                .addHeader("Prefer", "return=minimal")
                .patch(bodyJson.toString().toRequestBody(jsonType))
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    val body = response.body?.string().orEmpty()
                    return HikeSessionResult(
                        false,
                        if (body.isNotBlank()) body else "Failed to verify trailhead"
                    )
                }
                HikeSessionResult(true, "Trailhead verified.")
            }
        } catch (e: Exception) {
            HikeSessionResult(false, e.message ?: "Verify trailhead error")
        }
    }

    fun completeHikeSession(
        accessToken: String,
        userId: String,
        trailId: String,
        totalEarned: Double,
        nextTrailReady: Boolean
    ): HikeSessionResult {
        return try {
            val bodyJson = JSONObject().apply {
                put("active", false)
                put("target_reached", true)
                put("status", "Completed")
                put("total_earned", totalEarned)
                put("next_trail_ready", nextTrailReady)
                put("ended_at", Instant.now().toString())
            }

            val request = Request.Builder()
                .url("$supabaseUrl/rest/v1/hike_sessions?user_id=eq.$userId&trail_id=eq.$trailId")
                .addHeader("apikey", anonKey)
                .addHeader("Authorization", "Bearer $accessToken")
                .addHeader("Content-Type", "application/json")
                .addHeader("Prefer", "return=minimal")
                .patch(bodyJson.toString().toRequestBody(jsonType))
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    val body = response.body?.string().orEmpty()
                    return HikeSessionResult(
                        false,
                        if (body.isNotBlank()) body else "Failed to complete hike session"
                    )
                }
                HikeSessionResult(true, "Hike completed successfully.")
            }
        } catch (e: Exception) {
            HikeSessionResult(false, e.message ?: "Complete hike session error")
        }
    }

    fun fetchEvents(accessToken: String): List<EventModel> {
        return try {
            val request = Request.Builder()
                .url(
                    "$supabaseUrl/rest/v1/events?" +
                            "select=id,created_by,title,category,difficulty,description,event_image_url,start_date,expiration_date,start_time,end_time,capacity,stake_amount,route_start_name,route_destination_name,route_start_latitude,route_start_longitude,route_destination_latitude,route_destination_longitude,reward_pool,burn_amount,remaining_reward_pool,status" +
                            "&order=created_at.desc"
                )
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
                            EventModel(
                                id = obj.optString("id"),
                                createdBy = obj.optString("created_by"),
                                title = obj.optString("title"),
                                category = obj.optString("category"),
                                difficulty = obj.optString("difficulty"),
                                description = obj.optString("description"),
                                eventImageUrl = obj.optString("event_image_url").ifBlank { null },
                                startDate = obj.optString("start_date"),
                                expirationDate = obj.optString("expiration_date"),
                                startTime = obj.optString("start_time"),
                                endTime = obj.optString("end_time"),
                                capacity = obj.optInt("capacity", 0),
                                stakeAmount = obj.optDouble("stake_amount", 0.0),
                                routeStartName = obj.optString("route_start_name"),
                                routeDestinationName = obj.optString("route_destination_name"),
                                routeStartLatitude = if (obj.isNull("route_start_latitude")) null else obj.optDouble("route_start_latitude"),
                                routeStartLongitude = if (obj.isNull("route_start_longitude")) null else obj.optDouble("route_start_longitude"),
                                routeDestinationLatitude = if (obj.isNull("route_destination_latitude")) null else obj.optDouble("route_destination_latitude"),
                                routeDestinationLongitude = if (obj.isNull("route_destination_longitude")) null else obj.optDouble("route_destination_longitude"),
                                rewardPool = obj.optDouble("reward_pool", 0.0),
                                burnAmount = obj.optDouble("burn_amount", 0.0),
                                remainingRewardPool = obj.optDouble("remaining_reward_pool", 0.0),
                                status = obj.optString("status")
                            )
                        )
                    }
                }
            }
        } catch (_: Exception) {
            emptyList()
        }
    }

    fun createEvent(
        accessToken: String,
        createdBy: String,
        title: String,
        category: String,
        difficulty: String,
        description: String,
        startDate: String,
        expirationDate: String,
        startTime: String,
        endTime: String,
        capacity: Int,
        stakeAmount: Double,
        routeStartName: String,
        routeDestinationName: String,
        routeStartLatitude: Double?,
        routeStartLongitude: Double?,
        routeDestinationLatitude: Double?,
        routeDestinationLongitude: Double?
    ): EventActionResult {
        return try {
            val burnAmount = stakeAmount * 0.10
            val rewardPool = stakeAmount * 0.90

            val bodyJson = JSONObject().apply {
                put("created_by", createdBy)
                put("title", title)
                put("category", category)
                put("difficulty", difficulty)
                put("description", description)
                put("start_date", if (startDate.isBlank()) JSONObject.NULL else startDate)
                put("expiration_date", if (expirationDate.isBlank()) JSONObject.NULL else expirationDate)
                put("start_time", if (startTime.isBlank()) JSONObject.NULL else startTime)
                put("end_time", if (endTime.isBlank()) JSONObject.NULL else endTime)
                put("capacity", capacity)
                put("stake_amount", stakeAmount)
                put("route_start_name", routeStartName)
                put("route_destination_name", routeDestinationName)
                put("route_start_latitude", routeStartLatitude ?: JSONObject.NULL)
                put("route_start_longitude", routeStartLongitude ?: JSONObject.NULL)
                put("route_destination_latitude", routeDestinationLatitude ?: JSONObject.NULL)
                put("route_destination_longitude", routeDestinationLongitude ?: JSONObject.NULL)
                put("reward_pool", rewardPool)
                put("burn_amount", burnAmount)
                put("remaining_reward_pool", rewardPool)
                put("status", "open")
            }

            val request = Request.Builder()
                .url("$supabaseUrl/rest/v1/events")
                .addHeader("apikey", anonKey)
                .addHeader("Authorization", "Bearer $accessToken")
                .addHeader("Content-Type", "application/json")
                .addHeader("Prefer", "return=minimal")
                .post(bodyJson.toString().toRequestBody(jsonType))
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    val body = response.body?.string().orEmpty()
                    return EventActionResult(
                        false,
                        if (body.isNotBlank()) body else "Failed to create event"
                    )
                }
                EventActionResult(true, "Event created successfully.")
            }
        } catch (e: Exception) {
            EventActionResult(false, e.message ?: "Create event error")
        }
    }

    fun joinEvent(
        accessToken: String,
        eventId: String,
        userId: String
    ): EventActionResult {
        return try {
            val bodyJson = JSONObject().apply {
                put("event_id", eventId)
                put("user_id", userId)
                put("joined", true)
                put("verified_start", false)
                put("completed", false)
                put("failed", false)
                put("reward_claimed", false)
            }

            val request = Request.Builder()
                .url("$supabaseUrl/rest/v1/event_participants")
                .addHeader("apikey", anonKey)
                .addHeader("Authorization", "Bearer $accessToken")
                .addHeader("Content-Type", "application/json")
                .addHeader("Prefer", "return=minimal")
                .post(bodyJson.toString().toRequestBody(jsonType))
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    val body = response.body?.string().orEmpty()
                    return EventActionResult(
                        false,
                        if (body.isNotBlank()) body else "Failed to join event"
                    )
                }
                EventActionResult(true, "Joined event successfully.")
            }
        } catch (e: Exception) {
            EventActionResult(false, e.message ?: "Join event error")
        }
    }

    fun fetchMyEventParticipation(
        accessToken: String,
        userId: String
    ): List<EventParticipant> {
        return try {
            val request = Request.Builder()
                .url("$supabaseUrl/rest/v1/event_participants?user_id=eq.$userId&select=id,event_id,user_id,joined,verified_start,completed,failed,reward_claimed")
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
                            EventParticipant(
                                id = obj.optString("id"),
                                eventId = obj.optString("event_id"),
                                userId = obj.optString("user_id"),
                                joined = obj.optBoolean("joined", false),
                                verifiedStart = obj.optBoolean("verified_start", false),
                                completed = obj.optBoolean("completed", false),
                                failed = obj.optBoolean("failed", false),
                                rewardClaimed = obj.optBoolean("reward_claimed", false)
                            )
                        )
                    }
                }
            }
        } catch (_: Exception) {
            emptyList()
        }
    }

    fun verifyEventStart(
        accessToken: String,
        eventId: String,
        userId: String
    ): EventActionResult {
        return try {
            val bodyJson = JSONObject().apply {
                put("verified_start", true)
            }

            val request = Request.Builder()
                .url("$supabaseUrl/rest/v1/event_participants?event_id=eq.$eventId&user_id=eq.$userId")
                .addHeader("apikey", anonKey)
                .addHeader("Authorization", "Bearer $accessToken")
                .addHeader("Content-Type", "application/json")
                .addHeader("Prefer", "return=minimal")
                .patch(bodyJson.toString().toRequestBody(jsonType))
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    val body = response.body?.string().orEmpty()
                    return EventActionResult(
                        false,
                        if (body.isNotBlank()) body else "Failed to verify event start"
                    )
                }
                EventActionResult(true, "Event start verified.")
            }
        } catch (e: Exception) {
            EventActionResult(false, e.message ?: "Verify event start error")
        }
    }

    fun completeEvent(
        accessToken: String,
        eventId: String,
        userId: String
    ): EventActionResult {
        return try {
            val bodyJson = JSONObject().apply {
                put("completed", true)
                put("failed", false)
            }

            val request = Request.Builder()
                .url("$supabaseUrl/rest/v1/event_participants?event_id=eq.$eventId&user_id=eq.$userId")
                .addHeader("apikey", anonKey)
                .addHeader("Authorization", "Bearer $accessToken")
                .addHeader("Content-Type", "application/json")
                .addHeader("Prefer", "return=minimal")
                .patch(bodyJson.toString().toRequestBody(jsonType))
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    val body = response.body?.string().orEmpty()
                    return EventActionResult(
                        false,
                        if (body.isNotBlank()) body else "Failed to complete event"
                    )
                }
                EventActionResult(true, "Event marked as completed.")
            }
        } catch (e: Exception) {
            EventActionResult(false, e.message ?: "Complete event error")
        }
    }

    fun claimEventReward(
        accessToken: String,
        eventId: String,
        userId: String,
        rewardAmount: Double
    ): EventActionResult {
        return try {
            val rewardResult = addToWalletBalance(
                accessToken = accessToken,
                userId = userId,
                amount = rewardAmount
            )

            if (!rewardResult.success) {
                return rewardResult
            }

            val txResult = insertWalletTransaction(
                accessToken = accessToken,
                userId = userId,
                txType = "event_reward",
                amount = rewardAmount,
                direction = "credit",
                title = "Event Reward",
                description = "Reward claimed from event participation.",
                referenceId = eventId
            )

            if (!txResult.success) {
                return txResult
            }

            val bodyJson = JSONObject().apply {
                put("reward_claimed", true)
            }

            val request = Request.Builder()
                .url("$supabaseUrl/rest/v1/event_participants?event_id=eq.$eventId&user_id=eq.$userId")
                .addHeader("apikey", anonKey)
                .addHeader("Authorization", "Bearer $accessToken")
                .addHeader("Content-Type", "application/json")
                .addHeader("Prefer", "return=minimal")
                .patch(bodyJson.toString().toRequestBody(jsonType))
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    val body = response.body?.string().orEmpty()
                    return EventActionResult(
                        false,
                        if (body.isNotBlank()) body else "Failed to mark reward as claimed"
                    )
                }
                EventActionResult(true, "Reward claimed and added to wallet.")
            }
        } catch (e: Exception) {
            EventActionResult(false, e.message ?: "Claim reward error")
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