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

        return model.saveMedicine(medicine)
    }

    fun formatExpiryInputForDisplay(rawInput: String): String {
        // Оставляем только цифры и точки в том порядке, как ввёл пользователь
        val filtered = rawInput.filter { it.isDigit() || it == '.' }

        // Список позиций, где пользователь поставил точку (в filtered)
        val dotPositions = filtered.mapIndexedNotNull { idx, ch -> if (ch == '.') idx else null }

        val digits = filtered.replace(".", "")

        // Буфер результата
        val out = StringBuilder()

        var digitIdx = 0

        // --- Обработка дня ---
        if (digitIdx < digits.length) {

            val firstDotPos = dotPositions.firstOrNull()
            if (firstDotPos == 1 && digits.length >= 1) {

                val dChar = digits[0]
                val dVal = dChar.toString().toIntOrNull() ?: 0
                if (dVal in 1..31) {
                    out.append(dVal.toString().padStart(2, '0')).append('.')
                    digitIdx = 1
                } else {
                    // недопустимый однозначный день — не добавляем
                    // оставляем пустым, пользователь увидит ничего
                    digitIdx = 1
                }
            } else {
                // берём до 2 цифр для дня
                val remaining = digits.length - digitIdx
                if (remaining >= 2) {
                    val dayRaw = digits.substring(digitIdx, digitIdx + 2)
                    val dVal = dayRaw.toIntOrNull() ?: -1
                    if (dVal in 1..31) {
                        out.append(dayRaw).append('.')
                        digitIdx += 2
                    } else {
                        // если два символа дают >31, используем только первый
                        val first = dayRaw[0]
                        val fVal = first.toString().toIntOrNull() ?: -1
                        if (fVal in 1..9) {
                            out.append(first)
                            digitIdx += 1
                        } else {
                            // некорректно — не добавляем
                            digitIdx += 1
                        }
                    }
                } else {
                    val dayRaw = digits.substring(digitIdx, digitIdx + 1)
                    val dVal = dayRaw.toIntOrNull() ?: -1
                    if (dVal in 1..9) {
                        // показываем однозначный ввод (пока без точки, пользователь может продолжить)
                        out.append(dayRaw)
                        digitIdx += 1
                    } else {
                        digitIdx += 1
                    }
                }
            }
        }

        // --- Обработка месяца ---
        // месяц начинается только если день уже завершён
        val dayCompleted = out.contains('.')
        if (dayCompleted) {
            // Если пользователь поставил точку где-то после первого сегмента, учтём это.
            val remaining = digits.length - digitIdx
            if (remaining >= 1) {
                // проверка на ручную точку после одного знака месяца:
                // если в filtered есть точка на позиции, соответствующей месяцу (после израсходованных знаков и/или явной точки),
                // то мы обработаем однозначный месяц как "0M."
                // Для надёжности: если пользователь ввёл только 1 цифру месяца и дальше стоит точка в filtered,
                // то выдаём "0M."
                val monthFirstDotIndexInFiltered = run {
                    null
                }

                if (remaining >= 2) {
                    val monthRaw = digits.substring(digitIdx, digitIdx + 2)
                    val mVal = monthRaw.toIntOrNull() ?: -1
                    if (mVal in 1..12) {
                        out.append(monthRaw).append('.')
                        digitIdx += 2
                    } else {

                        val first = monthRaw[0]
                        val fVal = first.toString().toIntOrNull() ?: -1
                        if (fVal in 1..9) {
                            out.append(first)
                            digitIdx += 1
                        } else {
                            digitIdx += 1
                        }
                    }
                } else {

                    val monthRaw = digits.substring(digitIdx, digitIdx + 1)
                    val mVal = monthRaw.toIntOrNull() ?: -1
                    if (mVal in 1..9) {

                        val positionOfThisDigitInFiltered = findNthDigitPositionInFiltered(filtered, digitIdx)
                        val hasDotRightAfter = if (positionOfThisDigitInFiltered != -1 && positionOfThisDigitInFiltered + 1 < filtered.length) {
                            filtered[positionOfThisDigitInFiltered + 1] == '.'
                        } else false

                        if (hasDotRightAfter) {
                            out.append(monthRaw.padStart(2, '0')).append('.')
                            digitIdx += 1
                        } else {
                            out.append(monthRaw)
                            digitIdx += 1
                        }
                    } else {
                        digitIdx += 1
                    }
                }
            }
        }

        // --- Обработка года ---
        val dotsCount = out.count { it == '.' }
        if (dotsCount >= 2) {
            val remaining = digits.length - digitIdx
            if (remaining > 0) {
                // возьмём до 4 цифр
                val take = minOf(4, remaining)
                val yearRaw = digits.substring(digitIdx, digitIdx + take)
                // Проверки: если введено >=2 цифр — первые два должны быть "20"
                if (yearRaw.length >= 2) {
                    if (!yearRaw.startsWith("20")) {

                    } else {
                        // допустимо — добавляем те цифры, что ввёл пользователь (до 4)
                        out.append(yearRaw)
                        digitIdx += yearRaw.length
                    }
                } else {

                    val ch = yearRaw[0]
                    if (ch == '2') {
                        out.append(ch)
                        digitIdx += 1
                    } else {
                    }
                }
            }
        }

        // Ограничение длины результата
        var result = out.toString()
        if (result.length > 10) result = result.substring(0, 10)

        // Защита: результат должен содержать только цифры и точки
        result = result.filter { it.isDigit() || it == '.' }

        return result
    }

    private fun findNthDigitPositionInFiltered(filtered: String, digitIndex: Int): Int {
        var dCount = 0
        for (i in filtered.indices) {
            if (filtered[i].isDigit()) {
                if (dCount == digitIndex) return i
                dCount++
            }
        }
        return -1
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

    fun handleBarcodeScan(barcode: String): String {
        val info = model.getMedicineByBarcode(barcode)
        return if (info != null) {
            "📦 Найдено лекарство:\n$info\nШтрихкод: $barcode\nВведите срок годности"
        } else {
            "❌ Лекарство не найдено\nШтрихкод: $barcode\nВведите название вручную"
        }
    }
}
