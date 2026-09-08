@@
     private val firestoreRepository by lazy {
         FirestoreWashRepository(context = getApplication<Application>())
     }
+
+    // Temporary cache to support undoing deletions (timestamp -> record)
+    private val recentlyDeletedRecords = mutableMapOf<Long, WashRecord>()
@@
     fun deleteStoreExpense(
         expense: StoreExpense,
         onComplete: (Boolean, String) -> Unit = { _, _ -> }
     ) {
         viewModelScope.launch {
             repository.deleteExpense(expense)
             viewModelScope.launch(Dispatchers.IO) {
                 firestoreRepository.deleteExpenseRemote(expense.timestamp)
             }
             onComplete(true, "Pengeluaran toko berhasil dihapus!")
         }
     }
+
+    /**
+     * Deletes a WashRecord locally and stores a copy for potential undo.
+     * The onComplete callback receives (success, message) which the UI can show in a Snackbar.
+     */
+    fun deleteRecordWithUndo(record: WashRecord, onComplete: (Boolean, String) -> Unit = { _, _ -> }) {
+        viewModelScope.launch {
+            try {
+                // Cache the record so it can be restored if user taps Undo
+                recentlyDeletedRecords[record.timestamp] = record
+
+                // Delete from local DB immediately for responsive UI
+                repository.deleteRecord(record)
+
+                // Fire-and-forget: try to delete from cloud as well (best-effort)
+                viewModelScope.launch(Dispatchers.IO) {
+                    try {
+                        firestoreRepository.deleteTransaction(record)
+                    } catch (_: Exception) {
+                        // ignore cloud failures — local deletion already succeeded
+                    }
+                }
+
+                onComplete(true, "Transaksi dihapus")
+            } catch (e: Exception) {
+                recentlyDeletedRecords.remove(record.timestamp)
+                onComplete(false, "Gagal menghapus transaksi: ${e.message}")
+            }
+        }
+    }
+
+    /**
+     * Restores a previously deleted record (if available in cache).
+     */
+    fun restoreRecord(record: WashRecord, onComplete: (Boolean, String) -> Unit = { _, _ -> }) {
+        viewModelScope.launch {
+            try {
+                repository.insertRecord(record)
+                recentlyDeletedRecords.remove(record.timestamp)
+
+                viewModelScope.launch(Dispatchers.IO) {
+                    try {
+                        firestoreRepository.saveTransaction(record)
+                    } catch (_: Exception) {
+                        // ignore
+                    }
+                }
+
+                onComplete(true, "Transaksi dikembalikan")
+            } catch (e: Exception) {
+                onComplete(false, "Gagal mengembalikan transaksi: ${e.message}")
+            }
+        }
+    }
