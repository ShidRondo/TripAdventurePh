package com.example.tripadventureph

import android.content.Context
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject

data class AuthResult(
    val success: Boolean,
    val message: String,
    val accessToken: String? = null,
    val userId: String? = null,
    val email: String? = null,
    val profileComplete: Boolean = false
)

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
                put("updated_at", java.time.Instant.now().toString())
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