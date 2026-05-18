package com.example.killersudoku.domain.model

data class GameHistory(
    val gameId: Long,
    val difficulty: Difficulty,
    val startedAt: Long,
    val completedAt: Long,
    val elapsedMillis: Long,
    val usedHint: Boolean,
    val usedSolve: Boolean,
    val rewardCoins: Int = 0,
    val rewardTier: RewardTier = RewardTier.NONE,
)

data class DifficultyStats(
    val difficulty: Difficulty,
    val completedGames: Int,
    val bestTimeMillis: Long?,
)

data class GameStats(
    val latestCompleted: GameHistory? = null,
    val difficultyStats: List<DifficultyStats> = Difficulty.entries.map {
        DifficultyStats(
            difficulty = it,
            completedGames = 0,
            bestTimeMillis = null,
        )
    },
)
