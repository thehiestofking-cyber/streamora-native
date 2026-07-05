package site.everyniche.streamora.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import site.everyniche.streamora.data.model.ContentItem
import site.everyniche.streamora.data.network.SupabaseData
import site.everyniche.streamora.data.repo.HomeData
import site.everyniche.streamora.data.repo.StreamoraRepository

data class HomeUiState(
    val loading: Boolean = true,
    val data: HomeData? = null,
    val continueWatching: List<ContentItem> = emptyList(),
    val activeCategory: String = "For You",
    val error: String? = null,
)

class HomeViewModel(
    private val repo: StreamoraRepository,
    private val supabaseData: SupabaseData,
) : ViewModel() {

    private val _state = MutableStateFlow(HomeUiState())
    val state: StateFlow<HomeUiState> = _state

    init {
        loadCategory("For You")
        loadContinueWatching()
    }

    fun loadContinueWatching() {
        viewModelScope.launch {
            val rows = supabaseData.getHistory(12)
            val items = rows.map {
                ContentItem(
                    subjectId = it.subject_id,
                    subjectType = it.subject_type ?: 1,
                    title = it.title ?: "Untitled",
                    cover = it.cover,
                    detailPath = it.detail_path,
                )
            }
            _state.value = _state.value.copy(continueWatching = items)
        }
    }

    fun loadCategory(category: String) {
        viewModelScope.launch {
            _state.value = _state.value.copy(loading = _state.value.data == null, activeCategory = category)
            runCatching { repo.getCategoryData(category) }
                .onSuccess { _state.value = _state.value.copy(loading = false, data = it, error = null) }
                .onFailure { _state.value = _state.value.copy(loading = false, error = it.message) }
        }
    }
}
