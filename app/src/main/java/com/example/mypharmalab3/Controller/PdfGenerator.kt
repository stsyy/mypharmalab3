package com.example.mypharmalab3.Controller

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.util.Log
import com.example.mypharmalab3.Model.Medicine
import java.io.IOException
import java.util.Date

/**
 * Менеджер для генерации PDF файлов (S9) в общем хранилище (Shared Storage).
 * Использует встроенный PdfDocument.
 */
class PdfGenerator(private val context: Context) {

    private val TAG = "PdfGenerator"

    // Параметры страницы A4 (в postscript points, 1/72 дюйма)
    private val PAGE_WIDTH = 595
    private val PAGE_HEIGHT = 842

    // Отступы
    private val MARGIN_X = 40f
    private val MARGIN_Y = 40f
    private val LINE_HEIGHT = 30f
    private val TABLE_ROW_HEIGHT = 20f

    /**
     * Генерирует и записывает PDF-файл с каталогом лекарств.
     */
    fun generateAndWritePdf(uri: Uri, medicineList: List<Medicine>): Boolean {
        if (medicineList.isEmpty()) {
            Log.w(TAG, "Список лекарств пуст, PDF не будет создан.")
            return false
        }

        val document = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, 1).create()
        val page = document.startPage(pageInfo)
        val canvas = page.canvas

        // Рисуем содержимое на странице
        drawContent(canvas, medicineList)

        document.finishPage(page)

        return try {
            // Открываем OutputStream для записи по предоставленному Uri
            context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                document.writeTo(outputStream)
            } ?: throw IOException("Не удалось открыть поток для записи Uri.")

            Log.i(TAG, "PDF файл успешно записан по Uri: $uri")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Ошибка записи PDF файла: ${e.message}", e)
            false
        } finally {
            document.close()
        }
    }

    private fun drawContent(canvas: Canvas, medicineList: List<Medicine>) {
        var currentY = MARGIN_Y

        // 1. Заголовок
        val titlePaint = Paint().apply {
            textSize = 24f
            isFakeBoldText = true
        }
        canvas.drawText("Отчет по Цифровой Аптечке", MARGIN_X, currentY, titlePaint)
        currentY += LINE_HEIGHT

        // 2. Подзаголовок
        val subtitlePaint = Paint().apply {
            textSize = 12f
        }
        canvas.drawText("Дата генерации: ${Date()}", MARGIN_X, currentY, subtitlePaint)
        currentY += LINE_HEIGHT * 2

        // 3. Таблица: Заголовки столбцов
        val headerPaint = Paint().apply {
            textSize = 14f
            isFakeBoldText = true
            color = android.graphics.Color.DKGRAY
        }
        val textPaint = Paint().apply {
            textSize = 12f
        }

        val colName = MARGIN_X
        val colExpiry = MARGIN_X + 200
        val colReminder = MARGIN_X + 350

        // Отрисовка заголовков
        canvas.drawText("Название", colName, currentY, headerPaint)
        canvas.drawText("Срок годности", colExpiry, currentY, headerPaint)
        canvas.drawText("Напоминание", colReminder, currentY, headerPaint)

        currentY += TABLE_ROW_HEIGHT

        // 4. Таблица: Данные (ИСПРАВЛЕНО: forEach заменен на for для поддержки break)
        for (medicine in medicineList) {
            if (currentY > PAGE_HEIGHT - MARGIN_Y) {
                // Если место закончилось, прекращаем рисование.
                break // ⭐️ Теперь 'break' находится внутри цикла и работает корректно!
            }

            val reminderText = if (medicine.reminder) "Да" else "Нет"

            canvas.drawText(medicine.name, colName, currentY, textPaint)
            canvas.drawText(medicine.expiryDate, colExpiry, currentY, textPaint)
            canvas.drawText(reminderText, colReminder, currentY, textPaint)

            currentY += TABLE_ROW_HEIGHT
        }

        // 5. Итог
        currentY += LINE_HEIGHT
        canvas.drawText("Всего позиций: ${medicineList.size}", MARGIN_X, currentY, titlePaint)
    }
}