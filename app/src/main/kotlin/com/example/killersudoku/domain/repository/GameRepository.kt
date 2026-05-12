package com.example.killersudoku.domain.repository

import com.example.killersudoku.domain.model.Difficulty
import com.example.killersudoku.domain.model.Game
import kotlinx.coroutines.flow.Flow

interface GameRepository {
    fun observeLatestGame(): Flow<Game?>

    suspend fun createGame(difficulty: Difficulty): Game

    suspend fun saveGame(game: Game)

    suspend fun getGame(gameId: Long): Game?
}
