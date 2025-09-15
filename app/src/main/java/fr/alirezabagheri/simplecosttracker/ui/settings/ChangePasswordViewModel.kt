package fr.alirezabagheri.simplecosttracker.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class ChangePasswordViewModel : ViewModel() {

    private val auth = FirebaseAuth.getInstance()

    val currentPassword = MutableStateFlow("")
    val newPassword = MutableStateFlow("")
    val confirmPassword = MutableStateFlow("")

    private val _uiState = MutableStateFlow<ChangePasswordState>(ChangePasswordState.Idle)
    val uiState = _uiState.asStateFlow()

    fun changePassword() {
        if (newPassword.value != confirmPassword.value) {
            _uiState.value = ChangePasswordState.Error("New passwords do not match.")
            return
        }
        if (newPassword.value.length < 6) {
            _uiState.value = ChangePasswordState.Error("Password must be at least 6 characters.")
            return
        }

        viewModelScope.launch {
            _uiState.value = ChangePasswordState.Loading
            try {
                val user = auth.currentUser
                if (user?.email != null) {
                    val credential = EmailAuthProvider.getCredential(user.email!!, currentPassword.value)
                    user.reauthenticate(credential).await()
                    user.updatePassword(newPassword.value).await()
                    _uiState.value = ChangePasswordState.Success
                } else {
                    _uiState.value = ChangePasswordState.Error("User not found.")
                }
            } catch (e: Exception) {
                _uiState.value = ChangePasswordState.Error(e.message ?: "An unknown error occurred.")
            }
        }
    }

    fun resetState() {
        _uiState.value = ChangePasswordState.Idle
    }
}

sealed class ChangePasswordState {
    object Idle : ChangePasswordState()
    object Loading : ChangePasswordState()
    object Success : ChangePasswordState()
    data class Error(val message: String) : ChangePasswordState()
}