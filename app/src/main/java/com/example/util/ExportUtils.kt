package com.example.util

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.print.PrintAttributes
import android.print.PrintManager
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileWriter

object ExportUtils {

    /**
     * Prints or saves an HTML document as PDF using Android's native PrintManager.
     */
    fun printHtmlDocument(
        context: Context,
        jobName: String,
        htmlContent: String
    ): Boolean {
        return try {
            val mainHandler = Handler(Looper.getMainLooper())
            mainHandler.post {
                val printManager = context.getSystemService(Context.PRINT_SERVICE) as? PrintManager
                if (printManager == null) return@post

                val webView = WebView(context)
                webView.webViewClient = object : WebViewClient() {
                    override fun onPageFinished(view: WebView, url: String) {
                        val printAdapter = webView.createPrintDocumentAdapter(jobName)
                        val printAttributes = PrintAttributes.Builder()
                            .setMediaSize(PrintAttributes.MediaSize.ISO_A4)
                            .setResolution(PrintAttributes.Resolution("pdf", "pdf", 300, 300))
                            .setMinMargins(PrintAttributes.Margins.NO_MARGINS)
                            .build()
                        printManager.print(jobName, printAdapter, printAttributes)
                    }
                }
                webView.loadDataWithBaseURL(null, htmlContent, "text/html", "UTF-8", null)
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    /**
     * Shares an HTML document via Android ShareSheet (WhatsApp, Chrome, Drive, Gmail, etc.)
     */
    fun shareHtmlFile(
        context: Context,
        fileName: String,
        htmlContent: String,
        chooserTitle: String = "Bagikan Dokumen Laporan PT"
    ): Boolean {
        return try {
            val reportDir = File(context.cacheDir, "reports")
            if (!reportDir.exists()) {
                reportDir.mkdirs()
            }
            val safeFileName = if (fileName.endsWith(".html", ignoreCase = true)) fileName else "$fileName.html"
            val file = File(reportDir, safeFileName)
            FileWriter(file).use { writer ->
                writer.write(htmlContent)
            }

            val authority = "${context.packageName}.fileprovider"
            val fileUri = FileProvider.getUriForFile(context, authority, file)

            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "text/html"
                putExtra(Intent.EXTRA_STREAM, fileUri)
                putExtra(Intent.EXTRA_SUBJECT, safeFileName)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            val chooser = Intent.createChooser(intent, chooserTitle)
            chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(chooser)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    /**
     * Shares a CSV file via Android ShareSheet (WhatsApp, Excel, Drive, Gmail, etc.)
     */
    fun shareCsvFile(
        context: Context,
        fileName: String,
        csvContent: String,
        chooserTitle: String = "Bagikan File CSV Laporan"
    ): Boolean {
        return try {
            val reportDir = File(context.cacheDir, "reports")
            if (!reportDir.exists()) {
                reportDir.mkdirs()
            }
            val safeFileName = if (fileName.endsWith(".csv", ignoreCase = true)) fileName else "$fileName.csv"
            val file = File(reportDir, safeFileName)
            FileWriter(file).use { writer ->
                writer.write(csvContent)
            }

            val authority = "${context.packageName}.fileprovider"
            val fileUri = FileProvider.getUriForFile(context, authority, file)

            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "text/csv"
                putExtra(Intent.EXTRA_STREAM, fileUri)
                putExtra(Intent.EXTRA_SUBJECT, safeFileName)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            val chooser = Intent.createChooser(intent, chooserTitle)
            chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(chooser)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    /**
     * Shares plain text via Android ShareSheet (WhatsApp, Telegram, etc.)
     */
    fun shareText(
        context: Context,
        text: String,
        chooserTitle: String = "Bagikan Laporan Steam Motor"
    ) {
        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, text)
            type = "text/plain"
        }
        val chooser = Intent.createChooser(sendIntent, chooserTitle)
        chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(chooser)
    }

    /**
     * Copies text to system clipboard
     */
    fun copyToClipboard(context: Context, label: String, text: String) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText(label, text)
        clipboard.setPrimaryClip(clip)
    }
}
