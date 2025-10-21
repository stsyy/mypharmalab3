package com.example.mypharmalab3.Controller

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.Data
import androidx.work.WorkManager
import com.example.mypharmalab3.util.NotificationHelper
import com.example.mypharmalab3.Model.Medicine
import java.util.concurrent.TimeUnit
import java.text.SimpleDateFormat
import java.util.Locale

class MedicineReminderWorker(
    context: Context,
    params: WorkerParameters
) : Worker(context, params) {

    override fun doWork(): Result {
        val name = inputData.getString("medicine_name") ?: return Result.failure()
        val date = inputData.getString("expiration_date") ?: "неизвестно"

        NotificationHelper.showNotification(
            context = applicationContext,
            title = "Срок годности истекает",
            message = "Лекарство \"$name\" годно до $date. Проверь аптечку."
        )
        return Result.success()
    }

    companion object {
        fun scheduleReminder(context: Context, medicine: Medicine) {
            if (!medicine.reminder) return

            val sdf = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
            val expiryDate = sdf.parse(medicine.expiryDate) ?: return

            // Напоминание за 3 дня до окончания срока
            val delay = expiryDate.time - System.currentTimeMillis() - 3*24*60*60*1000
            if (delay <= 0) return

            val workData = Data.Builder()
                .putString("medicine_name", medicine.name)
                .putString("expiration_date", medicine.expiryDate)
                .build()

            val reminderRequest = OneTimeWorkRequestBuilder<MedicineReminderWorker>()
                .setInitialDelay(delay, TimeUnit.MILLISECONDS)
                .setInputData(workData)
                .build()

            WorkManager.getInstance(context).enqueue(reminderRequest)
        }
    }
}
