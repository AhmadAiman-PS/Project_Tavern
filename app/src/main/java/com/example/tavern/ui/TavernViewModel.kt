package com.example.tavern.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.tavern.data.PostEntity
import com.example.tavern.data.TavernRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class TavernViewModel(private val repository: TavernRepository) : ViewModel() {

    // Converts the Flow from Room into a StateFlow for Compose to observe [cite: 24]
    val uiState: StateFlow<List<PostEntity>> = repository.allPosts
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Function to add data, running on a background coroutine [cite: 24, 165]
    fun createPost(title: String, content: String) {
        viewModelScope.launch {
            val newPost = PostEntity(
                author = "u/TavernKeeper", // Hardcoded for now
                title = title,
                content = content,
                upvotes = (0..100).random()
            )
            repository.addPost(newPost)
        }
    }
}

// Factory to help creating the ViewModel with dependencies
class TavernViewModelFactory(private val repository: TavernRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TavernViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return TavernViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}