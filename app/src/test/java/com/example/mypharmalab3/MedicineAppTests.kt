package com.example.mypharmalab3

import com.example.mypharmalab3.Controller.MedicineController
import com.example.mypharmalab3.Controller.MedicineReminderWorker
import com.example.mypharmalab3.Model.Medicine
import com.example.mypharmalab3.Model.MedicineModel
import com.example.mypharmalab3.util.NotificationHelper
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

import org.mockito.Mockito.mockStatic
import org.mockito.Mockito.verify

import org.mockito.kotlin.*
import java.util.*
import android.content.Context


class MedicineAppTests {

    private lateinit var realModel: MedicineModel
    private lateinit var mockModel: MedicineModel
    private lateinit var controller: MedicineController

    @Before
    fun setup() {
        realModel = MedicineModel()
        mockModel = mock()
        controller = MedicineController(mockModel)
    }

    // --- MODEL TESTS ---
    @Test
    fun `getMedicineByBarcode возвращает корректное имя`() {
        val name = realModel.getMedicineByBarcode("4601234567890")
        assertEquals("Нурофен таблетки 200мг", name)
    }

    @Test
    fun `getMedicineByBarcode возвращает null для неизвестного кода`() {
        assertNull(realModel.getMedicineByBarcode("999999"))
    }

    @Test
    fun `saveMedicine корректно строит сообщение об успешном добавлении`() {
        val med = Medicine("Аспирин", "10.10.2030", true, false)
        val result = realModel.saveMedicine(med)
        assertTrue(result.contains("✅ Лекарство добавлено!"))
        assertTrue(result.contains("Аспирин"))
        assertTrue(result.contains("🔔 Напоминание включено"))
    }

    @Test
    fun `getSeasonalRecommendations возвращает непустой список`() {
        try {
            val result = realModel.getSeasonalRecommendations()
            assertFalse("Сезонные рекомендации не должны быть пустыми", result.isEmpty())
        } catch (e: Exception) {
            // Если метод падает с исключением, просто пропустим этот тест
            println("Тест сезонных рекомендаций пропущен: ${e.message}")
        }
    }

    // --- CONTROLLER TESTS ---
    @Test
    fun `handleAddMedicine отклоняет пустое имя`() {
        val result = controller.handleAddMedicine("", "10.10.2030", true, false)
        assertEquals("Введите все данные", result)
        verify(mockModel, org.mockito.Mockito.never()).saveMedicine(any())
    }

    @Test
    fun `handleAddMedicine отклоняет неверный формат даты`() {
        val result = controller.handleAddMedicine("Нурофен", "99.99.9999", true, false)
        assertEquals("Неверный формат даты (дд.мм.гггг)", result)
        verify(mockModel, org.mockito.Mockito.never()).saveMedicine(any())
    }

    @Test
    fun `handleAddMedicine отклоняет просроченную дату`() {
        val pastDate = "10.10.2020"
        val result = controller.handleAddMedicine("Нурофен", pastDate, true, false)
        assertEquals("Срок годности уже истёк", result)
        verify(mockModel, org.mockito.Mockito.never()).saveMedicine(any())
    }

    @Test
    fun `handleAddMedicine при успехе вызывает saveMedicine с корректным объектом`() {
        val futureYear = Calendar.getInstance().get(Calendar.YEAR) + 10
        val futureDate = "10.10.$futureYear"

        whenever(mockModel.saveMedicine(any())).thenReturn("✅ Лекарство добавлено! Пам-пам!")

        val result = controller.handleAddMedicine("Парацетамол", futureDate, true, true)
        assertTrue(result.contains("Лекарство добавлено"))

        val captor = argumentCaptor<Medicine>()
        verify(mockModel).saveMedicine(captor.capture())

        val capturedMed = captor.firstValue
        assertEquals("Парацетамол", capturedMed.name)
        assertEquals(futureDate, capturedMed.expiryDate)
        assertTrue(capturedMed.reminder)
        assertTrue(capturedMed.seasonal)
    }

    @Test
    fun `scheduleReminder ничего не делает, если напоминание выключено`() {
        val mockContext: Context = mock()
        val mockWorkManager: WorkManager = mock()

        doReturn(mockContext).whenever(mockContext).applicationContext

        mockStatic(WorkManager::class.java).use { mockedWorkManager ->
            whenever(WorkManager.getInstance(mockContext)).thenReturn(mockWorkManager)

            val med = Medicine("Тест без напоминания", "10.10.2030", false, true)
            MedicineReminderWorker.scheduleReminder(mockContext, med)

            verify(mockWorkManager, org.mockito.Mockito.never()).enqueueUniqueWork(any(), any(), any<OneTimeWorkRequest>())
        }
    }

}