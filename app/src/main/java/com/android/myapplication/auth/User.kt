package com.android.myapplication.auth

data class User(
    val email: String,
    val password: String,
    val username: String = "Anonymous"
)