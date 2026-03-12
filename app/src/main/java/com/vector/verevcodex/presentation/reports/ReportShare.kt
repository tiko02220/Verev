package com.vector.verevcodex.presentation.reports

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import com.vector.verevcodex.domain.model.reports.ReportExport
import java.io.File

internal fun openReport(context: Context, report: ReportExport) {
    val uri = reportUri(context, report)
    val intent = Intent(Intent.ACTION_VIEW).apply {
        setDataAndType(uri, report.mimeType)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    context.startActivity(Intent.createChooser(intent, report.fileName))
}

internal fun shareReport(context: Context, report: ReportExport) {
    val uri = reportUri(context, report)
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = report.mimeType
        putExtra(Intent.EXTRA_STREAM, uri)
        putExtra(Intent.EXTRA_SUBJECT, report.fileName)
        putExtra(Intent.EXTRA_TEXT, report.summary)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    context.startActivity(Intent.createChooser(intent, report.fileName))
}

private fun reportUri(context: Context, report: ReportExport) =
    FileProvider.getUriForFile(
        context,
        "${context.packageName}.fileprovider",
        File(report.absolutePath),
    )
