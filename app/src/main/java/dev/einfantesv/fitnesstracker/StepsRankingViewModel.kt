package dev.einfantesv.fitnesstracker

import androidx.lifecycle.ViewModel
import dev.einfantesv.fitnesstracker.data.remote.firebase.FirebaseGetDataManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class RankingViewModel : ViewModel() {
    private val _ranking = MutableStateFlow<List<String>>(emptyList())
    val ranking: StateFlow<List<String>> = _ranking

    init {
        loadRanking()
    }

    fun loadRanking() {
        FirebaseGetDataManager.getTopStepUsers { uids ->
            _ranking.value = uids
        }
    }
}
