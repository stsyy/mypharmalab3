package com.example.mypharmalab3.View

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.mypharmalab3.Model.Medicine
import com.example.mypharmalab3.R

/**
 * Интерфейс-слушатель для обработки кликов и нажатий внутри элемента списка (ViewHolder).
 * HomeFragment будет реализовывать этот интерфейс, чтобы реагировать на действия пользователя.
 */
interface OnMedicineItemClickListener {
    fun onDeleteClick(medicine: Medicine)
    fun onLongClick(medicine: Medicine): Boolean // Добавляем обработчик для контекстного меню
}

/**
 * Адаптер, который связывает список объектов Medicine с RecyclerView.
 * Он управляет созданием элементов списка (ViewHolder) и привязкой данных.
 */
class MedicineAdapter(
    private var medicineList: List<Medicine>,

    private val itemClickListener: OnMedicineItemClickListener
) :
// Наследуемся от RecyclerView.Adapter и указываем, какой ViewHolder будет использоваться
    RecyclerView.Adapter<MedicineAdapter.MedicineViewHolder>() {


    /**
     * Внутренний класс ViewHolder. Обязателен для оптимизации RecyclerView.
     * Хранит ссылки на View-элементы одного пункта списка.
     */
    class MedicineViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        // Ссылки на View-компоненты макета medicine_list_item.
        // findViewById() вызывается ОДИН РАЗ при создании ViewHolder, что ускоряет прокрутку
        val nameTextView: TextView = itemView.findViewById(R.id.tvMedicineName)
        val expiryTextView: TextView = itemView.findViewById(R.id.tvExpiryDate)
        val deleteButton: ImageButton = itemView.findViewById(R.id.deleteButton)
    }

    /**
     * Вызывается, когда RecyclerView нужно создать новый ViewHolder (когда элемент впервые появляется на экране).
     * Создает визуальное представление одного элемента списка.
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MedicineViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.medicine_list_item, parent, false)
        return MedicineViewHolder(view)
    }

    /**
     * Вызывается для заполнения или обновления ViewHolder новыми данными.
     * Вызывается, когда элемент становится видимым (включая переиспользование).
     * @param holder Готовый к использованию или переиспользованный ViewHolder.
     * @param position Индекс текущего элемента в списке medicineList.
     */
    override fun onBindViewHolder(holder: MedicineViewHolder, position: Int) {
        val medicine = medicineList[position]
        // 1. Привязываем данные к сохраненным View-элементам
        holder.nameTextView.text = medicine.name
        holder.expiryTextView.text = "Срок годности: ${medicine.expiryDate}"
// 2. Устанавливаем обработчик клика для кнопки удаления (D1)
        holder.deleteButton.setOnClickListener {
            itemClickListener.onDeleteClick(medicine)
        }

        holder.itemView.setOnLongClickListener {
            itemClickListener.onLongClick(medicine)
        }
    }

    /**
     * Сообщает RecyclerView, сколько всего элементов в списке.
     */
    override fun getItemCount(): Int = medicineList.size

    /**
     * Публичный метод для обновления списка данных в адаптере.
     * Используется HomeFragment при изменении LiveData.
     * @param newMedicineList Новый список лекарств.
     */
    fun updateData(newMedicineList: List<Medicine>) {
        medicineList = newMedicineList
        notifyDataSetChanged()
    }
}