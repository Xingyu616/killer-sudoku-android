package com.example.killersudoku.domain.repository

import com.example.killersudoku.domain.model.Difficulty
import com.example.killersudoku.domain.model.Game
import com.example.killersudoku.domain.model.GameStats
import kotlinx.coroutines.flow.Flow

interface GameRepository {
    fun observeLatestGame(): Flow<Game?>

    fun observeStats(): Flow<GameStats>

    suspend fun createGame(difficulty: Difficulty): Game

    suspend fun saveGame(game: Game)

    suspend fun recordCompletedGame(game: Game)

    suspend fun getGame(gameId: Long): Game?
}
