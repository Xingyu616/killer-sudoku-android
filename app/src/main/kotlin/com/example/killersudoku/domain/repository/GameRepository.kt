package com.example.killersudoku.domain.repository

import com.example.killersudoku.domain.model.Difficulty
import com.example.killersudoku.domain.model.Game
import com.example.killersudoku.domain.model.GameStats
import com.example.killersudoku.domain.model.PlayerProgress
import com.example.killersudoku.domain.model.RewardResult
import kotlinx.coroutines.flow.Flow

interface GameRepository {
    fun observeLatestGame(): Flow<Game?>

    fun observeStats(): Flow<GameStats>

    fun observePlayerProgress(): Flow<PlayerProgress>

    suspend fun createGame(difficulty: Difficulty): Game

    suspend fun saveGame(game: Game)

    suspend fun recordCompletedGame(game: Game): RewardResult?

    suspend fun claimDailyCheckIn(): RewardResult?

    suspend fun getGame(gameId: Long): Game?
}
