package com.example.killersudoku.domain.model

enum class Difficulty(
    val title: String,
    val subtitle: String,
    val emptyCells: IntRange,
    val maxCageSize: Int,
) {
    EASY("简单", "更多初始数字，笼区较大", 32..38, 5),
    MEDIUM("中等", "需要更多候选推理", 46..54, 4),
    HARD("困难", "初始数字少，笼区更碎", 58..64, 3),
}
