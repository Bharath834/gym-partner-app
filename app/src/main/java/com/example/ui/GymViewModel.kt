package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.ChatMessage
import com.example.data.model.GymGoerProfile
import com.example.data.model.UserMatch
import com.example.data.model.UserProfile
import com.example.data.repository.GymRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class GymViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = GymRepository(application)

    // Reactive states from Room
    val userProfile: StateFlow<UserProfile?> = repository.userProfile
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val allGymGoers: StateFlow<List<GymGoerProfile>> = repository.allGymGoers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allMatches: StateFlow<List<UserMatch>> = repository.allMatches
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Active state for discovery / matching
    private val _currentMatchCelebration = MutableStateFlow<GymGoerProfile?>(null)
    val currentMatchCelebration: StateFlow<GymGoerProfile?> = _currentMatchCelebration.asStateFlow()

    // Filtering states
    val workoutStyleFilter = MutableStateFlow<String>("All")
    val timeSlotFilter = MutableStateFlow<String>("All")

    // Active screen navigation helper states
    private val _activeChatPartner = MutableStateFlow<GymGoerProfile?>(null)
    val activeChatPartner: StateFlow<GymGoerProfile?> = _activeChatPartner.asStateFlow()

    // Chat messages list flows reactively when active partner is selected
    val activeChatMessages: StateFlow<List<ChatMessage>> = activeChatPartner
        .flatMapLatest { partner ->
            if (partner == null) flowOf(emptyList())
            else repository.getMessagesForMatch(partner.id)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        viewModelScope.launch {
            // Seed database immediately on launch if empty
            repository.seedDatabaseIfEmpty()
        }
    }

    // Discovery: filter out gymgoers that have already been liked/passed by user
    val discoveryQueue: StateFlow<List<GymGoerProfile>> = combine(
        allGymGoers,
        allMatches,
        userProfile,
        workoutStyleFilter,
        timeSlotFilter
    ) { gymGoers, matches, uProfile, style, time ->
        if (uProfile == null) return@combine emptyList()

        val swipedIds = matches.map { it.gymGoerId }.toSet()
        gymGoers.filter { partner ->
            // Filter out already swiped users
            !swipedIds.contains(partner.id) &&
            // Must be in the user's local location (matching city)
            partner.city.trim().equals(uProfile.city.trim(), ignoreCase = true) &&
            // Filter by selected workout style if not "All"
            (style == "All" || partner.workoutStyle == style) &&
            // Filter by selected timeslot if not "All"
            (time == "All" || partner.timeSlot == time) &&
            // Age criteria filter (18-35 range)
            partner.age in 18..35
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun swipe(gymGoer: GymGoerProfile, liked: Boolean) {
        viewModelScope.launch {
            val resultMatch = repository.performSwipe(gymGoer.id, liked)
            if (resultMatch != null && resultMatch.isMatch) {
                // Trigger match overlay
                _currentMatchCelebration.value = gymGoer
            }
        }
    }

    fun dismissCelebration() {
        _currentMatchCelebration.value = null
    }

    fun selectChatPartner(partner: GymGoerProfile?) {
        _activeChatPartner.value = partner
    }

    fun sendChatMessage(content: String) {
        val partner = _activeChatPartner.value ?: return
        if (content.isBlank()) return
        viewModelScope.launch {
            repository.sendMessage(partner.id, content, senderId = "me")
        }
    }

    fun updateProfile(
        name: String,
        age: Int,
        city: String,
        gymName: String,
        workoutStyle: String,
        timeSlot: String,
        avatarId: String,
        hideLocationUntilMatch: Boolean
    ) {
        viewModelScope.launch {
            val current = userProfile.value ?: return@launch
            val updated = current.copy(
                name = name,
                age = age,
                city = city,
                gymName = gymName,
                workoutStyle = workoutStyle,
                timeSlot = timeSlot,
                avatarId = avatarId,
                hideLocationUntilMatch = hideLocationUntilMatch
            )
            repository.saveUserProfile(updated)
        }
    }

    fun requestVerification(type: String, details: String) {
        viewModelScope.launch {
            val current = userProfile.value ?: return@launch
            val updated = current.copy(
                isVerified = true,
                verificationType = type,
                verificationDetails = details
            )
            repository.saveUserProfile(updated)
        }
    }

    fun resetVerification() {
        viewModelScope.launch {
            val current = userProfile.value ?: return@launch
            val updated = current.copy(
                isVerified = false,
                verificationType = "",
                verificationDetails = ""
            )
            repository.saveUserProfile(updated)
        }
    }

    // List of matches that are double-opt-in success (both swiped true)
    val matchedGymGoers: StateFlow<List<GymGoerProfile>> = combine(
        allGymGoers,
        allMatches
    ) { gymGoers, matches ->
        val matchedIds = matches.filter { it.isMatch }.map { it.gymGoerId }.toSet()
        gymGoers.filter { matchedIds.contains(it.id) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
}
