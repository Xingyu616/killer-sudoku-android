package com.example.killersudoku.data.repository

import com.example.killersudoku.data.local.dao.GameDao
import com.example.killersudoku.data.mapper.toDomain
import com.example.killersudoku.data.mapper.toEntity
import com.example.killersudoku.data.mapper.toHistoryEntity
import com.example.killersudoku.domain.model.Difficulty
import com.example.killersudoku.domain.model.DifficultyStats
import com.example.killersudoku.domain.model.Game
import com.example.killersudoku.domain.model.GameStats
import com.example.killersudoku.domain.repository.GameRepository
import com.example.killersudoku.domain.usecase.GeneratePuzzleUseCase
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

@Singleton
class GameRepositoryImpl @Inject constructor(
    private val gameDao: GameDao,
    private val generatePuzzle: GeneratePuzzleUseCase,
) : GameRepository {
    override fun observeLatestGame(): Flow<Game?> =
        gameDao.observeLatest().map { entity -> entity?.toDomain() }

    override fun observeStats(): Flow<GameStats> =
        gameDao.observeHistory().map { entities ->
            val history = entities.map { it.toDomain() }
            val byDifficulty = history.groupBy { it.difficulty }
            GameStats(
                latestCompleted = history.maxByOrNull { it.completedAt },
                difficultyStats = Difficulty.entries.map { difficulty ->
                    val games = byDifficulty[difficulty].orEmpty()
                    DifficultyStats(
                        difficulty = difficulty,
                        completedGames = games.size,
                        bestTimeMillis = games.minOfOrNull { it.elapsedMillis },
                    )
                },
            )
        }

    override suspend fun createGame(difficulty: Difficulty): Game {
        val puzzle = generatePuzzle(difficulty)
        val now = System.currentTimeMillis()
        val game = Game(
            puzzle = puzzle,
            currentGrid = puzzle.initialGrid,
            startedAt = now,
            lastModified = now,
            elapsedMillis = 0L,
            timerStartedAt = now,
            pausedAt = null,
        )
        val id = gameDao.insert(game.toEntity())
        return game.copy(id = id)
    }

    override suspend fun saveGame(game: Game) {
        gameDao.update(game.toEntity())
    }

    override suspend fun recordCompletedGame(game: Game) {
        game.toHistoryEntity()?.let { gameDao.insertHistory(it) }
    }

    override suspend fun getGame(gameId: Long): Game? =
        gameDao.getById(gameId)?.toDomain()
}
