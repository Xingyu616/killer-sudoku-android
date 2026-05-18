package com.example.killersudoku.domain.model

data class PlayerProgress(
    val coins: Int = 0,
    val lastCheckInDate: String? = null,
    val checkInStreak: Int = 0,
    val lastFirstWinDate: String? = null,
)

enum class RewardTier(
    val title: String,
    val bonusPercent: Int,
) {
    NONE("无评级", 0),
    BRONZE("铜牌", 0),
    SILVER("银牌", 15),
    GOLD("金牌", 35),
    PERFECT("完美", 60),
}

data class RewardResult(
    val coins: Int,
    val tier: RewardTier,
    val baseCoins: Int,
    val timeBonusCoins: Int,
    val firstWinBonusCoins: Int = 0,
    val checkInStreak: Int? = null,
)
