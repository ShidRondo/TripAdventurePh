package com.example.tripadventureph

import android.content.Context

class SessionManager(context: Context) {
    private val prefs = context.getSharedPreferences("travelquest_session", Context.MODE_PRIVATE)

    fun saveSession(
        accessToken: String,
        userId: String,
        email: String,
        profileComplete: Boolean
    ) {
        prefs.edit()
            .putString("access_token", accessToken)
            .putString("user_id", userId)
            .putString("email", email)
            .putBoolean("profile_complete", profileComplete)
            .apply()
    }

    fun saveProfileComplete(value: Boolean) {
        prefs.edit()
            .putBoolean("profile_complete", value)
            .apply()
    }

    fun getAccessToken(): String? = prefs.getString("access_token", null)

    fun getUserId(): String? = prefs.getString("user_id", null)

    fun getEmail(): String? = prefs.getString("email", null)

    fun isProfileComplete(): Boolean = prefs.getBoolean("profile_complete", false)

    fun isLoggedIn(): Boolean = !getAccessToken().isNullOrBlank()

    fun clear() {
        prefs.edit().clear().apply()
    }
}