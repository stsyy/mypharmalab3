package com.example.mypharmalab3.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResultListener
import com.example.mypharmalab3.R

class HomeFragment : Fragment() {

    private lateinit var resultTextView: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        resultTextView = view.findViewById(R.id.tv_result_message)

        setFragmentResultListener("add_medicine_request") { requestKey, bundle ->
            if (requestKey == "add_medicine_request") {
                val resultMessage = bundle.getString("result_message")
                val medicineName = bundle.getString("medicine_name_added")

                if (!resultMessage.isNullOrBlank()) {

                    val displayMessage = "✅ Успешно добавлено: ${medicineName}\nСообщение: ${resultMessage}"
                    resultTextView.text = displayMessage

                    Toast.makeText(requireContext(), "Данные получены:\n$resultMessage", Toast.LENGTH_LONG).show()
                }
            }
        }

    }
}