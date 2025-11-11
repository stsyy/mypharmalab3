package com.example.mypharmalab3.ui

import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mypharmalab3.R
import com.example.mypharmalab3.View.MedicineAdapter
import com.example.mypharmalab3.Model.SharedMedicineViewModel
import com.example.mypharmalab3.View.OnDeleteClickListener // ⭐️ ВАЖНО: Импортируем интерфейс!
import com.example.mypharmalab3.Model.Medicine

class HomeFragment : Fragment(R.layout.fragment_home), OnDeleteClickListener {

    private val sharedViewModel: SharedMedicineViewModel by activityViewModels()

    private lateinit var adapter: MedicineAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var resultTextView: TextView

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerView = view.findViewById(R.id.medicineRecyclerView)
        resultTextView = view.findViewById(R.id.tv_result_message)

        recyclerView.layoutManager = GridLayoutManager(requireContext(), 2)

        adapter = MedicineAdapter(
            medicineList = sharedViewModel.medicines.value ?: emptyList(),
            deleteClickListener = this // Передаем ссылку на себя (фрагмент)
        )
        recyclerView.adapter = adapter

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

        if (sharedViewModel.medicines.value.isNullOrEmpty()) {
            sharedViewModel.loadMedicines()
        }

    }
    override fun onDeleteClick(medicine: Medicine) {
        // Вызываем метод ViewModel для удаления лекарства.
        // ViewModel заботится об обновлении Model и LiveData.
        sharedViewModel.deleteMedicine(medicine)

        // Опционально: можно добавить Toast для подтверждения
        Toast.makeText(requireContext(), "Удалено: ${medicine.name}", Toast.LENGTH_SHORT).show()
    }
}