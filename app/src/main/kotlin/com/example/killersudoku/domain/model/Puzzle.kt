package com.example.killersudoku.domain.model

data class Puzzle(
    val id: String,
    val difficulty: Difficulty,
    val initialGrid: Grid,
    val solutionGrid: Grid,
    val cages: List<Cage>,
    val createdAt: Long,
) {
    fun cageFor(position: GridPosition): Cage? = cages.firstOrNull { it.contains(position) }

    fun isGiven(position: GridPosition): Boolean = initialGrid.valueAt(position) != 0
}
