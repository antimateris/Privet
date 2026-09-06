package com.example.util

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.print.PrintAttributes
import android.print.PrintManager
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import com.example.data.model.ValidationState
import com.example.data.model.WashRecord
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object ThermalReceiptUtils {

    const val BUSINESS_NAME = "LION STEAM MOTOR"
    const val BUSINESS_ADDRESS = "Layanan Cuci Motor Salju & Semir Ban"
    const val BUSINESS_PHONE = "WA: 0812-3456-7890"

    /**
     * Generates a 32-column monospace plain text receipt optimized for 58mm/80mm thermal printers.
     */
    fun generateReceiptText(
        record: WashRecord,
        businessName: String = BUSINESS_NAME,
        address: String = BUSINESS_ADDRESS,
        phone: String = BUSINESS_PHONE
    ): String {
        val cleanBusinessName = businessName
            .replace("PT.", "", ignoreCase = true)
            .replace("PT", "", ignoreCase = true)
            .trim()

        val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale("id", "ID"))
        val dateStr = sdf.format(Date(record.timestamp))
        val trxId = "TRX-${record.timestamp.toString().takeLast(6)}"

        val statusLabel = when (record.getValidationState()) {
            ValidationState.VALID_APPROVED -> "VALID (DISETUJUI)"
            ValidationState.VALID_AUTO_24H -> "VALID (AUTO 24 JAM)"
            ValidationState.PENDING_REVIEW -> "DALAM VERIFIKASI (24 JAM)"
            ValidationState.DISPUTED -> "DISANGGAH: ${record.disputeReason.take(15)}"
        }

        val plate = if (record.licensePlate.isNotBlank()) record.licensePlate else "-"
        val washer = if (record.washerName.isNotBlank()) record.washerName else "-"

        val lineSeparator = "--------------------------------"
        val doubleSeparator = "================================"

        return buildString {
            appendLine(doubleSeparator)
            appendLine(centerText(cleanBusinessName, 32))
            appendLine(centerText(address, 32))
            appendLine(centerText(phone, 32))
            appendLine(doubleSeparator)
            appendLine("No. Trx  : $trxId")
            appendLine("Waktu    : $dateStr")
            appendLine("Kasir    : ${record.createdBy}")
            appendLine("Status   : $statusLabel")
            appendLine(lineSeparator)
            appendLine("Plat No  : $plate")
            appendLine("Tipe     : ${record.motorType}")
            appendLine("Petugas  : $washer")
            appendLine("Jumlah   : ${record.motorCount} Motor")
            appendLine("Harga/Mtr: ${FormatUtils.formatRupiah(record.pricePerMotor)}")
            appendLine(lineSeparator)
            appendLine(formatRowTwoColumns("TOTAL BAYAR", FormatUtils.formatRupiah(record.totalPrice), 32))
            appendLine(formatRowTwoColumns("Metode Bayar", record.paymentMethod, 32))
            if (record.note.isNotBlank()) {
                appendLine("Catatan  : ${record.note}")
            }
            appendLine(doubleSeparator)
            appendLine(centerText("* Bersih, Wangi & Berkilau *", 32))
            appendLine(centerText("Terima Kasih Atas", 32))
            appendLine(centerText("Kunjungan Anda!", 32))
            appendLine(doubleSeparator)
            appendLine("\n\n") // Feed lines for thermal tear-off
        }
    }

    /**
     * Share formatted receipt text directly to Bluetooth Thermal Printer apps (RawBT, ESC POS, etc.)
     */
    fun shareToThermalPrinter(context: Context, record: WashRecord) {
        val receiptText = generateReceiptText(record)
        val sendIntent = Intent(Intent.ACTION_SEND).apply {
            putExtra(Intent.EXTRA_TEXT, receiptText)
            type = "text/plain"
        }
        val chooser = Intent.createChooser(sendIntent, "Cetak ke Printer Thermal / Bluetooth")
        context.startActivity(chooser)
    }

    /**
     * Copy receipt text to clipboard
     */
    fun copyReceiptToClipboard(context: Context, record: WashRecord) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("Struk Thermal Lion Steam", generateReceiptText(record))
        clipboard.setPrimaryClip(clip)
        Toast.makeText(context, "Teks struk thermal berhasil disalin!", Toast.LENGTH_SHORT).show()
    }

    /**
     * Print receipt using Android Native PrintManager (supports WiFi thermal printers, Mopria, PDF)
     */
    fun printReceiptNative(context: Context, record: WashRecord) {
        try {
            val cleanBusinessName = BUSINESS_NAME
                .replace("PT.", "", ignoreCase = true)
                .replace("PT", "", ignoreCase = true)
                .trim()

            val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale("id", "ID"))
            val dateStr = sdf.format(Date(record.timestamp))
            val trxId = "TRX-${record.timestamp.toString().takeLast(6)}"
            val plate = if (record.licensePlate.isNotBlank()) record.licensePlate else "-"
            val washer = if (record.washerName.isNotBlank()) record.washerName else "-"

            val htmlContent = """
                <!DOCTYPE html>
                <html>
                <head>
                    <meta charset="utf-8">
                    <style>
                        @page {
                            size: 58mm auto;
                            margin: 0;
                        }
                        body {
                            font-family: 'Courier New', Courier, monospace;
                            width: 48mm;
                            margin: 0 auto;
                            padding: 8px 4px;
                            color: #000;
                            font-size: 11px;
                            line-height: 1.25;
                        }
                        .text-center { text-align: center; }
                        .title { font-weight: bold; font-size: 13px; margin-bottom: 2px; }
                        .subtitle { font-size: 9px; margin-bottom: 2px; }
                        .divider { border-top: 1px dashed #000; margin: 5px 0; }
                        .double-divider { border-top: 1px solid #000; border-bottom: 1px solid #000; height: 2px; margin: 5px 0; }
                        .row { display: flex; justify-content: space-between; }
                        .bold { font-weight: bold; }
                        .footer { margin-top: 8px; font-size: 9px; text-align: center; }
                    </style>
                </head>
                <body>
                    <div class="double-divider"></div>
                    <div class="text-center title">$cleanBusinessName</div>
                    <div class="text-center subtitle">$BUSINESS_ADDRESS</div>
                    <div class="text-center subtitle">$BUSINESS_PHONE</div>
                    <div class="double-divider"></div>

                    <div>No. Trx: $trxId</div>
                    <div>Waktu  : $dateStr</div>
                    <div>Kasir  : ${record.createdBy}</div>
                    <div class="divider"></div>

                    <div>Plat   : $plate</div>
                    <div>Tipe   : ${record.motorType}</div>
                    <div>Petugas: $washer</div>
                    <div>Jumlah : ${record.motorCount} Motor</div>
                    <div class="row">
                        <span>Harga/Mtr:</span>
                        <span>${FormatUtils.formatRupiah(record.pricePerMotor)}</span>
                    </div>
                    <div class="divider"></div>

                    <div class="row bold" style="font-size: 12px;">
                        <span>TOTAL:</span>
                        <span>${FormatUtils.formatRupiah(record.totalPrice)}</span>
                    </div>
                    <div class="row">
                        <span>Bayar:</span>
                        <span>${record.paymentMethod}</span>
                    </div>
                    <div class="double-divider"></div>

                    <div class="footer">
                        <div>* Bersih, Wangi & Kilap *</div>
                        <div>Terima kasih atas kunjungan Anda!</div>
                    </div>
                </body>
                </html>
            """.trimIndent()

            val webView = WebView(context)
            webView.webViewClient = object : WebViewClient() {
                override fun onPageFinished(view: WebView?, url: String?) {
                    val printManager = context.getSystemService(Context.PRINT_SERVICE) as PrintManager
                    val printAdapter = webView.createPrintDocumentAdapter("Struk_${trxId}")
                    val printAttributes = PrintAttributes.Builder()
                        .setMediaSize(PrintAttributes.MediaSize.ISO_A6)
                        .setResolution(PrintAttributes.Resolution("thermal", "Thermal Printer", 203, 203))
                        .setMinMargins(PrintAttributes.Margins.NO_MARGINS)
                        .build()

                    printManager.print("Struk_$trxId", printAdapter, printAttributes)
                }
            }
            webView.loadDataWithBaseURL(null, htmlContent, "text/html", "UTF-8", null)
        } catch (e: Exception) {
            Toast.makeText(context, "Gagal membuka print manager: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun centerText(text: String, width: Int): String {
        if (text.length >= width) return text.take(width)
        val totalPadding = width - text.length
        val leftPadding = totalPadding / 2
        val rightPadding = totalPadding - leftPadding
        return " ".repeat(leftPadding) + text + " ".repeat(rightPadding)
    }

    private fun formatRowTwoColumns(left: String, right: String, totalWidth: Int): String {
        val space = totalWidth - left.length - right.length
        return if (space > 0) {
            left + " ".repeat(space) + right
        } else {
            "$left $right"
        }
    }
}
