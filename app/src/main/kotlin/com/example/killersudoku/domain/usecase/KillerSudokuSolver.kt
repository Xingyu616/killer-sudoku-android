package com.example.killersudoku.domain.usecase

import com.example.killersudoku.domain.model.Cage
import com.example.killersudoku.domain.model.Grid
import com.example.killersudoku.domain.model.GridPosition
import javax.inject.Inject

class KillerSudokuSolver @Inject constructor() {
    fun solve(initialGrid: Grid, cages: List<Cage>): Grid? =
        collectSolutions(initialGrid, cages, limit = 1).firstOrNull()

    fun countSolutions(initialGrid: Grid, cages: List<Cage>, limit: Int = 2): Int =
        collectSolutions(initialGrid, cages, limit).size

    private fun collectSolutions(initialGrid: Grid, cages: List<Cage>, limit: Int): List<Grid> {
        if (!initialGrid.hasValidShape()) return emptyList()

        val grid = initialGrid.toMutableGrid()
        val cageByPosition = cages.flatMap { cage -> cage.cells.map { it to cage } }.toMap()
        if (!grid.isConsistent(cages, cageByPosition)) return emptyList()

        val solutions = mutableListOf<Grid>()
        search(grid, cageByPosition, solutions, limit.coerceAtLeast(1))
        return solutions
    }

    private fun search(
        grid: Array<IntArray>,
        cageByPosition: Map<GridPosition, Cage>,
        solutions: MutableList<Grid>,
        limit: Int,
    ) {
        if (solutions.size >= limit) return

        val next = selectNextPosition(grid, cageByPosition) ?: run {
            solutions += grid.toGrid()
            return
        }
        val (position, candidates) = next
        if (candidates.isEmpty()) return

        for (value in candidates) {
            grid[position.row][position.col] = value
            search(grid, cageByPosition, solutions, limit)
            grid[position.row][position.col] = 0
            if (solutions.size >= limit) return
        }
    }

    private fun selectNextPosition(
        grid: Array<IntArray>,
        cageByPosition: Map<GridPosition, Cage>,
    ): Pair<GridPosition, List<Int>>? {
        var best: Pair<GridPosition, List<Int>>? = null
        repeat(9) { row ->
            repeat(9) { col ->
                if (grid[row][col] == 0) {
                    val position = GridPosition(row, col)
                    val candidates = candidatesFor(grid, position, cageByPosition)
                    val currentBest = best
                    if (currentBest == null || candidates.size < currentBest.second.size) {
                        best = position to candidates
                    }
                    if (candidates.isEmpty()) return best
                }
            }
        }
        return best
    }

    private fun candidatesFor(
        grid: Array<IntArray>,
        position: GridPosition,
        cageByPosition: Map<GridPosition, Cage>,
    ): List<Int> =
        (1..9).filter { value -> canPlace(grid, position, value, cageByPosition[position]) }

    private fun canPlace(
        grid: Array<IntArray>,
        position: GridPosition,
        value: Int,
        cage: Cage?,
    ): Boolean {
        repeat(9) { index ->
            if (grid[position.row][index] == value) return false
            if (grid[index][position.col] == value) return false
        }

        val rowStart = position.row / 3 * 3
        val colStart = position.col / 3 * 3
        for (row in rowStart until rowStart + 3) {
            for (col in colStart until colStart + 3) {
                if (grid[row][col] == value) return false
            }
        }

        if (cage == null) return true

        val usedValues = cage.cells
            .filterNot { it == position }
            .map { grid[it.row][it.col] }
            .filter { it != 0 }
            .toSet()
        if (value in usedValues) return false

        val sumWithValue = usedValues.sum() + value
        if (sumWithValue > cage.targetSum) return false

        val remainingCells = cage.cells.count { it != position && grid[it.row][it.col] == 0 }
        if (remainingCells == 0) return sumWithValue == cage.targetSum

        val available = (1..9).filter { it != value && it !in usedValues }
        if (available.size < remainingCells) return false

        val remainingTarget = cage.targetSum - sumWithValue
        val minPossible = available.take(remainingCells).sum()
        val maxPossible = available.takeLast(remainingCells).sum()
        return remainingTarget in minPossible..maxPossible
    }

    private fun Array<IntArray>.isConsistent(
        cages: List<Cage>,
        cageByPosition: Map<GridPosition, Cage>,
    ): Boolean {
        repeat(9) { row ->
            if (hasDuplicate((0..8).map { col -> this[row][col] })) return false
        }
        repeat(9) { col ->
            if (hasDuplicate((0..8).map { row -> this[row][col] })) return false
        }
        for (rowStart in listOf(0, 3, 6)) {
            for (colStart in listOf(0, 3, 6)) {
                val values = (rowStart until rowStart + 3).flatMap { row ->
                    (colStart until colStart + 3).map { col -> this[row][col] }
                }
                if (hasDuplicate(values)) return false
            }
        }

        if (cageByPosition.size != cages.sumOf { it.cells.size }) return false
        cages.forEach { cage ->
            val values = cage.cells.map { this[it.row][it.col] }.filter { it != 0 }
            if (hasDuplicate(values)) return false
            val sum = values.sum()
            if (sum > cage.targetSum) return false
            if (values.size == cage.cells.size && sum != cage.targetSum) return false
        }
        return true
    }

    private fun hasDuplicate(values: List<Int>): Boolean {
        val filled = values.filter { it != 0 }
        return filled.size != filled.toSet().size
    }

    private fun Grid.hasValidShape(): Boolean =
        size == 9 && all { row -> row.size == 9 && row.all { it in 0..9 } }

    private fun Grid.toMutableGrid(): Array<IntArray> =
        Array(9) { row -> IntArray(9) { col -> this[row][col] } }

    private fun Array<IntArray>.toGrid(): Grid =
        List(9) { row -> List(9) { col -> this[row][col] } }
}
