package com.example.mypharmalab3.View

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.mypharmalab3.Model.Medicine
import com.example.mypharmalab3.R

// ⭐️ ОБНОВЛЕННЫЙ ИНТЕРФЕЙС: Добавляем метод для долгого нажатия
interface OnMedicineItemClickListener {
    fun onDeleteClick(medicine: Medicine)
    fun onLongClick(medicine: Medicine): Boolean // Добавляем обработчик для контекстного меню
}

class MedicineAdapter(
    private var medicineList: List<Medicine>,
    // ⭐️ ИСПОЛЬЗУЕМ ОБЩИЙ ИНТЕРФЕЙС
    private val itemClickListener: OnMedicineItemClickListener
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

        // Обработка удаления (D1)
        holder.deleteButton.setOnClickListener {
            itemClickListener.onDeleteClick(medicine)
        }

        // ⭐️ ОБРАБОТКА ДОЛГОГО НАЖАТИЯ (U1)
        holder.itemView.setOnLongClickListener {
            // Возвращаем результат из обработчика (true, если событие обработано)
            itemClickListener.onLongClick(medicine)
        }
    }

    override fun getItemCount(): Int = medicineList.size

    fun updateData(newMedicineList: List<Medicine>) {
        medicineList = newMedicineList
        notifyDataSetChanged()
    }
}