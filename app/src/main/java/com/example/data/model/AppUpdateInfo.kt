package com.example.data.model

/**
 * Represents metadata for In-App updates published exclusively by the Team IT account.
 * Synced across all devices via Firebase Firestore in real-time.
 */
data class AppUpdateInfo(
    val latestVersionCode: Long = 2L,
    val latestVersionName: String = "1.1.0",
    val downloadUrl: String = "",
    val releaseNotes: String = "",
    val isForceUpdate: Boolean = false,
    val releasedBy: String = "Team IT",
    val releaseTimestamp: Long = 0L,
    val fileSizeMb: String = ""
) {
    /**
     * Checks whether a new version is available compared to the currently installed app versionCode.
     */
    fun hasNewVersion(currentVersionCode: Long): Boolean {
        return latestVersionCode > currentVersionCode && downloadUrl.isNotBlank()
    }
}
