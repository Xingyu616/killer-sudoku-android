package com.example.killersudoku.domain.model

enum class Difficulty(
    val level: Int,
    val title: String,
    val subtitle: String,
    val emptyCells: IntRange,
    val minCageSize: Int,
    val maxCageSize: Int,
    val targetAverageCombinations: Double,
) {
    LEVEL_1(1, "1级", "更多题面数字，笼区组合更直接", 30..36, 1, 5, 1.6),
    LEVEL_2(2, "2级", "适合熟悉规则和基础排除", 34..40, 1, 5, 1.9),
    LEVEL_3(3, "3级", "开始需要观察行列与笼区交叉", 38..44, 1, 5, 2.2),
    LEVEL_4(4, "4级", "候选组合略多，推理节奏更慢", 42..48, 2, 5, 2.6),
    LEVEL_5(5, "5级", "中等挑战，笼区形状更丰富", 46..52, 2, 5, 3.0),
    LEVEL_6(6, "6级", "需要更频繁使用组合提示", 50..56, 2, 5, 3.5),
    LEVEL_7(7, "7级", "题面数字减少，组合歧义增加", 54..60, 2, 5, 4.1),
    LEVEL_8(8, "8级", "更依赖候选剪枝和跨宫推理", 58..63, 2, 5, 4.7),
    LEVEL_9(9, "9级", "高难度，笼区组合更开放", 61..66, 2, 5, 5.4),
    LEVEL_10(10, "10级", "专家挑战，题面更少且组合更复杂", 64..68, 2, 5, 6.2),
    ;

    companion object
}

fun Difficulty.Companion.fromStoredName(name: String): Difficulty =
    when (name) {
        "EASY" -> Difficulty.LEVEL_2
        "MEDIUM" -> Difficulty.LEVEL_5
        "HARD" -> Difficulty.LEVEL_8
        else -> Difficulty.valueOf(name)
    }
