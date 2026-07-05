package site.everyniche.streamora.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import site.everyniche.streamora.AppContainer
import site.everyniche.streamora.ui.screens.HomeViewModel

/** Minimal manual ViewModelProvider.Factory (no Hilt needed for this app size). */
class ViewModelFactory(private val container: AppContainer) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when (modelClass) {
            HomeViewModel::class.java -> HomeViewModel(container.repository, container.supabaseData) as T
            else -> throw IllegalArgumentException("Unknown ViewModel class: $modelClass")
        }
    }
}
