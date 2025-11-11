package com.example.mypharmalab3.ui

import android.os.Bundle
import android.view.ContextMenu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController // ⭐️ ВАЖНО: для навигации
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mypharmalab3.R
import com.example.mypharmalab3.View.MedicineAdapter
import com.example.mypharmalab3.View.OnMedicineItemClickListener // ⭐️ Обновленный импорт интерфейса
import com.example.mypharmalab3.Model.SharedMedicineViewModel
import com.example.mypharmalab3.Model.Medicine

// ⭐️ РЕАЛИЗУЕМ НОВЫЙ ИНТЕРФЕЙС OnMedicineItemClickListener
class HomeFragment : Fragment(R.layout.fragment_home), OnMedicineItemClickListener {

    private val sharedViewModel: SharedMedicineViewModel by activityViewModels()

    private lateinit var adapter: MedicineAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var resultTextView: TextView

    // ⭐️ НОВОЕ: Временное хранение medicine для контекстного меню
    private var contextMenuMedicine: Medicine? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerView = view.findViewById(R.id.medicineRecyclerView)
        resultTextView = view.findViewById(R.id.tv_result_message)

        recyclerView.layoutManager = GridLayoutManager(requireContext(), 2)

        // ⭐️ ПЕРЕДАЕМ НОВЫЙ СЛУШАТЕЛЬ В АДАПТЕР: this
        adapter = MedicineAdapter(
            medicineList = sharedViewModel.medicines.value ?: emptyList(),
            itemClickListener = this // Используем this для общего интерфейса
        )
        recyclerView.adapter = adapter

        // Регистрируем RecyclerView для получения контекстного меню
        registerForContextMenu(recyclerView)

        sharedViewModel.medicines.observe(viewLifecycleOwner) { newMedicineList ->
            adapter.updateData(newMedicineList)
            // ... (логика Empty State остается прежней) ...
            if (newMedicineList.isEmpty()) {
                recyclerView.visibility = View.GONE
                resultTextView.visibility = View.VISIBLE
                resultTextView.text = "Здесь появятся лекарства"
            } else {
                recyclerView.visibility = View.VISIBLE
                resultTextView.visibility = View.GONE
            }
        }

        // ⭐️ НАБЛЮДЕНИЕ ЗА SELECTED_MEDICINE
        // Если ViewModel говорит, что выбран элемент для редактирования, переходим на AddFragment.
        sharedViewModel.selectedMedicine.observe(viewLifecycleOwner) { medicine ->
            if (medicine != null) {
                // Предполагаем, что у тебя есть NavDirection для перехода на экран добавления
                // findNavController().navigate(R.id.action_homeFragment_to_addMedicineFragment)
                // Пока просто очистим, чтобы не было зацикливания, пока не настроена навигация.
                // В реальном проекте здесь будет навигация!
                // sharedViewModel.clearSelectedMedicine()
                Toast.makeText(requireContext(), "Выбран элемент: ${medicine.name}", Toast.LENGTH_SHORT).show()
                findNavController().navigate(R.id.action_homeFragment_to_addMedicineFragment)
            }
        }


        if (sharedViewModel.medicines.value.isNullOrEmpty()) {
            sharedViewModel.loadMedicines()
        }
    }

    // --- Реализация интерфейса OnMedicineItemClickListener ---

    override fun onDeleteClick(medicine: Medicine) {
        sharedViewModel.deleteMedicine(medicine)
        Toast.makeText(requireContext(), "Удалено: ${medicine.name}", Toast.LENGTH_SHORT).show()
    }

    // ⭐️ U1: Обработка долгого нажатия для вызова контекстного меню
    override fun onLongClick(medicine: Medicine): Boolean {
        contextMenuMedicine = medicine // Сохраняем выбранный объект
        return false // Важно: false, чтобы событие передалось для открытия контекстного меню
    }

    // ⭐️ СОЗДАНИЕ КОНТЕКСТНОГО МЕНЮ (Покажем опцию "Редактировать")
    override fun onCreateContextMenu(
        menu: ContextMenu,
        v: View,
        menuInfo: ContextMenu.ContextMenuInfo?
    ) {
        super.onCreateContextMenu(menu, v, menuInfo)
        val inflater: MenuInflater = requireActivity().menuInflater
        // Здесь тебе нужно создать XML-файл меню (например, context_menu_medicine.xml)
        // с id="menu_edit"
        // Но пока что создадим опцию вручную, чтобы показать логику
        menu.add(0, R.id.menu_edit, 0, "Редактировать") // Используем R.id.menu_edit (ты должна создать этот ID)
    }

    // ⭐️ ОБРАБОТКА ВЫБОРА ИЗ КОНТЕКСТНОГО МЕНЮ
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
        // Если это опция, которую ты реализуешь (например, удаление)
        // if (item.itemId == R.id.menu_delete) { ... }
        return super.onContextItemSelected(item)
    }
}