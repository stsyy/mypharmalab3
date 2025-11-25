package com.example.mypharmalab3.Controller

import com.example.mypharmalab3.Model.Medicine
import com.example.mypharmalab3.Model.MedicineModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class MedicineController(private val model: MedicineModel) {

    fun handleAddMedicine(
        name: String,
        expiryInput: String,
        reminder: Boolean,
        seasonal: Boolean
    ): String {
        if (name.isBlank() || expiryInput.isBlank()) {
            return "Введите все данные"
        }
        val formattedDate = formatAndValidateDate(expiryInput) ?: return "Неверный формат даты (дд.мм.гггг)"
        val expiryDate = parseDate(formattedDate) ?: return "Некорректная дата"

        if (!isFutureDate(expiryDate)) {
            return "Срок годности уже истёк"
        }

        val medicine = Medicine(
            name = name,
            expiryDate = formattedDate,
            reminder = reminder,
            seasonal = seasonal
        )

        val saveResult = model.saveMedicine(medicine)

        if (saveResult.startsWith("✅")) {
            return "✅ Лекарство добавлено:\n" +
                    "Название: $name\n" +
                    "Срок годности: $formattedDate\n" +
                    "Напоминание: ${if (reminder) "Включено" else "Нет"}\n" +
                    "Сезонные рекомендации: ${if (seasonal) "Да" else "Нет"}"
        }

        return saveResult

    }

    /**
     * Обрабатывает список импортированных лекарств, добавляя их в MedicineModel.
     * @param importedList Список лекарств, прочитанных из CSV.
     */
    fun handleImportMedicines(importedList: List<Medicine>) {

        importedList.forEach { medicine ->
            // 🚨 ВАЖНО: Используем твою модель для сохранения каждого элемента.
            // Убедись, что в MedicineModel есть метод addMedicine.
            model.saveMedicine(medicine)
        }
    }

    fun getUniqueMedicineNames(): List<String> {
        return model.getUniqueMedicineNames()
    }

    private fun formatAndValidateDate(input: String): String? {

        val digits = input.filter { it.isDigit() }
        if (digits.length < 8) return null

        val d = digits.substring(0, 2)
        val m = digits.substring(2, 4)
        val y = digits.substring(4, 8)

        val day = d.toIntOrNull() ?: return null
        val month = m.toIntOrNull() ?: return null
        val year = y.toIntOrNull() ?: return null

        if (year < 2000 || year > 2099) return null
        if (month !in 1..12) return null

        val maxDay = when (month) {
            1, 3, 5, 7, 8, 10, 12 -> 31
            4, 6, 9, 11 -> 30
            2 -> if (isLeapYear(year)) 29 else 28
            else -> return null
        }

        if (day !in 1..maxDay) return null

        val formattedDay = day.toString().padStart(2, '0')
        val formattedMonth = month.toString().padStart(2, '0')

        return "$formattedDay.$formattedMonth.$year"
    }

    private fun parseDate(dateStr: String): Date? {
        return try {
            val sdf = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
            sdf.isLenient = false
            sdf.parse(dateStr)
        } catch (e: Exception) {
            null
        }
    }

    private fun isFutureDate(date: Date): Boolean {
        val todayCal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val today = todayCal.time
        return !date.before(today)
    }

    private fun isLeapYear(year: Int): Boolean {
        return (year % 4 == 0 && year % 100 != 0) || (year % 400 == 0)
    }

    fun getMedicineList(): List<Medicine> {

        return model.getAllMedicines()
    }

    fun deleteMedicine(medicine: Medicine): Boolean {
        return model.deleteMedicine(medicine)
    }

    fun handleBarcodeScan(barcode: String): String {
        val info = model.getMedicineByBarcode(barcode)
        return if (info != null) {
            "📦 Найдено лекарство:\n$info\nШтрихкод: $barcode\nВведите срок годности"
        } else {
            "❌ Лекарство не найдено\nШтрихкод: $barcode\nВведите название вручную"
        }
    }
}