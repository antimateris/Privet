package com.example.util

import com.example.data.model.AssetEntity
import com.example.data.model.BudgetItemEntity
import com.example.data.model.BudgetSettingEntity
import com.example.data.model.DebtEntity
import com.example.data.model.TransactionEntity
import com.example.data.model.WishlistItemEntity
import org.json.JSONArray
import org.json.JSONObject

/**
 * Kumpulan seluruh data aplikasi yang dapat diekspor/diimpor sebagai satu
 * berkas cadangan (backup) JSON.
 */
data class BackupPayload(
  val transactions: List<TransactionEntity>,
  val debts: List<DebtEntity>,
  val wishlists: List<WishlistItemEntity>,
  val assets: List<AssetEntity>,
  val budgetItems: List<BudgetItemEntity>,
  val budgetSetting: BudgetSettingEntity?
)

object BackupUtils {

  private const val BACKUP_FORMAT_VERSION = 1

  fun buildBackupJson(payload: BackupPayload): String {
    val root = JSONObject()
    root.put("formatVersion", BACKUP_FORMAT_VERSION)
    root.put("exportedAtMillis", System.currentTimeMillis())
    root.put("appName", "iFinance")

    val txArray = JSONArray()
    payload.transactions.forEach { tx ->
      txArray.put(
        JSONObject().apply {
          put("id", tx.id)
          put("title", tx.title)
          put("amount", tx.amount)
          put("type", tx.type)
          put("category", tx.category)
          put("dateMillis", tx.dateMillis)
          put("note", tx.note)
        }
      )
    }
    root.put("transactions", txArray)

    val debtArray = JSONArray()
    payload.debts.forEach { debt ->
      debtArray.put(
        JSONObject().apply {
          put("id", debt.id)
          put("personName", debt.personName)
          put("type", debt.type)
          put("amount", debt.amount)
          put("dueDateMillis", debt.dueDateMillis)
          put("notes", debt.notes)
          put("isPaid", debt.isPaid)
          put("paidDateMillis", debt.paidDateMillis ?: JSONObject.NULL)
          put("createdDateMillis", debt.createdDateMillis)
        }
      )
    }
    root.put("debts", debtArray)

    val wishlistArray = JSONArray()
    payload.wishlists.forEach { item ->
      wishlistArray.put(
        JSONObject().apply {
          put("id", item.id)
          put("name", item.name)
          put("targetAmount", item.targetAmount)
          put("savedAmount", item.savedAmount)
          put("targetDateMillis", item.targetDateMillis)
          put("category", item.category)
          put("notes", item.notes)
          put("isCompleted", item.isCompleted)
          put("createdDateMillis", item.createdDateMillis)
        }
      )
    }
    root.put("wishlists", wishlistArray)

    val assetArray = JSONArray()
    payload.assets.forEach { asset ->
      assetArray.put(
        JSONObject().apply {
          put("id", asset.id)
          put("name", asset.name)
          put("category", asset.category)
          put("value", asset.value)
          put("notes", asset.notes)
          put("dateAdded", asset.dateAdded)
        }
      )
    }
    root.put("assets", assetArray)

    val budgetItemArray = JSONArray()
    payload.budgetItems.forEach { item ->
      budgetItemArray.put(
        JSONObject().apply {
          put("id", item.id)
          put("name", item.name)
          put("category", item.category)
          put("allocatedAmount", item.allocatedAmount)
          put("isDeductedFromCash", item.isDeductedFromCash)
          put("colorHex", item.colorHex)
          put("iconName", item.iconName)
          put("notes", item.notes)
          put("createdAtMillis", item.createdAtMillis)
        }
      )
    }
    root.put("budgetItems", budgetItemArray)

    payload.budgetSetting?.let { setting ->
      root.put(
        "budgetSetting",
        JSONObject().apply {
          put("id", setting.id)
          put("monthlyBudget", setting.monthlyBudget)
          put("alertThresholdPercent", setting.alertThresholdPercent)
          put("notificationEnabled", setting.notificationEnabled)
          put("lastAlertDateMillis", setting.lastAlertDateMillis)
        }
      )
    }

    return root.toString(2)
  }

