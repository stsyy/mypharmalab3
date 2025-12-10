package com.example.mypharmalab3.Controller

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import com.example.mypharmalab3.db_entities.DaoMaster
import com.example.mypharmalab3.db_entities.DaoSession
import com.example.mypharmalab3.db_entities.DoseTime
import android.util.Log

/**
 * Класс для инициализации и управления сессией greenDAO.
 * Он управляет созданием базы данных и ее обновлением.
 */
class DatabaseHelper(context: Context) {

    private val DB_NAME = "pharmacy-db"
    private val helper: DaoMaster.OpenHelper
    private val db: SQLiteDatabase
    private val daoMaster: DaoMaster
    val daoSession: DaoSession

    init {
        // Создаем помощник для открытия/создания базы данных
        helper = DaoMaster.OpenHelper(context, DB_NAME) { db, oldVersion, newVersion ->
            // Это метод, который вызывается при обновлении версии базы данных
            // Здесь нужно реализовать миграцию данных, если структура меняется
            Log.w("greenDAO", "Обновление базы данных с версии $oldVersion до $newVersion")
            // Здесь можно вызвать DoseTime.dropTable(db, true) и т.д.
        }

        // Открываем базу данных для записи
        db = helper.writableDb

        // Создаем DaoMaster, который содержит всю схему базы данных
        daoMaster = DaoMaster(db)

        // Создаем DaoSession для выполнения всех операций
        daoSession = daoMaster.newSession()

        // Гарантируем, что справочник времени приема заполнен при первом запуске
        // Важно: этот метод должен быть вызван только после создания таблиц
        initializeDoseTimeData()
    }

    /**
     * Заполняет таблицу DoseTime стандартными значениями (Утро, День, Вечер, Ночь),
     * если она пуста.
     */
    private fun initializeDoseTimeData() {
        val doseTimeDao = daoSession.doseTimeDao

        if (doseTimeDao.count() == 0L) {
            Log.i("greenDAO", "Таблица DoseTime пуста. Заполнение стандартными значениями.")

            // Запись в базу данных
            doseTimeDao.insert(DoseTime("Утро", 1))
            doseTimeDao.insert(DoseTime("День", 2))
            doseTimeDao.insert(DoseTime("Вечер", 3))
            doseTimeDao.insert(DoseTime("Ночь", 4))
        }
    }
}