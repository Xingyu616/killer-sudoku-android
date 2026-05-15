package com.example.killersudoku.domain.usecase

import com.example.killersudoku.domain.model.Cage
import com.example.killersudoku.domain.model.Difficulty
import com.example.killersudoku.domain.model.Grid
import com.example.killersudoku.domain.model.GridPosition
import com.example.killersudoku.domain.model.Puzzle
import com.example.killersudoku.domain.model.withValue
import javax.inject.Inject
import kotlin.math.abs
import kotlin.random.Random

class GeneratePuzzleUseCase @Inject constructor(
    private val solver: KillerSudokuSolver,
) {
    operator fun invoke(difficulty: Difficulty): Puzzle {
        val random = Random(System.currentTimeMillis())
        var bestPuzzle: Puzzle? = null

        repeat(MAX_GENERATION_ATTEMPTS + difficulty.level) {
            val solution = generateSolvedGrid(random)
            val cages = createCages(solution, difficulty, random)
            val initial = createInitialGrid(solution, cages, difficulty, random)
            val puzzle = createPuzzle(difficulty, initial, solution, cages)
            if (initial.emptyCellCount() in difficulty.emptyCells) return puzzle

            val best = bestPuzzle
            if (best == null || puzzle.scoreFor(difficulty) < best.scoreFor(difficulty)) {
                bestPuzzle = puzzle
            }
        }

        return bestPuzzle ?: run {
            val solution = generateSolvedGrid(random)
            val cages = createCages(solution, difficulty, random)
            createPuzzle(difficulty, solution, solution, cages)
        }
    }

    private fun generateSolvedGrid(random: Random): Grid {
        val base = List(9) { row ->
            List(9) { col -> ((row * 3 + row / 3 + col) % 9) + 1 }
        }
        val rows = shuffledGroups(random)
        val cols = shuffledGroups(random)
        val digits = (1..9).shuffled(random)
        return rows.map { row ->
            cols.map { col ->
                digits[base[row][col] - 1]
            }
        }
    }

    private fun shuffledGroups(random: Random): List<Int> =
        (0..2).shuffled(random).flatMap { band ->
            (0..2).shuffled(random).map { offset -> band * 3 + offset }
        }

    private fun createInitialGrid(
        solution: Grid,
        cages: List<Cage>,
        difficulty: Difficulty,
        random: Random,
    ): Grid {
        val targetEmptyCount = difficulty.emptyCells.random(random)
        var grid = solution
        val positions = solution.positions().shuffled(random)

        for (position in positions) {
            if (grid.emptyCellCount() >= targetEmptyCount) break

            val candidate = grid.withValue(position, 0)
            if (solver.countSolutions(candidate, cages, limit = 2) == 1) {
                grid = candidate
            }
        }

        return grid
    }

    private fun createCages(solution: Grid, difficulty: Difficulty, random: Random): List<Cage> {
        val layouts = List(CAGE_LAYOUT_ATTEMPTS + difficulty.level) {
            createCageLayout(solution, difficulty, random)
        }
        return layouts.minByOrNull { it.layoutScore(difficulty) }.orEmpty()
    }

    private fun createCageLayout(solution: Grid, difficulty: Difficulty, random: Random): List<Cage> {
        val unvisited = mutableSetOf<GridPosition>().apply {
            repeat(9) { row ->
                repeat(9) { col -> add(GridPosition(row, col)) }
            }
        }
        val cageCells = mutableListOf<List<GridPosition>>()

        while (unvisited.isNotEmpty()) {
            val start = unvisited.minByOrNull { it.unvisitedNeighborCount(unvisited) } ?: break
            val targetSize = randomCageSize(difficulty, random, unvisited.size)
            val cells = mutableListOf(start)
            unvisited.remove(start)

            while (cells.size < targetSize) {
                val usedDigits = cells.map { solution[it.row][it.col] }.toSet()
                val candidates = cells
                    .flatMap { it.neighbors() }
                    .filter { it in unvisited }
                    .filter { solution[it.row][it.col] !in usedDigits }
                    .distinct()
                    .sortedBy { it.unvisitedNeighborCount(unvisited) }

                if (candidates.isEmpty()) break
                val next = candidates.weightedPick(random)
                cells += next
                unvisited.remove(next)
            }

            cageCells += cells
        }

        return mergeSmallCages(cageCells, solution, difficulty, random)
            .mapIndexed { index, cells ->
                Cage(
                    id = index + 1,
                    cells = cells.sortedWith(compareBy<GridPosition> { it.row }.thenBy { it.col }),
                    targetSum = cells.sumOf { solution[it.row][it.col] },
                )
            }
    }

    private fun mergeSmallCages(
        cages: List<List<GridPosition>>,
        solution: Grid,
        difficulty: Difficulty,
        random: Random,
    ): List<List<GridPosition>> {
        val mutable = cages.map { it.toMutableList() }.toMutableList()
        var changed = true
        while (changed) {
            changed = false
            val smallIndex = mutable.indexOfFirst { it.size < difficulty.minCageSize }
            if (smallIndex == -1) break

            val small = mutable[smallIndex]
            val smallDigits = small.map { solution[it.row][it.col] }.toSet()
            val targetIndex = mutable.indices
                .filter { it != smallIndex }
                .filter { index -> mutable[index].size + small.size <= difficulty.maxCageSize }
                .filter { index -> mutable[index].any { cell -> small.any { it.isNeighborOf(cell) } } }
                .filter { index ->
                    mutable[index].map { solution[it.row][it.col] }.none { it in smallDigits }
                }
                .shuffled(random)
                .minByOrNull { mutable[it].size }

            if (targetIndex != null) {
                mutable[targetIndex] += small
                mutable.removeAt(smallIndex)
                changed = true
            } else {
                break
            }
        }
        return mutable
    }

    private fun randomCageSize(difficulty: Difficulty, random: Random, remainingCells: Int): Int {
        if (remainingCells <= difficulty.minCageSize) return remainingCells

        val maxSize = minOf(difficulty.maxCageSize, remainingCells)
        val weightedSizes = (difficulty.minCageSize..maxSize).flatMap { size ->
            val center = when (difficulty.level) {
                in 1..3 -> 2.4
                in 4..6 -> 2.8
                in 7..8 -> 3.1
                else -> 3.4
            }
            val distance = abs(size - center)
            val weight = (8 - (distance * 2).toInt()).coerceAtLeast(1)
            List(weight) { size }
        }
        return weightedSizes.random(random)
    }

    private fun List<Cage>.layoutScore(difficulty: Difficulty): Double {
        if (isEmpty()) return Double.MAX_VALUE

        val averageCombinations = averageCombinations()
        val singletonCount = count { it.cells.size == 1 }
        val largeCageCount = count { it.cells.size >= 4 }
        val averageSize = sumOf { it.cells.size }.toDouble() / size
        val targetSize = when (difficulty.level) {
            in 1..3 -> 2.35
            in 4..6 -> 2.75
            in 7..8 -> 3.05
            else -> 3.25
        }

        return abs(averageCombinations - difficulty.targetAverageCombinations) * 10.0 +
            abs(averageSize - targetSize) * 4.0 +
            singletonCount * if (difficulty.level <= 3) 0.4 else 4.0 -
            largeCageCount * if (difficulty.level >= 7) 0.2 else 0.0
    }

    private fun List<Cage>.averageCombinations(): Double =
        if (isEmpty()) {
            0.0
        } else {
            sumOf { cage -> combinationsFor(cage.targetSum, cage.cells.size).size }.toDouble() / size
        }

    private fun combinationsFor(targetSum: Int, size: Int): List<List<Int>> {
        val results = mutableListOf<List<Int>>()

        fun search(start: Int, remainingSize: Int, remainingSum: Int, current: List<Int>) {
            if (remainingSize == 0) {
                if (remainingSum == 0) results += current
                return
            }
            for (value in start..9) {
                if (value > remainingSum) break
                search(value + 1, remainingSize - 1, remainingSum - value, current + value)
            }
        }

        search(start = 1, remainingSize = size, remainingSum = targetSum, current = emptyList())
        return results
    }

    private fun GridPosition.neighbors(): List<GridPosition> =
        listOf(
            GridPosition(row - 1, col),
            GridPosition(row + 1, col),
            GridPosition(row, col - 1),
            GridPosition(row, col + 1),
        ).filter { it.row in 0..8 && it.col in 0..8 }

    private fun GridPosition.isNeighborOf(other: GridPosition): Boolean =
        abs(row - other.row) + abs(col - other.col) == 1

    private fun GridPosition.unvisitedNeighborCount(unvisited: Set<GridPosition>): Int =
        neighbors().count { it in unvisited }

    private fun <T> List<T>.weightedPick(random: Random): T {
        val index = (random.nextDouble() * random.nextDouble() * size).toInt()
        return this[index.coerceIn(indices)]
    }

    private fun createPuzzle(difficulty: Difficulty, initial: Grid, solution: Grid, cages: List<Cage>): Puzzle =
        Puzzle(
            id = "level-${difficulty.level}-${System.currentTimeMillis()}",
            difficulty = difficulty,
            initialGrid = initial,
            solutionGrid = solution,
            cages = cages,
            createdAt = System.currentTimeMillis(),
        )

    private fun Puzzle.scoreFor(difficulty: Difficulty): Double =
        abs(initialGrid.emptyCellCount() - difficulty.emptyCells.midpoint()) +
            cages.layoutScore(difficulty)

    private fun IntRange.midpoint(): Int = (first + last) / 2

    private fun Grid.emptyCellCount(): Int = sumOf { row -> row.count { it == 0 } }

    private fun Grid.positions(): List<GridPosition> =
        flatMapIndexed { row, values ->
            values.indices.map { col -> GridPosition(row, col) }
        }

    private companion object {
        const val MAX_GENERATION_ATTEMPTS = 8
        const val CAGE_LAYOUT_ATTEMPTS = 16
    }
}
