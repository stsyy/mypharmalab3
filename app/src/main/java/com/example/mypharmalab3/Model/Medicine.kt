package com.example.mypharmalab3.Model

data class Medicine(
    val name: String,
    val expiryDate: String,
    val reminder: Boolean,
    val seasonal: Boolean
)