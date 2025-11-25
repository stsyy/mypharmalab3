package com.example.mypharmalab3.Controller

import android.content.Context
import android.util.Log
import com.example.mypharmalab3.Model.AppPreferences
import java.io.*
import java.util.Date

class BinarySettingsManager(private val context: Context) {

    private val FILE_NAME = "app_settings.bin"

    // 1. ЗАПИСЬ (СОХРАНЕНИЕ)
    fun savePreferences(preferences: AppPreferences) {
        try {
            // Открываем OutputStream для записи в файл в закрытой папке приложения
            val fos = context.openFileOutput(FILE_NAME, Context.MODE_PRIVATE)
            val oos = ObjectOutputStream(fos)

            // Сериализация (запись объекта в поток)
            oos.writeObject(preferences)

            oos.close()
            Log.i("BinarySettingsManager", "Настройки успешно сохранены в $FILE_NAME.")
        } catch (e: Exception) {
            Log.e("BinarySettingsManager", "Ошибка сохранения настроек: ${e.message}")
        }
    }

    // 2. ЧТЕНИЕ (ЗАГРУЗКА)
    fun loadPreferences(): AppPreferences {
        return try {
            // Открываем InputStream для чтения из файла
            val fis = context.openFileInput(FILE_NAME)
            val ois = ObjectInputStream(fis)

            // Десериализация (чтение объекта из потока)
            val preferences = ois.readObject() as AppPreferences

            ois.close()
            Log.i("BinarySettingsManager", "Настройки успешно загружены.")
            preferences
        } catch (e: FileNotFoundException) {
            Log.w("BinarySettingsManager", "Файл настроек не найден. Возвращаем настройки по умолчанию.")
            // Если файл не существует, возвращаем настройки по умолчанию
            AppPreferences(lastSyncDate = Date(0), isDarkModeEnabled = false)
        } catch (e: Exception) {
            Log.e("BinarySettingsManager", "Ошибка загрузки настроек: ${e.message}")
            // В случае ошибки возвращаем настройки по умолчанию
            AppPreferences(lastSyncDate = Date(0), isDarkModeEnabled = false)
        }
    }

    // 3. УДАЛЕНИЕ (СБРОС)
    fun deletePreferences(): Boolean {
        // Удаляем файл из закрытого хранилища
        return context.deleteFile(FILE_NAME).also {
            if (it) {
                Log.i("BinarySettingsManager", "Файл настроек удален.")
            } else {
                Log.w("BinarySettingsManager", "Файл настроек не найден для удаления.")
            }
        }
    }

    // 4. ИЗМЕНЕНИЕ (Для изменения нужно сначала прочитать, затем изменить и записать обратно)
    fun updateDefaultReminderDays(newDays: Int) {
        val currentPrefs = loadPreferences()
        val updatedPrefs = currentPrefs.copy(defaultReminderDays = newDays, lastSyncDate = Date())
        savePreferences(updatedPrefs)
        Log.i("BinarySettingsManager", "Настройки обновлены: дни напоминания = $newDays")
    }
}