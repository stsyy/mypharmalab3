package com.example.mypharmalab3.Model

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.mypharmalab3.Controller.MedicineController

class SharedMedicineViewModel : ViewModel() {

    // 1. ViewModel является владельцем единственного экземпляра Controller/Model
    //    Создает ЕДИНСТВЕННЫЙ экземпляр Model, который хранит список лекарств.
    private val controller = MedicineController(MedicineModel())

    private val _medicines = MutableLiveData<List<Medicine>>()
    val medicines: LiveData<List<Medicine>> = _medicines

    private val _uniqueNames = MutableLiveData<List<String>>()
    val uniqueNames: LiveData<List<String>> = _uniqueNames

    init {
        loadMedicines()
    }

    // Функция, которую вызывает HomeFragment для загрузки
    fun loadMedicines() {
        // ДВУНАПРАВЛЕННАЯ СВЯЗЬ: обновление LiveData
        _medicines.value = controller.getMedicineList()
        loadUniqueMedicineNames()
    }

    private fun loadUniqueMedicineNames() {
        _uniqueNames.value = controller.getUniqueMedicineNames()
    }

    // ⭐️ МЕТОД 1: Обработка добавления лекарства
    fun handleAddMedicine(
        name: String,
        expiryInput: String,
        reminder: Boolean,
        seasonal: Boolean
    ): String {
        // Вызываем логику в Controller (он теперь один!)
        val message = controller.handleAddMedicine(name, expiryInput, reminder, seasonal)

        // 3. Если сохранение прошло успешно, немедленно обновляем LiveData
        if (message.startsWith("✅")) {
            // ЭТО заставляет HomeFragment сработать (реактивная связь)
            loadMedicines()
        }

        return message
    }

    // ⭐️ МЕТОД 2: Обработка сканирования штрихкода (добавлен для полноты)
    fun handleBarcodeScan(barcode: String): String {
        // Делегируем работу Controller, который знает о Model
        return controller.handleBarcodeScan(barcode)
    }
}