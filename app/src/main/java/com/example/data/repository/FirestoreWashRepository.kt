package com.example.data.repository

import android.content.Context
import android.util.Log
import com.example.data.model.AppUpdateInfo
import com.example.data.model.StoreExpense
import com.example.data.model.UserPresence
import com.example.data.model.WashRecord
import com.example.data.model.Worker
import com.google.android.gms.tasks.Task
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * Data wrapper containing the Firestore document ID alongside the WashRecord data.
 */
data class CloudWashRecord(
    val documentId: String,
    val record: WashRecord
)

/**
 * Shared app maintenance/lock status, synced across all devices via Firestore.
 */
data class MaintenanceStatusData(
    val enabled: Boolean = false,
    val message: String = "Aplikasi sedang dalam perbaikan. Silakan coba lagi nanti."
)

/**
 * Real-time push broadcast model to broadcast push notifications to all connected devices.
 */
data class BroadcastPushMessage(
    val id: String = "",
    val title: String = "",
    val message: String = "",
    val senderName: String = "",
    val timestamp: Long = 0L
)

/**
 * Repository class for Firebase Firestore handling real-time synchronization
 * of motor wash transactions for PT. LION STEAM MOTOR.
 */
class FirestoreWashRepository(
    private val injectedFirestore: FirebaseFirestore? = null,
    private val context: Context? = null
) {
    companion object {
        private const val TAG = "FirestoreWashRepo"
        const val DEFAULT_BRANCH = "lion_steam_pusat"
        private const val COLLECTION_BRANCHES = "branches"
        private const val COLLECTION_TRANSACTIONS = "transactions"
        private const val COLLECTION_SETTINGS = "settings"
        private const val COLLECTION_WORKERS = "workers"
        private const val COLLECTION_EXPENSES = "expenses"
        private const val COLLECTION_ACTIVE_USERS = "active_users"
        private const val DOC_ACCOUNTS = "account_credentials"
        private const val DOC_APP_STATUS = "app_status"
        private const val DOC_APP_UPDATE = "app_update"
        private const val OFFLINE_MESSAGE = "Mode lokal aktif: Data tersimpan aman di HP"
    }

    private fun getFirestore(): FirebaseFirestore? {
        if (injectedFirestore != null) return injectedFirestore
        return try {
            val app = try {
                FirebaseApp.getInstance()
            } catch (_: Exception) {
                val ctx = context
                if (ctx != null) {
                    try {
                        FirebaseApp.initializeApp(ctx)
                    } catch (_: Exception) {
                        try {
                            val options = FirebaseOptions.Builder()
                                .setApplicationId("1:507656640500:android:2f53f7fe6b8260fcae8ba1")
                                .setApiKey("AIzaSyCN7hfhHO6VZQttNc2TKc6aHls1ZjSr_GU")
                                .setProjectId("lion-steam-motor-e8eb3")
                                .setStorageBucket("lion-steam-motor-e8eb3.firebasestorage.app")
                                .build()
                            FirebaseApp.initializeApp(ctx, options)
                        } catch (_: Exception) {
                            null
                        }
                    }
                } else null
            }

            if (app != null) {
                FirebaseFirestore.getInstance(app).apply {
                    val settings = FirebaseFirestoreSettings.Builder()
                        .setPersistenceEnabled(true)
                        .build()
                    firestoreSettings = settings
                }
            } else {
                null
            }
        } catch (e: Exception) {
            Log.w(TAG, "Firebase Firestore init exception: ${e.message}")
            null
        }
    }

    private fun getTransactionsCollection(branchId: String = DEFAULT_BRANCH) =
        getFirestore()
            ?.collection(COLLECTION_BRANCHES)
            ?.document(branchId)
            ?.collection(COLLECTION_TRANSACTIONS)

    private fun getWorkersCollection(branchId: String = DEFAULT_BRANCH) =
        getFirestore()
            ?.collection(COLLECTION_BRANCHES)
            ?.document(branchId)
            ?.collection(COLLECTION_WORKERS)

    private fun getExpensesCollection(branchId: String = DEFAULT_BRANCH) =
        getFirestore()
            ?.collection(COLLECTION_BRANCHES)
            ?.document(branchId)
            ?.collection(COLLECTION_EXPENSES)

    private fun getActiveUsersCollection(branchId: String = DEFAULT_BRANCH) =
        getFirestore()
            ?.collection(COLLECTION_BRANCHES)
            ?.document(branchId)
            ?.collection(COLLECTION_ACTIVE_USERS)

    /**
     * Turns a petugas/worker name into a stable, safe Firestore document ID so the same
     * worker maps to the same document no matter which phone added them (local Room IDs
     * differ per device, so the name is used as the shared cross-device key instead).
     */
    private fun workerDocId(name: String): String {
        val slug = name.trim().lowercase()
            .replace(Regex("\\s+"), "_")
            .replace(Regex("[^a-z0-9_]"), "")
        return if (slug.isBlank()) "worker_${name.hashCode()}" else slug
    }

    private fun getAccountSettingsDoc(branchId: String = DEFAULT_BRANCH) =
        getFirestore()
            ?.collection(COLLECTION_BRANCHES)
            ?.document(branchId)
            ?.collection(COLLECTION_SETTINGS)
            ?.document(DOC_ACCOUNTS)

    private fun getAppStatusDoc(branchId: String = DEFAULT_BRANCH) =
        getFirestore()
            ?.collection(COLLECTION_BRANCHES)
            ?.document(branchId)
            ?.collection(COLLECTION_SETTINGS)
            ?.document(DOC_APP_STATUS)

    private fun getAppUpdateDoc(branchId: String = DEFAULT_BRANCH) =
        getFirestore()
            ?.collection(COLLECTION_BRANCHES)
            ?.document(branchId)
            ?.collection(COLLECTION_SETTINGS)
            ?.document(DOC_APP_UPDATE)

    /**
     * Checks if Firestore is ready and available in the current environment.
     */
    fun isAvailable(): Boolean = getFirestore() != null

    /**
     * Listens to real-time transaction updates from Firestore ordered by timestamp descending.
     * Automatically updates whenever any phone adds, edits, or deletes a transaction.
     */
    fun listenTransactions(branchId: String = DEFAULT_BRANCH): Flow<List<WashRecord>> {
        val collection = getTransactionsCollection(branchId)
            ?: return flowOf(emptyList())

        return callbackFlow {
            val listenerRegistration = collection
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.e(TAG, "Listen transactions failed", error)
                        return@addSnapshotListener
                    }

                    if (snapshot != null) {
                        val records = snapshot.documents.mapNotNull { doc ->
                            try {
                                doc.toWashRecord()
                            } catch (e: Exception) {
                                Log.e(TAG, "Error parsing doc ${doc.id}", e)
                                null
                            }
                        }
                        trySend(records)
                    }
                }

            awaitClose {
                listenerRegistration.remove()
            }
        }
    }

    /**
     * Listens to real-time transaction updates including the Firestore Document IDs.
     */
    fun listenCloudTransactions(branchId: String = DEFAULT_BRANCH): Flow<List<CloudWashRecord>> {
        val collection = getTransactionsCollection(branchId)
            ?: return flowOf(emptyList())

        return callbackFlow {
            val listenerRegistration = collection
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.e(TAG, "Listen cloud transactions failed", error)
                        return@addSnapshotListener
                    }

                    if (snapshot != null) {
                        val items = snapshot.documents.mapNotNull { doc ->
                            try {
                                CloudWashRecord(
                                    documentId = doc.id,
                                    record = doc.toWashRecord()
                                )
                            } catch (e: Exception) {
                                Log.e(TAG, "Error parsing doc ${doc.id}", e)
                                null
                            }
                        }
                        trySend(items)
                    }
                }

            awaitClose {
                listenerRegistration.remove()
            }
        }
    }

    /**
     * Saves or updates a transaction in Firestore using a deterministic timestamp-based ID
     * so both devices stay synchronized without duplicate records.
     */
    suspend fun saveTransaction(
        record: WashRecord,
        branchId: String = DEFAULT_BRANCH
    ): Result<String> {
        val collection = getTransactionsCollection(branchId)
            ?: return Result.failure(IllegalStateException(OFFLINE_MESSAGE))

        val docId = "wash_${record.timestamp}"
        return try {
            val docRef = collection.document(docId)
            val data = record.toFirestoreMap(documentId = docId)
            docRef.set(data).awaitTask()
            Log.d(TAG, "Transaction saved to Firestore with ID: $docId")
            Result.success(docId)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to save transaction to Firestore", e)
            Result.failure(e)
        }
    }

    /**
     * Deletes a transaction in Firestore using the record's timestamp ID.
     */
    suspend fun deleteTransaction(
        record: WashRecord,
        branchId: String = DEFAULT_BRANCH
    ): Result<Unit> {
        val collection = getTransactionsCollection(branchId)
            ?: return Result.failure(IllegalStateException(OFFLINE_MESSAGE))

        val docId = "wash_${record.timestamp}"
        return try {
            collection.document(docId).delete().awaitTask()
            Log.d(TAG, "Transaction $docId deleted from Firestore")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to delete transaction $docId from Firestore", e)
            Result.failure(e)
        }
    }

    /**
     * Adds a new motor wash transaction to Firestore.
     * Returns Result with generated document ID on success.
     */
    suspend fun addTransaction(
        record: WashRecord,
        branchId: String = DEFAULT_BRANCH
    ): Result<String> {
        return saveTransaction(record, branchId)
    }

    /**
     * Updates an existing transaction in Firestore by document ID.
     */
    suspend fun updateTransaction(
        documentId: String,
        record: WashRecord,
        branchId: String = DEFAULT_BRANCH
    ): Result<Unit> {
        val collection = getTransactionsCollection(branchId)
            ?: return Result.failure(IllegalStateException(OFFLINE_MESSAGE))

        return try {
            val data = record.toFirestoreMap(documentId = documentId)
            collection.document(documentId).set(data).awaitTask()
            Log.d(TAG, "Transaction $documentId successfully updated")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to update transaction $documentId", e)
            Result.failure(e)
        }
    }

    /**
     * Deletes a transaction in Firestore by document ID.
     */
    suspend fun deleteTransaction(
        documentId: String,
        branchId: String = DEFAULT_BRANCH
    ): Result<Unit> {
        val collection = getTransactionsCollection(branchId)
            ?: return Result.failure(IllegalStateException(OFFLINE_MESSAGE))

        return try {
            collection.document(documentId).delete().awaitTask()
            Log.d(TAG, "Transaction $documentId successfully deleted")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to delete transaction $documentId", e)
            Result.failure(e)
        }
    }

    /**
     * Synchronizes a batch of local Room records to Firestore (e.g. initial upload or migration).
     */
    suspend fun batchUploadLocalRecords(
        records: List<WashRecord>,
        branchId: String = DEFAULT_BRANCH
    ): Result<Int> {
        val firestore = getFirestore()
            ?: return Result.failure(IllegalStateException(OFFLINE_MESSAGE))

        val collection = getTransactionsCollection(branchId)
            ?: return Result.failure(IllegalStateException("Collection tidak ditemukan"))

        return try {
            val batch = firestore.batch()
            for (record in records) {
                val docRef = collection.document("wash_${record.timestamp}")
                batch.set(docRef, record.toFirestoreMap(documentId = docRef.id))
            }
            batch.commit().awaitTask()
            Log.d(TAG, "Batch synced ${records.size} records to Firestore")
            Result.success(records.size)
        } catch (e: Exception) {
            Log.e(TAG, "Batch sync failed", e)
            Result.failure(e)
        }
    }

    /**
     * Clears all transactions in Firestore.
     */
    suspend fun clearAllRemoteTransactions(branchId: String = DEFAULT_BRANCH): Result<Unit> {
        val collection = getTransactionsCollection(branchId)
            ?: return Result.failure(IllegalStateException(OFFLINE_MESSAGE))

        val firestore = getFirestore()
            ?: return Result.failure(IllegalStateException(OFFLINE_MESSAGE))

        return try {
            val snapshot = collection.get().awaitTask()
            val batch = firestore.batch()
            for (doc in snapshot.documents) {
                batch.delete(doc.reference)
            }
            batch.commit().awaitTask()
            Log.d(TAG, "All Firestore transactions cleared")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to clear all Firestore transactions", e)
            Result.failure(e)
        }
    }

    // --- Workers / Petugas Sync (shared list of petugas across all devices) ---

    /**
     * Listens in real-time to the shared petugas/worker list, so adding or deleting a
     * petugas on one phone reflects on every other connected phone within seconds.
     */
    fun listenWorkers(branchId: String = DEFAULT_BRANCH): Flow<List<Worker>> {
        val collection = getWorkersCollection(branchId)
            ?: return flowOf(emptyList())

        return callbackFlow {
            val listenerRegistration = collection
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.e(TAG, "Listen workers failed", error)
                        return@addSnapshotListener
                    }
                    if (snapshot != null) {
                        val workers = snapshot.documents.mapNotNull { doc ->
                            val name = doc.getString("name") ?: return@mapNotNull null
                            Worker(
                                name = name,
                                isActive = doc.getBoolean("isActive") ?: true
                            )
                        }
                        trySend(workers)
                    }
                }
            awaitClose { listenerRegistration.remove() }
        }
    }

    /**
     * Adds/updates a petugas in the shared cloud list so it appears on all other phones.
     */
    suspend fun saveWorker(worker: Worker, branchId: String = DEFAULT_BRANCH): Result<Unit> {
        val collection = getWorkersCollection(branchId)
            ?: return Result.failure(IllegalStateException(OFFLINE_MESSAGE))

        return try {
            val docRef = collection.document(workerDocId(worker.name))
            val data = hashMapOf(
                "name" to worker.name,
                "isActive" to worker.isActive,
                "updatedAt" to System.currentTimeMillis()
            )
            docRef.set(data).awaitTask()
            Log.d(TAG, "Worker synced to Firestore: ${worker.name}")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to sync worker", e)
            Result.failure(e)
        }
    }

    /**
     * Removes a petugas from the shared cloud list so it disappears on all other phones too.
     */
    suspend fun deleteWorkerRemote(name: String, branchId: String = DEFAULT_BRANCH): Result<Unit> {
        val collection = getWorkersCollection(branchId)
            ?: return Result.failure(IllegalStateException(OFFLINE_MESSAGE))

        return try {
            collection.document(workerDocId(name)).delete().awaitTask()
            Log.d(TAG, "Worker removed from Firestore: $name")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to delete worker remotely", e)
            Result.failure(e)
        }
    }

    // --- Account Credentials Sync (name & password per role, shared across devices) ---

    /**
     * Listens in real-time to the shared account credentials document, so that a password
     * or name change made on one device (Kasir/Manager/Owner) reflects on all other devices.
     * Returns null on a snapshot where the document doesn't exist yet (first run).
     */
    fun listenAccountSettings(branchId: String = DEFAULT_BRANCH): Flow<Map<String, Any>?> {
        val doc = getAccountSettingsDoc(branchId) ?: return flowOf(null)

        return callbackFlow {
            val listenerRegistration = doc.addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "Listen account settings failed", error)
                    return@addSnapshotListener
                }
                trySend(snapshot?.data)
            }
            awaitClose { listenerRegistration.remove() }
        }
    }

    /**
     * Merges the given fields (e.g. "KASIR_name", "KASIR_pass") into the shared account
     * credentials document without overwriting fields belonging to other roles.
     */
    suspend fun saveAccountFields(
        fields: Map<String, Any>,
        branchId: String = DEFAULT_BRANCH
    ): Result<Unit> {
        val doc = getAccountSettingsDoc(branchId)
            ?: return Result.failure(IllegalStateException(OFFLINE_MESSAGE))

        return try {
            doc.set(fields, com.google.firebase.firestore.SetOptions.merge()).awaitTask()
            Log.d(TAG, "Account fields synced to Firestore: ${fields.keys}")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to sync account fields", e)
            Result.failure(e)
        }
    }

    // --- Maintenance Mode (remote app lock, controlled by the Pemilik/Owner) ---

    /**
     * Listens in real-time to the shared app status document. When another device (or this one)
     * turns maintenance mode on/off, every connected device reflects it within seconds.
     * Emits enabled=false by default if the document doesn't exist yet or Firestore is offline.
     */
    fun listenMaintenanceStatus(branchId: String = DEFAULT_BRANCH): Flow<MaintenanceStatusData> {
        val doc = getAppStatusDoc(branchId) ?: return flowOf(MaintenanceStatusData())

        return callbackFlow {
            val listenerRegistration = doc.addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "Listen maintenance status failed", error)
                    return@addSnapshotListener
                }
                val enabled = snapshot?.getBoolean("enabled") ?: false
                val message = snapshot?.getString("message")
                    ?: "Aplikasi sedang dalam perbaikan. Silakan coba lagi nanti."
                trySend(MaintenanceStatusData(enabled = enabled, message = message))
            }
            awaitClose { listenerRegistration.remove() }
        }
    }

    /**
     * Turns maintenance mode on or off for all devices sharing this branch.
     */
    suspend fun setMaintenanceStatus(
        enabled: Boolean,
        message: String,
        branchId: String = DEFAULT_BRANCH
    ): Result<Unit> {
        val doc = getAppStatusDoc(branchId)
            ?: return Result.failure(IllegalStateException(OFFLINE_MESSAGE))

        return try {
            val data = hashMapOf(
                "enabled" to enabled,
                "message" to message,
                "updatedAt" to System.currentTimeMillis()
            )
            doc.set(data).awaitTask()
            Log.d(TAG, "Maintenance mode set to $enabled")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to set maintenance status", e)
            Result.failure(e)
        }
    }

    // --- In-App Updates & OTA Releases (Controlled exclusively by Team IT) ---

    /**
     * Listens in real-time for new app version updates pushed by Team IT.
     */
    fun listenAppUpdate(branchId: String = DEFAULT_BRANCH): Flow<AppUpdateInfo> {
        val doc = getAppUpdateDoc(branchId) ?: return flowOf(AppUpdateInfo())

        return callbackFlow {
            val listenerRegistration = doc.addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "Listen app update failed", error)
                    return@addSnapshotListener
                }
                if (snapshot != null && snapshot.exists()) {
                    val info = AppUpdateInfo(
                        latestVersionCode = snapshot.getLong("latestVersionCode") ?: 3L,
                        latestVersionName = snapshot.getString("latestVersionName") ?: "1.2.0",
                        downloadUrl = snapshot.getString("downloadUrl") ?: "",
                        releaseNotes = snapshot.getString("releaseNotes") ?: "",
                        isForceUpdate = snapshot.getBoolean("isForceUpdate") ?: false,
                        releasedBy = snapshot.getString("releasedBy") ?: "Team IT",
                        releaseTimestamp = snapshot.getLong("releaseTimestamp") ?: 0L,
                        fileSizeMb = snapshot.getString("fileSizeMb") ?: ""
                    )
                    trySend(info)
                } else {
                    trySend(AppUpdateInfo())
                }
            }
            awaitClose { listenerRegistration.remove() }
        }
    }

    /**
     * Publishes a new app update metadata to Firestore. Must be called after Team IT authentication.
     */
    suspend fun publishAppUpdate(
        updateInfo: AppUpdateInfo,
        branchId: String = DEFAULT_BRANCH
    ): Result<Unit> {
        val doc = getAppUpdateDoc(branchId)
            ?: return Result.failure(IllegalStateException(OFFLINE_MESSAGE))

        return try {
            val data = hashMapOf(
                "latestVersionCode" to updateInfo.latestVersionCode,
                "latestVersionName" to updateInfo.latestVersionName,
                "downloadUrl" to updateInfo.downloadUrl,
                "releaseNotes" to updateInfo.releaseNotes,
                "isForceUpdate" to updateInfo.isForceUpdate,
                "releasedBy" to updateInfo.releasedBy,
                "releaseTimestamp" to if (updateInfo.releaseTimestamp > 0) updateInfo.releaseTimestamp else System.currentTimeMillis(),
                "fileSizeMb" to updateInfo.fileSizeMb
            )
            doc.set(data).awaitTask()
            Log.d(TAG, "App update published: ${updateInfo.latestVersionName} (code ${updateInfo.latestVersionCode})")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to publish app update", e)
            Result.failure(e)
        }
    }

    // --- Store Expenses (Pengeluaran Toko) Sync ---

    fun listenExpenses(branchId: String = DEFAULT_BRANCH): Flow<List<StoreExpense>> {
        val collection = getExpensesCollection(branchId)
            ?: return flowOf(emptyList())

        return callbackFlow {
            val listenerRegistration = collection
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.e(TAG, "Listen expenses failed", error)
                        return@addSnapshotListener
                    }
                    if (snapshot != null) {
                        val expenses = snapshot.documents.mapNotNull { doc ->
                            try {
                                StoreExpense(
                                    id = doc.getLong("localId") ?: 0L,
                                    title = doc.getString("title") ?: "",
                                    category = doc.getString("category") ?: StoreExpense.CATEGORY_BAHAN_CUCI,
                                    amount = doc.getLong("amount") ?: 0L,
                                    timestamp = doc.getLong("timestamp") ?: System.currentTimeMillis(),
                                    recordedBy = doc.getString("recordedBy") ?: "Kasir",
                                    note = doc.getString("note") ?: ""
                                )
                            } catch (e: Exception) {
                                Log.e(TAG, "Error parsing expense doc ${doc.id}", e)
                                null
                            }
                        }
                        trySend(expenses)
                    }
                }
            awaitClose { listenerRegistration.remove() }
        }
    }

    suspend fun saveExpense(
        expense: StoreExpense,
        branchId: String = DEFAULT_BRANCH
    ): Result<Unit> {
        val collection = getExpensesCollection(branchId)
            ?: return Result.failure(IllegalStateException(OFFLINE_MESSAGE))

        return try {
            val docId = "exp_${expense.timestamp}"
            val data = hashMapOf(
                "localId" to expense.id,
                "title" to expense.title,
                "category" to expense.category,
                "amount" to expense.amount,
                "timestamp" to expense.timestamp,
                "recordedBy" to expense.recordedBy,
                "note" to expense.note,
                "syncTimestamp" to System.currentTimeMillis()
            )
            collection.document(docId).set(data).awaitTask()
            Log.d(TAG, "Expense synced to Firestore: ${expense.title}")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to sync expense to Firestore", e)
            Result.failure(e)
        }
    }

    suspend fun deleteExpenseRemote(
        timestamp: Long,
        branchId: String = DEFAULT_BRANCH
    ): Result<Unit> {
        val collection = getExpensesCollection(branchId)
            ?: return Result.failure(IllegalStateException(OFFLINE_MESSAGE))

        return try {
            val docId = "exp_$timestamp"
            collection.document(docId).delete().awaitTask()
            Log.d(TAG, "Expense deleted from Firestore: $docId")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to delete expense from Firestore", e)
            Result.failure(e)
        }
    }

    // --- User Presence & Activity Sync ---

    suspend fun updateUserPresence(
        presence: UserPresence,
        branchId: String = DEFAULT_BRANCH
    ): Result<Unit> {
        val collection = getActiveUsersCollection(branchId)
            ?: return Result.failure(IllegalStateException(OFFLINE_MESSAGE))

        val docId = if (presence.userId.isNotBlank()) presence.userId else "user_${presence.userName.hashCode()}"

        return try {
            val data = hashMapOf(
                "userId" to docId,
                "userName" to presence.userName,
                "role" to presence.role,
                "lastActiveTime" to presence.lastActiveTime,
                "lastLoginTime" to presence.lastLoginTime,
                "deviceModel" to presence.deviceModel,
                "isOnline" to true
            )
            collection.document(docId).set(data).awaitTask()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun listenActiveUsers(branchId: String = DEFAULT_BRANCH): Flow<List<UserPresence>> {
        val collection = getActiveUsersCollection(branchId)
            ?: return flowOf(emptyList())

        return callbackFlow {
            val listenerRegistration = collection.addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "Listen active users failed", error)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val users = snapshot.documents.mapNotNull { doc ->
                        try {
                            UserPresence(
                                userId = doc.getString("userId") ?: doc.id,
                                userName = doc.getString("userName") ?: "",
                                role = doc.getString("role") ?: "",
                                lastActiveTime = doc.getLong("lastActiveTime") ?: 0L,
                                lastLoginTime = doc.getLong("lastLoginTime") ?: 0L,
                                deviceModel = doc.getString("deviceModel") ?: "",
                                isOnline = doc.getBoolean("isOnline") ?: true
                            )
                        } catch (e: Exception) {
                            null
                        }
                    }
                    trySend(users)
                }
            }
            awaitClose { listenerRegistration.remove() }
        }
    }

    // --- Private Helper Extensions ---

    private fun WashRecord.toFirestoreMap(documentId: String): Map<String, Any> {
        return hashMapOf(
            "documentId" to documentId,
            "localId" to id,
            "motorCount" to motorCount,
            "licensePlate" to licensePlate,
            "motorType" to motorType,
            "washerName" to washerName,
            "pricePerMotor" to pricePerMotor,
            "washerSharePerMotor" to washerSharePerMotor,
            "totalPrice" to totalPrice,
            "totalWasherShare" to totalWasherShare,
            "totalOwnerShare" to totalOwnerShare,
            "paymentMethod" to paymentMethod,
            "note" to note,
            "timestamp" to timestamp,
            "createdBy" to createdBy,
            "validationStatus" to validationStatus,
            "disputeReason" to disputeReason,
            "disputedBy" to disputedBy,
            "disputedAt" to disputedAt,
            "syncTimestamp" to System.currentTimeMillis()
        )
    }

    /**
     * Broadcasts a push notification message to all connected devices in real-time.
     */
    fun sendBroadcastPush(
        title: String,
        message: String,
        senderName: String,
        branchId: String = DEFAULT_BRANCH
    ): Task<Void>? {
        val firestore = getFirestore() ?: return null
        val pushId = "push_${System.currentTimeMillis()}"
        val data = mapOf(
            "id" to pushId,
            "title" to title,
            "message" to message,
            "senderName" to senderName,
            "timestamp" to System.currentTimeMillis()
        )
        return firestore.collection(COLLECTION_BRANCHES)
            .document(branchId)
            .collection("broadcast_pushes")
            .document(pushId)
            .set(data)
    }

    /**
     * Real-time listener for incoming broadcast push notifications.
     */
    fun listenBroadcastPushes(branchId: String = DEFAULT_BRANCH): Flow<List<BroadcastPushMessage>> {
        val firestore = getFirestore() ?: return flowOf(emptyList())
        return callbackFlow {
            val listener = firestore.collection(COLLECTION_BRANCHES)
                .document(branchId)
                .collection("broadcast_pushes")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(10)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        trySend(emptyList())
                        return@addSnapshotListener
                    }
                    if (snapshot != null) {
                        val list = snapshot.documents.mapNotNull { doc ->
                            try {
                                BroadcastPushMessage(
                                    id = doc.getString("id") ?: doc.id,
                                    title = doc.getString("title") ?: "",
                                    message = doc.getString("message") ?: "",
                                    senderName = doc.getString("senderName") ?: "",
                                    timestamp = doc.getLong("timestamp") ?: 0L
                                )
                            } catch (_: Exception) {
                                null
                            }
                        }
                        trySend(list)
                    }
                }
            awaitClose { listener.remove() }
        }
    }

    private fun DocumentSnapshot.toWashRecord(): WashRecord {
        return WashRecord(
            id = getLong("localId") ?: 0L,
            motorCount = (getLong("motorCount") ?: 1L).toInt(),
            licensePlate = getString("licensePlate") ?: "",
            motorType = getString("motorType") ?: "Standar (Matic/Bebek)",
            washerName = getString("washerName") ?: "",
            pricePerMotor = getLong("pricePerMotor") ?: 10000L,
            washerSharePerMotor = getLong("washerSharePerMotor") ?: 5000L,
            totalPrice = getLong("totalPrice") ?: 10000L,
            totalWasherShare = getLong("totalWasherShare") ?: 5000L,
            totalOwnerShare = getLong("totalOwnerShare") ?: 5000L,
            paymentMethod = getString("paymentMethod") ?: "Tunai",
            note = getString("note") ?: "",
            timestamp = getLong("timestamp") ?: System.currentTimeMillis(),
            createdBy = getString("createdBy") ?: "Kasir",
            validationStatus = getString("validationStatus") ?: "PENDING",
            disputeReason = getString("disputeReason") ?: "",
            disputedBy = getString("disputedBy") ?: "",
            disputedAt = getLong("disputedAt") ?: 0L
        )
    }

    private suspend fun <T> Task<T>.awaitTask(): T = suspendCancellableCoroutine { cont ->
        addOnSuccessListener { result ->
            if (cont.isActive) cont.resume(result)
        }
        addOnFailureListener { exception ->
            if (cont.isActive) cont.resumeWithException(exception)
        }
        addOnCanceledListener {
            if (cont.isActive) cont.cancel()
        }
    }
}
