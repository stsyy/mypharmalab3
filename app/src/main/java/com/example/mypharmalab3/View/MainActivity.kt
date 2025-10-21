package com.example.mypharmalab3.View

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.mypharmalab3.R
import com.example.mypharmalab3.Controller.MedicineController
import com.example.mypharmalab3.Controller.MedicineReminderWorker
import com.example.mypharmalab3.Model.Medicine
import com.example.mypharmalab3.Model.MedicineModel
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions

class MainActivity : AppCompatActivity() {

    private lateinit var controller: MedicineController

    private lateinit var medicineName: EditText
    private lateinit var expiryDate: EditText
    private lateinit var reminderCheckbox: CheckBox
    private lateinit var seasonalCheckbox: CheckBox
    private lateinit var addButton: Button
    private lateinit var scanButton: Button

    private val barcodeLauncher = registerForActivityResult(ScanContract()) { result ->
        if (result.contents == null) {
            Toast.makeText(this, "Сканирование отменено", Toast.LENGTH_SHORT).show()
        } else {
            val message = controller.handleBarcodeScan(result.contents)
            medicineName.setText(
                controller.handleBarcodeScan(result.contents)
                    .substringAfter("📦 Найдено лекарство:\n")
                    .substringBefore("\nШтрихкод")
                    .trim()
            )
            Toast.makeText(this, message, Toast.LENGTH_LONG).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        controller = MedicineController(MedicineModel())

        medicineName = findViewById(R.id.medicineName)
        expiryDate = findViewById(R.id.expiryDate)
        reminderCheckbox = findViewById(R.id.reminderCheckbox)
        seasonalCheckbox = findViewById(R.id.seasonalCheckbox)
        addButton = findViewById(R.id.addButton)
        scanButton = findViewById(R.id.scanButton)

        medicineName.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_NEXT) {
                expiryDate.requestFocus()
                true
            } else {
                false
            }
        }

        expiryDate.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(expiryDate.windowToken, 0)
                expiryDate.clearFocus()
                true
            } else {
                false
            }
        }

        expiryDate.addTextChangedListener(object : TextWatcher {
            private var isEditing = false

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                if (isEditing) return
                isEditing = true

                val input = s?.toString() ?: ""
                val formatted = controller.formatExpiryInputForDisplay(input)

                if (formatted != input) {
                    expiryDate.setText(formatted)
                    expiryDate.setSelection(formatted.length.coerceAtMost(formatted.length))
                }
                isEditing = false
            }
        })

        addButton.setOnClickListener { onAddMedicineClicked() }
        scanButton.setOnClickListener { startBarcodeScanner() }
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
        val message = controller.handleAddMedicine(
            name = medicineName.text.toString(),
            expiryInput = expiryDate.text.toString(),
            reminder = reminderCheckbox.isChecked,
            seasonal = seasonalCheckbox.isChecked
        )

        Toast.makeText(this, message, Toast.LENGTH_LONG).show()

        val medicine = Medicine(
            name = medicineName.text.toString(),
            expiryDate = expiryDate.text.toString(),
            reminder = reminderCheckbox.isChecked,
            seasonal = seasonalCheckbox.isChecked
        )

        MedicineReminderWorker.scheduleReminder(this, medicine)

        clearFields()
    }

    private fun clearFields() {
        medicineName.text.clear()
        expiryDate.text.clear()
        reminderCheckbox.isChecked = false
        seasonalCheckbox.isChecked = false
    }
}
