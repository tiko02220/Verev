package com.vector.verevcodex.data.repository.reports

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

internal object ReportFormatting {
    private val subtitleFormatter = DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm")
    private val transactionFormatter = DateTimeFormatter.ofPattern("dd MMM HH:mm")

    fun subtitle(storeName: String, generatedAt: LocalDateTime): String =
        "$storeName • ${generatedAt.format(subtitleFormatter)}"

    fun summary(totalCustomers: Int, totalRevenue: Double, averageTicket: Double): String =
        "Customers $totalCustomers, revenue ${currency(totalRevenue)}, avg ticket ${currency(averageTicket)}"

    fun currency(value: Double): String = String.format(Locale.US, "%.2f", value)

    fun percent(fraction: Double): String = String.format(Locale.US, "%.0f%%", fraction * 100)

    fun transactionTimestamp(value: LocalDateTime): String = value.format(transactionFormatter)
}
