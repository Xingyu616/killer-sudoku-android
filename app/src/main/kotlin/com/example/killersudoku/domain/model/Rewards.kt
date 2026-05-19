package com.example.killersudoku.domain.model

data class PlayerProgress(
    val coins: Int = 0,
    val hintTickets: Int = 0,
    val lastCheckInDate: String? = null,
    val checkInStreak: Int = 0,
    val lastFirstWinDate: String? = null,
    val bgmEnabled: Boolean = true,
    val soundEnabled: Boolean = true,
    val autoClearNotes: Boolean = true,
    val errorHighlightEnabled: Boolean = true,
    val selectedTheme: BoardTheme = BoardTheme.DEFAULT,
    val unlockedThemes: Set<BoardTheme> = setOf(BoardTheme.DEFAULT),
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

enum class BoardTheme(
    val title: String,
    val unlockCost: Int,
) {
    DEFAULT("默认", 0),
    EYE_CARE("护眼", 80),
    NIGHT("夜间", 120),
    PAPER("纸张", 100),
}

data class HintSpendResult(
    val coinsSpent: Int,
    val usedTicket: Boolean,
)

const val HINT_TICKET_COST = 30
const val HINT_COIN_COST = 20
