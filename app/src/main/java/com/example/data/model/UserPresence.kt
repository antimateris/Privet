package com.example.data.model

/**
 * Model representing user activity and presence in the app,
 * tracking who is currently active and their last login timestamp.
 */
data class UserPresence(
    val userId: String = "",
    val userName: String = "",
    val role: String = "",
    val lastActiveTime: Long = 0L,
    val lastLoginTime: Long = 0L,
    val deviceModel: String = "",
    val isOnline: Boolean = true
) {
    /**
     * Considers user actively online if a heartbeat was received within the last 5 minutes.
     */
    fun isOnlineNow(): Boolean {
        if (lastActiveTime <= 0L) return false
        val diff = System.currentTimeMillis() - lastActiveTime
        return diff >= 0 && diff < 5 * 60 * 1000L
    }
}
