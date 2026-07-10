package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_profile")
data class UserProfile(
    @PrimaryKey val id: Int = 1, // Single local user
    val name: String,
    val age: Int,
    val city: String,
    val gymName: String,
    val workoutStyle: String, // e.g. "Bodybuilding", "Powerlifting", "Cardio", etc.
    val timeSlot: String,     // e.g. "Morning (6AM - 10AM)", "Afternoon (12PM - 4PM)", etc.
    val avatarId: String,     // e.g. "avatar_1", "avatar_2"
    val isVerified: Boolean = false,
    val verificationType: String = "", // "Gym Card" or "Selfie"
    val verificationDetails: String = "",
    val hideLocationUntilMatch: Boolean = true
)

@Entity(tableName = "gymgoer_profiles")
data class GymGoerProfile(
    @PrimaryKey val id: String,
    val name: String,
    val age: Int,
    val city: String,
    val gymName: String,
    val workoutStyle: String,
    val timeSlot: String,
    val avatarId: String,
    val bio: String,
    val isVerified: Boolean = false,
    val hideLocationUntilMatch: Boolean = true
)

@Entity(tableName = "user_matches")
data class UserMatch(
    @PrimaryKey val id: String, // composite id "me_${gymGoerId}"
    val gymGoerId: String,
    val likedByMe: Boolean,
    val likedByThem: Boolean,
    val isMatch: Boolean
)

@Entity(tableName = "chat_messages")
data class ChatMessage(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val matchId: String, // references GymGoerProfile.id
    val senderId: String, // "me" or GymGoerProfile.id
    val content: String,
    val timestamp: Long = System.currentTimeMillis()
)
