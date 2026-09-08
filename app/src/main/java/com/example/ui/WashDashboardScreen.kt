@@
                 items(filteredRecords, key = { it.id }) { record ->
                     TransactionItemCard(
                         record = record,
                         currentUser = currentUser,
                         onEdit = {
                             editingRecord = it
                             showAddEditDialog = true
                         },
-                        onDelete = { rec ->
-                            // default delete behavior
-                            recordToDelete = rec
-                        },
+                        onDelete = { rec ->
+                            // trigger delete-with-undo from viewModel and show snackbar
+                            viewModel.deleteRecordWithUndo(rec) { success, message ->
+                                coroutineScope.launch {
+                                    val result = snackbarHostState.showSnackbar(message, actionLabel = "Undo")
+                                    if (result == androidx.compose.material3.SnackbarResult.ActionPerformed) {
+                                        viewModel.restoreRecord(rec)
+                                    }
+                                }
+                            }
+                        },
