package com.example.mypharmalab3.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.mypharmalab3.R // Убедись, что это твой R-файл

class HomeFragment : Fragment() {

    // 1. Метод для создания (инфлейта) макета
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // !!! ВАЖНО: Замени R.layout.fragment_home на имя твоего XML-макета
        return inflater.inflate(R.layout.activity_main, container, false)
    }

    // 2. Метод, который вызывается после создания View. Тут можно настраивать элементы.
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Например: val textView = view.findViewById<TextView>(R.id.my_text_view)
        // Инициализация тут
    }
}