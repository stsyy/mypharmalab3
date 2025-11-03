package com.example.mypharmalab3.View

import android.app.DatePickerDialog // НОВЫЙ ИМПОРТ
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.CheckBox
import android.widget.DatePicker // НОВЫЙ ИМПОРТ
import android.widget.EditText
import android.widget.Toast
import androidx.core.content.ContextCompat.getSystemService
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResult
import androidx.navigation.fragment.findNavController
import com.example.mypharmalab3.R
import com.example.mypharmalab3.Controller.MedicineController
import com.example.mypharmalab3.Controller.MedicineReminderWorker
import com.example.mypharmalab3.Model.Medicine
import com.example.mypharmalab3.Model.MedicineModel
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions
import java.text.SimpleDateFormat // НОВЫЙ ИМПОРТ
import java.util.Calendar // НОВЫЙ ИМПОРТ
import java.util.Locale // НОВЫЙ ИМПОРТ

class AddMedicineFragment : Fragment() {

    private lateinit var controller: MedicineController

    private lateinit var medicineName: EditText
    private lateinit var expiryDate: EditText
    private lateinit var reminderCheckbox: CheckBox
    private lateinit var seasonalCheckbox: CheckBox
    private lateinit var addButton: Button
    private lateinit var scanButton: Button

    private val barcodeLauncher = registerForActivityResult(ScanContract()) { result ->
        if (result.contents == null) {
            Toast.makeText(requireContext(), "Сканирование отменено", Toast.LENGTH_SHORT).show()
        } else {
            val message = controller.handleBarcodeScan(result.contents)

            val foundName = controller.handleBarcodeScan(result.contents)
                .substringAfter("📦 Найдено лекарство:\n")
                .substringBefore("\nШтрихкод")
                .trim()

            medicineName.setText(foundName)

            Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
            expiryDate.requestFocus()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        controller = MedicineController(MedicineModel())
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_add_medicine, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        medicineName = view.findViewById(R.id.medicineName)
        expiryDate = view.findViewById(R.id.expiryDate)
        reminderCheckbox = view.findViewById(R.id.reminderCheckbox)
        seasonalCheckbox = view.findViewById(R.id.seasonalCheckbox)
        addButton = view.findViewById(R.id.addButton)
        scanButton = view.findViewById(R.id.scanButton)

        medicineName.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_NEXT) {
                expiryDate.requestFocus()
                true
            } else {
                false
            }
        }

        // *** ВМЕСТО TextWatcher и EditorActionListener ***
        expiryDate.setOnClickListener {
            showDatePickerDialog()
        }

        addButton.setOnClickListener { onAddMedicineClicked() }
        scanButton.setOnClickListener { startBarcodeScanner() }
    }

    // *** НОВАЯ ФУНКЦИЯ ДЛЯ ВЫЗОВА КАЛЕНДАРЯ ***
    private fun showDatePickerDialog() {
        val calendar = Calendar.getInstance()

        // Если в поле уже есть дата, устанавливаем ее для календаря
        expiryDate.text.toString().let { currentText ->
            try {
                // Пытаемся распарсить текущий текст
                val date = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).parse(currentText)
                date?.let { calendar.time = it }
            } catch (e: Exception) {
                // Если дата невалидна, используем текущую
            }
        }

        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val dialog = DatePickerDialog(
            requireContext(),
            // Слушатель сработает, когда пользователь выберет дату
            { _: DatePicker, selectedYear: Int, selectedMonth: Int, selectedDayOfMonth: Int ->
                val selectedCalendar = Calendar.getInstance().apply {
                    // Month (selectedMonth) в DatePicker начинается с 0, поэтому это корректно
                    set(selectedYear, selectedMonth, selectedDayOfMonth)
                }
                val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())

                // Устанавливаем отформатированную дату в EditText
                expiryDate.setText(dateFormat.format(selectedCalendar.time))
            },
            year, month, day
        )

        // Ограничение: нельзя выбрать прошедшую дату (срок годности не может быть в прошлом)
        dialog.datePicker.minDate = System.currentTimeMillis()

        dialog.show()
    }
    // *** КОНЕЦ НОВОЙ ФУНКЦИИ ***

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
        val message = controller.handleAddMedicine(
            name = medicineName.text.toString(),
            expiryInput = expiryDate.text.toString(),
            reminder = reminderCheckbox.isChecked,
            seasonal = seasonalCheckbox.isChecked
        )

        Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()

        val medicine = Medicine(
            name = medicineName.text.toString(),
            expiryDate = expiryDate.text.toString(),
            reminder = reminderCheckbox.isChecked,
            seasonal = seasonalCheckbox.isChecked
        )

        if (message.startsWith("✅")) {
            MedicineReminderWorker.scheduleReminder(requireContext(), medicine)

            val bundle = Bundle().apply {
                putString("result_message", message)
                putString("medicine_name_added", medicine.name)
            }

            setFragmentResult("add_medicine_request", bundle)

            findNavController().popBackStack()

            clearFields()
        }
    }

    private fun clearFields() {
        medicineName.text.clear()
        expiryDate.text.clear()
        reminderCheckbox.isChecked = false
        seasonalCheckbox.isChecked = false
    }
}