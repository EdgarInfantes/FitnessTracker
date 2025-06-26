package dev.einfantesv.fitnesstracker

import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import dev.einfantesv.fitnesstracker.data.remote.firebase.FirebaseGetDataManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class RankingViewModel : ViewModel() {
    private val _ranking = MutableStateFlow<List<String>>(emptyList())
    val ranking: StateFlow<List<String>> = _ranking

    private val auth = FirebaseAuth.getInstance()

    fun loadFriendRanking() {
        val currentUser = auth.currentUser ?: return

        FirebaseGetDataManager.getUserFriends(currentUser.uid) { friendUids ->
            // Agregamos el propio UID para incluir al usuario actual
            val uidsToCheck = friendUids + currentUser.uid

            FirebaseGetDataManager.getStepCountsForUids(uidsToCheck) { stepsMap ->
                val sortedUids = stepsMap.entries
                    .sortedByDescending { it.value }
                    .map { it.key }
                    .take(3)

                _ranking.value = sortedUids
            }
        }
    }
}
