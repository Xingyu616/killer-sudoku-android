package com.example.killersudoku.domain.usecase

import com.example.killersudoku.domain.model.Cage
import com.example.killersudoku.domain.model.Difficulty
import com.example.killersudoku.domain.model.Grid
import com.example.killersudoku.domain.model.GridPosition
import com.example.killersudoku.domain.model.Puzzle
import com.example.killersudoku.domain.model.emptyGrid
import com.example.killersudoku.domain.model.withValue
import javax.inject.Inject
import kotlin.random.Random

class GeneratePuzzleUseCase @Inject constructor() {
    operator fun invoke(difficulty: Difficulty): Puzzle {
        val random = Random(System.currentTimeMillis())
        val solution = generateSolvedGrid(random)
        val initial = createInitialGrid(solution, difficulty, random)
        val cages = createCages(solution, difficulty, random)
        return Puzzle(
            id = "${difficulty.name.lowercase()}-${System.currentTimeMillis()}",
            difficulty = difficulty,
            initialGrid = initial,
            solutionGrid = solution,
            cages = cages,
            createdAt = System.currentTimeMillis(),
        )
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

    private fun createInitialGrid(solution: Grid, difficulty: Difficulty, random: Random): Grid {
        val emptyCount = difficulty.emptyCells.random(random)
        val givens = solution
            .flatMapIndexed { row, values ->
                values.indices.map { col -> GridPosition(row, col) }
            }
            .shuffled(random)
            .drop(emptyCount)
            .toSet()

        return givens.fold(emptyGrid()) { grid, position ->
            grid.withValue(position, solution[position.row][position.col])
        }
    }

    private fun createCages(solution: Grid, difficulty: Difficulty, random: Random): List<Cage> {
        val unvisited = mutableSetOf<GridPosition>().apply {
            repeat(9) { row ->
                repeat(9) { col -> add(GridPosition(row, col)) }
            }
        }
        val cages = mutableListOf<Cage>()
        var nextId = 1

        while (unvisited.isNotEmpty()) {
            val start = unvisited.random(random)
            val targetSize = random.nextInt(1, difficulty.maxCageSize + 1)
            val cells = mutableListOf(start)
            unvisited.remove(start)

            while (cells.size < targetSize) {
                val usedDigits = cells.map { solution[it.row][it.col] }.toSet()
                val candidates = cells
                    .flatMap { it.neighbors() }
                    .filter { it in unvisited }
                    .filter { solution[it.row][it.col] !in usedDigits }
                    .distinct()

                if (candidates.isEmpty()) break
                val next = candidates.random(random)
                cells += next
                unvisited.remove(next)
            }

            cages += Cage(
                id = nextId++,
                cells = cells.sortedWith(compareBy<GridPosition> { it.row }.thenBy { it.col }),
                targetSum = cells.sumOf { solution[it.row][it.col] },
            )
        }

        return cages
    }

    private fun GridPosition.neighbors(): List<GridPosition> =
        listOf(
            GridPosition(row - 1, col),
            GridPosition(row + 1, col),
            GridPosition(row, col - 1),
            GridPosition(row, col + 1),
        ).filter { it.row in 0..8 && it.col in 0..8 }
}
