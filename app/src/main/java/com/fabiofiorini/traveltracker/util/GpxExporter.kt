package com.fabiofiorini.traveltracker.util

import android.content.ContentValues
import android.content.Context
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import com.fabiofiorini.traveltracker.data.RouteEntity
import com.fabiofiorini.traveltracker.data.RoutePointEntity
import java.text.SimpleDateFormat
import java.util.*

object GpxExporter {

    fun export(
        context: Context,
        route: RouteEntity,
        points: List<RoutePointEntity>
    ): String? {
        val gpxContent = buildGpx(route, points) ?: return null
        val fileName = sanitizeFileName(route.title) + ".gpx"

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            saveViaMediaStore(context, fileName, gpxContent)
        } else {
            saveToAppDir(context, fileName, gpxContent)
        }
    }

    private fun buildGpx(
        route: RouteEntity,
        points: List<RoutePointEntity>
    ): String? {
        if (points.isEmpty()) return null

        val dateFormat = SimpleDateFormat(
            "yyyy-MM-dd'T'HH:mm:ss'Z'",
            Locale.US
        )

        return buildString {
            appendLine("<?xml version=\"1.0\" encoding=\"UTF-8\"?>")
            appendLine("<gpx version=\"1.1\" creator=\"PathTracker\" xmlns=\"http://www.topografix.com/GPX/1/1\">")
            appendLine("  <trk>")
            appendLine("    <name>${escapeXml(route.title)}</name>")
            appendLine("    <trkseg>")
            for (pt in points) {
                appendLine("      <trkpt lat=\"${pt.lat}\" lon=\"${pt.lon}\">")
                appendLine("        <time>${dateFormat.format(Date(pt.timestamp))}</time>")
                appendLine("      </trkpt>")
            }
            appendLine("    </trkseg>")
            appendLine("  </trk>")
            appendLine("</gpx>")
        }
    }

    private fun saveViaMediaStore(
        context: Context,
        fileName: String,
        content: String
    ): String? {
        val values = ContentValues().apply {
            put(MediaStore.Downloads.DISPLAY_NAME, fileName)
            put(MediaStore.Downloads.MIME_TYPE, "application/gpx+xml")
            put(
                MediaStore.Downloads.RELATIVE_PATH,
                Environment.DIRECTORY_DOWNLOADS + "/PathTracker"
            )
        }
        val uri = context.contentResolver.insert(
            MediaStore.Downloads.EXTERNAL_CONTENT_URI,
            values
        ) ?: return null

        context.contentResolver.openOutputStream(uri)?.use { stream ->
            stream.write(content.toByteArray(Charsets.UTF_8))
        }
        return uri.toString()
    }

    private fun saveToAppDir(
        context: Context,
        fileName: String,
        content: String
    ): String? {
        val dir = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
            ?: return null
        val file = java.io.File(dir, fileName)
        file.writeBytes(content.toByteArray(Charsets.UTF_8))
        return file.absolutePath
    }

    private fun sanitizeFileName(title: String): String {
        return title.replace(Regex("[\\\\/:*?\"<>|]"), "_").take(100)
    }

    private fun escapeXml(s: String): String {
        return s
            .replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
            .replace("'", "&apos;")
    }
}
