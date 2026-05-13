package com.example.killersudoku.domain.usecase

import com.example.killersudoku.domain.model.Cage
import com.example.killersudoku.domain.model.GridPosition
import com.example.killersudoku.domain.model.withValue
import org.junit.Assert.assertEquals
import org.junit.Test

class KillerSudokuSolverTest {
    private val solver = KillerSudokuSolver()

    @Test
    fun solvesSingleCellCagePuzzle() {
        val solution = solvedGrid()
        val cages = singleCellCages(solution)
        val initial = solution.withValue(GridPosition(0, 0), 0)

        val solved = solver.solve(initial, cages)

        assertEquals(solution, solved)
    }

    @Test
    fun returnsNoSolutionsForContradictoryGrid() {
        val solution = solvedGrid()
        val cages = singleCellCages(solution)
        val contradictory = solution.withValue(GridPosition(0, 1), solution[0][0])

        val count = solver.countSolutions(contradictory, cages, limit = 2)

        assertEquals(0, count)
    }

    private fun solvedGrid() =
        List(9) { row ->
            List(9) { col -> ((row * 3 + row / 3 + col) % 9) + 1 }
        }

    private fun singleCellCages(solution: List<List<Int>>): List<Cage> =
        (0..8).flatMap { row ->
            (0..8).map { col ->
                val position = GridPosition(row, col)
                Cage(
                    id = row * 9 + col + 1,
                    cells = listOf(position),
                    targetSum = solution[row][col],
                )
            }
        }
}
