package com.example.killersudoku.domain.usecase

import com.example.killersudoku.domain.model.Game
import com.example.killersudoku.domain.model.GridPosition
import com.example.killersudoku.domain.model.ValidationResult
import com.example.killersudoku.domain.model.valueAt
import com.example.killersudoku.domain.model.withValue
import javax.inject.Inject

class ValidateMoveUseCase @Inject constructor() {
    operator fun invoke(game: Game, position: GridPosition, value: Int): ValidationResult {
        if (game.puzzle.isGiven(position)) {
            return ValidationResult(false, "初始数字不能修改")
        }
        if (value == 0) return ValidationResult(true)
        if (value !in 1..9) return ValidationResult(false, "请输入 1 到 9")

        val grid = game.currentGrid.withValue(position, value)
        duplicateInRow(grid[position.row])?.let {
            return ValidationResult(false, "这一行已经有 $it")
        }

        val column = (0..8).map { row -> grid[row][position.col] }
        duplicateInRow(column)?.let {
            return ValidationResult(false, "这一列已经有 $it")
        }

        val boxValues = boxValues(grid, position)
        duplicateInRow(boxValues)?.let {
            return ValidationResult(false, "这个九宫格已经有 $it")
        }

        val cage = game.puzzle.cageFor(position) ?: return ValidationResult(true)
        val cageValues = cage.cells.map { grid.valueAt(it) }.filter { it != 0 }
        duplicateInRow(cageValues)?.let {
            return ValidationResult(false, "同一个笼区不能重复 $it")
        }

        val cageSum = cageValues.sum()
        val isCageFull = cage.cells.all { grid.valueAt(it) != 0 }
        if (cageSum > cage.targetSum) {
            return ValidationResult(false, "笼区总和不能超过 ${cage.targetSum}")
        }
        if (isCageFull && cageSum != cage.targetSum) {
            return ValidationResult(false, "笼区总和需要等于 ${cage.targetSum}")
        }

        return ValidationResult(true)
    }

    private fun duplicateInRow(values: List<Int>): Int? =
        values.filter { it != 0 }.groupingBy { it }.eachCount().entries.firstOrNull { it.value > 1 }?.key

    private fun boxValues(grid: List<List<Int>>, position: GridPosition): List<Int> {
        val rowStart = position.row / 3 * 3
        val colStart = position.col / 3 * 3
        return (rowStart until rowStart + 3).flatMap { row ->
            (colStart until colStart + 3).map { col -> grid[row][col] }
        }
    }
}
