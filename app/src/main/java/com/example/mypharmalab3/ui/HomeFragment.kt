package com.example.mypharmalab3.ui

import android.os.Bundle
import android.util.Log
import android.view.ContextMenu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import android.widget.Toast
import android.widget.Button
import com.example.mypharmalab3.Controller.CsvFileHandler
import com.example.mypharmalab3.Controller.PdfGenerator
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mypharmalab3.Controller.BinarySettingsManager
import com.example.mypharmalab3.R
import com.example.mypharmalab3.View.MedicineAdapter
import com.example.mypharmalab3.View.OnMedicineItemClickListener
import com.example.mypharmalab3.Model.SharedMedicineViewModel
import com.example.mypharmalab3.Model.Medicine
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.util.Date
import android.content.Intent // ⭐️ Импортируем Intent
import androidx.activity.result.contract.ActivityResultContracts

class HomeFragment : Fragment(R.layout.fragment_home), OnMedicineItemClickListener {

    private val sharedViewModel: SharedMedicineViewModel by activityViewModels()
    private val args: HomeFragmentArgs by navArgs()

    private lateinit var adapter: MedicineAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var resultTextView: TextView
    private lateinit var fabAdd: FloatingActionButton

    private lateinit var settingsManager: BinarySettingsManager
    private lateinit var csvHandler: CsvFileHandler
    private lateinit var pdfGenerator: PdfGenerator

    private lateinit var exportButton: Button
    private lateinit var importButton: Button
    private lateinit var exportPdfButton: Button

    private var contextMenuMedicine: Medicine? = null


    private val createCsvFile = registerForActivityResult(ActivityResultContracts.CreateDocument("text/csv")) { uri ->
        if (uri != null) {
            val medicines = sharedViewModel.medicines.value ?: emptyList()
            if (medicines.isEmpty()) {
                Toast.makeText(requireContext(), "Нечего экспортировать: список пуст.", Toast.LENGTH_SHORT).show()
                return@registerForActivityResult
            }

            val csvData = csvHandler.listToCsv(medicines)
            val success = csvHandler.writeCsvFile(uri, csvData)

            val message = if (success) "✅ Каталог экспортирован в CSV!" else "❌ Ошибка экспорта CSV."
            Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
        }
    }

