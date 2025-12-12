package com.example.tavern.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.tavern.data.PostEntity
import com.example.tavern.data.TavernRepository
import com.example.tavern.data.UserEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class TavernViewModel(private val repository: TavernRepository) : ViewModel() {

    val uiState: StateFlow<List<PostEntity>> = repository.allPosts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Tracks the current logged-in user (Null means logged out)
    private val _currentUser = MutableStateFlow<UserEntity?>(null)
    val currentUser = _currentUser.asStateFlow()

    // Tracks login errors (like "Wrong password")
    private val _loginError = MutableStateFlow<String?>(null)
    val loginError = _loginError.asStateFlow()

    fun login(user: String, pass: String) {
        viewModelScope.launch {
            val validUser = repository.login(user, pass)
            if (validUser != null) {
                _currentUser.value = validUser
                _loginError.value = null
            } else {
                _loginError.value = "Invalid Name or Password, traveler."
            }
        }
    }

    fun register(user: String, pass: String) {
        viewModelScope.launch {
            val success = repository.register(UserEntity(user, pass))
            if (success) {
                login(user, pass) // Auto-login after register
            } else {
                _loginError.value = "That name is already taken!"
            }
        }
    }

    fun logout() {
        _currentUser.value = null
    }

    fun createPost(title: String, content: String) {
        val authorName = _currentUser.value?.username ?: "Anonymous" // Use real name
        viewModelScope.launch {
            repository.addPost(PostEntity(
                author = authorName,
                title = title,
                content = content,
                upvotes = (0..100).random()
            ))
        }
    }
}

class TavernViewModelFactory(private val repository: TavernRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TavernViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return TavernViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}