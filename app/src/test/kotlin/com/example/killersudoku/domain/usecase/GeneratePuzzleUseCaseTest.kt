package com.example.killersudoku.domain.usecase

import com.example.killersudoku.domain.model.Difficulty
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GeneratePuzzleUseCaseTest {
    private val solver = KillerSudokuSolver()
    private val generatePuzzle = GeneratePuzzleUseCase(solver)

    @Test
    fun generatedPuzzleHasValidShapeAndCages() {
        val puzzle = generatePuzzle(Difficulty.LEVEL_5)
        val cageCells = puzzle.cages.flatMap { it.cells }

        assertEquals(9, puzzle.solutionGrid.size)
        assertTrue(puzzle.solutionGrid.all { it.size == 9 })
        assertEquals(81, cageCells.size)
        assertEquals(81, cageCells.toSet().size)

        puzzle.cages.forEach { cage ->
            val sum = cage.cells.sumOf { puzzle.solutionGrid[it.row][it.col] }
            assertEquals(sum, cage.targetSum)
        }
    }

    @Test
    fun generatedPuzzleHasUniqueSolution() {
        val puzzle = generatePuzzle(Difficulty.LEVEL_2)

        val solutionCount = solver.countSolutions(puzzle.initialGrid, puzzle.cages, limit = 2)

        assertEquals(1, solutionCount)
    }

    @Test
    fun generatedPuzzleSupportsTenDifficultyLevels() {
        assertEquals(10, Difficulty.entries.size)

        Difficulty.entries.forEachIndexed { index, difficulty ->
            assertEquals(index + 1, difficulty.level)
            assertTrue(difficulty.minCageSize <= difficulty.maxCageSize)
        }

        listOf(Difficulty.LEVEL_1, Difficulty.LEVEL_5, Difficulty.LEVEL_10).forEach { difficulty ->
            val puzzle = generatePuzzle(difficulty)

            assertEquals(difficulty, puzzle.difficulty)
            assertTrue(puzzle.initialGrid.emptyCellCount() > 0)
            assertTrue(puzzle.cages.all { it.cells.size <= difficulty.maxCageSize })
        }
    }

    private fun List<List<Int>>.emptyCellCount(): Int =
        sumOf { row -> row.count { it == 0 } }
}
