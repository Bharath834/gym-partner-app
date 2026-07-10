package com.example.data.repository

import android.content.Context
import com.example.data.db.AppDatabase
import com.example.data.model.ChatMessage
import com.example.data.model.GymGoerProfile
import com.example.data.model.UserMatch
import com.example.data.model.UserProfile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext

class GymRepository(context: Context) {
    private val db = AppDatabase.getDatabase(context)
    private val userProfileDao = db.userProfileDao()
    private val gymGoerDao = db.gymGoerDao()
    private val userMatchDao = db.userMatchDao()
    private val chatMessageDao = db.chatMessageDao()

    val userProfile: Flow<UserProfile?> = userProfileDao.getUserProfile()
    val allGymGoers: Flow<List<GymGoerProfile>> = gymGoerDao.getAllGymGoers()
    val allMatches: Flow<List<UserMatch>> = userMatchDao.getAllMatches()

    suspend fun saveUserProfile(profile: UserProfile) = withContext(Dispatchers.IO) {
        userProfileDao.insertUserProfile(profile)
        // Dynamically update pre-seeded gymgoers' city to match user's city so they always find matches!
        val currentGymGoers = gymGoerDao.getAllGymGoers().firstOrNull() ?: emptyList()
        if (currentGymGoers.isNotEmpty()) {
            val updated = currentGymGoers.map { it.copy(city = profile.city) }
            gymGoerDao.insertGymGoers(updated)
        }
    }

    fun getMessagesForMatch(matchId: String): Flow<List<ChatMessage>> {
        return chatMessageDao.getMessagesForMatch(matchId)
    }

    suspend fun sendMessage(matchId: String, content: String, senderId: String = "me") = withContext(Dispatchers.IO) {
        val msg = ChatMessage(matchId = matchId, senderId = senderId, content = content)
        chatMessageDao.insertMessage(msg)
    }

    suspend fun performSwipe(gymGoerId: String, liked: Boolean): UserMatch? = withContext(Dispatchers.IO) {
        val compositeId = "me_$gymGoerId"
        
        // Retrieve or determine if they liked us back
        // For a beautiful simulation, let's say some liked us, and some didn't.
        // We look up the gymGoer to see their name or just check their ID to decide if they liked us back.
        val gymGoer = gymGoerDao.getGymGoerById(gymGoerId)
        val likedByThem = if (liked) {
            // High match rate for a great demo experience! 
            // Odd ids like us back, even ones don't, or let's say 75% match rate.
            gymGoerId.hashCode() % 4 != 0
        } else {
            false
        }

        val isMatch = liked && likedByThem

        val match = UserMatch(
            id = compositeId,
            gymGoerId = gymGoerId,
            likedByMe = liked,
            likedByThem = likedByThem,
            isMatch = isMatch
        )
        userMatchDao.insertMatch(match)

        if (isMatch && gymGoer != null) {
            // Trigger an automated greeting from the matched partner after a tiny delay
            // (Simulated as if they texted you first once you matched!)
            val introMessage = getIntroMessageForGymGoer(gymGoer)
            val msg = ChatMessage(
                matchId = gymGoerId,
                senderId = gymGoerId,
                content = introMessage,
                timestamp = System.currentTimeMillis() + 1000 // slightly in future to appear after match
            )
            chatMessageDao.insertMessage(msg)
        }

        return@withContext if (isMatch) match else null
    }

    private fun getIntroMessageForGymGoer(gymGoer: GymGoerProfile): String {
        return when (gymGoer.workoutStyle) {
            "Powerlifting" -> "Hey! High bar or low bar squats? 🏋️‍♂️ Awesome matching with you. I'm hitting deadlifts tomorrow, need a solid training partner!"
            "Bodybuilding" -> "Hey there! Ready to build some serious muscle? 💪 What's your current routine look like? Let's smash a chest day!"
            "Cardio / HIIT" -> "Hey! Down for a track workout or a high-intensity circuit? 🏃‍♂️ I usually go to ${gymGoer.gymName}. Let's do some conditioning!"
            "Yoga / Pilates" -> "Hello! Always good to connect with a fellow yogi/pilates practitioner. Let's keep each other accountable on flexibility and core! 🧘‍♀️"
            else -> "Hey! Great matching with you. I usually lift around ${gymGoer.timeSlot} at ${gymGoer.gymName}. Let's train together soon! 🤝"
        }
    }

