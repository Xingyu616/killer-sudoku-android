package com.example.killersudoku.data.repository

import com.example.killersudoku.data.local.dao.GameDao
import com.example.killersudoku.data.mapper.toDomain
import com.example.killersudoku.data.mapper.toEntity
import com.example.killersudoku.data.mapper.toHistoryEntity
import com.example.killersudoku.domain.model.Difficulty
import com.example.killersudoku.domain.model.DifficultyStats
import com.example.killersudoku.domain.model.Game
import com.example.killersudoku.domain.model.GameStats
import com.example.killersudoku.domain.model.PlayerProgress
import com.example.killersudoku.domain.model.RewardResult
import com.example.killersudoku.domain.model.RewardTier
import com.example.killersudoku.domain.repository.GameRepository
import com.example.killersudoku.domain.usecase.GeneratePuzzleUseCase
import java.time.LocalDate
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

    override fun observePlayerProgress(): Flow<PlayerProgress> =
        gameDao.observePlayerProgress().map { entity ->
            entity?.toDomain() ?: PlayerProgress()
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

    override suspend fun recordCompletedGame(game: Game): RewardResult? {
        if (gameDao.getHistoryByGameId(game.id) != null) return null
        val reward = completionRewardFor(game)
        game.toHistoryEntity(reward)?.let { gameDao.insertHistory(it) }
        addProgressCoins(reward.coins) { progress ->
            progress.copy(lastFirstWinDate = today().takeIf { reward.firstWinBonusCoins > 0 } ?: progress.lastFirstWinDate)
        }
        return reward
    }

    override suspend fun claimDailyCheckIn(): RewardResult? {
        val now = today()
        val progress = gameDao.getPlayerProgress()?.toDomain() ?: PlayerProgress()
        if (progress.lastCheckInDate == now) return null

        val yesterday = LocalDate.parse(now).minusDays(1).toString()
        val streak = if (progress.lastCheckInDate == yesterday) progress.checkInStreak + 1 else 1
        val streakBonus = ((streak - 1).coerceAtMost(6)) * 5
        val reward = RewardResult(
            coins = 30 + streakBonus,
            tier = RewardTier.NONE,
            baseCoins = 30,
            timeBonusCoins = streakBonus,
            checkInStreak = streak,
        )
        addProgressCoins(reward.coins) {
            it.copy(
                lastCheckInDate = now,
                checkInStreak = streak,
            )
        }
        return reward
    }

    override suspend fun getGame(gameId: Long): Game? =
        gameDao.getById(gameId)?.toDomain()

    private suspend fun addProgressCoins(
        coins: Int,
        update: (PlayerProgress) -> PlayerProgress,
    ) {
        val current = gameDao.getPlayerProgress()?.toDomain() ?: PlayerProgress()
        val updated = update(current.copy(coins = current.coins + coins))
        gameDao.upsertPlayerProgress(updated.toEntity())
    }

    private suspend fun completionRewardFor(game: Game): RewardResult {
        val baseCoins = 20 + game.puzzle.difficulty.level * 5
        val tier = tierFor(game.puzzle.difficulty, game.elapsedMillis)
        val timeBonus = baseCoins * tier.bonusPercent / 100
        val firstWinBonus = if (gameDaoCanAwardFirstWin(game)) 50 else 0
        return RewardResult(
            coins = baseCoins + timeBonus + firstWinBonus,
            tier = tier,
            baseCoins = baseCoins,
            timeBonusCoins = timeBonus,
            firstWinBonusCoins = firstWinBonus,
        )
    }

    private suspend fun gameDaoCanAwardFirstWin(game: Game): Boolean {
        val progress = gameDao.getPlayerProgress()?.toDomain() ?: PlayerProgress()
        return progress.lastFirstWinDate != today() && game.completedAt != null
    }

    private fun tierFor(difficulty: Difficulty, elapsedMillis: Long): RewardTier {
        val minutes = (elapsedMillis / 60_000L).coerceAtLeast(0L)
        val target = 4L + difficulty.level * 2L
        return when {
            minutes <= target -> RewardTier.PERFECT
            minutes <= target + 4L -> RewardTier.GOLD
            minutes <= target + 9L -> RewardTier.SILVER
            else -> RewardTier.BRONZE
        }
    }

    private fun today(): String = LocalDate.now().toString()
}
