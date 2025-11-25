package com.example.mypharmalab3.Controller

import android.content.Context
import android.net.Uri
import android.util.Log
import com.example.mypharmalab3.Model.Medicine
import java.io.*

class CsvFileHandler(private val context: Context) {

    private val TAG = "CsvFileHandler"
    private val HEADER = "name,expiryDate,reminder,seasonal\n"

    // 1. КОНВЕРТАЦИЯ: Список лекарств -> CSV строка
    fun listToCsv(medicineList: List<Medicine>): String {
        val sb = StringBuilder(HEADER)
        medicineList.forEach { medicine ->
            sb.append("${escapeCsvValue(medicine.name)},")
                .append("${escapeCsvValue(medicine.expiryDate)},")
                .append("${medicine.reminder},")
                .append("${medicine.seasonal}\n")
        }
        return sb.toString()
    }

    // 2. ЗАПИСЬ: Сохранение CSV строки по заданному Uri
    fun writeCsvFile(uri: Uri, csvData: String): Boolean {
        return try {
            // Открываем OutputStream для записи по предоставленному Uri
            context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                outputStream.write(csvData.toByteArray())
            } ?: throw IOException("Не удалось открыть поток для записи Uri.")

            Log.i(TAG, "CSV файл успешно записан по Uri: $uri")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Ошибка записи CSV файла: ${e.message}", e)
            false
        }
    }

    // 3. КОНВЕРТАЦИЯ: CSV строка -> Список лекарств
    fun csvToList(csvString: String): List<Medicine> {
        val lines = csvString.trim().lines().drop(1) // Пропускаем заголовок
        val medicines = mutableListOf<Medicine>()

        lines.forEach { line ->
            val parts = line.split(",").map { it.trim() }
            if (parts.size >= 4) {
                try {
                    val name = unescapeCsvValue(parts[0])
                    val expiry = unescapeCsvValue(parts[1])
                    val reminder = parts[2].toBoolean()
                    val seasonal = parts[3].toBoolean()

                    medicines.add(Medicine(name, expiry, reminder, seasonal))
                } catch (e: Exception) {
                    Log.e(TAG, "Пропуск строки CSV из-за ошибки парсинга: $line")
                }
            }
        }
        return medicines
    }

    // 4. ЧТЕНИЕ: Загрузка CSV строки по заданному Uri
    fun readCsvFile(uri: Uri): String? {
        return try {
            // Открываем InputStream для чтения по предоставленному Uri
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                inputStream.bufferedReader().use { it.readText() }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Ошибка чтения CSV файла: ${e.message}", e)
            null
        }
    }

    // Вспомогательная функция для экранирования запятых (если имя лекарства содержит запятую)
    private fun escapeCsvValue(value: String): String {
        if (value.contains(',')) {
            return "\"${value.replace("\"", "\"\"")}\""
        }
        return value
    }

    // Вспомогательная функция для обратного деэкранирования
    private fun unescapeCsvValue(value: String): String {
        if (value.startsWith("\"") && value.endsWith("\"")) {
            return value.substring(1, value.length - 1).replace("\"\"", "\"")
        }
        return value
    }
}