    // Seed the database with high-quality simulated gym-goers
    suspend fun seedDatabaseIfEmpty() = withContext(Dispatchers.IO) {
        val currentProfiles = gymGoerDao.getAllGymGoers().firstOrNull() ?: emptyList()
        if (currentProfiles.isEmpty()) {
            val defaultCity = "New York"
            val mockGymGoers = listOf(
                GymGoerProfile(
                    id = "partner_1",
                    name = "Alex Rivera",
                    age = 24,
                    city = defaultCity,
                    gymName = "Gold's Gym",
                    workoutStyle = "Bodybuilding",
                    timeSlot = "Evening (5PM - 8PM)",
                    avatarId = "avatar_beast",
                    bio = "Trying to prep for my first amateur physique show! Need someone to hit heavy sets and keep high intensity. Focused, but love to joke around. Let's grow! 🦍",
                    isVerified = true,
                    hideLocationUntilMatch = true
                ),
                GymGoerProfile(
                    id = "partner_2",
                    name = "Jessica Taylor",
                    age = 27,
                    city = defaultCity,
                    gymName = "Equinox",
                    workoutStyle = "Yoga / Pilates",
                    timeSlot = "Morning (6AM - 10AM)",
                    avatarId = "avatar_yoga",
                    bio = "Runner turned flexibility practitioner. Looking for a positive partner to keep consistency during early morning sessions. Certified yoga teacher, happy to guide! 🧘‍♀️✨",
                    isVerified = true,
                    hideLocationUntilMatch = false
                ),
                GymGoerProfile(
                    id = "partner_3",
                    name = "Marcus Vance",
                    age = 29,
                    city = defaultCity,
                    gymName = "Powerhouse Gym",
                    workoutStyle = "Powerlifting",
                    timeSlot = "Late Night (8PM - 12AM)",
                    avatarId = "avatar_power",
                    bio = "Current squat: 495, bench: 315, deadlift: 585. Looking for an experienced spotter and heavy training partner. Let's chase some PRs. 🏋️‍♂️☠️",
                    isVerified = false,
                    hideLocationUntilMatch = true
                ),
                GymGoerProfile(
                    id = "partner_4",
                    name = "Chloe Chen",
                    age = 22,
                    city = defaultCity,
                    gymName = "LA Fitness",
                    workoutStyle = "Cardio / HIIT",
                    timeSlot = "Afternoon (12PM - 4PM)",
                    avatarId = "avatar_cardio",
                    bio = "I do high-intensity functional training, jump rope, and heavy bags. Looking for a gym buddy who won't mind sweating through intense circuits together. 🥊🔥",
                    isVerified = true,
                    hideLocationUntilMatch = true
                ),
                GymGoerProfile(
                    id = "partner_5",
                    name = "Danny Patel",
                    age = 31,
                    city = defaultCity,
                    gymName = "Planet Fitness",
                    workoutStyle = "General Fitness",
                    timeSlot = "Evening (5PM - 8PM)",
                    avatarId = "avatar_flex",
                    bio = "Working professional trying to stay healthy. Workout schedule is consistent. Looking for an accountability buddy to make sure I don't skip gym days after work!",
                    isVerified = false,
                    hideLocationUntilMatch = false
                ),
                GymGoerProfile(
                    id = "partner_6",
                    name = "Sophie Dubois",
                    age = 25,
                    city = defaultCity,
                    gymName = "Iron Temple Gym",
                    workoutStyle = "Powerlifting",
                    timeSlot = "Morning (6AM - 10AM)",
                    avatarId = "avatar_speed",
                    bio = "Competitive powerlifter. Looking for serious lifting partners. Focus is on technique, form, and lifting heavy. Let's lift!",
                    isVerified = true,
                    hideLocationUntilMatch = true
                )
            )
            gymGoerDao.insertGymGoers(mockGymGoers)
        }

        // Initialize user profile if empty
        val currentLocalProfile = userProfileDao.getUserProfileDirect()
        if (currentLocalProfile == null) {
            val defaultUser = UserProfile(
                name = "Chris Evans",
                age = 25,
                city = "New York",
                gymName = "Gold's Gym",
                workoutStyle = "Bodybuilding",
                timeSlot = "Evening (5PM - 8PM)",
                avatarId = "avatar_beast",
                isVerified = false,
                verificationType = "",
                verificationDetails = "",
                hideLocationUntilMatch = true
            )
            userProfileDao.insertUserProfile(defaultUser)
        }
    }
}
