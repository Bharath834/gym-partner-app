package com.example.data.db

import android.content.Context
import androidx.room.*
import com.example.data.model.ChatMessage
import com.example.data.model.GymGoerProfile
import com.example.data.model.UserMatch
import com.example.data.model.UserProfile
import kotlinx.coroutines.flow.Flow

@Dao
interface UserProfileDao {
    @Query("SELECT * FROM user_profile WHERE id = 1 LIMIT 1")
    fun getUserProfile(): Flow<UserProfile?>

    @Query("SELECT * FROM user_profile WHERE id = 1 LIMIT 1")
    suspend fun getUserProfileDirect(): UserProfile?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUserProfile(profile: UserProfile)
}

@Dao
interface GymGoerDao {
    @Query("SELECT * FROM gymgoer_profiles")
    fun getAllGymGoers(): Flow<List<GymGoerProfile>>

    @Query("SELECT * FROM gymgoer_profiles WHERE id = :id LIMIT 1")
    suspend fun getGymGoerById(id: String): GymGoerProfile?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGymGoers(profiles: List<GymGoerProfile>)
}

@Dao
interface UserMatchDao {
    @Query("SELECT * FROM user_matches")
    fun getAllMatches(): Flow<List<UserMatch>>

    @Query("SELECT * FROM user_matches WHERE gymGoerId = :gymGoerId LIMIT 1")
    suspend fun getMatchForGymGoer(gymGoerId: String): UserMatch?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMatch(match: UserMatch)
}

@Dao
interface ChatMessageDao {
    @Query("SELECT * FROM chat_messages WHERE matchId = :matchId ORDER BY timestamp ASC")
    fun getMessagesForMatch(matchId: String): Flow<List<ChatMessage>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: ChatMessage)
}

@Database(
    entities = [
        UserProfile::class,
        GymGoerProfile::class,
        UserMatch::class,
        ChatMessage::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userProfileDao(): UserProfileDao
    abstract fun gymGoerDao(): GymGoerDao
    abstract fun userMatchDao(): UserMatchDao
    abstract fun chatMessageDao(): ChatMessageDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "gym_partner_match_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
