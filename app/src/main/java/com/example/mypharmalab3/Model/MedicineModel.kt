package com.example.mypharmalab3.Model

import android.icu.util.Calendar
import android.os.Build
import androidx.annotation.RequiresApi
import java.time.LocalDate

class MedicineModel {

    private val medicineList = mutableListOf<Medicine>()

    private val medicineDatabase = mapOf(
        "4601234567890" to "Нурофен таблетки 200мг",
        "4602345678901" to "Супрастин 25мг",
        "4610123456789" to "Йод 5% раствор",
        "4602193012837" to "Ингавирин 90мгыЫ"

    )

    fun getMedicineByBarcode(barcode: String): String? {
        return medicineDatabase[barcode]
    }

    fun saveMedicine(medicine: Medicine): String {

        medicineList.add(medicine)

        val sb = StringBuilder().apply {
            append("✅ Лекарство добавлено!\n")
            append("Название: ${medicine.name}\n")
            append("Срок годности: ${medicine.expiryDate}\n")
            if (medicine.reminder) append("🔔 Напоминание включено\n")
            if (medicine.seasonal) append("📌 Сезонные рекомендации: ${getSeasonalRecommendations().joinToString(", ")}\n")
        }
        return sb.toString()
    }

    fun getAllMedicines(): List<Medicine> {

        return medicineList.toList()
    }

    fun getUniqueMedicineNames(): List<String> {

        return medicineList.map { it.name }.distinct().sorted()
    }

    fun deleteMedicine(medicine: Medicine): Boolean {

        return medicineList.remove(medicine)
    }

    fun getSeasonalRecommendations(): List<String> {
        val month = Calendar.getInstance().get(Calendar.MONTH) + 1
        return when (month) {
            in 3..5 -> listOf("антигистаминные (от аллергии)", "средства от укусов насекомых")
            in 6..8 -> listOf("солнцезащитный крем", "средства от ожогов", "противодиарейные препараты")
            in 9..11 -> listOf("витамины", "противопростудные", "спрей для горла")
            else -> listOf("противовирусные", "теплые компрессы", "средства от кашля")
        }
    }
}