  /**
   * Parse string JSON hasil ekspor kembali menjadi [BackupPayload].
   * Melempar exception jika format tidak valid supaya pemanggil bisa
   * menampilkan pesan error yang jelas ke pengguna.
   */
  fun parseBackupJson(jsonString: String): BackupPayload {
    val root = JSONObject(jsonString)

    val transactions = mutableListOf<TransactionEntity>()
    val txArray = root.optJSONArray("transactions") ?: JSONArray()
    for (i in 0 until txArray.length()) {
      val o = txArray.getJSONObject(i)
      transactions.add(
        TransactionEntity(
          id = 0, // biarkan Room generate ulang ID agar tidak bentrok
          title = o.getString("title"),
          amount = o.getDouble("amount"),
          type = o.getString("type"),
          category = o.getString("category"),
          dateMillis = o.getLong("dateMillis"),
          note = o.optString("note", "")
        )
      )
    }

    val debts = mutableListOf<DebtEntity>()
    val debtArray = root.optJSONArray("debts") ?: JSONArray()
    for (i in 0 until debtArray.length()) {
      val o = debtArray.getJSONObject(i)
      debts.add(
        DebtEntity(
          id = 0,
          personName = o.getString("personName"),
          type = o.getString("type"),
          amount = o.getDouble("amount"),
          dueDateMillis = o.getLong("dueDateMillis"),
          notes = o.optString("notes", ""),
          isPaid = o.optBoolean("isPaid", false),
          paidDateMillis = if (o.isNull("paidDateMillis")) null else o.optLong("paidDateMillis"),
          createdDateMillis = o.optLong("createdDateMillis", System.currentTimeMillis())
        )
      )
    }

    val wishlists = mutableListOf<WishlistItemEntity>()
    val wishlistArray = root.optJSONArray("wishlists") ?: JSONArray()
    for (i in 0 until wishlistArray.length()) {
      val o = wishlistArray.getJSONObject(i)
      wishlists.add(
        WishlistItemEntity(
          id = 0,
          name = o.getString("name"),
          targetAmount = o.getDouble("targetAmount"),
          savedAmount = o.optDouble("savedAmount", 0.0),
          targetDateMillis = o.getLong("targetDateMillis"),
          category = o.optString("category", "Gadget"),
          notes = o.optString("notes", ""),
          isCompleted = o.optBoolean("isCompleted", false),
          createdDateMillis = o.optLong("createdDateMillis", System.currentTimeMillis())
        )
      )
    }

    val assets = mutableListOf<AssetEntity>()
    val assetArray = root.optJSONArray("assets") ?: JSONArray()
    for (i in 0 until assetArray.length()) {
      val o = assetArray.getJSONObject(i)
      assets.add(
        AssetEntity(
          id = 0,
          name = o.getString("name"),
          category = o.getString("category"),
          value = o.getDouble("value"),
          notes = o.optString("notes", ""),
          dateAdded = o.optLong("dateAdded", System.currentTimeMillis())
        )
      )
    }

    val budgetItems = mutableListOf<BudgetItemEntity>()
    val budgetItemArray = root.optJSONArray("budgetItems") ?: JSONArray()
    for (i in 0 until budgetItemArray.length()) {
      val o = budgetItemArray.getJSONObject(i)
      budgetItems.add(
        BudgetItemEntity(
          id = 0,
          name = o.getString("name"),
          category = o.getString("category"),
          allocatedAmount = o.getDouble("allocatedAmount"),
          isDeductedFromCash = o.optBoolean("isDeductedFromCash", true),
          colorHex = o.optString("colorHex", "#0A84FF"),
          iconName = o.optString("iconName", "category"),
          notes = o.optString("notes", ""),
          createdAtMillis = o.optLong("createdAtMillis", System.currentTimeMillis())
        )
      )
    }

    val budgetSetting = root.optJSONObject("budgetSetting")?.let { o ->
      BudgetSettingEntity(
        id = 1,
        monthlyBudget = o.optDouble("monthlyBudget", 5_000_000.0),
        alertThresholdPercent = o.optInt("alertThresholdPercent", 80),
        notificationEnabled = o.optBoolean("notificationEnabled", true),
        lastAlertDateMillis = o.optLong("lastAlertDateMillis", 0L)
      )
    }

    return BackupPayload(
      transactions = transactions,
      debts = debts,
      wishlists = wishlists,
      assets = assets,
      budgetItems = budgetItems,
      budgetSetting = budgetSetting
    )
  }
}
