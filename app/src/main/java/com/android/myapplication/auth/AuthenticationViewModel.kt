package com.android.myapplication.auth

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class AuthenticationViewModel : ViewModel() {
    private val users: MutableMap<String, User> = mutableMapOf()

    val _uiState: MutableStateFlow<UiState> = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()
    val _currentUser: MutableStateFlow<User?> = MutableStateFlow(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    fun onEmailChange(newEmail: String) {
        _currentUser.update {
            it?.copy(email = newEmail) ?: User(email = newEmail, password = "")
        }
    }

    fun onPasswordChange(newPassword: String) {
        _currentUser.update {
            it?.copy(password = newPassword) ?: User(
                email = "",
                password = newPassword,
            )
        }
    }

    private fun setUsername(email: String): String {
        return email.substringBefore("@")
    }

    fun registerNewUser() {
        val user = _currentUser.value
        if (user == null) {
            _uiState.value = UiState(
                isLoading = false,
                errorMessage = "Please enter email and password",
                isLoggedIn = false
            )
            return
        }

        if (!validateCredentials(user.email, user.password)) {
            _uiState.value = UiState(
                isLoading = false,
                errorMessage = "Invalid email or password",
                isLoggedIn = false
            )
            return
        }

        if (users.containsKey(user.email)) {
            _uiState.value = UiState(
                isLoading = false,
                errorMessage = "User already exists",
                isLoggedIn = false
            )
            return
        }

        val newUser = user.copy(username = setUsername(user.email))
        users[user.email] = newUser
        _currentUser.value = newUser
        _uiState.value = UiState(
            isLoading = false,
            errorMessage = null,
            isLoggedIn = true
        )
    }

    fun loginUser() {
        val user = _currentUser.value
        if (user == null) {
            _uiState.value = UiState(
                isLoading = false,
                errorMessage = "Please enter email and password",
                isLoggedIn = false
            )
            return
        }

        val stored = users[user.email]
        if (stored == null || stored.password != user.password) {
            _uiState.value = UiState(
                isLoading = false,
                errorMessage = "Invalid email or password",
                isLoggedIn = false
            )
            return
        }

        _currentUser.value = stored
        _uiState.value = UiState(
            isLoading = false,
            errorMessage = null,
            isLoggedIn = true
        )
    }

    fun logoutUser() {
        _currentUser.value = null
        _uiState.value = UiState(
            isLoading = false,
            errorMessage = null,
            isLoggedIn = false
        )
    }

    private fun validateCredentials(email: String, password: String): Boolean {
        val regex = "^[A-Za-z](.*)(@)(.+)(\\.)(.+)"
        if (!email.matches(regex.toRegex())) {
            return false
        }
        return email.isNotEmpty() && password.length >= 6
    }
}