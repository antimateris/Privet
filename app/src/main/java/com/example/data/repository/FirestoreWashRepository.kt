package com.example.data.repository

import android.content.Context
import android.util.Log
import com.example.data.model.WashRecord
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
