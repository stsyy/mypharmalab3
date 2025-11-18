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
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mypharmalab3.R
import com.example.mypharmalab3.View.MedicineAdapter
import com.example.mypharmalab3.View.OnMedicineItemClickListener
import com.example.mypharmalab3.Model.SharedMedicineViewModel
import com.example.mypharmalab3.Model.Medicine
import com.google.android.material.floatingactionbutton.FloatingActionButton

class HomeFragment : Fragment(R.layout.fragment_home), OnMedicineItemClickListener {

    private val sharedViewModel: SharedMedicineViewModel by activityViewModels()
    // ⭐️ Инициализируем специальный делегат для работы с аргументами
    private val args: HomeFragmentArgs by navArgs()

    private lateinit var adapter: MedicineAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var resultTextView: TextView
    private lateinit var fabAdd: FloatingActionButton

    private var contextMenuMedicine: Medicine? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerView = view.findViewById(R.id.medicineRecyclerView)
        resultTextView = view.findViewById(R.id.tv_result_message)
        fabAdd = view.findViewById(R.id.fabAdd)

        recyclerView.layoutManager = GridLayoutManager(requireContext(), 2)

        adapter = MedicineAdapter(
            medicineList = sharedViewModel.medicines.value ?: emptyList(),
            itemClickListener = this
        )
        recyclerView.adapter = adapter

        // Регистрируем RecyclerView для получения контекстного меню
        registerForContextMenu(recyclerView)

        // ⭐️⭐️⭐️ ИЗМЕНЕНИЯ ЗДЕСЬ ⭐️⭐️⭐️

        // Проверяем, было ли передано сообщение об успехе из AddMedicineFragment
        if (args.resultMessage.isNotBlank()) {
            Toast.makeText(requireContext(), args.resultMessage, Toast.LENGTH_LONG).show()

            // Важно: Сбрасываем аргумент resultMessage, чтобы избежать повторного показа.
            // Мы "переходим" на этот же фрагмент, но с пустым аргументом.
            val action = HomeFragmentDirections.actionHomeFragmentSelf(resultMessage = " ")
            findNavController().navigate(action)
        }
        // ⭐️⭐️⭐️ КОНЕЦ ИЗМЕНЕНИЙ ⭐️⭐️⭐️


        fabAdd.setOnClickListener {
            // Обязательно очищаем selectedMedicine, чтобы AddFragment открылся
            //    в режиме "Добавить", а не в режиме "Редактировать"
            sharedViewModel.clearSelectedMedicine()

            // 2. Выполняем навигацию на фрагмент добавления
            findNavController().navigate(R.id.action_homeFragment_to_addMedicineFragment)
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

    //  ОБРАБОТКА ВЫБОРА ИЗ КОНТЕКСТНОГО МЕНЮ
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