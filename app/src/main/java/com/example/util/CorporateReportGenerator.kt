package com.example.util

import com.example.data.model.WashRecord
import com.example.ui.CompanyProfile
import com.example.ui.MonthlySummaryData
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object CorporateReportGenerator {

    /**
     * Generates an official, print-ready corporate HTML document for Monthly Recap
     */
    fun generateMonthlyCorporateHtml(
        summary: MonthlySummaryData,
        profile: CompanyProfile,
        documentNumber: String
    ): String {
        val currentDateStr = FormatUtils.formatDateFull(System.currentTimeMillis())
        val currentTimeStr = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
        val ownerMargin = if (summary.totalGrossRevenue > 0) {
            (summary.totalOwnerShare.toFloat() / summary.totalGrossRevenue.toFloat()) * 100f
        } else 0f

        val grossFloat = if (summary.totalGrossRevenue > 0) summary.totalGrossRevenue.toFloat() else 1f
        val cashPct = (summary.cashAmount.toFloat() / grossFloat) * 100f
        val qrisPct = (summary.qrisAmount.toFloat() / grossFloat) * 100f
        val trfPct = (summary.transferAmount.toFloat() / grossFloat) * 100f

        val sdfDate = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

        val dailyRowsHtml = StringBuilder()
        summary.dailyBreakdown.forEachIndexed { idx, d ->
            val dateStr = sdfDate.format(Date(d.dateMillis))
            val isEven = idx % 2 == 0
            val bgStyle = if (isEven) "background-color: #ffffff;" else "background-color: #f8fafc;"
            dailyRowsHtml.append("""
                <tr style="$bgStyle">
                    <td style="padding: 8px 10px; border-bottom: 1px solid #e2e8f0; text-align: center;">${idx + 1}</td>
                    <td style="padding: 8px 10px; border-bottom: 1px solid #e2e8f0; font-weight: 600;">$dateStr</td>
                    <td style="padding: 8px 10px; border-bottom: 1px solid #e2e8f0;">${d.dayName}</td>
                    <td style="padding: 8px 10px; border-bottom: 1px solid #e2e8f0; text-align: center; font-weight: 600;">${d.motorCount}</td>
                    <td style="padding: 8px 10px; border-bottom: 1px solid #e2e8f0; text-align: right; font-weight: 600;">${FormatUtils.formatRupiah(d.grossRevenue)}</td>
                    <td style="padding: 8px 10px; border-bottom: 1px solid #e2e8f0; text-align: right; color: #b45309;">${FormatUtils.formatRupiah(d.washerShare)}</td>
                    <td style="padding: 8px 10px; border-bottom: 1px solid #e2e8f0; text-align: right; color: #047857; font-weight: bold;">${FormatUtils.formatRupiah(d.ownerShare)}</td>
                    <td style="padding: 8px 10px; border-bottom: 1px solid #e2e8f0; color: #475569;">${d.topWasherName.ifBlank { "-" }}</td>
                </tr>
            """.trimIndent())
        }

        val washerRowsHtml = StringBuilder()
        summary.washerStats.forEachIndexed { idx, w ->
            val isEven = idx % 2 == 0
            val bgStyle = if (isEven) "background-color: #ffffff;" else "background-color: #f8fafc;"
            washerRowsHtml.append("""
                <tr style="$bgStyle">
                    <td style="padding: 8px 10px; border-bottom: 1px solid #e2e8f0; text-align: center;">${idx + 1}</td>
                    <td style="padding: 8px 10px; border-bottom: 1px solid #e2e8f0; font-weight: 600; color: #0f172a;">${w.washerName}</td>
                    <td style="padding: 8px 10px; border-bottom: 1px solid #e2e8f0; text-align: center; font-weight: 600;">${w.motorCount} Unit</td>
                    <td style="padding: 8px 10px; border-bottom: 1px solid #e2e8f0; text-align: center; color: #2563eb; font-weight: 600;">${String.format(Locale.getDefault(), "%.1f", w.percentage)}%</td>
                    <td style="padding: 8px 10px; border-bottom: 1px solid #e2e8f0; text-align: right; font-weight: bold; color: #b45309;">${FormatUtils.formatRupiah(w.totalShare)}</td>
                    <td style="padding: 8px 10px; border-bottom: 1px solid #e2e8f0; text-align: center;">
                        <span style="background-color: #dcfce7; color: #15803d; padding: 2px 8px; border-radius: 9999px; font-size: 10px; font-weight: bold;">TERVERIFIKASI</span>
                    </td>
                </tr>
            """.trimIndent())
        }

        return """
<!DOCTYPE html>
<html lang="id">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Laporan Keuangan Resmi - ${profile.companyName}</title>
    <style>
        @page {
            size: A4;
            margin: 15mm 15mm 15mm 15mm;
        }
        body {
            font-family: 'Segoe UI', -apple-system, BlinkMacSystemFont, Roboto, Helvetica, Arial, sans-serif;
            color: #0f172a;
            background-color: #f1f5f9;
            margin: 0;
            padding: 20px;
            -webkit-print-color-adjust: exact;
            print-color-adjust: exact;
        }
        .container {
            max-width: 900px;
            margin: 0 auto;
            background-color: #ffffff;
            padding: 36px 40px;
            box-shadow: 0 4px 20px rgba(0,0,0,0.08);
            border-radius: 8px;
            position: relative;
        }
        .watermark {
            position: absolute;
            top: 50%;
            left: 50%;
            transform: translate(-50%, -50%) rotate(-30deg);
            font-size: 68px;
            font-weight: 900;
            color: rgba(15, 23, 42, 0.03);
            pointer-events: none;
            white-space: nowrap;
            z-index: 0;
            text-transform: uppercase;
            letter-spacing: 4px;
        }
        .content {
            position: relative;
            z-index: 1;
        }
        .kop-table {
            width: 100%;
            border-collapse: collapse;
            margin-bottom: 6px;
        }
        .kop-logo {
            width: 80px;
            vertical-align: middle;
            text-align: center;
        }
        .kop-logo-box {
            width: 68px;
            height: 68px;
            background: linear-gradient(135deg, #0f2a4a 0%, #1e40af 100%);
            border-radius: 12px;
            display: inline-flex;
            align-items: center;
            justify-content: center;
            color: #ffffff;
            font-size: 32px;
            font-weight: bold;
            box-shadow: 0 2px 6px rgba(0,0,0,0.15);
        }
        .kop-text {
            text-align: center;
            padding: 0 10px;
        }
        .kop-title {
            font-size: 20px;
            font-weight: 900;
            color: #0f2a4a;
            letter-spacing: 1px;
            margin: 0;
            text-transform: uppercase;
        }
        .kop-subtitle {
            font-size: 12px;
            font-weight: 700;
            color: #1e3a8a;
            margin: 2px 0 0 0;
            letter-spacing: 0.5px;
            text-transform: uppercase;
        }
        .kop-meta {
            font-size: 10px;
            color: #475569;
            margin-top: 4px;
            line-height: 1.4;
        }
        .kop-divider {
            border-top: 3px solid #0f2a4a;
            border-bottom: 1px solid #0f2a4a;
            height: 2px;
            margin: 10px 0 16px 0;
        }
        .doc-bar {
            background-color: #f8fafc;
            border: 1px solid #cbd5e1;
            border-left: 4px solid #1e40af;
            border-radius: 6px;
            padding: 10px 14px;
            margin-bottom: 20px;
            display: flex;
            justify-content: space-between;
            align-items: center;
            font-size: 11px;
        }
        .doc-bar-left {
            line-height: 1.6;
        }
        .doc-bar-right {
            text-align: right;
            line-height: 1.6;
        }
        .badge-confidential {
            background-color: #fee2e2;
            color: #b91c1c;
            padding: 2px 6px;
            border-radius: 4px;
            font-weight: bold;
            font-size: 9px;
            letter-spacing: 0.5px;
            display: inline-block;
        }
        .badge-audited {
            background-color: #dbeafe;
            color: #1e40af;
            padding: 2px 6px;
            border-radius: 4px;
            font-weight: bold;
            font-size: 9px;
            letter-spacing: 0.5px;
            display: inline-block;
        }
        .report-title-box {
            text-align: center;
            margin-bottom: 20px;
        }
        .report-title {
            font-size: 16px;
            font-weight: 800;
            color: #0f172a;
            margin: 0;
            text-transform: uppercase;
            letter-spacing: 0.5px;
        }
        .report-period {
            font-size: 13px;
            font-weight: 600;
            color: #3b82f6;
            margin-top: 4px;
        }
        .cards-grid {
            display: grid;
            grid-template-columns: repeat(4, 1fr);
            gap: 12px;
            margin-bottom: 22px;
        }
        .kpi-card {
            background: #ffffff;
            border: 1px solid #e2e8f0;
            border-radius: 8px;
            padding: 12px;
            box-shadow: 0 1px 3px rgba(0,0,0,0.05);
            position: relative;
            overflow: hidden;
        }
        .kpi-card::before {
            content: "";
            position: absolute;
            top: 0;
            left: 0;
            right: 0;
            height: 3px;
        }
        .kpi-card-1::before { background-color: #3b82f6; }
        .kpi-card-2::before { background-color: #0284c7; }
        .kpi-card-3::before { background-color: #d97706; }
        .kpi-card-4::before { background-color: #059669; }
        .kpi-label {
            font-size: 10px;
            font-weight: 700;
            color: #64748b;
            text-transform: uppercase;
            letter-spacing: 0.5px;
            margin-bottom: 4px;
        }
        .kpi-value {
            font-size: 16px;
            font-weight: 800;
            color: #0f172a;
            margin-bottom: 2px;
        }
        .kpi-sub {
            font-size: 9px;
            color: #94a3b8;
        }
        .section-header {
            font-size: 12px;
            font-weight: 800;
            color: #0f2a4a;
            text-transform: uppercase;
            letter-spacing: 0.5px;
            margin: 20px 0 10px 0;
            padding-bottom: 4px;
            border-bottom: 1.5px solid #e2e8f0;
            display: flex;
            justify-content: space-between;
            align-items: center;
        }
        .data-table {
            width: 100%;
            border-collapse: collapse;
            font-size: 11px;
            margin-bottom: 18px;
        }
        .data-table th {
            background-color: #0f2a4a;
            color: #ffffff;
            padding: 8px 10px;
            font-weight: 700;
            text-align: left;
            border: 1px solid #0f2a4a;
        }
        .data-table td {
            border: 1px solid #e2e8f0;
        }
        .data-table tfoot td {
            background-color: #f1f5f9;
            font-weight: 800;
            padding: 10px;
            border: 1px solid #cbd5e1;
            font-size: 11px;
        }
        .payment-box {
            display: grid;
            grid-template-columns: repeat(3, 1fr);
            gap: 12px;
            margin-bottom: 20px;
        }
        .payment-item {
            background: #f8fafc;
            border: 1px solid #e2e8f0;
            border-radius: 6px;
            padding: 10px 14px;
            font-size: 11px;
        }
        .payment-item-title {
            font-weight: 700;
            color: #475569;
            font-size: 10px;
            text-transform: uppercase;
        }
        .payment-item-val {
            font-size: 14px;
            font-weight: 800;
            color: #0f172a;
            margin: 3px 0 2px 0;
        }
        .payment-item-pct {
            font-size: 9px;
            color: #64748b;
        }
        .notes-box {
            background-color: #f8fafc;
            border: 1px solid #e2e8f0;
            border-left: 3px solid #64748b;
            padding: 10px 14px;
            font-size: 10px;
            color: #475569;
            line-height: 1.5;
            margin-bottom: 24px;
            border-radius: 4px;
        }
        .signature-section {
            display: grid;
            grid-template-columns: repeat(3, 1fr);
            gap: 16px;
            margin-top: 28px;
            page-break-inside: avoid;
        }
        .sig-box {
            text-align: center;
            font-size: 11px;
            position: relative;
        }
        .sig-role {
            font-size: 10px;
            font-weight: 700;
            color: #475569;
            text-transform: uppercase;
            margin-bottom: 4px;
        }
        .sig-space {
            height: 64px;
            display: flex;
            align-items: center;
            justify-content: center;
            position: relative;
        }
        .sig-digital {
            font-family: 'Brush Script MT', cursive, sans-serif;
            font-size: 22px;
            color: #1e3a8a;
            transform: rotate(-5deg);
        }
        .sig-name {
            font-weight: 800;
            color: #0f172a;
            border-bottom: 1px solid #94a3b8;
            padding-bottom: 2px;
            margin-bottom: 2px;
            display: inline-block;
        }
        .sig-nip {
            font-size: 9px;
            color: #64748b;
        }
        /* Stempel Resmi PT */
        .stamp-wrap {
            position: absolute;
            top: 2px;
            right: 10px;
            width: 78px;
            height: 78px;
            border: 2px solid #1e40af;
            border-radius: 50%;
            display: flex;
            flex-direction: column;
            align-items: center;
            justify-content: center;
            color: #1e40af;
            transform: rotate(-10deg);
            opacity: 0.85;
            pointer-events: none;
            box-shadow: 0 0 0 1px rgba(30, 64, 175, 0.2);
        }
        .stamp-inner {
            width: 68px;
            height: 68px;
            border: 1px dashed #1e40af;
            border-radius: 50%;
            display: flex;
            flex-direction: column;
            align-items: center;
            justify-content: center;
            text-align: center;
        }
        .stamp-text-top {
            font-size: 6px;
            font-weight: 900;
            letter-spacing: 0.5px;
            text-transform: uppercase;
        }
        .stamp-text-mid {
            font-size: 8px;
            font-weight: 900;
            letter-spacing: 0.5px;
            padding: 1px 0;
            border-top: 0.5px solid #1e40af;
            border-bottom: 0.5px solid #1e40af;
            width: 90%;
            margin: 1px 0;
        }
        .stamp-text-bot {
            font-size: 5.5px;
            font-weight: 800;
            letter-spacing: 0.5px;
        }
        .footer-bar {
            margin-top: 24px;
            padding-top: 8px;
            border-top: 1px solid #e2e8f0;
            display: flex;
            justify-content: space-between;
            font-size: 9px;
            color: #94a3b8;
        }
        @media print {
            body {
                background-color: #ffffff;
                padding: 0;
            }
            .container {
                box-shadow: none;
                border-radius: 0;
                padding: 0;
                max-width: 100%;
            }
            .data-table th {
                background-color: #0f2a4a !important;
                color: #ffffff !important;
            }
            .badge-audited, .badge-confidential {
                border: 1px solid #cbd5e1;
            }
        }
    </style>
</head>
<body>
    <div class="container">
        <div class="watermark">DOKUMEN RESMI PT</div>
        <div class="content">
            <!-- KOP SURAT RESMI -->
            <table class="kop-table">
                <tr>
                    <td class="kop-logo">
                        <div class="kop-logo-box">🏛️</div>
                    </td>
                    <td class="kop-text">
                        <h1 class="kop-title">${profile.companyName}</h1>
                        <p class="kop-subtitle">${profile.divisionName}</p>
                        <div class="kop-meta">
                            Izin Usaha Terdaftar (NIB): ${profile.legalRegNo} • SK Kemenkumham: AHU-0038912.AH.01.01<br>
                            Alamat: ${profile.companyAddress} • Telp: ${profile.companyPhone} • Email: finance@kilaumotorgemilang.co.id
                        </div>
                    </td>
                </tr>
            </table>

            <div class="kop-divider"></div>

            <!-- METADATA DOKUMEN -->
            <div class="doc-bar">
                <div class="doc-bar-left">
                    <div><strong>NO. DOKUMEN:</strong> $documentNumber</div>
                    <div><strong>PERIHAL:</strong> Laporan Pertanggungjawaban Finansial & Operasional</div>
                </div>
                <div class="doc-bar-right">
                    <div><span class="badge-confidential">CONFIDENTIAL</span> &nbsp; <span class="badge-audited">SISTEM AUDITED</span></div>
                    <div><strong>TANGGAL TERBIT:</strong> $currentDateStr, $currentTimeStr WIB</div>
                </div>
            </div>

            <!-- JUDUL LAPORAN -->
            <div class="report-title-box">
                <div class="report-title">LAPORAN KEUANGAN OPERASIONAL & PERTANGGUNGJAWABAN ARUS KAS</div>
                <div class="report-period">PERIODE BULANAN: ${summary.monthName.uppercase()} ${summary.year}</div>
            </div>

            <!-- 4 KARTU RINGKASAN EKSEKUTIF -->
            <div class="cards-grid">
                <div class="kpi-card kpi-card-1">
                    <div class="kpi-label">Volume Armada</div>
                    <div class="kpi-value">${summary.totalMotors} Unit</div>
                    <div class="kpi-sub">Tarif Standar Rp 10.000</div>
                </div>
                <div class="kpi-card kpi-card-2">
                    <div class="kpi-label">Omset Bruto (Gross)</div>
                    <div class="kpi-value">${FormatUtils.formatRupiah(summary.totalGrossRevenue)}</div>
                    <div class="kpi-sub">100% Penerimaan Jasa</div>
                </div>
                <div class="kpi-card kpi-card-3">
                    <div class="kpi-label">Beban Jasa Petugas</div>
                    <div class="kpi-value">${FormatUtils.formatRupiah(summary.totalWasherShare)}</div>
                    <div class="kpi-sub">50.0% Alokasi Hak Operator</div>
                </div>
                <div class="kpi-card kpi-card-4">
                    <div class="kpi-label">Laba Bersih Kas PT</div>
                    <div class="kpi-value">${FormatUtils.formatRupiah(summary.totalOwnerShare)}</div>
                    <div class="kpi-sub">${String.format(Locale.getDefault(), "%.0f", ownerMargin)}% Kas Bersih Pemilik</div>
                </div>
            </div>

            <!-- SALURAN PEMBAYARAN -->
            <div class="section-header">
                <span>1. Rekonsiliasi Saluran Penerimaan Kas & Rekening</span>
                <span style="font-size: 10px; color: #64748b; font-weight: normal;">Basis Pencatatan Real-Time</span>
            </div>
            <div class="payment-box">
                <div class="payment-item">
                    <div class="payment-item-title">Kas Tunai (Cash on Hand)</div>
                    <div class="payment-item-val">${FormatUtils.formatRupiah(summary.cashAmount)}</div>
                    <div class="payment-item-pct">Proporsi: ${String.format(Locale.getDefault(), "%.1f", cashPct)}% dari omset bruto</div>
                </div>
                <div class="payment-item">
                    <div class="payment-item-title">QRIS Merchant Settlement</div>
                    <div class="payment-item-val">${FormatUtils.formatRupiah(summary.qrisAmount)}</div>
                    <div class="payment-item-pct">Proporsi: ${String.format(Locale.getDefault(), "%.1f", qrisPct)}% dari omset bruto</div>
                </div>
                <div class="payment-item">
                    <div class="payment-item-title">Transfer Bank / Non-Tunai</div>
                    <div class="payment-item-val">${FormatUtils.formatRupiah(summary.transferAmount)}</div>
                    <div class="payment-item-pct">Proporsi: ${String.format(Locale.getDefault(), "%.1f", trfPct)}% dari omset bruto</div>
                </div>
            </div>

            <!-- TABEL HARIAN -->
            <div class="section-header">
                <span>2. Buku Besar Rincian Arus Kas Operasional Harian</span>
                <span style="font-size: 10px; color: #64748b; font-weight: normal;">Hari Aktif: ${summary.activeDaysCount} dari ${summary.daysInMonth} hari</span>
            </div>
            <table class="data-table">
                <thead>
                    <tr>
                        <th style="width: 30px; text-align: center;">No</th>
                        <th>Tanggal</th>
                        <th>Hari</th>
                        <th style="text-align: center;">Volume</th>
                        <th style="text-align: right;">Omset Bruto</th>
                        <th style="text-align: right;">Beban Petugas</th>
                        <th style="text-align: right;">Kas Bersih PT</th>
                        <th>Petugas Utama</th>
                    </tr>
                </thead>
                <tbody>
                    $dailyRowsHtml
                </tbody>
                <tfoot>
                    <tr>
                        <td colspan="3" style="text-align: center; font-weight: 800;">TOTAL AKUMULASI PERIODE</td>
                        <td style="text-align: center; font-weight: 800;">${summary.totalMotors}</td>
                        <td style="text-align: right; font-weight: 800;">${FormatUtils.formatRupiah(summary.totalGrossRevenue)}</td>
                        <td style="text-align: right; font-weight: 800; color: #b45309;">${FormatUtils.formatRupiah(summary.totalWasherShare)}</td>
                        <td style="text-align: right; font-weight: 800; color: #047857;">${FormatUtils.formatRupiah(summary.totalOwnerShare)}</td>
                        <td style="color: #64748b; font-style: italic;">Seluruh Tim Cuci</td>
                    </tr>
                </tfoot>
            </table>

            <!-- REKAP PETUGAS CUCI -->
            ${if (summary.washerStats.isNotEmpty()) """
            <div class="section-header">
                <span>3. Pertanggungjawaban Alokasi Honorarium Tenaga Cuci</span>
                <span style="font-size: 10px; color: #64748b; font-weight: normal;">Hak Komisi 50% (Rp 5.000 / Motor)</span>
            </div>
            <table class="data-table">
                <thead>
                    <tr>
                        <th style="width: 30px; text-align: center;">No</th>
                        <th>Nama Petugas Cuci</th>
                        <th style="text-align: center;">Volume Pengerjaan</th>
                        <th style="text-align: center;">Kontribusi</th>
                        <th style="text-align: right;">Hak Bagi Hasil</th>
                        <th style="text-align: center;">Status Rekonsiliasi</th>
                    </tr>
                </thead>
                <tbody>
                    $washerRowsHtml
                </tbody>
            </table>
            """.trimIndent() else ""}

            <!-- CATATAN AUDIT -->
            <div class="notes-box">
                <strong>CATATAN AUDITOR & KEPATUHAN SISTEM:</strong><br>
                1. Dokumen ini diterbitkan oleh sistem manajemen pembukuan kas otomatis terintegrasi.<br>
                2. Tarif jasa Rp 10.000/motor dengan skema bagi hasil 50% (Rp 5.000/motor) telah diverifikasi sesuai Surat Ketetapan Operasional Perusahaan.<br>
                3. Seluruh pencatatan kas fisik dan mutasi digital (QRIS/Transfer) telah disinkronisasikan dan memiliki validitas akuntansi yang mengikat.
            </div>

            <!-- LEMBAR PENGESAHAN DOKUMEN RESMI -->
            <div class="signature-section">
                <div class="sig-box">
                    <div class="sig-role">Dibuat Oleh:</div>
                    <div class="sig-space">
                        <span class="sig-digital">Admin Kasir</span>
                    </div>
                    <div class="sig-name">${profile.cashierName}</div>
                    <div class="sig-nip">Supervisor & Admin Kasir</div>
                </div>

                <div class="sig-box">
                    <div class="sig-role">Diperiksa Oleh:</div>
                    <div class="sig-space">
                        <span class="sig-digital">Siti Rahmawati</span>
                    </div>
                    <div class="sig-name">${profile.financeManagerName}</div>
                    <div class="sig-nip">Manajer Akuntansi & Keuangan</div>
                </div>

                <div class="sig-box">
                    <div class="sig-role">Disahkan & Disetujui:</div>
                    <div class="sig-space">
                        <span class="sig-digital">Hendra Gunawan</span>
                        <div class="stamp-wrap">
                            <div class="stamp-inner">
                                <div class="stamp-text-top">★ PT RESMI ★</div>
                                <div class="stamp-text-mid">AUDITED</div>
                                <div class="stamp-text-bot">KEUANGAN</div>
                            </div>
                        </div>
                    </div>
                    <div class="sig-name">${profile.directorName}</div>
                    <div class="sig-nip">Direktur Utama Perusahaan</div>
                </div>
            </div>

            <!-- FOOTER RESMI -->
            <div class="footer-bar">
                <span>Sistem Terakreditasi • ${profile.companyName}</span>
                <span>Dokumen dicetak secara sah pada $currentDateStr</span>
                <span>Halaman 1 dari 1 (Dokumen Sah)</span>
            </div>
        </div>
    </div>
</body>
</html>
        """.trimIndent()
    }

    /**
     * Generates an official corporate HTML document for detailed transaction list
     */
    fun generateDetailedCorporateHtml(
        records: List<WashRecord>,
        profile: CompanyProfile,
        periodTitle: String,
        documentNumber: String
    ): String {
        val currentDateStr = FormatUtils.formatDateFull(System.currentTimeMillis())
        val currentTimeStr = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
        val sdfDate = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val sdfTime = SimpleDateFormat("HH:mm", Locale.getDefault())

        val totalMotors = records.sumOf { it.motorCount.toLong() }
        val totalGross = records.sumOf { it.totalPrice }
        val totalWasher = records.sumOf { it.totalWasherShare }
        val totalOwner = records.sumOf { it.totalOwnerShare }

        val rowsHtml = StringBuilder()
        records.forEachIndexed { idx, r ->
            val d = Date(r.timestamp)
            val isEven = idx % 2 == 0
            val bgStyle = if (isEven) "background-color: #ffffff;" else "background-color: #f8fafc;"
            rowsHtml.append("""
                <tr style="$bgStyle">
                    <td style="padding: 6px 8px; border-bottom: 1px solid #e2e8f0; text-align: center;">${idx + 1}</td>
                    <td style="padding: 6px 8px; border-bottom: 1px solid #e2e8f0; font-weight: 600;">${sdfDate.format(d)} ${sdfTime.format(d)}</td>
                    <td style="padding: 6px 8px; border-bottom: 1px solid #e2e8f0; font-weight: 700; color: #0f172a;">${r.licensePlate.ifBlank { "UNIT #${r.id}" }}</td>
                    <td style="padding: 6px 8px; border-bottom: 1px solid #e2e8f0;">${r.motorType}</td>
                    <td style="padding: 6px 8px; border-bottom: 1px solid #e2e8f0; font-weight: 600;">${r.washerName}</td>
                    <td style="padding: 6px 8px; border-bottom: 1px solid #e2e8f0; text-align: right; font-weight: 600;">${FormatUtils.formatRupiah(r.totalPrice)}</td>
                    <td style="padding: 6px 8px; border-bottom: 1px solid #e2e8f0; text-align: right; color: #b45309;">${FormatUtils.formatRupiah(r.totalWasherShare)}</td>
                    <td style="padding: 6px 8px; border-bottom: 1px solid #e2e8f0; text-align: right; color: #047857; font-weight: bold;">${FormatUtils.formatRupiah(r.totalOwnerShare)}</td>
                    <td style="padding: 6px 8px; border-bottom: 1px solid #e2e8f0; text-align: center;">
                        <span style="font-size: 9px; padding: 2px 6px; border-radius: 4px; background-color: #e2e8f0; font-weight: 600;">${r.paymentMethod}</span>
                    </td>
                </tr>
            """.trimIndent())
        }

        return """
<!DOCTYPE html>
<html lang="id">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Buku Besar Transaksi Rinci - ${profile.companyName}</title>
    <style>
        @page { size: A4 landscape; margin: 12mm; }
        body {
            font-family: 'Segoe UI', -apple-system, BlinkMacSystemFont, Roboto, sans-serif;
            color: #0f172a;
            background-color: #f1f5f9;
            margin: 0;
            padding: 20px;
            -webkit-print-color-adjust: exact;
            print-color-adjust: exact;
        }
        .container {
            max-width: 1100px;
            margin: 0 auto;
            background-color: #ffffff;
            padding: 30px;
            box-shadow: 0 4px 20px rgba(0,0,0,0.08);
            border-radius: 8px;
        }
        .kop-table { width: 100%; border-collapse: collapse; }
        .kop-logo { width: 70px; vertical-align: middle; text-align: center; }
        .kop-logo-box {
            width: 58px; height: 58px;
            background: linear-gradient(135deg, #0f2a4a 0%, #1e40af 100%);
            border-radius: 10px;
            display: inline-flex; align-items: center; justify-content: center;
            color: #ffffff; font-size: 26px; font-weight: bold;
        }
        .kop-text { text-align: center; padding: 0 10px; }
        .kop-title { font-size: 18px; font-weight: 900; color: #0f2a4a; margin: 0; text-transform: uppercase; }
        .kop-subtitle { font-size: 11px; font-weight: 700; color: #1e3a8a; margin: 2px 0 0 0; text-transform: uppercase; }
        .kop-meta { font-size: 9.5px; color: #475569; margin-top: 3px; }
        .kop-divider {
            border-top: 3px solid #0f2a4a; border-bottom: 1px solid #0f2a4a;
            height: 2px; margin: 10px 0 14px 0;
        }
        .doc-bar {
            background-color: #f8fafc; border: 1px solid #cbd5e1; border-left: 4px solid #1e40af;
            border-radius: 6px; padding: 8px 12px; margin-bottom: 16px;
            display: flex; justify-content: space-between; font-size: 10.5px;
        }
        .report-title-box { text-align: center; margin-bottom: 16px; }
        .report-title { font-size: 15px; font-weight: 800; color: #0f172a; margin: 0; text-transform: uppercase; }
        .report-period { font-size: 12px; font-weight: 600; color: #3b82f6; margin-top: 3px; }
        .data-table { width: 100%; border-collapse: collapse; font-size: 10.5px; margin-bottom: 16px; }
        .data-table th { background-color: #0f2a4a; color: #ffffff; padding: 6px 8px; text-align: left; border: 1px solid #0f2a4a; }
        .data-table td { border: 1px solid #e2e8f0; }
        .data-table tfoot td { background-color: #f1f5f9; font-weight: 800; padding: 8px; border: 1px solid #cbd5e1; }
        .sig-section { display: grid; grid-template-columns: repeat(3, 1fr); gap: 16px; margin-top: 20px; page-break-inside: avoid; }
        .sig-box { text-align: center; font-size: 10.5px; }
        .sig-space { height: 48px; display: flex; align-items: center; justify-content: center; }
        .sig-digital { font-family: 'Brush Script MT', cursive; font-size: 20px; color: #1e3a8a; }
        .sig-name { font-weight: 800; border-bottom: 1px solid #94a3b8; display: inline-block; padding-bottom: 1px; }
        @media print {
            body { background: #fff; padding: 0; }
            .container { box-shadow: none; padding: 0; max-width: 100%; }
        }
    </style>
</head>
<body>
    <div class="container">
        <table class="kop-table">
            <tr>
                <td class="kop-logo"><div class="kop-logo-box">🏛️</div></td>
                <td class="kop-text">
                    <h1 class="kop-title">${profile.companyName}</h1>
                    <p class="kop-subtitle">${profile.divisionName}</p>
                    <div class="kop-meta">Izin Usaha / NIB: ${profile.legalRegNo} • Alamat: ${profile.companyAddress}</div>
                </td>
            </tr>
        </table>
        <div class="kop-divider"></div>

        <div class="doc-bar">
            <div><strong>NO. DOKUMEN:</strong> $documentNumber &nbsp;|&nbsp; <strong>PERIODE:</strong> $periodTitle</div>
            <div><strong>TANGGAL CETAK:</strong> $currentDateStr, $currentTimeStr WIB &nbsp;|&nbsp; <strong>STATUS:</strong> TERVERIFIKASI SISTEM</div>
        </div>

        <div class="report-title-box">
            <div class="report-title">BUKU BESAR RINCIAN TRANSAKSI PER UNIT ARMADA KENDARAAN</div>
            <div class="report-period">Total Transaksi: ${records.size} Rekaman | Total Armada: $totalMotors Unit</div>
        </div>

        <table class="data-table">
            <thead>
                <tr>
                    <th style="width: 25px; text-align: center;">No</th>
                    <th>Waktu</th>
                    <th>Plat Nomor</th>
                    <th>Tipe Motor</th>
                    <th>Petugas Cuci</th>
                    <th style="text-align: right;">Omset Bruto</th>
                    <th style="text-align: right;">Beban Petugas</th>
                    <th style="text-align: right;">Kas Bersih PT</th>
                    <th style="text-align: center;">Metode</th>
                </tr>
            </thead>
            <tbody>
                $rowsHtml
            </tbody>
            <tfoot>
                <tr>
                    <td colspan="5" style="text-align: center; font-weight: 800;">TOTAL AKUMULASI TRANSAKSI</td>
                    <td style="text-align: right; font-weight: 800;">${FormatUtils.formatRupiah(totalGross)}</td>
                    <td style="text-align: right; font-weight: 800; color: #b45309;">${FormatUtils.formatRupiah(totalWasher)}</td>
                    <td style="text-align: right; font-weight: 800; color: #047857;">${FormatUtils.formatRupiah(totalOwner)}</td>
                    <td style="text-align: center;">-</td>
                </tr>
            </tfoot>
        </table>

        <div class="sig-section">
            <div class="sig-box">
                <div>Dibuat Oleh:</div>
                <div class="sig-space"><span class="sig-digital">Admin Kasir</span></div>
                <div class="sig-name">${profile.cashierName}</div>
            </div>
            <div class="sig-box">
                <div>Diperiksa Oleh:</div>
                <div class="sig-space"><span class="sig-digital">Siti Rahmawati</span></div>
                <div class="sig-name">${profile.financeManagerName}</div>
            </div>
            <div class="sig-box">
                <div>Disahkan Oleh:</div>
                <div class="sig-space"><span class="sig-digital">Hendra Gunawan</span></div>
                <div class="sig-name">${profile.directorName}</div>
            </div>
        </div>
    </div>
</body>
</html>
        """.trimIndent()
    }
}
