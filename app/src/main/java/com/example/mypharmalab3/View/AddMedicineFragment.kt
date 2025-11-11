package com.example.mypharmalab3.View

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.CheckBox
import android.widget.DatePicker
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.mypharmalab3.R
import com.example.mypharmalab3.Controller.MedicineReminderWorker
import com.example.mypharmalab3.Model.Medicine
import com.example.mypharmalab3.Model.SharedMedicineViewModel
import androidx.fragment.app.activityViewModels
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class AddMedicineFragment : Fragment() {

    // ⭐️ 1. ДВУНАПРАВЛЕННАЯ СВЯЗЬ: Объявление Shared ViewModel (Единственный источник истины)
    private val sharedViewModel: SharedMedicineViewModel by activityViewModels()

    // 🛑 УДАЛЕНО: private lateinit var controller: MedicineController (больше не нужен)

    private lateinit var medicineName: AutoCompleteTextView
    private lateinit var expiryDate: EditText
    private lateinit var reminderCheckbox: CheckBox
    private lateinit var seasonalCheckbox: CheckBox
    private lateinit var addButton: Button
    private lateinit var scanButton: Button
    private var medicineToEdit: Medicine? = null

    private val barcodeLauncher = registerForActivityResult(ScanContract()) { result ->
        if (result.contents == null) {
            Toast.makeText(requireContext(), "Сканирование отменено", Toast.LENGTH_SHORT).show()
        } else {
            // ⭐️ ИСПРАВЛЕНО: Вызываем через ViewModel
            val message = sharedViewModel.handleBarcodeScan(result.contents)

            val foundName = message
                .substringAfter("📦 Найдено лекарство:\n")
                .substringBefore("\nШтрихкод")
                .trim()

            medicineName.setText(foundName)
            Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
            expiryDate.requestFocus()
        }
    }

    // 🛑 УДАЛЕНО: override fun onCreate(...) {...} (инициализация контроллера теперь в ViewModel)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_add_medicine, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        medicineName = view.findViewById(R.id.medicineName) as AutoCompleteTextView
        expiryDate = view.findViewById(R.id.expiryDate)
        reminderCheckbox = view.findViewById(R.id.reminderCheckbox)
        seasonalCheckbox = view.findViewById(R.id.seasonalCheckbox)
        addButton = view.findViewById(R.id.addButton)
        scanButton = view.findViewById(R.id.scanButton)

        sharedViewModel.uniqueNames.observe(viewLifecycleOwner) { namesList ->
            val adapter = ArrayAdapter(
                requireContext(),
                android.R.layout.simple_dropdown_item_1line, // Стандартный Android-макет
                namesList
            )
            medicineName.setAdapter(adapter)
            // Подсказка появляется после ввода первого символа
            medicineName.threshold = 1
        }

        medicineName.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_NEXT) {
                expiryDate.requestFocus()
                true
            } else {
                false
            }
        }

        expiryDate.setOnClickListener {
            showDatePickerDialog()
        }

        addButton.setOnClickListener { onAddMedicineClicked() }
        scanButton.setOnClickListener { startBarcodeScanner() }

        sharedViewModel.selectedMedicine.value?.let { medicine ->
            // Мы в режиме редактирования, если selectedMedicine не null!
            medicineToEdit = medicine

            // 1. Заполняем поля данными
            medicineName.setText(medicine.name)
            expiryDate.setText(medicine.expiryDate)
            reminderCheckbox.isChecked = medicine.reminder
            seasonalCheckbox.isChecked = medicine.seasonal

            // 2. Меняем текст кнопки
            addButton.text = "Сохранить изменения"

            // 3. Отключаем сканер (сканировать при редактировании не нужно)
            scanButton.visibility = View.GONE
        }
    }

    private fun showDatePickerDialog() {
        val calendar = Calendar.getInstance()
        expiryDate.text.toString().let { currentText ->
            try {
                val date = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).parse(currentText)
                date?.let { calendar.time = it }
            } catch (e: Exception) {}
        }

        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val dialog = DatePickerDialog(
            requireContext(),
            { _: DatePicker, selectedYear: Int, selectedMonth: Int, selectedDayOfMonth: Int ->
                val selectedCalendar = Calendar.getInstance().apply {
                    set(selectedYear, selectedMonth, selectedDayOfMonth)
                }
                val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
                expiryDate.setText(dateFormat.format(selectedCalendar.time))
            },
            year, month, day
        )
        dialog.datePicker.minDate = System.currentTimeMillis()
        dialog.show()
    }

    private fun startBarcodeScanner() {
        val options = ScanOptions().apply {
            setDesiredBarcodeFormats(ScanOptions.ALL_CODE_TYPES)
            setPrompt("Наведите камеру на штрихкод")
            setCameraId(0)
            setBeepEnabled(true)
            setOrientationLocked(false)
        }
        barcodeLauncher.launch(options)
    }

    private fun onAddMedicineClicked() {

        // ⭐️ НОВАЯ ЛОГИКА: "Удалить старое" перед сохранением (если мы редактируем)
        medicineToEdit?.let { oldMedicine ->
            // Сначала удаляем старую версию объекта
            sharedViewModel.deleteMedicine(oldMedicine)
        }

        val message = sharedViewModel.handleAddMedicine(
            name = medicineName.text.toString(),
            expiryInput = expiryDate.text.toString(),
            reminder = reminderCheckbox.isChecked,
            seasonal = seasonalCheckbox.isChecked
        )

        Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()

        if (message.startsWith("✅")) {
            // ⭐️ ОЧИСТКА ВЫБРАННОГО ОБЪЕКТА (После успешного сохранения/редактирования)
            // Это гарантирует, что фрагмент при следующем открытии будет в режиме "Добавить"
            sharedViewModel.clearSelectedMedicine()

            val medicine = Medicine(
                name = medicineName.text.toString(),
                expiryDate = expiryDate.text.toString(),
                reminder = reminderCheckbox.isChecked,
                seasonal = seasonalCheckbox.isChecked
            )
            MedicineReminderWorker.scheduleReminder(requireContext(), medicine)

            findNavController().popBackStack()

            clearFields()
        }
        else {
            expiryDate.text.clear()
        }
    }

    private fun clearFields() {
        medicineName.text.clear()
        expiryDate.text.clear()
        reminderCheckbox.isChecked = false
        seasonalCheckbox.isChecked = false
    }
}