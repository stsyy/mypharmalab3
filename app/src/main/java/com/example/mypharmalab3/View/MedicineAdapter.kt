// Файл MedicineAdapter.kt
package com.example.mypharmalab3.View

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.mypharmalab3.Model.Medicine // Убедитесь, что путь верный
import com.example.mypharmalab3.R
import android.widget.ImageButton

interface OnDeleteClickListener {
    fun onDeleteClick(medicine: Medicine)
}

class MedicineAdapter(private var medicineList: List<Medicine>,
                      private val deleteClickListener: OnDeleteClickListener
) :
    RecyclerView.Adapter<MedicineAdapter.MedicineViewHolder>() {

    class MedicineViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val nameTextView: TextView = itemView.findViewById(R.id.tvMedicineName)
        val expiryTextView: TextView = itemView.findViewById(R.id.tvExpiryDate)
        val deleteButton: ImageButton = itemView.findViewById(R.id.deleteButton)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MedicineViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.medicine_list_item, parent, false)
        return MedicineViewHolder(view)
    }

    override fun onBindViewHolder(holder: MedicineViewHolder, position: Int) {
        val medicine = medicineList[position]
        holder.nameTextView.text = medicine.name
        holder.expiryTextView.text = "Срок годности: ${medicine.expiryDate}"
        holder.deleteButton.setOnClickListener {
            // Вызываем метод интерфейса, который передаст событие во HomeFragment
            deleteClickListener.onDeleteClick(medicine)
        }
    }

    override fun getItemCount(): Int = medicineList.size

    // Функция для обновления данных (нужна, когда вы добавляете новое лекарство)
    fun updateData(newMedicineList: List<Medicine>) {
        medicineList = newMedicineList
        notifyDataSetChanged()
    }
}