    private val openCsvFile = registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) {
            val csvString = csvHandler.readCsvFile(uri)

            if (csvString != null) {
                val importedList = csvHandler.csvToList(csvString)
                if (importedList.isNotEmpty()) {
                    sharedViewModel.importMedicines(importedList)
                    Toast.makeText(requireContext(), "✅ Импортировано ${importedList.size} лекарств из CSV!", Toast.LENGTH_LONG).show()
                } else {
                    Toast.makeText(requireContext(), "❌ CSV файл пуст или не содержит данных.", Toast.LENGTH_LONG).show()
                }
            } else {
                Toast.makeText(requireContext(), "❌ Не удалось прочитать CSV файл.", Toast.LENGTH_LONG).show()
            }
        }
    }

    private val createPdfFile = registerForActivityResult(ActivityResultContracts.CreateDocument("application/pdf")) { uri ->
        if (uri != null) {
            val medicines = sharedViewModel.medicines.value ?: emptyList()
            if (medicines.isEmpty()) {
                Toast.makeText(requireContext(), "Нечего генерировать: список пуст.", Toast.LENGTH_SHORT).show()
                return@registerForActivityResult
            }

            val success = pdfGenerator.generateAndWritePdf(uri, medicines)

            val message = if (success) "✅ Отчет экспортирован в PDF!" else "❌ Ошибка генерации PDF."
            Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 1. Инициализируем manager, как только Context станет доступен (в onViewCreated)
        settingsManager = BinarySettingsManager(requireContext())
        csvHandler = CsvFileHandler(requireContext())
        pdfGenerator = PdfGenerator(requireContext())

        resultTextView = view.findViewById(R.id.tv_result_message)

        exportButton = view.findViewById(R.id.exportButton)
        importButton = view.findViewById(R.id.importButton)
        exportPdfButton = view.findViewById(R.id.exportPdfButton)

        // 2.Чтение, Изменение, Запись
        val loadedPrefs = settingsManager.loadPreferences()
        Log.d("Prefs", "Последняя синхронизация: ${loadedPrefs.lastSyncDate}")

        // 3. Изменение и сохранение
        val newPrefs = loadedPrefs.copy(isDarkModeEnabled = true, lastSyncDate = Date())
        settingsManager.savePreferences(newPrefs)

        // 4. Обновление
        settingsManager.updateDefaultReminderDays(7)

        // 5. Удаление (закомментировано, чтобы не удалять сразу)
        // settingsManager.deletePreferences()

        recyclerView = view.findViewById(R.id.medicineRecyclerView)
        fabAdd = view.findViewById(R.id.fabAdd)

        recyclerView.layoutManager = GridLayoutManager(requireContext(), 2)

        adapter = MedicineAdapter(
            medicineList = sharedViewModel.medicines.value ?: emptyList(),
            itemClickListener = this
        )
        recyclerView.adapter = adapter

        // Регистрируем RecyclerView для получения контекстного меню
        registerForContextMenu(recyclerView)


        // Проверяем, было ли передано сообщение об успехе из AddMedicineFragment
        if (args.resultMessage.isNotBlank()) {
            Toast.makeText(requireContext(), args.resultMessage, Toast.LENGTH_LONG).show()


            val action = HomeFragmentDirections.actionHomeFragmentSelf(resultMessage = " ")
            findNavController().navigate(action)
        }

        fabAdd.setOnClickListener {
            // Обязательно очищаем selectedMedicine, чтобы AddFragment открылся
            //    в режиме "Добавить", а не в режиме "Редактировать"
            sharedViewModel.clearSelectedMedicine()

            // 2. Выполняем навигацию на фрагмент добавления
            findNavController().navigate(R.id.action_homeFragment_to_addMedicineFragment)
        }

        exportButton.setOnClickListener {
            // Запускаем системный диалог для создания нового файла
            val defaultFileName = "med_catalog_${Date().time}.csv"
            createCsvFile.launch(defaultFileName)
        }

        importButton.setOnClickListener {
            // Запускаем системный диалог для выбора файла
            // Устанавливаем тип файла, который хотим открыть (только CSV)
            openCsvFile.launch(arrayOf("text/csv"))
        }

        exportPdfButton.setOnClickListener {
            val defaultFileName = "pharmacy_report_${Date().time}.pdf"
            createPdfFile.launch(defaultFileName)
        }

        sharedViewModel.medicines.observe(viewLifecycleOwner) { newMedicineList ->
            adapter.updateData(newMedicineList)
            if (newMedicineList.isEmpty()) {
                recyclerView.visibility = View.GONE
                resultTextView.visibility = View.VISIBLE
                resultTextView.text = "Здесь появятся лекарства"
            } else {
                recyclerView.visibility = View.VISIBLE
                resultTextView.visibility = View.GONE
            }
        }

        sharedViewModel.selectedMedicine.observe(viewLifecycleOwner) { medicine ->
            if (medicine != null) {
                Toast.makeText(requireContext(), "Выбран элемент: ${medicine.name}", Toast.LENGTH_SHORT).show()
                findNavController().navigate(R.id.action_homeFragment_to_addMedicineFragment)
            }
        }


        if (sharedViewModel.medicines.value.isNullOrEmpty()) {
            sharedViewModel.loadMedicines()
        }
    }

    override fun onDeleteClick(medicine: Medicine) {
        sharedViewModel.deleteMedicine(medicine)
        Toast.makeText(requireContext(), "Удалено: ${medicine.name}", Toast.LENGTH_SHORT).show()
    }

    override fun onLongClick(medicine: Medicine): Boolean {
        contextMenuMedicine = medicine // Сохраняем выбранный объект
        return false // Важно: false, чтобы событие передалось для открытия контекстного меню
    }

    override fun onCreateContextMenu(
        menu: ContextMenu,
        v: View,
        menuInfo: ContextMenu.ContextMenuInfo?
    ) {
        super.onCreateContextMenu(menu, v, menuInfo)
        val inflater: MenuInflater = requireActivity().menuInflater

        menu.add(0, R.id.menu_edit, 0, "Редактировать") // Используем R.id.menu_edit (ты должна создать этот ID)
    }

    override fun onContextItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.menu_edit) {
            contextMenuMedicine?.let { medicine ->
                // 1. Устанавливаем элемент для редактирования в ViewModel
                sharedViewModel.setMedicineToEdit(medicine)
                // 2. ViewModel в ответ на setMedicineToEdit вызовет navigate (см. выше)
            }
            contextMenuMedicine = null // Очистка
            return true
        }

        return super.onContextItemSelected(item)
    }
}