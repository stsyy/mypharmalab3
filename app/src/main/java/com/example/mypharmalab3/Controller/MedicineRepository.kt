package com.example.mypharmalab3.Controller

import com.example.mypharmalab3.db_entities.DaoSession
import com.example.mypharmalab3.db_entities.Medicine
import com.example.mypharmalab3.db_entities.DoseTime
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Репозиторий для выполнения всех операций CRUD с базой данных (SQLite/greenDAO).
 * Все операции должны выполняться в фоновом потоке (например, с помощью Dispatchers.IO).
 */
class MedicineRepository(private val daoSession: DaoSession) {

    private val medicineDao = daoSession.medicineDao
    private val doseTimeDao = daoSession.doseTimeDao
    private val TAG = "MedicineRepository"

    // ⭐️ CREATE (Создание новой записи о лекарстве)
    suspend fun insertMedicine(medicine: Medicine): Long = withContext(Dispatchers.IO) {
        val newId = medicineDao.insert(medicine)
        Log.d(TAG, "Вставлено новое лекарство с ID: $newId")
        newId
    }

    // ⭐️ READ (Чтение всех лекарств)
    suspend fun getAllMedicines(): List<Medicine> = withContext(Dispatchers.IO) {
        val medicines = medicineDao.loadAll()
        // Важно: нужно загрузить связанные объекты DoseTime для каждого Medicine
        medicines.forEach { it.refresh() }
        Log.d(TAG, "Загружено ${medicines.size} лекарств.")
        medicines
    }

    // ⭐️ READ (Получение всех времен приема)
    suspend fun getAllDoseTimes(): List<DoseTime> = withContext(Dispatchers.IO) {
        // Используем QueryBuilder для сортировки по sortOrder
        doseTimeDao.queryBuilder()
            .orderAsc(DoseTimeDao.Properties.SortOrder)
            .list()
    }

    // ⭐️ UPDATE (Изменение существующей записи)
    suspend fun updateMedicine(medicine: Medicine) = withContext(Dispatchers.IO) {
        medicineDao.update(medicine)
        Log.d(TAG, "Обновлено лекарство с ID: ${medicine.id}")
    }

    // ⭐️ DELETE (Удаление записи)
    suspend fun deleteMedicine(medicine: Medicine) = withContext(Dispatchers.IO) {
        medicineDao.delete(medicine)
        Log.d(TAG, "Удалено лекарство с ID: ${medicine.id}")
    }
}