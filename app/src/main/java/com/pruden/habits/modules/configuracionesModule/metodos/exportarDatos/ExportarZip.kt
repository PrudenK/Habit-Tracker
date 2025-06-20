package com.pruden.habits.modules.configuracionesModule.metodos.exportarDatos

import android.content.Context
import java.io.*
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import com.pruden.habits.R
import com.pruden.habits.common.metodos.fechas.obtenerFechaActual

fun crearZipConArchivosYDirectorio(context: Context, habitosCSV: File, dataHabitosCSV: File, directorio: File): File {
    val zipFile = File(context.filesDir, context.getString(R.string.datos_habit_tracker_zip, obtenerFechaActual()))

    ZipOutputStream(BufferedOutputStream(FileOutputStream(zipFile))).use { zos ->
        listOf(habitosCSV, dataHabitosCSV).forEach { file ->
            FileInputStream(file).use { fis ->
                val entry = ZipEntry(file.name) // Nombre del archivo
                zos.putNextEntry(entry)
                fis.copyTo(zos)
                zos.closeEntry()
            }
        }

        agregarDirectorioAlZip(directorio, zos)
    }

    return zipFile
}

private fun agregarDirectorioAlZip(directorio: File, zos: ZipOutputStream) {
    directorio.listFiles()?.forEach { file ->
        FileInputStream(file).use { fis ->
            val entry = ZipEntry("${directorio.name}/${file.name}")
            zos.putNextEntry(entry)
            fis.copyTo(zos)
            zos.closeEntry()
        }
    }
}

fun descargarZip(context: Context, zipFile: File) {
    val zipUri = FileProvider.getUriForFile(
        context,
        "${context.packageName}.provider",
        zipFile
    )

    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "application/zip"
        putExtra(Intent.EXTRA_STREAM, zipUri)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }

    context.startActivity(Intent.createChooser(intent, "Descargar ZIP"))
}

fun descargarCSVFile(context: Context, file: File) {
    val uri: Uri = FileProvider.getUriForFile(
        context,
        "${context.packageName}.provider",
        file
    )

    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/csv"
        putExtra(Intent.EXTRA_STREAM, uri)
    }

    context.startActivity(Intent.createChooser(intent, "Compartir archivo CSV"))
}


fun crearZipConContenidoDeDirectorio(context: Context, directorio: File): File {
    val zipFile = File(context.filesDir, context.getString(R.string.datos_habitos_zip, obtenerFechaActual()))

    ZipOutputStream(BufferedOutputStream(FileOutputStream(zipFile))).use { zos ->
        // Agregar el contenido del directorio al ZIP
        agregarDirectorioAlZip(directorio, zos)
    }

    return zipFile
}

