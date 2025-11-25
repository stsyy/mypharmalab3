package com.example.mypharmalab3.Model

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.mypharmalab3.Controller.MedicineController

class SharedMedicineViewModel : ViewModel() {

    private val controller = MedicineController(MedicineModel())

    private val _medicines = MutableLiveData<List<Medicine>>()
    val medicines: LiveData<List<Medicine>> = _medicines

    private val _uniqueNames = MutableLiveData<List<String>>()
    val uniqueNames: LiveData<List<String>> = _uniqueNames

    private val _selectedMedicine = MutableLiveData<Medicine?>()
    val selectedMedicine: LiveData<Medicine?> = _selectedMedicine

    init {
        loadMedicines()
    }

    fun loadMedicines() {

        _medicines.value = controller.getMedicineList()
        loadUniqueMedicineNames()
    }

    private fun loadUniqueMedicineNames() {
        _uniqueNames.value = controller.getUniqueMedicineNames()
    }

    fun handleAddMedicine(
        name: String,
        expiryInput: String,
        reminder: Boolean,
        seasonal: Boolean
    ): String {

        val message = controller.handleAddMedicine(name, expiryInput, reminder, seasonal)

        if (message.startsWith("✅")) {

            loadMedicines()
        }

        return message
    }

    fun importMedicines(importedList: List<Medicine>) {

        // 1. Передаем список в Controller для сохранения (этот метод мы добавим ниже)
        controller.handleImportMedicines(importedList)

        // 2. Обновляем LiveData, чтобы UI сразу обновился
        loadMedicines()
    }

    fun deleteMedicine(medicine: Medicine) {
        controller.deleteMedicine(medicine)

        loadMedicines()
    }

    fun setMedicineToEdit(medicine: Medicine) {
        _selectedMedicine.value = medicine
    }

    fun clearSelectedMedicine() {
        _selectedMedicine.value = null
    }

    fun handleBarcodeScan(barcode: String): String {

        return controller.handleBarcodeScan(barcode)
    }
}