package com.example.mypharmalab3.db_entities

import org.greenrobot.greendao.annotation.Entity
import org.greenrobot.greendao.annotation.Id
import org.greenrobot.greendao.annotation.Generated
import org.greenrobot.greendao.annotation.ToOne
import org.greenrobot.greendao.DaoException
import org.greenrobot.greendao.annotation.NotNull
import org.jetbrains.annotations.NotNull

/**
 * Сущность для таблицы "Лекарства" (Medicine).
 * Добавлен внешний ключ (Foreign Key) doseTimeId для связи с DoseTime.
 */
@Entity
data class Medicine(
    // Уникальный ID, первичный ключ.
    @Id(autoincrement = true)
    var id: Long = 0,

    // Основные поля (как в Lab #3)
    var name: String = "",
    var expiryDate: String = "",
    var reminder: Boolean = false,
    var seasonal: Boolean = false,

    // ⭐️ ВНЕШНИЙ КЛЮЧ: Ссылка на id из таблицы DoseTime
    @NotNull
    var doseTimeId: Long
) {
    // ⭐️ greenDAO требует, чтобы внешние ключи были определены как @ToOne
    @ToOne(joinProperty = "doseTimeId")
    var doseTime: DoseTime? = null
        private set // Ограничиваем сеттер, чтобы greenDAO мог его использовать

    /** Used to resolve relations */
    @Generated(hash = 2040043836)
    @Throws(DaoException::class)
    fun __loadDaoSession(): com.example.mypharmalab3.db_entities.DaoSession {
        throw DaoException("Entity is detached from DAO context");
    }

    /** Convenient call for {@link org.greenrobot.greendao.AbstractDao#refresh(Object)}. */
    @Generated(hash = 1269382282)
    @Throws(DaoException::class)
    fun refresh() {
        // Здесь greenDAO сгенерирует код для обновления отношений
    }

    /** Convenient call for {@link org.greenrobot.greendao.AbstractDao#update(Object)}. */
    @Generated(hash = 711100067)
    @Throws(DaoException::class)
    fun update() {
        // Здесь greenDAO сгенерирует код для обновления объекта
    }

    /** Convenient call for {@link org.greenrobot.greendao.AbstractDao#delete(Object)}. */
    @Generated(hash = 1285586616)
    @Throws(DaoException::class)
    fun delete() {
        // Здесь greenDAO сгенерирует код для удаления объекта
    }

    // Пустой конструктор, требуемый greenDAO
    @Generated(hash = 746144577)
    constructor() : this(0, "", "", false, false, 0)

    // Конструктор, который ты будешь использовать для создания новых лекарств
    constructor(
        name: String,
        expiryDate: String,
        reminder: Boolean,
        seasonal: Boolean,
        doseTimeId: Long
    ) : this(0, name, expiryDate, reminder, seasonal, doseTimeId)
}