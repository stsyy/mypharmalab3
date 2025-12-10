package com.example.mypharmalab3.db_entities

import org.greenrobot.greendao.annotation.Entity
import org.greenrobot.greendao.annotation.Id
import org.greenrobot.greendao.annotation.Generated
import javax.annotation.processing.Generated

/**
 * Сущность для таблицы "Время приема" (DoseTime).
 * Это справочная таблица (справочник) с фиксированными значениями: Утро, День, Вечер, Ночь.
 * Устанавливаем Primary Key (ключ) и другие поля.
 */
@Entity
data class DoseTime(
    // Уникальный ID, первичный ключ.
    @Id(autoincrement = true)
    var id: Long = 0,

    // Название времени приема (например, "Утро")
    var label: String,

    // Порядок сортировки (1 для Утра, 4 для Ночи)
    var sortOrder: Int
) {
    // Пустой конструктор, требуемый greenDAO.
    @Generated(hash = 1533722971)
    constructor() : this(0, "", 0)

    // Конструктор без ID для создания новых объектов перед сохранением.
    constructor(label: String, sortOrder: Int) : this(0, label, sortOrder)
}