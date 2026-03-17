package com.vector.verevcodex.presentation.reports

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import com.vector.verevcodex.domain.model.reports.ReportExport
import java.io.File

internal fun openReport(context: Context, report: ReportExport) {
    val uri = report.contentUri?.let { Uri.parse(it) } ?: reportUri(context, report)
    val intent = Intent(Intent.ACTION_VIEW).apply {
        setDataAndType(uri, report.mimeType)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    context.startActivity(intent)
}

internal fun shareReport(context: Context, report: ReportExport) {
    val uri = report.contentUri?.let { Uri.parse(it) } ?: reportUri(context, report)
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = report.mimeType
        putExtra(Intent.EXTRA_STREAM, uri)
        putExtra(Intent.EXTRA_SUBJECT, report.fileName)
        putExtra(Intent.EXTRA_TEXT, report.summary)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    context.startActivity(intent)
}

internal fun createSaveReportIntent(report: ReportExport): Intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
    addCategory(Intent.CATEGORY_OPENABLE)
    type = report.mimeType
    putExtra(Intent.EXTRA_TITLE, report.fileName)
}

internal fun saveReportToUri(context: Context, report: ReportExport, destinationUri: Uri): Result<Unit> = runCatching {
    val sourceStream = report.contentUri?.let { uri ->
        context.contentResolver.openInputStream(Uri.parse(uri))
    } ?: File(report.absolutePath).inputStream()
    context.contentResolver.openOutputStream(destinationUri)?.use { outputStream ->
        sourceStream.use { inputStream ->
            inputStream.copyTo(outputStream)
        }
    } ?: error("Unable to open destination for report export")
}

private fun reportUri(context: Context, report: ReportExport) =
    FileProvider.getUriForFile(
        context,
        "${context.packageName}.fileprovider",
        File(report.absolutePath),
    )
