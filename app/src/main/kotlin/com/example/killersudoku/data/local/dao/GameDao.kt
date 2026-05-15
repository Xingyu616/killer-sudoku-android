package com.example.killersudoku.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.killersudoku.data.local.entity.GameEntity
import com.example.killersudoku.data.local.entity.GameHistoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface GameDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(game: GameEntity): Long

    @Update
    suspend fun update(game: GameEntity)

    @Query("SELECT * FROM games WHERE id = :gameId")
    suspend fun getById(gameId: Long): GameEntity?

    @Query("SELECT * FROM games ORDER BY lastModified DESC LIMIT 1")
    fun observeLatest(): Flow<GameEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHistory(history: GameHistoryEntity)

    @Query("SELECT * FROM game_history ORDER BY completedAt DESC")
    fun observeHistory(): Flow<List<GameHistoryEntity>>
}
