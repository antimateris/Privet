package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entity representing an operational store expense for PT. Lion Steam Motor.
 * (e.g. Sabun salju, semir ban, listrik/air, bensin mesin steam, konsumsi, perawatan alat).
 */
@Entity(tableName = "store_expenses")
data class StoreExpense(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val category: String = CATEGORY_BAHAN_CUCI,
    val amount: Long,
    val timestamp: Long = System.currentTimeMillis(),
    val recordedBy: String = "Kasir",
    val note: String = ""
) {
    companion object {
        const val CATEGORY_BAHAN_CUCI = "Bahan Cuci (Sabun/Semir)"
        const val CATEGORY_LISTRIK_AIR = "Listrik & Air"
        const val CATEGORY_BENSIN = "Bahan Bakar Mesin"
        const val CATEGORY_PERLENGKAPAN = "Perlengkapan & Spon"
        const val CATEGORY_KONSUMSI = "Konsumsi Petugas"
        const val CATEGORY_PERAWATAN = "Perawatan Mesin Steam"
        const val CATEGORY_LAINNYA = "Operasional Lainnya"

        val ALL_CATEGORIES = listOf(
            CATEGORY_BAHAN_CUCI,
            CATEGORY_LISTRIK_AIR,
            CATEGORY_BENSIN,
            CATEGORY_PERLENGKAPAN,
            CATEGORY_KONSUMSI,
            CATEGORY_PERAWATAN,
            CATEGORY_LAINNYA
        )
    }
}
